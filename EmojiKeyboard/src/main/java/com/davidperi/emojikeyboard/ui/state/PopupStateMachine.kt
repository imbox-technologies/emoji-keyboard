package com.davidperi.emojikeyboard.ui.state

import android.util.Log
import com.davidperi.emojikeyboard.EmojiPopup

internal class PopupStateMachine(
    private val popup: EmojiPopup
) {

    private var _state: PopupState = PopupState.COLLAPSED
    val state: PopupState get() = _state

    private var expectedIme: Boolean = false  // true=UP, false=DOWN
    private var currentIme: Boolean = false


    // Events (user actions & system reactions)
    fun toggle() {
        Log.v("EMOJI StMch", "toggle() with state=$_state")
        when (_state) {
            PopupState.COLLAPSED -> transitionTo(PopupState.FOCUSED)
            PopupState.BEHIND -> transitionTo(PopupState.FOCUSED)
            PopupState.FOCUSED -> popup.showKeyboard()
            PopupState.SEARCHING -> transitionTo(PopupState.BEHIND)
        }
    }

    fun hide() {
        Log.v("EMOJI StMch", "hide() with state=$_state")
        if (state == PopupState.FOCUSED || state == PopupState.SEARCHING) {
            transitionTo(PopupState.COLLAPSED)
        }
    }

    fun search() {
        Log.v("EMOJI StMch", "search() with state=$_state")
        if (_state == PopupState.FOCUSED) {
            expectedIme = true
        }
    }

    fun write() {
        Log.v("EMOJI StMch", "write() with state=$_state")
        if (_state == PopupState.SEARCHING) {
            transitionTo(PopupState.BEHIND)
        }
    }

    fun imeUp() {
        if (!currentIme) {
            Log.v("EMOJI StMch", "imeUp() with state=$_state")
            currentIme = true
            when (_state) {
                PopupState.COLLAPSED -> transitionTo(PopupState.BEHIND)
                PopupState.FOCUSED -> {
                    if (!expectedIme) transitionTo(PopupState.BEHIND)
                    else transitionTo(PopupState.SEARCHING)
                }
                else -> { /* IME is supposed to be up in BEHIND and SEARCHING */ }
            }
        }
    }

    fun imeDown() {
        if (currentIme) {
            Log.v("EMOJI StMch", "imeDown() with state=$_state")
            currentIme = false
            when (_state) {
                PopupState.BEHIND -> if (expectedIme) transitionTo(PopupState.COLLAPSED)
                PopupState.SEARCHING -> transitionTo(PopupState.FOCUSED)
                else -> { /* IME is supposed to be down in COLLAPSED and FOCUSED */ }
            }
        }
    }


    // State controller
    private fun transitionTo(newState: PopupState) {
        Log.e("EMOJI StMch", "transitioning $_state -> $newState")
        val oldState = _state
        if (oldState == newState) return
        changeState(newState)

        when (newState) {
            PopupState.COLLAPSED -> {
                val animate = oldState in listOf(PopupState.FOCUSED, PopupState.SEARCHING)
                expectedIme = false
                popup.hideKeyboard()
                popup.updatePopupHeight(0, animate)
            }

            PopupState.BEHIND -> {
                val animate = oldState in listOf(PopupState.SEARCHING)
                expectedIme = true
                popup.showKeyboard()
                if (oldState == PopupState.FOCUSED) {
                    popup.updatePopupHeight(popup.getKeyboardStandardHeight(), animate)
                } else {
                    popup.updatePopupHeight(0, animate)
                }
            }

            PopupState.FOCUSED -> {
                val animate = oldState in listOf(PopupState.COLLAPSED, PopupState.SEARCHING)
                expectedIme = false
                popup.hideKeyboard()
                popup.updatePopupHeight(popup.getKeyboardStandardHeight(), animate)
            }

            PopupState.SEARCHING -> {
                val animate = oldState in listOf(PopupState.FOCUSED)
                expectedIme = true
                popup.showKeyboard()
                popup.updatePopupHeight(popup.getKeyboardStandardHeight() + popup.getSearchContentHeight(), animate)
            }
        }
    }

    private fun changeState(newState: PopupState) {
        if (_state == newState) return
        _state = newState
        popup.popupStateChanged(_state)
    }

}