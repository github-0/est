package com.example.evfunenhancer.ui.strings

object StringsFi : AppStrings {
    override val username = "Käyttäjänimi"
    override val createNewUsername = "Valitse nimi (2 kirjainta)"
    override val usernameAlreadyTaken = "Käyttäjä on jo olemassa"
    override val show = "Valitse ohjelma"
    override val semiFinal1 = "Semi 1"
    override val semiFinal2 = "Semi 2"
    override val final = "Finaali"
    override val confirm = "VAHVISTA"
    override fun translateCountry(name: String) = countryNamesFi[name] ?: name
    override fun showLabel(showId: String) = when (showId) {
        "semi1" -> semiFinal1
        "semi2" -> semiFinal2
        "final" -> final
        else -> showId
    }

    override val createRoom = "Luo huone"
    override val or = "TAI"
    override val joinRoom = "Liity huoneeseen"
    override val roomCode = "Huonekoodi"
    override val enterRoomCode = "Syötä huonekoodi"
    override val roomNotFound = "Huonetta ei löydy"
    override val shareRoomCode = "Jaa huonekoodi"
    override val leave = "Poistu huoneesta"
    override val renameUser = "Vaihda käyttäjänimi"
    override val members = "Jäsenet"

    override val profileTab = "Asetukset"
    override val pointsTab = "Pisteet"
    override val summaryTab = "Yhteenveto"

    override val countryHeader = "Maa"
    override val totalPointsHeader = "Pisteet yhteensä"
    override val winnerGuess = "Voittaja?"

    override val cancel = "Peruuta"

    override val maintenanceMode = "Ylläpito"
    override val back = "Takaisin"
    override val maintenanceFirebaseUid = "Firebase UID"
    override val maintenanceFirestoreStatus = "Firestore-tila"
    override val maintenanceStatusChecking = "Tarkistetaan…"
    override val maintenanceStatusOnline = "Online"
    override val maintenanceStatusOffline = "Offline"
    override fun maintenanceLastChecked(time: String) = "Tarkistettu $time"
    override val maintenanceRefreshContentDescription = "Päivitä"
    override val maintenanceAppVersion = "Sovelluksen versio"

    override val disclaimerLabel = "Tärkeää tietoa"
    override val disclaimerTitle = "Ennen kuin jatkat"
    override val disclaimerBody =
        "Vaikka sovellus sisältää menetelmiä tietojen suojaamiseksi kolmansilta osapuolilta, kaikkia syöttämiäsi tietoja on pidettävä julkisina.\n\n" +
        "Sovellus tarjotaan parhaiden kykyjemme mukaan, ilman minkäänlaisia takuita. Käytät sitä omalla vastuullasi."
    override val disclaimerButton = "Ymmärsin"

    override val aftershowSavedToPhotos = "Tallennettu kuviin"
    override val aftershowShare = "JAA"
    override val aftershowSave = "TALLENNA"
    override val aftershowGuessedWinners = "KUKA ARVASI VOITTAJAT"
    override val aftershowNoGuesses = "Kukaan ei arvannut oikein."
    override val aftershowScoringHint = "1 p. top 3:ssa  ·  2 p. oikeasta sijoituksesta"
    override val aftershowSharedFeelings = "JAETTIINKO TUNNELMA"
    override val aftershowGroupAgreement = "ryhmän yksimielisyys virallisten tulosten kanssa"
    override val aftershowMostGenerous = "ANTELIAIMMAT ÄÄNESTÄJÄT"
    override val aftershowNoMedals = "Ei mitaliarvauksia."
    override val aftershowMedalTable = "MITALITAULUKKO"
    override val aftershowJudgedDifferently = "SUURIMMAT EROT"
    override val aftershowMostRobbed = "KUKA ANSAITSI ENEMMÄN"
    override val aftershowColGroup = "ryhmä"
    override val aftershowColOfficial = "tulos"
    override val aftershowBiggestSurprise = "MUIDEN SUOSIKKI"
    override fun aftershowPts(pts: Int) = "$pts p."
    override fun aftershowCountryFallback(order: Int) = "Maa $order"
    override val aftershowNotAvailableTitle = "Aftershow ei ole vielä saatavilla"
    override val aftershowNotAvailableBody = "Finaalin tuloksia ei ole vielä ladattu — palaa myöhemmin!"
    override val aftershowNoVotes = "Ei äänestyksiä vielä."
    override val updateAvailable: String = "Päivitys saatavilla"
    override val updateUpToDate: String = "Ajan tasalla"
    override val updateCheckFailed: String = "Tarkistus epäonnistui"
    override val updateChecking: String = "Tarkistetaan päivityksiä…"
}

// Keys match the exact English country name strings stored in Firestore.
// Add new entries here when new countries are added to the seed data.
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
)
