package com.example.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.InvoiceHistory
import com.example.data.model.Subscriber
import com.example.data.model.Settings
import java.util.UUID

@Composable
fun appTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
)

@Composable
fun AppContent(viewModel: MainViewModel) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf("subscribers") }
    
    // Check if configuration settings are pin locked
    val settingsState by viewModel.settings.collectAsStateWithLifecycle(initialValue = Settings())
    var pinAuthenticated by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(top = 36.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "عَدَّاد الجباية الذكي",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.testTag("app_title")
                    )
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Logo",
                        tint = Color.Yellow,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "جباية وإدارة المولدات الكهربائية دون إنترنت",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                NavigationBarItem(
                    selected = (currentTab == "subscribers"),
                    onClick = { currentTab = "subscribers" },
                    icon = { Icon(Icons.Default.AccountCircle, "المشتركين") },
                    label = { Text("المشتركون", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_subscribers")
                )
                NavigationBarItem(
                    selected = (currentTab == "stats"),
                    onClick = { currentTab = "stats" },
                    icon = { Icon(Icons.Default.Info, "الإحصائيات") },
                    label = { Text("المالية والأرقام", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_stats")
                )
                NavigationBarItem(
                    selected = (currentTab == "settings"),
                    onClick = { 
                        // If pin is set, reset authentication state so they must re-enter it when switching back
                        if (settingsState.pinCode.isNotEmpty()) {
                            pinAuthenticated = false
                        }
                        currentTab = "settings" 
                    },
                    icon = { Icon(Icons.Default.Settings, "الإعدادات") },
                    label = { Text("الإعدادات", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_settings")
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentTab) {
                "subscribers" -> SubscribersScreen(viewModel = viewModel)
                "stats" -> FinancialDashboard(viewModel = viewModel)
                "settings" -> {
                    if (settingsState.pinCode.isNotEmpty() && !pinAuthenticated) {
                        PinLockScreen(
                            correctPin = settingsState.pinCode,
                            onAuthorized = { pinAuthenticated = true }
                        )
                    } else {
                        SettingsScreen(viewModel = viewModel, onLockSettingsAgain = { pinAuthenticated = false })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscribersScreen(viewModel: MainViewModel) {
    val subscribers by viewModel.allSubscribers.collectAsStateWithLifecycle(initialValue = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    
    var showAddDialog by remember { mutableStateOf(false) }
    var subscriberToRecordReading by remember { mutableStateOf<Subscriber?>(null) }
    var subscriberToViewInvoices by remember { mutableStateOf<Subscriber?>(null) }
    
    val filteredList = subscribers.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.phoneNumber.contains(searchQuery)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Search Bar and Add button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ابحث عن مشترك بالاسم أو الرقم...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_subscriber_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = appTextFieldColors()
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .height(56.dp)
                        .testTag("btn_trigger_add_subscriber"),
                    contentPadding = PaddingValues(horizontal = 14.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "لا يوجد",
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "لا توجد نتائج مطابقة للبحث" else "لا يوجد مشتركون مسجلون حالياً",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList) { subscriber ->
                        SubscriberCard(
                            subscriber = subscriber,
                            onRecordReading = { subscriberToRecordReading = subscriber },
                            onViewInvoices = { subscriberToViewInvoices = subscriber },
                            onDelete = { viewModel.deleteSubscriber(subscriber) }
                        )
                    }
                }
            }
        }
        
        // Add Subscriber Dialog
        if (showAddDialog) {
            AddSubscriberDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, phone, initial, type, amps, notes ->
                    viewModel.addSubscriber(name, phone, initial, type, amps, notes)
                    showAddDialog = false
                }
            )
        }
        
        // Record Reading Dialog
        subscriberToRecordReading?.let { subscriber ->
            RecordReadingDialog(
                subscriber = subscriber,
                onDismiss = { subscriberToRecordReading = null },
                onSave = { reading, note, path ->
                    viewModel.saveReading(subscriber.id, reading, note, path)
                    subscriberToRecordReading = null
                }
            )
        }
        
        // View Invoices Dialog
        subscriberToViewInvoices?.let { subscriber ->
            InvoicesForSubscriberDialog(
                subscriber = subscriber,
                viewModel = viewModel,
                onDismiss = { subscriberToViewInvoices = null }
            )
        }
    }
}

@Composable
fun SubscriberCard(
    subscriber: Subscriber,
    onRecordReading: () -> Unit,
    onViewInvoices: () -> Unit,
    onDelete: () -> Unit
) {
    var showFullMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showFullMenu = !showFullMenu }
            .testTag("subscriber_card_${subscriber.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = subscriber.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (subscriber.phoneNumber.isNotEmpty()) subscriber.phoneNumber else "لا يوجد رقم هاتف",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                val typeLabel = if (subscriber.subscriptionType == "Meter") "عدّاد كيلواط" else "اشتراك أمبير سلك"
                val typeColor = if (subscriber.subscriptionType == "Meter") 
                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

                Surface(
                    color = typeColor.copy(alpha = 0.1f),
                    contentColor = typeColor,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = typeLabel,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("آخر قراءة مسجلة", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = if (subscriber.subscriptionType == "Meter") "${subscriber.currentReading} ك.و" else "مستمر",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (subscriber.subscriptionType == "Amperage") {
                    Column {
                        Text("حجم السلك", fontSize = 11.sp, color = Color.Gray)
                        Text("${subscriber.amperage} أمبير", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onRecordReading,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("btn_record_reading_${subscriber.id}")
                    ) {
                        Text("تسجيل قراءة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onViewInvoices,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("btn_view_invoices_${subscriber.id}")
                    ) {
                        Text("الفواتير", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Expanded extra menu actions (Delete & Details)
            AnimatedVisibility(visible = showFullMenu) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Divider(color = Color.LightGray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))
                    if (subscriber.notes.isNotEmpty()) {
                        Text(
                            text = "ملاحظات: ${subscriber.notes}",
                            fontSize = 12.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red),
                            modifier = Modifier.testTag("btn_delete_subscriber_${subscriber.id}")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف المشترك", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("حذف المشترك", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriberDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, Int, String, Int, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var initialReading by remember { mutableStateOf("") }
    var subscriptionType by remember { mutableStateOf("Meter") } // "Meter" or "Amperage"
    var amperage by remember { mutableStateOf("5") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        title = {
            Text(
                "إضافة مشترك جديد",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.End
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المشترك") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_name_field"),
                    shape = RoundedCornerShape(10.dp),
                    colors = appTextFieldColors()
                )
                
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = appTextFieldColors()
                )

                // Selection for sub type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = (subscriptionType == "Amperage"),
                        onClick = { subscriptionType = "Amperage" },
                        label = { Text("أمبير سلك") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = (subscriptionType == "Meter"),
                        onClick = { subscriptionType = "Meter" },
                        label = { Text("عدّاد منزلي") },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (subscriptionType == "Meter") {
                    OutlinedTextField(
                        value = initialReading,
                        onValueChange = { initialReading = it },
                        label = { Text("القراءة الابتدائية للعدّاد") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = appTextFieldColors()
                    )
                } else {
                    OutlinedTextField(
                        value = amperage,
                        onValueChange = { amperage = it },
                        label = { Text("الأمبيرات المتعاقد عليها") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = appTextFieldColors()
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات ميدانية/العنوان") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = appTextFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.trim().isNotEmpty()) {
                        val initReadVal = initialReading.toIntOrNull() ?: 0
                        val ampVal = amperage.toIntOrNull() ?: 5
                        onAdd(name, phone, initReadVal, subscriptionType, ampVal, notes)
                    }
                },
                modifier = Modifier.testTag("btn_save_new_subscriber")
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordReadingDialog(
    subscriber: Subscriber,
    onDismiss: () -> Unit,
    onSave: (Int, String, String?) -> Unit
) {
    var readingInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }
    var photoProofPathState by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current

    val photoSelectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            photoProofPathState = uri.toString()
            Toast.makeText(context, "تم تحديد رابط صورة الإثبات بنجاح", Toast.LENGTH_SHORT).show()
        }
    }

    val noteSuggestions = listOf("عداد سليم", "مغلق", "لا يوجد أحد", "تجاوز", "ملغي")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        title = {
            Text(
                text = "تسجيل قراءة لـ ${subscriber.name}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.End
            ) {
                if (subscriber.subscriptionType == "Meter") {
                    Text(
                        "آخر قراءة للعداد: ${subscriber.currentReading} ك.و",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )
                    OutlinedTextField(
                        value = readingInput,
                        onValueChange = { readingInput = it },
                        label = { Text("القراءة الحالية الجديدة") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reading_field_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = appTextFieldColors()
                    )
                } else {
                    Text(
                        "اشتراك أمبير سلك مستمر (${subscriber.amperage} أمبير)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )
                }

                // Field Notes Suggestion clickable row
                Text("ملاحظات ميدانية سريعة:", fontSize = 11.sp, color = Color.Gray)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    noteSuggestions.forEach { suggestion ->
                        SuggestionChip(
                            onClick = { notesInput = suggestion },
                            label = { Text(suggestion, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("كتابة ملاحظة") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = appTextFieldColors()
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Capture image verification attachment mock button
                Button(
                    onClick = { photoSelectionLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "أرفق صورة")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (photoProofPathState == null) "إرفق صورة للعداد" else "تم إرفاق صورة العداد ✓", fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalReading = if (subscriber.subscriptionType == "Meter") {
                        readingInput.toIntOrNull() ?: subscriber.currentReading
                    } else {
                        subscriber.currentReading
                    }
                    onSave(finalReading, notesInput, photoProofPathState)
                },
                modifier = Modifier.testTag("btn_confirm_save_reading")
            ) {
                Text("تسجيل القراءة والفاتورة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun InvoicesForSubscriberDialog(
    subscriber: Subscriber,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val invoices: List<InvoiceHistory> by viewModel.getInvoicesForSubscriber(subscriber.id).collectAsState(initial = emptyList())
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        title = {
            Text(
                text = "فواتير المشترك: ${subscriber.name}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                if (invoices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("لا تتوفر فواتير مسجلة لهذا المشترك", fontSize = 13.sp, color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(invoices) { invoice ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${invoice.totalAmount} دولار",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = invoice.date,
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    
                                    if (subscriber.subscriptionType == "Meter") {
                                        Text(
                                            text = "القراءة: ${invoice.previousReading} ➔ ${invoice.currentReading} (${invoice.consumption} كيلوواط)",
                                            fontSize = 12.sp
                                        )
                                    } else {
                                        Text(
                                            text = "فترة الأمبيرية الحالية (${subscriber.amperage} أمبير)",
                                            fontSize = 12.sp
                                        )
                                    }

                                    if (invoice.notes.isNotEmpty()) {
                                        Text(
                                            text = "ملاحظة: ${invoice.notes}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (invoice.isPaid) {
                                            Text(
                                                "✓ تم الدفع والجباية",
                                                color = Color(0xFF2E7D32),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        } else {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Button(
                                                    onClick = { viewModel.payInvoice(invoice) },
                                                    shape = RoundedCornerShape(6.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                                    modifier = Modifier
                                                        .height(30.dp)
                                                        .testTag("pay_invoice_btn_${invoice.id}")
                                                ) {
                                                    Text("تسجيل دفع", fontSize = 10.sp, color = Color.White)
                                                }

                                                TextButton(
                                                    onClick = { viewModel.deleteInvoice(invoice) },
                                                    modifier = Modifier.height(30.dp),
                                                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                                                ) {
                                                    Text("حذف بالفاتورة", fontSize = 10.sp)
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
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق")
            }
        }
    )
}

@Composable
fun FinancialDashboard(viewModel: MainViewModel) {
    val subscribers by viewModel.allSubscribers.collectAsStateWithLifecycle(initialValue = emptyList())
    val invoices by viewModel.allInvoices.collectAsStateWithLifecycle(initialValue = emptyList())
    val settingsState by viewModel.settings.collectAsStateWithLifecycle(initialValue = Settings())

    val totalKw = invoices.sumOf { it.consumption }
    val totalAmountDue = invoices.sumOf { it.totalAmount }
    val totalInvoicesPaid = invoices.filter { it.isPaid }.sumOf { it.totalAmount }
    val totalRemainingDues = totalAmountDue - totalInvoicesPaid

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("إجمالي المبالغ والتحصيل كليا", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$totalAmountDue ${settingsState.currency}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DashboardStatCard(
                    title = "مبالغ تم جبايتها",
                    value = "$totalInvoicesPaid ${settingsState.currency}",
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
                DashboardStatCard(
                    title = "مبالغ مستحقة",
                    value = "$totalRemainingDues ${settingsState.currency}",
                    icon = Icons.Default.Warning,
                    color = Color(0xFFC62828),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("معلومات الاستهلاك العام", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("إجمالي الاستهلاك الكهربائي", fontSize = 13.sp, color = Color.Gray)
                        Text("$totalKw ك.و", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("عدد المشتركين الكلي", fontSize = 13.sp, color = Color.Gray)
                        Text("${subscribers.size} مشتركين", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Mock chart progression
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("نسبة التحصيل والتحقق المكتمل", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    val ratioFraction = if (totalAmountDue > 0) (totalInvoicesPaid / totalAmountDue).toFloat() else 0f
                    val percentage = (ratioFraction * 100).toInt()

                    LinearProgressIndicator(
                        progress = ratioFraction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("اكتمل بنسبة", fontSize = 12.sp, color = Color.Gray)
                        Text("$percentage%", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 11.sp, color = Color.Gray)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun SettingsScreen(viewModel: MainViewModel, onLockSettingsAgain: () -> Unit) {
    val settingsState by viewModel.settings.collectAsStateWithLifecycle(initialValue = Settings())

    var editPriceKw by remember { mutableStateOf(settingsState.pricePerKw.toString()) }
    var editPriceAmp by remember { mutableStateOf(settingsState.pricePerAmp.toString()) }
    var editFixedFee by remember { mutableStateOf(settingsState.fixedFee.toString()) }
    var editPinCode by remember { mutableStateOf(settingsState.pinCode) }

    val context = LocalContext.current

    LaunchedEffect(settingsState) {
        editPriceKw = settingsState.pricePerKw.toString()
        editPriceAmp = settingsState.pricePerAmp.toString()
        editFixedFee = settingsState.fixedFee.toString()
        editPinCode = settingsState.pinCode
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "إعدادات تسعرة الطاقة والجباية",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = editPriceKw,
                        onValueChange = { editPriceKw = it },
                        label = { Text("السعر لكل كيلوواط (دولار)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = appTextFieldColors()
                    )

                    OutlinedTextField(
                        value = editPriceAmp,
                        onValueChange = { editPriceAmp = it },
                        label = { Text("سعر سلك الأمبير الواحد شهريا") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = appTextFieldColors()
                    )

                    OutlinedTextField(
                        value = editFixedFee,
                        onValueChange = { editFixedFee = it },
                        label = { Text("الرسوم الثابتة أو الفنية المضافة") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = appTextFieldColors()
                    )

                    OutlinedTextField(
                        value = editPinCode,
                        onValueChange = { editPinCode = it },
                        label = { Text("رمز القفل السري للوصول للإعدادات (PIN)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("اتركه فارغاً لإلغاء القفل") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pin_code_setting_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = appTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val kw = editPriceKw.toDoubleOrNull() ?: settingsState.pricePerKw
                            val amp = editPriceAmp.toDoubleOrNull() ?: settingsState.pricePerAmp
                            val fixed = editFixedFee.toDoubleOrNull() ?: settingsState.fixedFee
                            viewModel.updateSettings(kw, amp, fixed, editPinCode)
                            Toast.makeText(context, "تم حفظ الإعدادات بنجاح!", Toast.LENGTH_SHORT).show()
                            onLockSettingsAgain()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_save_settings")
                    ) {
                        Text("حفظ التغييرات وحظره", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PinLockScreen(correctPin: String, onAuthorized: () -> Unit) {
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "🔒",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "الإعدادات محمية بقفل سري",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "الرجاء إدخال الرمز السري للوصول وتعديل الأسعار",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = enteredPin,
            onValueChange = { 
                enteredPin = it
                isError = false
            },
            label = { Text("رمز PIN السري") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .width(200.dp)
                .testTag("entered_pin_field"),
            shape = RoundedCornerShape(12.dp),
            isError = isError,
            colors = appTextFieldColors()
        )

        if (isError) {
            Spacer(modifier = Modifier.height(4.dp))
            Text("الرمز السري غير صحيح!", color = Color.Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (enteredPin == correctPin) {
                    onAuthorized()
                } else {
                    isError = true
                    enteredPin = ""
                }
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .width(160.dp)
                .testTag("btn_verify_pin")
        ) {
            Text("فتح القفل", fontWeight = FontWeight.Bold)
        }
    }
}
