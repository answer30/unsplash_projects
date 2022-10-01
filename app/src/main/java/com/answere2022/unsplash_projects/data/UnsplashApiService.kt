package com.answere2022.unsplash_projects.data

import com.answere2022.unsplash_projects.BuildConfig
import com.answere2022.unsplash_projects.data.models.PhotoResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface UnsplashApiService {

    @GET(
        "photos/random?" +
                "client_id=${BuildConfig.UNSPLASH_ACCESS_KEY}" +
                "&count=30"


    )


    suspend fun getRandomPhotos(
        @Query("query") query: String?


    ): Response<List<PhotoResponse>>
}