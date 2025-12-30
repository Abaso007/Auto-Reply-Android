package com.matrix.autoreply.utils

import android.content.Context
import android.provider.ContactsContract
import android.util.Log

/**
 * Utility class for handling contact operations
 */
object ContactUtils {
    
    private const val TAG = "ContactUtils"
    
    /**
     * Load all contacts from device and return unique contact names
     * Removes duplicates by display name
     */
    fun getUniqueContactNames(context: Context): List<String> {
        val contacts = mutableListOf<String>()
        
        try {
            val cursor = context.contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC"
            )
            
            cursor?.use {
                val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                
                while (it.moveToNext()) {
                    val name = it.getString(nameIndex)
                    if (!name.isNullOrBlank()) {
                        contacts.add(name)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading contacts: ${e.message}", e)
        }
        
        // Remove duplicates and sort
        return contacts.distinct().sorted()
    }
}
