package com.kej.wordbook.presenter.add

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.widget.addTextChangedListener
import com.google.android.material.chip.Chip
import com.kej.wordbook.LibContents.EDIT_WORLD
import com.kej.wordbook.LibContents.IS_UPDATE
import com.kej.wordbook.LibContents.WORLD
import com.kej.wordbook.R
import com.kej.wordbook.data.model.Word
import com.kej.wordbook.databinding.ActivityAddBinding

class AddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBinding
    private var editWord: Word? = null
    private var isEdit = false
    private val viewModel by viewModels<AddViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initChipGroup()
        getIntentData()
        initViews()
    }

    private fun initChipGroup() {
        val types = resources.getStringArray(R.array.chip_group)
        binding.typeChipGroup.apply {
            types.forEach { text ->
                addView(createChip(text))
            }
        }
    }

    private fun initViews() {
        binding.addButton.apply {
            text = if (!isEdit) {
                getString(R.string.add)
            } else {
                getString(R.string.edit)
            }
            setOnClickListener {
                addData()
            }
        }

        binding.textInputEditText.addTextChangedListener {
            it?.let { text ->
                binding.textInputEditText.error = when(text.length) {
                    0 -> getString(R.string.please_input_word)
                    1 -> "2자 이상을 입력해주세요"
                    else -> null
                }
            }
        }
    }

    private fun getIntentData() {
        val word = intent.getParcelableExtra<Word>(WORLD)
        word?.let { inWord ->
            editWord = inWord
            isEdit = true
            binding.textInputEditText.setText(inWord.text)
            binding.meanTextInputEditText.setText(inWord.mean)
            val selectChip = binding.typeChipGroup.children.firstOrNull { (it as Chip).text == inWord.type } as? Chip
            selectChip?.isChecked = true
        }
    }


    private fun addData() {
        val text = binding.textInputEditText.text.toString()
        val mean = binding.meanTextInputEditText.text.toString()
        val checkId = binding.typeChipGroup.checkedChipId
        if (!checkValue(text, mean, checkId)) {
            return
        }
        val type = findViewById<Chip>(checkId).text.toString()

        if (!isEdit) {
            val word = Word(text, mean, type)
            viewModel.insertData(word)
            val intent = Intent().putExtra(IS_UPDATE, true)
            setResult(RESULT_OK, intent)
            finish()
        } else {
            editWord?.id?.let { Word(text, mean, type, it) }?.let { word ->
                viewModel.updateData(word)
                val intent = Intent().putExtra(EDIT_WORLD, word)
                setResult(RESULT_OK, intent)
                finish()
            } ?: Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkValue(text: String, mean: String, checkId: Int): Boolean {
        when {
            text.isEmpty() -> {
                Toast.makeText(this, getString(R.string.please_input_word), Toast.LENGTH_SHORT).show()
                return false
            }
            mean.isEmpty() -> {
                Toast.makeText(this, getString(R.string.please_input_mean), Toast.LENGTH_SHORT).show()
                return false
            }
            checkId == -1 -> {
                Toast.makeText(this, getString(R.string.please_check_type), Toast.LENGTH_SHORT).show()
                return false
            }
            else -> {
                return true
            }
        }
    }

    private fun createChip(text: String): Chip {
        return Chip(this).apply {
            setText(text)
            isCheckable = true
            isClickable = true
        }
    }
}