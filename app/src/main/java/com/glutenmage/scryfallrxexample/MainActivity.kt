package com.glutenmage.scryfallrxexample

import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.widget.SearchView
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
import com.bumptech.glide.Glide
import com.glutenmage.scryfall.models.AutocompleteResult
import com.glutenmage.scryfallrx.ScryfallRx
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private lateinit var scryfallRx: ScryfallRx
    private var disposable = CompositeDisposable()
    private var searchChanged = PublishSubject.create<String>()
    private lateinit var searchView: SearchView
    private var autocompleteResult: AutocompleteResult = AutocompleteResult(emptyArray())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        scryfallRx = ScryfallRx(application)
        random()
    }

    override fun onPause() {
        super.onPause()
        disposable.dispose()
    }

    override fun onResume() {
        super.onResume()
        disposable.add(searchChanged.debounce(100, TimeUnit.MILLISECONDS)
            .subscribe {
                autocomplete(it)
            })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        val searchItem =  menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                    newText?.let {searchChanged.onNext(newText)}
                return true
            }

        })
        searchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener{
            override fun onSuggestionSelect(position: Int): Boolean {return true}

            override fun onSuggestionClick(position: Int): Boolean {
                search(autocompleteResult.data[position])
                return true
            }
        })

        return true
    }

    private fun random() {
        disposable.add(scryfallRx.random()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe ({
                Glide.with(this).load(it.imageUris.png).into(image)
            }, {}))
    }

    private fun autocomplete(query: String){
        disposable.add(scryfallRx.autoComplete(query)
            .observeOn(AndroidSchedulers.mainThread())
            .onErrorReturn { AutocompleteResult(emptyArray())}
            .subscribe ({
                autocompleteResult  = it
                if (searchView.suggestionsAdapter == null) {
                    searchView.suggestionsAdapter = AutocompleteAdapter(searchView.context, autocompleteResult)
                } else {
                    searchView.suggestionsAdapter.changeCursor(AutocompleteCursor.Build(autocompleteResult))
                }
            }, {
                if (searchView.suggestionsAdapter != null) {
                    searchView.suggestionsAdapter.changeCursor(AutocompleteCursor.Build(AutocompleteResult(emptyArray())))
                }
            }))
    }

    private fun search(query: String) {
        disposable.add(scryfallRx.cardNamed(query)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {Glide.with(this).load(it.imageUris.png).into(image)},
                {}))
    }

    class AutocompleteCursor{
        companion object {
                    fun Build(autocompleteResult: AutocompleteResult): Cursor {
                        val matrixCursor = MatrixCursor(arrayOf("_id","names"))
                        for((id, card) in autocompleteResult.data.withIndex()){
                            matrixCursor.addRow(arrayOf(id,card))
                        }
                        return matrixCursor
                    }
            }


    }

    class AutocompleteAdapter(context: Context, autocompleteResult: AutocompleteResult):
        SimpleCursorAdapter(context,
            R.layout.list_item_view,
            AutocompleteCursor.Build(autocompleteResult),
            arrayOf("names"),
            intArrayOf(R.id.itemTextView),
            CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER)

}
