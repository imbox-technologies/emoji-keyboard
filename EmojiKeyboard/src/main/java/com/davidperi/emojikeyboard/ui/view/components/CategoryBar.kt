package com.davidperi.emojikeyboard.ui.view.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.children
import androidx.core.view.isEmpty
import com.davidperi.emojikeyboard.R
import com.davidperi.emojikeyboard.data.model.Category
import com.davidperi.emojikeyboard.ui.view.EmojiDelegate
import com.davidperi.emojikeyboard.utils.DisplayUtils.dp
import kotlin.math.abs

internal class CategoryBar(context: Context, private val delegate: EmojiDelegate) :
    LinearLayout(context) {

    private var lastSelectedIndex: Int = -1

    // Slide through categories
    private var initialDownX = 0f
    private var isSliding = false
    private var touchSlop = ViewConfiguration.get(context).scaledTouchSlop


    init {
        orientation = HORIZONTAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 45.dp)
    }


    // Public (internal) API
    fun setup(categories: List<Category>) {
        removeAllViews()
        lastSelectedIndex = -1

        val buttonSize = context.resources.getDimensionPixelSize(R.dimen.EmojiButtonSize)
        val iconTint = createCategoryIconTint(context)
        val backgroundDrawable = { createCategoryBackground(context) }

        categories.forEachIndexed { index, category ->
            // Frame Layout that wraps each category icon
            val itemContainer = FrameLayout(context).apply {
                layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
                setPadding(2.dp, 2.dp, 2.dp, 2.dp)
            }

            // Category icon
            val iconView = AppCompatImageView(context).apply {
                id = generateViewId()
                setImageResource(category.icon)
                layoutParams = FrameLayout.LayoutParams(buttonSize, buttonSize, Gravity.CENTER)
                background = backgroundDrawable()
                imageTintList = iconTint
                scaleType = ImageView.ScaleType.FIT_CENTER
                setPadding(4.dp, 4.dp, 4.dp, 4.dp)
                isDuplicateParentStateEnabled = true
            }

            itemContainer.addView(iconView)
            addView(itemContainer)
        }
    }

    fun setSelectedCategory(index: Int) {
        if (index == lastSelectedIndex || index !in 0 until childCount) return
        lastSelectedIndex = index

        children.forEachIndexed { i, view ->
            view.isSelected = (i == index)
//            val container = view as FrameLayout
//            val icon = container.getChildAt(0)
//            icon.isSelected = (i == index)
        }
    }


    // Sliding and touching logic
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isEmpty()) return false

        val widthPerItem = width.toFloat() / childCount
        val rawIndex = (event.x / widthPerItem).toInt()
        val index = rawIndex.coerceIn(0, childCount - 1)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialDownX = event.x
                isSliding = false

                if (index != lastSelectedIndex) {
                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    setSelectedCategory(index)
                }

                delegate.onCategorySelected(index, 0.0f)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (!isSliding && abs(event.x - initialDownX) > touchSlop) {
                    isSliding = true
                }

                val localX = event.x % widthPerItem
                val progress = (localX / widthPerItem).coerceIn(0f, 1f)

                if (index != lastSelectedIndex) {
                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    setSelectedCategory(index)
                }

                delegate.onCategorySelected(index, if (isSliding) progress else 0f)
                return true
            }
        }
        return true
    }


    // Helpers
    private fun createCategoryBackground(context: Context): StateListDrawable {
        val selectedShape = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(context.getColor(R.color.emoji_keyboard_light_gray))
        }

        return StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_selected), selectedShape)
            addState(intArrayOf(), Color.TRANSPARENT.toDrawable())
        }
    }

    private fun createCategoryIconTint(context: Context): ColorStateList {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_selected),
            intArrayOf()
        )
        val colors = intArrayOf(
            context.getColor(R.color.emoji_keyboard_black),
            context.getColor(R.color.emoji_keyboard_gray)
        )
        return ColorStateList(states, colors)
    }

}