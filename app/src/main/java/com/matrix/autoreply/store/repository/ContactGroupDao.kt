package com.matrix.autoreply.store.repository

import androidx.room.*
import com.matrix.autoreply.store.data.ContactGroup

@Dao
interface ContactGroupDao {
    
    @Query("SELECT * FROM contact_groups ORDER BY lastUsedAt DESC")
    fun getAllGroups(): List<ContactGroup>
    
    @Query("SELECT * FROM contact_groups WHERE id = :groupId")
    fun getGroupById(groupId: Long): ContactGroup?
    
    @Query("SELECT * FROM contact_groups ORDER BY lastUsedAt DESC LIMIT :limit")
    fun getRecentGroups(limit: Int): List<ContactGroup>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGroup(group: ContactGroup): Long
    
    @Update
    fun updateGroup(group: ContactGroup)
    
    @Delete
    fun deleteGroup(group: ContactGroup)
    
    @Query("DELETE FROM contact_groups WHERE id = :groupId")
    fun deleteGroupById(groupId: Long)
    
    @Query("UPDATE contact_groups SET lastUsedAt = :timestamp WHERE id = :groupId")
    fun updateLastUsedTime(groupId: Long, timestamp: Long)
}
