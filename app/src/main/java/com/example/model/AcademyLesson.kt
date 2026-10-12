package com.example.model

data class QuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class AcademyLesson(
    val id: String,
    val levelNumber: Int,
    val levelTitle: String,
    val title: String,
    val subtitle: String,
    val coreConcept: String,
    val detailedText: String,
    val institutionalKeyTakeaways: List<String>,
    val chartAnnotationExample: String,
    val quiz: QuizQuestion
)
