package com.example.evfunenhancer.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class FirestoreRepository {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    suspend fun signInAnonymously() {
        if (auth.currentUser == null) {
            auth.signInAnonymously().await()
        }
    }

    fun getUid(): String = auth.currentUser!!.uid

    // Emits the current UID whenever Firebase auth state changes (null = signed out).
    fun observeAuthState(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { fbAuth ->
            trySend(fbAuth.currentUser?.uid)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    // Shows are global read-only data; structure unchanged from original.
    fun getShows(): Flow<Map<String, List<Participant>>> = callbackFlow {
        val listener = db.collection("shows")
            .addSnapshotListener { snapshot, _ ->
                val result = mutableMapOf<String, List<Participant>>()
                snapshot?.documents?.forEach { doc ->
                    @Suppress("UNCHECKED_CAST")
                    val raw = doc.get("participants") as? List<Map<String, Any>> ?: emptyList()
                    result[doc.id] = raw.map { m ->
                        Participant(
                            order = (m["order"] as? Long)?.toInt() ?: 0,
                            country = m["country"] as? String ?: "",
                            artist = m["artist"] as? String ?: "",
                            song = m["song"] as? String ?: ""
                        )
                    }.sortedBy { it.order }
                }
                trySend(result)
            }
        awaitClose { listener.remove() }
    }

    fun observeFirestoreConnectivity(): Flow<Boolean?> = callbackFlow {
        val listener = db.collection("shows")
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, _ ->
                if (snapshot != null) trySend(!snapshot.metadata.isFromCache)
            }
        awaitClose { listener.remove() }
    }

    // Results are global read-only data; unchanged from original.
    // Uses two live snapshot listeners (document + entries sub-collection) so that
    // ShowResults is only emitted once both the year field AND entries are present.
    // This prevents the race where the parent document lands before entries are written.
    fun watchResults(showId: String): Flow<ShowResults?> = callbackFlow {
        var cachedYear: Int? = null
        // null = entries listener hasn't fired yet; emptyList = fired but no entries
        var cachedEntries: List<CountryResult>? = null

        fun tryEmit() {
            val year = cachedYear
            val entries = cachedEntries
            if (year != null && !entries.isNullOrEmpty()) {
                trySend(ShowResults(year, entries))
            }
            // null is sent explicitly from the doc listener when the document doesn't exist;
            // no null emitted here to avoid a flash when listeners fire in entries-first order.
        }

        fun parseEntries(snapshot: com.google.firebase.firestore.QuerySnapshot) =
            snapshot.documents.mapNotNull { doc ->
                val order = doc.id.toIntOrNull() ?: return@mapNotNull null
                CountryResult(
                    order       = order,
                    rank        = doc.getLong("rank")?.toInt()        ?: return@mapNotNull null,
                    juryScore   = doc.getLong("juryScore")?.toInt()   ?: return@mapNotNull null,
                    publicScore = doc.getLong("publicScore")?.toInt() ?: return@mapNotNull null,
                )
            }

        val entriesListener = db.collection("results").document(showId)
            .collection("entries")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener
                cachedEntries = parseEntries(snapshot)
                tryEmit()
            }

        val docListener = db.collection("results").document(showId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null || !snapshot.exists()) {
                    cachedYear = null
                    cachedEntries = null
                    trySend(null)
                    return@addSnapshotListener
                }
                cachedYear = snapshot.getLong("year")?.toInt()
                tryEmit()
            }

        awaitClose {
            docListener.remove()
            entriesListener.remove()
        }
    }

    // -------------------------------------------------------------------------
    // Room operations
    // -------------------------------------------------------------------------

    private val CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

    private fun generateRoomCode(): String =
        (1..6).map { CODE_ALPHABET.random() }.joinToString("")

    suspend fun createRoom(username: String): Result<String> = try {
        val uid = getUid()
        var roomCode: String? = null
        repeat(5) {
            if (roomCode != null) return@repeat
            val candidate = generateRoomCode()
            val roomRef = db.collection("rooms").document(candidate)
            try {
                db.runTransaction { tx ->
                    if (tx.get(roomRef).exists()) throw Exception("collision")
                    tx.set(roomRef, mapOf(
                        "createdAt" to Timestamp.now(),
                        "lastActivityAt" to Timestamp.now(),
                        "creatorUid" to uid
                    ))
                    tx.set(
                        roomRef.collection("members").document(uid),
                        mapOf("username" to username, "joinedAt" to Timestamp.now())
                    )
                    // Write username lock doc for uniqueness enforcement
                    tx.set(
                        roomRef.collection("usernames").document(username.lowercase()),
                        mapOf("uid" to uid)
                    )
                }.await()
                roomCode = candidate
            } catch (_: Exception) { /* collision or transient error; retry */ }
        }
        roomCode?.let { Result.success(it) }
            ?: Result.failure(Exception("Could not generate unique room code"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun joinRoom(roomCode: String, username: String): Result<Unit> = try {
        val uid = getUid()
        val roomRef = db.collection("rooms").document(roomCode)
        if (!roomRef.get().await().exists()) throw Exception("Room not found")
        val usernameRef = roomRef.collection("usernames").document(username.lowercase())
        val memberRef = roomRef.collection("members").document(uid)
        db.runTransaction { tx ->
            val usernameDoc = tx.get(usernameRef)
            val memberDoc = tx.get(memberRef)
            if (usernameDoc.exists() && usernameDoc.getString("uid") != uid)
                throw Exception("Username already taken in this room")
            val joinedAt = if (memberDoc.exists()) memberDoc.getTimestamp("joinedAt") else null
            tx.set(usernameRef, mapOf("uid" to uid))
            tx.set(memberRef, mapOf("username" to username, "joinedAt" to (joinedAt ?: Timestamp.now())))
        }.await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Returns the room's current lastActivityAt so the caller can decide whether to update it.
    suspend fun verifyMembership(roomCode: String): Result<Timestamp?> = try {
        val uid = getUid()
        val memberDoc = db.collection("rooms").document(roomCode)
            .collection("members").document(uid).get().await()
        if (!memberDoc.exists()) throw Exception("Not a member of room $roomCode")
        val lastActivity = db.collection("rooms").document(roomCode)
            .get().await().getTimestamp("lastActivityAt")
        Result.success(lastActivity)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateLastActivityAt(roomCode: String, currentLastActivityAt: Timestamp?) {
        val twentyFourHoursAgo = Timestamp(Timestamp.now().seconds - 86400, 0)
        if (currentLastActivityAt != null &&
            currentLastActivityAt.seconds > twentyFourHoursAgo.seconds) return
        try {
            db.collection("rooms").document(roomCode)
                .update("lastActivityAt", Timestamp.now()).await()
        } catch (_: Exception) { /* best-effort */ }
    }

    fun getMembers(roomCode: String): Flow<Map<String, String>> = callbackFlow {
        val listener = db.collection("rooms").document(roomCode)
            .collection("members")
            .addSnapshotListener { snapshot, _ ->
                val result = snapshot?.documents?.associate { doc ->
                    doc.id to (doc.getString("username") ?: "")
                } ?: emptyMap()
                trySend(result)
            }
        awaitClose { listener.remove() }
    }

    fun getCreatorUid(roomCode: String): Flow<String?> = callbackFlow {
        val listener = db.collection("rooms").document(roomCode)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.getString("creatorUid"))
            }
        awaitClose { listener.remove() }
    }

    private val SHOW_IDS = listOf("sf1", "sf2", "final")

    suspend fun removeMember(roomCode: String, uidToRemove: String, usernameToRemove: String): Result<Unit> = try {
        val roomRef = db.collection("rooms").document(roomCode)
        val batch = db.batch()

        batch.delete(roomRef.collection("members").document(uidToRemove))
        batch.delete(roomRef.collection("usernames").document(usernameToRemove.lowercase()))

        for (showId in SHOW_IDS) {
            val entries = roomRef.collection("votes").document(showId)
                .collection("entries").get().await()
            for (entryDoc in entries.documents) {
                if (entryDoc.contains(uidToRemove))
                    batch.update(entryDoc.reference, uidToRemove, FieldValue.delete())
            }
            batch.delete(
                roomRef.collection("guesses").document(showId)
                    .collection("picks").document(uidToRemove)
            )
        }

        batch.commit().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun renameUser(roomCode: String, newUsername: String): Result<Unit> = try {
        val uid = getUid()
        val roomRef = db.collection("rooms").document(roomCode)
        val memberRef = roomRef.collection("members").document(uid)
        val oldUsername = memberRef.get().await().getString("username") ?: ""
        val newUsernameRef = roomRef.collection("usernames").document(newUsername.lowercase())
        val oldUsernameRef = roomRef.collection("usernames").document(oldUsername.lowercase())
        db.runTransaction { tx ->
            if (tx.get(newUsernameRef).exists()) throw Exception("Username already taken in this room")
            tx.delete(oldUsernameRef)
            tx.set(newUsernameRef, mapOf("uid" to uid))
            tx.update(memberRef, "username", newUsername)
        }.await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // -------------------------------------------------------------------------
    // Votes (room-scoped, keyed by UID)
    // -------------------------------------------------------------------------

    fun getVotes(roomCode: String, showId: String): Flow<Map<Int, Map<String, Int>>> = callbackFlow {
        val listener = db.collection("rooms").document(roomCode)
            .collection("votes").document(showId).collection("entries")
            .addSnapshotListener { snapshot, _ ->
                val result = mutableMapOf<Int, Map<String, Int>>()
                snapshot?.documents?.forEach { doc ->
                    val order = doc.id.toIntOrNull() ?: return@forEach
                    result[order] = doc.data
                        ?.filterValues { it is Long }
                        ?.mapValues { (_, v) -> (v as Long).toInt() }
                        ?: emptyMap()
                }
                trySend(result)
            }
        awaitClose { listener.remove() }
    }

    suspend fun submitVote(roomCode: String, showId: String, order: Int, uid: String, points: Int) {
        db.collection("rooms").document(roomCode)
            .collection("votes").document(showId)
            .collection("entries").document(order.toString())
            .set(mapOf(uid to points), SetOptions.merge())
            .await()
    }

    // -------------------------------------------------------------------------
    // Guesses (room-scoped, keyed by UID)
    // -------------------------------------------------------------------------

    fun getGuesses(roomCode: String, showId: String): Flow<Map<String, Map<Int, Int>>> = callbackFlow {
        val listener = db.collection("rooms").document(roomCode)
            .collection("guesses").document(showId).collection("picks")
            .addSnapshotListener { snapshot, _ ->
                val result = mutableMapOf<String, Map<Int, Int>>()
                snapshot?.documents?.forEach { doc ->
                    val picks = mutableMapOf<Int, Int>()
                    doc.data?.forEach { (k, v) ->
                        val rank = k.toIntOrNull() ?: return@forEach
                        val order = (v as? Long)?.toInt() ?: return@forEach
                        picks[rank] = order
                    }
                    if (picks.isNotEmpty()) result[doc.id] = picks
                }
                trySend(result)
            }
        awaitClose { listener.remove() }
    }

    suspend fun setGuess(roomCode: String, showId: String, uid: String, rank: Int, participantOrder: Int) {
        db.collection("rooms").document(roomCode)
            .collection("guesses").document(showId)
            .collection("picks").document(uid)
            .set(mapOf(rank.toString() to participantOrder), SetOptions.merge())
            .await()
    }

    suspend fun removeGuess(roomCode: String, showId: String, uid: String, rank: Int) {
        db.collection("rooms").document(roomCode)
            .collection("guesses").document(showId)
            .collection("picks").document(uid)
            .update(rank.toString(), FieldValue.delete())
            .await()
    }
}
