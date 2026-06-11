package com.example.evfunenhancer.data

data class CountryResult(val order: Int, val rank: Int, val juryScore: Int, val publicScore: Int)
data class ShowResults(val year: Int, val entries: List<CountryResult>)
