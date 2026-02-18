/*
 * Copyright 2026 - IMBox Technologies and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.imbox.emojikeyboardtest

import android.content.Context
import android.content.res.Configuration
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.imbox.emojikeyboard.EmojiConfig
import com.imbox.emojikeyboard.EmojiLayoutMode
import com.imbox.emojikeyboard.EmojiManager
import com.imbox.emojikeyboard.EmojiPopup
import com.imbox.emojikeyboard.EmojiThemeMode
import com.imbox.emojikeyboard.ui.state.PopupState
import com.imbox.emojikeyboardtest.adapter.ChatAdapter
import com.imbox.emojikeyboardtest.databinding.ActivityMainBinding
import com.imbox.emojikeyboardtest.model.ChatMessage
import com.imbox.emojikeyboardtest.ui.ConfigOptionItem
import com.imbox.emojikeyboardtest.ui.KeyboardConfigBottomSheet
import com.imbox.emojikeyboardtest.prefs.KeyboardConfigPrefs
import com.imbox.emojikeyboardtest.ui.KeyboardConfigDataProvider
import com.imbox.emojikeyboardtest.ui.KeyboardConfigSection

class MainActivity : AppCompatActivity(), KeyboardConfigDataProvider {

    private data class FontOption(
        val id: String,
        val label: String,
        val typefaceProvider: (Context) -> Typeface?
    )

    private lateinit var binding: ActivityMainBinding
    private lateinit var emojiPopup: EmojiPopup

    private val chatAdapter = ChatAdapter()
    private val messages = mutableListOf<ChatMessage>()
    private val availableFontOptions by lazy {
        listOf(
            FontOption(
                id = FONT_ID_DEFAULT,
                label = getString(R.string.option_font_default),
                typefaceProvider = { null }
            ),
            FontOption(
                id = FONT_ID_COOPER,
                label = getString(R.string.option_font_cooper),
                typefaceProvider = { context ->
                    Typeface.createFromAsset(context.assets, "fonts/Cooper.ttf")
                }
            )
        )
    }

    private lateinit var configPrefs: KeyboardConfigPrefs
    private var currentPopupHeight = 0
    private var selectedFontId = FONT_ID_COOPER
    private var selectedLayoutMode = EmojiLayoutMode.COOPER
    private var selectedThemeMode = EmojiThemeMode.DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            Log.d("EMOJI Activ", "insets in activity")
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val sysInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(sysInsets.left, sysInsets.top, sysInsets.right, maxOf(sysInsets.bottom, imeInsets.bottom, currentPopupHeight))
            insets
        }

        WindowInsetsControllerCompat(window, window.decorView).apply {
            val isDarkMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            isAppearanceLightStatusBars = !isDarkMode
            isAppearanceLightNavigationBars = !isDarkMode
        }

        init()
    }

    private fun init() {
        configPrefs = KeyboardConfigPrefs(this)
        initializeConfigState()
        setupRecyclerView()
        setupBackHandling()
        setupEmojiPopup()
        setupAnimations()
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

        binding.ivOpenConfig.setOnClickListener {
            showKeyboardConfigBottomSheet()
        }

        emojiPopup.setOnPopupStateChangedListener { state -> updateIcon(state) }
    }

    private fun setupEmojiPopup() {
        emojiPopup = EmojiPopup(this)
        emojiPopup.bindTo(binding.etTest)
    }

    private fun setupAnimations() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            emojiPopup.setOnPopupSizeChangeListener { size ->
                currentPopupHeight = size
            }
        }
    }

    private fun setupBackHandling() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d("EMOJI Activ", "back in activity")
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

    private fun initializeConfigState() {
        val config = EmojiManager.getConfig()
        selectedLayoutMode = config.layoutMode
        selectedThemeMode = config.themeMode
        selectedFontId = if (config.font == null || config.font == Typeface.DEFAULT) {
            FONT_ID_DEFAULT
        } else {
            FONT_ID_COOPER
        }
    }

    private fun showKeyboardConfigBottomSheet() {
        KeyboardConfigBottomSheet().apply {
            onSelectionChanged = { section, selectedId ->
                when (section) {
                    KeyboardConfigSection.FONT -> this@MainActivity.selectedFontId = selectedId
                    KeyboardConfigSection.LAYOUT -> {
                        this@MainActivity.selectedLayoutMode = runCatching { EmojiLayoutMode.valueOf(selectedId) }
                            .getOrDefault(this@MainActivity.selectedLayoutMode)
                    }
                    KeyboardConfigSection.THEME -> {
                        this@MainActivity.selectedThemeMode = runCatching { EmojiThemeMode.valueOf(selectedId) }
                            .getOrDefault(this@MainActivity.selectedThemeMode)
                    }
                }
                applyKeyboardConfig()
            }
        }.show(supportFragmentManager, KeyboardConfigBottomSheet::class.java.simpleName)
    }

    private fun applyKeyboardConfig() {
        val provider = EmojiManager.getConfig().provider
        val config = EmojiConfig(
            provider = provider,
            font = resolveSelectedTypeface(),
            layoutMode = selectedLayoutMode,
            themeMode = selectedThemeMode
        )
        EmojiManager.updateConfig(config)
        emojiPopup.recreateFromConfig()
        configPrefs.save(selectedFontId, selectedLayoutMode, selectedThemeMode)
    }

    private fun resolveSelectedTypeface(): Typeface? {
        val option = availableFontOptions.firstOrNull { it.id == selectedFontId } ?: return null
        return runCatching {
            option.typefaceProvider(this)
        }.getOrNull()
    }

    private fun Enum<*>.displayName(): String {
        return name.lowercase().replaceFirstChar { it.uppercase() }
    }

    override fun getFontOptions(): List<ConfigOptionItem> =
        availableFontOptions.map { ConfigOptionItem(it.id, it.label) }

    override fun getLayoutOptions(): List<ConfigOptionItem> =
        EmojiLayoutMode.entries.map { ConfigOptionItem(it.name, it.displayName()) }

    override fun getThemeOptions(): List<ConfigOptionItem> =
        EmojiThemeMode.entries.map { ConfigOptionItem(it.name, it.displayName()) }

    override fun getSelectedFontId(): String = selectedFontId

    override fun getSelectedLayoutId(): String = selectedLayoutMode.name

    override fun getSelectedThemeId(): String = selectedThemeMode.name

    companion object {
        private const val FONT_ID_DEFAULT = "default"
        private const val FONT_ID_COOPER = "cooper"
    }

}
