package com.example.labweek05.model

import com.squareup.moshi.Json

data class ImageData(
    @field:Json(name = "url") val imageUrl: String = "",
    val breeds: List<CatBreedData> = emptyList()
)
