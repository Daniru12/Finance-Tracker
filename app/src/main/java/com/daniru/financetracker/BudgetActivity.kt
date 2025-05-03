package com.daniru.financetracker

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.daniru.financetracker.util.TransactionManager
import com.daniru.financetracker.util.NotificationHelper
import java.text.DecimalFormat

class BudgetActivity : AppCompatActivity() {

    private lateinit var budgetProgressBar: ProgressBar
    private lateinit var remainingBudgetInfo: TextView
    private lateinit var budgetStatusMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        val budgetText = findViewById<TextView>(R.id.currentBudgetText)
        val budgetInput = findViewById<EditText>(R.id.editBudget)
        val setButton = findViewById<Button>(R.id.btnSetBudget)

        // Initialize the progress bar and remaining budget info
        budgetProgressBar = findViewById(R.id.budgetProgressBar)
        remainingBudgetInfo = findViewById(R.id.remainingBudgetInfo)
        budgetStatusMessage = findViewById(R.id.budgetStatusMessage)

        // Set initial Rs. prefix in the EditText
        budgetInput.setText("Rs. ")

        // Ensure that the user only enters numeric values after "Rs."
        budgetInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Remove "Rs. " if the user starts typing
                if (s.toString().startsWith("Rs. ")) {
                    budgetInput.setText(s.toString().replace("Rs. ", ""))
                    budgetInput.setSelection(budgetInput.length())
                }
            }

            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Load the current budget and display it
        val currentBudget = TransactionManager.getBudget(this)
        budgetText.text = "Current Budget: ${format(currentBudget)}"

        // Notification helper to handle notifications
        val notificationHelper = NotificationHelper(this)

        setButton.setOnClickListener {
            val newBudget = budgetInput.text.toString().replace("Rs. ", "").toDoubleOrNull()

            if (newBudget != null && newBudget > 0) {
                // Save the new budget value
                saveBudget(newBudget)

                // Recalculate the total spending after saving the new budget
                val totalSpending = calculateTotalSpending()

                // Update UI with new budget value
                budgetText.text = "Current Budget: ${format(newBudget)}"
                Toast.makeText(this, "Budget set successfully", Toast.LENGTH_SHORT).show()

                // Check if the budget is exceeded and send notification
                if (totalSpending > newBudget) {
                    notificationHelper.sendBudgetExceededNotification()
                }

                // Update progress bar
                updateProgressBar(newBudget, totalSpending)

                // After setting the budget, navigate back to MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent) // Navigate back to MainActivity
                finish() // Close BudgetActivity so that it doesn't stay in the back stack
            } else {
                // If invalid input, show a message
                Toast.makeText(this, "Enter a valid positive amount", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Update progress and budget info when returning to BudgetActivity
        val currentBudget = TransactionManager.getBudget(this)
        val totalSpending = calculateTotalSpending()

        // Update the UI with the current budget and progress
        updateProgressBar(currentBudget, totalSpending)
    }

    private fun updateProgressBar(currentBudget: Double, totalSpending: Double) {
        // Update the progress bar based on the current budget and total expenses
        val progress = if (currentBudget > 0) {
            (totalSpending / currentBudget * 100).toInt()
        } else {
            0
        }

        budgetProgressBar.progress = progress

        // Display the remaining budget
        val remainingBudget = currentBudget - totalSpending
        remainingBudgetInfo.text = "Budget Remaining: ${format(remainingBudget)}"

        // Show message if out of budget
        if (totalSpending > currentBudget) {
            budgetStatusMessage.text = "Out of budget!"
        } else {
            budgetStatusMessage.text = ""
        }
    }

    private fun calculateTotalSpending(): Double {
        // Get the list of transactions and calculate the total spending (only expense transactions)
        val transactions = TransactionManager.getTransactions(this)
        return transactions.filter { it.type == "expense" }.sumOf { it.amount }
    }

    // Function to save the budget as a separate value
    private fun saveBudget(newBudget: Double) {
        // Save the new budget value as a separate variable (not as an income or expense)
        TransactionManager.saveBudget(this, newBudget)
    }

    // Format the amount as currency with "Rs." prefix (e.g., Rs. 500.00)
    private fun format(amount: Double): String {
        // Create a DecimalFormat to format the number
        val formatter = DecimalFormat("###,###,###.##")
        return "Rs. ${formatter.format(amount)}"  // Return formatted value with Rs. prefix
    }
}
