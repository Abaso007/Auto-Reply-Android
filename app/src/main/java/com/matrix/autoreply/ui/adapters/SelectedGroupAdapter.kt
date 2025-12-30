package com.matrix.autoreply.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.matrix.autoreply.databinding.ItemSelectedGroupBinding

class SelectedGroupAdapter(
    private val groups: MutableList<String>,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<SelectedGroupAdapter.GroupViewHolder>() {

    inner class GroupViewHolder(private val binding: ItemSelectedGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(groupName: String) {
            binding.groupNameText.text = groupName
            binding.deleteGroupButton.setOnClickListener {
                onDeleteClick(groupName)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = ItemSelectedGroupBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.bind(groups[position])
    }

    override fun getItemCount(): Int = groups.size

    fun addGroup(groupName: String) {
        groups.add(0, groupName) // Add at top instead of bottom
        notifyItemInserted(0)
    }
    
    fun removeGroup(groupName: String) {
        val position = groups.indexOf(groupName)
        if (position != -1) {
            groups.removeAt(position)
            notifyItemRemoved(position)
        }
    }
    
    fun updateGroups(newGroups: List<String>) {
        groups.clear()
        groups.addAll(newGroups)
        notifyDataSetChanged()
    }
}
