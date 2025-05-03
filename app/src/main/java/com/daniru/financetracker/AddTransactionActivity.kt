package com.daniru.financetracker

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.daniru.financetracker.model.Transaction
import com.daniru.financetracker.util.TransactionManager
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.util.*
import android.app.AlertDialog

class AddTransactionActivity : AppCompatActivity() {

    private var selectedDate: Long = System.currentTimeMillis() // Default to current time (Long)
    private var isUpdateMode = false  // Flag to check if we are updating an existing transaction
    private var transactionToUpdate: Transaction? = null  // Holds the transaction data to update

    private var totalBalance: Double = 1000.0 // Example: This can be fetched dynamically, representing the remaining balance for expenses

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        // Initialize views
        val titleEditText = findViewById<EditText>(R.id.editTitle)
        val amountEditText = findViewById<EditText>(R.id.editAmount)
        val categoryDropdown = findViewById<AutoCompleteTextView>(R.id.categoryDropdown)
        val dateButton = findViewById<Button>(R.id.btnSelectDate)
        val saveButton = findViewById<Button>(R.id.btnSaveTransaction)
        val transactionTypeTab = findViewById<TabLayout>(R.id.transactionTypeTab)

        // Set up category dropdown
        val categories = arrayOf("Meal", "Clothing", "Entertainment", "Utilities", "Transport", "Salary")
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        categoryDropdown.setAdapter(categoryAdapter)

        // Check if we're in update mode
        transactionToUpdate = intent.getParcelableExtra("transaction")
        transactionToUpdate?.let { transaction ->
            isUpdateMode = true
            titleEditText.setText(transaction.title)
            amountEditText.setText(transaction.amount.toString())
            categoryDropdown.setText(transaction.category, false)
            selectedDate = transaction.date
            dateButton.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selectedDate))

            // Set the tab based on transaction type
            if (transaction.type == "income") {
                transactionTypeTab.getTabAt(0)?.select()
            } else {
                transactionTypeTab.getTabAt(1)?.select()
            }
        }

        // Date picker setup
        dateButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            android.app.DatePickerDialog(this, { _, year, month, day ->
                calendar.set(year, month, day)
                selectedDate = calendar.timeInMillis // Store date as Long
                dateButton.text =
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
            }, calendar[Calendar.YEAR], calendar[Calendar.MONTH], calendar[Calendar.DAY_OF_MONTH]).show()
        }

        // Save button logic
        saveButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val amountString = amountEditText.text.toString().trim()
            val amount = amountString.toDoubleOrNull() ?: 0.0
            val category = categoryDropdown.text.toString().trim()

            if (title.isEmpty() || category.isEmpty() || amount <= 0) {
                Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if the transaction is an expense and if the expense exceeds the available balance
            if (transactionTypeTab.selectedTabPosition == 1 && amount > totalBalance) {
                // Show validation error in case of expense transaction exceeding available balance
                showExpenseExceedsBalanceErrorDialog()
                return@setOnClickListener
            }

            // Get the selected transaction type from the TabLayout
            val transactionType = when (transactionTypeTab.selectedTabPosition) {
                0 -> "income" // Income tab selected
                1 -> "expense" // Expense tab selected
                else -> "income" // Default to income if no selection
            }

            val transaction = Transaction(
                title = title,
                amount = amount,
                category = category,
                date = selectedDate,
                type = transactionType
            )

            val transactions = TransactionManager.getTransactions(this).toMutableList()

            if (isUpdateMode) {
                val indexToUpdate = transactions.indexOfFirst { it.date == transactionToUpdate?.date }
                if (indexToUpdate != -1) {
                    transactions[indexToUpdate] = transaction // Update the transaction
                }
            } else {
                transactions.add(transaction) // Add the new transaction
            }

            // Save the updated transactions list
            TransactionManager.saveTransactions(this, transactions)

            Toast.makeText(this, "Transaction saved!", Toast.LENGTH_SHORT).show()
            finish()  // Close the activity and return to the previous screen
        }
    }

    // Method to show an error dialog when the expense exceeds the available balance
    private fun showExpenseExceedsBalanceErrorDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Expense Exceeds Balance")
        builder.setMessage("You cannot add an expense that exceeds the available balance. Please enter a valid amount.")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }
}
