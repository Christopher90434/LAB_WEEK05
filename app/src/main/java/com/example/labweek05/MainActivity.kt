package com.example.labweek05

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.labweek05.api.CatApiService
import com.example.labweek05.model.ImageData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivity : AppCompatActivity() {

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.thecatapi.com/v1/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    private val catApiService by lazy {
        retrofit.create(CatApiService::class.java)
    }

    private val apiResponseView: TextView by lazy {
        findViewById(R.id.api_response)
    }

    private val imageResultView: ImageView by lazy {
        findViewById(R.id.image_result)
    }

    private val imageLoader: ImageLoader by lazy {
        GlideLoader(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(MAIN_ACTIVITY, "MainActivity started")
        getCatImageResponse()
    }

    private fun getCatImageResponse() {
        Log.d(MAIN_ACTIVITY, "Starting API call")

        apiResponseView.text = "Loading cat data..."

        val call = catApiService.searchImages(1, "full")
        call.enqueue(object : Callback<List<ImageData>> {
            override fun onFailure(call: Call<List<ImageData>>, t: Throwable) {
                Log.e(MAIN_ACTIVITY, "API call failed", t)
                apiResponseView.text = "Error: Network problem"
                imageResultView.setImageResource(android.R.drawable.stat_notify_error)
            }

            override fun onResponse(
                call: Call<List<ImageData>>,
                response: Response<List<ImageData>>
            ) {
                Log.d(MAIN_ACTIVITY, "Response received: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val images = response.body()!!
                    Log.d(MAIN_ACTIVITY, "Images count: ${images.size}")

                    if (images.isNotEmpty()) {
                        val firstImage = images[0]
                        val imageUrl = firstImage.imageUrl

                        Log.d(MAIN_ACTIVITY, "Image URL: $imageUrl")

                        // Load image with Glide
                        if (imageUrl.isNotEmpty()) {
                            imageLoader.loadImage(imageUrl, imageResultView)
                            Log.d(MAIN_ACTIVITY, "Image loading started")
                        } else {
                            imageResultView.setImageResource(android.R.drawable.ic_menu_gallery)
                        }

                        // Get breed name - SIMPLE VERSION
                        val breedName = if (firstImage.breeds.isNotEmpty()) {
                            firstImage.breeds[0].name.ifEmpty { "Unknown" }
                        } else {
                            "Unknown"
                        }

                        Log.d(MAIN_ACTIVITY, "Breed name: $breedName")
                        apiResponseView.text = "Breed: $breedName"

                    } else {
                        Log.e(MAIN_ACTIVITY, "No images in response")
                        apiResponseView.text = "No images found"
                        imageResultView.setImageResource(android.R.drawable.ic_menu_gallery)
                    }
                } else {
                    Log.e(MAIN_ACTIVITY, "API error: ${response.code()}")
                    apiResponseView.text = "API Error: ${response.code()}"
                    imageResultView.setImageResource(android.R.drawable.stat_notify_error)
                }
            }
        })
    }

    companion object {
        const val MAIN_ACTIVITY = "MAIN_ACTIVITY"
    }
}
