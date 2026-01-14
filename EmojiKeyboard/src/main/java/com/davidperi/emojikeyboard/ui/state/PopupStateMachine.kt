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
            PopupState.FOCUSED -> transitionTo(PopupState.BEHIND)
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
            transitionTo(PopupState.SEARCHING)
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
                PopupState.FOCUSED -> if (!expectedIme) transitionTo(PopupState.BEHIND)
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
        if (_state == newState) return
        changeState(newState)

        when (newState) {
            PopupState.COLLAPSED -> {
                expectedIme = false
                popup.hideKeyboard()
                popup.updatePopupHeight(0)
            }

            PopupState.BEHIND -> {
                expectedIme = true
                popup.showKeyboard()
                popup.updatePopupHeight(0)
            }

            PopupState.FOCUSED -> {
                expectedIme = false
                popup.hideKeyboard()
                popup.updatePopupHeight(popup.getKeyboardStandardHeight())
            }

            PopupState.SEARCHING -> {
                expectedIme = true
                popup.showKeyboard()
                popup.updatePopupHeight(popup.getKeyboardStandardHeight() + popup.getSearchContentHeight())
            }
        }
    }

    private fun changeState(newState: PopupState) {
        if (_state == newState) return
        _state = newState
        popup.popupStateChanged(_state)
    }

}