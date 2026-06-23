package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediKartApp(
    viewModel: MediKartViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val notificationMessage by viewModel.notificationMessage.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    Scaffold(
        topBar = {
            Column {
                // Top Brand and Configuration Bar
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "MediKart",
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 24.sp
                            )
                            Text(
                                text = "Alipur Chatha Service Area",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onToggleTheme) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Dark/Light Mode"
                            )
                        }
                    },
                    actions = {
                        if (isLoggedIn) {
                            IconButton(
                                onClick = { viewModel.logout() },
                                modifier = Modifier.testTag("app_logout_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = "Logout",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Global Role Selector (Presentation / Simulation Helper)
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SIMULATOR: CHOOSE SYSTEM PERSONA",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val roles = listOf(
                                Triple("Customer", Icons.Default.Person, "role_customer"),
                                Triple("Pharmacy", Icons.Default.MedicalServices, "role_pharmacy"),
                                Triple("Rider", Icons.Default.Motorcycle, "role_rider"),
                                Triple("Admin", Icons.Default.AdminPanelSettings, "role_admin")
                            )
                            roles.forEach { (roleName, icon, tag) ->
                                val active = currentRole == roleName
                                FilterChip(
                                    selected = active,
                                    onClick = { viewModel.setRole(roleName) },
                                    label = { Text(roleName, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = roleName,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier.testTag(tag)
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    tonalElevation = 4.dp
                ) {
                    Text(
                        text = "Your Medicines, Delivered with Care. Applet V1.0",
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main content based on Login or Role
            if (!isLoggedIn) {
                LoginScreen(viewModel = viewModel)
            } else {
                when (currentRole) {
                    "Customer" -> CustomerDashboard(viewModel = viewModel)
                    "Pharmacy" -> PharmacyDashboard(viewModel = viewModel)
                    "Rider" -> RiderDashboard(viewModel = viewModel)
                    "Admin" -> AdminDashboard(viewModel = viewModel)
                }
            }

            // Real-time notification Banner
            AnimatedVisibility(
                visible = notificationMessage != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(12.dp)
                    .zIndex(10f)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Alert",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = notificationMessage ?: "",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearNotification() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// PHONE REGISTRATION & OTP LOGIN SCREEN
// -------------------------------------------------------------
@Composable
fun LoginScreen(viewModel: MediKartViewModel) {
    var phoneInput by remember { mutableStateOf("+92 300 ") }
    var nameInput by remember { mutableStateOf("Ibrahim Chatha") }
    var otpSent by remember { mutableStateOf(false) }
    var otpInput by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.LocalHospital,
                contentDescription = "Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Welcome to MediKart",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "\"Your Medicines, Delivered with Care.\"",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (!otpSent) "Phone Registration" else "Verify OTP",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (!otpSent) "Secure OTP access for Alipur Chatha residents" else "Enter simulated 4-digit code (use '1234')",
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (!otpSent) {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("login_name_input"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text("Phone Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_phone_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                if (phoneInput.length > 8 && nameInput.isNotBlank()) {
                                    otpSent = true
                                    viewModel.showNotification("OTP Pin code 1234 sent premium to $phoneInput")
                                } else {
                                    viewModel.showNotification("Please enter a valid Name and Phone Number")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("login_send_otp_button")
                        ) {
                            Text("Send OTP", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    } else {
                        OutlinedTextField(
                            value = otpInput,
                            onValueChange = { otpInput = it },
                            label = { Text("OTP (Pin Code)") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_otp_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                if (otpInput == "1234" || otpInput.length == 4) {
                                    viewModel.login(phoneInput, nameInput)
                                } else {
                                    viewModel.showNotification("Incorrect Pin! Try '1234' on simulation.")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("login_verify_otp_button")
                        ) {
                            Text("Submit & Log In", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        TextButton(onClick = { otpSent = false }) {
                            Text("Back to Edit Number", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// CUSTOMER ROLE INTERFACE
// -------------------------------------------------------------
@Composable
fun CustomerDashboard(viewModel: MediKartViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Shop", "Rx & Cart", "Trackers", "Care & Plus")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    icon = {
                        val icon = when (index) {
                            0 -> Icons.Default.MedicalInformation
                            1 -> Icons.Default.ShoppingCart
                            2 -> Icons.Default.LocalShipping
                            else -> Icons.Default.Favorite
                        }
                        Icon(imageVector = icon, contentDescription = null)
                    },
                    modifier = Modifier.testTag("customer_tab_$index")
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> CustomerShopTab(viewModel = viewModel)
                1 -> CustomerCartTab(viewModel = viewModel)
                2 -> CustomerTrackersTab(viewModel = viewModel)
                3 -> CustomerCarePlusTab(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CustomerShopTab(viewModel: MediKartViewModel) {
    val medicines by viewModel.medicines.collectAsState()
    val pharmacies by viewModel.pharmacies.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val premiumActive by viewModel.premiumActive.collectAsState()

    val categories = listOf("All", "Pain Relief", "Cold & Flu", "Antibiotics", "Vitamins", "Heart & BP", "Pediatric Care", "Digestion", "Diabetes")

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // PROMO BANNER with Generated Image
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .testTag("customer_promo_banner_card"),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Attempt to load the true generated image.
                        // We use painterResource linking directly.
                        Image(
                            painter = painterResource(id = R.drawable.medikart_promo_banner),
                            contentDescription = "Online medicine delivery, Alipur Chatha",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Elegant overlay text
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(bottom = 4.dp)
                            ) {
                                Text(
                                    "ALIPUR CHATHA EXCLUSIVE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                "Medicines Delivered in 30 Mins",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                "Standard local delivery PKR 30-50. Free for MediKart Plus subscribers!",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            // SEARCH BAR
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("medicine_search_input"),
                    placeholder = { Text("Search medicines (e.g. Panadol, Amoxil)") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                    )
                )
            }

            // CATEGORY TABS
            item {
                Column {
                    Text(
                        "Browse Categories",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { category ->
                            val selected = selectedCategory == category
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.updateCategory(category) },
                                label = { Text(category, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.testTag("category_chip_$category")
                            )
                        }
                    }
                }
            }

            // MEDICINE LIST
            val filteredMedicines = medicines.filter { med ->
                (selectedCategory == "All" || med.category == selectedCategory) &&
                        med.name.contains(searchQuery, ignoreCase = true)
            }

            if (filteredMedicines.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalInformation,
                            contentDescription = "No match",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "No medicines match filters.",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                item {
                    Text(
                        "Available Medicines",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(filteredMedicines) { med ->
                    val finalPrice = if (premiumActive) med.price * 0.90 else med.price // 10% discount
                    val df = SimpleDateFormat("K:mm a", Locale.getDefault())
                    val updatedStr = df.format(Date(med.lastUpdated))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("medicine_card_${med.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = med.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (med.isPrescriptionRequired) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.errorContainer,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                "Rx Required",
                                                color = MaterialTheme.colorScheme.error,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "Pharmacy: ${med.pharmacyName}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "PKR ${String.format("%.2f", finalPrice)}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if (premiumActive) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "PKR ${med.price}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                            ),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        color = if (med.availability) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (med.availability) "In Stock" else "Low Stock",
                                            color = if (med.availability) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = "Updated: $updatedStr",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (med.availability) {
                                    Button(
                                        onClick = { viewModel.addToCart(med.id) },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        modifier = Modifier.testTag("add_to_cart_btn_${med.id}")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    IconButton(
                                        enabled = false,
                                        onClick = {},
                                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                    ) {
                                        Icon(
                                            Icons.Default.Block,
                                            contentDescription = "Unavailable",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // PARTNER PHARMACIES PREVIEW
            item {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(
                        "Local Partner Pharmacies",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Verified by Admin for prescription privacy and security",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(pharmacies) { pharmacy ->
                            Card(
                                modifier = Modifier
                                    .width(180.dp)
                                    .testTag("pharmacy_vendor_card_${pharmacy.id}"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            pharmacy.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (pharmacy.isVerified) {
                                            Icon(
                                                imageVector = Icons.Default.Verified,
                                                contentDescription = "Verified Partner",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        pharmacy.address,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = "Rating",
                                            tint = Color(0xFFFFB300),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${pharmacy.rating}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // EMERGENCY BUTTON OVERLAY
        ExtendedFloatingActionButton(
            onClick = {
                viewModel.addToCart(1) // Auto-add Fast acting Panadol
                viewModel.placeOrder(isEmergency = true)
            },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White) },
            text = { Text("Emergency Dispatch", fontWeight = FontWeight.Bold, color = Color.White) },
            containerColor = MaterialTheme.colorScheme.error,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("customer_emergency_dispatch_button")
        )
    }
}

@Composable
fun CustomerCartTab(viewModel: MediKartViewModel) {
    val cart by viewModel.cart.collectAsState()
    val medicines by viewModel.medicines.collectAsState()
    val addresses by viewModel.addresses.collectAsState()
    val selectedAddress by viewModel.selectedAddress.collectAsState()
    val rxPath by viewModel.uploadedPrescriptionPath.collectAsState()
    val premiumActive by viewModel.premiumActive.collectAsState()

    var newAddressInput by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Cash on Delivery") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Upload Prescription",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                "Required for antibiotics, diabetes, cardiac, and clinical medicines",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rx_upload_interaction_card"),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (rxPath == null) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Upload",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Take Photo of Prescription or Select Gallery",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = { viewModel.uploadPrescriptionMock() },
                                modifier = Modifier.testTag("rx_camera_upload")
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Camera", fontSize = 11.sp)
                            }
                            FilledTonalButton(
                                onClick = { viewModel.uploadPrescriptionMock() },
                                modifier = Modifier.testTag("rx_gallery_upload")
                            ) {
                                Icon(Icons.Default.Photo, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Gallery", fontSize = 11.sp)
                            }
                        }
                    } else {
                        // Display uploaded visual indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Task,
                                contentDescription = "Active RX",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("prescription_snapshot_enc.png", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Encrypted Patient Profile Securely Linked", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { viewModel.removePrescriptionPath() }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        // SHOPPING CART LIST
        item {
            Text(
                "Your Cart",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (cart.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Empty",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Your shopping cart is currently empty.", fontSize = 13.sp)
                    }
                }
            }
        } else {
            val totalMedicineCost = cart.map { (id, qty) ->
                val med = medicines.find { it.id == id }
                val price = if (med != null) (if (premiumActive) med.price * 0.9 else med.price) else 0.0
                price * qty
            }.sum()

            val deliveryFee = if (premiumActive) 0.0 else 40.0
            val subtotalPayable = totalMedicineCost + deliveryFee

            items(cart.toList()) { (id, qty) ->
                val med = medicines.find { it.id == id } ?: return@items
                val price = if (premiumActive) med.price * 0.9 else med.price

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(med.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("PKR ${String.format("%.2f", price)} each • Total: PKR ${String.format("%.2f", price * qty)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.removeFromCart(med.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                        }
                        Text("$qty", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        IconButton(
                            onClick = { viewModel.addToCart(med.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Checkout Summary calculations
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Medicines Subtotal", modifier = Modifier.weight(1f), fontSize = 13.sp)
                            Text("PKR ${String.format("%.2f", totalMedicineCost)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Delivery Fee (Alipur Chatha)", modifier = Modifier.weight(1f), fontSize = 13.sp)
                            Text(
                                text = if (premiumActive) "FREE (Plus)" else "PKR ${String.format("%.2f", deliveryFee)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (premiumActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Grand Total Payable", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("PKR ${String.format("%.2f", subtotalPayable)}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // ADDRESS SELECTION & ADD ADDRESS
        item {
            Text(
                "Multiple Delivery Addresses",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            addresses.forEach { address ->
                val active = selectedAddress == address
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setSelectedAddress(address) }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = active,
                        onClick = { viewModel.setSelectedAddress(address) },
                        modifier = Modifier.testTag("address_radio_${addresses.indexOf(address)}")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = address,
                        fontSize = 12.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = newAddressInput,
                onValueChange = { newAddressInput = it },
                label = { Text("Add New Home/Office Address") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_address_textbox"),
                trailingIcon = {
                    IconButton(onClick = {
                        if (newAddressInput.isNotBlank()) {
                            viewModel.addAddress(newAddressInput)
                            newAddressInput = ""
                        }
                    }) {
                        Icon(Icons.Default.AddLocation, contentDescription = "Add Location")
                    }
                },
                singleLine = true
            )
        }

        // PAYMENT METHOD CHOSEN
        item {
            Text(
                "Payment Method",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                val methods = listOf("Cash on Delivery", "Digital Payment")
                methods.forEach { method ->
                    val selected = paymentMethod == method
                    FilterChip(
                        selected = selected,
                        onClick = { paymentMethod = method },
                        label = { Text(method, fontSize = 12.sp) },
                        modifier = Modifier.testTag("payment_chip_$method")
                    )
                }
            }
        }

        // PLACE ORDER BUTTON
        item {
            val isEnabled = cart.isNotEmpty() || rxPath != null
            Button(
                onClick = { viewModel.placeOrder(paymentMethod = paymentMethod) },
                enabled = isEnabled,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("place_customer_order_btn")
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Confirm and Dispatch Order", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
fun CustomerTrackersTab(viewModel: MediKartViewModel) {
    val orders by viewModel.orders.collectAsState()
    val loggedInPhone by viewModel.loggedInPhone.collectAsState()

    val myOrders = orders.filter { it.customerPhone == loggedInPhone }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Real-Time Order Tracking & History",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                "Track the dynamic status of medicine dispatches in Alipur Chatha city",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        if (myOrders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No active or previous orders found.", fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(myOrders) { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_order_history_card_${order.orderId}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Order #${order.orderId}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            val df = SimpleDateFormat("K:mm a • d MMM", Locale.getDefault())
                            Text(
                                text = df.format(Date(order.orderTime)),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = order.medicineName + " x${order.quantity}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "From: ${order.pharmacyName} • Total Paid: PKR ${String.format("%.2f", order.totalPrice + order.deliveryFee)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        if (order.isEmergency) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    "PRIORITY EMERGENCY REQUEST",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        // Progress Step Indicators
                        val statusSteps = listOf("Pending", "Accepted", "Out for Delivery", "Delivered")
                        val currentStepIndex = statusSteps.indexOf(order.status).coerceAtLeast(0)

                        Text(
                            "Delivery Progress Status: ${order.status}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            statusSteps.forEachIndexed { idx, label ->
                                val active = idx <= currentStepIndex
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (idx < currentStepIndex) Icons.Default.TaskAlt else if (idx == currentStepIndex) Icons.Default.PlayCircleFilled else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = label,
                                        tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = label,
                                        fontSize = 9.sp,
                                        textAlign = TextAlign.Center,
                                        fontWeight = if (idx == currentStepIndex) FontWeight.Bold else FontWeight.Normal,
                                        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }

                        // Reorder capability
                        if (order.status == "Delivered") {
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { viewModel.reorder(order) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .testTag("reorder_button_${order.orderId}")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reorder", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reorder Item", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerCarePlusTab(viewModel: MediKartViewModel) {
    val reminders by viewModel.reminders.collectAsState()
    val familyMembers by viewModel.familyMembers.collectAsState()
    val premiumActive by viewModel.premiumActive.collectAsState()

    var medNameInput by remember { mutableStateOf("") }
    var reminderTime by remember { mutableStateOf("09:00 AM") }
    var reminderMember by remember { mutableStateOf("Self") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // MEDIKART PLUS subscription banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(2.dp, Color(0xFFFFB300)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("medikart_plus_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = "Plus Emblem",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "MediKart PLUS",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Unlimited care for only PKR 120 / Month",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (premiumActive) MaterialTheme.colorScheme.primary else Color(0xFFFFB300).copy(alpha = 0.2f)
                        ) {
                            Text(
                                if (premiumActive) "ACTIVE" else "JOIN",
                                color = if (premiumActive) Color.White else Color(0xFFFFB300),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    val benefits = listOf(
                        "Unlimited FREE delivery anywhere within Alipur Chatha",
                        "Priority order fulfillment (Direct track bypass)",
                        "Flat 10% instant discount on all medicines",
                        "Dedicated fast care health updates & priority support"
                    )
                    benefits.forEach { benefit ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 3.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(benefit, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { viewModel.togglePremium() },
                        colors = ButtonDefaults.buttonColors(containerColor = if (premiumActive) MaterialTheme.colorScheme.error else Color(0xFFFFB300)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("subscribe_plus_btn")
                    ) {
                        Text(
                            text = if (premiumActive) "Cancel Premium Subscription" else "Subscribe for PKR 120 / Month",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // FAMILY ACCOUNTS
        item {
            Text(
                "Family Accounts",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                "Add care members to dispatch medicines and track schedules together",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(familyMembers) { member ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(64.dp)
                            .testTag("family_member_avatar_$member")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    member.take(2).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = 16.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(member, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }

                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(64.dp)
                            .clickable { viewModel.showNotification("Simulation: Added mother-in-law to circle!") }
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Add, contentDescription = "Add Member", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Add New", fontSize = 11.sp)
                    }
                }
            }
        }

        // MEDICINE REMINDERS LIST
        item {
            Text(
                "Medicine Reminders",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (reminders.isEmpty()) {
            item {
                Text(
                    "No pending reminders scheduled. Stay healthy!",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        } else {
            items(reminders) { reminder ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessAlarm,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                reminder.medicineName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                "Time: ${reminder.time} • Schedule: ${reminder.frequency} • Target: ${reminder.personName}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.deleteReminder(reminder) },
                            modifier = Modifier.testTag("delete_reminder_${reminder.id}")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        // SCHEDULE NEW REMINDER FORM
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Add Health Alarm Alert", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = medNameInput,
                        onValueChange = { medNameInput = it },
                        label = { Text("Medicine (e.g. Panadol / Insulin)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reminder_medicine_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = reminderTime,
                        onValueChange = { reminderTime = it },
                        label = { Text("Alarm Time (e.g. 08:30 AM)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reminder_time_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Target family member tag:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        familyMembers.forEach { member ->
                            val active = reminderMember == member
                            FilterChip(
                                selected = active,
                                onClick = { reminderMember = member },
                                label = { Text(member, fontSize = 10.sp) },
                                modifier = Modifier.testTag("reminder_member_chip_$member")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (medNameInput.isNotBlank()) {
                                viewModel.addReminder(medNameInput, reminderTime, reminderMember)
                                medNameInput = ""
                            } else {
                                viewModel.showNotification("Enter medicine name first!")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_reminder_btn")
                    ) {
                        Text("Save Health Reminder", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// PHARMACY DASHBOARD ROLE INTERFACE
// -------------------------------------------------------------
@Composable
fun PharmacyDashboard(viewModel: MediKartViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Pending Orders", "Manage Inventory", "Pricing History Log")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    modifier = Modifier.testTag("pharmacy_tab_$index")
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> PharmacyOrdersTab(viewModel = viewModel)
                1 -> PharmacyInventoryTab(viewModel = viewModel)
                2 -> PharmacyPriceHistoryTab(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun PharmacyOrdersTab(viewModel: MediKartViewModel) {
    val orders by viewModel.orders.collectAsState()

    val pendingOrders = orders.filter { it.status == "Pending" || it.status == "Accepted" || it.status == "Out for Delivery" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Incoming Dispatch Orders",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                "Process orders and handoff to Alipur Chatha motorcycle delivery network",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        if (pendingOrders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = "No orders",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No orders pending right now. Good work!", fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(pendingOrders) { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pharmacy_order_item_${order.orderId}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Order ID: #${order.orderId}",
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (order.isEmergency) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = if (order.isEmergency) "EMERGENCY" else order.status,
                                    color = if (order.isEmergency) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = order.medicineName + " x${order.quantity}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Patient: ${order.customerName} (${order.customerPhone})", fontSize = 12.sp)
                        Text("Deliver to: ${order.deliveryAddress}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text("Payment: ${order.paymentMethod} • Total: PKR ${String.format("%.2f", order.totalPrice + order.deliveryFee)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                        if (order.prescriptionPhotoPath != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(10.dp)
                                ) {
                                    Icon(Icons.Default.VerifiedUser, contentDescription = "Encrypted RX", tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Prescription snapshot attached with order. Security verified.", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (order.status == "Pending") {
                                Button(
                                    onClick = { viewModel.updateOrderStatus(order, "Accepted") },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.testTag("pharmacy_accept_btn_${order.orderId}")
                                ) {
                                    Icon(Icons.Default.TaskAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Accept & Pack Items", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            } else if (order.status == "Accepted") {
                                Button(
                                    onClick = { viewModel.updateOrderStatus(order, "Out for Delivery") },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.testTag("pharmacy_rider_btn_${order.orderId}")
                                ) {
                                    Icon(Icons.Default.Motorcycle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Hand to Delivery Rider", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text(
                                    "Handed off. Rider is currently delivering...",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PharmacyInventoryTab(viewModel: MediKartViewModel) {
    val medicines by viewModel.medicines.collectAsState()

    var activeEditingMedId by remember { mutableStateOf<Int?>(null) }
    var priceEditInput by remember { mutableStateOf("") }
    var isAvailableState by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Verify & Live Price Control",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                "Automatic price revision updates customer screens in real-time instantly.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        items(medicines) { med ->
            val editing = activeEditingMedId == med.id

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pharmacy_inventory_item_${med.id}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(med.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Category: ${med.category} • Current: PKR ${med.price}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Surface(
                            color = if (med.availability) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                if (med.availability) "Online" else "In-Stock Red Alert",
                                color = if (med.availability) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (!editing) {
                        Button(
                            onClick = {
                                activeEditingMedId = med.id
                                priceEditInput = med.price.toString()
                                isAvailableState = med.availability
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                            modifier = Modifier
                                .align(Alignment.End)
                                .testTag("edit_price_btn_${med.id}")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Update Price & Availability", fontSize = 11.sp)
                        }
                    } else {
                        // PRICE EDIT FIELDS
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                .padding(10.dp)
                        ) {
                            OutlinedTextField(
                                value = priceEditInput,
                                onValueChange = { priceEditInput = it },
                                label = { Text("New Price (PKR)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("price_edit_field_${med.id}"),
                                singleLine = true
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isAvailableState,
                                    onCheckedChange = { isAvailableState = it },
                                    modifier = Modifier.testTag("available_check_${med.id}")
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("In Stock and Available for Alipur Chatha Delivery", fontSize = 12.sp)
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                TextButton(
                                    onClick = { activeEditingMedId = null },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Cancel", color = MaterialTheme.colorScheme.error)
                                }
                                Button(
                                    onClick = {
                                        val price = priceEditInput.toDoubleOrNull()
                                        if (price != null && price > 0) {
                                            viewModel.updateMedicinePriceAndAvailability(med.id, price, isAvailableState)
                                            activeEditingMedId = null
                                        } else {
                                            viewModel.showNotification("Invalid Price amount input!!")
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("save_price_btn_${med.id}")
                                ) {
                                    Text("Apply & Logs")
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
fun PharmacyPriceHistoryTab(viewModel: MediKartViewModel) {
    val logs by viewModel.priceLogs.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Price Audit & Compliance Log",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                "Admin records of pricing fluctuations to prevent local medication black-marketing",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (logs.isEmpty()) {
            item {
                Text(
                    "No pricing updates logged yet. Changes will be audited.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        } else {
            items(logs) { log ->
                val df = SimpleDateFormat("K:mm a • d MMM yyyy", Locale.getDefault())

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(log.medicineName, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text(df.format(Date(log.updatedAt)), fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        }
                        Text("Pharmacy: ${log.pharmacyName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text("Before: PKR ${log.oldPrice}", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                            Icon(Icons.Default.ArrowRight, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text("After: PKR ${log.newPrice}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// DELIVERY PARTNER (RIDER) ROLE INTERFACE
// -------------------------------------------------------------
@Composable
fun RiderDashboard(viewModel: MediKartViewModel) {
    val orders by viewModel.orders.collectAsState()
    val currentDeliveryOrder by viewModel.currentDeliveryOrder.collectAsState()
    val gpsLocation by viewModel.simulatedGpsLocation.collectAsState()

    // Filter orders eligible to deliver (e.g. accepted and not yet finished)
    val openJobs = orders.filter { it.status == "Accepted" || it.status == "Out for Delivery" }
    // Simulated Earnings calculations: completed deliveries * fee (PKR 40 per delivery)
    val completedDeliveriesCount = orders.filter { it.status == "Delivered" }.size
    val totalEarnings = completedDeliveriesCount * 40.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // EARNINGS CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rider_earnings_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Rider Partner Dashboard",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "PKR ${String.format("%.2f", totalEarnings)}",
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 28.sp
                    )
                    Text(
                        "Lifetime Deliveries: $completedDeliveriesCount • Guarantee PKR 40 flat fee",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // SIMULATED ACTIVE NAVIGATION UNIT
        if (currentDeliveryOrder != null) {
            val order = currentDeliveryOrder!!
            item {
                Card(
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().testTag("rider_active_job_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Navigation, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ACTIVE DISPATCH DELIVERY", fontWeight = FontWeight.Black, fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(order.medicineName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Pickup from: ${order.pharmacyName}", fontSize = 12.sp)
                        Text("Destination: ${order.deliveryAddress}", fontSize = 12.sp)
                        Text("Customer Contact: ${order.customerPhone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        Text("SIMULATED GPS NAVIGATION LOGS", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.DirectionsRun, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Rider Current Location: $gpsLocation", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        }

                        // Navigation map timeline layout
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.advanceDeliverySimulation() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sim_advance_location_btn_rider")
                        ) {
                            Text(
                                text = when (gpsLocation) {
                                    "Pharmacy (${order.pharmacyName})" -> "Start Ride (Out for Delivery)"
                                    "Transit (G.T. Road / Near Municipal Committee)" -> "Arrive at customer address"
                                    else -> "Complete Delivery & collect Cash"
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // ELIGIBLE OPEN DELIVERY RIDE JOBS
        item {
            Text(
                "Available Medicine Delivery Jobs",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        val deliverableJobs = openJobs.filter { currentDeliveryOrder?.orderId != it.orderId }

        if (deliverableJobs.isEmpty()) {
            item {
                Text(
                    "All pending local medicine shipments have been picked up. Standby on alert!",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        } else {
            items(deliverableJobs) { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rider_job_offer_${order.orderId}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Job ID: #${order.orderId}", fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    "Fee: PKR 40.00",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Client: ${order.customerName}", fontWeight = FontWeight.SemiBold)
                        Text("Pickup: ${order.pharmacyName}", fontSize = 11.sp)
                        Text("Deliver to: ${order.deliveryAddress}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.selectDeliveryOrder(order) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .align(Alignment.End)
                                .testTag("rider_accept_job_btn_${order.orderId}")
                        ) {
                            Text("Accept Delivery Request", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// ADMINISTRATOR ROLE CONTROL PANEL
// -------------------------------------------------------------
@Composable
fun AdminDashboard(viewModel: MediKartViewModel) {
    val pharmacies by viewModel.pharmacies.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val priceLogs by viewModel.priceLogs.collectAsState()
    val premiumActive by viewModel.premiumActive.collectAsState()

    var customBroadcastText by remember { mutableStateOf("") }

    val totalSalesVolume = orders.filter { it.status == "Delivered" }.map { it.totalPrice + it.deliveryFee }.sum()
    val complaintsCounter = 2 // Simulated hard complaints tickets

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Admin Operations Tower",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                "Oversee pharmacies verification, platform metrics, and push broadcast systems",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // METRICS COUNTERS
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("TOTAL REVENUE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("PKR ${String.format("%.1f", totalSalesVolume)}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("TOTAL ORDERS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("${orders.size}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("COMPLAINTS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("$complaintsCounter", fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        // DYNAMIC DIRECT NOTIFICATION SYSTEM
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Trigger Global Push Notification", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Pushes critical emergency messages or promotional warnings to all customer phones in Alipur Chatha.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customBroadcastText,
                        onValueChange = { customBroadcastText = it },
                        label = { Text("Broadcast Alert (e.g. Pharmacy accounts verified!)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_broadcast_input_tf"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (customBroadcastText.isNotBlank()) {
                                viewModel.triggerBroadcastNotification(customBroadcastText)
                                customBroadcastText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_trigger_broadcast_btn")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Broadcast Live Banner Notice")
                    }
                }
            }
        }

        // PHARMACY VERIFICATION SECTION
        item {
            Text(
                "Verify & Appraise Pharmacy Credentials",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                "Unverified pharmacies cannot accept prescription uploads on production",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        items(pharmacies) { pharmacy ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_pharmacy_card_${pharmacy.id}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(pharmacy.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            if (pharmacy.isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Verified, contentDescription = "Verified Badged", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            }
                        }
                        Text("Address: ${pharmacy.address}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text("Phone: ${pharmacy.contactNo} • Rating: ${pharmacy.rating}★", fontSize = 11.sp)
                    }
                    Switch(
                        checked = pharmacy.isVerified,
                        onCheckedChange = { viewModel.togglePharmacyVerification(pharmacy) },
                        modifier = Modifier.testTag("admin_verify_switch_${pharmacy.id}")
                    )
                }
            }
        }
    }
}
