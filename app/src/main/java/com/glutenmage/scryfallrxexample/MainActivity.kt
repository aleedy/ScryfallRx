package com.glutenmage.scryfallrxexample

import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.widget.SearchView
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
import com.glutenmage.scryfall.models.AutocompleteResult
import com.glutenmage.scryfallrx.ScryfallRx
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private lateinit var searchView: SearchView
    private var autocompleteResult: AutocompleteResult = AutocompleteResult(emptyArray())
    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = MainActivityViewModel(lifecycle, ScryfallRx(application))
        viewModel.addObserver { updateState(it) }
        viewModel.performAction(MainActivityViewModel.Action.Random)
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
                    newText?.let {viewModel.performAction(MainActivityViewModel.Action.Autocomplete(it))}
                return true
            }

        })
        searchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener{
            override fun onSuggestionSelect(position: Int): Boolean {return true}

            override fun onSuggestionClick(position: Int): Boolean {
                viewModel.performAction(MainActivityViewModel.Action.Search(autocompleteResult.data[position]))
                return true
            }
        })

        return true
    }

    private fun updateState(state: MainActivityViewModel.MainActivityViewModelState) {
        when (state){
            is MainActivityViewModel.MainActivityViewModelState.FetchSuccess -> {
                GlideApp.with(this)
                    .load(state.cardModel.imageUris.large)
                    .into(image)
            }
            is MainActivityViewModel.MainActivityViewModelState.AutoCompleteSuccess -> {
                autocompleteResult = state.autocompleteResult
                if (searchView.suggestionsAdapter == null) {
                    searchView.suggestionsAdapter = AutocompleteAdapter(searchView.context, autocompleteResult)
                } else {
                    searchView.suggestionsAdapter.changeCursor(AutocompleteCursor.Build(autocompleteResult))
                }
            }
        }

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
