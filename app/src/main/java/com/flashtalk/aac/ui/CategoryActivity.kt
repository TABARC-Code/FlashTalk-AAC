package com.flashtalk.aac.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flashtalk.aac.R
import com.flashtalk.aac.data.FlashCard
import com.flashtalk.aac.utils.ImageSetExporter
import com.flashtalk.aac.utils.TTSManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class CategoryActivity : BaseActivity() {

    private lateinit var viewModel: CategoryViewModel
    private lateinit var ttsManager: TTSManager
    private lateinit var flashCardAdapter: FlashCardAdapter
    private var categoryId: Long = -1
    private val prefs by lazy { getSharedPreferences(TTSManager.PREFS_NAME, Context.MODE_PRIVATE) }

    // Set just before launching the picker from the edit-card dialog, read
    // back in the ActivityResult callback — GetContent() is a separate
    // activity, so this is how the callback knows which card it's for.
    private var pendingImagePickTarget: ((String) -> Unit)? = null

    private val pickImageForEdit = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val onPicked = pendingImagePickTarget
        pendingImagePickTarget = null
        if (uri != null && onPicked != null) copyPickedImage(uri, onPicked)
    }

    private val createExportFile = registerForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) runExport(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        categoryId = intent.getLongExtra(MainActivity.EXTRA_CATEGORY_ID, -1)
        title = intent.getStringExtra(MainActivity.EXTRA_CATEGORY_NAME)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ttsManager = TTSManager(this)
        ttsManager.initialize()

        viewModel = ViewModelProvider(
            this,
            CategoryViewModelFactory(application, categoryId)
        )[CategoryViewModel::class.java]

        setupRecyclerView()
        setupFab()
        observeData()
    }

    override fun onResume() {
        super.onResume()
        applyEditModeState()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.flashCardsRecyclerView)
        flashCardAdapter = FlashCardAdapter(onCardClick = { card -> speakCard(card) })
        recyclerView.apply {
            layoutManager = GridLayoutManager(this@CategoryActivity, 3)
            adapter = flashCardAdapter
            setHasFixedSize(true)
        }
    }

    private fun applyEditModeState() {
        val editModeOn = prefs.getBoolean(TTSManager.KEY_EDIT_MODE, false)
        findViewById<View>(R.id.editModeBanner).visibility = if (editModeOn) View.VISIBLE else View.GONE
        flashCardAdapter.onCardLongClick = if (editModeOn) { card -> showEditCardDialog(card) } else null
        flashCardAdapter.notifyDataSetChanged()
    }

    private fun setupFab() {
        findViewById<FloatingActionButton>(R.id.fabAddCard).setOnClickListener {
            startActivity(
                Intent(this, CustomCardActivity::class.java)
                    .putExtra(EXTRA_CATEGORY_ID, categoryId)
            )
        }
    }

    private fun observeData() {
        viewModel.flashCards.observe(this) { cards ->
            flashCardAdapter.submitList(cards)
        }
    }

    private fun speakCard(flashCard: FlashCard) {
        // Tap-to-speak latency is the product (CLAUDE.md invariant 1): speak
        // first, the speech bar update is fire-and-forget visual
        // confirmation only. Speaks speechText, not the on-screen label —
        // they differ for cards like "Bathroom / Toilet" (label) which
        // should just say "Bathroom".
        ttsManager.speak(flashCard.speechText)
        showInSpeechBar(flashCard.text, flashCard.speechText)
    }

    private fun showInSpeechBar(label: String, speechText: String) {
        findViewById<TextView>(R.id.speechBarText).text = label
        findViewById<View>(R.id.buttonRepeat).apply {
            visibility = View.VISIBLE
            setOnClickListener { ttsManager.speak(speechText) }
        }
    }

    private fun showEditCardDialog(flashCard: FlashCard) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_card, null)
        val textInput = view.findViewById<EditText>(R.id.inputCardText).apply { setText(flashCard.text) }
        var pendingImagePath: String? = null

        view.findViewById<Button>(R.id.buttonChangeImage).setOnClickListener {
            pendingImagePickTarget = { path -> pendingImagePath = path }
            pickImageForEdit.launch("image/*")
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.edit_card_title)
            .setView(view)
            .setPositiveButton(R.string.save) { _, _ ->
                val text = textInput.text.toString().trim()
                if (text.isNotEmpty()) {
                    val newImagePath = pendingImagePath
                    val updated = if (newImagePath != null) {
                        flashCard.copy(text = text, speechText = text, imagePath = newImagePath, isCustom = true)
                    } else {
                        flashCard.copy(text = text, speechText = text)
                    }
                    viewModel.updateCard(updated)
                }
            }
            .setNeutralButton(R.string.delete) { _, _ -> showDeleteCardConfirm(flashCard) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showDeleteCardConfirm(flashCard: FlashCard) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_card_title)
            .setMessage(getString(R.string.delete_card_message, flashCard.text))
            .setPositiveButton(R.string.delete) { _, _ -> viewModel.deleteCard(flashCard) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun copyPickedImage(uri: Uri, onSaved: (String) -> Unit) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val destDir = File(filesDir, "custom_images").apply { mkdirs() }
                val destFile = File(destDir, "${UUID.randomUUID()}.jpg")
                contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output -> input.copyTo(output) }
                }
                withContext(Dispatchers.Main) {
                    onSaved(destFile.absolutePath)
                    Toast.makeText(this@CategoryActivity, R.string.select_image, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CategoryActivity, R.string.image_select_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showEditThisCategoryDialog() {
        lifecycleScope.launch {
            val category = viewModel.getCategory() ?: return@launch
            val view = LayoutInflater.from(this@CategoryActivity).inflate(R.layout.dialog_add_category, null)
            val nameInput = view.findViewById<EditText>(R.id.inputCategoryName).apply { setText(category.name) }
            val iconInput = view.findViewById<EditText>(R.id.inputCategoryIcon).apply { setText(category.icon) }

            AlertDialog.Builder(this@CategoryActivity)
                .setTitle(R.string.edit_category_title)
                .setView(view)
                .setPositiveButton(R.string.save) { _, _ ->
                    val name = nameInput.text.toString().trim()
                    if (name.isNotEmpty()) {
                        val icon = iconInput.text.toString().trim().ifEmpty { category.icon }
                        viewModel.updateCategory(category.copy(name = name, icon = icon))
                        title = name
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.category_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_edit_category -> {
                if (prefs.getBoolean(TTSManager.KEY_EDIT_MODE, false)) {
                    showEditThisCategoryDialog()
                } else {
                    Toast.makeText(this, R.string.edit_mode_off_hint, Toast.LENGTH_SHORT).show()
                }
                true
            }
            R.id.action_export_category -> {
                exportThisCategory()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun exportThisCategory() {
        lifecycleScope.launch {
            val category = viewModel.getCategory() ?: return@launch
            val safeName = category.name.replace(Regex("[^A-Za-z0-9 _-]"), "").ifBlank { "category" }
            createExportFile.launch("$safeName.zip")
        }
    }

    private fun runExport(destUri: Uri) {
        lifecycleScope.launch {
            val category = viewModel.getCategory()
            if (category == null) {
                Toast.makeText(this@CategoryActivity, getString(R.string.export_failed, "category not found"), Toast.LENGTH_SHORT).show()
                return@launch
            }
            val cards = flashCardAdapter.currentList
            val result = withContext(Dispatchers.IO) {
                ImageSetExporter(this@CategoryActivity).exportCategory(category, cards, destUri)
            }
            when (result) {
                is ImageSetExporter.ExportResult.Success -> {
                    Toast.makeText(
                        this@CategoryActivity,
                        getString(R.string.export_success, category.name, result.cardCount),
                        Toast.LENGTH_LONG
                    ).show()
                }
                is ImageSetExporter.ExportResult.Error -> {
                    Toast.makeText(
                        this@CategoryActivity,
                        getString(R.string.export_failed, result.message),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
    }

    companion object {
        const val EXTRA_CATEGORY_ID = "category_id"
    }
}
