package com.example.musicapplicationse114.di

import com.example.musicapplicationse114.repositories.Api
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module


@InstallIn(SingletonComponent::class)
object ApiModule {

        private const val BASE_URL = "http://172.20.10.2:8082/"
//      private const val BASE_URL = "https://6820d3f1259dad2655adb9ff.mockapi.io/"

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApi(retrofit: Retrofit): Api {
        return retrofit.create(Api::class.java)
    }
}
