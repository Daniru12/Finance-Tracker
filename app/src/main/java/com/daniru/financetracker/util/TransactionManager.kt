package com.daniru.financetracker.util

import android.content.Context
import com.daniru.financetracker.model.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken as GsonTypeToken

object TransactionManager {

    private const val PREF_NAME = "transactions"
    private const val KEY_TRANSACTIONS = "transaction_list"
    private const val KEY_BUDGET = "monthly_budget"

    // Save a list of transactions to SharedPreferences
    fun saveTransactions(context: Context, transactions: List<Transaction>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val json = Gson().toJson(transactions) // Convert the transactions list to JSON string
        editor.putString(KEY_TRANSACTIONS, json) // Save the JSON string
        editor.apply() // Apply the changes asynchronously
    }

    // Get the list of transactions from SharedPreferences
    fun getTransactions(context: Context): MutableList<Transaction> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_TRANSACTIONS, null) // Retrieve the JSON string
        return if (json != null) {
            val type = object : GsonTypeToken<List<Transaction>>() {}.type // Define the type for deserialization
            Gson().fromJson(json, type) // Deserialize JSON string back to List<Transaction>
        } else {
            mutableListOf() // Return an empty list if no transactions are found
        }
    }

    // Save the budget value to SharedPreferences
    fun saveBudget(context: Context, budget: Double) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putFloat(KEY_BUDGET, budget.toFloat()) // Save the budget as a float
        editor.apply() // Apply the changes asynchronously
    }

    // Get the saved budget value from SharedPreferences
    fun getBudget(context: Context): Double {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getFloat(KEY_BUDGET, 0.0f).toDouble() // Retrieve the budget (default 0.0f)
    }
}
