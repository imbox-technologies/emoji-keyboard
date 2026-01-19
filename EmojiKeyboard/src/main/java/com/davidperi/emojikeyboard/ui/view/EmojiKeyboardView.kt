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
import com.davidperi.emojikeyboard.EmojiKeyboardConfig
import com.davidperi.emojikeyboard.EmojiLayoutMode
import com.davidperi.emojikeyboard.R
import com.davidperi.emojikeyboard.data.model.Category
import com.davidperi.emojikeyboard.logic.EmojiSearchEngine
import com.davidperi.emojikeyboard.logic.RecentEmojiManager
import com.davidperi.emojikeyboard.ui.adapter.EmojiListItem
import com.davidperi.emojikeyboard.ui.adapter.EmojiListMapper
import com.davidperi.emojikeyboard.ui.span.EmojiTypefaceSpan
import com.davidperi.emojikeyboard.ui.state.PopupState
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

internal class EmojiKeyboardView(context: Context) : LinearLayout(context), EmojiDelegate {

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
    private var searchJob: Job? = null

    // Status and Config
    private var config = EmojiKeyboardConfig()
    private var targetEditText: EditText? = null
    private var categoryRanges: List<IntRange> = emptyList()
    private var recentCount = 0
    private var loadedCategories: List<Category> = emptyList()
    private var currentSpan = 4

    // Callbacks
    var onSearchBarFocusChange: ((Boolean) -> Unit)? = null


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
                searchResults.updateLayoutParams { height = calculateEmojiRowHeight() }
                searchResults.isVisible = true
                searchBar.isVisible = true
                onQueryChanged("")
            }
        }
    }

    fun getSearchContentHeight(): Int {
        val resultsHeight = calculateEmojiRowHeight()
        val searchBarHeight = searchBar.measuredHeight
        val params = searchBar.layoutParams as? MarginLayoutParams
        val marginTop = params?.topMargin ?: 0
        val marginBottom = params?.bottomMargin ?: 0
        val resultsMarginBottom = (searchResults.layoutParams as? MarginLayoutParams)?.bottomMargin ?: 0

        return marginTop + searchBarHeight + marginBottom + resultsHeight + resultsMarginBottom
    }

    fun updateContentHeight(newHeight: Int) {
        if (height != newHeight) {
            updateLayoutParams { height = newHeight }
        }
    }


    // BUILD LAYOUT
    private fun setupLayout() {
        removeAllViews()

        val isVertical = config.layoutMode == EmojiLayoutMode.ROBOT
        currentSpan = if (isVertical) 9 else 4
        emojiGrid.setup(currentSpan, if (isVertical) VERTICAL else HORIZONTAL)

        when (config.layoutMode) {
            EmojiLayoutMode.ROBOT -> {
                emojiGrid.onSpanCountChanged = null
                addTopBar()
                addSearchBar()
                addSearchResults()
                addEmojiGrid()
            }
            EmojiLayoutMode.COOPER -> {
                emojiGrid.onSpanCountChanged = { newSpan -> onSpanCountChanged(newSpan) }
                addSearchBar()
                addSearchResults()
                addEmojiGrid()
                addTopBar()
            }
        }
    }

    private fun onSpanCountChanged(newSpan: Int) {
        if (newSpan == currentSpan) return
        currentSpan = newSpan

        emojiGrid.setup(newSpan, HORIZONTAL)

        if (loadedCategories.isNotEmpty()) {
            val (mappedItems, mappedRanges) = EmojiListMapper.map(
                loadedCategories,
                isVertical = false,
                spanCount = newSpan
            )
            categoryRanges = mappedRanges
            emojiGrid.submitEmojis(mappedItems)
            refreshRecents()
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
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            setMargins(0, 0, 0, 8.dp)
        }
        addView(searchResults, params)
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
                val (mappedItems, mappedRanges) = EmojiListMapper.map(cats, isVertical, currentSpan)

                searchEngine.initialize(cats)
                Triple(cats, mappedItems, mappedRanges)
            }

            loadedCategories = categories
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
        val recentsItem = EmojiListMapper.mapRecents(recents, isVertical, currentSpan)

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

        val totalItemsInCategory = range.last - range.first
        val offsetItems = (totalItemsInCategory * progress).toInt()

        var targetLocalPos = range.first + offsetItems
        targetLocalPos -= (targetLocalPos % currentSpan)

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
            val recentUnicodes = recentManager.getRecentUnicodes()
            searchResults.setResults(EmojiListMapper.mapSuggestions(recentUnicodes))
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

    override fun onSearchFocusChange(hasFocus: Boolean) {
        onSearchBarFocusChange?.invoke(hasFocus)
    }


    // AUXILIARY
    private fun calculateEmojiRowHeight(): Int {
        val isVertical = config.layoutMode == EmojiLayoutMode.ROBOT

        return if (isVertical) {
            val totalWidth = if (width > 0) width else resources.displayMetrics.widthPixels
            totalWidth / 9
        } else {
            emojiGrid.height / currentSpan
        }
    }


    // LIFECYCLE
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewScope.cancel()
        recentManager.forcePersist()
    }

}