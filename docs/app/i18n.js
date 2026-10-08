// UI strings and country names for the web version. Country tables are copied from the app
// (aliases and flags from utils/FlagUtils.kt, Finnish names from ui/strings/StringsFi.kt, which
// also lists names that are the same in Finnish): add a new entrant there and here.

const ALIASES = {"Czech Republic": "Czechia", "Türkiye": "Turkey", "Macedonia": "North Macedonia", "FYR Macedonia": "North Macedonia", "The Netherlands": "Netherlands", "Holland": "Netherlands", "UK": "United Kingdom", "Great Britain": "United Kingdom", "Bosnia & Herzegovina": "Bosnia and Herzegovina"};

const ISO = {"Albania": "AL", "Andorra": "AD", "Armenia": "AM", "Australia": "AU", "Austria": "AT", "Azerbaijan": "AZ", "Belarus": "BY", "Belgium": "BE", "Bosnia and Herzegovina": "BA", "Bulgaria": "BG", "Croatia": "HR", "Cyprus": "CY", "Czechia": "CZ", "Denmark": "DK", "Estonia": "EE", "Finland": "FI", "France": "FR", "Georgia": "GE", "Germany": "DE", "Greece": "GR", "Hungary": "HU", "Iceland": "IS", "Ireland": "IE", "Israel": "IL", "Italy": "IT", "Kazakhstan": "KZ", "Latvia": "LV", "Lebanon": "LB", "Liechtenstein": "LI", "Lithuania": "LT", "Luxembourg": "LU", "Malta": "MT", "Moldova": "MD", "Monaco": "MC", "Montenegro": "ME", "Morocco": "MA", "Netherlands": "NL", "North Macedonia": "MK", "Norway": "NO", "Poland": "PL", "Portugal": "PT", "Romania": "RO", "Russia": "RU", "San Marino": "SM", "Serbia": "RS", "Slovakia": "SK", "Slovenia": "SI", "Spain": "ES", "Sweden": "SE", "Switzerland": "CH", "Tunisia": "TN", "Turkey": "TR", "Ukraine": "UA", "United Kingdom": "GB"};

const FI_NAMES = {"Austria": "Itävalta", "Azerbaijan": "Azerbaidžan", "Belgium": "Belgia", "Croatia": "Kroatia", "Cyprus": "Kypros", "Czechia": "Tšekki", "Denmark": "Tanska", "Estonia": "Viro", "Finland": "Suomi", "France": "Ranska", "Germany": "Saksa", "Greece": "Kreikka", "Italy": "Italia", "Lithuania": "Liettua", "Luxembourg": "Luxemburg", "Netherlands": "Alankomaat", "Norway": "Norja", "Poland": "Puola", "Portugal": "Portugali", "Spain": "Espanja", "Sweden": "Ruotsi", "Switzerland": "Sveitsi", "Ukraine": "Ukraina", "United Kingdom": "Yhdistynyt kuningaskunta", "Belarus": "Valko-Venäjä", "Bosnia and Herzegovina": "Bosnia ja Hertsegovina", "Hungary": "Unkari", "Iceland": "Islanti", "Ireland": "Irlanti", "Morocco": "Marokko", "North Macedonia": "Pohjois-Makedonia", "Russia": "Venäjä", "Turkey": "Turkki", "Serbia and Montenegro": "Serbia ja Montenegro", "Yugoslavia": "Jugoslavia", "Kazakhstan": "Kazakstan", "Lebanon": "Libanon"};

const canonicalCountry = name => { const n = name.trim(); return ALIASES[n] ?? n; };

export function countryFlag(country) {
  const iso = ISO[canonicalCountry(country)];
  if (!iso) return "🏳️";
  return [...iso].map(c => String.fromCodePoint(0x1F1E6 + c.charCodeAt(0) - 65)).join("");
}

export const STRINGS = {
  en: {
    username: "Username",
    usernameHint: "2 letters or digits",
    roomCode: "Room code",
    joinRoom: "Join room",
    joinIntro: "Score Eurovision live with your friends. Ask whoever made the room for its code.",
    createInApp: "Rooms are created in the Android app.",
    getApp: "Get the app",
    roomNotFound: "Room not found",
    usernameTaken: "Username already taken",
    joinFailed: "Couldn't join. Check your connection and try again.",
    members: "Members",
    leave: "Leave room",
    show: "Select show",
    showLabel: { semi1: "Semi 1", semi2: "Semi 2", final: "Final" },
    showTitle: { semi1: "Semifinal 1", semi2: "Semifinal 2", final: "Final" },
    votingOn: "Voting on",
    pickShow: "Pick a show above to start voting.",
    noShows: "No shows have participants yet.",
    tapToScore: "Tap a row to give points.",
    removed: "You were removed from the room.",
    voteFailed: "Vote not saved. Check your connection.",
    online: name => `${name} is online`,
    connecting: "Connecting…",
    configMissing: "The web version isn't set up yet (Firebase web config missing).",
    authFailed: "Couldn't connect to the server. Reload the page to try again.",
    cancel: "Cancel",
  },
  fi: {
    username: "Käyttäjänimi",
    usernameHint: "2 kirjainta tai numeroa",
    roomCode: "Huonekoodi",
    joinRoom: "Liity huoneeseen",
    joinIntro: "Pisteytä Euroviisut livenä kavereiden kanssa. Kysy huonekoodi huoneen luojalta.",
    createInApp: "Huoneet luodaan Android-sovelluksessa.",
    getApp: "Hae sovellus",
    roomNotFound: "Huonetta ei löydy",
    usernameTaken: "Käyttäjä on jo olemassa",
    joinFailed: "Liittyminen epäonnistui. Tarkista yhteys ja yritä uudelleen.",
    members: "Jäsenet",
    leave: "Poistu huoneesta",
    show: "Valitse ohjelma",
    showLabel: { semi1: "Semi 1", semi2: "Semi 2", final: "Finaali" },
    showTitle: { semi1: "Semifinaali 1", semi2: "Semifinaali 2", final: "Finaali" },
    votingOn: "Äänestys:",
    pickShow: "Valitse ohjelma yltä aloittaaksesi äänestyksen.",
    noShows: "Ohjelmilla ei ole vielä esiintyjiä.",
    tapToScore: "Anna pisteet napauttamalla riviä.",
    removed: "Sinut poistettiin huoneesta.",
    voteFailed: "Ääntä ei tallennettu. Tarkista yhteys.",
    online: name => `${name} on paikalla`,
    connecting: "Yhdistetään…",
    configMissing: "Selainversiota ei ole vielä otettu käyttöön (Firebase-asetukset puuttuvat).",
    authFailed: "Palvelimeen ei saatu yhteyttä. Yritä uudelleen lataamalla sivu.",
    cancel: "Peruuta",
  },
};

export function translateCountry(lang, name) {
  return lang === "fi" ? (FI_NAMES[canonicalCountry(name)] ?? name) : name;
}
