package com.answere2022.unsplash_projects.data.models


import com.google.gson.annotations.SerializedName

data class LinksX(
    @SerializedName("html")
    val html: String? = null,
    @SerializedName("likes")
    val likes: String? = null,
    @SerializedName("photos")
    val photos: String? = null,
    @SerializedName("portfolio")
    val portfolio: String? = null,
    @SerializedName("self")
    val self: String? = null
)
