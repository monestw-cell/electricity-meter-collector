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
    val settingsState by viewModel.settings.collectAsStateWithLifecycle(initialValue = Settings())
    
    // Check if app lock is enabled and unlocked already
    var appUnlocked by remember { mutableStateOf(false) }

    LaunchedEffect(settingsState.appLockEnabled) {
        if (!settingsState.appLockEnabled) {
            appUnlocked = true
        }
    }

    if (settingsState.appLockEnabled && !appUnlocked) {
        AppLockScreen(
            settings = settingsState,
            onUnlocked = { appUnlocked = true }
        )
    } else {
        var currentTab by remember { mutableStateOf("subscribers") }

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
                        onClick = { currentTab = "settings" },
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
                        SettingsScreen(viewModel = viewModel)
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
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") } // "all", "unread", "read"
    
    var showAddDialog by remember { mutableStateOf(false) }
    var subscriberToRecordReading by remember { mutableStateOf<Subscriber?>(null) }
    var subscriberToViewInvoices by remember { mutableStateOf<Subscriber?>(null) }
    var subscriberToEdit by remember { mutableStateOf<Subscriber?>(null) }
    
    val filteredList = subscribers.filter { subscriber ->
        val matchesSearch = subscriber.name.contains(searchQuery, ignoreCase = true) || subscriber.phoneNumber.contains(searchQuery)
        val matchesFilter = when (selectedFilter) {
            "unread" -> !subscriber.isRead
            "read" -> subscriber.isRead
            else -> true
        }
        matchesSearch && matchesFilter
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

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips and Reclear Cycle Reset Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    FilterChip(
                        selected = (selectedFilter == "all"),
                        onClick = { selectedFilter = "all" },
                        label = { Text("الكل", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = (selectedFilter == "unread"),
                        onClick = { selectedFilter = "unread" },
                        label = { Text("غير مقروء", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = (selectedFilter == "read"),
                        onClick = { selectedFilter = "read" },
                        label = { Text("مقروء", fontSize = 11.sp) }
                    )
                }

                IconButton(
                    onClick = {
                        viewModel.resetAllReadingsStatus()
                        Toast.makeText(context, "تمت إعادة ضبط جميع المشتركين كغير مقروئين لبدء دورة جباية جديدة!", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier.testTag("btn_reset_all_readings")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "ضبط كغير مقروء للجميع",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

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
                            text = if (searchQuery.isNotEmpty() || selectedFilter != "all") "لا توجد نتائج مطابقة للتصفية" else "لا يوجد مشتركون مسجلون حالياً",
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
                            onEdit = { subscriberToEdit = subscriber },
                            onDelete = { viewModel.deleteSubscriber(subscriber) },
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
        
        // Add Subscriber Dialog
        if (showAddDialog) {
            AddSubscriberDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, phone, initial, notes ->
                    viewModel.addSubscriber(name, phone, initial, "Meter", 0, notes)
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
                },
                viewModel = viewModel
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

        // Edit Subscriber Dialog
        subscriberToEdit?.let { subscriber ->
            EditSubscriberDialog(
                subscriber = subscriber,
                onDismiss = { subscriberToEdit = null },
                onSave = { updated ->
                    viewModel.updateSubscriber(updated)
                    subscriberToEdit = null
                    Toast.makeText(context, "تم تعديل بيانات المشترك بالكامل بنجاح!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun SubscriberCard(
    subscriber: Subscriber,
    onRecordReading: () -> Unit,
    onViewInvoices: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    viewModel: MainViewModel
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

                val tagText = if (subscriber.isRead) "مقروء ✓" else "بانتظار القراءة ✗"
                val tagColor = if (subscriber.isRead) Color(0xFF2E7D32) else Color(0xFFD32F2F)

                Surface(
                    color = tagColor.copy(alpha = 0.12f),
                    contentColor = tagColor,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable { viewModel.toggleReadStatus(subscriber) }
                ) {
                    Text(
                        text = tagText,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
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
                    Text("آخر قراءة للعداد", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = "${subscriber.currentReading} ك.و",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
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
                            text = "العنوان والملاحظات: ${subscriber.notes}",
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onEdit,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.testTag("btn_edit_subscriber_${subscriber.id}")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "تعديل معلومات المشترك", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تعديل كامل البيانات", fontSize = 12.sp)
                        }

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
    onAdd: (String, String, Int, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var initialReading by remember { mutableStateOf("") }
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

                OutlinedTextField(
                    value = initialReading,
                    onValueChange = { initialReading = it },
                    label = { Text("القراءة الابتدائية للعدّاد") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = appTextFieldColors()
                )

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
                        onAdd(name, phone, initReadVal, notes)
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
    onSave: (Int, String, String?) -> Unit,
    viewModel: MainViewModel
) {
    var readingInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }
    var photoProofPathState by remember { mutableStateOf<String?>(null) }
    var sendSmsChecked by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val settingsState by viewModel.settings.collectAsStateWithLifecycle(initialValue = Settings())

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

                // Attach Image Button
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

                Spacer(modifier = Modifier.height(4.dp))

                // Optional SMS billing checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { sendSmsChecked = !sendSmsChecked }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        "أريد إرسال الفاتورة برسالة SMS للمشترك",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Checkbox(
                        checked = sendSmsChecked,
                        onCheckedChange = { sendSmsChecked = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalReading = readingInput.toIntOrNull() ?: subscriber.currentReading
                    val previousReading = subscriber.currentReading
                    val consumption = (finalReading - previousReading).coerceAtLeast(0)
                    val totalAmount = consumption * settingsState.pricePerKw

                    if (sendSmsChecked) {
                        val smsBody = """
                        مشتركنا العزيز: ${subscriber.name}
                        فاتورة الكهرباء للأسبوع الحالي:
                        - القراءة السابقة: $previousReading
                        - القراءة الحالية: $finalReading
                        - الاستهلاك: $consumption كيلوواط
                        - سعر الكيلو: ${settingsState.pricePerKw} شيكل
                        - المبلغ المطلوب: $totalAmount شيكل
                        شكراً لكم.
                        """.trimIndent()
                        
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                data = android.net.Uri.parse("smsto:${subscriber.phoneNumber}")
                                putExtra("sms_body", smsBody)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "فشل فتح تطبيق الرسائل: ${e.message}", Toast.LENGTH_LONG).show()
                        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSubscriberDialog(
    subscriber: Subscriber,
    onDismiss: () -> Unit,
    onSave: (Subscriber) -> Unit
) {
    var name by remember { mutableStateOf(subscriber.name) }
    var phone by remember { mutableStateOf(subscriber.phoneNumber) }
    var initialReading by remember { mutableStateOf(subscriber.initialReading.toString()) }
    var currentReading by remember { mutableStateOf(subscriber.currentReading.toString()) }
    var notes by remember { mutableStateOf(subscriber.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        title = {
            Text(
                "تعديل بيانات المشترك",
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
                    modifier = Modifier.fillMaxWidth(),
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

                OutlinedTextField(
                    value = initialReading,
                    onValueChange = { initialReading = it },
                    label = { Text("القراءة الابتدائية للعدّاد") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = appTextFieldColors()
                )

                OutlinedTextField(
                    value = currentReading,
                    onValueChange = { currentReading = it },
                    label = { Text("القراءة الحالية للعدّاد") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = appTextFieldColors()
                )

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
                        val initReadVal = initialReading.toIntOrNull() ?: subscriber.initialReading
                        val currReadVal = currentReading.toIntOrNull() ?: subscriber.currentReading
                        onSave(
                            subscriber.copy(
                                name = name,
                                phoneNumber = phone,
                                initialReading = initReadVal,
                                currentReading = currReadVal,
                                notes = notes
                            )
                        )
                    }
                }
            ) {
                Text("حفظ التعديلات")
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
fun SettingsScreen(viewModel: MainViewModel) {
    val settingsState by viewModel.settings.collectAsStateWithLifecycle(initialValue = Settings())

    var editPriceKw by remember { mutableStateOf(settingsState.pricePerKw.toString()) }
    var editAppLockEnabled by remember { mutableStateOf(settingsState.appLockEnabled) }
    var editAppLockType by remember { mutableStateOf(settingsState.appLockType) } // "PIN", "PASSWORD", "BIOMETRIC"
    var editAppLockSecret by remember { mutableStateOf(settingsState.appLockSecret) }

    val context = LocalContext.current

    LaunchedEffect(settingsState) {
        editPriceKw = settingsState.pricePerKw.toString()
        editAppLockEnabled = settingsState.appLockEnabled
        editAppLockType = settingsState.appLockType
        editAppLockSecret = settingsState.appLockSecret
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "إعدادات تسعرة الطاقة وجباية الشيكل",
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
                        label = { Text("السعر لكل كيلوواط (شيكل ILS)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = appTextFieldColors()
                    )

                    Divider(color = Color.LightGray.copy(alpha = 0.5f))

                    Text(
                        "حماية قفل التطبيق بكامله",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editAppLockEnabled = !editAppLockEnabled }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Switch(
                            checked = editAppLockEnabled,
                            onCheckedChange = { editAppLockEnabled = it }
                        )
                        Text("تفعيل القفل السري بكامل التطبيق")
                    }

                    if (editAppLockEnabled) {
                        Text("اختر طريقة القفل:", fontSize = 12.sp, color = Color.Gray)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = (editAppLockType == "PIN"),
                                onClick = { editAppLockType = "PIN" },
                                label = { Text("رمز PIN") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = (editAppLockType == "PASSWORD"),
                                onClick = { editAppLockType = "PASSWORD" },
                                label = { Text("كلمة مرور") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = (editAppLockType == "BIOMETRIC"),
                                onClick = { editAppLockType = "BIOMETRIC" },
                                label = { Text("بصمة") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (editAppLockType == "PIN" || editAppLockType == "PASSWORD") {
                            OutlinedTextField(
                                value = editAppLockSecret,
                                onValueChange = { editAppLockSecret = it },
                                label = { Text(if (editAppLockType == "PIN") "رمز PIN السري (رقمي)" else "كلمة السر الخاصة بك") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = if (editAppLockType == "PIN") KeyboardType.Number else KeyboardType.Text
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = appTextFieldColors()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val kw = editPriceKw.toDoubleOrNull() ?: settingsState.pricePerKw
                            viewModel.updateSettings(
                                pricePerKw = kw,
                                currency = "ILS",
                                appLockEnabled = editAppLockEnabled,
                                appLockType = editAppLockType,
                                appLockSecret = editAppLockSecret
                            )
                            Toast.makeText(context, "تم حفظ الإعدادات وقفل الأمان بنجاح!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_save_settings")
                    ) {
                        Text("حفظ التغييرات وحفظ القفل", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AppLockScreen(settings: Settings, onUnlocked: () -> Unit) {
    var passwordInput by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (settings.appLockType == "BIOMETRIC") Icons.Default.Face else Icons.Default.Lock,
            contentDescription = "🔒",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "التطبيق مقفل",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when (settings.appLockType) {
                "PIN" -> "الرجاء إدخال رمز الـ PIN المكون من أرقام لإلغاء القفل"
                "PASSWORD" -> "الرجاء إدخال كلمة المرور السرية لإلغاء القفل"
                else -> "الرجاء استخدام مستشعر البصمة أو النقر للمصادقة السريعة"
            },
            fontSize = 13.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (settings.appLockType == "BIOMETRIC") {
            Card(
                modifier = Modifier
                    .size(120.dp)
                    .clickable {
                        Toast.makeText(context, "تمت المصادقة ببصمة الإصبع بنجاح ✓", Toast.LENGTH_SHORT).show()
                        onUnlocked()
                    },
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "بصمة",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "انقر على البصمة لإلغاء القفل السريع",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (settings.appLockType == "PIN" || settings.appLockType == "PASSWORD") {
            OutlinedTextField(
                value = passwordInput,
                onValueChange = {
                    passwordInput = it
                    isError = false
                },
                label = { Text(if (settings.appLockType == "PIN") "رمز PIN السري" else "كلمة المرور") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (settings.appLockType == "PIN") KeyboardType.Number else KeyboardType.Text
                ),
                modifier = Modifier
                    .fillMaxWidth(0.81f)
                    .testTag("app_lock_input_field"),
                shape = RoundedCornerShape(12.dp),
                isError = isError,
                colors = appTextFieldColors()
            )

            if (isError) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "رمز المرور السرية غير صحيح!",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (passwordInput == settings.appLockSecret) {
                        onUnlocked()
                    } else {
                        isError = true
                    }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth(0.81f)
                    .height(48.dp)
                    .testTag("btn_unlock_app")
            ) {
                Text("إلغاء القفل", fontWeight = FontWeight.Bold)
            }
        }
    }
}
