package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReadingSession
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.CoralWarning
import com.example.ui.theme.SageGreenBreak
import com.example.ui.theme.SageGreenDark
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ReadingStatsScreen(
    sessions: List<ReadingSession>,
    totalFocusMinutes: Int,
    completedCount: Int,
    onDeleteSession: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Overview", "Today", "This Week", "This Month")

    // Time boundary calculations
    // Start of Today
    val startOfToday = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    // Start of 7 Days Ago
    val startOf7Days = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -6)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    // Start of 30 Days Ago
    val startOf30Days = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -29)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val todaySessions = sessions.filter { (it.startTime.takeIf { t -> t > 0 } ?: it.timestamp) >= startOfToday }
    val weekSessions = sessions.filter { (it.startTime.takeIf { t -> t > 0 } ?: it.timestamp) >= startOf7Days }
    val monthSessions = sessions.filter { (it.startTime.takeIf { t -> t > 0 } ?: it.timestamp) >= startOf30Days }

    val todayFocusMins = todaySessions.sumOf { it.actualFocusMinutes }
    val weekFocusMins = weekSessions.sumOf { it.actualFocusMinutes }
    val monthFocusMins = monthSessions.sumOf { it.actualFocusMinutes }

    // Statistics Calculations
    val averageSessionLength = if (sessions.isNotEmpty()) {
        (sessions.sumOf { it.actualFocusMinutes }.toDouble() / sessions.size).toInt()
    } else 0

    val longestStreakDays = calculateLongestStreak(sessions)
    val totalBreaksMinutes = sessions.sumOf { it.breakMinutes }

    val displayedSessions = when (selectedTab) {
        1 -> todaySessions
        2 -> weekSessions
        3 -> monthSessions
        else -> sessions
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("reading_stats_screen"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Reading Focus Stats",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Pure reading time tracked • Break times are excluded",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Time Filter Tabs
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = AmberPrimary
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == index) AmberPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }
        }

        // Period Cards: Today / Week / Month Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatPeriodCard(
                    title = "TODAY",
                    minutes = todayFocusMins,
                    subtitle = "${todaySessions.size} sessions",
                    isHighlight = selectedTab == 1,
                    modifier = Modifier.weight(1f)
                )
                StatPeriodCard(
                    title = "THIS WEEK",
                    minutes = weekFocusMins,
                    subtitle = "${weekSessions.size} sessions",
                    isHighlight = selectedTab == 2,
                    modifier = Modifier.weight(1f)
                )
                StatPeriodCard(
                    title = "THIS MONTH",
                    minutes = monthFocusMins,
                    subtitle = "${monthSessions.size} sessions",
                    isHighlight = selectedTab == 3,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Key Metrics: Streak, Average Session, Completed, Breaks Rested
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PERFORMANCE METRICS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = AmberPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricTile(
                            icon = Icons.Default.LocalFireDepartment,
                            iconColor = AmberPrimary,
                            label = "Longest Streak",
                            value = if (longestStreakDays > 0) "$longestStreakDays ${if (longestStreakDays == 1) "Day" else "Days"}" else "0 Days",
                            subtext = "Daily reading habit",
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        MetricTile(
                            icon = Icons.Default.AccessTime,
                            iconColor = SageGreenDark,
                            label = "Avg Session",
                            value = "$averageSessionLength min",
                            subtext = "Per focus block",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricTile(
                            icon = Icons.Default.CheckCircle,
                            iconColor = SageGreenBreak,
                            label = "Completed",
                            value = "$completedCount",
                            subtext = "Finished targets",
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        MetricTile(
                            icon = Icons.Default.Spa,
                            iconColor = AmberPrimary,
                            label = "Rest & Breaks",
                            value = "${totalBreaksMinutes}m",
                            subtext = "Eye & mind rest",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Detailed Session History Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Session History (${displayedSessions.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = when (selectedTab) {
                        1 -> "Showing Today"
                        2 -> "Showing Last 7 Days"
                        3 -> "Showing Last 30 Days"
                        else -> "Showing All"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (displayedSessions.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "No sessions",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No sessions found for this period.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            items(displayedSessions, key = { it.id }) { session ->
                val dateOnlyFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

                val sessionDate = dateOnlyFormat.format(Date(session.timestamp))
                val startTimeStr = if (session.startTime > 0) timeFormat.format(Date(session.startTime)) else timeFormat.format(Date(session.timestamp))
                val endTimeStr = if (session.endTime > 0) timeFormat.format(Date(session.endTime)) else timeFormat.format(Date(session.timestamp + session.actualFocusMinutes * 60000L))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("session_item_${session.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(
                                            if (session.completed) SageGreenBreak.copy(alpha = 0.15f) else AmberPrimary.copy(alpha = 0.15f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Book,
                                        contentDescription = "Session",
                                        tint = if (session.completed) SageGreenDark else AmberPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = session.bookTitle,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = sessionDate,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Completion Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (session.completed) SageGreenBreak.copy(alpha = 0.2f) else CoralWarning.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (session.completed) "✓ Completed" else "Ended Early",
                                    color = if (session.completed) SageGreenDark else CoralWarning,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            IconButton(
                                onClick = { onDeleteSession(session.id) },
                                modifier = Modifier.testTag("delete_session_${session.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete Session",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Details Row: Start/End Time, Pure Focus Duration, Break Time
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = "Time",
                                        tint = AmberPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$startTimeStr – $endTimeStr",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AmberPrimary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${session.actualFocusMinutes}m Focus",
                                    color = AmberPrimary,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            if (session.breakMinutes > 0) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SageGreenBreak.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${session.breakMinutes}m Rest",
                                        color = SageGreenDark,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatPeriodCard(
    title: String,
    minutes: Int,
    subtitle: String,
    isHighlight: Boolean,
    modifier: Modifier = Modifier
) {
    val hrs = minutes / 60
    val remMins = minutes % 60
    val timeText = if (hrs > 0) "${hrs}h ${remMins}m" else "${remMins}m"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlight) AmberPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isHighlight) AmberPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = timeText,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MetricTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    subtext: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }
    }
}

private fun calculateLongestStreak(sessions: List<ReadingSession>): Int {
    if (sessions.isEmpty()) return 0

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dayTimestamps = sessions
        .map { (it.startTime.takeIf { t -> t > 0 } ?: it.timestamp) }
        .map { dateFormat.format(Date(it)) }
        .distinct()
        .sorted()

    if (dayTimestamps.isEmpty()) return 0

    var maxStreak = 1
    var currentStreak = 1
    val parsedDates = dayTimestamps.mapNotNull {
        try { dateFormat.parse(it) } catch (_: Exception) { null }
    }

    for (i in 1 until parsedDates.size) {
        val prev = Calendar.getInstance().apply { time = parsedDates[i - 1] }
        val curr = Calendar.getInstance().apply { time = parsedDates[i] }

        prev.add(Calendar.DAY_OF_YEAR, 1)
        if (prev.get(Calendar.YEAR) == curr.get(Calendar.YEAR) &&
            prev.get(Calendar.DAY_OF_YEAR) == curr.get(Calendar.DAY_OF_YEAR)) {
            currentStreak++
            if (currentStreak > maxStreak) {
                maxStreak = currentStreak
            }
        } else {
            currentStreak = 1
        }
    }

    return maxStreak
}

