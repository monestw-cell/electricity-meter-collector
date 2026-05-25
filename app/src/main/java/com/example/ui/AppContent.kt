package com.example.ui

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
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
import com.example.data.model.InvoiceHistory
import com.example.data.model.Settings
import com.example.data.model.Subscriber
import java.io.File

@Composable
fun appTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.6f),
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = Color.Gray
)

@Composable
fun AppContent(viewModel: MainViewModel) {
    val settingsState by viewModel.settings.collectAsStateWithLifecycle(initialValue = Settings())
    var appUnlocked by remember { mutableStateOf(false) }

    LaunchedEffect(settingsState.appLockEnabled) {
        if (!settingsState.appLockEnabled) {
            appUnlocked = true
        }
    }

    if (settingsState.appLockEnabled && !appUnlocked) {
        AppLockScreen(settings = settingsState, onUnlocked = { appUnlocked = true })
    } else {
        var currentTab by remember { mutableStateOf("subscribers") }

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
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    NavigationBarItem(
                        selected = (currentTab == "settings"),
                        onClick = { currentTab = "settings" },
                        icon = { Icon(Icons.Default.Settings, "الإعداد") },
                        label = { Text("الإعدادات", fontSize = 11.sp) },
                        modifier = Modifier.testTag("tab_settings")
                    )
                    NavigationBarItem(
                        selected = (currentTab == "stats"),
                        onClick = { currentTab = "stats" },
                        icon = { Icon(Icons.Default.Info, "المالية") },
                        label = { Text("المالية والأرقام", fontSize = 11.sp) },
                        modifier = Modifier.testTag("tab_stats")
                    )
                    NavigationBarItem(
                        selected = (currentTab == "subscribers"),
                        onClick = { currentTab = "subscribers" },
                        icon = { Icon(Icons.Default.AccountCircle, "المشتركين") },
                        label = { Text("المشتركون", fontSize = 11.sp) },
                        modifier = Modifier.testTag("tab_subscribers")
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
    var subscriberToRecord by remember { mutableStateOf<Subscriber?>(null) }
    var subscriberHistoryToView by remember { mutableStateOf<Subscriber?>(null) }

    val filteredSubscribers = remember(subscribers, searchText) {
        subscribers.filter {
            it.name.contains(searchText, ignoreCase = true) || 
            it.phoneNumber.contains(searchText, ignoreCase = true)
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
                            onDelete = { viewModel.deleteSubscriber(subscriber) },
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

    if (subscriberToEdit != null) {
        EditSubscriberDialog(
            subscriber = subscriberToEdit!!,
            onDismiss = { subscriberToEdit = null },
            onSave = { updated ->
                viewModel.updateSubscriber(updated)
                subscriberToEdit = null
            }
        )
    }

    if (subscriberToRecord != null) {
        RecordReadingDialog(
            subscriber = subscriberToRecord!!,
            viewModel = viewModel,
            onDismiss = { subscriberToRecord = null },
            onSave = { reading, notes, photo ->
                viewModel.recordReading(subscriberToRecord!!, reading, notes, photo) {
                    subscriberToRecord = null
                }
            }
        )
    }

    if (subscriberHistoryToView != null) {
        SubscriberBillHistoryDialog(
            subscriber = subscriberHistoryToView!!,
            viewModel = viewModel,
            onDismiss = { subscriberHistoryToView = null }
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
                Text(
                    text = subscriber.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
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
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(30.dp).background(Color.LightGray.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = Color.Gray, modifier = Modifier.size(14.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(30.dp).background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "الحالية: ${subscriber.currentReading} Kw",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "البدء: ${subscriber.initialReading} Kw",
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
    onAdd: (String, String, Int, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var initialReading by remember { mutableStateOf("0") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة مشترك جديد", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
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
                    label = { Text("القراءة الابتدائية (Kw)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.trim().isNotEmpty()) onAdd(name, phone, initialReading.toIntOrNull() ?: 0, notes) }
            ) { Text("حفظ") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
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
        title = { Text("تعديل بيانات المشترك", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.End
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("الاسم") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = appTextFieldColors())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("الهاتف") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = appTextFieldColors())
                OutlinedTextField(value = initialReading, onValueChange = { initialReading = it }, label = { Text("القراءة الابتدائية (Kw)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = appTextFieldColors())
                OutlinedTextField(value = currentReading, onValueChange = { currentReading = it }, label = { Text("القراءة الحالية (Kw)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = appTextFieldColors())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("ملاحظات/العنوان") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = appTextFieldColors())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.trim().isNotEmpty()) {
                        onSave(subscriber.copy(
                            name = name, phoneNumber = phone,
                            initialReading = initialReading.toIntOrNull() ?: subscriber.initialReading,
                            currentReading = currentReading.toIntOrNull() ?: subscriber.currentReading,
                            notes = notes
                        ))
                    }
                }
            ) { Text("حفظ التغييرات") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordReadingDialog(
    subscriber: Subscriber,
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onSave: (Int, String, String?) -> Unit
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

    var showWarningPrompt by remember { mutableStateOf(false) }

    fun handleSave() {
        val finalReading = readingInput.toIntOrNull() ?: subscriber.currentReading
        val previousReading = subscriber.currentReading
        val consumption = (finalReading - previousReading).coerceAtLeast(0)
        val totalAmount = consumption * settingsState.pricePerKw

        if (finalReading < previousReading) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    it.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(300)
                }
            }
            Toast.makeText(context, "خطأ: القراءة الحالية أقل من القراءة السابقة (${previousReading} Kw)!", Toast.LENGTH_LONG).show()
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
            - القراءة السابقة: $previousReading Kw
            - القراءة الحالية: $finalReading Kw
            - استهلاكك: $consumption Kw
            - القيمة المطلوبة: $totalAmount شيكل
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تسجيل قراءة للعداد", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text("المشترك: ${subscriber.name}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("آخر قراءة للعداد: ${subscriber.currentReading} Kw", fontSize = 12.sp, color = Color.Gray)
                
                if (historicalAverage > 0) {
                    Text("معدل الاستهلاك المعتاد للزبون: %.1f Kw".format(historicalAverage), fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                }

                if (showWarningPrompt) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.End) {
                            Text("تنبيه: الاستهلاك مرتفع جداً!", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(2.dp))
                            val inputVal = readingInput.toIntOrNull() ?: subscriber.currentReading
                            Text("الاستهلاك الحالي (${inputVal - subscriber.currentReading} Kw) أعلى بـ 5 أضعاف من المعدل المعتاد (%.1f Kw)! هل أنت متأكد من صحة القراءة؟".format(historicalAverage), fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer, textAlign = TextAlign.Right)
                        }
                    }
                }

                OutlinedTextField(
                    value = readingInput,
                    onValueChange = { readingInput = it; showWarningPrompt = false },
                    label = { Text("القراءة الحالية الجديدة (Kw)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("reading_field_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = appTextFieldColors()
                )

                Text("ملاحظات ميدانية سريعة:", fontSize = 10.sp, color = Color.Gray)
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
                        try {
                            val file = File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
                            tempPhotoFile = file
                            val uri = FileProvider.getUriForFile(context, "com.example.aadad.fileprovider", file)
                            cameraLauncher.launch(uri)
                        } catch (e: Exception) {
                            Toast.makeText(context, "فشل الكاميرا: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (photoPath == null) "التقاط صورة للعداد 📸" else "تم التقاط صورة العداد ✓", fontSize = 12.sp)
                }

                photoPath?.let { path ->
                    val bitmap = remember(path) {
                        try { BitmapFactory.decodeFile(path) } catch (e: Exception) { null }
                    }
                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(), contentDescription = "Preview",
                            modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
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
        },
        confirmButton = {
            Button(onClick = { handleSave() }, modifier = Modifier.testTag("btn_confirm_save_reading")) {
                Text(if (showWarningPrompt) "تأكيد واستمرار بالحفظ" else "حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = { if (showWarningPrompt) showWarningPrompt = false else onDismiss() }) {
                Text(if (showWarningPrompt) "تعديل القراءة" else "إلغاء")
            }
        }
    )
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("سجل فواتير المشترك", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                horizontalAlignment = Alignment.End
            ) {
                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("لا يوجد فواتير مسجلة بعد.", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filtered) { invoice ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.End) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${invoice.amount} شيكل", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Text(invoice.date, fontSize = 10.sp, color = Color.Gray)
                                    }
                                    Text("القراءات: ${invoice.previousReading} -> ${invoice.currentReading} (${invoice.consumption} Kw)", fontSize = 11.sp)
                                    if (invoice.notes.isNotEmpty()) {
                                        Text("ملاحظة: ${invoice.notes}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    invoice.photoProofPath?.let { path ->
                                        val bitmap = remember(path) {
                                            try { BitmapFactory.decodeFile(path) } catch (e: Exception) { null }
                                        }
                                        bitmap?.let {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Image(
                                                bitmap = it.asImageBitmap(), contentDescription = "Proof",
                                                modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(6.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                    TextButton(
                                        onClick = { viewModel.deleteInvoice(invoice) },
                                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) { Text("حذف الفاتورة 🗑", fontSize = 10.sp) }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("إغلاق") } }
    )
}

@Composable
fun FinancialDashboard(viewModel: MainViewModel) {
    val subscribers by viewModel.subscribers.collectAsStateWithLifecycle(initialValue = emptyList())
    val allInvoices by viewModel.allInvoices.collectAsStateWithLifecycle(initialValue = emptyList())

    val totalConsumption = remember(allInvoices) { allInvoices.sumOf { it.consumption } }
    val totalCashExpected = remember(allInvoices) { allInvoices.sumOf { it.amount } }
    val readCount = remember(subscribers) { subscribers.count { it.isRead } }
    val unreadCount = remember(subscribers) { subscribers.count { !it.isRead } }

    var selectedInvoiceForPhoto by remember { mutableStateOf<InvoiceHistory?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        item {
            Text("لوحة الأرقام والجباية المالية", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("مجموع الجباية", fontSize = 11.sp)
                        Text("%.1f شيكل".format(totalCashExpected), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("مجموع الاستهلاك", fontSize = 11.sp)
                        Text("$totalConsumption Kw", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
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
            items(allInvoices) { invoice ->
                val sub = subscribers.find { it.id == invoice.subscriberId }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("${invoice.amount} شيكل", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            invoice.photoProofPath?.let {
                                IconButton(onClick = { selectedInvoiceForPhoto = invoice }, modifier = Modifier.size(28.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape)) {
                                    Icon(Icons.Default.ThumbUp, contentDescription = "View Photo", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(sub?.name ?: "مشترك محذوف", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("الاستهلاك: ${invoice.consumption} Kw | من ${invoice.previousReading} إلى ${invoice.currentReading}", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }

    selectedInvoiceForPhoto?.let { invoice ->
        AlertDialog(
            onDismissRequest = { selectedInvoiceForPhoto = null },
            title = { Text("صورة العداد المرفقة بالفاتورة", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right) },
            text = {
                val bitmap = remember(invoice) {
                    try { BitmapFactory.decodeFile(invoice.photoProofPath) } catch (e: Exception) { null }
                }
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(), contentDescription = "Photo Proof",
                        modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                } ?: Text("فشل تحميل الصورة.")
            },
            confirmButton = { Button(onClick = { selectedInvoiceForPhoto = null }) { Text("إغلاق") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val settingsState by viewModel.settings.collectAsStateWithLifecycle(initialValue = Settings())

    var editPriceKw by remember { mutableStateOf("") }
    var editAppLockEnabled by remember { mutableStateOf(false) }
    var editAppLockType by remember { mutableStateOf("PIN") }
    var editAppLockSecret by remember { mutableStateOf("") }

    val context = LocalContext.current

    LaunchedEffect(settingsState) {
        editPriceKw = settingsState.pricePerKw.toString()
        editAppLockEnabled = settingsState.appLockEnabled
        editAppLockType = settingsState.appLockType
        editAppLockSecret = settingsState.appLockSecret
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        item {
            Text("إعدادات التسعيرة وقفل التطبيق", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.End) {
                    OutlinedTextField(
                        value = editPriceKw,
                        onValueChange = { editPriceKw = it },
                        label = { Text("السعر الكيلوواط (شيكل ILS)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = appTextFieldColors()
                    )

                    Divider(color = Color.LightGray.copy(alpha = 0.4f))

                    Text("قفل أمان التطبيق بالكامل", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { editAppLockEnabled = !editAppLockEnabled }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Switch(checked = editAppLockEnabled, onCheckedChange = { editAppLockEnabled = it })
                        Text("قفل التطبيق برمز حماية")
                    }

                    if (editAppLockEnabled) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(selected = (editAppLockType == "PIN"), onClick = { editAppLockType = "PIN" }, label = { Text("رمز PIN") }, modifier = Modifier.weight(1f))
                            FilterChip(selected = (editAppLockType == "PASSWORD"), onClick = { editAppLockType = "PASSWORD" }, label = { Text("كلمة سر") }, modifier = Modifier.weight(1f))
                            FilterChip(selected = (editAppLockType == "BIOMETRIC"), onClick = { editAppLockType = "BIOMETRIC" }, label = { Text("بصمة") }, modifier = Modifier.weight(1f))
                        }

                        if (editAppLockType == "PIN" || editAppLockType == "PASSWORD") {
                            OutlinedTextField(
                                value = editAppLockSecret,
                                onValueChange = { editAppLockSecret = it },
                                label = { Text(if (editAppLockType == "PIN") "رمز PIN (رقمي)" else "كلمة السر") },
                                keyboardOptions = KeyboardOptions(keyboardType = if (editAppLockType == "PIN") KeyboardType.Number else KeyboardType.Text),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = appTextFieldColors()
                            )
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.updateSettings(
                                pricePerKw = editPriceKw.toDoubleOrNull() ?: settingsState.pricePerKw,
                                currency = "ILS",
                                appLockEnabled = editAppLockEnabled,
                                appLockType = editAppLockType,
                                appLockSecret = editAppLockSecret
                            )
                            Toast.makeText(context, "تم حفظ الإعدادات وقفل الأمان بنجاح!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) { Text("حفظ الإعدادات وقفل الحماية") }
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
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (settings.appLockType == "BIOMETRIC") Icons.Default.Face else Icons.Default.Lock,
            contentDescription = "Lock",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "التطبيق مقفل", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(
            text = when (settings.appLockType) {
                "PIN" -> "الرجاء إدخال رمز الـ PIN المكون من أرقام لإلغاء القفل"
                "PASSWORD" -> "الرجاء إدخال كلمة المرور السرية لإلغاء القفل"
                else -> "الرجاء استخدام المصادقة بالنقر السريع للبصمة"
            },
            fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (settings.appLockType == "BIOMETRIC") {
            Card(
                modifier = Modifier.size(100.dp).clickable {
                    Toast.makeText(context, "تمت المصادقة ببصمة الإصبع بنجاح ✓", Toast.LENGTH_SHORT).show()
                    onUnlocked()
                },
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "بصمة", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text("انقر هنا لمحاكاة بصمة الإصبع", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
        }

        if (settings.appLockType == "PIN" || settings.appLockType == "PASSWORD") {
            OutlinedTextField(
                value = passwordInput,
                onValueChange = { passwordInput = it; isError = false },
                label = { Text(if (settings.appLockType == "PIN") "رمز PIN السري" else "كلمة المرور") },
                keyboardOptions = KeyboardOptions(keyboardType = if (settings.appLockType == "PIN") KeyboardType.Number else KeyboardType.Text),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(0.8f).testTag("app_lock_input_field"),
                shape = RoundedCornerShape(12.dp),
                isError = isError,
                colors = appTextFieldColors()
            )

            if (isError) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "الرمز السري غير صحيح!", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (passwordInput == settings.appLockSecret) {
                        onUnlocked()
                    } else isError = true
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(0.8f).height(48.dp).testTag("btn_unlock_app")
            ) { Text("إلغاء القفل", fontWeight = FontWeight.Bold) }
        }
    }
}
