package com.daniru.financetracker

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.daniru.financetracker.model.Transaction
import com.daniru.financetracker.util.TransactionManager
import com.daniru.financetracker.util.NotificationHelper
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var fabAdd: ExtendedFloatingActionButton
    private lateinit var totalBalanceText: TextView
    private lateinit var incomeText: TextView
    private lateinit var expenseText: TextView
    private lateinit var lastCategoryText: TextView
    private lateinit var lastAmountText: TextView
    private lateinit var btnSeeAll: Button
    private lateinit var btnSetBudget: Button
    private lateinit var todayTransactionsLayout: LinearLayout
    private lateinit var pieChart: PieChart
    private lateinit var imageView2: ImageView

    private lateinit var notificationHelper: NotificationHelper  // Add notification helper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        fabAdd = findViewById(R.id.fabAdd)
        totalBalanceText = findViewById(R.id.totalBalanceValue)
        incomeText = findViewById(R.id.incomeValue)
        expenseText = findViewById(R.id.expenseValue)
        lastCategoryText = findViewById(R.id.lastCategoryValue)
        lastAmountText = findViewById(R.id.lastAmountValue)
        btnSeeAll = findViewById(R.id.btnSeeAll)
        btnSetBudget = findViewById(R.id.btnSetBudget)
        todayTransactionsLayout = findViewById(R.id.todayTransactionsLayout)
        pieChart = findViewById(R.id.pieChart)
        imageView2 = findViewById(R.id.imageView2)

        notificationHelper = NotificationHelper(this)  // Initialize NotificationHelper

        // Set onClickListeners
        fabAdd.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }

        btnSeeAll.setOnClickListener {
            val intent = Intent(this, TransactionListActivity::class.java)
            startActivity(intent)
        }

        btnSetBudget.setOnClickListener {
            val intent = Intent(this, BudgetActivity::class.java)
            startActivity(intent)
        }

        imageView2.setOnClickListener {
            // Navigate to the ExportRestoreActivity
            val intent = Intent(this, ExportRestoreActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        updateDashboard()  // Update dashboard when the activity is resumed
    }

    private fun updateDashboard() {
        val transactions = TransactionManager.getTransactions(this)
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        // Filter transactions for today
        val todayTransactions = transactions.filter {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it.date)) == today
        }

        // Calculate income and expense
        val income = transactions.filter { it.type == "income" }.sumOf { it.amount }
        val expense = transactions.filter { it.type == "expense" }.sumOf { it.amount }
        val balance = income - expense

        // Get the current budget
        val currentBudget = TransactionManager.getBudget(this)

        // If the balance exceeds the budget, send a notification
        if (balance > currentBudget) {
            notificationHelper.sendBudgetExceededNotification()
        }

        // Update UI with totals
        totalBalanceText.text = "LKR %.2f".format(balance)
        incomeText.text = "LKR %.2f".format(income)
        expenseText.text = "LKR %.2f".format(expense)

        // Set data for PieChart
        updatePieChart(income, expense)

        // Clear previous transactions and display today's transactions
        todayTransactionsLayout.removeAllViews()
        todayTransactions.forEach { txn ->
            val card = CardView(this).apply {
                radius = 12f
                cardElevation = 8f
                useCompatPadding = true
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 4, 0, 4)
                }
            }

            val content = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32, 24, 32, 24)
            }

            val title = TextView(this).apply {
                text = txn.title
                textSize = 16f
                setTextColor(resources.getColor(android.R.color.black))
            }

            val category = TextView(this).apply {
                text = "Category: ${txn.category}"
                textSize = 14f
                setTextColor(resources.getColor(android.R.color.darker_gray))
            }

            val amount = TextView(this).apply {
                text = "LKR %.2f".format(txn.amount)
                textSize = 18f
                setTextColor(
                    if (txn.type == "income") resources.getColor(android.R.color.holo_green_dark)
                    else resources.getColor(android.R.color.holo_red_dark)
                )
                setPadding(0, 0, 0, 0)
            }

            content.addView(title)
            content.addView(category)
            content.addView(amount)
            card.addView(content)
            todayTransactionsLayout.addView(card)
        }

        // Update last transaction details
        if (todayTransactions.isNotEmpty()) {
            val last = todayTransactions.last()
            lastCategoryText.text = last.category
            lastAmountText.text = "LKR %.2f".format(last.amount)
        } else {
            lastCategoryText.text = "No transactions"
            lastAmountText.text = "LKR 0.00"
        }
    }

    private fun updatePieChart(income: Double, expense: Double) {
        // Prepare data for the PieChart
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(income.toFloat(), "Income"))
        entries.add(PieEntry(expense.toFloat(), "Expense"))

        val dataSet = PieDataSet(entries, "Income vs Expense")
        // Set colors for the income and expense sections
        dataSet.setColors(intArrayOf(Color.GREEN, Color.RED), 255)

        val pieData = PieData(dataSet)
        pieChart.data = pieData
        pieChart.invalidate()  // Refresh the chart
    }
}
