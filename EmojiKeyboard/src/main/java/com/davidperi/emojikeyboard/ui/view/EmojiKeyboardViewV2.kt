package com.davidperi.emojikeyboard.ui.view

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.davidperi.emojikeyboard.R
import com.davidperi.emojikeyboard.data.model.Category
import com.davidperi.emojikeyboard.EmojiKeyboardConfig
import com.davidperi.emojikeyboard.EmojiLayoutMode
import com.davidperi.emojikeyboard.data.prefs.PrefsManager
import com.davidperi.emojikeyboard.ui.view.EmojiDelegate
import com.davidperi.emojikeyboard.logic.EmojiSearchEngine
import com.davidperi.emojikeyboard.ui.state.PopupState
import com.davidperi.emojikeyboard.logic.RecentEmojiManager
import com.davidperi.emojikeyboard.ui.adapter.EmojiListItem
import com.davidperi.emojikeyboard.ui.adapter.EmojiListMapper
import com.davidperi.emojikeyboard.ui.span.EmojiTypefaceSpan
import com.davidperi.emojikeyboard.ui.view.components.Backspace
import com.davidperi.emojikeyboard.ui.view.components.CategoryBar
import com.davidperi.emojikeyboard.ui.view.components.EmojiGrid
import com.davidperi.emojikeyboard.ui.view.components.SearchBar
import com.davidperi.emojikeyboard.ui.view.components.SearchResults
import com.davidperi.emojikeyboard.utils.DisplayUtils.dp
import com.davidperi.emojikeyboard.utils.EmojiFontManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

internal class EmojiKeyboardViewV2(context: Context) : LinearLayout(context), EmojiDelegate {

    // Components
    private val categoryBar = CategoryBar(context, this)
    private val backspace = Backspace(context, this)
    private val searchBar = SearchBar(context, this)
    private val searchResults = SearchResults(context, this)
    private val emojiGrid = EmojiGrid(context, this)

    // Logic
    private val searchEngine = EmojiSearchEngine()
    private val recentManager = RecentEmojiManager(context)
    private val prefs = PrefsManager(context)
    private val viewScope = CoroutineScope(Dispatchers.Main + Job())
    private var searchJob: Job? = null

    // Status and Config
    private var config = EmojiKeyboardConfig()
    private var targetEditText: EditText? = null
    private var categoryRanges: List<IntRange> = emptyList()
    private var recentCount = 0


    init {
        orientation = VERTICAL
        setBackgroundColor(context.getColor(R.color.emoji_keyboard_gray_background))
        setupLayout()
        loadData()
    }


    // Public (internal) API
    fun setupWith(editText: EditText) {
        this.targetEditText = editText
    }

    fun setConfig(config: EmojiKeyboardConfig) {
        this.config = config
        setupLayout()
        loadData()
    }

    fun onStateChanged(state: PopupState) {
        when (state) {
            PopupState.COLLAPSED,
            PopupState.BEHIND,
            PopupState.FOCUSED -> {
                categoryBar.isVisible = true
                backspace.isVisible = true
                emojiGrid.isVisible = true
                searchResults.isVisible = false
                searchBar.isVisible = true
                searchBar.clearInput()
                refreshRecents()
            }
            PopupState.SEARCHING -> {
                categoryBar.isVisible = false
                backspace.isVisible = false
                emojiGrid.isVisible = false
                searchResults.isVisible = true
                searchBar.isVisible = true
            }
        }
    }

    fun getSearchContentHeight(): Int {
        val resultsHeight = calculateEmojiRowHeight()

        MeasureSpec.makeMeasureSpec(resources.displayMetrics.widthPixels, MeasureSpec.AT_MOST)
        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        val searchBarHeight = searchBar.measuredHeight

        val params = searchBar.layoutParams as? MarginLayoutParams
        val marginTop = params?.topMargin ?: 0
        val marginBottom = params?.bottomMargin ?: 0

        return marginTop + searchBarHeight + marginBottom + resultsHeight
    }

    fun setInternalContentHeight(newHeight: Int) {
        this.updateLayoutParams {
            height = newHeight
        }
    }

    fun isSearchFocused(): Boolean {
        return searchBar.isInputFocused()
    }


    // BUILD LAYOUT
    private fun setupLayout() {
        removeAllViews()

        val isVertical = config.layoutMode in listOf(EmojiLayoutMode.ROBOT)
        val spanCount = if (isVertical) 9 else 4
        emojiGrid.setup(spanCount, if (isVertical) VERTICAL else HORIZONTAL)

        when (config.layoutMode) {
            EmojiLayoutMode.ROBOT -> {
                addTopBar()
                addSearchBar()
                addSearchResults()
                addEmojiGrid()
            }
            EmojiLayoutMode.COOPER -> {
                addSearchBar()
                addSearchResults()
                addEmojiGrid()
                addTopBar()
            }
        }
    }

    private fun addTopBar() {
        val container = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(8.dp, 4.dp, 8.dp, 4.dp)
        }

        val catParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        container.addView(categoryBar, catParams)

        val backParams = LayoutParams(48.dp, 48.dp)
        container.addView(backspace, backParams)

        addView(container, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
    }

    private fun addSearchBar() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            setMargins(8.dp, 8.dp, 8.dp, 8.dp)
        }
        addView(searchBar, params)
    }

    private fun addSearchResults() {
        addView(searchResults, LayoutParams(LayoutParams.MATCH_PARENT, calculateEmojiRowHeight()))
        searchResults.isVisible = false
    }

    private fun addEmojiGrid() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f)
        addView(emojiGrid, params)
    }


    // LOAD DATA
    private fun loadData() {
        viewScope.launch {
            val (categories, items, ranges) = withContext(Dispatchers.IO) {
                val cats = config.provider.getCategories(context)
                val isVertical = config.layoutMode == EmojiLayoutMode.ROBOT
                val span = if (isVertical) 9 else 4
                val (mappedItems, mappedRanges) = EmojiListMapper.map(cats, isVertical, span)

                searchEngine.initialize(cats)
                Triple(cats, mappedItems, mappedRanges)
            }

            categoryRanges = ranges

            val recentCat = Category("recent", "Recents", R.drawable.clock, emptyList())
            categoryBar.setup(listOf(recentCat) + categories)
            categoryBar.setSelectedCategory(0)

            emojiGrid.submitEmojis(items)
            refreshRecents()
        }
    }

    private fun refreshRecents() {
        val recents = recentManager.getRecentUnicodes()
        val isVertical = config.layoutMode == EmojiLayoutMode.ROBOT
        val span = if (isVertical) 9 else 4
        val recentsItem = EmojiListMapper.mapRecents(recents, isVertical, span)

        recentCount = recentsItem.items.size
        emojiGrid.submitRecent(recentsItem.items)
    }


    // DELEGATE INTERFACE
    override fun onCategorySelected(index: Int, progress: Float) {
        if (index == 0) {
            emojiGrid.scrollToPosition(0, 0)
            return
        }

        val range = categoryRanges.getOrNull(index - 1) ?: return
        val isVertical = config.layoutMode == EmojiLayoutMode.ROBOT
        val span = if (isVertical) 9 else 4

        val totalItemsInCategory = range.last - range.first
        val offsetItems = (totalItemsInCategory * progress).toInt()

        var targetLocalPos = range.first + offsetItems
        targetLocalPos -= (targetLocalPos % span)

        emojiGrid.scrollToPosition(targetLocalPos + recentCount, 0)
    }

    override fun onEmojiClicked(unicode: String) {
        val editText = targetEditText ?: return
        val start = max(editText.selectionStart, 0)
        val end = max(editText.selectionEnd, 0)

        val typeface = EmojiFontManager.getTypeface(context)
        val spannable = SpannableString(unicode).apply {
            setSpan(EmojiTypefaceSpan(typeface), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        editText.text.replace(min(start, end), max(start, end), spannable)

        recentManager.addEmoji(unicode)
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    override fun onGridScrolled(position: Int) {
        if (position < recentCount) {
            categoryBar.setSelectedCategory(0)
            return
        }

        val localPos = position - recentCount
        val index = categoryRanges.indexOfFirst { localPos in it }
        if (index >= 0) {
            categoryBar.setSelectedCategory(index + 1)
        }
    }

    override fun onQueryChanged(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            searchResults.isVisible = false
            return
        }

        searchJob = viewScope.launch {
            delay(150)
            val results = searchEngine.search(query)
            searchResults.isVisible = true
            searchResults.setResults(results.map { EmojiListItem.EmojiKey(it) })
        }
    }

    override fun onBackspacePressed() {
        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)
        targetEditText?.dispatchKeyEvent(event)
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }


    // AUXILIARY
    private fun calculateEmojiRowHeight(): Int {
        val isVertical = config.layoutMode == EmojiLayoutMode.ROBOT

        return if (isVertical) {
            val totalWidth = if (width > 0) width else resources.displayMetrics.widthPixels
            totalWidth / 9
        } else {
            emojiGrid.measuredHeight / 4
        }

    }


    // LIFECYCLE
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewScope.cancel()
        recentManager.forcePersist()
    }

}