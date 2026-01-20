package com.davidperi.emojikeyboard

import android.content.Context
import android.graphics.Typeface
import com.davidperi.emojikeyboard.data.EmojiIndex
import com.davidperi.emojikeyboard.data.model.Category
import com.davidperi.emojikeyboard.data.prefs.PrefsManager
import com.davidperi.emojikeyboard.logic.RecentEmojiManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object EmojiManager {

    private const val DEFAULT_FONT_PATH = "fonts/Cooper.ttf"

    @Volatile
    private var installed = false
    private lateinit var appContext: Context
    private lateinit var config: EmojiConfig

    private var categories: List<Category> = emptyList()
    private var typeface: Typeface? = null
    private var emojiIndex: EmojiIndex? = null

    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val recentEmojiManager: RecentEmojiManager by lazy {
        checkInstalled()
        RecentEmojiManager(appContext)
    }

    private val prefs: PrefsManager by lazy {
        checkInstalled()
        PrefsManager(appContext)
    }

    @JvmStatic
    @JvmOverloads
    fun install(context: Context, config: EmojiConfig = EmojiConfig()) {
        if (installed) return

        synchronized(this) {
            if (installed) return

            appContext = context.applicationContext
            this.config = config

            loadTypeface()
            preloadCategories()

            installed = true
        }
    }

    @JvmStatic
    fun isInstalled(): Boolean = installed

    @JvmStatic
    fun getConfig(): EmojiConfig {
        checkInstalled()
        return config
    }

    @JvmStatic
    fun getTypeface(): Typeface {
        checkInstalled()
        return typeface ?: synchronized(this) {
            typeface ?: Typeface.createFromAsset(appContext.assets, DEFAULT_FONT_PATH).also {
                typeface = it
            }
        }
    }

    @JvmStatic
    fun getLayoutMode(): EmojiLayoutMode {
        checkInstalled()
        return config.layoutMode
    }

    @JvmStatic
    fun getCategories(): List<Category> {
        checkInstalled()
        if (categories.isEmpty()) {
            runBlocking {
                loadCategoriesAndBuildIndex()
            }
        }
        return categories
    }

    @JvmStatic
    suspend fun getCategoriesAsync(): List<Category> {
        checkInstalled()
        if (categories.isEmpty()) {
            loadCategoriesAndBuildIndex()
        }
        return categories
    }

    @JvmStatic
    fun getEmojiIndex(): EmojiIndex {
        checkInstalled()
        return emojiIndex ?: synchronized(this) {
            emojiIndex ?: run {
                if (categories.isEmpty()) {
                    runBlocking { loadCategoriesAndBuildIndex() }
                }
                emojiIndex ?: EmojiIndex.build(categories).also { emojiIndex = it }
            }
        }
    }

    @JvmStatic
    fun getRecentManager(): RecentEmojiManager {
        checkInstalled()
        return recentEmojiManager
    }

    @JvmStatic
    fun getPrefsManager(): PrefsManager {
        checkInstalled()
        return prefs
    }

    @JvmStatic
    fun getContext(): Context {
        checkInstalled()
        return appContext
    }

    private fun loadTypeface() {
        typeface = config.font ?: try {
            Typeface.createFromAsset(appContext.assets, DEFAULT_FONT_PATH)
        } catch (e: Exception) {
            null
        }
    }

    private fun preloadCategories() {
        managerScope.launch(Dispatchers.IO) {
            loadCategoriesAndBuildIndex()
        }
    }

    private suspend fun loadCategoriesAndBuildIndex() {
        if (categories.isEmpty()) {
            categories = config.provider.getCategories(appContext)
        }
        if (emojiIndex == null && categories.isNotEmpty()) {
            emojiIndex = EmojiIndex.build(categories)
        }
    }

    private fun checkInstalled() {
        check(installed) {
            "EmojiManager is not installed. Call EmojiManager.install(context) in your Application.onCreate()"
        }
    }

    internal fun destroy() {
        installed = false
        categories = emptyList()
        typeface = null
        emojiIndex = null
    }
}
