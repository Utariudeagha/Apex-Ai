package com.example.model

enum class NewsImpact { HIGH, MEDIUM, LOW }

data class EconomicEvent(
    val id: String,
    val title: String,
    val currency: String,
    val impact: NewsImpact,
    val scheduledTime: Long,
    val previous: String,
    val forecast: String,
    val actual: String?,
    val surprise: String?,
    val isBlackoutActive: Boolean = false, // True if within +/- 45 mins of high-impact event
    val relevanceSummary: String
)
