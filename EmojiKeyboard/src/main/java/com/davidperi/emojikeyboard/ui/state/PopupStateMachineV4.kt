package com.davidperi.emojikeyboard.ui.state

import android.util.Log
import com.davidperi.emojikeyboard.EmojiPopupV4
import com.davidperi.emojikeyboard.utils.DisplayUtils.dp

class PopupStateMachineV4(
    private val popup: EmojiPopupV4
) {

    private var _state: PopupState = PopupState.COLLAPSED
    val state: PopupState get() = _state

    private var expectedIme: Boolean = false  // true=UP, false=DOWN
    private var currentIme: Boolean = false


    // Events (user actions & system reactions)
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

    fun imeUp() {
        Log.i("EMOJI", "imeUp() with state=$_state")
        if (!currentIme) {
            currentIme = true
            when (_state) {
                PopupState.COLLAPSED -> transitionTo(PopupState.BEHIND)
                PopupState.FOCUSED -> if (!expectedIme) transitionTo(PopupState.BEHIND)
                else -> { /* IME is supposed to be up in BEHIND and SEARCHING */ }
            }
        }
    }

    fun imeDown() {
        Log.i("EMOJI", "imeDown() with state=$_state")
        if (currentIme) {
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
        Log.v("EMOJI", "transitioning $_state -> $newState")
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
                popup.updatePopupHeight(965)
            }

            PopupState.SEARCHING -> {
                expectedIme = true
                popup.showKeyboard()
                popup.updatePopupHeight(1265)
            }
        }


//        when (newState) {
//            PopupState.COLLAPSED -> {
//                expectedImeVisibility = null
//            }
//            PopupState.BEHIND -> {
//                if (!isAutomatic) {
//                    expectedImeVisibility = true
//                } else {
//                    expectedImeVisibility = null
//                }
//            }
//            PopupState.FOCUSED -> {
//                expectedImeVisibility = false
//            }
//            PopupState.SEARCHING -> {
//                expectedImeVisibility = true
//            }
//        }
//
//        changeState(newState)
//
//        when (newState) {
//            PopupState.COLLAPSED -> {
//                popup.updatePopupHeight(0)
//            }
//            PopupState.BEHIND -> {
//                popup.updatePopupHeight(0)
//                if (!isAutomatic) {
//                    popup.showKeyboard()
//                } else {
//                    currentImeVisibility = true
//                }
//            }
//            PopupState.FOCUSED -> {
//                popup.updatePopupHeight(300.dp)
//                popup.hideKeyboard()
//                currentImeVisibility = false
//            }
//            PopupState.SEARCHING -> {
//                popup.updatePopupHeight(60.dp)
//                popup.showKeyboard()
//            }
//        }
    }

    private fun changeState(newState: PopupState) {
        if (_state == newState) return
        _state = newState
        popup.popupStateChanged(_state)
    }

}