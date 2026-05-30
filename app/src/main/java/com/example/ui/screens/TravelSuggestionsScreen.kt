package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.GeminiApiClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelSuggestionsScreen(
    city: String,
    temperature: Double,
    weatherCode: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var insightsText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val systemInstruction = """
        You are "Pak Travel Oracle", a smart travel advisor utilizing compiled Pakistan search trends and weather conditions.
        Based on the user's selected city ($city) and current temperature ($temperature°C), provide:
        1. "Search Interest Trends": (Simulated weather interest - e.g., 'Google searches for "fog in Lahore" peaked this morning').
        2. "Travel Advisory Status" for major routing channels connected to $city (e.g., M-2 Motorway, Karakoram Highway, Coastal Highway, GT Road).
        3. "Recommended Destinations": 2-3 travel suggestions nearby (e.g. if Karachi, suggest Kund Malir or Hawksbay; if Rawalpindi/Islamabad, suggest Murree or Monal).
        
        Style the text beautifully with bold headers, bullet items, and a friendly tone. Limit to 3 short paragraphs.
    """.trimIndent()

    LaunchedEffect(city) {
        isLoading = true
        scope.launch {
            val response = GeminiApiClient.getAdvice(
                prompt = "Provide weather insights and context-aware travel suggestions starting from $city. Current temperature is $temperature°C.",
                systemInstruction = systemInstruction
            )
            insightsText = response
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Travel Insights & Sugggestions",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("travel_insights_back")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.CardTravel,
                        contentDescription = "Travel Route",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                    Column {
                        Text(
                            text = "Traveling from $city",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Current Status: $temperature°C",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Route Status Indicators (Pre-compiled Pakistan Specific Routes)
            Text(
                text = "Pakistan Highway Advisory Monitor",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            RouteItemRow(
                routeName = "M-2 Motorway (Lahore - Islamabad)",
                status = if (temperature > 38 || weatherCode in listOf(51, 61, 80)) "Caution (Rain/Heat)" else "Clear & Safe",
                statusColor = if (temperature > 38 || weatherCode in listOf(51, 61, 80)) Color(0xFFFFA000) else Color(0xFF4CAF50),
                icon = Icons.Default.DirectionsCar
            )

            RouteItemRow(
                routeName = "Hazara Expressway / Murree Road",
                status = if (weatherCode in listOf(61, 63, 65, 80, 81)) "Landslide Alert (Heavy Rain)" else "Safe for Light Vehicles",
                statusColor = if (weatherCode in listOf(61, 63, 65, 80, 81)) Color(0xFFF44336) else Color(0xFF4CAF50),
                icon = Icons.Default.Terrain
            )

            RouteItemRow(
                routeName = "Karakoram Highway (KKH)",
                status = "Warning (High altitude climate variability)",
                statusColor = Color(0xFFFFA000),
                icon = Icons.Default.Tour
            )

            Spacer(modifier = Modifier.height(8.dp))

            // AI Compiled insights
            Text(
                text = "Google Trends & Weather Intelligence",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Analyzing regional search indices...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = "Insight icon",
                                tint = Color(0xFFFFD54F)
                            )
                            Text(
                                text = "Search Trends & Travel Recommendations",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = insightsText,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RouteItemRow(
    routeName: String,
    status: String,
    statusColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = "Route icon", tint = statusColor)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = routeName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
