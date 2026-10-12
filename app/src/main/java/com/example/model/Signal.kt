package com.example.model

enum class SignalState(val label: String, val description: String) {
    POTENTIAL("POTENTIAL", "Possible setup forming; awaiting zone tap"),
    WAITING_FOR_CONFIRMATION("WAITING FOR CONFIRMATION", "Price at zone; awaiting 5M trigger & rejection close"),
    CONFIRMED("CONFIRMED", "All 6 score categories >= 85 and ALL hard filters passed"),
    ACTIVE("ACTIVE", "Trigger filled; in active lifecycle management"),
    COMPLETED("COMPLETED", "Terminal state reached (Target hit or controlled stop)"),
    INVALIDATED("INVALIDATED", "Structure invalidated before entry triggered"),
    EXPIRED("EXPIRED", "Trading session expired without fill"),
    NO_TRADE("NO TRADE", "No qualifying setup satisfies high-probability rules")
}

enum class SignalDirection(val label: String) {
    LONG("LONG / BUY"),
    SHORT("SHORT / SELL"),
    NEUTRAL("NEUTRAL")
}

enum class SetupType(val code: String, val title: String) {
    LIQUIDITY_SWEEP_REVERSAL("SWEEP-REV", "Institutional Liquidity Sweep & Reversal"),
    ORDER_BLOCK_MITIGATION("OB-MIT", "Displacement Order Block Mitigation"),
    FVG_CONTINUATION("FVG-CONT", "Fair Value Gap Imbalance Continuation"),
    CHOCH_STRUCTURAL_SHIFT("CHOCH-SHIFT", "Change of Character Structural Shift"),
    BREAKER_RETEST("BRK-RETEST", "Failed Auction Breaker Block Retest")
}

data class HardFilterCheck(
    val filterName: String,
    val passed: Boolean,
    val detail: String
)

data class ScoreBreakdown(
    val htfAlignment: Int,         // Max 25
    val structureQuality: Int,     // Max 20
    val liquidityConfirmation: Int,// Max 20
    val entryZoneTrigger: Int,     // Max 15
    val riskRewardStop: Int,       // Max 10
    val fundamentalsSession: Int   // Max 10
) {
    val totalScore: Int
        get() = htfAlignment + structureQuality + liquidityConfirmation +
                entryZoneTrigger + riskRewardStop + fundamentalsSession

    val isScorePassing: Boolean get() = totalScore >= 85
}

data class SignalExplanation(
    val htfNarrative: String,          // A
    val currentMarketRegime: String,   // B
    val fundamentalDrivers: String,    // C
    val scheduledEvents: String,       // D
    val liquidityMap: String,          // E
    val structuralLevels: String,      // F
    val bullishScenario: String,       // G
    val bearishScenario: String,       // H
    val entryTrigger: String,          // I
    val invalidationCondition: String, // J
    val riskAssessment: String,        // K
    val engineVerdict: String          // L
)

data class ApexSignal(
    val id: String,
    val symbol: String,
    val strategyId: String = "APEX-SMC-V3.2",
    val direction: SignalDirection,
    val setupType: SetupType,
    val state: SignalState,
    val scoreBreakdown: ScoreBreakdown,
    val hardFilters: List<HardFilterCheck>,
    val allHardFiltersPassed: Boolean,
    val executionTimeframe: Timeframe = Timeframe.M5,
    val currentPrice: Double,
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit1: Double,
    val takeProfit2: Double,
    val takeProfit3: Double,
    val netRewardToRisk: Double,
    val suggestedRiskPercent: Double = 0.50, // 0.5% default conservative institutional risk
    val recommendedLots: Double,
    val invalidationPrice: Double,
    val expirationTimestamp: Long,
    val creationTimestamp: Long,
    val supportingTimeframes: List<Timeframe>,
    val conflictingTimeframes: List<Timeframe>,
    val spreadPipsAtSignal: Double,
    val explanation: SignalExplanation,
    val dataSource: String = "Institutional Aggregator L2 (Zero-Synthetics)"
) {
    val stopDistancePips: Double
        get() = Math.abs(entryPrice - stopLoss)

    val isExecutable: Boolean
        get() = state == SignalState.CONFIRMED || state == SignalState.ACTIVE
}
