package com.glutenmage.scryfallrx

import android.app.Application
import com.glutenmage.scryfall.DaggerScryfallComponent
import com.glutenmage.scryfall.models.AutocompleteResult
import com.glutenmage.scryfallrx.models.CardModel
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers

class ScryfallRx(application: Application) {
    private fun String.querify(): String{
        this.replace("\\s+".toRegex(), "+")
        return this
    }

    private val component = DaggerScryfallComponent.builder().application(application).build()
    private val scryfallApi = component.scryfallApi()

    fun autoComplete(query: String): Flowable<AutocompleteResult> = scryfallApi.autocompleteCards(query)
                .subscribeOn(Schedulers.io())

    fun random(): Flowable<CardModel> = scryfallApi.randomCard()
        .subscribeOn(Schedulers.io())

    fun cardNamed(query: String): Flowable<CardModel> {
        return scryfallApi.cardNamed(query.querify())
            .subscribeOn(Schedulers.io())

    }

}