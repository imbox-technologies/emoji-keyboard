package com.davidperi.emojikeyboard.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.util.AttributeSet
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.EditText
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.davidperi.emojikeyboard.R
import com.davidperi.emojikeyboard.databinding.EmojiKeyboardPopupBinding
import com.davidperi.emojikeyboard.model.Category
import com.davidperi.emojikeyboard.model.Emoji
import com.davidperi.emojikeyboard.ui.adapter.EmojiAdapter
import com.davidperi.emojikeyboard.ui.adapter.EmojiListItem
import com.davidperi.emojikeyboard.ui.adapter.EmojiListMapper
import com.davidperi.emojikeyboard.ui.model.EmojiKeyboardConfig
import com.davidperi.emojikeyboard.ui.model.EmojiLayoutMode
import com.davidperi.emojikeyboard.ui.span.EmojiTypefaceSpan
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

class EmojiKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private val binding = EmojiKeyboardPopupBinding.inflate(LayoutInflater.from(context), this, true)
    private val viewScope = CoroutineScope(Dispatchers.Main + Job())

    // Config
    private var config: EmojiKeyboardConfig = EmojiKeyboardConfig()

    private val isVerticalLayout: Boolean
        get() = config.layoutMode in listOf(EmojiLayoutMode.ROBOT)  // for future reference
    private val spanCount: Int
        get() = if (isVerticalLayout) VERTICAL_SPAN_COUNT else HORIZONTAL_SPAN_COUNT

    // Controllers
    private var stateMachine: PopupStateMachine? = null
    private val searchEngine = EmojiSearchEngine()
    private val fontManager = EmojiFontManager
    private val recentManager = RecentEmojiManager(context)

    // Adapters
    private val emojisAdapter = EmojiAdapter { emoji -> onEmojiSelected(emoji.unicode) }
    private val searchAdapter = EmojiAdapter { emoji -> onEmojiSelected(emoji.unicode) }
    private val recentAdapter = EmojiAdapter { emoji -> onEmojiSelected(emoji.unicode) }

    private var targetEditText: EditText? = null

    private var categoryRanges: List<IntRange> = emptyList()
    private var recentsCache: List<EmojiListItem> = emptyList()
    private var isProgrammaticScroll = false

    private var searchJob: Job? = null

    private val deleteHandler = Handler(Looper.getMainLooper())
    private val deleteRepeater = object : Runnable {
        override fun run() {
            handleBackspace()
            deleteHandler.postDelayed(this, 50L)
        }
    }

    companion object {
        private const val HORIZONTAL_SPAN_COUNT = 4
        private const val VERTICAL_SPAN_COUNT = 9
        const val HORIZONTAL_GAP_SIZE = 40  // dp
    }

    init {
        clipChildren = true
        applyConfig()
        setupDeleteButton()
        setupSearchBar()
    }

    // PUBLIC API
    enum class PopupState { COLLAPSED, BEHIND, FOCUSED, SEARCHING }

    fun setupWith(editText: EditText) {
        this.targetEditText = editText
        stateMachine = PopupStateMachine(this, editText)
    }

    fun configure(newConfig: EmojiKeyboardConfig) {
        config = newConfig
        applyConfig()
    }

    fun toggle() { stateMachine?.toggle() }
    fun hide() { stateMachine?.hide() }
    fun state() = stateMachine?.state

    fun onStateChangedListener(callback: (PopupState) -> Unit) {
        stateMachine?.onStateChanged = callback
    }


    // CONFIGURATION (One-time or with dynamic Config changes)
    private fun applyConfig() {
        config.font?.let { fontManager.setCustomTypeface(it) }

        setupLayoutMode(config.layoutMode)  // depends on Config

        setupEmojisAdapter()  // depends on Config
        setupSearchAdapter()  // doesn't depend on Config

        loadEmojis()
    }

    private fun setupLayoutMode(mode: EmojiLayoutMode) {
        val set = ConstraintSet()
        set.clone(binding.root)

        when (mode) {
            EmojiLayoutMode.ROBOT -> {
                // Top Bar
                set.connect(binding.topBar.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                set.clear(binding.topBar.id, ConstraintSet.BOTTOM)

                // Search Bar
                set.connect(binding.searchBar.root.id, ConstraintSet.TOP, binding.topBar.id, ConstraintSet.BOTTOM)

                // Recycler
                set.connect(binding.rvEmojis.id, ConstraintSet.TOP, binding.searchBar.root.id, ConstraintSet.BOTTOM)
                set.connect(binding.rvEmojis.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
                // set.constrainHeight(binding.rvEmojis.id, ConstraintSet.MATCH_CONSTRAINT)
            }

            EmojiLayoutMode.COOPER -> {
                // Top Bar
                set.clear(binding.topBar.id, ConstraintSet.TOP)
                set.connect(binding.topBar.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

                // Search Bar
                set.connect(binding.searchBar.root.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)

                // Recycler
                set.connect(binding.rvEmojis.id, ConstraintSet.TOP, binding.searchBar.root.id, ConstraintSet.BOTTOM)
                set.connect(binding.rvEmojis.id, ConstraintSet.BOTTOM, binding.topBar.id, ConstraintSet.TOP)
                // set.constrainHeight(binding.rvEmojis.id, ConstraintSet.WRAP_CONTENT)
            }
        }

        set.applyTo(binding.root)
    }

    private fun setupEmojisAdapter() {
        // Setup GridLayoutManager
        val orientation = if (isVerticalLayout) RecyclerView.VERTICAL else RecyclerView.HORIZONTAL
        val gridManager = GridLayoutManager(context, spanCount, orientation, false)
        gridManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val recentCount = recentAdapter.itemCount
                if (position < recentCount) {
                    return 1
                }

                val localPos = position - recentCount
                if (localPos < 0 || localPos >= emojisAdapter.itemCount) {
                    return 1
                }

                if (isVerticalLayout) {
                    val type = emojisAdapter.getItemViewType(localPos)
                    return if (type == EmojiAdapter.Companion.VIEW_TYPE_EMOJI) 1 else spanCount
                } else {
                    return 1
                }
            }
        }

        // Connect Recycler with Adapter and GridLayoutManager
        binding.rvEmojis.apply {
            layoutManager = gridManager
            adapter = ConcatAdapter(
                ConcatAdapter.Config.Builder().setIsolateViewTypes(false).build(),
                recentAdapter,
                emojisAdapter
            )
            setHasFixedSize(true)
        }

        // Scroll listener for categories
        binding.rvEmojis.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (isProgrammaticScroll) {
                    isProgrammaticScroll = false
                    return
                }

                val lm = recyclerView.layoutManager as GridLayoutManager
                val firstPos = lm.findFirstVisibleItemPosition()

                if (firstPos == RecyclerView.NO_POSITION) return

                val recentCount = recentAdapter.itemCount

                if (firstPos < recentCount) {
                    binding.categoriesSelector.setSelectedCategory(0)
                    return
                }

                val localPos = firstPos - recentCount
                val index = categoryRanges.indexOfFirst { range -> localPos in range }
                if (index >= 0) {
                    binding.categoriesSelector.setSelectedCategory(index + 1)
                }
            }
        })
    }

    private fun setupSearchAdapter() {
        // Connect Recycler with Adapter and LinearLayoutManager
        binding.rvSearch.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = searchAdapter
            setHasFixedSize(true)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupDeleteButton() {
        binding.backspace.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    handleBackspace()
                    deleteHandler.postDelayed(deleteRepeater, 400L)
                    v.isPressed = true
                    true
                }

                MotionEvent.ACTION_UP -> {
                    deleteHandler.removeCallbacks(deleteRepeater)
                    v.isPressed = false
                    v.performClick()
                    true
                }

                MotionEvent.ACTION_CANCEL -> {
                    deleteHandler.removeCallbacks(deleteRepeater)
                    v.isPressed = false
                    true
                }

                else -> false
            }
        }
    }

    private fun setupSearchBar() {
        binding.searchBar.icClear.setOnClickListener {
            binding.searchBar.searchBar.text.clear()
        }

        binding.searchBar.searchBar.addTextChangedListener { s ->
            val query = s?.toString() ?: ""
            binding.searchBar.icClear.isVisible = query.isNotEmpty()
            performSearch(query)
        }
    }

    private fun loadEmojis() {
        viewScope.launch {
            val (categories, items, ranges) = withContext(Dispatchers.IO) {
                val categories = config.provider.getCategories(context)
                val (items, ranges) = EmojiListMapper.map(categories, isVerticalLayout, spanCount)
                searchEngine.initialize(categories)
                Triple(categories, items, ranges)
            }

            val recents = listOf(Category("recent", "Recent Emojis", R.drawable.clock, emptyList()))

            binding.categoriesSelector.setup(recents + categories)
            categoryRanges = ranges

            binding.categoriesSelector.setSelectedCategory(0)
            binding.categoriesSelector.setOnSeekListener(::onSeekListener)

            emojisAdapter.submitList(items)
            refreshRecentList()
        }
    }

    private fun onSeekListener(index: Int, progress: Float) {
        if (index == 0) {
            isProgrammaticScroll = true
            val lm = binding.rvEmojis.layoutManager as GridLayoutManager
            lm.scrollToPositionWithOffset(0, 0)
            return
        }

        val staticIndex = index - 1
        val range = categoryRanges.getOrNull(staticIndex) ?: return
        val totalItemsInCategory = range.last - range.first
        val offsetItems = (totalItemsInCategory * progress).toInt()
        var targetLocalPos = range.first + offsetItems
        val remainder = targetLocalPos % spanCount
        targetLocalPos -= remainder
        val globalPos = targetLocalPos + recentAdapter.itemCount

        isProgrammaticScroll = true
        val lm = binding.rvEmojis.layoutManager as GridLayoutManager
        lm.scrollToPositionWithOffset(globalPos, 0)
    }

    private fun onEmojiSelected(unicode: String) {
        val editText = targetEditText ?: return

        val start = editText.selectionStart.coerceAtLeast(0)
        val end = editText.selectionEnd.coerceAtLeast(0)

        val spannableString = SpannableString(unicode)
        val typeface = EmojiFontManager.getTypeface(context)

        spannableString.setSpan(
            EmojiTypefaceSpan(typeface),
            0,
            spannableString.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        editText.text.replace(
            min(start, end),
            max(start, end),
            spannableString
        )

        recentManager.addEmoji(unicode)
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    private fun refreshRecentList() {
        val recents = recentManager.getRecentUnicodes()
        val recentsItem = EmojiListMapper.mapRecents(recents, isVerticalLayout, spanCount)
        recentAdapter.submitList(recentsItem.items)
        searchAdapter.submitList(recentsItem.items)
        recentsCache = recentsItem.items
    }

    private fun handleBackspace() {
        val editText = targetEditText ?: return
        if (editText.text.isNullOrEmpty()) return
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)
        editText.dispatchKeyEvent(event)
    }

    private fun performSearch(query: String) {
        searchJob?.cancel()

        if (query.isEmpty()) {
            searchAdapter.submitList(recentsCache) {
                binding.rvSearch.scrollToPosition(0)
            }
            binding.tvNoSearchResults.isVisible = false
        } else {
            searchJob = viewScope.launch {
                delay(150)
                val results = searchEngine.search(query)
                updateAdapterWithSearchResults(results)
            }
        }
    }

    private fun updateAdapterWithSearchResults(results: List<Emoji>) {
        if (results.isEmpty()) {
            binding.tvNoSearchResults.isVisible = true
            searchAdapter.submitList(emptyList())
        } else {
            binding.tvNoSearchResults.isVisible = false
            val listItems = results.map { EmojiListItem.EmojiKey(it) }
            searchAdapter.submitList(listItems) {
                binding.rvSearch.scrollToPosition(0)
            }
        }
    }


    internal val searchBar = binding.searchBar.root
    internal val topBar = binding.topBar
    internal val rvKeyboard = binding.rvEmojis
    internal val searchResults = binding.searchResults

    internal fun setInternalContentHeight(newHeight: Int) {
        binding.root.updateLayoutParams {
            height = newHeight
            foregroundGravity = Gravity.BOTTOM
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        targetEditText = null
        viewScope.cancel()
    }
}
