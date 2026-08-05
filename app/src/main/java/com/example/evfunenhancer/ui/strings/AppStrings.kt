package com.example.evfunenhancer.ui.strings

import androidx.compose.runtime.staticCompositionLocalOf

interface AppStrings {
    // Profile screen
    val username: String
    val createNewUsername: String
    val usernameAlreadyTaken: String
    val show: String
    val semiFinal1: String
    val semiFinal2: String
    val final: String
    val confirm: String
    fun showLabel(showId: String): String
    fun translateCountry(name: String): String

    // Room UI
    val createRoom: String
    val or: String
    val joinRoom: String
    val roomCode: String
    val enterRoomCode: String
    val roomNotFound: String
    val shareRoomCode: String
    val leave: String
    val renameUser: String
    val members: String

    // Nav tabs
    val profileTab: String
    val pointsTab: String
    val summaryTab: String

    // Summary screen
    val countryHeader: String
    val medalsHeader: String
    val totalPointsHeader: String
    // Number picker dialog
    val winnerGuess: String

    // Dialog buttons
    val cancel: String
    val remove: String

    // Member removal (Maintenance screen)
    val removeMembers: String
    fun removeMembersNotCreator(creatorUsername: String): String
    val removeMembersNoCreatorInfo: String
    val removeMembersSelectTitle: String
    fun removeMembersConfirmBody(username: String): String
    val removeMembersConfirmWord: String
    val removeMembersSuccess: String
    val removeMembersFailed: String

    // Maintenance screen
    val maintenanceMode: String
    val back: String
    val maintenanceFirebaseUid: String
    val maintenanceFirestoreStatus: String
    val maintenanceStatusChecking: String
    val maintenanceStatusOnline: String
    val maintenanceStatusOffline: String
    fun maintenanceLastChecked(time: String): String
    val maintenanceRefreshContentDescription: String
    val maintenanceAppVersion: String
    val maintenanceSectionStatus: String
    val maintenanceSectionTools: String

    // Disclaimer
    val disclaimerLabel: String
    val disclaimerTitle: String
    val disclaimerBody: String
    val disclaimerButton: String
    val updateAvailable: String
    val updateUpToDate: String
    val updateCheckFailed: String
    val updateChecking: String

    // AfterShow screen
    val aftershowSavedToPhotos: String
    val aftershowSaveShareFailed: String
    val aftershowShare: String
    val aftershowSave: String
    val aftershowGuessedWinners: String
    val aftershowNoGuesses: String
    val aftershowSharedFeelings: String
    val aftershowGroupAgreement: String
    val aftershowMostGenerous: String
    val aftershowNoMedals: String
    val aftershowMedalTable: String
    val aftershowJudgedDifferently: String
    val aftershowMostRobbed: String
    val aftershowColGroup: String
    val aftershowColOfficial: String
    val aftershowBiggestSurprise: String
    fun aftershowPts(pts: Int): String
    fun aftershowCountryFallback(order: Int): String
    val aftershowNotAvailableBody: String
    val aftershowNoVotes: String
    val aftershowOfficial: String
    val aftershowOfficialResults: String
    val aftershowColJury: String
    val aftershowColPublic: String
    val aftershowColTotal: String
    val aftershowComingSoon: String
}

val LocalAppStrings = staticCompositionLocalOf<AppStrings> { StringsEn }
