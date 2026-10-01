package com.example.evfunenhancer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.evfunenhancer.BuildConfig
import com.example.evfunenhancer.data.FirestoreRepository
import com.example.evfunenhancer.data.Participant
import com.example.evfunenhancer.data.PrefsStore
import com.example.evfunenhancer.data.ShowResults
import com.example.evfunenhancer.data.UpdateCheckResult
import com.example.evfunenhancer.data.checkForUpdate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FirestoreRepository()
    private val prefs = PrefsStore(application)

    private val _authReady = MutableStateFlow(false)
    val authReady: StateFlow<Boolean> = _authReady.asStateFlow()

    // True once the startup sequence is fully resolved: auth done, membership verified,
    // and (if in a room) the first members batch has arrived from Firestore.
    private val _startupComplete = MutableStateFlow(false)
    val startupComplete: StateFlow<Boolean> = _startupComplete.asStateFlow()

    private val _disclaimerAccepted = MutableStateFlow(prefs.hasAcceptedDisclaimer())
    val disclaimerAccepted: StateFlow<Boolean> = _disclaimerAccepted.asStateFlow()

    fun acceptDisclaimer() {
        prefs.setDisclaimerAccepted()
        _disclaimerAccepted.value = true
    }

    // The Aftershow plays as a one-slide-at-a-time story until it has been gone through once per results year.
    fun hasSeenAftershowStory(year: Int): Boolean = prefs.hasSeenAftershowStory(year)
    fun markAftershowStorySeen(year: Int) = prefs.setAftershowStorySeen(year)

    // Bumped by the developer toggle so an open Aftershow screen re-reads the flag.
    private val _aftershowStoryVersion = MutableStateFlow(0)
    val aftershowStoryVersion: StateFlow<Int> = _aftershowStoryVersion.asStateFlow()

    /** Developer shortcut: flips the current results year between story and browse mode. Returns the new "seen" state, or null without results. */
    fun toggleAftershowStorySeen(): Boolean? {
        val year = results.value?.year ?: return null
        val seen = !prefs.hasSeenAftershowStory(year)
        prefs.setAftershowStorySeen(year, seen)
        _aftershowStoryVersion.value++
        return seen
    }

    private val _username = MutableStateFlow<String?>(null)
    val username: StateFlow<String?> = _username.asStateFlow()

    private val _roomCode = MutableStateFlow<String?>(null)
    val roomCode: StateFlow<String?> = _roomCode.asStateFlow()

    private val _selectedShowId = MutableStateFlow<String?>(null)
    val selectedShowId: StateFlow<String?> = _selectedShowId.asStateFlow()

    private val _language = MutableStateFlow(prefs.getLanguage())
    val language: StateFlow<String> = _language.asStateFlow()

    val myUid: String? get() = if (_authReady.value) repository.getUid() else null

    // Prefs username may survive a lost room — used to pre-fill the create/join form.
    val savedUsername: String? get() = prefs.getUsername()

    // Last room code the user joined — used to pre-fill the Join Room field.
    val savedRoomCode: String? get() = prefs.getLastJoinedRoomCode()

    fun observeFirestoreConnectivity(): Flow<Boolean?> =
        repository.observeFirestoreConnectivity()

    val members: StateFlow<Map<String, String>> = _roomCode
        .flatMapLatest { code ->
            if (code != null) repository.getMembers(code) else flowOf(emptyMap())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val creatorUid: StateFlow<String?> = _roomCode
        .flatMapLatest { code ->
            if (code != null) repository.getCreatorUid(code) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isRoomCreator: StateFlow<Boolean> = combine(creatorUid, _authReady) { cuid, ready ->
        ready && cuid != null && try { cuid == repository.getUid() } catch (_: Exception) { false }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val shows: StateFlow<Map<String, List<Participant>>> = _authReady
        .flatMapLatest { ready -> if (ready) repository.getShows() else flowOf(emptyMap()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Eagerly so the listeners start as soon as a room and show are set, not when the Points or
    // Summary screen first subscribes. Otherwise opening Summary first renders every country at
    // 0 points and then animates the whole list into its sorted order when the votes arrive.
    val votes: StateFlow<Map<Int, Map<String, Int>>> =
        combine(_roomCode, _selectedShowId) { code, showId -> code to showId }
            .flatMapLatest { (code, showId) ->
                if (code != null && showId != null) repository.getVotes(code, showId)
                else flowOf(emptyMap())
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val guesses: StateFlow<Map<String, Map<Int, Int>>> =
        combine(_roomCode, _selectedShowId) { code, showId -> code to showId }
            .flatMapLatest { (code, showId) ->
                if (code != null && showId != null) repository.getGuesses(code, showId)
                else flowOf(emptyMap())
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    // Always watch final-show votes/guesses, regardless of selected show — used by AfterShowScreen.
    // Eagerly so the Firestore listener starts as soon as a room is joined, avoiding a race where
    // results arrive before the first vote snapshot when the user first opens the Aftershow screen.
    val finalVotes: StateFlow<Map<Int, Map<String, Int>>> = _roomCode
        .flatMapLatest { code ->
            if (code != null) repository.getVotes(code, "final") else flowOf(emptyMap())
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val finalGuesses: StateFlow<Map<String, Map<Int, Int>>> = _roomCode
        .flatMapLatest { code ->
            if (code != null) repository.getGuesses(code, "final") else flowOf(emptyMap())
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    // Only the Aftershow's Chatterbox award reads this, so it listens only while that screen is open.
    val finalComments: StateFlow<Map<Int, Map<String, String>>> = _roomCode
        .flatMapLatest { code ->
            if (code != null) repository.getComments(code, "final") else flowOf(emptyMap())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val comments: StateFlow<Map<Int, Map<String, String>>> =
        combine(_roomCode, _selectedShowId) { code, showId -> code to showId }
            .flatMapLatest { (code, showId) ->
                if (code != null && showId != null) repository.getComments(code, showId)
                else flowOf(emptyMap())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Key for one member's comment on one entry of the selected show.
    data class CommentKey(val order: Int, val uid: String)

    // When this device first saw each comment (or its latest edit) arrive, in epoch millis.
    // There is no server timestamp, so this is the only ordering signal: comments already
    // present in the baseline snapshot (app start or show switch) have no entry and are
    // left out of the feed's Latest tab.
    private val _commentSeenAt = MutableStateFlow<Map<CommentKey, Long>>(emptyMap())
    val commentSeenAt: StateFlow<Map<CommentKey, Long>> = _commentSeenAt.asStateFlow()

    // Other members' comments that arrived live and haven't been looked at yet (via the
    // entry's vote dialog or the feed).
    private val _unreadComments = MutableStateFlow<Set<CommentKey>>(emptySet())
    val unreadComments: StateFlow<Set<CommentKey>> = _unreadComments.asStateFlow()

    fun markCommentsRead(order: Int) {
        _unreadComments.value = _unreadComments.value.filterNot { it.order == order }.toSet()
    }

    fun markAllCommentsRead() {
        _unreadComments.value = emptySet()
    }

    // One comment snapshot compared with the previous one for the same room/show.
    // baseline = first snapshot after a room/show change; nothing in it counts as new.
    private data class CommentDiff(
        val baseline: Boolean,
        val changed: List<CommentKey>,
        val removed: List<CommentKey>
    )

    // Diffs successive comment snapshots for the selected show to detect additions/edits.
    // Declared as its own combine+flatMapLatest (rather than derived from `comments`) so
    // `previous` resets cleanly on every room/show change: it's a local var inside the
    // flatMapLatest lambda, scoped to that inner subscription's lifetime, so the first
    // snapshot after a show switch is always treated as a baseline, never as a burst of
    // "new" (unread) comments.
    private val commentDiffs: Flow<CommentDiff> =
        combine(_roomCode, _selectedShowId) { code, showId -> code to showId }
            .flatMapLatest { (code, showId) ->
                // No room/show: emit a baseline so seen-at and unread state are cleared.
                if (code == null || showId == null) {
                    return@flatMapLatest flowOf(CommentDiff(true, emptyList(), emptyList()))
                }
                var previous: Map<Int, Map<String, String>>? = null
                repository.getComments(code, showId).map { current ->
                    val prev = previous
                    previous = current
                    if (prev == null) return@map CommentDiff(true, emptyList(), emptyList())
                    val changed = mutableListOf<CommentKey>()
                    current.forEach { (order, byUid) ->
                        byUid.forEach { (uid, text) ->
                            if (prev[order]?.get(uid) != text) changed += CommentKey(order, uid)
                        }
                    }
                    val removed = prev.flatMap { (order, byUid) ->
                        byUid.keys.filter { current[order]?.containsKey(it) != true }
                            .map { CommentKey(order, it) }
                    }
                    CommentDiff(false, changed, removed)
                }
            }

    private fun applyCommentDiff(diff: CommentDiff) {
        if (diff.baseline) {
            _commentSeenAt.value = emptyMap()
            _unreadComments.value = emptySet()
            return
        }
        val now = System.currentTimeMillis()
        val myUid = try { repository.getUid() } catch (_: Exception) { null }
        _commentSeenAt.value = _commentSeenAt.value - diff.removed.toSet() +
            diff.changed.map { it to now }
        _unreadComments.value = _unreadComments.value - diff.removed.toSet() +
            diff.changed.filter { it.uid != myUid }
    }

    val results: StateFlow<ShowResults?> = _authReady
        .flatMapLatest { ready -> if (ready) repository.watchResults("final") else flowOf(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _updateInfo = MutableStateFlow<UpdateCheckResult>(UpdateCheckResult.Pending)
    val updateInfo: StateFlow<UpdateCheckResult> = _updateInfo.asStateFlow()

    init {
        // Watch for the auth user being dropped mid-session (e.g. deleted from Firebase console).
        // _authReady guards against triggering recovery before the initial sign-in completes.
        viewModelScope.launch {
            repository.observeAuthState().collect { uid ->
                if (uid == null && _authReady.value) {
                    repository.signInAnonymously()
                    // New UID is not a member of any room; mirror what startup does on failed membership.
                    _roomCode.value = null
                    _username.value = null
                    _selectedShowId.value = null
                    prefs.setRoomCode(null)
                    // Username and show ID kept in prefs for pre-fill when the user rejoins.
                }
            }
        }

        viewModelScope.launch {
            // signInAnonymously() needs a network call the very first time. If there is no
            // internet on first launch, it throws — retry until it succeeds. Set startupComplete
            // on the first failure so the UI renders in offline mode instead of hanging.
            while (true) {
                try {
                    repository.signInAnonymously()
                    break
                } catch (_: Exception) {
                    if (!_startupComplete.value) _startupComplete.value = true
                    delay(5_000L)
                }
            }
            _authReady.value = true

            val savedRoomCode = prefs.getRoomCode()
            if (savedRoomCode != null) {
                val result = repository.verifyMembership(savedRoomCode)
                if (result.isSuccess) {
                    _roomCode.value = savedRoomCode
                    _username.value = prefs.getUsername()
                    prefs.getShowId()?.let { _selectedShowId.value = it }
                    viewModelScope.launch {
                        repository.updateLastActivityAt(savedRoomCode, result.getOrNull())
                    }
                    // Wait for the first non-empty members batch before letting the UI render.
                    // This prevents the active state from flashing in before pills are ready.
                    viewModelScope.launch {
                        withTimeoutOrNull(10_000L) { members.first { it.isNotEmpty() } }
                        _startupComplete.value = true
                    }
                } else {
                    prefs.setRoomCode(null)
                    // Username kept in prefs so the create/join form can pre-fill it.
                    _startupComplete.value = true
                }
            } else {
                _startupComplete.value = true
            }
        }

        // Detect external removal. When a member is removed by an admin, Firestore security rules
        // deny access so the snapshot listener fires with a null snapshot → emptyMap(). We use
        // seenSelfInRoom to avoid false-positives during the brief window between _roomCode being
        // set and the first real snapshot arriving (where members is also empty).
        viewModelScope.launch {
            _startupComplete.first { it }
            var seenSelfInRoom = false
            members.collect { currentMembers ->
                val code = _roomCode.value
                if (code == null) { seenSelfInRoom = false; return@collect }
                val uid = try { repository.getUid() } catch (_: Exception) { return@collect }
                if (currentMembers.containsKey(uid)) {
                    seenSelfInRoom = true
                } else if (seenSelfInRoom) {
                    seenSelfInRoom = false
                    leaveRoom()
                }
            }
        }

        viewModelScope.launch { _updateInfo.value = checkForUpdate(BuildConfig.VERSION_NAME) }

        // Permanent subscriber so unread state and seen-at times keep flowing
        // regardless of which tab is on screen (mirrors why finalVotes/finalGuesses are
        // watched Eagerly).
        viewModelScope.launch {
            commentDiffs.collect(::applyCommentDiff)
        }
    }

    fun refreshUpdateCheck() {
        _updateInfo.value = UpdateCheckResult.Pending
        viewModelScope.launch { _updateInfo.value = checkForUpdate(BuildConfig.VERSION_NAME) }
    }

    suspend fun createRoom(username: String): Result<String> {
        val result = repository.createRoom(username)
        if (result.isSuccess) {
            val code = result.getOrThrow()
            _roomCode.value = code
            _username.value = username
            prefs.setRoomCode(code)
            prefs.setUsername(username)
            prefs.setLastJoinedRoomCode(code)
            // A new room starts with no show selected.
            _selectedShowId.value = null
            prefs.setShowId(null)
            viewModelScope.launch { repository.updateLastActivityAt(code, null) }
        }
        return result
    }

    suspend fun joinRoom(roomCode: String, username: String): Result<Unit> {
        val result = repository.joinRoom(roomCode, username)
        if (result.isSuccess) {
            // Restore the saved show only when rejoining the room it was picked in.
            val sameRoom = prefs.getLastJoinedRoomCode() == roomCode
            _roomCode.value = roomCode
            _username.value = username
            prefs.setRoomCode(roomCode)
            prefs.setUsername(username)
            prefs.setLastJoinedRoomCode(roomCode)
            if (sameRoom) {
                prefs.getShowId()?.let { _selectedShowId.value = it }
            } else {
                _selectedShowId.value = null
                prefs.setShowId(null)
            }
            viewModelScope.launch { repository.updateLastActivityAt(roomCode, null) }
        }
        return result
    }

    suspend fun renameUser(newUsername: String): Result<Unit> {
        val code = _roomCode.value ?: return Result.failure(Exception("No room"))
        val result = repository.renameUser(code, newUsername)
        if (result.isSuccess) {
            _username.value = newUsername
            prefs.setUsername(newUsername)
        }
        return result
    }

    suspend fun removeMember(uidToRemove: String): Result<Unit> {
        val code = _roomCode.value ?: return Result.failure(Exception("Not in a room"))
        val username = members.value[uidToRemove] ?: return Result.failure(Exception("Member not found"))
        return repository.removeMember(code, uidToRemove, username)
    }

    fun leaveRoom() {
        _roomCode.value = null
        _username.value = null
        _selectedShowId.value = null
        prefs.setRoomCode(null)
    }

    fun selectShow(showId: String) {
        _selectedShowId.value = showId
        prefs.setShowId(showId)
    }

    fun setLanguage(lang: String) {
        _language.value = lang
        prefs.setLanguage(lang)
    }

    fun submitVote(order: Int, points: Int) {
        val showId = _selectedShowId.value ?: return
        val code = _roomCode.value ?: return
        viewModelScope.launch {
            try {
                val uid = repository.getUid()
                repository.submitVote(code, showId, order, uid, points)
            } catch (_: Exception) { /* best-effort; e.g. auth not ready or write denied */ }
        }
    }

    // Submitting an empty string deletes the caller's existing comment for this entry.
    fun submitComment(order: Int, text: String) {
        val showId = _selectedShowId.value ?: return
        val code = _roomCode.value ?: return
        viewModelScope.launch {
            try {
                val uid = repository.getUid()
                repository.submitComment(code, showId, order, uid, text)
            } catch (_: Exception) { /* best-effort; e.g. auth not ready or write denied */ }
        }
    }

    fun submitGuess(participantOrder: Int, rank: Int?) {
        val showId = _selectedShowId.value ?: return
        val code = _roomCode.value ?: return
        val uid = myUid ?: return
        // A country holds at most one rank and a rank at most one country: drop this country
        // from wherever it was, then (re)place it, overwriting the rank's previous occupant.
        val current = guesses.value[uid].orEmpty()
        val picks = current.filterValues { it != participantOrder } +
            listOfNotNull(rank?.let { it to participantOrder })
        if (picks == current) return
        viewModelScope.launch {
            try {
                repository.setGuesses(code, showId, uid, picks)
            } catch (_: Exception) { /* best-effort; e.g. auth not ready or write denied */ }
        }
    }

}
