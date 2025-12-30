package com.matrix.autoreply.ui.activity

import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import com.matrix.autoreply.R
import com.matrix.autoreply.ui.fragment.ContactSelectorFragment

class ContactSelectorActivity : BaseActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_selector)
        
        // Customize action bar
        val actionBar: ActionBar? = supportActionBar
        val colorDrawable = "#171D3B".toColorInt().toDrawable()
        actionBar?.setBackgroundDrawable(colorDrawable)
        actionBar?.title = "Select Contacts"
        actionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Add ContactSelectorFragment if not already added
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ContactSelectorFragment())
                .commit()
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
