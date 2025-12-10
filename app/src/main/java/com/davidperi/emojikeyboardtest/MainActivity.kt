package com.davidperi.emojikeyboardtest

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

        init()
    }

    private fun init() {
        setupListeners()
        setupEmojiKeyboard()
    }

    private fun setupListeners() {
        binding.ivToggleEmojiKeyboard.setOnClickListener {
            binding.emojiKeyboard.toggle()
        }
    }

    private fun setupEmojiKeyboard() {
        binding.emojiKeyboard.setupWith(binding.etTest)

    }

//    private fun updateIcon(state: EmojiPopup.PopupState) {
//        binding.ivToggleEmojiKeyboard.setImageResource(
//            when (state) {
//                EmojiPopup.PopupState.Collapsed -> R.drawable.smile
//                EmojiPopup.PopupState.Behind -> R.drawable.smile
//                EmojiPopup.PopupState.Focused -> R.drawable.keyboard
//                EmojiPopup.PopupState.Searching -> R.drawable.keyboard
//            }
//        )
//
//        binding.debugStatus.text = when (state) {
//            EmojiPopup.PopupState.Collapsed -> "COLLAPSED"
//            EmojiPopup.PopupState.Behind -> "BEHIND"
//            EmojiPopup.PopupState.Focused -> "FOCUSED"
//            EmojiPopup.PopupState.Searching -> "SEARCHING"
//        }
//    }

}
