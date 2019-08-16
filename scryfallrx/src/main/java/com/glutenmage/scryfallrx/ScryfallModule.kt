package com.glutenmage.scryfall

import com.glutenmage.scryfall.api.ScryfallApi
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import javax.inject.Named
import javax.inject.Singleton

@Module
class ScryfallModule {
    @Singleton
    @Provides
    fun provideScryFallApi(retrofit: Retrofit): ScryfallApi = retrofit.create(ScryfallApi::class.java)

    @Singleton
    @Provides
    fun provideRetrofit(@Named("viewedLiveData") baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    @Named("viewedLiveData")
    fun provideBaseUrl() = "https://api.scryfall.com/"
}