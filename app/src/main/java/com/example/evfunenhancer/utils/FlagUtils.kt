package com.example.evfunenhancer.utils

// Other spellings that may appear in participants.json, mapped to the name the country tables
// use (flags here, Finnish names in StringsFi, map centres in EuropeMapData).
// The web version has a copy in docs/app/i18n.js.
private val COUNTRY_ALIASES = mapOf(
    "Czech Republic" to "Czechia",
    "Türkiye" to "Turkey",
    "Macedonia" to "North Macedonia",
    "FYR Macedonia" to "North Macedonia",
    "The Netherlands" to "Netherlands",
    "Holland" to "Netherlands",
    "UK" to "United Kingdom",
    "Great Britain" to "United Kingdom",
    "Bosnia & Herzegovina" to "Bosnia and Herzegovina",
)

fun canonicalCountry(country: String): String = country.trim().let { COUNTRY_ALIASES[it] ?: it }

fun countryFlag(country: String): String {
    val iso = COUNTRY_ISO[canonicalCountry(country)] ?: return "🏳️"
    return iso.map { c -> String(Character.toChars(0x1F1E6 + (c.uppercaseChar() - 'A'))) }.joinToString("")
}

// Every country that has entered Eurovision and still exists, plus likely debutants. Kosovo
// has no flag emoji (XK isn't an official code), and Yugoslavia / Serbia and Montenegro no
// longer exist, so those show the white flag.
private val COUNTRY_ISO = mapOf(
    "Albania" to "AL",
    "Andorra" to "AD",
    "Armenia" to "AM",
    "Australia" to "AU",
    "Austria" to "AT",
    "Azerbaijan" to "AZ",
    "Belarus" to "BY",
    "Belgium" to "BE",
    "Bosnia and Herzegovina" to "BA",
    "Bulgaria" to "BG",
    "Croatia" to "HR",
    "Cyprus" to "CY",
    "Czechia" to "CZ",
    "Denmark" to "DK",
    "Estonia" to "EE",
    "Finland" to "FI",
    "France" to "FR",
    "Georgia" to "GE",
    "Germany" to "DE",
    "Greece" to "GR",
    "Hungary" to "HU",
    "Iceland" to "IS",
    "Ireland" to "IE",
    "Israel" to "IL",
    "Italy" to "IT",
    "Kazakhstan" to "KZ",
    "Latvia" to "LV",
    "Lebanon" to "LB",
    "Liechtenstein" to "LI",
    "Lithuania" to "LT",
    "Luxembourg" to "LU",
    "Malta" to "MT",
    "Moldova" to "MD",
    "Monaco" to "MC",
    "Montenegro" to "ME",
    "Morocco" to "MA",
    "Netherlands" to "NL",
    "North Macedonia" to "MK",
    "Norway" to "NO",
    "Poland" to "PL",
    "Portugal" to "PT",
    "Romania" to "RO",
    "Russia" to "RU",
    "San Marino" to "SM",
    "Serbia" to "RS",
    "Slovakia" to "SK",
    "Slovenia" to "SI",
    "Spain" to "ES",
    "Sweden" to "SE",
    "Switzerland" to "CH",
    "Tunisia" to "TN",
    "Turkey" to "TR",
    "Ukraine" to "UA",
    "United Kingdom" to "GB"
)
