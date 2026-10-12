package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trade_journal")
data class TradeJournalEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val symbol: String,
    val direction: String,
    val setupType: String,
    val entryPrice: Double,
    val exitPrice: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val lotSize: Double,
    val pnlDollars: Double,
    val netRMultiple: Double,
    val ruleComplianceRating: Int, // 1 to 5
    val emotionState: String,
    val notes: String,
    val timestamp: Long,
    val outcome: String, // WIN, LOSS, BREAK_EVEN, OPEN
    val isPaperTrade: Boolean = true
)
