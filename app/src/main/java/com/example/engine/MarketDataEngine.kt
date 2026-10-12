package com.example.engine

import com.example.data.DefaultInstruments
import com.example.model.Candle
import com.example.model.EconomicEvent
import com.example.model.Instrument
import com.example.model.NewsImpact
import com.example.model.Timeframe
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Random
import kotlin.math.abs
import kotlin.math.sin

object MarketDataEngine {

    private val random = Random(42)

    /**
     * Generates a realistic historical candle series for a given instrument and timeframe
     */
    fun generateHistoricalCandles(instrument: Instrument, timeframe: Timeframe, count: Int = 80): List<Candle> {
        val candles = mutableListOf<Candle>()
        val basePrice = instrument.lastPrice
        val tfMinutes = timeframe.minutes
        val intervalMillis = tfMinutes * 60 * 1000L
        val now = System.currentTimeMillis()
        var currentClose = basePrice * 0.96 // Start slightly lower to create historical wave

        val volatility = when {
            instrument.symbol.startsWith("BTC") -> 0.012
            instrument.symbol.startsWith("ETH") -> 0.015
            instrument.symbol.startsWith("XAU") -> 0.006
            instrument.symbol.startsWith("US100") -> 0.005
            else -> 0.0018 // Forex pairs
        }

        for (i in 0 until count) {
            val candleTime = now - ((count - i) * intervalMillis)
            val trendComponent = sin(i * 0.15) * (basePrice * volatility * 1.5)
            val randomFactor = (random.nextGaussian() * volatility * basePrice)

            val open = currentClose
            val close = open + (trendComponent * 0.1) + (randomFactor * 0.4)
            val high = maxOf(open, close) + abs(randomFactor * 0.6)
            val low = minOf(open, close) - abs(randomFactor * 0.6)
            val volume = abs(random.nextGaussian() * 1500.0) + 500.0

            currentClose = close
            candles.add(Candle(candleTime, open, high, low, close, volume))
        }

        // Align the very last candle's close with the instrument's lastPrice
        if (candles.isNotEmpty()) {
            val last = candles.last()
            candles[candles.size - 1] = last.copy(close = instrument.lastPrice)
        }

        return candles
    }

    /**
     * Real-time tick stream emitting updated quotes and spread fluctuations
     */
    fun streamLiveTicks(instruments: List<Instrument>): Flow<List<Instrument>> = flow {
        var currentList = instruments.toMutableList()
        while (true) {
            delay(1500)
            val updated = currentList.map { inst ->
                val tickNoise = (random.nextGaussian() * inst.pipSize * 0.8)
                val newPrice = (inst.lastPrice + tickNoise).coerceAtLeast(0.0001)
                val spreadNoise = (random.nextGaussian() * 0.1).coerceIn(-0.2, 0.3)
                val newSpread = (inst.spreadPips + spreadNoise).coerceIn(0.4, 5.0)
                val halfSpread = (newSpread * inst.pipSize) / 2.0

                inst.copy(
                    lastPrice = newPrice,
                    bidPrice = newPrice - halfSpread,
                    askPrice = newPrice + halfSpread,
                    spreadPips = Math.round(newSpread * 10.0) / 10.0,
                    lastUpdated = System.currentTimeMillis()
                )
            }
            currentList = updated.toMutableList()
            emit(updated)
        }
    }

    /**
     * Scheduled macroeconomic events calendar
     */
    val upcomingEconomicEvents: List<EconomicEvent> = listOf(
        EconomicEvent(
            id = "EV-1",
            title = "US Core CPI YoY",
            currency = "USD",
            impact = NewsImpact.HIGH,
            scheduledTime = System.currentTimeMillis() + (2 * 3600 * 1000), // in 2 hours
            previous = "3.2%",
            forecast = "3.1%",
            actual = null,
            surprise = null,
            isBlackoutActive = false,
            relevanceSummary = "Direct impact on Fed terminal rate expectations and US Treasury yields."
        ),
        EconomicEvent(
            id = "EV-2",
            title = "ECB Monetary Policy Statement",
            currency = "EUR",
            impact = NewsImpact.HIGH,
            scheduledTime = System.currentTimeMillis() + (18 * 3600 * 1000),
            previous = "3.65%",
            forecast = "3.40%",
            actual = null,
            surprise = null,
            isBlackoutActive = false,
            relevanceSummary = "Potential 25 bps rate cut; impact on EURUSD dealing range."
        ),
        EconomicEvent(
            id = "EV-3",
            title = "US Non-Farm Payrolls (NFP)",
            currency = "USD",
            impact = NewsImpact.HIGH,
            scheduledTime = System.currentTimeMillis() + (48 * 3600 * 1000),
            previous = "142K",
            forecast = "150K",
            actual = null,
            surprise = null,
            isBlackoutActive = false,
            relevanceSummary = "Primary labor market indicator driving Federal Reserve dual mandate."
        ),
        EconomicEvent(
            id = "EV-4",
            title = "UK Retail Sales MoM",
            currency = "GBP",
            impact = NewsImpact.MEDIUM,
            scheduledTime = System.currentTimeMillis() + (5 * 3600 * 1000),
            previous = "0.6%",
            forecast = "-0.2%",
            actual = null,
            surprise = null,
            isBlackoutActive = false,
            relevanceSummary = "UK consumer resilience metric."
        ),
        EconomicEvent(
            id = "EV-5",
            title = "OPEC+ Ministerial Meeting",
            currency = "USD",
            impact = NewsImpact.HIGH,
            scheduledTime = System.currentTimeMillis() + (72 * 3600 * 1000),
            previous = "Quota Extension",
            forecast = "Gradual Phase-in",
            actual = null,
            surprise = null,
            isBlackoutActive = false,
            relevanceSummary = "Crucial supply quota decisions for USOIL and UKOIL crude benchmarks."
        )
    )
}
