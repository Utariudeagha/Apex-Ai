package com.example.data

import com.example.model.AcademyLesson
import com.example.model.QuizQuestion

object DefaultAcademyData {
    val lessons: List<AcademyLesson> = listOf(
        AcademyLesson(
            id = "L1-1",
            levelNumber = 1,
            levelTitle = "Level 1: Foundations",
            title = "Market Microstructure, Bid/Ask & True Spread",
            subtitle = "The mathematical basis of trade execution and hidden frictions",
            coreConcept = "Every order crosses the spread. Retail traders pay the ask when buying and receive the bid when selling. In high-spread conditions, expected value collapses.",
            detailedText = "Understanding order matching is foundational. Institutional dealers quote a two-way price. When liquidity thins during session transitions or news releases, the spread expands exponentially. A strategy with a 2-pip stop and 1.8-pip spread incurs an immediate 90% adverse friction hurdle before the trade begins.",
            institutionalKeyTakeaways = listOf(
                "Ask price is where buyers enter long; Bid price is where sellers enter short.",
                "Real execution cost = Spread + Broker Commission + Slippage latency.",
                "Never execute high-frequency intraday trades during spread-widening rollover hours (5:00 PM EST)."
            ),
            chartAnnotationExample = "Bid: 1.09334 | Ask: 1.09340 | Spread: 0.6 pips (Optimal Institutional Window)",
            quiz = QuizQuestion(
                id = "Q1-1",
                question = "If EURUSD Bid is 1.09330 and Ask is 1.09342, what price will your Buy Limit fill at if matched at current ask, and what is the spread?",
                options = listOf(
                    "Fills at 1.09330 with 0.6 pips spread",
                    "Fills at 1.09342 with 1.2 pips spread",
                    "Fills at 1.09336 with 0.0 pips spread",
                    "Fills at 1.09342 with 0.6 pips spread"
                ),
                correctIndex = 1,
                explanation = "Buy orders fill at the Ask (1.09342). The spread is Ask minus Bid: 1.09342 - 1.09330 = 0.00012, which equals 1.2 pips."
            )
        ),
        AcademyLesson(
            id = "L2-1",
            levelNumber = 2,
            levelTitle = "Level 2: Technical Analysis",
            title = "Market Structure: BOS vs CHoCH Dynamics",
            subtitle = "Distinguishing trend continuation from true institutional character shifts",
            coreConcept = "A Break of Structure (BOS) confirms continuation in the prevailing trend. A Change of Character (CHoCH) marks the failure of the last opposing swing pivot and warns of structural reversal.",
            detailedText = "In a bullish market, price forms Higher Highs (HH) and Higher Lows (HL). When price breaks above the most recent confirmed swing high with a full candle body close, a BOS is registered. Conversely, when price violates the most recent confirmed HL that produced the highest peak, a CHoCH occurs. A wick alone is not a confirmed break.",
            institutionalKeyTakeaways = listOf(
                "BOS requires a decisive body close beyond the prior swing extreme.",
                "CHoCH is the very first structural warning of trend exhaustion or reversal.",
                "Never trade a CHoCH against higher timeframe bias unless aligned with major HTF liquidity sweeps."
            ),
            chartAnnotationExample = "BOS: Price closed 12 pips above Swing High 1.3060. CHoCH: Decisive close below Swing Low 1.3015.",
            quiz = QuizQuestion(
                id = "Q2-1",
                question = "In an established uptrend, price wicks 3 pips below the previous Higher Low, but closes 10 pips above it. Has a confirmed CHoCH occurred?",
                options = listOf(
                    "Yes, any price penetration below a low constitutes a confirmed CHoCH",
                    "No, a wick without a body close is candidate liquidity absorption/sweep, not a structural break",
                    "Yes, wicks carry more weight than body closes in modern market structure",
                    "No, because CHoCH can only happen on monthly charts"
                ),
                correctIndex = 1,
                explanation = "Institutional structural rules require a decisive candle body close below the pivot to confirm structural invalidation. A wick below followed by a close back inside is a liquidity sweep, not a CHoCH."
            )
        ),
        AcademyLesson(
            id = "L3-1",
            levelNumber = 3,
            levelTitle = "Level 3: Advanced Price Action",
            title = "Liquidity Sweeps & Failed Auctions",
            subtitle = "How institutional order books absorb stop runs and trap retail momentum",
            coreConcept = "Large participants cannot enter multi-million dollar positions without counterparty liquidity. Equal highs and prior swing highs hold concentrated resting buy-stops that institutions use to sell into.",
            detailedText = "When price surges above equal highs or previous day highs, retail breakout traders enter long while short-seller stop losses (buy orders) are triggered. This flood of buy liquidity allows institutional desks to fill substantial short positions without slippage. Once liquidity is absorbed, price violently displaces back inside the range.",
            institutionalKeyTakeaways = listOf(
                "Equal highs/lows are liquidity magnets, not unbreakable support/resistance.",
                "A true sweep features penetration followed by immediate displacement back within range.",
                "We do not claim certainty about hidden broker books; we measure observable price rejection."
            ),
            chartAnnotationExample = "Sweep of Asia High: 2668.50 penetrated by 1.8 points, 5M candle closed sharply lower at 2664.20.",
            quiz = QuizQuestion(
                id = "Q3-1",
                question = "What primary price condition confirms a high-probability liquidity sweep setup?",
                options = listOf(
                    "Price touches a round number and RSI is above 70",
                    "Penetration beyond a key liquidity level followed by displacement close back inside and opposing structure shift",
                    "Three consecutive green candles breaking through resistance with high retail volume",
                    "A moving average crossover immediately following a news spike"
                ),
                correctIndex = 1,
                explanation = "A valid sweep requires price to penetrate the resting liquidity pool, fail to accept the higher price, and close decisively back inside the dealing range accompanied by displacement."
            )
        ),
        AcademyLesson(
            id = "L3-2",
            levelNumber = 3,
            levelTitle = "Level 3: Advanced Price Action",
            title = "Fair Value Gaps (FVG) & Order Block Mechanics",
            subtitle = "Three-candle imbalances and mitigation entry zones",
            coreConcept = "A Fair Value Gap represents price inefficiency where one side of the market overwhelmed the other. Candle 1 high and Candle 3 low leave an unfilled imbalance in Candle 2.",
            detailedText = "When institutions enter aggressively, the market moves too rapidly to offer two-sided liquidity. This creates a gap between Candle 1 and Candle 3 wicks. As price later returns to rebalance this inefficiency (consequent encroachment / 50% midpoint), it frequently offers an optimal risk-to-reward continuation trigger.",
            institutionalKeyTakeaways = listOf(
                "Bullish FVG: Space between Candle 1 High and Candle 3 Low during an up move.",
                "Bearish FVG: Space between Candle 1 Low and Candle 3 High during a down move.",
                "An Order Block is the last opposing candle prior to displacement that caused a structural break."
            ),
            chartAnnotationExample = "Bullish FVG: 20240 to 20255 on H1 chart. Consequent Encroachment at 20247.50.",
            quiz = QuizQuestion(
                id = "Q3-2",
                question = "How is a Bearish Fair Value Gap identified in a 3-candle sequence?",
                options = listOf(
                    "Candle 2 is a doji and Candle 3 has the highest high",
                    "There is a price gap between the Low of Candle 1 and the High of Candle 3 with Candle 2 being a displacement down candle",
                    "Candle 3 closes higher than Candle 1 open",
                    "Candle 1 and Candle 3 have identical highs"
                ),
                correctIndex = 1,
                explanation = "A Bearish FVG exists when Candle 1 low is strictly higher than Candle 3 high, creating a zone of price imbalance where only sell orders were matched during Candle 2."
            )
        ),
        AcademyLesson(
            id = "L4-1",
            levelNumber = 4,
            levelTitle = "Level 4: Fundamentals",
            title = "Macro Drivers, Real Yields & Economic Calendar Risks",
            subtitle = "Trading with the macroeconomic wind and managing news blackouts",
            coreConcept = "Technical setups fail when macro catalysts shift capital flows. Gold responds to real US yields; currencies respond to rate differentials and surprise metrics vs consensus.",
            detailedText = "Market prices reflect consensus forecasts prior to a release. The volatility catalyst is the 'Surprise' delta: Actual minus Forecast. When US CPI prints 0.3% above consensus, Treasury yields surge, the USD rallies, and Gold experiences rapid liquidation regardless of technical support.",
            institutionalKeyTakeaways = listOf(
                "Apex rules mandate a +/- 45-minute blackout around High-Impact news (NFP, CPI, FOMC, Rate decisions).",
                "Gold (XAUUSD) has an inverse relationship with 10-year US Real Yields and the US Dollar Index (DXY).",
                "Never hold tight stops across central bank rate decisions; spreads widen and slippage can exceed stops."
            ),
            chartAnnotationExample = "US CPI Surprise: +0.2% vs Consensus -> USD Index +45 ticks, XAUUSD -18 points.",
            quiz = QuizQuestion(
                id = "Q4-1",
                question = "Why does APEX SNIPER AI enforce a strict mandatory news blackout filter before high-impact events?",
                options = listOf(
                    "Because technical charts cease to update during news",
                    "Because institutional spreads widen severely, order books thin, and technical levels are easily overrun by macro repricing",
                    "Because brokers prohibit opening trades on weekdays",
                    "Because news always causes markets to move sideways without volatility"
                ),
                correctIndex = 1,
                explanation = "During high-impact releases, liquidity providers pull quotes, spreads widen exponentially, and slippage can cause massive losses that violate predefined risk management rules."
            )
        ),
        AcademyLesson(
            id = "L5-1",
            levelNumber = 5,
            levelTitle = "Level 5: Execution",
            title = "Precision Execution & Net Reward-to-Risk Mathematics",
            subtitle = "Why net R after commissions and spread is the only number that matters",
            coreConcept = "A nominal 2:1 trade with wide spread and slippage often nets less than 1.4:1 in reality. Apex requires minimum 2.0 Net R after modeled execution costs.",
            detailedText = "To calculate true Net R: Subtract entry spread, exit spread, and round-turn commissions from the target distance, while adding spread and slippage to the stop loss. If nominal risk is $100 and nominal reward is $200, but fees and spread cost $30, Net Reward is $170 on $115 actual risk = Net 1.48R.",
            institutionalKeyTakeaways = listOf(
                "Minimum Net R filter: 2.0R. Anything less is rejected by hard filters.",
                "Position size must always be calculated backwards from exact dollar risk and stop distance.",
                "Scale out at TP1 (50% volume at 2R, move stop to breakeven + costs)."
            ),
            chartAnnotationExample = "Entry: 1.09340 | SL: 1.09190 (15 pips) | TP1: 1.09640 (30 pips) | Net R: 2.00R",
            quiz = QuizQuestion(
                id = "Q5-1",
                question = "If your account balance is $50,000 and your risk rule is 0.50% per trade, what is your maximum allowable dollar loss on a trade?",
                options = listOf(
                    "$500",
                    "$250",
                    "$1,000",
                    "$100"
                ),
                correctIndex = 1,
                explanation = "$50,000 x 0.0050 = $250.00. This is the exact monetary risk that must determine your position lot sizing."
            )
        ),
        AcademyLesson(
            id = "L6-1",
            levelNumber = 6,
            levelTitle = "Level 6: Quantitative Trading",
            title = "Mathematical Expectancy, Drawdown & Overfitting",
            subtitle = "Statistical robustness and why 55% win rate with 2R beats 90% win rate traps",
            coreConcept = "Expectancy = (Win% x Avg Win) - (Loss% x Avg Loss). A strategy with 50% win rate and 2.2R net produces superior compounding with minimal risk of ruin.",
            detailedText = "Retail traders obsess over high win rates, which often mask toxic tail risk (such as wide stops or martingale doubling). A systematic trader calculates expected value over a minimum sample of 200 trades. Walk-forward backtesting across trending, ranging, and high-volatility regimes protects against curve-fitting.",
            institutionalKeyTakeaways = listOf(
                "Expectancy formula proves that a 40% win rate at 2.5R net yields +0.40R per trade.",
                "Maximum drawdown duration tests emotional endurance far more than peak-to-trough depth.",
                "Backtest reports must incorporate realistic spreads, swaps, and slippage to avoid false optimism."
            ),
            chartAnnotationExample = "Monte Carlo Simulation: 500 Iterations | Expectancy: +0.42R/trade | Max Drawdown: 6.8%",
            quiz = QuizQuestion(
                id = "Q6-1",
                question = "A strategy has a 45% win rate, average win of 2.4R, and average loss of 1.0R. What is the mathematical expectancy per trade?",
                options = listOf(
                    "-0.15R per trade",
                    "+0.53R per trade",
                    "+0.08R per trade",
                    "+1.08R per trade"
                ),
                correctIndex = 1,
                explanation = "Expectancy = (0.45 x 2.4R) - (0.55 x 1.0R) = 1.08R - 0.55R = +0.53R per trade. Over 100 trades, the expected return is +53R."
            )
        ),
        AcademyLesson(
            id = "L7-1",
            levelNumber = 7,
            levelTitle = "Level 7: Psychology and Discipline",
            title = "Behavioral Risk, Tilt Prevention & Cooldown Protocols",
            subtitle = "Eliminating revenge trading, FOMO, and consecutive-loss spirals",
            coreConcept = "The primary cause of trader failure is behavioral breakdown following a string of losses. APEX enforces an automated daily loss limit and consecutive loss lockout.",
            detailedText = "Under acute stress or after 2-3 consecutive losses, the amygdala triggers irrational fight-or-flight responses: increasing position sizes, taking unverified setups, or removing stop losses. APEX SNIPER AI features an automated risk lockout that disables new trading recommendations when the daily loss threshold is reached.",
            institutionalKeyTakeaways = listOf(
                "Mandatory rule: 2 consecutive losses in one session triggers a 4-hour trading cooldown.",
                "Max daily loss cap: 1.5% of total account capital. When breached, the platform locks.",
                "A journal is not just an accounting record; it is an audit of your psychological discipline."
            ),
            chartAnnotationExample = "Risk Safeguard Active: 2 Consecutive Losses Detected -> 4h Cooldown Engaged.",
            quiz = QuizQuestion(
                id = "Q7-1",
                question = "What should a systematic trader do immediately after experiencing two consecutive stop-loss hits in the London session?",
                options = listOf(
                    "Double the lot size on the next trade to recover losses quickly before the session ends",
                    "Switch to a 1-minute chart to hunt for quick scalps",
                    "Step away, activate a cooldown protocol, log the trades in the journal, and assess rule compliance",
                    "Remove stop-losses on future trades to prevent getting stopped out"
                ),
                correctIndex = 2,
                explanation = "A disciplined institutional trader halts trading, follows cooldown safeguards, logs the events objectively, and preserves mental and financial capital."
            )
        )
    )
}
