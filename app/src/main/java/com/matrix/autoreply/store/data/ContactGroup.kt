package com.matrix.autoreply.store.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contact_groups")
data class ContactGroup(
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0,
    val name: String,
    val contactNames: String, // JSON array of contact names
    val createdAt: Long,
    val lastUsedAt: Long
)
