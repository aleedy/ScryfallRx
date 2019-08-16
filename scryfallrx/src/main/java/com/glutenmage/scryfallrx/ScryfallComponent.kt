package com.glutenmage.scryfall

import android.app.Application
import com.glutenmage.scryfall.api.ScryfallApi
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component (modules = [ScryfallModule::class])
interface ScryfallComponent {
    fun scryfallApi(): ScryfallApi
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder
        fun build(): ScryfallComponent
    }
}