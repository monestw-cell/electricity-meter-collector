package com.example.ui

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.InvoiceHistory
import com.example.data.model.Settings
import com.example.data.model.Subscriber
import java.io.File

fun Double.formatAmount(): String {
    return if (this % 1.0 == 0.0) {
        String.format(java.util.Locale.US, "%.0f", this)
    } else {
        val formatted = String.format(java.util.Locale.US, "%.2f", this)
        if (formatted.endsWith(".00")) {
            formatted.substring(0, formatted.length - 3)
        } else if (formatted.endsWith("0")) {
            formatted.substring(0, formatted.length - 1)
        } else {
            formatted
        }
    }
}

@Composable
fun appTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.6f),
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = Color.Gray
)

@Composable
fun AppContent(viewModel: MainViewModel) {
    var currentTab by remember { mutableStateOf("subscribers") }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(top = 36.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "عَدَّاد الجباية الذكي",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.testTag("app_title")
                        )
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Logo",
                            tint = Color.Yellow,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "جباية وإدارة المولدات الكهربائية دون إنترنت",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = (currentTab == "subscribers"),
                        onClick = { currentTab = "subscribers" },
                        icon = { Icon(Icons.Default.AccountCircle, "المشتركين") },
                        label = { Text("المشتركون", fontSize = 11.sp) },
                        modifier = Modifier.testTag("tab_subscribers"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                    NavigationBarItem(
                        selected = (currentTab == "stats"),
                        onClick = { currentTab = "stats" },
                        icon = { Icon(Icons.Default.Info, "المالية") },
                        label = { Text("المالية والأرقام", fontSize = 11.sp) },
                        modifier = Modifier.testTag("tab_stats"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                    NavigationBarItem(
                        selected = (currentTab == "settings"),
                        onClick = { currentTab = "settings" },
                        icon = { Icon(Icons.Default.Settings, "الإعداد") },
                        label = { Text("الإعدادات", fontSize = 11.sp) },
                        modifier = Modifier.testTag("tab_settings"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
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
                    "settings" -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun SubscribersScreen(viewModel: MainViewModel) {
    val subscribers by viewModel.subscribers.collectAsStateWithLifecycle(initialValue = emptyList())
    var searchText by remember { mutableStateOf("") }
    
    var showAddDialog by remember { mutableStateOf(false) }
    var subscriberToEdit by remember { mutableStateOf<Subscriber?>(null) }
    var subscriberToDelete by remember { mutableStateOf<Subscriber?>(null) }
    var subscriberToRecord by remember { mutableStateOf<Subscriber?>(null) }
    var subscriberHistoryToView by remember { mutableStateOf<Subscriber?>(null) }

    var filterType by remember { mutableStateOf("all") } // "all", "read", "unread"

    val filteredSubscribers = remember(subscribers, searchText, filterType) {
        subscribers.filter {
            val matchesSearch = it.name.contains(searchText, ignoreCase = true) || 
                               it.phoneNumber.contains(searchText, ignoreCase = true)
            val matchesFilter = when (filterType) {
                "read" -> it.isRead
                "unread" -> !it.isRead
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_subscriber")
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة مشترك")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("ابحث باسم المشترك أو الهاتف...", textAlign = TextAlign.Right) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_bar_input"),
                trailingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث") },
                shape = RoundedCornerShape(12.dp),
                colors = appTextFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3-State Filter Row for Read / Unread Subscribers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val filterOptions = listOf(
                    Triple("all", "الكل", null),
                    Triple("read", "تمت قراءتهم", Color(0xFF10B981)),
                    Triple("unread", "بانتظار القراءة", Color(0xFFFBBF24))
                )
                filterOptions.forEach { (type, label, dotColor) ->
                    val isSelected = filterType == type
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { filterType = type },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (dotColor != null) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(dotColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.resetDailyLectures() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("إعادة تصفير قراءات اليوم 🔄", fontSize = 11.sp)
                }
                Text(
                    text = "عدد المشتركين: ${filteredSubscribers.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredSubscribers.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("لا توجد نتائج مطابقة لمشتركي الخدمة.", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredSubscribers, key = { it.id }) { subscriber ->
                        SubscriberItemCard(
                            subscriber = subscriber,
                            onRecord = { subscriberToRecord = subscriber },
                            onEdit = { subscriberToEdit = subscriber },
                            onDelete = { subscriberToDelete = subscriber },
                            onViewHistory = { subscriberHistoryToView = subscriber }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddSubscriberDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, phone, initial, notes ->
                viewModel.addSubscriber(name, phone, initial, notes)
                showAddDialog = false
            }
        )
    }

    val currentToEdit = subscriberToEdit
    if (currentToEdit != null) {
        EditSubscriberDialog(
            subscriber = currentToEdit,
            onDismiss = { subscriberToEdit = null },
            onSave = { updated ->
                viewModel.updateSubscriber(updated)
                subscriberToEdit = null
            }
        )
    }

    val currentToRecord = subscriberToRecord
    if (currentToRecord != null) {
        RecordReadingDialog(
            subscriber = currentToRecord,
            viewModel = viewModel,
            onDismiss = { subscriberToRecord = null },
            onSave = { reading, notes, photo ->
                viewModel.recordReading(currentToRecord, reading, notes, photo) {
                    subscriberToRecord = null
                }
            }
        )
    }

    val currentHistoryToView = subscriberHistoryToView
    if (currentHistoryToView != null) {
        SubscriberBillHistoryDialog(
            subscriber = currentHistoryToView,
            viewModel = viewModel,
            onDismiss = { subscriberHistoryToView = null }
        )
    }

    val currentToDelete = subscriberToDelete
    if (currentToDelete != null) {
        AlertDialog(
            onDismissRequest = { subscriberToDelete = null },
            title = { Text("تأكيد حذف المشترك", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right) },
            text = { Text("هل أنت متأكد من رغبتك في حذف المشترك '${currentToDelete.name}'؟ سيتم حذف كافة البيانات والفواتير السابقة الخاصة به بالكامل ولا يمكن التراجع عن هذا الإجراء.", fontSize = 14.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSubscriber(currentToDelete)
                        subscriberToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("تأكيد الحذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { subscriberToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun SubscriberItemCard(
    subscriber: Subscriber,
    onRecord: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewHistory: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewHistory() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (subscriber.isRead) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف المشترك", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                    }
                    Card(
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (subscriber.isRead) Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                    ) {
                        Text(
                            text = if (subscriber.isRead) "تمت قراءته" else "لم يقرأ",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = subscriber.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(2.dp))
            if (subscriber.phoneNumber.isNotEmpty()) {
                Text("الهاتف: ${subscriber.phoneNumber}", fontSize = 11.sp, color = Color.Gray)
            }
            if (subscriber.notes.isNotEmpty()) {
                Text("الموقع/ملاحظات: ${subscriber.notes}", fontSize = 11.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Divider(color = Color.LightGray.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp).background(Color.LightGray.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "تعديل بيانات المشترك", tint = Color.Gray, modifier = Modifier.size(14.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "الحالية: ${subscriber.currentReading.formatAmount()} Kw",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "البدء: ${subscriber.initialReading.formatAmount()} Kw",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }

                    Button(
                        onClick = onRecord,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (subscriber.isRead) Color.Gray else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(if (subscriber.isRead) "قراءة جديدة" else "تسجيل قراءة", fontSize = 11.sp)
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
    onAdd: (String, String, Double, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var initialReading by remember { mutableStateOf("0") }
    var notes by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "إضافة مشترك جديد",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )

                Spacer(modifier = Modifier.height(4.dp))

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
                    label = { Text("القراءة الابتدائية (Kw)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = appTextFieldColors()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات / العنوان") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = appTextFieldColors()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
                ) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Button(
                        onClick = { if (name.trim().isNotEmpty()) onAdd(name, phone, initialReading.toDoubleOrNull() ?: 0.0, notes) }
                    ) { Text("حفظ") }
                }
            }
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
    var initialReading by remember { mutableStateOf(subscriber.initialReading.formatAmount()) }
    var currentReading by remember { mutableStateOf(subscriber.currentReading.formatAmount()) }
    var notes by remember { mutableStateOf(subscriber.notes) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "تعديل بيانات المشترك",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("الاسم") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = appTextFieldColors())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("الهاتف") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = appTextFieldColors())
                OutlinedTextField(value = initialReading, onValueChange = { initialReading = it }, label = { Text("القراءة الابتدائية (Kw)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = appTextFieldColors())
                OutlinedTextField(value = currentReading, onValueChange = { currentReading = it }, label = { Text("القراءة الحالية (Kw)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = appTextFieldColors())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("ملاحظات/العنوان") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = appTextFieldColors())

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
                ) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Button(
                        onClick = {
                            if (name.trim().isNotEmpty()) {
                                onSave(subscriber.copy(
                                    name = name, phoneNumber = phone,
                                    initialReading = initialReading.toDoubleOrNull() ?: subscriber.initialReading,
                                    currentReading = currentReading.toDoubleOrNull() ?: subscriber.currentReading,
                                    notes = notes
                                ))
                            }
                        }
                    ) { Text("حفظ التغييرات") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordReadingDialog(
    subscriber: Subscriber,
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onSave: (Double, String, String?) -> Unit
) {
    var readingInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }
    var sendSmsChecked by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val settingsState by viewModel.settings.collectAsStateWithLifecycle(initialValue = Settings())

    var historicalAverage by remember { mutableStateOf(-1.0) }
    LaunchedEffect(subscriber.id) {
        historicalAverage = viewModel.getAverageConsumptionForSubscriber(subscriber.id)
    }

    var tempPhotoFile by remember { mutableStateOf<File?>(null) }
    var photoPath by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoFile != null) {
            photoPath = tempPhotoFile!!.absolutePath
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            try {
                val file = File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
                tempPhotoFile = file
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                Toast.makeText(context, "فشل الكاميرا: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "عذراً، يجب الموافقة على صلاحية الكاميرا لالتقاط صورة للعداد!", Toast.LENGTH_LONG).show()
        }
    }

    var showWarningPrompt by remember { mutableStateOf(false) }

    fun handleSave() {
        val finalReading = readingInput.toDoubleOrNull() ?: subscriber.currentReading
        val previousReading = subscriber.currentReading
        val consumption = (finalReading - previousReading).coerceAtLeast(0.0)
        val totalAmount = consumption * settingsState.pricePerKw

        if (finalReading < previousReading) {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    it.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(300)
                }
            }
            Toast.makeText(context, "خطأ: القراءة الحالية أقل من القراءة السابقة (${previousReading.formatAmount()} Kw)!", Toast.LENGTH_LONG).show()
            return
        }

        if (historicalAverage > 0 && consumption >= historicalAverage * 5 && !showWarningPrompt) {
            showWarningPrompt = true
            return
        }

        val persistentPhotoPath = if (photoPath != null) {
            try {
                val originFile = File(photoPath!!)
                if (originFile.exists()) {
                    val destFile = File(context.filesDir, "meter_${subscriber.id}_${System.currentTimeMillis()}.jpg")
                    originFile.copyTo(destFile, overwrite = true)
                    destFile.absolutePath
                } else null
            } catch (e: Exception) { null }
        } else null

        if (sendSmsChecked) {
            val smsBody = """
            مشتركنا العزيز: ${subscriber.name}
            فاتورة الكهرباء:
            - القراءة السابقة: ${previousReading.formatAmount()} Kw
            - القراءة الحالية: ${finalReading.formatAmount()} Kw
            - استهلاكك: ${consumption.formatAmount()} Kw
            - القيمة المطلوبة: ${totalAmount.formatAmount()} شيكل
            """.trimIndent()
            try {
                val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("smsto:${subscriber.phoneNumber}")
                    putExtra("sms_body", smsBody)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "خطأ بالرسائل: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        onSave(finalReading, notesInput, persistentPhotoPath)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "تسجيل قراءة للعداد",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text("المشترك: ${subscriber.name}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
                    Text("آخر قراءة للعداد: ${subscriber.currentReading.formatAmount()} Kw", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
                    
                    if (historicalAverage > 0) {
                        Text("معدل الاستهلاك المعتاد للزبون: ${historicalAverage.formatAmount()} Kw", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
                    }

                    if (showWarningPrompt) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.End) {
                                Text("تنبيه: الاستهلاك مرتفع جداً!", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.height(2.dp))
                                val inputVal = readingInput.toDoubleOrNull() ?: subscriber.currentReading
                                Text("الاستهلاك الحالي (${(inputVal - subscriber.currentReading).formatAmount()} Kw) أعلى بـ 5 أضعاف من المعدل المعتاد (${historicalAverage.formatAmount()} Kw)! هل أنت متأكد من صحة القراءة؟", fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer, textAlign = TextAlign.Right)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = readingInput,
                        onValueChange = { readingInput = it; showWarningPrompt = false },
                        label = { Text("القراءة الحالية الجديدة (Kw)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("reading_field_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = appTextFieldColors()
                    )

                    Text("ملاحظات ميدانية سريعة:", fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End)
                    ) {
                        listOf("العداد معطل", "استهلاك مرتفع", "مغلق").forEach { tag ->
                            Card(
                                modifier = Modifier.clickable { notesInput = tag },
                                shape = RoundedCornerShape(4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                Text(tag, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                    }

                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("ملاحظات الفاتورة") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = appTextFieldColors()
                    )

                    Button(
                        onClick = {
                            val hasCameraPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.CAMERA
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                            if (hasCameraPermission) {
                                try {
                                    val file = File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
                                    tempPhotoFile = file
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                    cameraLauncher.launch(uri)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "فشل الكاميرا: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                permissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (photoPath == null) "التقاط صورة للعداد 📸" else "تم التقاط صورة العداد ✓", fontSize = 12.sp)
                    }

                    photoPath?.let { path ->
                        AsyncFileImage(
                            filePath = path,
                            contentDescription = "Preview",
                            modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(8.dp))
                        )
                    }

                    if (subscriber.phoneNumber.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { sendSmsChecked = !sendSmsChecked },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text("إرسال فاتورة SMS للمشترك", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Checkbox(checked = sendSmsChecked, onCheckedChange = { sendSmsChecked = it })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
                ) {
                    TextButton(onClick = { if (showWarningPrompt) showWarningPrompt = false else onDismiss() }) {
                        Text(if (showWarningPrompt) "تعديل القراءة" else "إلغاء")
                    }
                    Button(onClick = { handleSave() }, modifier = Modifier.testTag("btn_confirm_save_reading")) {
                        Text(if (showWarningPrompt) "تأكيد واستمرار بالحفظ" else "حفظ")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriberBillHistoryDialog(
    subscriber: Subscriber,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val allInvoices by viewModel.allInvoices.collectAsStateWithLifecycle(initialValue = emptyList())
    val filtered = remember(allInvoices, subscriber.id) {
        allInvoices.filter { it.subscriberId == subscriber.id }
    }
    
    var selectedInvoiceForHistoryPhoto by remember { mutableStateOf<InvoiceHistory?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Title and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                    Text(
                        text = "سجل الفواتير وقراءات: ${subscriber.name}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    )
                }

                val totalPendingAmount = remember(filtered) {
                    filtered.filter { !it.isPaid }.sumOf { it.amount }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (totalPendingAmount > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(
                            text = if (totalPendingAmount > 0) "المتبقي للدفع: ${totalPendingAmount.formatAmount()} شيكل" else "خالي من الديون ✓",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (totalPendingAmount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                    
                    val totalKw = remember(filtered) { filtered.sumOf { it.consumption } }
                    Text(
                        text = "إجمالي الاستهلاك: ${totalKw.formatAmount()} Kw",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("لا يوجد فواتير مسجلة بعد لهذا المشترك.", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filtered, key = { it.id }) { invoice ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.End) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(), 
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically, 
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.weight(1f, fill = false)
                                        ) {
                                            Text("${invoice.amount.formatAmount()} شيكل", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                            
                                            // Payment status clickable badge
                                            Surface(
                                                modifier = Modifier.clickable { viewModel.toggleInvoicePaidStatus(invoice) },
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (invoice.isPaid) Color(0xFFE6F4EA) else Color(0xFFFFF3CD),
                                                border = BorderStroke(1.dp, if (invoice.isPaid) Color(0xFF10B981) else Color(0xFFD97706))
                                            ) {
                                                Text(
                                                    text = if (invoice.isPaid) "تم الدفع ✓" else "اضغط لدفع 💵",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (invoice.isPaid) Color(0xFF137333) else Color(0xFFB06000),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = invoice.date, 
                                            fontSize = 11.sp, 
                                            color = Color.Gray, 
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // Professional readings view
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(6.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text("السابقة", fontSize = 10.sp, color = Color.Gray)
                                                Text(invoice.previousReading.formatAmount(), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(6.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text("الحالية", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                                Text(invoice.currentReading.formatAmount(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(6.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text("الاستهلاك", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                                Text("${invoice.consumption.formatAmount()} Kw", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                            }
                                        }
                                        
                                        val calculatedRate = if (invoice.consumption > 0.0) invoice.amount / invoice.consumption else 0.0
                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f))
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(6.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text("سعر الكيلو", fontSize = 10.sp, color = MaterialTheme.colorScheme.tertiary)
                                                Text("${calculatedRate.formatAmount()} ش.ك", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                                            }
                                        }
                                    }
                                    
                                    if (invoice.notes.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("ملاحظة: ${invoice.notes}", fontSize = 12.sp, color = Color.DarkGray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
                                    }
                                    
                                    invoice.photoProofPath?.let { path ->
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(130.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { selectedInvoiceForHistoryPhoto = invoice }
                                        ) {
                                            AsyncFileImage(
                                                filePath = path,
                                                contentDescription = "صورة العداد",
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color.Black.copy(alpha = 0.4f))
                                                    .align(Alignment.BottomCenter)
                                                    .padding(vertical = 4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("🔍 انقر لتكبير صورة العداد بالكامل", fontSize = 11.sp, color = Color.White)
                                            }
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        TextButton(
                                            onClick = { viewModel.deleteInvoice(invoice) },
                                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("حذف هذه الفاتورة 🗑", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("إغلاق السجل", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    selectedInvoiceForHistoryPhoto?.let { invoice ->
        Dialog(
            onDismissRequest = { selectedInvoiceForHistoryPhoto = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .wrapContentHeight()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { selectedInvoiceForHistoryPhoto = null }) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق")
                        }
                        Text(
                            text = "صورة العداد كاملة",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Right
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    invoice.photoProofPath?.let { path ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 250.dp, max = 500.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            AsyncFullFileImage(
                                filePath = path,
                                contentDescription = "قراءة العداد المكبرة",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } ?: Text("لم يتم العثور على المسار أو فشل تحميل الصورة.", modifier = Modifier.padding(24.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { selectedInvoiceForHistoryPhoto = null },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("إغلاق نافذة العرض")
                    }
                }
            }
        }
    }
}

@Composable
fun FinancialDashboard(viewModel: MainViewModel) {
    val subscribers by viewModel.subscribers.collectAsStateWithLifecycle(initialValue = emptyList())
    val allInvoices by viewModel.allInvoices.collectAsStateWithLifecycle(initialValue = emptyList())

    val totalConsumption = remember(allInvoices) { allInvoices.sumOf { it.consumption } }
    
    // Total cash only includes paid invoices
    val totalCashCollected = remember(allInvoices) { allInvoices.filter { it.isPaid }.sumOf { it.amount } }
    val totalCashPending = remember(allInvoices) { allInvoices.filter { !it.isPaid }.sumOf { it.amount } }
    
    val readCount = remember(subscribers) { subscribers.count { it.isRead } }
    val unreadCount = remember(subscribers) { subscribers.count { !it.isRead } }

    var selectedInvoiceForPhoto by remember { mutableStateOf<InvoiceHistory?>(null) }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        item {
            Text("لوحة الأرقام والجباية المالية", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
        }

        // Prominent Excel export button
        item {
            Button(
                onClick = { exportDataToExcel(context, subscribers, allInvoices) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)), // Excel Green
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("تصدير البيانات والاحتياطي إلى ملف إكسل 📊", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Share, contentDescription = "تصدير أكسل", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("مجموع الجباية (المدفوعة)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("${totalCashCollected.formatAmount()} شيكل", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("مجموع الاستهلاك", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("${totalConsumption.formatAmount()} Kw", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Outstanding/pending dues card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${totalCashPending.formatAmount()} شيكل", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    Text("المبالغ المتبقية بانتظار الدفع:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("تمت قراءتهم", fontSize = 11.sp, color = Color.Gray)
                        Text("$readCount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }
                }
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("بانتظار قراءتهم", fontSize = 11.sp, color = Color.Gray)
                        Text("$unreadCount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                    }
                }
            }
        }

        item {
            Text("آخر الفواتير الصادرة", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
        }

        if (allInvoices.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text("لا توجد فواتير ومستندات جباية حالياً.", fontSize = 12.sp, color = Color.Gray)
                }
            }
        } else {
            items(allInvoices, key = { it.id }) { invoice ->
                val sub = subscribers.find { it.id == invoice.subscriberId }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp), 
                        horizontalArrangement = Arrangement.SpaceBetween, 
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left hand side elements: Amount, Payment state toggle, Image preview
                        Row(
                            verticalAlignment = Alignment.CenterVertically, 
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("${invoice.amount.formatAmount()} شيكل", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            
                            // Interactive Payment Toggle status
                            Surface(
                                modifier = Modifier.clickable { viewModel.toggleInvoicePaidStatus(invoice) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (invoice.isPaid) Color(0xFFE6F4EA) else Color(0xFFFFF3CD),
                                border = BorderStroke(1.dp, if (invoice.isPaid) Color(0xFF10B981) else Color(0xFFD97706))
                            ) {
                                Text(
                                    text = if (invoice.isPaid) "تم الدفع ✓" else "اضغط لدفع 💵",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (invoice.isPaid) Color(0xFF137333) else Color(0xFFB06000),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            
                            // Image Preview indicator using highly optimized async decoder
                            invoice.photoProofPath?.let { path ->
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { selectedInvoiceForPhoto = invoice }
                                ) {
                                    AsyncFileImage(
                                        filePath = path,
                                        contentDescription = "صورة العداد",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                        
                        // Right hand side elements: Customer Name and Consumption
                        Column(horizontalAlignment = Alignment.End) {
                            Text(sub?.name ?: "مشترك محذوف", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            val rate = if (invoice.consumption > 0.0) invoice.amount / invoice.consumption else 0.0
                            Text("الاستهلاك: ${invoice.consumption.formatAmount()} Kw | سعر الكيلو: ${rate.formatAmount()} ش.ك | من ${invoice.previousReading.formatAmount()} إلى ${invoice.currentReading.formatAmount()}", fontSize = 11.sp, color = Color.Gray)
                            Text(invoice.date, fontSize = 9.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }

    selectedInvoiceForPhoto?.let { invoice ->
        Dialog(
            onDismissRequest = { selectedInvoiceForPhoto = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .wrapContentHeight()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { selectedInvoiceForPhoto = null }) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق")
                        }
                        Text(
                            text = "عرض صورة العداد كاملة",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Right
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    invoice.photoProofPath?.let { path ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 250.dp, max = 500.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            AsyncFullFileImage(
                                filePath = path,
                                contentDescription = "صورة العداد",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } ?: Text("لم يتم العثور على المسار أو فشل تحميل الصورة.", modifier = Modifier.padding(24.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { selectedInvoiceForPhoto = null },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("إغلاق النافذة")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val settingsState by viewModel.settings.collectAsStateWithLifecycle(initialValue = Settings())

    var editPriceKw by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(settingsState) {
        editPriceKw = settingsState.pricePerKw.toString()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        item {
            Text("إعدادات التسعيرة والجباية", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.End) {
                    OutlinedTextField(
                        value = editPriceKw,
                        onValueChange = { editPriceKw = it },
                        label = { Text("السعر لكل كيلوواط (شيكل ILS)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = appTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.updateSettings(
                                pricePerKw = editPriceKw.toDoubleOrNull() ?: settingsState.pricePerKw,
                                currency = "ILS",
                                appLockEnabled = false,
                                appLockType = "PIN",
                                appLockSecret = ""
                            )
                            Toast.makeText(context, "تم حفظ الإعدادات بنجاح!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) { Text("حفظ الإعدادات") }
                }
            }
        }

        // Dedicated Backup & Microsoft Excel export card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "نسخ احتياطي وتصدير البيانات",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF137333)
                    )
                    Text(
                        text = "قم بتصدير جميع بيانات المشتركين مع قراءاتهم وفواتيرهم التاريخية وسجل الجباية والمدفوعات إلى ملف Excel (CSV) لمشاركتها وحفظها في مكان آمن للشركاء أو الأرشيف.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Right
                    )
                    
                    val subscribers by viewModel.subscribers.collectAsStateWithLifecycle(initialValue = emptyList())
                    val allInvoices by viewModel.allInvoices.collectAsStateWithLifecycle(initialValue = emptyList())
                    
                    Button(
                        onClick = { exportDataToExcel(context, subscribers, allInvoices) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)), // Excel color
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("مشاركة وتصدير ملف Excel 📊", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified Seal",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "تطبيق محصل عدادات الكهرباء الذكية",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "نسخة مرخصة ومحمية بالكامل جباية ذكية دون إنترنت ✓",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun AsyncFileImage(
    filePath: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    var bitmapState by remember(filePath) { mutableStateOf<android.graphics.Bitmap?>(null) }
    var triedLoading by remember(filePath) { mutableStateOf(false) }

    LaunchedEffect(filePath) {
        triedLoading = false
        withContext(Dispatchers.IO) {
            try {
                val file = File(filePath)
                if (file.exists()) {
                    val options = BitmapFactory.Options().apply {
                        inSampleSize = 2
                    }
                    val decoded = BitmapFactory.decodeFile(filePath, options)
                    bitmapState = decoded
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                triedLoading = true
            }
        }
    }

    val bmp = bitmapState
    if (bmp != null) {
        Image(
            bitmap = bmp.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (!triedLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 1.5.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "No photo",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun AsyncFullFileImage(
    filePath: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    var bitmapState by remember(filePath) { mutableStateOf<android.graphics.Bitmap?>(null) }
    var triedLoading by remember(filePath) { mutableStateOf(false) }

    LaunchedEffect(filePath) {
        triedLoading = false
        withContext(Dispatchers.IO) {
            try {
                val file = File(filePath)
                if (file.exists()) {
                    val decoded = BitmapFactory.decodeFile(filePath)
                    bitmapState = decoded
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                triedLoading = true
            }
        }
    }

    val bmp = bitmapState
    if (bmp != null) {
        Image(
            bitmap = bmp.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Box(
            modifier = modifier.fillMaxWidth().height(300.dp).background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (!triedLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text("فشل تحميل الصورة.")
            }
        }
    }
}

fun exportDataToExcel(context: Context, subscribers: List<Subscriber>, invoices: List<InvoiceHistory>) {
    try {
        val csvHeader = "\uFEFF" + "اسم المشترك,رقم الهاتف,حالة القراءة,آخر قراءة للعداد,تاريخ آخر قراءة,مجموع الاستهلاك (Kw),الفواتير المدفوعة (شيكل),الفواتير المعلقة (شيكل),سجل القراءات التاريخية بالفواصل\n"
        val csvBody = StringBuilder()
        csvBody.append(csvHeader)
        
        subscribers.forEach { sub ->
            val subInvoices = invoices.filter { it.subscriberId == sub.id }
            val totalKw = subInvoices.sumOf { it.consumption }
            val paidAmount = subInvoices.filter { it.isPaid }.sumOf { it.amount }
            val pendingAmount = subInvoices.filter { !it.isPaid }.sumOf { it.amount }
            
            val status = if (sub.isRead) "تمت القراءة" else "بانتظار القراءة"
            
            val historyDetails = subInvoices.joinToString(" | ") { inv ->
                "${inv.previousReading.formatAmount()}->${inv.currentReading.formatAmount()} (${inv.consumption.formatAmount()}Kw : ${inv.amount.formatAmount()}شيكل : ${if (inv.isPaid) "مدفوع" else "غير مدفوع"} : ${inv.date})"
            }
            
            val nameSafe = sub.name.replace(",", "-")
            val phoneSafe = sub.phoneNumber.replace(",", "-")
            val historySafe = historyDetails.replace(",", " ")
            
            val latestInvoice = subInvoices.maxByOrNull { it.id }
            val latestReading = latestInvoice?.currentReading ?: sub.currentReading
            val latestReadingDate = latestInvoice?.date ?: "تأسيس المشترك"
            
            csvBody.append("$nameSafe,$phoneSafe,$status,${latestReading.formatAmount()},$latestReadingDate,${totalKw.formatAmount()},${paidAmount.formatAmount()},${pendingAmount.formatAmount()},$historySafe\n")
        }
        
        val file = File(context.cacheDir, "SmartCollector_Export_${System.currentTimeMillis()}.csv")
        file.writeText(csvBody.toString(), Charsets.UTF_8)
        
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(android.content.Intent.EXTRA_SUBJECT, "تصدير واحتياط لبيانات المشتركين والفواتير")
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(android.content.Intent.createChooser(intent, "حفظ وتصدير ملف Excel (CSV)"))
    } catch (e: Exception) {
        Toast.makeText(context, "فشل تصدير البيانات إلى تنسيق إكسل: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
