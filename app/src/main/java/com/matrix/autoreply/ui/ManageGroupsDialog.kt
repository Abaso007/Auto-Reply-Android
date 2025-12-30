package com.matrix.autoreply.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.matrix.autoreply.databinding.DialogManageGroupsBinding
import com.matrix.autoreply.preferences.PreferencesManager
import com.matrix.autoreply.ui.adapters.SelectedGroupAdapter

class ManageGroupsDialog(
    context: Context,
    private val onGroupsUpdated: () -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogManageGroupsBinding
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var adapter: SelectedGroupAdapter
    private val selectedGroups = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        
        binding = DialogManageGroupsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Set dialog width to 90% of screen width
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        preferencesManager = PreferencesManager.getPreferencesInstance(context)!!
        
        // Load existing groups
        selectedGroups.clear()
        selectedGroups.addAll(preferencesManager.getSelectedGroupsForReply())
        
        setupRecyclerView()
        setupButtons()
        updateEmptyState()
    }

    private fun setupRecyclerView() {
        adapter = SelectedGroupAdapter(selectedGroups) { groupName ->
            // Delete group callback
            adapter.removeGroup(groupName)
            updateEmptyState()
        }
        
        binding.groupsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.groupsRecyclerView.adapter = adapter
    }

    private fun setupButtons() {
        // Add Group button
        binding.addGroupButton.setOnClickListener {
            val groupName = binding.groupNameInput.text?.toString()?.trim()
            
            if (groupName.isNullOrEmpty()) {
                Toast.makeText(context, "Please enter a group name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (selectedGroups.contains(groupName)) {
                Toast.makeText(context, "Group already added", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Add group
            adapter.addGroup(groupName)
            binding.groupNameInput.text?.clear()
            updateEmptyState()
            
            Toast.makeText(context, "Group added: $groupName", Toast.LENGTH_SHORT).show()
        }
        
        // Cancel button
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
        
        // Save button
        binding.saveButton.setOnClickListener {
            // Save groups to preferences
            preferencesManager.setSelectedGroupsForReply(selectedGroups.toSet())
            Toast.makeText(context, "Groups saved", Toast.LENGTH_SHORT).show()
            
            // Notify parent to update UI
            onGroupsUpdated()
            dismiss()
        }
    }

    private fun updateEmptyState() {
        if (selectedGroups.isEmpty()) {
            binding.emptyGroupsText.visibility = View.VISIBLE
            binding.groupsRecyclerView.visibility = View.GONE
        } else {
            binding.emptyGroupsText.visibility = View.GONE
            binding.groupsRecyclerView.visibility = View.VISIBLE
        }
    }
}
