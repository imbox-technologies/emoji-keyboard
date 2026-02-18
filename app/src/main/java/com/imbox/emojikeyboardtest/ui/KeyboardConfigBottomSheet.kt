package com.imbox.emojikeyboardtest.ui

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.imbox.emojikeyboardtest.R
import com.imbox.emojikeyboardtest.databinding.BottomSheetKeyboardConfigBinding

enum class KeyboardConfigSection {
    FONT,
    LAYOUT,
    THEME
}

data class ConfigOptionItem(
    val id: String,
    val label: String
)

interface KeyboardConfigDataProvider {
    fun getFontOptions(): List<ConfigOptionItem>
    fun getLayoutOptions(): List<ConfigOptionItem>
    fun getThemeOptions(): List<ConfigOptionItem>
    fun getSelectedFontId(): String
    fun getSelectedLayoutId(): String
    fun getSelectedThemeId(): String
}

class KeyboardConfigBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetKeyboardConfigBinding? = null
    private val binding get() = _binding!!

    var fontOptions: List<ConfigOptionItem> = emptyList()
    var layoutOptions: List<ConfigOptionItem> = emptyList()
    var themeOptions: List<ConfigOptionItem> = emptyList()

    var selectedFontId: String = ""
    var selectedLayoutId: String = ""
    var selectedThemeId: String = ""

    var onSelectionChanged: ((KeyboardConfigSection, String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetKeyboardConfigBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? KeyboardConfigDataProvider)?.let { provider ->
            fontOptions = provider.getFontOptions()
            layoutOptions = provider.getLayoutOptions()
            themeOptions = provider.getThemeOptions()
            selectedFontId = provider.getSelectedFontId()
            selectedLayoutId = provider.getSelectedLayoutId()
            selectedThemeId = provider.getSelectedThemeId()
        }
        bindOptions()
    }

    private fun bindOptions() {
        bindChipGroup(
            options = fontOptions,
            selectedId = selectedFontId,
            section = KeyboardConfigSection.FONT
        )
        bindChipGroup(
            options = layoutOptions,
            selectedId = selectedLayoutId,
            section = KeyboardConfigSection.LAYOUT
        )
        bindChipGroup(
            options = themeOptions,
            selectedId = selectedThemeId,
            section = KeyboardConfigSection.THEME
        )
    }

    private fun bindChipGroup(
        options: List<ConfigOptionItem>,
        selectedId: String,
        section: KeyboardConfigSection
    ) {
        val chipGroup = when (section) {
            KeyboardConfigSection.FONT -> binding.chipGroupFont
            KeyboardConfigSection.LAYOUT -> binding.chipGroupLayout
            KeyboardConfigSection.THEME -> binding.chipGroupTheme
        }

        chipGroup.removeAllViews()

        val bgTint = ContextCompat.getColorStateList(requireContext(), R.color.chip_config_background)
        val strokeColor = ContextCompat.getColorStateList(requireContext(), R.color.chip_config_stroke)
        val textColor = ContextCompat.getColorStateList(requireContext(), R.color.chip_config_text)
        val strokeWidthPx = resources.getDimension(R.dimen.chip_config_stroke_width)
        options.forEach { option ->
            val chip = Chip(requireContext()).apply {
                tag = option.id
                text = option.label
                isCheckable = true
                isChecked = option.id == selectedId
                chipBackgroundColor = bgTint
                chipStrokeColor = strokeColor
                chipStrokeWidth = strokeWidthPx
                setTextColor(textColor)
                checkedIcon = null
                setOnClickListener {
                    when (section) {
                        KeyboardConfigSection.FONT -> selectedFontId = option.id
                        KeyboardConfigSection.LAYOUT -> selectedLayoutId = option.id
                        KeyboardConfigSection.THEME -> selectedThemeId = option.id
                    }
                    for (i in 0 until chipGroup.childCount) {
                        (chipGroup.getChildAt(i) as? Chip)?.isChecked = (chipGroup.getChildAt(i).tag == option.id)
                    }
                    onSelectionChanged?.invoke(section, option.id)
                }
            }
            chipGroup.addView(chip)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
