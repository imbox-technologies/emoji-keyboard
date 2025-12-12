package com.davidperi.emojikeyboard.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.LinearLayout
import androidx.core.view.children
import com.davidperi.emojikeyboard.R
import com.davidperi.emojikeyboard.databinding.ItemCategoryBinding
import com.davidperi.emojikeyboard.model.Category
import com.davidperi.emojikeyboard.utils.DisplayUtils.inflate
import androidx.core.view.isEmpty
import kotlin.math.abs

class CategoryBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : LinearLayout(context, attrs) {

    private var onSeekListener: ((index: Int, progress: Float) -> Unit)? = null
    private var lastSelectedIndex = -1

    private var initialDownX = 0f
    private var isScrubbing = false
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    init {
        orientation = HORIZONTAL
    }

    fun setup(categories: List<Category>) {
        removeAllViews()
        lastSelectedIndex = -1

        categories.forEachIndexed { index, category ->
            val itemView = this.inflate(R.layout.item_category)
            val itemBinding = ItemCategoryBinding.bind(itemView)
            itemBinding.categoryIcon.setImageResource(category.icon)

            val params = LayoutParams(0, LayoutParams.MATCH_PARENT, 1f)
            itemView.layoutParams = params

            itemView.isClickable = false
            itemView.isFocusable = false
            addView(itemView)
        }
    }

    fun setSelectedCategory(index: Int) {
        if (index == lastSelectedIndex) return
        if (index !in 0 until childCount) return

        lastSelectedIndex = index
        children.forEachIndexed { i, view ->
            view.isSelected = (i == index)
        }
    }

    fun setOnSeekListener(listener: (Int, Float) -> Unit) {
        onSeekListener = listener
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isEmpty()) return false

        val widthPerItem = width.toFloat() / childCount

        val rawIndex = (event.x / widthPerItem).toInt()
        val index = rawIndex.coerceIn(0, childCount - 1)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialDownX = event.x
                isScrubbing = false

                if (index != lastSelectedIndex) {
                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    setSelectedCategory(index)
                }

                onSeekListener?.invoke(index, 0.0f)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (!isScrubbing) {
                    if (abs(event.x - initialDownX) > touchSlop) {
                        isScrubbing = true
                    }
                }

                val localX = event.x % widthPerItem
                val progress = when {
                    rawIndex < 0 -> 0f
                    rawIndex >= childCount -> 1f
                    else -> (localX / widthPerItem).coerceIn(0f, 1f)
                }

                if (isScrubbing) {
                    if (index != lastSelectedIndex) {
                        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        setSelectedCategory(index)
                    }
                    onSeekListener?.invoke(index, progress)
                } else {
                    if (index != lastSelectedIndex) {
                        setSelectedCategory(index)
                        onSeekListener?.invoke(index, 0.0f)
                    }
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isScrubbing = false
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
