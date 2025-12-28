package com.matrix.autoreply.helpers

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.matrix.autoreply.R
import androidx.core.net.toUri

object DonateHelper {
    
    fun showDonateDialog(context: Context) {
        val builder = android.app.AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.donate_card_title))
        builder.setMessage(context.getString(R.string.donate_card_message))
        
        // Negative button (LEFT) - Buy Me a Coffee (primary)
        builder.setNegativeButton(context.getString(R.string.donate_button_primary)) { _, _ ->
            openDonateUrl(context, context.getString(R.string.donate_url_primary))
        }
        
        // Neutral button (MIDDLE) - GitHub Sponsors (secondary)
        builder.setNeutralButton(context.getString(R.string.donate_button_secondary)) { _, _ ->
            openDonateUrl(context, context.getString(R.string.donate_url_secondary))
        }
        
        // Positive button (RIGHT) - Cancel
        builder.setPositiveButton(android.R.string.cancel, null)
        
        builder.show()
    }
    
    private fun openDonateUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            context.startActivity(intent)
            Toast.makeText(context, context.getString(R.string.donate_thank_you), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open browser", Toast.LENGTH_SHORT).show()
        }
    }
}
