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
    override fun showTitle(showId: String) = when (showId) {
        "semi1" -> "Semifinal 1"
        "semi2" -> "Semifinal 2"
        else -> showLabel(showId)
    }

    override val createRoom = "Create Room"
    override val or = "OR"
    override val joinRoom = "Join Room"
    override val roomCode = "Room Code"
    override val enterRoomCode = "Enter room code"
    override val roomNotFound = "Room not found"
    override val shareRoomCode = "Share code"
    override val shareCodeLabel = listOf("SHARE", "CODE")
    override val shareCodeLabelJustified = true
    override val leave = "Leave room"
    override val renameUser = "Change Username"
    override val members = "Room Members"
    override fun memberOnline(username: String) = "$username is online"

    override val profileTab = "Settings"
    override val pointsTab = "Voting"
    override val summaryTab = "Summary"

    override val votingOn = "Voting on"

    override val countryHeader = "Country"
    override val medalsHeader = "Medals"
    override val guessTheWinner = "Guess as winner"
    override val totalPointsHeader = "Pts"
    override val commentDialogTitle = "Comment"
    override val leaveComment = "Add a comment"
    override fun commentByOn(username: String, countryName: String) = "$username on $countryName"
    override val commentsFeedTitle = "Comments"
    override val commentsFeedByCountry = "By country"
    override val commentsFeedLatest = "Latest"
    override val commentsFeedEmpty = "Nothing here yet"
    override val commentNewTag = "NEW"
    override fun commentsNew(count: Int) = "$count new"

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
    override val aftershowGuessedWinners = "WHO GUESSED THE WINNERS"
    override val aftershowNoGuesses = "No one guessed correctly."
    override val aftershowGuessScoringExplainer = "correct guess +2 pts · top 3 +1 pt"
    override val aftershowGuessScoreUnit = "pts"
    override val aftershowSharedFeelings = "SHARED FAVORITES"
    override val aftershowGroupAgreement = "group agreement with the official results"
    override val aftershowMostGenerous = "MOST GENEROUS VOTERS"
    override val aftershowMostRobbed = "MOST ROBBED"
    override val aftershowColGroup = "group"
    override val aftershowColOfficial = "official"
    override val aftershowBiggestSurprise = "BIGGEST SURPRISE"
    override val aftershowRankShift = "RANK SHIFT"
    override val aftershowCompatibility = "TASTE TWINS"
    override val aftershowCompatibilityClosest = "YOUR CLOSEST MATCH"
    override val aftershowCompatibilityNoOwnVotes = "Score the final to see who shares your taste."
    override val aftershowCompatibilityNoOthers = "No one else scored enough of the same songs."
    override val aftershowCompatibilityInfoTitle = "How the match works"
    override val aftershowCompatibilityInfoBody =
        "The match is calculated using Spearman's rank correlation, which turns each person's scores into places " +
        "(1st, 2nd, 3rd…) and measures how closely the two orderings match.\n\n" +
        "100% = same order · 50% = no pattern · 0% = opposite order\n\n" +
        "Only the songs you both scored are compared. Someone is left out if you have fewer than 3 of these, " +
        "or if one of you gave them all the same score."
    override val aftershowInfoClose = "Got it"
    override val aftershowConsensus = "GROUP CONSENSUS"
    override val aftershowConsensusPick = "MOST AGREED ON"
    override val aftershowMostDivisive = "MOST DIVISIVE"
    override val aftershowConsensusTooFewVoters = "At least two members need to score the final."
    override val aftershowConsensusNoOverlap = "No song was scored by enough members."
    override val aftershowConsensusInfoTitle = "How the lists work"
    override val aftershowConsensusInfoBody =
        "Each song's spread is measured with the standard deviation: how far the members' points " +
        "are from the song's average.\n\nA song is left out if fewer than half of the voters " +
        "(or fewer than 2) scored it."
    override val aftershowAwards = "AWARDS"
    override val aftershowAwardsNoneForYou = "No award for you this year — here's who got one."
    override val aftershowAwardsOthers = "OTHER AWARDS"
    override fun aftershowAwardsShowOthers(count: Int) = if (count == 1) "Show 1 other award" else "Show $count other awards"
    override val aftershowAwardsHideOthers = "Hide other awards"
    override val aftershowAwardsInfoTitle = "How the awards work"
    override val aftershowAwardsInfoBody =
        "Similarity % compares the order of a member's points with another ranking: the official result, " +
        "the jury, the public or the other members. 100% = exactly the same order, 50% = no connection, " +
        "0% = the exact opposite.\n\n" +
        "Mainstream: similarity to the official result 70% or more. Hipster: 45% or less.\n" +
        "Jury Brain / Televote Heart: similarity to the jury at least 10 points higher than to the public, or the other way round.\n" +
        "Nostradamus: top-3 guess worth 2 points or more.\n" +
        "Sharp Eye: 3 or more of your 5 highest-scored songs finished in the official top 5.\n" +
        "Kingmaker / Lost Cause: a song you gave your highest points won, or finished in the bottom 5.\n" +
        "Hive Mind: the one member whose average similarity to the others is at least 4 points ahead of the runner-up. " +
        "Lone Wolf: the one member at least 8 points below the group average. Both need 3 or more voters.\n" +
        "Chatterbox: the one member with the most comments on the final (at least 5).\n\n" +
        "Everyone who qualifies gets the other awards."
    override val aftershowAwardMainstream = "MAINSTREAM"
    override val aftershowAwardHipster = "HIPSTER"
    override val aftershowAwardJuryBrain = "JURY BRAIN"
    override val aftershowAwardTelevoteHeart = "TELEVOTE HEART"
    override val aftershowAwardNostradamus = "NOSTRADAMUS"
    override val aftershowAwardSharpEye = "SHARP EYE"
    override val aftershowAwardKingmaker = "KINGMAKER"
    override val aftershowAwardLostCause = "LOST CAUSE"
    override val aftershowAwardHiveMind = "HIVE MIND"
    override val aftershowAwardLoneWolf = "LONE WOLF"
    override val aftershowAwardChatterbox = "CHATTERBOX"
    override val aftershowAwardMainstreamAbout = "Ranked the songs like Europe"
    override val aftershowAwardHipsterAbout = "Ranked the songs unlike Europe"
    override val aftershowAwardJuryBrainAbout = "Voted like the jury"
    override val aftershowAwardTelevoteHeartAbout = "Voted like the public"
    override val aftershowAwardNostradamusAbout = "Guessed the winners"
    override val aftershowAwardSharpEyeAbout = "Top 5 matched official results"
    override val aftershowAwardKingmakerAbout = "Gave the winner their highest points"
    override val aftershowAwardLostCauseAbout = "Gave top points to a flop"
    override val aftershowAwardHiveMindAbout = "Most in tune with the group"
    override val aftershowAwardLoneWolfAbout = "Least in tune with the group"
    override val aftershowAwardChatterboxAbout = "Most comments"
    override fun aftershowAwardMainstreamDetail(percent: Int) = "$percent%"
    override fun aftershowAwardHipsterDetail(percent: Int) = "$percent%"
    override fun aftershowAwardJuryBrainDetail(jury: Int, televote: Int) = "Jury $jury% · Public $televote%"
    override fun aftershowAwardTelevoteHeartDetail(televote: Int, jury: Int) = "Public $televote% · Jury $jury%"
    override fun aftershowAwardNostradamusDetail(pts: Int) = "Guess: $pts pts"
    override fun aftershowAwardSharpEyeDetail(hits: Int) = "$hits of 5 matched"
    override fun aftershowAwardKingmakerDetail(country: String) = country
    override fun aftershowAwardLostCauseDetail(country: String, rank: Int) = "$country, ${ordinal(rank)}"
    override fun aftershowAwardHiveMindDetail(percent: Int, runnerUpPercent: Int) = "$percent% · next $runnerUpPercent%"
    override fun aftershowAwardLoneWolfDetail(percent: Int, averagePercent: Int) = "$percent% · avg $averagePercent%"
    override fun aftershowAwardChatterboxDetail(count: Int) = "$count comments"
    private fun ordinal(n: Int) = "$n" + when {
        n % 100 in 11..13 -> "th"
        n % 10 == 1 -> "st"
        n % 10 == 2 -> "nd"
        n % 10 == 3 -> "rd"
        else -> "th"
    }
    override fun aftershowPts(pts: Int) = "$pts pts"
    override fun aftershowCountryFallback(order: Int) = "Country $order"
    override val aftershowNotAvailableBody = "Final results have not yet been uploaded — check back later!"
    override val aftershowNoVotes = "No votes have been cast yet."
    override val aftershowOfficialResults = "OFFICIAL RESULTS"
    override val aftershowOfficialLabel = "OFFICIAL"
    override val aftershowResultsLabel = "RESULTS"
    override val aftershowColJury = "JURY"
    override val aftershowColPublic = "PUBLIC"
    override val aftershowColTotal = "TOTAL"
    override val aftershowComingSoon = "COMING SOON"
    override val aftershowPointsMap = "WHERE THE POINTS LANDED"
    override val aftershowMapYourGroup = "Your group"
    override val aftershowMapOfficial = "Official results"
    override val aftershowMapLessPoints = "Less points"
    override val aftershowMapMorePoints = "More points"
    override val aftershowMapResolution = "Resolution"
    override fun aftershowMapSpreadKm(km: Int) = "~$km km"
    override fun aftershowMapNotShown(flags: String) = "$flags not shown"
    override val aftershowStoryBack = "Back"
    override val aftershowStoryNext = "Next"
    override val aftershowStoryReset = "Aftershow story reset"
    override val aftershowStorySkipped = "Aftershow story skipped"
    override val aftershowStripPointsMap = "HEATMAP"
    override val aftershowStripGuessedWinners = "PREDICTIONS"
    override val aftershowStripMostGenerous = "GENEROSITY"
    override val aftershowStripOfficialResults = "RESULTS"
    override val aftershowFormatAllCards = "All cards"
    override val aftershowFormatSummary = "Summary card (9:16)"
    override val aftershowSummaryBestGuesser = "BEST TOP3 GUESSER"
    override val aftershowSummaryClosestMatch = "CLOSEST TO OFFICIAL"
    override val aftershowSummaryMostGenerous = "MOST GENEROUS"
    override val aftershowSummaryOverlapHint = "group vs official results"
    override fun aftershowSummaryGroupRank(rank: Int?) = if (rank != null) "group #$rank" else "group –"
    override fun aftershowSummaryTop10Match(count: Int) = "$count/10 of the top 10"
    override val updateAvailable: String = "Update available"
    override val updateUpToDate: String = "Up to date"
    override val updateCheckFailed: String = "Check failed"
    override val updateChecking: String = "Checking for updates…"
}
