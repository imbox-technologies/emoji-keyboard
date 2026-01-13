package com.davidperi.emojikeyboardtest

import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.davidperi.emojikeyboard.EmojiPopupV6
import com.davidperi.emojikeyboard.PopupApi
import com.davidperi.emojikeyboard.ui.state.PopupState
import com.davidperi.emojikeyboardtest.databinding.ActivityMainBinding
import com.davidperi.emojikeyboardtest.model.ChatMessage
import com.davidperi.emojikeyboardtest.adapter.ChatAdapter
import kotlin.math.max

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var emojiPopup: PopupApi

    private val chatAdapter = ChatAdapter()
    private val messages = mutableListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            Log.d("EMOJI", "insets in activity")
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val sysInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(sysInsets.left, sysInsets.top, sysInsets.right, max(sysInsets.bottom, imeInsets.bottom))
            insets
        }

        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
        }

        init()
    }

    private fun init() {
        setupRecyclerView()
        setupBackHandling()
        setupEmojiPopup()
        setupListeners()
    }

    private fun setupRecyclerView() {
        binding.rvChat.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(this@MainActivity).apply {
                stackFromEnd = true
            }
        }
    }

    private fun setupListeners() {
        binding.ivToggleEmojiKeyboard.setOnClickListener {
            emojiPopup.toggle()
        }

        binding.ivSend.setOnClickListener {
            val text = binding.etTest.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
            }
        }

        emojiPopup.setOnPopupStateChangedListener { state -> updateIcon(state) }
    }

    private fun setupEmojiPopup() {
        emojiPopup = EmojiPopupV6(this)
        emojiPopup.bindTo(binding.etTest)
    }

    private fun setupBackHandling() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d("EMOJI", "Back in Activity")
                if (emojiPopup.state == PopupState.FOCUSED) {
                    emojiPopup.hide()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })
    }

    private fun updateIcon(state: PopupState) {
        binding.ivToggleEmojiKeyboard.setImageResource(
            when (state) {
                PopupState.COLLAPSED -> R.drawable.smile
                PopupState.BEHIND -> R.drawable.smile
                PopupState.FOCUSED -> R.drawable.keyboard
                PopupState.SEARCHING -> R.drawable.keyboard
            }
        )

        binding.debugStatus.text = when (state) {
            PopupState.COLLAPSED -> "COLLAPSED"
            PopupState.BEHIND -> "BEHIND"
            PopupState.FOCUSED -> "FOCUSED"
            PopupState.SEARCHING -> "SEARCHING"
        }
    }

    private fun sendMessage(text: String) {
        val newMessage = ChatMessage(
            id = System.currentTimeMillis(),
            text = text
        )

        messages.add(newMessage)

        chatAdapter.submitList(messages.toList()) {
            binding.rvChat.smoothScrollToPosition(messages.lastIndex)
        }

        binding.etTest.text?.clear()
    }

}
