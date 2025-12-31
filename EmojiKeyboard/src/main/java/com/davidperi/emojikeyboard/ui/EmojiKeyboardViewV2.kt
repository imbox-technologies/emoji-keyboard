package com.davidperi.emojikeyboard.ui

import android.content.Context
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import com.davidperi.emojikeyboard.ui.model.EmojiKeyboardConfig
import com.davidperi.emojikeyboard.ui.model.EmojiLayoutMode
import com.davidperi.emojikeyboard.utils.DisplayUtils.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class EmojiKeyboardViewV2(context: Context) : LinearLayout(context), EmojiDelegate {

    // Components
    private val categoryBar = CategoryBar(context, this)
    private val backspace = Backspace(context, this)
    private val searchBar = SearchBar(context, this)
    private val searchResults = SearchResults(context, this)
    private val emojiGrid = EmojiGrid(context, this)

    // Logic
    private val searchEngine = EmojiSearchEngine()
    private val recentManager = RecentEmojiManager(context)

    private val viewScope = CoroutineScope(Dispatchers.Main + Job())
    private val searchJob: Job? = null

    // Status and Config
    private var config = EmojiKeyboardConfig()
    private var targetEditText: EditText? = null
    private var categoryRanges: List<IntRange> = emptyList()
    private var recentsCount = 0


    init {
        orientation = VERTICAL
        setupLayout()
    }


    // Public (internal) API
    fun setConfig(config: EmojiKeyboardConfig) {
        this.config = config
        setupLayout()
    }


    // Layout Modes and Setup
    private fun setupLayout() {
        removeAllViews()

        when (config.layoutMode) {
            EmojiLayoutMode.ROBOT -> {}
            EmojiLayoutMode.COOPER -> {}
        }
    }

    private fun addCategoryBar() {
        val params = LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            setMargins(8.dp, marginTop, 8.dp, marginBottom)
        }
        addView(categoryBar, params)
    }


    // Delegate
    override fun onCategorySelected(index: Int, progress: Float) {
        TODO("Not yet implemented")
    }

    override fun onEmojiClicked(unicode: String) {
        TODO("Not yet implemented")
    }

    override fun onGridScrolled(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onQueryChanged(query: String) {
        TODO("Not yet implemented")
    }

    override fun onBackspacePressed() {
        TODO("Not yet implemented")
    }
}