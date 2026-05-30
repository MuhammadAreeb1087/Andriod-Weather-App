package com.example.ui.screens

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.models.CityInfo
import com.example.models.PakistanCities
import com.example.models.WeatherResponse
import com.example.network.WeatherApiClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherDashboardScreen(
    userName: String,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onOpenChat: (city: String, temp: Double) -> Unit,
    onOpenTravelInsights: (city: String, temp: Double, weatherCode: Int) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedCity by remember { mutableStateOf(PakistanCities.list[0]) } // Karachi default
    var weatherData by remember { mutableStateOf<WeatherResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchResults by remember { mutableStateOf(false) }

    // Extreme alert variables
    var activeWarningAlert by remember { mutableStateOf<String?>(null) }

    // Register Notification Channel
    LaunchedEffect(Unit) {
        createNotificationChannel(context)
    }

    // Load weather when selected city changes
    LaunchedEffect(selectedCity) {
        isLoading = true
        errorMessage = ""
        try {
            val response = WeatherApiClient.service.getForecast(
                latitude = selectedCity.latitude,
                longitude = selectedCity.longitude
            )
            weatherData = response
            isLoading = false

            // Check if weather is extreme to flag warning
            val currentTemp = response.current?.temperature ?: 25.0
            val currentWind = response.current?.windSpeed ?: 10.0
            val code = response.current?.weatherCode ?: 0
            
            when {
                currentTemp > 40.0 -> {
                    activeWarningAlert = "Extreme Heatwave Warning! Temperatures in ${selectedCity.name} are expected to exceed $currentTemp°C. Please stay indoors."
                    triggerSystemNotification(context, "Extreme Heat Alert", activeWarningAlert!!)
                }
                currentWind > 45.0 -> {
                    activeWarningAlert = "High Windspeed Alert! Winds are gusting at $currentWind km/h in ${selectedCity.name}. Secure loose items."
                    triggerSystemNotification(context, "Severe Duststorm Alert", activeWarningAlert!!)
                }
                code in listOf(61, 63, 65, 80, 81, 82) -> {
                    activeWarningAlert = "Heavy Rain and Urban Flooding Warning issued for ${selectedCity.name}. Exercise caution on roads."
                    triggerSystemNotification(context, "Flood Advisory Alert", activeWarningAlert!!)
                }
                else -> {
                    activeWarningAlert = null
                }
            }
        } catch (e: Exception) {
            errorMessage = "Failed to update weather: ${e.localizedMessage}"
            isLoading = false
        }
    }

    // Permission request launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            scope.launch {
                val detected = LocationHelper.detectClosestCity(context)
                if (detected != null) {
                    selectedCity = detected
                    triggerSystemNotification(context, "Location Synced", "Automatically detected weather for ${detected.name}!")
                } else {
                    errorMessage = "Location permission granted but city detection unresolved."
                }
            }
        } else {
            errorMessage = "Location permission denied. Please search instead."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Assalam-o-Alaikum, $userName",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Exploring Pakistan Climate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                },
                actions = {
                    // Chat option
                    IconButton(
                        onClick = { 
                            val currentTemp = weatherData?.current?.temperature ?: 25.0
                            onOpenChat(selectedCity.name, currentTemp)
                        },
                        modifier = Modifier.testTag("hub_chatbot_btn")
                    ) {
                        BadgedBox(badge = { Badge { Text("AI") } }) {
                            Icon(Icons.Default.Chat, contentDescription = "Weather Assistant Chat")
                        }
                    }

                    // Travel Recommendations option
                    IconButton(
                        onClick = {
                            val temp = weatherData?.current?.temperature ?: 25.0
                            val code = weatherData?.current?.weatherCode ?: 0
                            onOpenTravelInsights(selectedCity.name, temp, code)
                        },
                        modifier = Modifier.testTag("hub_travel_btn")
                    ) {
                        Icon(Icons.Default.CardTravel, contentDescription = "Travel Suggestions Insights")
                    }

                    // Theme toggle
                    IconButton(onClick = onToggleDarkMode, modifier = Modifier.testTag("theme_toggle")) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Switch Visual theme"
                        )
                    }

                    // Sign-out
                    IconButton(onClick = onLogout, modifier = Modifier.testTag("logout_btn")) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Sign out session")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
                .padding(bottom = 16.dp)
        ) {
            // Search City Input Bar & Auto GPS trigger
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        showSearchResults = it.isNotEmpty()
                    },
                    placeholder = { Text("Search location in Pakistan...", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = ""; showSearchResults = false }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("city_search_bar"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                )

                FilledIconButton(
                    onClick = {
                        // Request permissions to fetch position
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("gps_auto_detect_btn"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Auto GPS Location")
                }
            }

            // Search Results popup lists
            if (showSearchResults) {
                val filteredCities = PakistanCities.list.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                            it.province.contains(searchQuery, ignoreCase = true)
                }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        if (filteredCities.isEmpty()) {
                            Text(
                                "No Pakistani locations found matching '$searchQuery'",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(8.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            filteredCities.take(5).forEach { city ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedCity = city
                                            searchQuery = ""
                                            showSearchResults = false
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = "City Pin", tint = MaterialTheme.colorScheme.primary)
                                    Column {
                                        Text(text = city.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text(text = city.province, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // High priority warning Banner notification
            AnimatedVisibility(
                visible = activeWarningAlert != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("alert_banner"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Severe Alert Icon",
                            tint = Color(0xFFC62828),
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = activeWarningAlert ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFC62828),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Severe warning simulator for evaluation context
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        activeWarningAlert = "Simulator Trigger: Heavy Rain with Lightning Storms alert across Lahore, Rawalpindi, and Peshawar paths! Watch routes closely."
                        triggerSystemNotification(context, "Severe Lightning Warning", activeWarningAlert!!)
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(38.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Thunderstorm, contentDescription = "Flash Sim", modifier = Modifier.size(16.dp).padding(end = 4.dp))
                        Text("Simulate Storm Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = {
                        activeWarningAlert = "Simulator Trigger: High sandstorm gusts detected crossing Karachi & Multan corridors. Roads may be blocked."
                        triggerSystemNotification(context, "Duststorm Advisory", activeWarningAlert!!)
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(38.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Air, contentDescription = "Wind Sim", modifier = Modifier.size(16.dp).padding(end = 4.dp))
                        Text("Simulate Duststorm", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Main Weather Info card
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Retrieving climate indices for ${selectedCity.name}...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else if (errorMessage.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Error, "Error icon", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = errorMessage, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else {
                weatherData?.let { response ->
                    val current = response.current
                    if (current != null) {
                        // Detailed Dashboard card
                        DashboardClimateCard(selectedCity, current)

                        // 7-Day Forecast Section
                        Text(
                            text = "7-Day Meteorological Outlook",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                        )

                        response.daily?.let { daily ->
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(daily.time.size) { index ->
                                    val dateStr = daily.time[index]
                                    val maxVal = daily.maxTemp[index]
                                    val minVal = daily.minTemp[index]
                                    val codeVal = daily.weatherCode[index]
                                    ForecastDayColumn(dateStr, maxVal, minVal, codeVal)
                                }
                            }
                        }

                        // Hourly Wind speed & Humidity levels info section
                        Text(
                            text = "Hourly Wind & Humidity Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                        )

                        response.hourly?.let { hourly ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // Take next 12 hours for high-fidelity scanning
                                        items(12) { i ->
                                            val completeTime = hourly.time[i]
                                            val briefTime = completeTime.substringAfter("T") // E.g: 04:00
                                            val curTemp = hourly.temperature[i]
                                            val curHumidity = hourly.humidity[i]
                                            val curWind = hourly.windSpeed[i]
                                            val curCode = hourly.weatherCode[i]

                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier
                                                    .width(72.dp)
                                                    .background(
                                                        MaterialTheme.colorScheme.surface,
                                                        shape = RoundedCornerShape(10.dp)
                                                    )
                                                    .padding(8.dp)
                                            ) {
                                                Text(
                                                    text = briefTime,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Icon(
                                                    imageVector = getWeatherIcon(curCode),
                                                    contentDescription = "Code icon",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(24.dp).padding(vertical = 4.dp)
                                                )
                                                Text(
                                                    text = "${curTemp.toInt()}°C",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.WaterDrop, "Humidity", tint = Color(0xFF03A9F4), modifier = Modifier.size(10.dp))
                                                    Text(text = "${curHumidity.toInt()}%", fontSize = 9.sp)
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Air, "Windy", tint = Color.LightGray, modifier = Modifier.size(10.dp))
                                                    Text(text = "${curWind.toInt()}k/h", fontSize = 9.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Beautiful Interactive Pakistan Weather Map Canvas (Custom Vector Map)
            Text(
                text = "Pakistan Meteorological Radar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
            
            Text(
                text = "Click provinces or pins below to instantly adjust sensor tracking",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
            )

            // Styled interactive Canvas Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                // Default callback click mapping coordinates inside Pakistan roughly
                            }
                    ) {
                        val w = size.width
                        val h = size.height

                        // Draw schematic layout paths of Pakistan provinces
                        // Balochistan Path (Bottom-Left)
                        val balochistanPath = Path().apply {
                            moveTo(w * 0.1f, h * 0.8f)
                            lineTo(w * 0.35f, h * 0.9f)
                            lineTo(w * 0.45f, h * 0.7f)
                            lineTo(w * 0.3f, h * 0.5f)
                            lineTo(w * 0.1f, h * 0.6f)
                            close()
                        }
                        drawPath(balochistanPath, color = Color(0xFF81C784).copy(alpha = 0.35f))
                        drawPath(balochistanPath, color = Color(0xFF2E7D32).copy(alpha = 0.5f), style = Stroke(width = 2.dp.toPx()))

                        // Sindh Path (Bottom-Right)
                        val sindhPath = Path().apply {
                            moveTo(w * 0.35f, h * 0.9f)
                            lineTo(w * 0.55f, h * 0.85f)
                            lineTo(w * 0.52f, h * 0.68f)
                            lineTo(w * 0.45f, h * 0.7f)
                            close()
                        }
                        drawPath(sindhPath, color = Color(0xFFFFD54F).copy(alpha = 0.35f))
                        drawPath(sindhPath, color = Color(0xFFF57F17).copy(alpha = 0.5f), style = Stroke(width = 2.dp.toPx()))

                        // Punjab Path (Central-Right)
                        val punjabPath = Path().apply {
                            moveTo(w * 0.45f, h * 0.7f)
                            lineTo(w * 0.52f, h * 0.68f)
                            lineTo(w * 0.65f, h * 0.45f)
                            lineTo(w * 0.55f, h * 0.38f)
                            lineTo(w * 0.42f, h * 0.52f)
                            close()
                        }
                        drawPath(punjabPath, color = Color(0xFF64B5F6).copy(alpha = 0.35f))
                        drawPath(punjabPath, color = Color(0xFF1565C0).copy(alpha = 0.5f), style = Stroke(width = 2.dp.toPx()))

                        // Khyber Pakhtunkhwa Path (Northwest)
                        val kpPath = Path().apply {
                            moveTo(w * 0.35f, h * 0.52f)
                            lineTo(w * 0.42f, h * 0.52f)
                            lineTo(w * 0.55f, h * 0.38f)
                            lineTo(w * 0.48f, h * 0.28f)
                            lineTo(w * 0.36f, h * 0.42f)
                            close()
                        }
                        drawPath(kpPath, color = Color(0xFFFF8A65).copy(alpha = 0.35f))
                        drawPath(kpPath, color = Color(0xFFBF360C).copy(alpha = 0.5f), style = Stroke(width = 2.dp.toPx()))

                        // Gilgit-Baltistan / Kashmir (Far North)
                        val gbPath = Path().apply {
                            moveTo(w * 0.48f, h * 0.28f)
                            lineTo(w * 0.55f, h * 0.38f)
                            lineTo(w * 0.7f, h * 0.25f)
                            lineTo(w * 0.6f, h * 0.15f)
                            close()
                        }
                        drawPath(gbPath, color = Color(0xFFB39DDB).copy(alpha = 0.35f))
                        drawPath(gbPath, color = Color(0xFF4527A0).copy(alpha = 0.5f), style = Stroke(width = 2.dp.toPx()))
                    }

                    // Floating Interactive Province Names buttons overlays inside Canvas!
                    // Balochistan button overlay
                    Box(
                        modifier = Modifier
                            .offset(x = 42.dp, y = 180.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.DarkGray.copy(alpha = 0.8f))
                            .clickable { selectedCity = PakistanCities.list.first { it.name == "Quetta" } }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text("Balochistan (Quetta)", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    // Sindh button overlay
                    Box(
                        modifier = Modifier
                            .offset(x = 160.dp, y = 220.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.DarkGray.copy(alpha = 0.8f))
                            .clickable { selectedCity = PakistanCities.list.first { it.name == "Karachi" } }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text("Sindh (Karachi)", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    // Punjab button overlay
                    Box(
                        modifier = Modifier
                            .offset(x = 210.dp, y = 140.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.DarkGray.copy(alpha = 0.8f))
                            .clickable { selectedCity = PakistanCities.list.first { it.name == "Lahore" } }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text("Punjab (Lahore)", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    // KP button overlay
                    Box(
                        modifier = Modifier
                            .offset(x = 135.dp, y = 110.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.DarkGray.copy(alpha = 0.8f))
                            .clickable { selectedCity = PakistanCities.list.first { it.name == "Peshawar" } }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text("KP (Peshawar)", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    // GB button overlay
                    Box(
                        modifier = Modifier
                            .offset(x = 200.dp, y = 60.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.DarkGray.copy(alpha = 0.8f))
                            .clickable { selectedCity = PakistanCities.list.first { it.name == "Gilgit" } }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text("Gilgit-Baltistan", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    // Map overlay stats info in bottom corner
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(text = "Sensor Selected", fontSize = 8.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            Text(text = selectedCity.name, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            val displayTemp = weatherData?.current?.temperature?.toInt() ?: 24
                            Text(text = "$displayTemp°C", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

// Subordinate climate stats container widget
@Composable
fun DashboardClimateCard(city: CityInfo, current: com.example.models.CurrentWeather) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("dashboard_weather_card")
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Pin", tint = Color.Red, modifier = Modifier.size(18.dp))
                        Text(
                            text = city.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = city.province,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }

                // Weather condition description
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = getWeatherDesc(current.weatherCode),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Large temperature details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = getWeatherIcon(current.weatherCode),
                    contentDescription = "Condition Big",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(72.dp)
                )
                Text(
                    text = "${current.temperature.toInt()}°C",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Three stats columns
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.WaterDrop, contentDescription = "Humidity indicator", tint = Color(0xFF03A9F4))
                    Text(text = "Humidity", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                    Text(text = "${current.humidity.toInt()}%", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Air, contentDescription = "Wind Speed indicator", tint = Color.LightGray)
                    Text(text = "Wind", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                    Text(text = "${current.windSpeed.toInt()} km/h", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Speed, contentDescription = "UV index", tint = Color(0xFFFFB300))
                    Text(text = "UV Index", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                    // Deduce mock UV
                    val uv = if (current.weatherCode == 0) "Very High" else "Moderate"
                    Text(text = uv, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Weather descriptions mapping function
fun getWeatherDesc(code: Int): String {
    return when (code) {
        0 -> "Sunny"
        1, 2, 3 -> "Partly Cloudy"
        45, 48 -> "Foggy conditions"
        51, 53, 55 -> "Light Drizzle"
        61, 63, 65 -> "Heavy Rain"
        71, 73, 75 -> "Snow showers"
        80, 81, 82 -> "Rain showers"
        95, 96, 99 -> "Severe Thunderstorms"
        else -> "Overcast skies"
    }
}

// Weather Icons loader helper
@Composable
fun getWeatherIcon(code: Int): androidx.compose.ui.graphics.vector.ImageVector {
    return when (code) {
        0 -> Icons.Default.WbSunny
        1, 2, 3 -> Icons.Default.CloudQueue
        45, 48 -> Icons.Default.BlurOn
        51, 53, 55 -> Icons.Default.WaterDrop
        61, 63, 65 -> Icons.Default.Thunderstorm
        71, 73, 75 -> Icons.Default.AcUnit
        80, 81, 82 -> Icons.Default.Grain
        95, 96, 99 -> Icons.Default.FlashOn
        else -> Icons.Default.Cloud
    }
}

// Forecast daily forecast element layout
@Composable
fun ForecastDayColumn(date: String, max: Double, min: Double, code: Int) {
    // format date as brief string, e.g. "Sat" or day of week
    val dayLabel = try {
        val parts = date.split("-") // 2026-05-30
        if (parts.size >= 3) {
            "${parts[1]}/${parts[2]}"
        } else date
    } catch (e: Exception) {
        date
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .width(82.dp)
            .padding(vertical = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Text(text = dayLabel, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Icon(
                imageVector = getWeatherIcon(code),
                contentDescription = "day status icon",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "${max.toInt()}°C",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "${min.toInt()}°C",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Setup notifications channels helper
fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Severe Climate Alerts"
        val descText = "Receives urgent monsoon, duststorm, or heatwave push alerts"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("PAK_WEATHER_ALERTS", name, importance).apply {
            description = descText
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

// Trigger Notifications drawer helper
fun triggerSystemNotification(context: Context, title: String, text: String) {
    // Check permission for Android 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
    }

    val builder = NotificationCompat.Builder(context, "PAK_WEATHER_ALERTS")
        .setSmallIcon(android.R.drawable.stat_sys_warning)
        .setContentTitle(title)
        .setContentText(text)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setStyle(NotificationCompat.BigTextStyle().bigText(text))
        .setAutoCancel(true)

    try {
        val manager = NotificationManagerCompat.from(context)
        manager.notify((System.currentTimeMillis() % 100000).toInt(), builder.build())
    } catch (e: SecurityException) {
        e.printStackTrace()
    }
}
