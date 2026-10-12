package com.example.engine

import com.example.model.Instrument
import kotlin.math.abs
import kotlin.math.max

data class RiskCalculationResult(
    val accountBalance: Double,
    val riskPercent: Double,
    val monetaryRiskDollars: Double,
    val stopDistancePips: Double,
    val recommendedLotSize: Double,
    val estimatedCommissionDollars: Double,
    val estimatedSpreadCostDollars: Double,
    val totalModeledFrictionDollars: Double,
    val netPotentialLossDollars: Double,
    val netPotentialProfitTP1Dollars: Double,
    val netRewardToRisk: Double,
    val marginRequiredDollars: Double,
    val isRiskExceededWarning: Boolean,
    val warningMessage: String?
)

object RiskManagementEngine {

    const val DEFAULT_COMMISSION_PER_LOT = 7.0 // $7 round-turn per standard lot
    const val MAX_RECOMMENDED_RISK_PERCENT = 1.0 // 1% institutional hard cap
    const val MAX_CONSECUTIVE_LOSSES_BEFORE_COOLDOWN = 2

    /**
     * Calculates exact institutional lot size and friction costs based on contract specifications
     */
    fun calculatePositionSize(
        instrument: Instrument,
        accountBalance: Double,
        riskPercent: Double,
        entryPrice: Double,
        stopLoss: Double,
        takeProfit1: Double,
        leverage: Double = 30.0
    ): RiskCalculationResult {
        val monetaryRisk = accountBalance * (riskPercent / 100.0)
        val priceDistance = abs(entryPrice - stopLoss)
        val stopDistancePips = if (instrument.pipSize > 0) priceDistance / instrument.pipSize else 1.0

        // Monetary value of 1 pip for 1.0 standard lot
        val pipValueStandardLot = instrument.tickValue * 1.0

        val rawLots = if (stopDistancePips > 0 && pipValueStandardLot > 0) {
            monetaryRisk / (stopDistancePips * pipValueStandardLot)
        } else 0.01

        val lotSize = (rawLots * 100).toInt() / 100.0 // 2 decimal precision

        val commission = lotSize * DEFAULT_COMMISSION_PER_LOT
        val spreadCost = lotSize * (instrument.spreadPips * instrument.tickValue)
        val totalFriction = commission + spreadCost

        val targetDistance = abs(takeProfit1 - entryPrice)
        val targetDistancePips = if (instrument.pipSize > 0) targetDistance / instrument.pipSize else 1.0
        val grossProfit = targetDistancePips * pipValueStandardLot * lotSize
        val netProfit = max(0.0, grossProfit - totalFriction)
        val netLoss = monetaryRisk + totalFriction

        val netR = if (netLoss > 0) netProfit / netLoss else 0.0

        val notionalValue = lotSize * instrument.contractSize * entryPrice
        val marginRequired = if (leverage > 0) notionalValue / leverage else 0.0

        val isExceeded = riskPercent > MAX_RECOMMENDED_RISK_PERCENT
        val warning = if (isExceeded) {
            "RISK WARNING: Configured risk (${riskPercent}%) exceeds institutional 1.0% limit. Extreme drawdown hazard."
        } else null

        return RiskCalculationResult(
            accountBalance = accountBalance,
            riskPercent = riskPercent,
            monetaryRiskDollars = monetaryRisk,
            stopDistancePips = stopDistancePips,
            recommendedLotSize = max(0.01, lotSize),
            estimatedCommissionDollars = commission,
            estimatedSpreadCostDollars = spreadCost,
            totalModeledFrictionDollars = totalFriction,
            netPotentialLossDollars = netLoss,
            netPotentialProfitTP1Dollars = netProfit,
            netRewardToRisk = netR,
            marginRequiredDollars = marginRequired,
            isRiskExceededWarning = isExceeded,
            warningMessage = warning
        )
    }

    /**
     * Checks whether portfolio correlation limits are breached
     * E.g. Trading EURUSD Long, GBPUSD Long, and AUDUSD Long simultaneously triples USD short exposure
     */
    fun checkCorrelatedExposure(activeSymbols: List<String>, candidateSymbol: String): String? {
        val usdCount = activeSymbols.count { it.contains("USD") }
        if (candidateSymbol.contains("USD") && usdCount >= 2) {
            return "PORTFOLIO CORRELATION CAUTION: You already hold $usdCount active USD-denominated positions. Adding $candidateSymbol compounds macroeconomic dollar concentration."
        }
        return null
    }
}
