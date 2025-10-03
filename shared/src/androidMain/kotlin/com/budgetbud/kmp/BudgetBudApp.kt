package com.budgetbud.kmp

import android.app.Application
import com.budgetbud.kmp.auth.appContext
import android.util.Log


class BudgetBudApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("AppContext", "Initializing appContext in BudgetBudApp: $this")
        appContext = this.applicationContext
    }
}