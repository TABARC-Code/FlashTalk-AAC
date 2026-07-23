package com.flashtalk.aac.ui

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.flashtalk.aac.R

class ImportSetActivity : BaseActivity() {

    private lateinit var viewModel: ImportViewModel
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView

    private val pickZip = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.importZip(it) }
    }
    private val pickJson = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.importJson(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import_set)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val profileId = intent.getLongExtra(MainActivity.EXTRA_PROFILE_ID, 0L)
        viewModel = ViewModelProvider(this, ImportViewModelFactory(application, profileId))[ImportViewModel::class.java]
        progressBar = findViewById(R.id.importProgress)
        statusText = findViewById(R.id.importStatus)

        findViewById<View>(R.id.buttonImportZip).setOnClickListener { pickZip.launch("application/zip") }
        findViewById<View>(R.id.buttonImportJson).setOnClickListener { pickJson.launch("application/json") }

        viewModel.state.observe(this) { state -> render(state) }
    }

    private fun render(state: ImportUiState) {
        when (state) {
            is ImportUiState.Idle -> {
                progressBar.visibility = View.GONE
                statusText.text = ""
            }
            is ImportUiState.Importing -> {
                progressBar.visibility = View.VISIBLE
                statusText.text = getString(R.string.importing)
            }
            is ImportUiState.Done -> {
                progressBar.visibility = View.GONE
                val base = getString(R.string.import_success, state.cardCount, state.categoryName)
                statusText.text = if (state.warnings.isEmpty()) {
                    base
                } else {
                    base + "\n\n" + state.warnings.joinToString("\n")
                }
            }
            is ImportUiState.Failed -> {
                progressBar.visibility = View.GONE
                statusText.text = state.message
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
