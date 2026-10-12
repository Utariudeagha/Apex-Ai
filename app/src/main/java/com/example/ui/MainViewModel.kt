package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DefaultAcademyData
import com.example.data.DefaultInstruments
import com.example.data.local.AppDatabase
import com.example.engine.BacktestEngine
import com.example.engine.MarketDataEngine
import com.example.engine.PriceActionEngine
import com.example.engine.RiskCalculationResult
import com.example.engine.RiskManagementEngine
import com.example.engine.SignalScoringEngine
import com.example.engine.TopDownAnalysisEngine
import com.example.model.AcademyLesson
import com.example.model.ApexSignal
import com.example.model.AssetCategory
import com.example.model.BacktestReport
import com.example.model.Candle
import com.example.model.Instrument
import com.example.model.SetupType
import com.example.model.SignalDirection
import com.example.model.SignalState
import com.example.model.Timeframe
import com.example.model.TimeframeAnalysis
import com.example.model.TradeJournalEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen(val label: String) {
    DASHBOARD("Terminal"),
    SCANNER("Scanner"),
    CHART("Charts"),
    SIGNAL_DETAILS("Signal"),
    RISK_CALC("Risk Engine"),
    JOURNAL("Journal"),
    BACKTEST("Backtest"),
    ACADEMY("Academy"),
    SETTINGS("Settings")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val tradeDao = db.tradeDao()

    // Screen Navigation
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Instruments
    private val _instruments = MutableStateFlow(DefaultInstruments.list)
    val instruments: StateFlow<List<Instrument>> = _instruments.asStateFlow()

    // Selected Instrument & Timeframe for Charting
    private val _selectedInstrument = MutableStateFlow(DefaultInstruments.list[0]) // XAUUSD
    val selectedInstrument: StateFlow<Instrument> = _selectedInstrument.asStateFlow()

    private val _selectedTimeframe = MutableStateFlow(Timeframe.H1)
    val selectedTimeframe: StateFlow<Timeframe> = _selectedTimeframe.asStateFlow()

    // Candle series for the selected instrument and timeframe
    private val _chartCandles = MutableStateFlow<List<Candle>>(emptyList())
    val chartCandles: StateFlow<List<Candle>> = _chartCandles.asStateFlow()

    // Multi-timeframe cache for selected instrument
    private val _multiTfAnalyses = MutableStateFlow<List<TimeframeAnalysis>>(emptyList())
    val multiTfAnalyses: StateFlow<List<TimeframeAnalysis>> = _multiTfAnalyses.asStateFlow()

    // Active & Qualifying Signals
    private val _signals = MutableStateFlow<List<ApexSignal>>(emptyList())
    val signals: StateFlow<List<ApexSignal>> = _signals.asStateFlow()

    private val _selectedSignal = MutableStateFlow<ApexSignal?>(null)
    val selectedSignal: StateFlow<ApexSignal?> = _selectedSignal.asStateFlow()

    // Risk Calculator State
    private val _accountBalance = MutableStateFlow(100000.0)
    val accountBalance: StateFlow<Double> = _accountBalance.asStateFlow()

    private val _riskPercent = MutableStateFlow(0.50)
    val riskPercent: StateFlow<Double> = _riskPercent.asStateFlow()

    private val _riskCalcResult = MutableStateFlow<RiskCalculationResult?>(null)
    val riskCalcResult: StateFlow<RiskCalculationResult?> = _riskCalcResult.asStateFlow()

    // Backtest Report
    private val _backtestReport = MutableStateFlow<BacktestReport?>(null)
    val backtestReport: StateFlow<BacktestReport?> = _backtestReport.asStateFlow()
    private val _isBacktesting = MutableStateFlow(false)
    val isBacktesting: StateFlow<Boolean> = _isBacktesting.asStateFlow()

    // Academy Lessons & Progress
    val academyLessons: List<AcademyLesson> = DefaultAcademyData.lessons
    private val _completedQuizIds = MutableStateFlow<Set<String>>(emptySet())
    val completedQuizIds: StateFlow<Set<String>> = _completedQuizIds.asStateFlow()

    // Persistent Journal Entries
    val journalEntries: StateFlow<List<TradeJournalEntry>> = tradeDao.getAllTrades()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Consecutive Losses Tracker (Safeguard)
    private val _consecutiveLosses = MutableStateFlow(0)
    val consecutiveLosses: StateFlow<Int> = _consecutiveLosses.asStateFlow()

    init {
        // Load initial chart candles and run initial market scan
        loadCandlesForSelected()
        generateSignals()

        // Start live market data tick stream
        viewModelScope.launch {
            MarketDataEngine.streamLiveTicks(DefaultInstruments.list).collect { updatedList ->
                _instruments.value = updatedList
                // Keep selected instrument updated with live quote
                val currentSel = _selectedInstrument.value
                val matched = updatedList.find { it.symbol == currentSel.symbol }
                if (matched != null) {
                    _selectedInstrument.value = matched
                }
            }
        }

        // Initialize risk calculation with defaults
        recalculateRisk()
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun selectInstrument(instrument: Instrument) {
        _selectedInstrument.value = instrument
        loadCandlesForSelected()
    }

    fun selectTimeframe(timeframe: Timeframe) {
        _selectedTimeframe.value = timeframe
        loadCandlesForSelected()
    }

    fun selectSignal(signal: ApexSignal) {
        _selectedSignal.value = signal
        val inst = _instruments.value.find { it.symbol == signal.symbol }
        if (inst != null) {
            _selectedInstrument.value = inst
            loadCandlesForSelected()
        }
        _currentScreen.value = AppScreen.SIGNAL_DETAILS
    }

    private fun loadCandlesForSelected() {
        viewModelScope.launch {
            val inst = _selectedInstrument.value
            val tf = _selectedTimeframe.value
            val candles = MarketDataEngine.generateHistoricalCandles(inst, tf, count = 65)
            _chartCandles.value = candles

            // Generate multi-timeframe analyses
            val multiTfMap = Timeframe.values().associateWith {
                MarketDataEngine.generateHistoricalCandles(inst, it, count = 50)
            }
            val matrix = TopDownAnalysisEngine.performFullTopDownMatrix(multiTfMap, proposedDirectionIsLong = true)
            _multiTfAnalyses.value = matrix
        }
    }

    fun generateSignals() {
        viewModelScope.launch {
            val generated = mutableListOf<ApexSignal>()

            // Evaluate setups across the required catalog
            _instruments.value.forEach { inst ->
                val h1Candles = MarketDataEngine.generateHistoricalCandles(inst, Timeframe.H1, 50)
                val multiTfMap = Timeframe.values().associateWith {
                    MarketDataEngine.generateHistoricalCandles(inst, it, 40)
                }

                val swings = PriceActionEngine.detectSwingPoints(h1Candles)
                val isBullishBias = (swings.lastOrNull { it.type == com.example.model.SwingType.HIGH }?.price ?: inst.lastPrice) >= inst.lastPrice

                val direction = if (isBullishBias) SignalDirection.LONG else SignalDirection.SHORT
                val matrix = TopDownAnalysisEngine.performFullTopDownMatrix(multiTfMap, proposedDirectionIsLong = isBullishBias)

                val stopDistance = when (inst.symbol) {
                    "XAUUSD" -> 8.00 // $8.00 stop on Gold ($4,220 -> $4,212)
                    "XAGUSD" -> 0.50 // $0.50 stop on Silver
                    "US100" -> 55.00 // 55 pts on Nasdaq
                    "US500" -> 12.00 // 12 pts on S&P 500
                    "US30" -> 80.00 // 80 pts on Dow
                    "GER40" -> 45.00 // 45 pts on DAX
                    "UK100" -> 25.00 // 25 pts on FTSE
                    "USOIL", "UKOIL" -> 1.10 // $1.10 on Crude Oil
                    "BTCUSD" -> 600.00 // $600 on Bitcoin ($84,106 -> $83,506)
                    "ETHUSD" -> 28.00 // $28 on Ethereum
                    else -> if (inst.quoteCurrency == "JPY") 0.18 else 0.0015 // 15-18 pips on FX
                }

                val entryPrice = inst.lastPrice
                val stopLoss = if (direction == SignalDirection.LONG) entryPrice - stopDistance else entryPrice + stopDistance
                val tp1 = if (direction == SignalDirection.LONG) entryPrice + (stopDistance * 2.8) else entryPrice - (stopDistance * 2.8)
                val tp2 = if (direction == SignalDirection.LONG) entryPrice + (stopDistance * 4.0) else entryPrice - (stopDistance * 4.0)
                val tp3 = if (direction == SignalDirection.LONG) entryPrice + (stopDistance * 5.5) else entryPrice - (stopDistance * 5.5)

                val setupType = when (inst.symbol) {
                    "XAUUSD" -> SetupType.LIQUIDITY_SWEEP_REVERSAL
                    "US100" -> SetupType.FVG_CONTINUATION
                    "EURUSD" -> SetupType.ORDER_BLOCK_MITIGATION
                    "GBPUSD" -> SetupType.CHOCH_STRUCTURAL_SHIFT
                    "BTCUSD" -> SetupType.BREAKER_RETEST
                    else -> SetupType.FVG_CONTINUATION
                }

                val sig = SignalScoringEngine.evaluateSetup(
                    instrument = inst,
                    direction = direction,
                    setupType = setupType,
                    entryPrice = entryPrice,
                    stopLoss = stopLoss,
                    takeProfit1 = tp1,
                    takeProfit2 = tp2,
                    takeProfit3 = tp3,
                    timeframeAnalyses = matrix,
                    upcomingNews = MarketDataEngine.upcomingEconomicEvents,
                    currentAccountRiskPercent = _riskPercent.value
                )
                generated.add(sig)
            }

            _signals.value = generated
            // Set initial selected signal if none
            if (_selectedSignal.value == null && generated.isNotEmpty()) {
                _selectedSignal.value = generated.firstOrNull { it.state == SignalState.CONFIRMED } ?: generated.first()
            }
        }
    }

    // Risk Calculation
    fun updateAccountBalance(balance: Double) {
        _accountBalance.value = balance
        recalculateRisk()
    }

    fun updateRiskPercent(percent: Double) {
        _riskPercent.value = percent
        recalculateRisk()
    }

    private fun recalculateRisk() {
        val inst = _selectedInstrument.value
        val sig = _selectedSignal.value
        val entry = sig?.entryPrice ?: inst.lastPrice
        val defaultStopDist = when (inst.symbol) {
            "XAUUSD" -> 8.00
            "US100" -> 55.00
            "BTCUSD" -> 600.00
            else -> if (inst.quoteCurrency == "JPY") 0.18 else 0.0015
        }
        val sl = sig?.stopLoss ?: (entry - defaultStopDist)
        val tp = sig?.takeProfit1 ?: (entry + (defaultStopDist * 2.8))

        _riskCalcResult.value = RiskManagementEngine.calculatePositionSize(
            instrument = inst,
            accountBalance = _accountBalance.value,
            riskPercent = _riskPercent.value,
            entryPrice = entry,
            stopLoss = sl,
            takeProfit1 = tp
        )
    }

    // Backtesting
    fun runBacktestForSelected() {
        viewModelScope.launch {
            _isBacktesting.value = true
            val inst = _selectedInstrument.value
            val candles = MarketDataEngine.generateHistoricalCandles(inst, Timeframe.H1, count = 120)
            val report = BacktestEngine.runBacktest(inst, candles, riskRewardTarget = 2.4, riskPerTradePercent = _riskPercent.value)
            _backtestReport.value = report
            _isBacktesting.value = false
        }
    }

    // Academy Quiz Completed
    fun markQuizCompleted(quizId: String) {
        _completedQuizIds.value = _completedQuizIds.value + quizId
    }

    // Trade Logging to Room
    fun logTrade(entry: TradeJournalEntry) {
        viewModelScope.launch {
            tradeDao.insertTrade(entry)
            if (entry.outcome == "LOSS") {
                _consecutiveLosses.value += 1
            } else if (entry.outcome == "WIN") {
                _consecutiveLosses.value = 0
            }
        }
    }

    fun deleteTrade(entry: TradeJournalEntry) {
        viewModelScope.launch {
            tradeDao.deleteTrade(entry)
        }
    }
}
