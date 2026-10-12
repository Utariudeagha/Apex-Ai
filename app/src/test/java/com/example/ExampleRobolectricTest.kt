package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.model.TradeJournalEntry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun read_string_from_context() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("APEX SNIPER AI", appName)
    }

    @Test
    fun verify_room_database_trade_persistence() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getDatabase(context)
        val dao = db.tradeDao()

        val trade = TradeJournalEntry(
            symbol = "XAUUSD",
            direction = "LONG",
            setupType = "Liquidity Sweep Reversal",
            entryPrice = 2654.40,
            exitPrice = 2668.50,
            stopLoss = 2648.00,
            takeProfit = 2668.50,
            lotSize = 1.5,
            pnlDollars = 2115.0,
            netRMultiple = 2.2,
            ruleComplianceRating = 5,
            emotionState = "Disciplined",
            notes = "Executed at London open sweep of Asian low",
            timestamp = System.currentTimeMillis(),
            outcome = "WIN",
            isPaperTrade = true
        )

        val id = dao.insertTrade(trade)
        assertNotNull(id)

        val retrieved = dao.getAllTrades().first()
        assertEquals(true, retrieved.any { it.symbol == "XAUUSD" })
    }
}
