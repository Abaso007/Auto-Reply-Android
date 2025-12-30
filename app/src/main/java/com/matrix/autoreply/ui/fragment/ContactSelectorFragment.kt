package com.matrix.autoreply.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.matrix.autoreply.R
import com.matrix.autoreply.databinding.FragmentContactSelectorBinding
import com.matrix.autoreply.preferences.PreferencesManager
import com.matrix.autoreply.store.data.ContactGroup
import com.matrix.autoreply.store.database.MessageLogsDB
import com.matrix.autoreply.ui.adapters.ContactSelectorAdapter
import com.matrix.autoreply.utils.ContactUtils
import org.json.JSONArray

class ContactSelectorFragment : Fragment() {

    private var _binding: FragmentContactSelectorBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ContactSelectorAdapter
    private lateinit var recentlySelectedAdapter: ContactSelectorAdapter
    private lateinit var preferencesManager: PreferencesManager
    
    private val selectedContacts = mutableSetOf<String>()
    private var allContacts: List<String> = emptyList()
    private var lastSelectedContacts: Set<String> = emptySet()
    private var contactGroups: List<ContactGroup> = emptyList()
    
    private var isRecentlySectionExpanded = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactSelectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        preferencesManager = PreferencesManager.getPreferencesInstance(requireContext())!!
        
        // Load pre-selected contacts
        selectedContacts.addAll(preferencesManager.getSelectedContactsForReply())
        
        setupToolbar()
        loadContacts()
        loadLastSelected()
        loadContactGroups()
        setupListeners()
    }

    private fun setupToolbar() {
        // Hide fragment's toolbar since Activity provides its own action bar
        binding.appBarLayout.visibility = View.GONE
    }

    private fun loadContacts() {
        // Load contacts in background
        Thread {
            allContacts = ContactUtils.getUniqueContactNames(requireContext())
            
            activity?.runOnUiThread {
                if (allContacts.isEmpty()) {
                    showEmptyState()
                } else {
                    setupAdapter()
                    updateSelectedCount()
                }
            }
        }.start()
    }

    private fun loadLastSelected() {
        lastSelectedContacts = preferencesManager.getLastSelectedContacts()
        
        if (lastSelectedContacts.isNotEmpty()) {
            binding.recentlySelectedCard.visibility = View.VISIBLE
            setupRecentlySelectedAdapter()
        }
    }

    private fun loadContactGroups() {
        Thread {
            val db = MessageLogsDB.getInstance(requireContext())
            contactGroups = db?.contactGroupDao()?.getAllGroups() ?: emptyList()
            
            activity?.runOnUiThread {
                updateGroupChips()
            }
        }.start()
    }

    private fun setupAdapter() {
        adapter = ContactSelectorAdapter(allContacts, selectedContacts, {
            updateSelectedCount()
            updateSelectAllCheckbox()
            adapter.refreshSorting() // Re-sort after selection changes
        }, sortSelectedToTop = true)
        
        binding.contactsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.contactsRecyclerView.adapter = adapter
        binding.contactsRecyclerView.visibility = View.VISIBLE
        binding.emptyStateLayout.visibility = View.GONE
    }

    private fun setupRecentlySelectedAdapter() {
        val recentContacts = lastSelectedContacts.toList()
        recentlySelectedAdapter = ContactSelectorAdapter(
            recentContacts, 
            selectedContacts, 
            {
                updateSelectedCount()
                updateSelectAllCheckbox()
                adapter.refreshSorting()
            },
            sortSelectedToTop = false
        )
        
        binding.recentlySelectedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recentlySelectedRecyclerView.adapter = recentlySelectedAdapter
    }

    private fun showEmptyState() {
        binding.contactsRecyclerView.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.VISIBLE
        binding.selectAllCheckbox.isEnabled = false
    }

    private fun setupListeners() {
        // Search functionality
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (::adapter.isInitialized) {
                    adapter.filter(s?.toString() ?: "")
                }
            }
        })

        // Select all checkbox
        binding.selectAllCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (::adapter.isInitialized) {
                if (isChecked) {
                    adapter.selectAll()
                } else {
                    adapter.deselectAll()
                }
                updateSelectedCount()
            }
        }

        // Recently selected toggle
        binding.toggleRecentlySelectedButton.setOnClickListener {
            isRecentlySectionExpanded = !isRecentlySectionExpanded
            binding.recentlySelectedRecyclerView.visibility = 
                if (isRecentlySectionExpanded) View.VISIBLE else View.GONE
            binding.toggleRecentlySelectedButton.text = 
                if (isRecentlySectionExpanded) "▼" else "▶"
        }

        // Create group button
        binding.createGroupButton.setOnClickListener {
            showCreateGroupDialog()
        }

        // Save button
        binding.saveFab.setOnClickListener {
            saveAndExit()
        }
    }

    private fun updateGroupChips() {
        binding.groupChipGroup.removeAllViews()
        
        contactGroups.forEach { group ->
            val chip = Chip(requireContext()).apply {
                text = "${group.name} (${getContactCountFromGroup(group)})"
                isCheckable = true
                isClickable = true
                isCloseIconVisible = true
                
                setOnClickListener {
                    applyGroupToSelection(group)
                }
                
                setOnCloseIconClickListener {
                    showDeleteGroupDialog(group)
                }
                
                setOnLongClickListener {
                    showEditGroupDialog(group)
                    true
                }
            }
            binding.groupChipGroup.addView(chip)
        }
    }

    private fun getContactCountFromGroup(group: ContactGroup): Int {
        return try {
            JSONArray(group.contactNames).length()
        } catch (e: Exception) {
            group.contactNames.split(",").filter { it.isNotEmpty() }.size
        }
    }

    private fun applyGroupToSelection(group: ContactGroup) {
        try {
            val contacts = JSONArray(group.contactNames)
            for (i in 0 until contacts.length()) {
                val contactName = contacts.getString(i)
                if (allContacts.contains(contactName)) {
                    selectedContacts.add(contactName)
                }
            }
        } catch (e: Exception) {
            // Fallback to comma-separated
            val contacts = group.contactNames.split(",").filter { it.isNotEmpty() }
            contacts.forEach { contactName ->
                if (allContacts.contains(contactName)) {
                    selectedContacts.add(contactName)
                }
            }
        }
        
        // Update last used time
        Thread {
            val db = MessageLogsDB.getInstance(requireContext())
            db?.contactGroupDao()?.updateLastUsedTime(group.id, System.currentTimeMillis())
        }.start()
        
        adapter.refreshSorting()
        updateSelectedCount()
        updateSelectAllCheckbox()
        
        Toast.makeText(requireContext(), "Applied group: ${group.name}", Toast.LENGTH_SHORT).show()
    }

    private fun showCreateGroupDialog() {
        if (selectedContacts.isEmpty()) {
            Toast.makeText(requireContext(), "Please select contacts first", Toast.LENGTH_SHORT).show()
            return
        }
        
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_contact_group, null)
        val groupNameEditText = dialogView.findViewById<TextInputEditText>(R.id.groupNameEditText)
        val selectedContactsLabel = dialogView.findViewById<android.widget.TextView>(R.id.selectedContactsLabel)
        
        selectedContactsLabel.text = "Selected Contacts: ${selectedContacts.size}"
        
        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val groupName = groupNameEditText.text?.toString()?.trim()
                if (groupName.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "Please enter a group name", Toast.LENGTH_SHORT).show()
                } else {
                    saveContactGroup(groupName)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showEditGroupDialog(group: ContactGroup) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_contact_group, null)
        val dialogTitle = dialogView.findViewById<android.widget.TextView>(R.id.dialogTitle)
        val groupNameEditText = dialogView.findViewById<TextInputEditText>(R.id.groupNameEditText)
        val selectedContactsLabel = dialogView.findViewById<android.widget.TextView>(R.id.selectedContactsLabel)
        
        dialogTitle.text = "Edit Contact Group"
        groupNameEditText.setText(group.name)
        selectedContactsLabel.text = "Contacts: ${getContactCountFromGroup(group)}"
        
        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val newGroupName = groupNameEditText.text?.toString()?.trim()
                if (newGroupName.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "Please enter a group name", Toast.LENGTH_SHORT).show()
                } else {
                    updateContactGroup(group, newGroupName)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showDeleteGroupDialog(group: ContactGroup) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Group")
            .setMessage("Are you sure you want to delete \"${group.name}\"?")
            .setPositiveButton("Delete") { dialog, _ ->
                deleteContactGroup(group)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun saveContactGroup(groupName: String) {
        Thread {
            try {
                val contactsJson = JSONArray(selectedContacts.toList()).toString()
                
                val group = ContactGroup(
                    name = groupName,
                    contactNames = contactsJson,
                    createdAt = System.currentTimeMillis(),
                    lastUsedAt = System.currentTimeMillis()
                )
                
                val db = MessageLogsDB.getInstance(requireContext())
                db?.contactGroupDao()?.insertGroup(group)
                
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Group created: $groupName", Toast.LENGTH_SHORT).show()
                    loadContactGroups()
                }
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Error creating group: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun updateContactGroup(group: ContactGroup, newName: String) {
        Thread {
            try {
                val updatedGroup = group.copy(name = newName)
                val db = MessageLogsDB.getInstance(requireContext())
                db?.contactGroupDao()?.updateGroup(updatedGroup)
                
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Group updated", Toast.LENGTH_SHORT).show()
                    loadContactGroups()
                }
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Error updating group: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun deleteContactGroup(group: ContactGroup) {
        Thread {
            try {
                val db = MessageLogsDB.getInstance(requireContext())
                db?.contactGroupDao()?.deleteGroup(group)
                
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Group deleted", Toast.LENGTH_SHORT).show()
                    loadContactGroups()
                }
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Error deleting group: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun updateSelectedCount() {
        val count = selectedContacts.size
        binding.selectedCountText.text = "$count selected"
    }

    private fun updateSelectAllCheckbox() {
        if (::adapter.isInitialized && allContacts.isNotEmpty()) {
            binding.selectAllCheckbox.isChecked = selectedContacts.size == allContacts.size
        }
    }

    private fun saveAndExit() {
        // Save selected contacts
        preferencesManager.setSelectedContactsForReply(selectedContacts)
        
        // Save as last selected
        preferencesManager.setLastSelectedContacts(selectedContacts)
        
        Toast.makeText(requireContext(), "Contacts saved", Toast.LENGTH_SHORT).show()
        requireActivity().onBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
