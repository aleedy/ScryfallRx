package com.glutenmage.scryfall.api

import com.glutenmage.scryfall.models.AutocompleteResult
import com.glutenmage.scryfallrx.models.CardModel
import io.reactivex.Flowable
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface ScryfallApi {
    companion object {
        const val CARDS = "/cards"
        const val AUTOCOMPLETE = CARDS + "/autocomplete"
        const val RANDOM = CARDS + "/random"
        const val CARD_NAMED = CARDS + "/named"
    }
    @GET(AUTOCOMPLETE)
    fun autocompleteCards(@Query("q") query: String?): Flowable<AutocompleteResult>

    @GET(RANDOM)
    fun randomCard(): Flowable<CardModel>

    @GET(CARD_NAMED)
    fun cardNamed(@Query("exact") query: String?) : Flowable<CardModel>
}