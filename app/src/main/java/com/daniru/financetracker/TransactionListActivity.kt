package com.daniru.financetracker

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daniru.financetracker.model.Transaction
import com.daniru.financetracker.util.TransactionManager

class TransactionListActivity : AppCompatActivity(), TransactionAdapter.OnItemClickListener {

    private lateinit var transactions: MutableList<Transaction>  // Use a mutable list for dynamic updates
    private lateinit var recyclerView: RecyclerView
    private lateinit var incomeAmount: TextView
    private lateinit var expenseAmount: TextView
    private lateinit var balanceAmount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_list) // Make sure the correct layout is set

        // Initialize views with correct IDs from XML
        recyclerView = findViewById(R.id.transactionRecyclerView)
        incomeAmount = findViewById(R.id.incomeAmount)
        expenseAmount = findViewById(R.id.expenseAmount)
        balanceAmount = findViewById(R.id.balanceAmount)

        // Load transactions
        transactions = TransactionManager.getTransactions(this).toMutableList()

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = TransactionAdapter(transactions, this)

        // Update total text (Income and Expense)
        updateTotalText()

        // Set the balance amount (in your case this could be total income - total expense)
        balanceAmount.text = "Rs. %.2f".format(getTotalBalance())
    }

    // Handle Update button click in the RecyclerView
    override fun onUpdateClick(position: Int) {
        val transaction = transactions[position]

        // Start an activity to update the transaction (for example, open AddTransactionActivity for editing)
        val intent = Intent(this, AddTransactionActivity::class.java).apply {
            putExtra("transaction", transaction)  // Pass the transaction data to the update screen
        }
        startActivity(intent)
    }

    // Handle Delete button click in the RecyclerView
    override fun onDeleteClick(position: Int) {
        // Remove the transaction from the list
        transactions.removeAt(position)

        // Update the transaction list in SharedPreferences
        TransactionManager.saveTransactions(this, transactions)

        // Notify RecyclerView that an item was removed
        recyclerView.adapter?.notifyItemRemoved(position)

        // Update total text
        updateTotalText()
    }

    // Update total text (Income and Expense)
    private fun updateTotalText() {
        val income = transactions.filter { it.type == "income" }.sumOf { it.amount }
        val expense = transactions.filter { it.type == "expense" }.sumOf { it.amount }
        incomeAmount.text = "Rs. %.2f".format(income)
        expenseAmount.text = "Rs. %.2f".format(expense)
    }

    // Get the total balance (Income - Expense)
    private fun getTotalBalance(): Double {
        val income = transactions.filter { it.type == "income" }.sumOf { it.amount }
        val expense = transactions.filter { it.type == "expense" }.sumOf { it.amount }
        return income - expense
    }
}
