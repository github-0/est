package com.example.evfunenhancer.ui.strings

import androidx.compose.runtime.staticCompositionLocalOf

interface AppStrings {
    // Profile screen
    val username: String
    val createNewUsername: String
    val usernameAlreadyTaken: String
    val show: String
    val showsComingSoon: String
    val showsComingSoonHint: String
    val semiFinal1: String
    val semiFinal2: String
    val final: String
    val confirm: String
    fun showLabel(showId: String): String
    /** Long-form show name for titles ("Semifinal 1"); [showLabel] is the short form. */
    fun showTitle(showId: String): String
    fun translateCountry(name: String): String

    // Room UI
    val createRoom: String
    val or: String
    val joinRoom: String
    val roomCode: String
    val enterRoomCode: String
    val roomNotFound: String
    val shareRoomCode: String
    /** Room code share button label, one word per line. */
    val shareCodeLabel: List<String>
    /** True: spread letters so the words end flush. False: center the words. */
    val shareCodeLabelJustified: Boolean
    val leave: String
    val renameUser: String
    val members: String
    fun memberOnline(username: String): String

    // Nav tabs
    val profileTab: String
    val pointsTab: String
    val summaryTab: String

    // Points screen
    val votingOn: String

    // Summary screen
    val countryHeader: String
    val medalsHeader: String
    val guessTheWinner: String
    val totalPointsHeader: String
    // Number picker dialog
    val commentDialogTitle: String
    val leaveComment: String
    fun commentByOn(username: String, countryName: String): String
    val commentsFeedTitle: String
    val commentsFeedByCountry: String
    val commentsFeedLatest: String
    val commentsFeedEmpty: String
    val commentNewTag: String
    fun commentsNew(count: Int): String

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
    val maintenanceTestDatabase: String
    val maintenanceTestDatabaseHint: String
    val maintenanceSwitchToTestTitle: String
    val maintenanceSwitchToProductionTitle: String
    val maintenanceSwitchDatabaseBody: String
    val maintenanceSwitchAndRestart: String

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
    val aftershowGuessScoringExplainer: String
    val aftershowGuessScoreUnit: String
    val aftershowSharedFeelings: String
    val aftershowGroupAgreement: String
    val aftershowMostGenerous: String
    val aftershowMostRobbed: String
    val aftershowColGroup: String
    val aftershowColOfficial: String
    val aftershowBiggestSurprise: String
    val aftershowRankShift: String
    val aftershowCompatibility: String
    val aftershowCompatibilityClosest: String
    val aftershowCompatibilityNoOwnVotes: String
    val aftershowCompatibilityNoOthers: String
    val aftershowCompatibilityInfoTitle: String
    val aftershowCompatibilityInfoBody: String
    val aftershowInfoClose: String
    val aftershowConsensus: String
    val aftershowConsensusPick: String
    val aftershowMostDivisive: String
    val aftershowConsensusTooFewVoters: String
    val aftershowConsensusNoOverlap: String
    val aftershowConsensusInfoTitle: String
    val aftershowConsensusInfoBody: String
    val aftershowAwards: String
    val aftershowAwardsNoneForYou: String
    val aftershowAwardsOthers: String
    fun aftershowAwardsShowOthers(count: Int): String
    val aftershowAwardsHideOthers: String
    val aftershowAwardsInfoTitle: String
    val aftershowAwardsInfoBody: String
    val aftershowAwardMainstream: String
    val aftershowAwardHipster: String
    val aftershowAwardJuryBrain: String
    val aftershowAwardTelevoteHeart: String
    val aftershowAwardNostradamus: String
    val aftershowAwardSharpEye: String
    val aftershowAwardKingmaker: String
    val aftershowAwardLostCause: String
    val aftershowAwardHiveMind: String
    val aftershowAwardLoneWolf: String
    val aftershowAwardChatterbox: String
    val aftershowAwardMainstreamAbout: String
    val aftershowAwardHipsterAbout: String
    val aftershowAwardJuryBrainAbout: String
    val aftershowAwardTelevoteHeartAbout: String
    val aftershowAwardNostradamusAbout: String
    val aftershowAwardSharpEyeAbout: String
    val aftershowAwardKingmakerAbout: String
    val aftershowAwardLostCauseAbout: String
    val aftershowAwardHiveMindAbout: String
    val aftershowAwardLoneWolfAbout: String
    val aftershowAwardChatterboxAbout: String
    fun aftershowAwardMainstreamDetail(percent: Int): String
    fun aftershowAwardHipsterDetail(percent: Int): String
    fun aftershowAwardJuryBrainDetail(jury: Int, televote: Int): String
    fun aftershowAwardTelevoteHeartDetail(televote: Int, jury: Int): String
    fun aftershowAwardNostradamusDetail(pts: Int): String
    fun aftershowAwardSharpEyeDetail(hits: Int): String
    fun aftershowAwardKingmakerDetail(country: String): String
    fun aftershowAwardLostCauseDetail(country: String, rank: Int): String
    fun aftershowAwardHiveMindDetail(percent: Int, runnerUpPercent: Int): String
    fun aftershowAwardLoneWolfDetail(percent: Int, averagePercent: Int): String
    fun aftershowAwardChatterboxDetail(count: Int): String
    fun aftershowPts(pts: Int): String
    fun aftershowCountryFallback(order: Int): String
    val aftershowNotAvailableBody: String
    val aftershowNoVotes: String
    val aftershowOfficialResults: String
    val aftershowOfficialLabel: String
    val aftershowResultsLabel: String
    val aftershowColJury: String
    val aftershowColPublic: String
    val aftershowColTotal: String
    val aftershowComingSoon: String
    val aftershowPointsMap: String
    val aftershowMapYourGroup: String
    val aftershowMapOfficial: String
    val aftershowMapLessPoints: String
    val aftershowMapMorePoints: String
    val aftershowMapResolution: String
    fun aftershowMapSpreadKm(km: Int): String
    fun aftershowMapNotShown(flags: String): String
    val aftershowStoryBack: String
    val aftershowStoryNext: String
    val aftershowStoryReset: String
    val aftershowStorySkipped: String
    // Short card names shown in the card jump strip's scrub bubble
    val aftershowStripPointsMap: String
    val aftershowStripGuessedWinners: String
    val aftershowStripMostGenerous: String
    val aftershowStripOfficialResults: String
    val aftershowFormatAllCards: String
    val aftershowFormatSummary: String
    val aftershowSummaryBestGuesser: String
    val aftershowSummaryClosestMatch: String
    val aftershowSummaryMostGenerous: String
    val aftershowSummaryOverlapHint: String
    fun aftershowSummaryGroupRank(rank: Int?): String
    fun aftershowSummaryTop10Match(count: Int): String
}

val LocalAppStrings = staticCompositionLocalOf<AppStrings> { StringsEn }
