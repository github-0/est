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
import kotlinx.coroutines.flow.stateIn
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

    val shows: StateFlow<Map<String, List<Participant>>> = _authReady
        .flatMapLatest { ready -> if (ready) repository.getShows() else flowOf(emptyMap()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val votes: StateFlow<Map<Int, Map<String, Int>>> =
        combine(_roomCode, _selectedShowId) { code, showId -> code to showId }
            .flatMapLatest { (code, showId) ->
                if (code != null && showId != null) repository.getVotes(code, showId)
                else flowOf(emptyMap())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val guesses: StateFlow<Map<String, Map<Int, Int>>> =
        combine(_roomCode, _selectedShowId) { code, showId -> code to showId }
            .flatMapLatest { (code, showId) ->
                if (code != null && showId != null) repository.getGuesses(code, showId)
                else flowOf(emptyMap())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

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
            repository.signInAnonymously()
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

        viewModelScope.launch { _updateInfo.value = checkForUpdate(BuildConfig.VERSION_NAME) }
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
            prefs.getShowId()?.let { _selectedShowId.value = it }
            viewModelScope.launch { repository.updateLastActivityAt(code, null) }
        }
        return result
    }

    suspend fun joinRoom(roomCode: String, username: String): Result<Unit> {
        val result = repository.joinRoom(roomCode, username)
        if (result.isSuccess) {
            _roomCode.value = roomCode
            _username.value = username
            prefs.setRoomCode(roomCode)
            prefs.setUsername(username)
            prefs.setLastJoinedRoomCode(roomCode)
            prefs.getShowId()?.let { _selectedShowId.value = it }
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
        val uid = repository.getUid()
        viewModelScope.launch {
            repository.submitVote(code, showId, order, uid, points)
        }
    }

    fun submitGuess(participantOrder: Int, rank: Int?) {
        val showId = _selectedShowId.value ?: return
        val code = _roomCode.value ?: return
        val uid = repository.getUid()
        viewModelScope.launch {
            val existingRank = guesses.value[uid]
                ?.entries?.find { it.value == participantOrder }?.key
            if (rank == null) {
                if (existingRank != null) repository.removeGuess(code, showId, uid, existingRank)
            } else {
                if (existingRank != null && existingRank != rank) {
                    repository.removeGuess(code, showId, uid, existingRank)
                }
                repository.setGuess(code, showId, uid, rank, participantOrder)
            }
        }
    }

}
