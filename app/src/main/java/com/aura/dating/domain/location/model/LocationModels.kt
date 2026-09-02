package com.aura.dating.domain.location.model

import kotlinx.serialization.Serializable

@Serializable
data class Country(
    val id: String,
    val name: String,
    val code: String? = null
)

@Serializable
data class Region(
    val id: String,
    val countryId: String,
    val name: String
)

@Serializable
data class City(
    val id: String,
    val regionId: String,
    val name: String
)

data class LocationSearchFilter(
    val country: Country? = null,
    val region: Region? = null,
    val city: City? = null,
    val minAge: Int = 18,
    val maxAge: Int = 100,
    val gender: String = "ALL"
)
