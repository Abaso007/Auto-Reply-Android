package com.matrix.autoreply.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.matrix.autoreply.R
import com.matrix.autoreply.ui.adapters.ContactSelectorAdapter
import com.matrix.autoreply.utils.ContactUtils

class ContactSelectorDialog(
    context: Context,
    private val preSelectedContacts: Set<String>,
    private val onContactsSelected: (Set<String>) -> Unit
) : Dialog(context) {

    private lateinit var contactsRecyclerView: RecyclerView
    private lateinit var searchEditText: TextInputEditText
    private lateinit var selectAllCheckbox: MaterialCheckBox
    private lateinit var selectedCountText: TextView
    private lateinit var emptyStateLayout: View
    private lateinit var cancelButton: MaterialButton
    private lateinit var saveButton: MaterialButton
    
    private val selectedContacts = preSelectedContacts.toMutableSet()
    private lateinit var adapter: ContactSelectorAdapter
    private var allContacts: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_contact_selector)
        
        // Set dialog to be full width
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.95).toInt(),
            (context.resources.displayMetrics.heightPixels * 0.8).toInt()
        )
        
        initViews()
        loadContacts()
        setupListeners()
    }

    private fun initViews() {
        contactsRecyclerView = findViewById(R.id.contactsRecyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        selectAllCheckbox = findViewById(R.id.selectAllCheckbox)
        selectedCountText = findViewById(R.id.selectedCountText)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        cancelButton = findViewById(R.id.cancelButton)
        saveButton = findViewById(R.id.saveButton)
        
        contactsRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun loadContacts() {
        // Load contacts in background to avoid blocking UI
        Thread {
            allContacts = ContactUtils.getUniqueContactNames(context)
            
            // Update UI on main thread using Handler
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                if (allContacts.isEmpty()) {
                    showEmptyState()
                } else {
                    setupAdapter()
                    updateSelectedCount()
                }
            }
        }.start()
    }

    private fun setupAdapter() {
        adapter = ContactSelectorAdapter(
            allContacts, 
            selectedContacts, 
            onSelectionChanged = {
                updateSelectedCount()
                updateSelectAllCheckbox()
            },
            sortSelectedToTop = false  // Old dialog doesn't need sorting
        )
        contactsRecyclerView.adapter = adapter
        contactsRecyclerView.visibility = View.VISIBLE
        emptyStateLayout.visibility = View.GONE
    }

    private fun showEmptyState() {
        contactsRecyclerView.visibility = View.GONE
        emptyStateLayout.visibility = View.VISIBLE
        selectAllCheckbox.isEnabled = false
    }

    private fun setupListeners() {
        // Search functionality
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (::adapter.isInitialized) {
                    adapter.filter(s?.toString() ?: "")
                }
            }
        })

        // Select all checkbox
        selectAllCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (::adapter.isInitialized) {
                if (isChecked) {
                    adapter.selectAll()
                } else {
                    adapter.deselectAll()
                }
                updateSelectedCount()
            }
        }

        // Cancel button
        cancelButton.setOnClickListener {
            dismiss()
        }

        // Save button
        saveButton.setOnClickListener {
            onContactsSelected(selectedContacts)
            dismiss()
        }
    }

    private fun updateSelectedCount() {
        val count = selectedContacts.size
        selectedCountText.text = "$count selected"
    }

    private fun updateSelectAllCheckbox() {
        if (::adapter.isInitialized && allContacts.isNotEmpty()) {
            selectAllCheckbox.isChecked = selectedContacts.size == allContacts.size
        }
    }
}
