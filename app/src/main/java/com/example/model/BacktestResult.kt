package com.example.model

data class BacktestTrade(
    val id: String,
    val symbol: String,
    val direction: SignalDirection,
    val entryTime: Long,
    val exitTime: Long,
    val entryPrice: Double,
    val exitPrice: Double,
    val outcome: String,
    val rMultiple: Double,
    val pnlPercent: Double,
    val setupReason: String
)

data class BacktestReport(
    val strategyName: String = "Apex Institutional SMC Engine",
    val testPeriod: String,
    val totalTrades: Int,
    val winningTrades: Int,
    val losingTrades: Int,
    val winRatePercent: Double,
    val profitFactor: Double,
    val averageRMultiple: Double,
    val maxDrawdownPercent: Double,
    val sharpeRatio: Double,
    val netPnlPercent: Double,
    val consecutiveLossesMax: Int,
    val consecutiveWinsMax: Int,
    val modeledSpreadPips: Double,
    val modeledSlippagePips: Double,
    val tradeList: List<BacktestTrade>
)
