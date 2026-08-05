package com.example.evfunenhancer.ui.strings

object StringsEn : AppStrings {
    override val username = "Username"
    override val createNewUsername = "Choose name (2 letters)"
    override val usernameAlreadyTaken = "Username already taken"
    override val show = "Select show"
    override val semiFinal1 = "Semi 1"
    override val semiFinal2 = "Semi 2"
    override val final = "Final"
    override val confirm = "CONFIRM"
    override fun translateCountry(name: String) = name
    override fun showLabel(showId: String) = when (showId) {
        "semi1" -> semiFinal1
        "semi2" -> semiFinal2
        "final" -> final
        else -> showId
    }

    override val createRoom = "Create Room"
    override val or = "OR"
    override val joinRoom = "Join Room"
    override val roomCode = "Room Code"
    override val enterRoomCode = "Enter room code"
    override val roomNotFound = "Room not found"
    override val shareRoomCode = "Share code"
    override val leave = "Leave room"
    override val renameUser = "Change Username"
    override val members = "Room Members"

    override val profileTab = "Settings"
    override val pointsTab = "Voting"
    override val summaryTab = "Summary"

    override val countryHeader = "Country"
    override val medalsHeader = "Medals"
    override val totalPointsHeader = "Pts"
    override val winnerGuess = "Winner?"

    override val cancel = "Cancel"
    override val remove = "Remove"

    override val removeMembers = "Remove Room Members"
    override fun removeMembersNotCreator(creatorUsername: String) =
        "Only $creatorUsername can remove members from this room."
    override val removeMembersNoCreatorInfo =
        "Only the room creator can remove members (creator unknown for this room)."
    override val removeMembersSelectTitle = "Select member to remove"
    override fun removeMembersConfirmBody(username: String) =
        "Type YES to permanently remove $username and all their data."
    override val removeMembersConfirmWord = "YES"
    override val removeMembersSuccess = "Member removed."
    override val removeMembersFailed = "Removal failed."

    override val maintenanceMode = "Maintenance Mode"
    override val back = "Back"
    override val maintenanceFirebaseUid = "Firebase UID"
    override val maintenanceFirestoreStatus = "Firestore"
    override val maintenanceStatusChecking = "Checking…"
    override val maintenanceStatusOnline = "Online"
    override val maintenanceStatusOffline = "Offline"
    override fun maintenanceLastChecked(time: String) = "Last checked $time"
    override val maintenanceRefreshContentDescription = "Refresh"
    override val maintenanceAppVersion = "App version"
    override val maintenanceSectionStatus = "Application status"
    override val maintenanceSectionTools = "Maintenance tools"

    override val disclaimerLabel = "Disclaimer"
    override val disclaimerTitle = "Before you continue"
    override val disclaimerBody =
        "Although this app includes basic measures to limit exposure of information to third parties, any information you enter should be considered public.\n\n" +
        "This app is provided on a best-effort basis without any warranties of any kind. Use it at your own risk."
    override val disclaimerButton = "Understood"

    override val aftershowSavedToPhotos = "Saved to Photos"
    override val aftershowSaveShareFailed = "Couldn't save or share the image. Please try again."
    override val aftershowShare = "SHARE"
    override val aftershowSave = "SAVE"
    override val aftershowGuessedWinners = "GUESSED THE WINNERS"
    override val aftershowNoGuesses = "No one guessed correctly."
    override val aftershowSharedFeelings = "SHARED FAVORITES"
    override val aftershowGroupAgreement = "group agreement with the official results"
    override val aftershowMostGenerous = "MOST GENEROUS VOTERS"
    override val aftershowNoMedals = "No medal guesses."
    override val aftershowMedalTable = "MEDAL TABLE"
    override val aftershowJudgedDifferently = "JUDGED DIFFERENTLY"
    override val aftershowMostRobbed = "MOST ROBBED"
    override val aftershowColGroup = "group"
    override val aftershowColOfficial = "official"
    override val aftershowBiggestSurprise = "BIGGEST SURPRISE"
    override fun aftershowPts(pts: Int) = "$pts pts"
    override fun aftershowCountryFallback(order: Int) = "Country $order"
    override val aftershowNotAvailableBody = "Final results have not yet been uploaded — check back later!"
    override val aftershowNoVotes = "No votes have been cast yet."
    override val aftershowOfficial = "RESULTS"
    override val aftershowOfficialResults = "OFFICIAL RESULTS"
    override val aftershowColJury = "JURY"
    override val aftershowColPublic = "PUBLIC"
    override val aftershowColTotal = "TOTAL"
    override val aftershowComingSoon = "COMING SOON"
    override val updateAvailable: String = "Update available"
    override val updateUpToDate: String = "Up to date"
    override val updateCheckFailed: String = "Check failed"
    override val updateChecking: String = "Checking for updates…"
}
