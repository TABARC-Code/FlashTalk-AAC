package com.flashtalk.aac.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flashtalk.aac.R
import com.flashtalk.aac.data.Profile
import com.flashtalk.aac.utils.TTSManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class ProfileActivity : BaseActivity() {

    private lateinit var viewModel: ProfileViewModel
    private lateinit var profileAdapter: ProfileAdapter
    private val prefs by lazy { getSharedPreferences(TTSManager.PREFS_NAME, Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProvider(this, ProfileViewModelFactory(application))[ProfileViewModel::class.java]

        setupRecyclerView()
        setupFab()
        observeData()
    }

    override fun onResume() {
        super.onResume()
        applyEditModeState()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.profilesRecyclerView)
        profileAdapter = ProfileAdapter(onProfileClick = { profile -> switchToProfile(profile) })
        profileAdapter.currentProfileId = prefs.getLong(TTSManager.KEY_CURRENT_PROFILE_ID, -1L)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ProfileActivity)
            adapter = profileAdapter
            setHasFixedSize(true)
        }
    }

    private fun applyEditModeState() {
        val editModeOn = prefs.getBoolean(TTSManager.KEY_EDIT_MODE, false)
        findViewById<View>(R.id.editModeBanner).visibility = if (editModeOn) View.VISIBLE else View.GONE
        profileAdapter.onProfileLongClick = if (editModeOn) { profile -> showEditProfileDialog(profile) } else null
        profileAdapter.notifyDataSetChanged()
    }

    private fun setupFab() {
        findViewById<FloatingActionButton>(R.id.fabAddProfile).setOnClickListener {
            showAddProfileDialog()
        }
    }

    private fun observeData() {
        viewModel.allProfiles.observe(this) { profiles ->
            profileAdapter.submitList(profiles)
        }
    }

    // Just writes the pref and leaves — MainActivity.onResume() picks the
    // change up itself (refreshActiveProfile), same as every other
    // Settings-style toggle in this app.
    private fun switchToProfile(profile: Profile) {
        prefs.edit().putLong(TTSManager.KEY_CURRENT_PROFILE_ID, profile.id).apply()
        profileAdapter.currentProfileId = profile.id
        finish()
    }

    private fun showAddProfileDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_profile, null)
        val nameInput = view.findViewById<EditText>(R.id.inputProfileName)
        val iconInput = view.findViewById<EditText>(R.id.inputProfileIcon)

        AlertDialog.Builder(this)
            .setTitle(R.string.new_profile)
            .setView(view)
            .setPositiveButton(R.string.save) { _, _ ->
                val name = nameInput.text.toString().trim()
                if (name.isNotEmpty()) {
                    val icon = iconInput.text.toString().trim().ifEmpty { "👤" }
                    viewModel.addProfile(name, icon)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showEditProfileDialog(profile: Profile) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_profile, null)
        val nameInput = view.findViewById<EditText>(R.id.inputProfileName).apply { setText(profile.name) }
        val iconInput = view.findViewById<EditText>(R.id.inputProfileIcon).apply { setText(profile.icon) }

        AlertDialog.Builder(this)
            .setTitle(R.string.edit_profile_title)
            .setView(view)
            .setPositiveButton(R.string.save) { _, _ ->
                val name = nameInput.text.toString().trim()
                if (name.isNotEmpty()) {
                    val icon = iconInput.text.toString().trim().ifEmpty { profile.icon }
                    viewModel.updateProfile(profile.copy(name = name, icon = icon))
                }
            }
            .setNeutralButton(R.string.delete) { _, _ -> confirmDeleteProfile(profile) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun confirmDeleteProfile(profile: Profile) {
        lifecycleScope.launch {
            if (viewModel.profileCount() <= 1) {
                AlertDialog.Builder(this@ProfileActivity)
                    .setTitle(R.string.last_profile_title)
                    .setMessage(R.string.last_profile_message)
                    .setPositiveButton(R.string.ok, null)
                    .show()
                return@launch
            }
            val ownedCategoryCount = viewModel.ownedCategoryCountFor(profile.id)
            AlertDialog.Builder(this@ProfileActivity)
                .setTitle(R.string.delete_profile_title)
                .setMessage(getString(R.string.delete_profile_message, profile.name, ownedCategoryCount))
                .setPositiveButton(R.string.delete) { _, _ ->
                    viewModel.deleteProfile(profile)
                    if (prefs.getLong(TTSManager.KEY_CURRENT_PROFILE_ID, -1L) == profile.id) {
                        prefs.edit().remove(TTSManager.KEY_CURRENT_PROFILE_ID).apply()
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
