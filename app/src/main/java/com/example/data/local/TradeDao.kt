package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.TradeJournalEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeDao {
    @Query("SELECT * FROM trade_journal ORDER BY timestamp DESC")
    fun getAllTrades(): Flow<List<TradeJournalEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: TradeJournalEntry): Long

    @Update
    suspend fun updateTrade(trade: TradeJournalEntry)

    @Delete
    suspend fun deleteTrade(trade: TradeJournalEntry)

    @Query("SELECT * FROM trade_journal WHERE symbol = :symbol ORDER BY timestamp DESC")
    fun getTradesForSymbol(symbol: String): Flow<List<TradeJournalEntry>>

    @Query("SELECT COUNT(*) FROM trade_journal")
    suspend fun getTradeCount(): Int
}
