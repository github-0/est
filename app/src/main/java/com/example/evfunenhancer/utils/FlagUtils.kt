package com.example.evfunenhancer.utils

fun countryFlag(country: String): String {
    val iso = COUNTRY_ISO[country.trim()] ?: return "🏳️"
    return iso.map { c -> String(Character.toChars(0x1F1E6 + (c.uppercaseChar() - 'A'))) }.joinToString("")
}

private val COUNTRY_ISO = mapOf(
    "Albania" to "AL",
    "Armenia" to "AM",
    "Australia" to "AU",
    "Austria" to "AT",
    "Azerbaijan" to "AZ",
    "Belgium" to "BE",
    "Bulgaria" to "BG",
    "Croatia" to "HR",
    "Cyprus" to "CY",
    "Czech Republic" to "CZ",
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
    "Latvia" to "LV",
    "Lithuania" to "LT",
    "Luxembourg" to "LU",
    "Malta" to "MT",
    "Moldova" to "MD",
    "Monaco" to "MC",
    "Montenegro" to "ME",
    "Netherlands" to "NL",
    "North Macedonia" to "MK",
    "Norway" to "NO",
    "Poland" to "PL",
    "Portugal" to "PT",
    "Romania" to "RO",
    "San Marino" to "SM",
    "Serbia" to "RS",
    "Slovenia" to "SI",
    "Spain" to "ES",
    "Sweden" to "SE",
    "Switzerland" to "CH",
    "Ukraine" to "UA",
    "United Kingdom" to "GB"
)
