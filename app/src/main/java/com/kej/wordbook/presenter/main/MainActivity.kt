package com.kej.wordbook.presenter.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.kej.wordbook.LibContents.EDIT_WORLD
import com.kej.wordbook.LibContents.IS_UPDATE
import com.kej.wordbook.LibContents.WORLD
import com.kej.wordbook.R
import com.kej.wordbook.databinding.ActivityMainBinding
import com.kej.wordbook.domain.model.WordModel
import com.kej.wordbook.presenter.adapter.WordAdapter
import com.kej.wordbook.presenter.add.AddActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@SuppressLint("NotifyDataSetChanged")
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var wordAdapter: WordAdapter
    private var selectedWord: WordModel? = null
    private var currentType: String? = null
    private val currentWordList = arrayListOf<WordModel>()
    private val updateAddWordResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val isUpdate = result.data?.getBooleanExtra(IS_UPDATE, false) ?: false
        val editWord = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            result.data?.getParcelableExtra(EDIT_WORLD, WordModel::class.java)
        } else {
            result.data?.getParcelableExtra(EDIT_WORLD)
        }
        if (result.resultCode == RESULT_OK) {
            if (isUpdate) {
                viewModel.getLatestWord()
            } else if (editWord != null) {
                updateEditWord(editWord)
            }
        }
    }
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initObserve()
    }

    private fun initObserve() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mainState.collectLatest {
                    when (it) {
                        is MainState.UnInitialized -> {
                            initRecyclerView()
                            initViews()
                            initStatusBar()
                            initData()
                        }
                        is MainState.Delete -> {
                            deleteHandler()
                        }
                        is MainState.SuccessWordList -> {
                            successListHandler(it.wordList)
                        }
                        is MainState.SuccessLatestWord -> {
                            successLatestWordHandler(it.lastWord)
                        }
                        is MainState.Error -> {
                            Toast.makeText(this@MainActivity, getString(R.string.error), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun initViews() {
        binding.addButton.setOnClickListener {
            val intent = Intent(this, AddActivity::class.java)
            updateAddWordResult.launch(intent)
        }

        binding.deleteButton.setOnClickListener {
            deleteData()
        }

        binding.editButton.setOnClickListener {
            selectedWord?.let {
                val intent = Intent(this, AddActivity::class.java)
                intent.putExtra(WORLD, it)
                updateAddWordResult.launch(intent)
            }
        }
        binding.sortImageView.setOnClickListener {
            showDialog()
        }
        binding.sortTextView.setOnClickListener {
            showDialog()
        }
    }

    private fun setListSortType(type: String?) {
        wordAdapter.run {
            submitList(
                if (type != null) {
                    currentWordList.filter { it.type == type }
                } else {
                    currentWordList
                }
            )
            wordAdapter.notifyDataSetChanged()
        }
        binding.sortTextView.text = type
    }

    private fun initRecyclerView() {
        wordAdapter = WordAdapter { word ->
            selectedWord = word
            setScreenWord(word)
        }
        binding.wordRecyclerView.apply {
            adapter = wordAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            val divider = DividerItemDecoration(this@MainActivity, LinearLayoutManager.VERTICAL)
            addItemDecoration(divider)
        }
    }

    private fun initStatusBar() {
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        window?.statusBarColor = ContextCompat.getColor(this, R.color.white)
    }

    private fun initData() {
        viewModel.getAllList()
    }

    private fun deleteHandler() {
        setScreenWord(null)
        selectedWord = null
        Toast.makeText(this, getString(R.string.delete_success), Toast.LENGTH_SHORT).show()
    }

    private fun successLatestWordHandler(latestWord: WordModel) {
        setScreenWord(latestWord)
    }

    private fun successListHandler(wordList: List<WordModel>) {
        currentWordList.apply {
            clear()
            addAll(wordList)
        }
        with(wordAdapter) {
            submitList(
                if (currentType != null) {
                    currentWordList.filter { it.type == currentType }
                } else {
                    currentWordList
                }
            )
            notifyDataSetChanged()
        }
    }

    private fun deleteData() {
        selectedWord?.let { inSelectedWord ->
            viewModel.deleteData(inSelectedWord)
        }
    }

    private fun updateEditWord(word: WordModel) {
        setScreenWord(word)
    }

    private fun setScreenWord(word: WordModel?) {
        with(binding) {
            if (word == null) {
                textTextView.text = getString(R.string.noun)
                meanTextView.text = getString(R.string.value, "")
                typeTextView.text = getString(R.string.type, "")
            } else {
                textTextView.text = word.text
                meanTextView.text = getString(R.string.value, word.mean)
                typeTextView.text = getString(R.string.type, word.type)
            }
        }
    }

    private fun showDialog() {
        val typeArray = resources.getStringArray(R.array.type_group)
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.type_pick))
            .setItems(typeArray) { dialog, position ->
                val selectType = if (position == 0) {
                    null
                } else {
                    typeArray[position]
                }
                setListSortType(selectType)
                currentType = selectType
                dialog.dismiss()
            }.create()
            .show()
    }
}