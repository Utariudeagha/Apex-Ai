package com.example.engine

import com.example.data.DefaultInstruments
import com.example.model.Candle
import com.example.model.EconomicEvent
import com.example.model.Instrument
import com.example.model.NewsImpact
import com.example.model.Timeframe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Random
import kotlin.math.abs
import kotlin.math.sin

object MarketDataEngine {

    private val random = Random(System.currentTimeMillis())

    /**
     * Generates a realistic historical candle series for a given instrument and timeframe,
     * anchored directly around its current real market price.
     */
    fun generateHistoricalCandles(instrument: Instrument, timeframe: Timeframe, count: Int = 80): List<Candle> {
        val candles = mutableListOf<Candle>()
        val basePrice = instrument.lastPrice
        val tfMinutes = timeframe.minutes
        val intervalMillis = tfMinutes * 60 * 1000L
        val now = System.currentTimeMillis()

        // Calibrate step volatility to real instrument price dynamics
        val volatility = when {
            instrument.symbol.startsWith("BTC") -> 0.0035
            instrument.symbol.startsWith("ETH") -> 0.0045
            instrument.symbol.startsWith("XAU") -> 0.0018 // ~7.50 on 4220 Gold
            instrument.symbol.startsWith("US100") -> 0.0020 // ~60 pts on 31236 Nasdaq
            else -> 0.0008 // ~9 pips on Forex
        }

        // Generate series backwards so the latest candle closes precisely at basePrice
        var walkPrice = basePrice
        val tempCandles = mutableListOf<Candle>()

        for (i in 0 until count) {
            val candleTime = now - (i * intervalMillis)
            val trendFactor = sin(i * 0.18) * (basePrice * volatility * 0.6)
            val noise = (random.nextGaussian() * volatility * basePrice * 0.5)

            val close = walkPrice
            val open = walkPrice - trendFactor - noise
            val high = maxOf(open, close) + abs(random.nextGaussian() * volatility * basePrice * 0.4)
            val low = minOf(open, close) - abs(random.nextGaussian() * volatility * basePrice * 0.4)
            val volume = abs(random.nextGaussian() * 2500.0) + 800.0

            tempCandles.add(Candle(candleTime, open, high, low, close, volume))
            walkPrice = open
        }

        // Reverse to chronological order (oldest to newest)
        tempCandles.reverse()
        candles.addAll(tempCandles)

        // Ensure the very last candle close strictly equals instrument's real price
        if (candles.isNotEmpty()) {
            val last = candles.last()
            candles[candles.size - 1] = last.copy(
                close = instrument.lastPrice,
                high = maxOf(last.high, instrument.lastPrice),
                low = minOf(last.low, instrument.lastPrice)
            )
        }

        return candles
    }

    /**
     * Attempts to fetch real live quote from external market API
     */
    suspend fun fetchLivePrice(symbol: String): Double? = withContext(Dispatchers.IO) {
        try {
            when (symbol) {
                "BTCUSD" -> {
                    val res = fetchUrl("https://api.binance.com/api/v3/ticker/price?symbol=BTCUSDT")
                    if (res != null) JSONObject(res).optDouble("price", -1.0).takeIf { it > 0 } else null
                }
                "ETHUSD" -> {
                    val res = fetchUrl("https://api.binance.com/api/v3/ticker/price?symbol=ETHUSDT")
                    if (res != null) JSONObject(res).optDouble("price", -1.0).takeIf { it > 0 } else null
                }
                "XAUUSD" -> {
                    val res = fetchUrlWithHeader("https://query1.finance.yahoo.com/v8/finance/chart/GC=F")
                    parseYahooPrice(res)
                }
                "US100" -> {
                    val res = fetchUrlWithHeader("https://query1.finance.yahoo.com/v8/finance/chart/NQ=F")
                    parseYahooPrice(res)
                }
                "EURUSD" -> {
                    val res = fetchUrlWithHeader("https://query1.finance.yahoo.com/v8/finance/chart/EURUSD=X")
                    parseYahooPrice(res)
                }
                "GBPUSD" -> {
                    val res = fetchUrlWithHeader("https://query1.finance.yahoo.com/v8/finance/chart/GBPUSD=X")
                    parseYahooPrice(res)
                }
                "USDJPY" -> {
                    val res = fetchUrlWithHeader("https://query1.finance.yahoo.com/v8/finance/chart/USDJPY=X")
                    parseYahooPrice(res)
                }
                "USOIL" -> {
                    val res = fetchUrlWithHeader("https://query1.finance.yahoo.com/v8/finance/chart/CL=F")
                    parseYahooPrice(res)
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun parseYahooPrice(jsonStr: String?): Double? {
        if (jsonStr == null) return null
        return try {
            val root = JSONObject(jsonStr)
            val chart = root.getJSONObject("chart")
            val resultArr = chart.getJSONArray("result")
            val meta = resultArr.getJSONObject(0).getJSONObject("meta")
            meta.optDouble("regularMarketPrice", -1.0).takeIf { it > 0 }
        } catch (e: Exception) {
            null
        }
    }

    private fun fetchUrl(urlString: String): String? {
        return try {
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            conn.requestMethod = "GET"
            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                reader.close()
                sb.toString()
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun fetchUrlWithHeader(urlString: String): String? {
        return try {
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", "Mozilla/5.0")
            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                reader.close()
                sb.toString()
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Real-time tick stream: fetches real network prices periodically and applies micro-tick updates
     */
    fun streamLiveTicks(instruments: List<Instrument>): Flow<List<Instrument>> = flow {
        var currentList = instruments.toMutableList()
        var cycleCounter = 0

        while (true) {
            delay(1500)
            cycleCounter++

            // Every 10 cycles (~15s), query live market APIs for key benchmarks
            val livePriceMap = mutableMapOf<String, Double>()
            if (cycleCounter % 10 == 0) {
                listOf("BTCUSD", "ETHUSD", "XAUUSD", "US100", "EURUSD").forEach { sym ->
                    val liveP = fetchLivePrice(sym)
                    if (liveP != null && liveP > 0) {
                        livePriceMap[sym] = liveP
                    }
                }
            }

            val updated = currentList.map { inst ->
                val realP = livePriceMap[inst.symbol]
                val base = realP ?: inst.lastPrice
                val tickStep = when {
                    inst.symbol.startsWith("BTC") -> 2.5
                    inst.symbol.startsWith("XAU") -> 0.25
                    inst.symbol.startsWith("US100") -> 1.50
                    else -> inst.pipSize * 0.8
                }
                val tickNoise = (random.nextGaussian() * tickStep)
                val newPrice = (base + tickNoise).coerceAtLeast(0.0001)
                val halfSpread = (inst.spreadPips * inst.pipSize) / 2.0

                inst.copy(
                    lastPrice = newPrice,
                    bidPrice = newPrice - halfSpread,
                    askPrice = newPrice + halfSpread,
                    lastUpdated = System.currentTimeMillis()
                )
            }
            currentList = updated.toMutableList()
            emit(updated)
        }
    }

    val upcomingEconomicEvents: List<EconomicEvent> = listOf(
        EconomicEvent(
            id = "EV-1",
            title = "US Core CPI YoY",
            currency = "USD",
            impact = NewsImpact.HIGH,
            scheduledTime = System.currentTimeMillis() + (4 * 3600 * 1000),
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
        )
    )
}
