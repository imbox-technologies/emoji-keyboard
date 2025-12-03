package com.davidperi.emojikeyboardtest

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.widget.addTextChangedListener
import com.davidperi.emojikeyboard.EmojiPopup
import com.davidperi.emojikeyboardtest.databinding.ActivityMainBinding
import com.davidperi.emojikeyboardtest.utils.MeasureUtils.dp

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var emojiPopup: EmojiPopup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Manually manage insets
//            binding.welcomeText.updatePadding(top = systemBars.top)
//
//            binding.inputContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
//                goneBottomMargin = systemBars.bottom + 8.dp
//            }

            // Set padding to the full view
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
            emojiPopup.toggle()
        }

        binding.etTest.addTextChangedListener { s ->

        }
    }

    private fun setupEmojiKeyboard() {
        emojiPopup = EmojiPopup(
            binding.root, binding.emojiKeyboard, binding.etTest
        ) { newStatus -> updateIcon(newStatus) }
    }

    private fun updateIcon(status: Int) {
        binding.ivToggleEmojiKeyboard.setImageResource(
            when (status) {
                EmojiPopup.STATE_FOCUSED -> R.drawable.keyboard
                else -> R.drawable.smile
            }
        )
    }

}