package com.davidperi.emojikeyboardtest

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.davidperi.emojikeyboard.ui.EmojiKeyboardView
import com.davidperi.emojikeyboard.ui.model.EmojiKeyboardConfig
import com.davidperi.emojikeyboard.ui.model.EmojiLayoutMode
import com.davidperi.emojikeyboardtest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
        }

        init()
    }

    private fun init() {
        setupListeners()
        setupEmojiKeyboard()
        setupBackHandling()
    }

    private fun setupListeners() {
        binding.ivToggleEmojiKeyboard.setOnClickListener {
            binding.emojiKeyboard.toggle()
        }
    }

    private fun setupEmojiKeyboard() {
        binding.emojiKeyboard.setupWith(binding.etTest)
        binding.emojiKeyboard.onStateChangedListener { state -> updateIcon(state) }

        // val iosConfig = EmojiKeyboardConfig(layoutMode = EmojiLayoutMode.COOPER)
        // binding.emojiKeyboard.configure(iosConfig)
    }

    private fun setupBackHandling() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.emojiKeyboard.state() == EmojiKeyboardView.PopupState.FOCUSED) {
                    binding.emojiKeyboard.hide()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })
    }

    private fun updateIcon(state: EmojiKeyboardView.PopupState) {
        binding.ivToggleEmojiKeyboard.setImageResource(
            when (state) {
                EmojiKeyboardView.PopupState.COLLAPSED -> R.drawable.smile
                EmojiKeyboardView.PopupState.BEHIND -> R.drawable.smile
                EmojiKeyboardView.PopupState.FOCUSED -> R.drawable.keyboard
                EmojiKeyboardView.PopupState.SEARCHING -> R.drawable.keyboard
            }
        )

        binding.debugStatus.text = when (state) {
            EmojiKeyboardView.PopupState.COLLAPSED -> "COLLAPSED"
            EmojiKeyboardView.PopupState.BEHIND -> "BEHIND"
            EmojiKeyboardView.PopupState.FOCUSED -> "FOCUSED"
            EmojiKeyboardView.PopupState.SEARCHING -> "SEARCHING"
        }
    }

}
