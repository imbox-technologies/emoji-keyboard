package com.davidperi.emojikeyboard.ui.state

import android.util.Log
import com.davidperi.emojikeyboard.EmojiPopupV4
import com.davidperi.emojikeyboard.utils.DisplayUtils.dp

class PopupStateMachineV4(
    private val popup: EmojiPopupV4
) {

    private var _state: PopupState = PopupState.COLLAPSED
    val state: PopupState get() = _state

    private var expectedImeVisibility: Boolean? = null
    private var currentImeVisibility: Boolean? = null


    // Public events (user actions)
    fun toggle() {
        Log.i("EMOJI", "toggle() with state=$_state")
        when (_state) {
            PopupState.COLLAPSED -> transitionTo(PopupState.FOCUSED)
            PopupState.BEHIND -> transitionTo(PopupState.FOCUSED)
            PopupState.FOCUSED -> transitionTo(PopupState.BEHIND)
            PopupState.SEARCHING -> transitionTo(PopupState.BEHIND)
        }
    }

    fun hide() {
        Log.i("EMOJI", "hide() with state=$_state")
        if (state == PopupState.FOCUSED) {
            transitionTo(PopupState.COLLAPSED)
        }
    }

    fun search() {
        Log.i("EMOJI", "search() with state=$_state")
        if (_state == PopupState.FOCUSED) {
            transitionTo(PopupState.SEARCHING)
        }
    }


    // Internal events (reaction to system)
    fun onImeVisibilityChanged(isVisible: Boolean) {
        if (expectedImeVisibility != null && expectedImeVisibility == isVisible) {
            Log.d("EMOJI", "ignoring expected ime event (isVisible=$isVisible)")
            expectedImeVisibility = null
            currentImeVisibility = isVisible
            return
        }

        if (currentImeVisibility == isVisible) {
            Log.d("EMOJI", "ignoring redundant ime event (isVisible=$isVisible, no change)")
            expectedImeVisibility = null
            return
        }

        expectedImeVisibility = null
        currentImeVisibility = isVisible

        if (isVisible) {
            Log.i("EMOJI", "event ime up (unexpected) with state=$_state")
            if (_state == PopupState.COLLAPSED) {
                transitionTo(PopupState.BEHIND, isAutomatic = true)
            }
        } else {
            Log.i("EMOJI", "event ime down (unexpected) with state=$_state")
            if (_state == PopupState.BEHIND) {
                transitionTo(PopupState.COLLAPSED, isAutomatic = true)
            } else if (_state == PopupState.SEARCHING) {
                transitionTo(PopupState.FOCUSED, isAutomatic = true)
            }
        }
    }


    private fun transitionTo(newState: PopupState, isAutomatic: Boolean = false) {
        Log.v("EMOJI", "transitioning $_state -> $newState (isAutomatic=$isAutomatic)")
        if (_state == newState) return

        when (newState) {
            PopupState.COLLAPSED -> {
                expectedImeVisibility = null
            }
            PopupState.BEHIND -> {
                if (!isAutomatic) {
                    expectedImeVisibility = true
                } else {
                    expectedImeVisibility = null
                }
            }
            PopupState.FOCUSED -> {
                expectedImeVisibility = false
            }
            PopupState.SEARCHING -> {
                expectedImeVisibility = true
            }
        }

        changeState(newState)

        when (newState) {
            PopupState.COLLAPSED -> {
                popup.updatePopupHeight(0)
            }
            PopupState.BEHIND -> {
                popup.updatePopupHeight(0)
                if (!isAutomatic) {
                    popup.showKeyboard()
                } else {
                    currentImeVisibility = true
                }
            }
            PopupState.FOCUSED -> {
                popup.updatePopupHeight(300.dp)
                popup.hideKeyboard()
                currentImeVisibility = false
            }
            PopupState.SEARCHING -> {
                popup.updatePopupHeight(60.dp)
                popup.showKeyboard()
            }
        }
    }

    private fun changeState(newState: PopupState) {
        if (_state == newState) return
        _state = newState
        popup.popupStateChanged(_state)
    }

}