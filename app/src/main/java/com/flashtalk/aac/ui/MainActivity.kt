package com.flashtalk.aac.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flashtalk.aac.R
import com.flashtalk.aac.data.Category
import com.flashtalk.aac.utils.TTSManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var categoryAdapter: CategoryAdapter
    private val prefs by lazy { getSharedPreferences(TTSManager.PREFS_NAME, Context.MODE_PRIVATE) }

    // Read/refreshed every onResume (profile switching happens in a
    // separate activity that writes the pref and finishes — same pattern
    // as Edit mode/strip mode). Used when creating a new category so it's
    // scoped to whichever profile is actually active right now.
    private var currentProfileId: Long = 0L

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

    override fun onResume() {
        super.onResume()
        // Edit mode is toggled in SettingsActivity — pick up any change made
        // while we were away, without needing to rebuild the adapter.
        applyEditModeState()
        refreshActiveProfile()
    }

    // Profile switching happens in ProfileActivity, which just writes the
    // pref and finishes — this is where MainActivity actually picks the
    // change up, same pattern as applyEditModeState(). Also covers first
    // launch, when no profile id has been stored yet.
    private fun refreshActiveProfile() {
        lifecycleScope.launch {
            val profiles = viewModel.getAllProfilesSync()
            if (profiles.isEmpty()) return@launch
            val storedId = prefs.getLong(TTSManager.KEY_CURRENT_PROFILE_ID, -1L)
            val activeProfile = profiles.firstOrNull { it.id == storedId } ?: profiles.first()
            if (activeProfile.id != storedId) {
                prefs.edit().putLong(TTSManager.KEY_CURRENT_PROFILE_ID, activeProfile.id).apply()
            }
            currentProfileId = activeProfile.id
            supportActionBar?.subtitle = activeProfile.name
            viewModel.setCurrentProfile(activeProfile.id)
        }
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.categoriesRecyclerView)
        categoryAdapter = CategoryAdapter(onCategoryClick = { category -> openCategory(category) })
        recyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = categoryAdapter
            setHasFixedSize(true)
        }
    }

    private fun applyEditModeState() {
        val editModeOn = prefs.getBoolean(TTSManager.KEY_EDIT_MODE, false)
        findViewById<View>(R.id.editModeBanner).visibility = if (editModeOn) View.VISIBLE else View.GONE
        categoryAdapter.onCategoryLongClick = if (editModeOn) { category -> showEditCategoryDialog(category) } else null
        categoryAdapter.notifyDataSetChanged()
    }

    private fun setupFab() {
        findViewById<FloatingActionButton>(R.id.fabAddCategory).setOnClickListener {
            showAddCategoryDialog()
        }
    }

    private fun observeData() {
        viewModel.categories.observe(this) { categories ->
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
                    viewModel.addCategory(name, icon, color, currentProfileId)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showEditCategoryDialog(category: Category) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null)
        val nameInput = view.findViewById<EditText>(R.id.inputCategoryName).apply { setText(category.name) }
        val iconInput = view.findViewById<EditText>(R.id.inputCategoryIcon).apply { setText(category.icon) }

        AlertDialog.Builder(this)
            .setTitle(R.string.edit_category_title)
            .setView(view)
            .setPositiveButton(R.string.save) { _, _ ->
                val name = nameInput.text.toString().trim()
                if (name.isNotEmpty()) {
                    val icon = iconInput.text.toString().trim().ifEmpty { category.icon }
                    viewModel.updateCategory(category.copy(name = name, icon = icon))
                }
            }
            .setNeutralButton(R.string.delete) { _, _ -> showDeleteCategoryConfirm(category) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showDeleteCategoryConfirm(category: Category) {
        lifecycleScope.launch {
            val cardCount = viewModel.cardCountFor(category.id)
            AlertDialog.Builder(this@MainActivity)
                .setTitle(R.string.delete_category_title)
                .setMessage(getString(R.string.delete_category_message, category.name, cardCount))
                .setPositiveButton(R.string.delete) { _, _ -> viewModel.deleteCategory(category) }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
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
                MathGate.show(this) {
                    startActivity(
                        Intent(this, ImportSetActivity::class.java)
                            .putExtra(EXTRA_PROFILE_ID, currentProfileId)
                    )
                }
                true
            }
            R.id.action_profiles -> {
                MathGate.show(this) { startActivity(Intent(this, ProfileActivity::class.java)) }
                true
            }
            R.id.action_settings -> {
                MathGate.show(this) { startActivity(Intent(this, SettingsActivity::class.java)) }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val EXTRA_CATEGORY_ID = "category_id"
        const val EXTRA_CATEGORY_NAME = "category_name"
        const val EXTRA_CATEGORY_COLOR = "category_color"
        const val EXTRA_PROFILE_ID = "profile_id"
    }
}
