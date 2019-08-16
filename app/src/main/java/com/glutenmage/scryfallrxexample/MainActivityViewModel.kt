package com.glutenmage.scryfallrxexample

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModel
import com.glutenmage.scryfall.models.AutocompleteResult
import com.glutenmage.scryfallrx.ScryfallRx
import com.glutenmage.scryfallrx.models.CardModel
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class MainActivityViewModel( lifecycle: Lifecycle, private val scryfallRx: ScryfallRx): ViewModel(), LifecycleObserver {
    private val stateChanged = PublishSubject.create<MainActivityViewModelState>()
    private val networkDisposable = CompositeDisposable()
    private var observerDisposable: Disposable? = null
    private var observer:  Observer<MainActivityViewModelState>? = null

    init {
        lifecycle.addObserver(this)
    }

    fun addObserver(result: (m: MainActivityViewModelState) -> Unit){
        observer = stateChanged
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : Observer<MainActivityViewModelState>{
            override fun onComplete() {
                return
            }

            override fun onSubscribe(d: Disposable) {
                observerDisposable = d
            }

            override fun onNext(t: MainActivityViewModelState) {
                result(t)
            }

            override fun onError(e: Throwable) {
                e.printStackTrace()
            }
        })
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause(){
        observerDisposable?.dispose()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume(){
        observer = stateChanged.observeOn(AndroidSchedulers.mainThread()).subscribeWith(observer)
    }

    fun performAction(action: Action){
        when (action){
            is Action.Random -> random()
            is Action.Search -> search(action.query)
            is Action.Autocomplete ->
                networkDisposable.add(Observable.just(action.query)
                .debounce(50, TimeUnit.MILLISECONDS)
                .subscribe { autocomplete(it) })
        }
    }

    sealed class MainActivityViewModelState {
        data class FetchSuccess(val cardModel: CardModel): MainActivityViewModelState()
        data class AutoCompleteSuccess(val autocompleteResult: AutocompleteResult): MainActivityViewModelState()
    }

    private fun random() {
        networkDisposable.add(scryfallRx.random()
            .subscribe ({
                stateChanged.onNext(MainActivityViewModelState.FetchSuccess(it))
            }, {}))
    }

    private fun autocomplete(query: String){
        networkDisposable.add(scryfallRx.autoComplete(query)
            .subscribe({
                stateChanged.onNext(MainActivityViewModelState.AutoCompleteSuccess(it))
                },
                {
                    stateChanged.onNext(MainActivityViewModelState.AutoCompleteSuccess(AutocompleteResult(emptyArray())))
                }))
    }


    private fun search(query: String) {
        networkDisposable.add(scryfallRx.cardNamed(query)
            .subscribe(
                {stateChanged.onNext(MainActivityViewModelState.FetchSuccess(it))},
                {}))
    }

    sealed class Action {
        data class Search(val query: String): Action()
        object Random: Action()
        data class Autocomplete(val query: String): Action()
    }

}