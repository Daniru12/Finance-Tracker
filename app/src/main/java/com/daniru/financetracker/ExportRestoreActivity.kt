package com.daniru.financetracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.daniru.financetracker.model.Transaction
import com.daniru.financetracker.util.TransactionBackup
import com.daniru.financetracker.util.TransactionManager

class ExportRestoreActivity : AppCompatActivity() {

    private lateinit var btnExport: Button
    private lateinit var btnRestore: Button
    private lateinit var tvLastBackup: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export_restore)

        btnExport = findViewById(R.id.btnExport)
        btnRestore = findViewById(R.id.btnRestore)
        tvLastBackup = findViewById(R.id.tvLastBackup)

        // Display the last backup information
        updateLastBackupInfo()

        // Export Data Button Click Listener
        btnExport.setOnClickListener {
            val transactions = TransactionManager.getTransactions(this)
            if (transactions.isNotEmpty()) {
                val success = TransactionBackup.exportTransactionsToFile(this, transactions)
                if (success) {
                    saveLastBackupTime()  // Save the last backup time after successful export
                    updateLastBackupInfo()  // Update the displayed backup time
                    navigateToMainActivity()  // Navigate to MainActivity after export
                }
            } else {
                showToast("No transactions to export")
            }
        }

        // Restore Data Button Click Listener
        btnRestore.setOnClickListener {
            val restoredTransactions = TransactionBackup.restoreTransactionsFromFile(this)
            if (restoredTransactions.isNotEmpty()) {
                TransactionManager.saveTransactions(this, restoredTransactions)
                showToast("Data restored successfully")
                saveLastBackupTime()  // Save the last backup time after successful restore
                updateLastBackupInfo()  // Update the displayed backup time
                navigateToMainActivity()  // Navigate to MainActivity after restore
            }
        }
    }

    // Method to update the last backup info on the UI
    private fun updateLastBackupInfo() {
        val sharedPrefs = getSharedPreferences("transactions", Context.MODE_PRIVATE)
        val lastBackupTime = sharedPrefs.getLong("last_backup_time", 0L)
        if (lastBackupTime > 0) {
            tvLastBackup.text = "Last backup: ${formatDate(lastBackupTime)}"
        } else {
            tvLastBackup.text = "Last backup: Never"
        }
    }

    // Method to format the timestamp into a readable date
    private fun formatDate(timestamp: Long): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }

    // Method to show a toast message
    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    // Method to save the current timestamp as the last backup time in SharedPreferences
    private fun saveLastBackupTime() {
        val sharedPrefs = getSharedPreferences("transactions", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.putLong("last_backup_time", System.currentTimeMillis())  // Save current time as last backup time
        editor.apply()
    }

    // Method to navigate to MainActivity
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()  // Finish ExportRestoreActivity to prevent it from being on the back stack
    }
}
