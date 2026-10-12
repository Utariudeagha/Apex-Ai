package com.example

import com.example.data.DefaultInstruments
import com.example.engine.MarketDataEngine
import com.example.engine.PriceActionEngine
import com.example.engine.RiskManagementEngine
import com.example.engine.SignalScoringEngine
import com.example.engine.TopDownAnalysisEngine
import com.example.model.Candle
import com.example.model.EconomicEvent
import com.example.model.NewsImpact
import com.example.model.SetupType
import com.example.model.SignalDirection
import com.example.model.SignalState
import com.example.model.SwingType
import com.example.model.Timeframe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApexSniperEngineTest {

    @Test
    fun test_swing_points_detection() {
        val candles = listOf(
            Candle(1000, 10.0, 12.0, 9.0, 11.0, 100.0),
            Candle(2000, 11.0, 13.0, 10.0, 12.0, 100.0),
            Candle(3000, 12.0, 18.0, 11.0, 17.0, 100.0), // Swing High at index 2
            Candle(4000, 17.0, 14.0, 10.0, 12.0, 100.0),
            Candle(5000, 12.0, 13.0, 8.0, 9.0, 100.0),   // Swing Low at index 4
            Candle(6000, 9.0, 15.0, 9.0, 14.0, 100.0),
            Candle(7000, 14.0, 16.0, 13.0, 15.0, 100.0)
        )

        val swings = PriceActionEngine.detectSwingPoints(candles, window = 1)
        assertTrue("Should detect at least one swing high and low", swings.isNotEmpty())
        assertTrue("Should detect swing high", swings.any { it.type == SwingType.HIGH && it.price == 18.0 })
        assertTrue("Should detect swing low", swings.any { it.type == SwingType.LOW && it.price == 8.0 })
    }

    @Test
    fun test_fair_value_gap_detection() {
        // Bullish 3-candle FVG: c3.low > c1.high with displacement in c2
        val candles = listOf(
            Candle(1000, 100.0, 102.0, 99.0, 101.0, 200.0),  // c1: high = 102.0
            Candle(2000, 101.0, 110.0, 101.0, 109.0, 800.0), // c2: displacement up
            Candle(3000, 109.0, 112.0, 105.0, 111.0, 300.0)  // c3: low = 105.0 (gap between 102 and 105)
        )

        val fvgs = PriceActionEngine.detectFairValueGaps(candles)
        assertEquals(1, fvgs.size)
        val fvg = fvgs.first()
        assertTrue(fvg.isBullish)
        assertEquals(105.0, fvg.upperBoundary, 0.001)
        assertEquals(102.0, fvg.lowerBoundary, 0.001)
    }

    @Test
    fun test_liquidity_sweep_detection() {
        // High is established at 150.0. Later candle penetrates to 152.0 but closes at 148.0 (sweep & reject)
        val swings = listOf(
            com.example.model.SwingPoint(1, 1000, 150.0, SwingType.HIGH, true)
        )
        val candles = listOf(
            Candle(500, 140.0, 142.0, 139.0, 141.0, 100.0),
            Candle(1000, 141.0, 150.0, 140.0, 149.0, 100.0),
            Candle(1500, 149.0, 152.0, 147.0, 148.0, 400.0) // Penetrated 150.0 to 152.0, closed at 148.0
        )

        val sweeps = PriceActionEngine.detectLiquiditySweeps(candles, swings)
        assertEquals(1, sweeps.size)
        val sweep = sweeps.first()
        assertTrue(sweep.isSweepOfHigh)
        assertTrue(sweep.rejectionConfirmed)
        assertEquals(152.0, sweep.sweepExtremePrice, 0.001)
    }

    @Test
    fun test_position_sizing_risk_calculation() {
        val eurusd = DefaultInstruments.list.first { it.symbol == "EURUSD" }
        val accountBalance = 100000.0
        val riskPercent = 0.50 // 0.5% = $500 risk
        val entry = 1.09340
        val stopLoss = 1.09140 // 20 pips stop
        val tp1 = 1.09900      // 56 pips target = 2.8R nominal

        val result = RiskManagementEngine.calculatePositionSize(
            instrument = eurusd,
            accountBalance = accountBalance,
            riskPercent = riskPercent,
            entryPrice = entry,
            stopLoss = stopLoss,
            takeProfit1 = tp1
        )

        assertEquals(500.0, result.monetaryRiskDollars, 0.01)
        assertEquals(20.0, result.stopDistancePips, 0.01)
        // 1 pip on 1 lot EURUSD = $10. 20 pips = $200 per lot. $500 / $200 = 2.5 lots
        assertEquals(2.5, result.recommendedLotSize, 0.01)
        assertTrue("Net R should be positive and >= 2.5R", result.netRewardToRisk >= 2.5)
        assertFalse(result.isRiskExceededWarning)
    }

    @Test
    fun test_hard_filter_rejection_on_high_impact_news_blackout() {
        val eurusd = DefaultInstruments.list.first { it.symbol == "EURUSD" }
        val mockAnalyses = Timeframe.values().map { tf ->
            com.example.model.TimeframeAnalysis(
                timeframe = tf,
                bias = com.example.model.DirectionalBias.BULLISH,
                regime = com.example.model.MarketRegime.TRENDING_EXPANSION,
                recentSwingHigh = 1.0980,
                recentSwingLow = 1.0900,
                lastStructuralBreak = null,
                activeFVGs = emptyList(),
                activeOrderBlocks = emptyList(),
                liquiditySweeps = emptyList(),
                candleCloseConfirmed = true,
                agreesWithTrade = true,
                evidenceExplanation = "Bullish structure"
            )
        }

        // Active high impact news blackout for USD
        val activeNews = listOf(
            EconomicEvent(
                id = "N-1",
                title = "US CPI",
                currency = "USD",
                impact = NewsImpact.HIGH,
                scheduledTime = System.currentTimeMillis() + 60000,
                previous = "3.2%",
                forecast = "3.1%",
                actual = null,
                surprise = null,
                isBlackoutActive = true, // BLACKOUT ACTIVE
                relevanceSummary = "CPI inflation release"
            )
        )

        val signal = SignalScoringEngine.evaluateSetup(
            instrument = eurusd,
            direction = SignalDirection.LONG,
            setupType = SetupType.ORDER_BLOCK_MITIGATION,
            entryPrice = 1.09340,
            stopLoss = 1.09190,
            takeProfit1 = 1.09640,
            takeProfit2 = 1.09840,
            takeProfit3 = 1.10040,
            timeframeAnalyses = mockAnalyses,
            upcomingNews = activeNews,
            currentAccountRiskPercent = 0.50
        )

        // Must fail the news hard filter and be marked NO_TRADE or rejected
        assertFalse("All hard filters should not pass during blackout", signal.allHardFiltersPassed)
        val newsCheck = signal.hardFilters.first { it.filterName.contains("News") }
        assertFalse("News check must be marked failed", newsCheck.passed)
        assertTrue(signal.state == SignalState.NO_TRADE || signal.state == SignalState.INVALIDATED)
    }

    @Test
    fun test_hard_filter_rejection_on_substandard_reward_to_risk() {
        val eurusd = DefaultInstruments.list.first { it.symbol == "EURUSD" }
        val mockAnalyses = Timeframe.values().map { tf ->
            com.example.model.TimeframeAnalysis(
                timeframe = tf,
                bias = com.example.model.DirectionalBias.BULLISH,
                regime = com.example.model.MarketRegime.TRENDING_EXPANSION,
                recentSwingHigh = 1.0980,
                recentSwingLow = 1.0900,
                lastStructuralBreak = null,
                activeFVGs = emptyList(),
                activeOrderBlocks = emptyList(),
                liquiditySweeps = emptyList(),
                candleCloseConfirmed = true,
                agreesWithTrade = true,
                evidenceExplanation = "Bullish structure"
            )
        }

        // Target is only 10 pips for 20 pips risk -> 0.5R (fails minimum 2.0R filter)
        val signal = SignalScoringEngine.evaluateSetup(
            instrument = eurusd,
            direction = SignalDirection.LONG,
            setupType = SetupType.FVG_CONTINUATION,
            entryPrice = 1.09340,
            stopLoss = 1.09140, // 20 pips risk
            takeProfit1 = 1.09440, // 10 pips reward = 0.5R
            takeProfit2 = 1.09540,
            takeProfit3 = 1.09640,
            timeframeAnalyses = mockAnalyses,
            upcomingNews = emptyList(),
            currentAccountRiskPercent = 0.50
        )

        assertFalse("Sub-2.0R trade must fail hard filters", signal.allHardFiltersPassed)
        val rrCheck = signal.hardFilters.first { it.filterName.contains("Reward-to-Risk", ignoreCase = true) || it.filterName.contains("Risk-to-Reward", ignoreCase = true) }
        assertFalse(rrCheck.passed)
    }
}
