package com.flashtalk.aac.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flashtalk.aac.R
import com.flashtalk.aac.data.Category
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : BaseActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var categoryAdapter: CategoryAdapter

    // A fixed, high-contrast rotating palette for custom categories —
    // simpler and more foolproof than a colour-picker UI for v1.0.
    private val customCategoryPalette = listOf(
        "#FF6B6B", "#4ECDC4", "#95E1D3", "#FFD93D",
        "#A8E6CF", "#FF8B94", "#B4A7D6", "#F8B195"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = getString(R.string.app_name)

        viewModel = ViewModelProvider(this, MainViewModelFactory(application))[MainViewModel::class.java]

        setupRecyclerView()
        setupFab()
        observeData()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.categoriesRecyclerView)
        categoryAdapter = CategoryAdapter { category -> openCategory(category) }
        recyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = categoryAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupFab() {
        findViewById<FloatingActionButton>(R.id.fabAddCategory).setOnClickListener {
            showAddCategoryDialog()
        }
    }

    private fun observeData() {
        viewModel.allCategories.observe(this) { categories ->
            categoryAdapter.submitList(categories)
        }
    }

    private fun showAddCategoryDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null)
        val nameInput = view.findViewById<EditText>(R.id.inputCategoryName)
        val iconInput = view.findViewById<EditText>(R.id.inputCategoryIcon)

        AlertDialog.Builder(this)
            .setTitle(R.string.new_category)
            .setView(view)
            .setPositiveButton(R.string.save) { _, _ ->
                val name = nameInput.text.toString().trim()
                if (name.isNotEmpty()) {
                    val icon = iconInput.text.toString().trim().ifEmpty { "📦" }
                    val color = customCategoryPalette[(categoryAdapter.itemCount) % customCategoryPalette.size]
                    viewModel.addCategory(name, icon, color)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun openCategory(category: Category) {
        val intent = Intent(this, CategoryActivity::class.java).apply {
            putExtra(EXTRA_CATEGORY_ID, category.id)
            putExtra(EXTRA_CATEGORY_NAME, category.name)
            putExtra(EXTRA_CATEGORY_COLOR, category.color)
        }
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_import -> {
                startActivity(Intent(this, ImportSetActivity::class.java))
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val EXTRA_CATEGORY_ID = "category_id"
        const val EXTRA_CATEGORY_NAME = "category_name"
        const val EXTRA_CATEGORY_COLOR = "category_color"
    }
}
