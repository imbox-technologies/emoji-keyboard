package com.davidperi.emojikeyboard.ui.state

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.davidperi.emojikeyboard.EmojiPopupV3
import com.davidperi.emojikeyboard.utils.DisplayUtils.dp

class PopupStateMachineV3(
    private val popup: EmojiPopupV3,
    private val rootView: View,
) {

    private var _state: PopupState = PopupState.COLLAPSED
    val state: PopupState get() = _state

    var onStateChanged: (PopupState) -> Unit = {}

    init {
        startListeningForIme()
    }

    fun toggle() {
        when (_state) {
            PopupState.COLLAPSED -> transitionTo(PopupState.FOCUSED)
            PopupState.FOCUSED -> transitionTo(PopupState.BEHIND)
            PopupState.BEHIND -> transitionTo(PopupState.FOCUSED)
            PopupState.SEARCHING -> transitionTo(PopupState.BEHIND)
        }
    }

    fun hide() {
        if (_state == PopupState.FOCUSED || _state == PopupState.SEARCHING) {
            transitionTo(PopupState.COLLAPSED)
        }
    }

    fun onDismiss() {
        if (_state != PopupState.COLLAPSED && _state != PopupState.BEHIND) {
            changeState(PopupState.COLLAPSED)
        }
    }

    fun onSearchFocusChanged(hasFocus: Boolean) {
        if (hasFocus && _state == PopupState.FOCUSED) {
            transitionTo(PopupState.SEARCHING)
        } else if (!hasFocus && _state == PopupState.SEARCHING) {
            transitionTo(PopupState.FOCUSED)
        }
    }


    private fun transitionTo(newState: PopupState) {
        if (_state == newState) return
        val oldState = _state

        changeState(newState)

        when (newState) {
            PopupState.COLLAPSED -> {
                popup.dismissPopup()
            }
            PopupState.FOCUSED -> {
                val height = 300.dp // TODO: read usual ime height
                popup.showPopup(height)
            }
            PopupState.SEARCHING -> {
                val height = popup.getSearchContentHeight()
                popup.showPopup(height)
            }
            PopupState.BEHIND -> {
                popup.dismissPopup()
            }
        }
    }

    private fun changeState(newState: PopupState) {
        if (_state == newState) return
        _state = newState
        onStateChanged(_state)
    }

    private fun startListeningForIme() {
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
            val isImeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            if (isImeVisible) {
                onImeOpened()
            } else {
                onImeClosed()
            }
            insets
        }
    }

    private fun onImeOpened() {
        if (_state == PopupState.COLLAPSED) {
            transitionTo(PopupState.BEHIND)
        }
    }

    private fun onImeClosed() {
        if (_state == PopupState.BEHIND) {
            transitionTo(PopupState.COLLAPSED)
        }
    }

}