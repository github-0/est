package com.example.evfunenhancer.ui.strings

import com.example.evfunenhancer.utils.canonicalCountry

object StringsFi : AppStrings {
    override val username = "Käyttäjänimi"
    override val createNewUsername = "Valitse nimi (2 kirjainta)"
    override val usernameAlreadyTaken = "Käyttäjä on jo olemassa"
    override val show = "Valitse ohjelma"
    override val semiFinal1 = "Semi 1"
    override val semiFinal2 = "Semi 2"
    override val final = "Finaali"
    override val confirm = "VAHVISTA"
    override fun translateCountry(name: String) = countryNamesFi[canonicalCountry(name)] ?: name
    override fun showLabel(showId: String) = when (showId) {
        "semi1" -> semiFinal1
        "semi2" -> semiFinal2
        "final" -> final
        else -> showId
    }
    override fun showTitle(showId: String) = when (showId) {
        "semi1" -> "Semifinaali 1"
        "semi2" -> "Semifinaali 2"
        else -> showLabel(showId)
    }

    override val createRoom = "Luo huone"
    override val or = "TAI"
    override val joinRoom = "Liity huoneeseen"
    override val roomCode = "Huonekoodi"
    override val enterRoomCode = "Syötä huonekoodi"
    override val roomNotFound = "Huonetta ei löydy"
    override val shareRoomCode = "Jaa huonekoodi"
    override val shareCodeLabel = listOf("JAA", "KOODI")
    override val shareCodeLabelJustified = false
    override val leave = "Poistu huoneesta"
    override val renameUser = "Vaihda käyttäjänimi"
    override val members = "Jäsenet"
    override fun memberOnline(username: String) = "$username on paikalla"

    override val profileTab = "Asetukset"
    override val pointsTab = "Äänestys"
    override val summaryTab = "Yhteenveto"

    override val votingOn = "Äänestys:"

    override val countryHeader = "Maa"
    override val medalsHeader = "Mitalit"
    override val guessTheWinner = "Veikkaa voittajaksi"
    override val totalPointsHeader = "Pst"
    override val commentDialogTitle = "Kommentti"
    override val leaveComment = "Lisää kommentti"
    override fun commentByOn(username: String, countryName: String) = "$username – $countryName"
    override val commentsFeedTitle = "Kommentit"
    override val commentsFeedByCountry = "Maittain"
    override val commentsFeedLatest = "Uusimmat"
    override val commentsFeedEmpty = "Ei vielä uusia kommentteja"
    override val commentNewTag = "UUSI"
    override fun commentsNew(count: Int) = "$count uutta"

    override val cancel = "Peruuta"
    override val remove = "Poista"

    override val removeMembers = "Poista huoneen jäseniä"
    override fun removeMembersNotCreator(creatorUsername: String) =
        "Vain $creatorUsername voi poistaa jäseniä tästä huoneesta."
    override val removeMembersNoCreatorInfo =
        "Vain huoneen luoja voi poistaa jäseniä (luoja ei tiedossa tässä huoneessa)."
    override val removeMembersSelectTitle = "Valitse poistettava jäsen"
    override fun removeMembersConfirmBody(username: String) =
        "Kirjoita KYLLÄ poistaaksesi ${username}n kaikki tiedot pysyvästi."
    override val removeMembersConfirmWord = "KYLLÄ"
    override val removeMembersSuccess = "Jäsen poistettu."
    override val removeMembersFailed = "Poistaminen epäonnistui."

    override val maintenanceMode = "Ylläpito"
    override val back = "Takaisin"
    override val maintenanceFirebaseUid = "Firebase UID"
    override val maintenanceFirestoreStatus = "Firestore"
    override val maintenanceStatusChecking = "Tarkistetaan…"
    override val maintenanceStatusOnline = "Online"
    override val maintenanceStatusOffline = "Offline"
    override fun maintenanceLastChecked(time: String) = "Tarkistettu $time"
    override val maintenanceRefreshContentDescription = "Päivitä"
    override val maintenanceAppVersion = "Sovelluksen versio"
    override val maintenanceSectionStatus = "Sovelluksen tila"
    override val maintenanceSectionTools = "Ylläpitotyökalut"

    override val disclaimerLabel = "Tärkeää tietoa"
    override val disclaimerTitle = "Ennen kuin jatkat"
    override val disclaimerBody =
        "Vaikka sovellus sisältää menetelmiä tietojen suojaamiseksi kolmansilta osapuolilta, kaikkia syöttämiäsi tietoja on pidettävä julkisina.\n\n" +
        "Sovellus tarjotaan parhaiden kykyjemme mukaan, ilman minkäänlaisia takuita. Käytät sitä omalla vastuullasi."
    override val disclaimerButton = "Ymmärsin"

    override val aftershowSavedToPhotos = "Tallennettu kuviin"
    override val aftershowSaveShareFailed = "Kuvan tallennus tai jakaminen epäonnistui. Yritä uudelleen."
    override val aftershowShare = "JAA"
    override val aftershowSave = "TALLENNA"
    override val aftershowGuessedWinners = "KUKA ARVASI VOITTAJAT"
    override val aftershowNoGuesses = "Kukaan ei arvannut oikein."
    override val aftershowGuessScoringExplainer = "oikea sija +2 p · top 3 +1 p"
    override val aftershowGuessScoreUnit = "p."
    override val aftershowSharedFeelings = "JAETTIINKO TUNNELMA"
    override val aftershowGroupAgreement = "ryhmän yksimielisyys virallisten tulosten kanssa"
    override val aftershowMostGenerous = "ANTELIAIMMAT ÄÄNESTÄJÄT"
    override val aftershowMostRobbed = "KUKA ANSAITSI ENEMMÄN"
    override val aftershowColGroup = "Ryhmä"
    override val aftershowColOfficial = "Eurooppa"
    override val aftershowBiggestSurprise = "MUIDEN SUOSIKKI"
    override val aftershowRankShift = "SIJOITUSTEN VERTAILU"
    override val aftershowCompatibility = "MAKUKAVERIT"
    override val aftershowCompatibilityClosest = "LÄHIN MAKUKAVERISI"
    override val aftershowCompatibilityNoOwnVotes = "Pisteytä finaali nähdäksesi kenen kanssa makusi osuu yksiin."
    override val aftershowCompatibilityNoOthers = "Kukaan muu ei pisteyttänyt tarpeeksi samoja kappaleita."
    override val aftershowCompatibilityInfoTitle = "Miten osuvuus lasketaan"
    override val aftershowCompatibilityInfoBody =
        "Osuvuus lasketaan Spearmanin järjestyskorrelaatiolla: kummankin pisteet muutetaan sijoiksi " +
        "(1., 2., 3.…) ja mitataan, kuinka hyvin nämä kaksi järjestystä vastaavat toisiaan.\n\n" +
        "100 % = sama järjestys · 50 % = ei selvää linjaa · 0 % = päinvastainen järjestys\n\n" +
        "Vertailussa ovat mukana vain kappaleet, jotka molemmat pisteyttivät. Henkilö jätetään pois, " +
        "jos näitä on alle 3 tai jompikumpi antoi niille kaikille samat pisteet."
    override val aftershowInfoClose = "Selvä"
    override val aftershowConsensus = "RYHMÄN YKSIMIELISYYS"
    override val aftershowConsensusPick = "VÄHITEN HAJONTAA"
    override val aftershowMostDivisive = "ENITEN HAJONTAA"
    override val aftershowConsensusTooFewVoters = "Vähintään kahden jäsenen täytyy pisteyttää finaali."
    override val aftershowConsensusNoOverlap = "Yksikään kappale ei saanut pisteitä tarpeeksi monelta."
    override val aftershowConsensusInfoTitle = "Miten listat lasketaan"
    override val aftershowConsensusInfoBody =
        "Kappaleen pisteiden hajonta mitataan keskihajonnalla: kuinka kaukana jäsenten pisteet ovat " +
        "kappaleen keskiarvosta.\n\nKappale jätetään pois, jos alle puolet äänestäjistä " +
        "(tai alle 2) pisteytti sen."
    override val aftershowAwards = "PALKINNOT"
    override val aftershowAwardsNoneForYou = "Sinulle ei osunut palkintoa tänä vuonna — muut saivat nämä."
    override val aftershowAwardsOthers = "MUIDEN PALKINNOT"
    override fun aftershowAwardsShowOthers(count: Int) = if (count == 1) "Näytä 1 muu palkinto" else "Näytä $count muuta palkintoa"
    override val aftershowAwardsHideOthers = "Piilota muut palkinnot"
    override val aftershowAwardsInfoTitle = "Miten palkinnot jaetaan"
    override val aftershowAwardsInfoBody =
        "Samankaltaisuus vertaa jäsenen pisteiden järjestystä toiseen järjestykseen: viralliseen tulokseen, tuomaristoon, " +
        "yleisöön tai muihin jäseniin. 100 % = täsmälleen sama järjestys, 50 % = ei yhteyttä, " +
        "0 % = täysin päinvastainen.\n\n" +
        "Valtavirta: samankaltaisuus viralliseen tulokseen vähintään 70 %. Hipsteri: enintään 45 %.\n" +
        "Raadin aivot / Kansan sydän: samankaltaisuus tuomaristoon vähintään 10 prosenttiyksikköä suurempi kuin yleisöön, tai päinvastoin.\n" +
        "Nostradamus: top 3 -veikkaus tuotti vähintään 2 pistettä.\n" +
        "Tarkka silmä: vähintään 3 viidestä eniten pisteitä saaneesta kappaleestasi sijoittui viralliseen top 5:een.\n" +
        "Kuninkaantekijä / Toivoton tapaus: kappale, jolle annoit suurimmat pisteesi, voitti tai jäi viiden viimeisen " +
        "joukkoon.\n" +
        "Parvimieli: se yksi jäsen, jonka keskimääräinen samankaltaisuus muihin on vähintään 4 prosenttiyksikköä " +
        "seuraavaa edellä. Yksinäinen susi: se yksi jäsen, joka on vähintään 8 prosenttiyksikköä ryhmän keskiarvon " +
        "alapuolella. Molemmat vaativat vähintään 3 äänestäjää.\n" +
        "Suupaltti: se yksi jäsen, jolla on eniten kommentteja finaalissa (vähintään 5).\n\n" +
        "Muut palkinnot saa jokainen, joka täyttää ehdon."
    override val aftershowAwardMainstream = "VALTAVIRTA"
    override val aftershowAwardHipster = "HIPSTERI"
    override val aftershowAwardJuryBrain = "RAADIN AIVOT"
    override val aftershowAwardTelevoteHeart = "KANSAN SYDÄN"
    override val aftershowAwardNostradamus = "NOSTRADAMUS"
    override val aftershowAwardSharpEye = "TARKKA SILMÄ"
    override val aftershowAwardKingmaker = "KUNINKAANTEKIJÄ"
    override val aftershowAwardLostCause = "TOIVOTON TAPAUS"
    override val aftershowAwardHiveMind = "PARVIMIELI"
    override val aftershowAwardLoneWolf = "YKSINÄINEN SUSI"
    override val aftershowAwardChatterbox = "SUUPALTTI"
    override val aftershowAwardMainstreamAbout = "Pisteytti kuin Eurooppa"
    override val aftershowAwardHipsterAbout = "Pisteytti toisin kuin Eurooppa"
    override val aftershowAwardJuryBrainAbout = "Äänesti kuten tuomaristo"
    override val aftershowAwardTelevoteHeartAbout = "Äänesti kuten yleisö"
    override val aftershowAwardNostradamusAbout = "Veikkasi voittajat"
    override val aftershowAwardSharpEyeAbout = "Top 5 vastasi virallisia tuloksia"
    override val aftershowAwardKingmakerAbout = "Antoi voittajalle suurimmat pisteensä"
    override val aftershowAwardLostCauseAbout = "Antoi kärkipisteet flopille"
    override val aftershowAwardHiveMindAbout = "Samaa mieltä ryhmän kanssa"
    override val aftershowAwardLoneWolfAbout = "Eri mieltä ryhmän kanssa"
    override val aftershowAwardChatterboxAbout = "Eniten kommentteja"
    override fun aftershowAwardMainstreamDetail(percent: Int) = "$percent %"
    override fun aftershowAwardHipsterDetail(percent: Int) = "$percent %"
    override fun aftershowAwardJuryBrainDetail(jury: Int, televote: Int) = "Tuomaristo $jury % · Yleisö $televote %"
    override fun aftershowAwardTelevoteHeartDetail(televote: Int, jury: Int) = "Yleisö $televote % · Tuomaristo $jury %"
    override fun aftershowAwardNostradamusDetail(pts: Int) = "Veikkaus: $pts p."
    override fun aftershowAwardSharpEyeDetail(hits: Int) = "$hits/5 osui"
    override fun aftershowAwardKingmakerDetail(country: String) = country
    override fun aftershowAwardLostCauseDetail(country: String, rank: Int) = "$country, sija $rank"
    override fun aftershowAwardHiveMindDetail(percent: Int, runnerUpPercent: Int) = "$percent % · seuraava $runnerUpPercent %"
    override fun aftershowAwardLoneWolfDetail(percent: Int, averagePercent: Int) = "$percent % · keskiarvo $averagePercent %"
    override fun aftershowAwardChatterboxDetail(count: Int) = "$count kommenttia"
    override fun aftershowPts(pts: Int) = "$pts p."
    override fun aftershowCountryFallback(order: Int) = "Maa $order"
    override val aftershowNotAvailableBody = "Finaalin tuloksia ei ole vielä ladattu — tarkista myöhemmin uudelleen!"
    override val aftershowNoVotes = "Ei vielä äänestyksiä."
    override val aftershowOfficialResults = "LOPPUTULOKSET"
    override val aftershowOfficialLabel = "VIRALLISET"
    override val aftershowResultsLabel = "TULOKSET"
    override val aftershowColJury = "TUOM."
    override val aftershowColPublic = "YLEISÖ"
    override val aftershowColTotal = "YHT."
    override val aftershowComingSoon = "TULOSSA"
    override val aftershowPointsMap = "MINNE PISTEET MENIVÄT"
    override val aftershowMapYourGroup = "Oma ryhmä"
    override val aftershowMapOfficial = "Viralliset tulokset"
    override val aftershowMapLessPoints = "Vähemmän pisteitä"
    override val aftershowMapMorePoints = "Enemmän pisteitä"
    override val aftershowMapResolution = "Tarkkuus"
    override fun aftershowMapSpreadKm(km: Int) = "~$km km"
    override fun aftershowMapNotShown(flags: String) = "$flags ei kartalla"
    override val aftershowStoryBack = "Takaisin"
    override val aftershowStoryNext = "Seuraava"
    override val aftershowStoryReset = "Aftershow-tarina palautettu"
    override val aftershowStorySkipped = "Aftershow-tarina ohitettu"
    override val aftershowStripPointsMap = "LÄMPÖKARTTA"
    override val aftershowStripGuessedWinners = "VEIKKAUKSET"
    override val aftershowStripMostGenerous = "ANTELIAISUUS"
    override val aftershowStripOfficialResults = "TULOKSET"
    override val aftershowFormatAllCards = "Kaikki kortit"
    override val aftershowFormatSummary = "Yhteenvetokortti (9:16)"
    override val aftershowSummaryBestGuesser = "PARAS TOP3-ARVAAJA"
    override val aftershowSummaryClosestMatch = "LÄHIMPÄNÄ TULOSTA"
    override val aftershowSummaryMostGenerous = "ANTELIAIN"
    override val aftershowSummaryOverlapHint = "ryhmä vs viralliset tulokset"
    override fun aftershowSummaryGroupRank(rank: Int?) = if (rank != null) "ryhmä #$rank" else "ryhmä –"
    override fun aftershowSummaryTop10Match(count: Int) = "$count/10 top 10:stä"
    override val updateAvailable: String = "Päivitys saatavilla"
    override val updateUpToDate: String = "Ajan tasalla"
    override val updateCheckFailed: String = "Tarkistus epäonnistui"
    override val updateChecking: String = "Tarkistetaan päivityksiä…"
}

// Keys are the English names used in participants.json (other spellings are resolved through
// canonicalCountry() in FlagUtils.kt). Covers every past entrant plus likely debutants; names
// that are the same in Finnish are listed too so the table doubles as the country list.
// The web version has a copy in docs/app/i18n.js.
private val countryNamesFi = mapOf(
    // Current participants
    "Albania" to "Albania",
    "Armenia" to "Armenia",
    "Australia" to "Australia",
    "Austria" to "Itävalta",
    "Azerbaijan" to "Azerbaidžan",
    "Belgium" to "Belgia",
    "Bulgaria" to "Bulgaria",
    "Croatia" to "Kroatia",
    "Cyprus" to "Kypros",
    "Czechia" to "Tšekki",
    "Denmark" to "Tanska",
    "Estonia" to "Viro",
    "Finland" to "Suomi",
    "France" to "Ranska",
    "Georgia" to "Georgia",
    "Germany" to "Saksa",
    "Greece" to "Kreikka",
    "Israel" to "Israel",
    "Italy" to "Italia",
    "Latvia" to "Latvia",
    "Lithuania" to "Liettua",
    "Luxembourg" to "Luxemburg",
    "Malta" to "Malta",
    "Moldova" to "Moldova",
    "Montenegro" to "Montenegro",
    "Netherlands" to "Alankomaat",
    "Norway" to "Norja",
    "Poland" to "Puola",
    "Portugal" to "Portugali",
    "Romania" to "Romania",
    "San Marino" to "San Marino",
    "Serbia" to "Serbia",
    "Slovenia" to "Slovenia",
    "Spain" to "Espanja",
    "Sweden" to "Ruotsi",
    "Switzerland" to "Sveitsi",
    "Ukraine" to "Ukraina",
    "United Kingdom" to "Yhdistynyt kuningaskunta",
    // Historical / returning participants
    "Andorra" to "Andorra",
    "Belarus" to "Valko-Venäjä",
    "Bosnia and Herzegovina" to "Bosnia ja Hertsegovina",
    "Hungary" to "Unkari",
    "Iceland" to "Islanti",
    "Ireland" to "Irlanti",
    "Kosovo" to "Kosovo",
    "Liechtenstein" to "Liechtenstein",
    "Monaco" to "Monaco",
    "Morocco" to "Marokko",
    "North Macedonia" to "Pohjois-Makedonia",
    "Russia" to "Venäjä",
    "Slovakia" to "Slovakia",
    "Turkey" to "Turkki",
    "Serbia and Montenegro" to "Serbia ja Montenegro",
    "Yugoslavia" to "Jugoslavia",
    // Possible debutants
    "Kazakhstan" to "Kazakstan",
    "Lebanon" to "Libanon",
    "Tunisia" to "Tunisia",
)
