package com.matrix.autoreply.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.matrix.autoreply.R

class ContactSelectorAdapter(
    private var contacts: List<String>,
    private val selectedContacts: MutableSet<String>,
    private val onSelectionChanged: () -> Unit,
    private val sortSelectedToTop: Boolean = true
) : RecyclerView.Adapter<ContactSelectorAdapter.ContactViewHolder>() {

    private var filteredContacts: List<String> = if (sortSelectedToTop) {
        sortContactsWithSelectedFirst(contacts)
    } else {
        contacts
    }

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkbox: MaterialCheckBox = itemView.findViewById(R.id.contactCheckbox)
        val nameText: TextView = itemView.findViewById(R.id.contactNameText)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val contactName = filteredContacts[position]
                    if (selectedContacts.contains(contactName)) {
                        selectedContacts.remove(contactName)
                    } else {
                        selectedContacts.add(contactName)
                    }
                    notifyItemChanged(position)
                    onSelectionChanged()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact_selector, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contactName = filteredContacts[position]
        holder.nameText.text = contactName
        holder.checkbox.isChecked = selectedContacts.contains(contactName)
    }

    override fun getItemCount(): Int = filteredContacts.size

    private fun sortContactsWithSelectedFirst(contactList: List<String>): List<String> {
        if (!sortSelectedToTop) return contactList
        
        val selected = contactList.filter { selectedContacts.contains(it) }.sorted()
        val unselected = contactList.filter { !selectedContacts.contains(it) }.sorted()
        return selected + unselected
    }
    
    fun filter(query: String) {
        val baseList = if (query.isEmpty()) {
            contacts
        } else {
            contacts.filter { it.contains(query, ignoreCase = true) }
        }
        filteredContacts = if (sortSelectedToTop) {
            sortContactsWithSelectedFirst(baseList)
        } else {
            baseList
        }
        notifyDataSetChanged()
    }

    fun selectAll() {
        selectedContacts.clear()
        selectedContacts.addAll(filteredContacts)
        notifyDataSetChanged()
        onSelectionChanged()
    }

    fun deselectAll() {
        selectedContacts.clear()
        notifyDataSetChanged()
        onSelectionChanged()
    }

    fun updateContacts(newContacts: List<String>) {
        contacts = newContacts
        filteredContacts = if (sortSelectedToTop) {
            sortContactsWithSelectedFirst(newContacts)
        } else {
            newContacts
        }
        notifyDataSetChanged()
    }
    
    fun refreshSorting() {
        filteredContacts = if (sortSelectedToTop) {
            sortContactsWithSelectedFirst(filteredContacts)
        } else {
            filteredContacts
        }
        notifyDataSetChanged()
    }
}
