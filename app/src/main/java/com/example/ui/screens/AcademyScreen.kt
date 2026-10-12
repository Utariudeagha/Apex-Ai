package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AcademyLesson
import com.example.model.QuizQuestion
import com.example.ui.MainViewModel
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BorderDark
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun AcademyScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val lessons = viewModel.academyLessons
    val completedQuizzes by viewModel.completedQuizIds.collectAsState()

    var expandedLessonId by remember { mutableStateOf<String?>(lessons.firstOrNull()?.id) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // Academy Header
        item {
            Column {
                Text(
                    text = "APEX TRADING ACADEMY",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "7-Level Progressive Curriculum • Systematic Price Action & Microstructure",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Progress Bar Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("CURRICULUM PROGRESS", fontSize = 10.sp, color = TextMuted)
                        Text(
                            text = "${completedQuizzes.size} of ${lessons.size} Mastery Quizzes Completed",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyanAccent.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${((completedQuizzes.size.toFloat() / lessons.size.toFloat()) * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Lessons List
        items(lessons) { lesson ->
            val isExpanded = expandedLessonId == lesson.id
            val isQuizCompleted = completedQuizzes.contains(lesson.quiz.id)

            LessonCard(
                lesson = lesson,
                isExpanded = isExpanded,
                isQuizCompleted = isQuizCompleted,
                onToggleExpand = {
                    expandedLessonId = if (isExpanded) null else lesson.id
                },
                onQuizPassed = {
                    viewModel.markQuizCompleted(lesson.quiz.id)
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun LessonCard(
    lesson: AcademyLesson,
    isExpanded: Boolean,
    isQuizCompleted: Boolean,
    onToggleExpand: () -> Unit,
    onQuizPassed: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lesson.levelTitle.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = lesson.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = lesson.subtitle,
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isQuizCompleted) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = BullishGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = TextMuted
                    )
                }
            }

            // Expanded Lesson Content
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))

                // Core Concept Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceElevated)
                        .padding(10.dp)
                ) {
                    Column {
                        Text("CORE CONCEPT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(lesson.coreConcept, fontSize = 11.sp, color = TextPrimary, lineHeight = 15.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Detailed Text
                Text(
                    text = lesson.detailedText,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Key Institutional Takeaways
                Text("INSTITUTIONAL TAKEAWAYS:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                lesson.institutionalKeyTakeaways.forEach { takeaway ->
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text("• ", color = CyanAccent, fontSize = 11.sp)
                        Text(takeaway, color = TextSecondary, fontSize = 10.sp, lineHeight = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Chart Example Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceVariantDark, RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Chart Example: ${lesson.chartAnnotationExample}",
                        fontSize = 10.sp,
                        color = CyanAccent
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive Quiz Section
                QuizSection(
                    quiz = lesson.quiz,
                    isAlreadyCompleted = isQuizCompleted,
                    onCorrectAnswer = onQuizPassed
                )
            }
        }
    }
}

@Composable
private fun QuizSection(
    quiz: QuizQuestion,
    isAlreadyCompleted: Boolean,
    onCorrectAnswer: () -> Unit
) {
    var selectedOptionIndex by remember { mutableIntStateOf(-1) }
    var submitted by remember { mutableStateOf(isAlreadyCompleted) }
    var isCorrect by remember { mutableStateOf(isAlreadyCompleted) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BackgroundDark),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = "LEVEL MASTERY CHALLENGE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = WarningAmber,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = quiz.question, fontSize = 11.sp, color = TextPrimary, lineHeight = 15.sp)

            Spacer(modifier = Modifier.height(8.dp))

            quiz.options.forEachIndexed { index, option ->
                val isSelected = selectedOptionIndex == index
                val optionColor = when {
                    submitted && index == quiz.correctIndex -> BullishGreen
                    submitted && isSelected && index != quiz.correctIndex -> BearishRed
                    isSelected -> CyanAccent
                    else -> SurfaceVariantDark
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected || submitted) optionColor.copy(alpha = 0.15f) else SurfaceVariantDark)
                        .clickable(enabled = !submitted) { selectedOptionIndex = index }
                        .padding(8.dp)
                ) {
                    Text(
                        text = option,
                        fontSize = 10.sp,
                        color = if (submitted && index == quiz.correctIndex) BullishGreen else TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!submitted) {
                Button(
                    onClick = {
                        if (selectedOptionIndex >= 0) {
                            submitted = true
                            if (selectedOptionIndex == quiz.correctIndex) {
                                isCorrect = true
                                onCorrectAnswer()
                            } else {
                                isCorrect = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    shape = RoundedCornerShape(6.dp),
                    enabled = selectedOptionIndex >= 0
                ) {
                    Text("Submit Answer", fontSize = 11.sp, color = SurfaceDark, fontWeight = FontWeight.Bold)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isCorrect) BullishGreen.copy(alpha = 0.15f) else BearishRed.copy(alpha = 0.15f))
                        .padding(8.dp)
                ) {
                    Column {
                        Text(
                            text = if (isCorrect) "✓ Correct! Concept Mastered." else "✗ Incorrect. Review explanation below:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCorrect) BullishGreen else BearishRed
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = quiz.explanation,
                            fontSize = 10.sp,
                            color = TextPrimary,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}
