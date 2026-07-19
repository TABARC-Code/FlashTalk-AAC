package com.flashtalk.aac.ui

import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.flashtalk.aac.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class CustomCardActivity : BaseActivity() {

    private lateinit var viewModel: CustomCardViewModel
    private lateinit var imagePreview: ImageView
    private lateinit var textInput: EditText
    private var categoryId: Long = -1
    private var savedImagePath: String? = null

    // The system photo/document picker never requires READ_EXTERNAL_STORAGE
    // or READ_MEDIA_IMAGES — the chooser runs in the gallery app's own
    // process and hands back a content:// Uri we're granted for one read.
    // That's why this app declares no storage permission in the manifest.
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) handlePickedImage(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_card)

        categoryId = intent.getLongExtra(CategoryActivity.EXTRA_CATEGORY_ID, -1)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProvider(this, CustomCardViewModelFactory(application))[CustomCardViewModel::class.java]

        imagePreview = findViewById(R.id.imagePreview)
        textInput = findViewById(R.id.inputCardText)

        findViewById<android.widget.Button>(R.id.buttonSelectImage).setOnClickListener {
            pickImage.launch("image/*")
        }

        findViewById<FloatingActionButton>(R.id.fabSaveCard).setOnClickListener { saveCard() }
    }

    private fun handlePickedImage(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            val destDir = File(filesDir, "custom_images").apply { mkdirs() }
            val destFile = File(destDir, "${UUID.randomUUID()}.jpg")
            try {
                contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output -> input.copyTo(output) }
                }
                savedImagePath = destFile.absolutePath
                runOnUiThread {
                    Glide.with(this@CustomCardActivity)
                        .load(destFile)
                        .centerCrop()
                        .into(imagePreview)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@CustomCardActivity,
                        getString(R.string.image_select_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun saveCard() {
        val text = textInput.text.toString().trim()
        val imagePath = savedImagePath

        if (text.isEmpty()) {
            textInput.error = getString(R.string.card_text_required)
            return
        }
        if (imagePath == null) {
            Toast.makeText(this, R.string.select_image_first, Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.addCard(categoryId, text, imagePath)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
