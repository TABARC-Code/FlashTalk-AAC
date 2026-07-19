package com.flashtalk.aac.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flashtalk.aac.R
import com.flashtalk.aac.data.FlashCard
import com.flashtalk.aac.utils.TTSManager
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CategoryActivity : BaseActivity() {

    private lateinit var viewModel: CategoryViewModel
    private lateinit var ttsManager: TTSManager
    private lateinit var flashCardAdapter: FlashCardAdapter
    private var categoryId: Long = -1

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

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.flashCardsRecyclerView)
        flashCardAdapter = FlashCardAdapter { card -> speakCard(card) }
        recyclerView.apply {
            layoutManager = GridLayoutManager(this@CategoryActivity, 3)
            adapter = flashCardAdapter
            setHasFixedSize(true)
        }
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
        // first, Toast is fire-and-forget visual confirmation only.
        ttsManager.speak(flashCard.text)
        Toast.makeText(this, flashCard.text, Toast.LENGTH_SHORT).show()
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
                // Edit mode is BACKLOG.md P1 item 4 — deliberately not built
                // yet, since accidental entry mid-communication must be
                // designed against first, not bolted on here.
                Toast.makeText(this, R.string.edit_not_yet_available, Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
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
