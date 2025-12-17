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
import androidx.core.view.updateLayoutParams
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.davidperi.emojikeyboard.databinding.EmojiKeyboardPopupBinding
import com.davidperi.emojikeyboard.model.Emoji
import com.davidperi.emojikeyboard.ui.adapter.CategoryGapDecoration
import com.davidperi.emojikeyboard.ui.adapter.EmojiAdapter
import com.davidperi.emojikeyboard.ui.adapter.EmojiListItem
import com.davidperi.emojikeyboard.ui.adapter.EmojiListMapper
import com.davidperi.emojikeyboard.ui.model.EmojiKeyboardConfig
import com.davidperi.emojikeyboard.ui.model.EmojiLayoutMode
import com.davidperi.emojikeyboard.ui.span.EmojiTypefaceSpan
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

class EmojiKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private val binding = EmojiKeyboardPopupBinding.inflate(LayoutInflater.from(context), this, true)

    private var config: EmojiKeyboardConfig = EmojiKeyboardConfig()
    private var targetEditText: EditText? = null
    private var controller: PopupStateMachine? = null
    private var categoryRanges: List<IntRange> = emptyList()
    private var isProgrammaticScroll = false
    private var cachedMappedItems: List<EmojiListItem>? = null

    private val adapter = EmojiAdapter { emoji ->
        onEmojiSelected(emoji.unicode)
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }
    private val searchAdapter = EmojiAdapter { emoji ->
        onEmojiSelected(emoji.unicode)
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }
    private val searchEngine = EmojiSearchEngine()

    private var searchJob: Job? = null

    private val viewScope = CoroutineScope(Dispatchers.Main + Job())
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
        private const val HORIZONTAL_GAP_SIZE = 16  // dp
    }

    init {
        clipChildren = true
        applyConfig(config)
        setupDeleteButton()
        setupSearchBar()
    }

    // PUBLIC API
    enum class PopupState { COLLAPSED, BEHIND, FOCUSED, SEARCHING }

    fun setupWith(editText: EditText) {
        this.targetEditText = editText
        controller = PopupStateMachine(this, editText)
    }

    fun configure(newConfig: EmojiKeyboardConfig) {
        this.config = newConfig
        applyConfig(newConfig)
    }

    fun toggle() { controller?.toggle() }
    fun hide() { controller?.hide() }
    fun state() = controller?.state

    fun onStateChangedListener(callback: (PopupState) -> Unit) {
        controller?.onStateChanged = callback
    }


    private fun applyConfig(config: EmojiKeyboardConfig) {
        config.font?.let { EmojiFontManager.setCustomTypeface(it) }
        setupLayoutMode(config.layoutMode)
        setupAdapter(config)
        setupSearchAdapter()
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

    private fun setupAdapter(config: EmojiKeyboardConfig) {
        val isVertical = config.layoutMode == EmojiLayoutMode.ROBOT
        val spanCount = if (isVertical) VERTICAL_SPAN_COUNT else HORIZONTAL_SPAN_COUNT
        val orientation = if (isVertical) RecyclerView.VERTICAL else RecyclerView.HORIZONTAL

        val gridManager = GridLayoutManager(context, spanCount, orientation, false)

        gridManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if (isVertical) {
                    val type = adapter.getItemViewType(position)
                    return if (type == EmojiAdapter.Companion.VIEW_TYPE_EMOJI) 1 else spanCount
                } else {
                    return 1
                }
            }
        }

        binding.rvEmojis.apply {
            layoutManager = gridManager
            this.adapter = this@EmojiKeyboardView.adapter
            setHasFixedSize(true)
        }

        binding.rvEmojis.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (isProgrammaticScroll) {
                    isProgrammaticScroll = false
                    return
                }

                val lm = recyclerView.layoutManager as GridLayoutManager
                val firstPos = lm.findFirstVisibleItemPosition()

                if (firstPos == RecyclerView.NO_POSITION) return

                val index = categoryRanges.indexOfFirst { range -> firstPos in range }
                if (index >= 0) {
                    binding.categoriesSelector.setSelectedCategory(index)
                }
            }
        })
    }

    private fun setupSearchAdapter() {
        binding.rvSearch.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = searchAdapter
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
        // TODO: add button to clear

        binding.searchBar.searchBar.addTextChangedListener { s ->
            val query = s?.toString() ?: ""
            performSearch(query)
        }
    }

    private fun loadEmojis() {
        viewScope.launch {
            val result = withContext(Dispatchers.IO) {
                val isHorizontal = config.layoutMode == EmojiLayoutMode.COOPER
                val spanCount = if (isHorizontal) HORIZONTAL_SPAN_COUNT else VERTICAL_SPAN_COUNT
                val categories = config.provider.getCategories(context)

                searchEngine.initialize(categories)

                val mappedResult = EmojiListMapper.map(categories, config.layoutMode, spanCount)
                Pair(categories, mappedResult)
            }

            val (categories, mappedResult) = result

            cachedMappedItems = mappedResult.items

            binding.categoriesSelector.setup(categories)
            categoryRanges = mappedResult.categoryRanges

            binding.categoriesSelector.setSelectedCategory(0)
            binding.categoriesSelector.setOnSeekListener { index, progress ->
                val range = categoryRanges.getOrNull(index) ?: return@setOnSeekListener
                val totalItemsInCategory = range.last - range.first
                val offsetItems = (totalItemsInCategory * progress).toInt()
                val targetPosition = range.first + offsetItems

                isProgrammaticScroll = true
                val lm = binding.rvEmojis.layoutManager as GridLayoutManager
                lm.scrollToPositionWithOffset(targetPosition, 0)
            }

            if (config.layoutMode == EmojiLayoutMode.COOPER) {
                val isHorizontal = true
                val spanCount = HORIZONTAL_SPAN_COUNT
                binding.rvEmojis.addItemDecoration(
                    CategoryGapDecoration(
                        categoryRanges = mappedResult.categoryRanges,
                        gapSize = HORIZONTAL_GAP_SIZE.dp,
                        spanCount = spanCount
                    )
                )
            }

            adapter.submitList(mappedResult.items)
        }
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
            restoreCategoryView()
            return
        }

        searchJob = viewScope.launch {
            delay(150)
            val results = searchEngine.search(query)
            updateAdapterWithSearchResults(results)
        }
    }

    private fun updateAdapterWithSearchResults(results: List<Emoji>) {
        val listItems = results.map { EmojiListItem.EmojiKey(it) }
        searchAdapter.submitList(listItems) {
            // binding.rvEmojis.scrollToPosition(0)
        }
    }

    private fun restoreCategoryView() {
        searchJob?.cancel()

        cachedMappedItems?.let {
            adapter.submitList(it)
        }
    }


    internal val searchBar = binding.searchBar.root
    internal val topBar = binding.topBar
    internal val rvKeyboard = binding.rvEmojis
    internal val rvSearch = binding.rvSearch

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
