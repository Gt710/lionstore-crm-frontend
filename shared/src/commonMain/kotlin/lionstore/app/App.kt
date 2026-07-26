package lionstore.app

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay

// Navigation screens
sealed class Screen {
    object Dashboard : Screen()
    object Orders : Screen()
    object Settings : Screen()
    object Search : Screen()
    object NewTicket : Screen()
    data class TicketDetails(val ticketId: String) : Screen()
    data class EditTicket(val ticketId: String) : Screen()
}

// Data models
data class ActivityEvent(
    val title: String,
    val date: String,
    val by: String,
    val note: String? = null,
    val isPrimary: Boolean = false
)

data class RepairTicket(
    val id: String,
    val status: String,
    val deviceModel: String,
    val createdDate: String,
    val price: String,
    val clientName: String,
    val clientPhone: String,
    val issueDescription: String,
    val assignedWorker: String,
    val devicePassword: String = "",
    val activityHistory: List<ActivityEvent> = emptyList()
)

// Status colors helper
fun getStatusBgColor(status: String): Color {
    return when (status) {
        "На узгодженні" -> Color(0xFFEFF6FF)
        "В роботі" -> Color(0xFFFEF3C7)
        "Очікує запчастин" -> Color(0xFFFFEDD5)
        "Готовий до видачі" -> Color(0xFFD1FAE5)
        "Завершений" -> Color(0xFFF3F4F6)
        else -> Color(0xFFF8F7F5)
    }
}

fun getStatusTextColor(status: String): Color {
    return when (status) {
        "На узгодженні" -> Color(0xFF2563EB)
        "В роботі" -> Color(0xFFD97706)
        "Очікує запчастин" -> Color(0xFFEA580C)
        "Готовий до видачі" -> Color(0xFF059669)
        "Завершений" -> Color(0xFF4B5563)
        else -> Color(0xFF181511)
    }
}

// Helper to get initials
fun getInitials(name: String): String {
    val parts = name.split(" ").filter { it.isNotBlank() }
    return when {
        parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> "CL"
    }
}

// Helper to format assigned worker name as "First Name F." (e.g. "Михайло С.")
fun formatWorkerName(fullName: String): String {
    val parts = fullName.split(" ").filter { it.isNotBlank() }
    return if (parts.size >= 2) {
        "${parts[0]} ${parts[1].first()}."
    } else {
        fullName
    }
}

// Custom Material Symbols Outlined Icons
val AddIcon: ImageVector by lazy {
    ImageVector.Builder(name = "Add", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(19f, 13f)
            horizontalLineTo(13f)
            verticalLineTo(19f)
            horizontalLineTo(11f)
            verticalLineTo(13f)
            horizontalLineTo(5f)
            verticalLineTo(11f)
            horizontalLineTo(11f)
            verticalLineTo(5f)
            horizontalLineTo(13f)
            verticalLineTo(11f)
            horizontalLineTo(19f)
            verticalLineTo(13f)
            close()
        }
    }.build()
}

val SearchIcon: ImageVector by lazy {
    ImageVector.Builder(name = "Search", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(15.5f, 14f)
            horizontalLineTo(14.71f)
            lineTo(14.43f, 13.73f)
            curveTo(15.41f, 12.59f, 16f, 11.11f, 16f, 9.5f)
            curveTo(16f, 5.91f, 13.09f, 3f, 9.5f, 3f)
            curveTo(5.91f, 3f, 3f, 5.91f, 3f, 9.5f)
            curveTo(3f, 13.09f, 5.91f, 16f, 9.5f, 16f)
            curveTo(11.11f, 16f, 12.59f, 15.41f, 13.73f, 14.43f)
            lineTo(14f, 14.71f)
            verticalLineTo(15.5f)
            lineTo(19f, 20.49f)
            lineTo(20.49f, 19f)
            lineTo(15.5f, 14f)
            close()
            moveTo(9.5f, 14f)
            curveTo(7.01f, 14f, 5f, 11.99f, 5f, 9.5f)
            curveTo(5f, 7.01f, 7.01f, 5f, 9.5f, 5f)
            curveTo(11.99f, 5f, 14f, 7.01f, 14f, 9.5f)
            curveTo(14f, 11.99f, 11.99f, 14f, 9.5f, 14f)
            close()
        }
    }.build()
}

val ArrowBackIcon: ImageVector by lazy {
    ImageVector.Builder(name = "ArrowBack", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(20f, 11f)
            horizontalLineTo(7.83f)
            lineTo(13.42f, 5.41f)
            lineTo(12f, 4f)
            lineTo(4f, 12f)
            lineTo(12f, 20f)
            lineTo(13.41f, 18.59f)
            lineTo(7.83f, 13f)
            horizontalLineTo(20f)
            verticalLineTo(11f)
            close()
        }
    }.build()
}

val CloseIcon: ImageVector by lazy {
    ImageVector.Builder(name = "Close", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(19f, 6.41f)
            lineTo(17.59f, 5f)
            lineTo(12f, 10.59f)
            lineTo(6.41f, 5f)
            lineTo(5f, 6.41f)
            lineTo(10.59f, 12f)
            lineTo(5f, 17.59f)
            lineTo(6.41f, 19f)
            lineTo(12f, 13.41f)
            lineTo(17.59f, 19f)
            lineTo(19f, 17.59f)
            lineTo(13.41f, 12f)
            close()
        }
    }.build()
}

val CopyIcon: ImageVector by lazy {
    ImageVector.Builder(name = "Copy", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(16f, 1f)
            horizontalLineTo(4f)
            curveTo(2.9f, 1f, 2f, 1.9f, 2f, 3f)
            verticalLineTo(17f)
            horizontalLineTo(4f)
            verticalLineTo(3f)
            horizontalLineTo(16f)
            verticalLineTo(1f)
            close()
            moveTo(19f, 5f)
            horizontalLineTo(8f)
            curveTo(6.9f, 5f, 6f, 5.9f, 6f, 7f)
            verticalLineTo(21f)
            curveTo(6f, 22.1f, 6.9f, 23f, 8f, 23f)
            horizontalLineTo(19f)
            curveTo(20.1f, 23f, 21f, 22.1f, 21f, 21f)
            verticalLineTo(7f)
            curveTo(21f, 5.9f, 20.1f, 5f, 19f, 5f)
            close()
            moveTo(19f, 21f)
            horizontalLineTo(8f)
            verticalLineTo(7f)
            horizontalLineTo(19f)
            verticalLineTo(21f)
            close()
        }
    }.build()
}

val KeyboardArrowDownIcon: ImageVector by lazy {
    ImageVector.Builder(name = "KeyboardArrowDown", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(7.41f, 8.59f)
            lineTo(12f, 13.17f)
            lineTo(16.59f, 8.59f)
            lineTo(18f, 10f)
            lineTo(12f, 16f)
            lineTo(6f, 10f)
            lineTo(7.41f, 8.59f)
            close()
        }
    }.build()
}

val PhoneCallIcon: ImageVector by lazy {
    ImageVector.Builder(name = "PhoneCall", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(6.62f, 10.79f)
            curveTo(8.06f, 13.62f, 10.38f, 15.94f, 13.21f, 17.38f)
            lineTo(15.41f, 15.18f)
            curveTo(15.69f, 14.9f, 16.08f, 14.82f, 16.43f, 14.93f)
            curveTo(17.55f, 15.3f, 18.75f, 15.5f, 20f, 15.5f)
            curveTo(20.55f, 15.5f, 21f, 15.95f, 21f, 16.5f)
            verticalLineTo(20f)
            curveTo(21f, 20.55f, 20.55f, 21f, 20f, 21f)
            curveTo(10.61f, 21f, 3f, 13.39f, 3f, 4f)
            curveTo(3f, 3.45f, 3.45f, 3f, 4f, 3f)
            horizontalLineTo(7.5f)
            curveTo(8.05f, 3f, 8.5f, 3.45f, 8.5f, 4f)
            curveTo(8.5f, 5.25f, 8.7f, 6.45f, 9.07f, 7.57f)
            curveTo(9.18f, 7.92f, 9.1f, 8.31f, 8.82f, 8.59f)
            lineTo(6.62f, 10.79f)
            close()
        }
    }.build()
}

val DeleteIcon: ImageVector by lazy {
    ImageVector.Builder(name = "Delete", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(6f, 19f)
            curveTo(6f, 20.1f, 6.9f, 21f, 8f, 21f)
            horizontalLineTo(16f)
            curveTo(17.1f, 21f, 18f, 20.1f, 18f, 19f)
            verticalLineTo(7f)
            horizontalLineTo(6f)
            verticalLineTo(19f)
            close()
            moveTo(19f, 4f)
            horizontalLineTo(15.5f)
            lineTo(14.5f, 3f)
            horizontalLineTo(9.5f)
            lineTo(8.5f, 4f)
            horizontalLineTo(5f)
            verticalLineTo(6f)
            horizontalLineTo(19f)
            verticalLineTo(4f)
            close()
        }
    }.build()
}

// User requested exact icon: print_outlined (var=opsz,wght,FILL,GRAD,ROND@24,400,0,0,50)
val PrintOutlinedIcon: ImageVector by lazy {
    ImageVector.Builder(name = "PrintOutlined", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(19f, 8f)
            horizontalLineTo(5f)
            curveTo(3.34f, 8f, 2f, 9.34f, 2f, 11f)
            verticalLineTo(17f)
            horizontalLineTo(6f)
            verticalLineTo(21f)
            horizontalLineTo(18f)
            verticalLineTo(17f)
            horizontalLineTo(22f)
            verticalLineTo(11f)
            curveTo(22f, 9.34f, 20.66f, 8f, 19f, 8f)
            close()
            moveTo(16f, 19f)
            horizontalLineTo(8f)
            verticalLineTo(15f)
            horizontalLineTo(16f)
            verticalLineTo(19f)
            close()
            moveTo(20f, 15f)
            horizontalLineTo(18f)
            verticalLineTo(13f)
            horizontalLineTo(6f)
            verticalLineTo(15f)
            horizontalLineTo(4f)
            verticalLineTo(11f)
            curveTo(4f, 10.45f, 4.45f, 10f, 5f, 10f)
            horizontalLineTo(19f)
            curveTo(19.55f, 10f, 20f, 10.45f, 20f, 11f)
            verticalLineTo(15f)
            close()
            moveTo(18f, 3f)
            horizontalLineTo(6f)
            verticalLineTo(7f)
            horizontalLineTo(18f)
            verticalLineTo(3f)
            close()
            moveTo(16f, 5f)
            horizontalLineTo(8f)
            verticalLineTo(5f)
            horizontalLineTo(16f)
            close()
            moveTo(18.75f, 10.75f)
            curveTo(19.16f, 10.75f, 19.5f, 11.09f, 19.5f, 11.5f)
            curveTo(19.5f, 11.91f, 19.16f, 12.25f, 18.75f, 12.25f)
            curveTo(18.34f, 12.25f, 18f, 11.91f, 18f, 11.5f)
            curveTo(18f, 11.09f, 18.34f, 10.75f, 18.75f, 10.75f)
            close()
        }
    }.build()
}

// User requested exact icon: autorenew (var=opsz,wght,FILL,GRAD,ROND@24,400,0,0,50)
val AutorenewIcon: ImageVector by lazy {
    ImageVector.Builder(name = "Autorenew", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 6f)
            verticalLineTo(3.15f)
            lineTo(8.15f, 7f)
            lineTo(12f, 10.85f)
            verticalLineTo(8f)
            curveTo(15.31f, 8f, 18f, 10.69f, 18f, 14f)
            curveTo(18f, 15.01f, 17.75f, 15.97f, 17.3f, 16.8f)
            lineTo(18.78f, 18.28f)
            curveTo(19.55f, 17.06f, 20f, 15.58f, 20f, 14f)
            curveTo(20f, 9.58f, 16.42f, 6f, 12f, 6f)
            close()
            moveTo(12f, 20f)
            curveTo(8.69f, 20f, 6f, 17.31f, 6f, 14f)
            curveTo(6f, 12.99f, 6.25f, 12.03f, 6.7f, 11.2f)
            lineTo(5.22f, 9.72f)
            curveTo(4.45f, 10.94f, 4f, 12.42f, 4f, 14f)
            curveTo(4f, 18.42f, 7.58f, 22f, 12f, 22f)
            verticalLineTo(24.85f)
            lineTo(15.85f, 21f)
            lineTo(12f, 17.15f)
            verticalLineTo(20f)
            close()
        }
    }.build()
}

// User requested exact icon: library_add (var=opsz,wght,FILL,GRAD,ROND@24,400,0,0,50)
val LibraryAddIcon: ImageVector by lazy {
    ImageVector.Builder(name = "LibraryAdd", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(4f, 6f)
            horizontalLineTo(2f)
            verticalLineTo(20f)
            curveTo(2f, 21.1f, 2.9f, 22f, 4f, 22f)
            horizontalLineTo(18f)
            verticalLineTo(20f)
            horizontalLineTo(4f)
            verticalLineTo(6f)
            close()
            moveTo(20f, 2f)
            horizontalLineTo(8f)
            curveTo(6.9f, 2f, 6f, 2.9f, 6f, 4f)
            verticalLineTo(16f)
            curveTo(6f, 17.1f, 6.9f, 18f, 8f, 18f)
            horizontalLineTo(20f)
            curveTo(21.1f, 18f, 22f, 17.1f, 22f, 16f)
            verticalLineTo(4f)
            curveTo(22f, 2.9f, 21.1f, 2f, 20f, 2f)
            close()
            moveTo(20f, 16f)
            horizontalLineTo(8f)
            verticalLineTo(4f)
            horizontalLineTo(20f)
            verticalLineTo(16f)
            close()
            moveTo(13f, 15f)
            horizontalLineTo(15f)
            verticalLineTo(11f)
            horizontalLineTo(19f)
            verticalLineTo(9f)
            horizontalLineTo(15f)
            verticalLineTo(5f)
            horizontalLineTo(13f)
            verticalLineTo(9f)
            horizontalLineTo(9f)
            verticalLineTo(11f)
            horizontalLineTo(13f)
            verticalLineTo(15f)
            close()
        }
    }.build()
}

// User requested exact icon: save (var=opsz,wght,FILL,GRAD,ROND@24,400,0,0,50)
val SaveIcon: ImageVector by lazy {
    ImageVector.Builder(name = "Save", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(17f, 3f)
            lineTo(21f, 7f)
            verticalLineTo(19f)
            curveTo(21f, 20.1f, 20.1f, 21f, 19f, 21f)
            horizontalLineTo(5f)
            curveTo(3.89f, 21f, 3f, 20.1f, 3f, 19f)
            verticalLineTo(5f)
            curveTo(3f, 3.89f, 3.89f, 3f, 5f, 3f)
            horizontalLineTo(17f)
            close()
            moveTo(17f, 5f)
            horizontalLineTo(5f)
            verticalLineTo(19f)
            horizontalLineTo(19f)
            verticalLineTo(7f)
            lineTo(17f, 5f)
            close()
            moveTo(12f, 18f)
            curveTo(13.66f, 18f, 15f, 16.66f, 15f, 15f)
            curveTo(15f, 13.34f, 13.66f, 12f, 12f, 12f)
            curveTo(10.34f, 12f, 9f, 13.34f, 9f, 15f)
            curveTo(9f, 16.66f, 10.34f, 18f, 12f, 18f)
            close()
            moveTo(6f, 6f)
            horizontalLineTo(15f)
            verticalLineTo(10f)
            horizontalLineTo(6f)
            verticalLineTo(6f)
            close()
        }
    }.build()
}

// User requested exact icon: delete (var=opsz,wght,FILL,GRAD,ROND@24,400,0,0,50)
val DeleteOutlinedIcon: ImageVector by lazy {
    ImageVector.Builder(name = "DeleteOutlined", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(16f, 9f)
            verticalLineTo(19f)
            horizontalLineTo(8f)
            verticalLineTo(9f)
            horizontalLineTo(16f)
            close()
            moveTo(14.5f, 3f)
            horizontalLineTo(9.5f)
            lineTo(8.5f, 4f)
            horizontalLineTo(5f)
            verticalLineTo(6f)
            horizontalLineTo(19f)
            verticalLineTo(4f)
            horizontalLineTo(15.5f)
            lineTo(14.5f, 3f)
            close()
            moveTo(18f, 7f)
            horizontalLineTo(6f)
            verticalLineTo(19f)
            curveTo(6f, 20.1f, 6.9f, 21f, 8f, 21f)
            horizontalLineTo(16f)
            curveTo(17.1f, 21f, 18f, 20.1f, 18f, 19f)
            verticalLineTo(7f)
            close()
        }
    }.build()
}

val HomeIcon: ImageVector by lazy {
    ImageVector.Builder(name = "Home", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(10f, 20f)
            verticalLineTo(14f)
            horizontalLineTo(14f)
            verticalLineTo(20f)
            horizontalLineTo(19f)
            verticalLineTo(12f)
            horizontalLineTo(22f)
            lineTo(12f, 3f)
            lineTo(2f, 12f)
            horizontalLineTo(5f)
            verticalLineTo(20f)
            horizontalLineTo(10f)
            close()
        }
    }.build()
}

// User requested exact icon: handyman
val HandymanIcon: ImageVector by lazy {
    ImageVector.Builder(name = "Handyman", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(21.67f, 18.17f)
            lineTo(14.9f, 11.4f)
            curveTo(15.6f, 9.8f, 15.3f, 7.9f, 14f, 6.6f)
            curveTo(12.6f, 5.2f, 10.5f, 4.9f, 8.8f, 5.7f)
            lineTo(11.6f, 8.5f)
            lineTo(9.5f, 10.6f)
            lineTo(6.7f, 7.8f)
            curveTo(5.9f, 9.5f, 6.2f, 11.6f, 7.6f, 13f)
            curveTo(8.9f, 14.3f, 10.8f, 14.6f, 12.4f, 13.9f)
            lineTo(19.17f, 20.67f)
            curveTo(19.56f, 21.06f, 20.19f, 21.06f, 20.58f, 20.67f)
            lineTo(21.67f, 19.58f)
            curveTo(22.06f, 19.19f, 22.06f, 18.56f, 21.67f, 18.17f)
            close()
            moveTo(13.67f, 3.83f)
            lineTo(10.5f, 7f)
            lineTo(13f, 9.5f)
            lineTo(16.17f, 6.33f)
            curveTo(16.56f, 5.94f, 16.56f, 5.31f, 16.17f, 4.92f)
            lineTo(15.08f, 3.83f)
            curveTo(14.69f, 3.44f, 14.06f, 3.44f, 13.67f, 3.83f)
            close()
        }
    }.build()
}

// User requested exact icon: orders
val OrdersIcon: ImageVector by lazy {
    ImageVector.Builder(name = "Orders", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(19f, 3f)
            horizontalLineTo(5f)
            curveTo(3.9f, 3f, 3f, 3.9f, 3f, 5f)
            verticalLineTo(19f)
            curveTo(3f, 20.1f, 3.9f, 21f, 5f, 21f)
            horizontalLineTo(19f)
            curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f)
            verticalLineTo(5f)
            curveTo(21f, 3.9f, 20.1f, 3f, 19f, 3f)
            close()
            moveTo(19f, 19f)
            horizontalLineTo(5f)
            verticalLineTo(8f)
            horizontalLineTo(19f)
            verticalLineTo(19f)
            close()
            moveTo(7f, 10f)
            horizontalLineTo(17f)
            verticalLineTo(12f)
            horizontalLineTo(7f)
            verticalLineTo(10f)
            close()
            moveTo(7f, 14f)
            horizontalLineTo(14f)
            verticalLineTo(16f)
            horizontalLineTo(7f)
            verticalLineTo(14f)
            close()
        }
    }.build()
}

// User requested exact icon: settings
val SettingsIcon: ImageVector by lazy {
    ImageVector.Builder(name = "Settings", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(19.14f, 12.94f)
            curveTo(19.18f, 12.63f, 19.2f, 12.32f, 19.2f, 12f)
            curveTo(19.2f, 11.68f, 19.18f, 11.37f, 19.14f, 11.06f)
            lineTo(21.16f, 9.48f)
            curveTo(21.34f, 9.34f, 21.39f, 9.08f, 21.28f, 8.87f)
            lineTo(19.37f, 5.56f)
            curveTo(19.25f, 5.35f, 19f, 5.27f, 18.78f, 5.35f)
            lineTo(16.4f, 6.31f)
            curveTo(15.91f, 5.93f, 15.37f, 5.62f, 14.8f, 5.38f)
            lineTo(14.44f, 2.85f)
            curveTo(14.41f, 2.62f, 14.21f, 2.45f, 13.98f, 2.45f)
            horizontalLineTo(10.15f)
            curveTo(9.92f, 2.45f, 9.72f, 2.62f, 9.69f, 2.85f)
            lineTo(9.33f, 5.38f)
            curveTo(8.76f, 5.62f, 8.22f, 5.94f, 7.73f, 6.31f)
            lineTo(5.35f, 5.35f)
            curveTo(5.13f, 5.26f, 4.88f, 5.35f, 4.76f, 5.56f)
            lineTo(2.85f, 8.87f)
            curveTo(2.73f, 9.08f, 2.79f, 9.34f, 2.97f, 9.48f)
            lineTo(4.99f, 11.06f)
            curveTo(4.95f, 11.37f, 4.93f, 11.69f, 4.93f, 12f)
            curveTo(4.93f, 12.31f, 4.95f, 12.63f, 4.99f, 12.94f)
            lineTo(2.97f, 14.52f)
            curveTo(2.79f, 14.66f, 2.73f, 14.92f, 2.85f, 15.13f)
            lineTo(4.76f, 18.44f)
            curveTo(4.88f, 18.65f, 5.13f, 18.74f, 5.35f, 18.65f)
            lineTo(7.73f, 17.69f)
            curveTo(8.22f, 18.07f, 8.76f, 18.38f, 9.33f, 18.62f)
            lineTo(9.69f, 21.15f)
            curveTo(9.72f, 21.38f, 9.92f, 21.55f, 10.15f, 21.55f)
            horizontalLineTo(13.98f)
            curveTo(14.21f, 21.55f, 14.41f, 21.38f, 14.44f, 21.15f)
            lineTo(14.8f, 18.62f)
            curveTo(15.37f, 18.38f, 15.91f, 18.06f, 16.4f, 17.69f)
            lineTo(18.78f, 18.65f)
            curveTo(19f, 18.74f, 19.25f, 18.65f, 19.37f, 18.44f)
            lineTo(21.28f, 15.13f)
            curveTo(21.4f, 14.92f, 21.34f, 14.66f, 21.16f, 14.52f)
            lineTo(19.14f, 12.94f)
            close()
            moveTo(12f, 15.5f)
            curveTo(10.07f, 15.5f, 8.5f, 13.93f, 8.5f, 12f)
            curveTo(8.5f, 10.07f, 10.07f, 8.5f, 12f, 8.5f)
            curveTo(13.93f, 8.5f, 15.5f, 10.07f, 15.5f, 12f)
            curveTo(15.5f, 13.93f, 13.93f, 15.5f, 12f, 15.5f)
            close()
        }
    }.build()
}

val PendingActionsIcon: ImageVector by lazy {
    ImageVector.Builder(name = "PendingActions", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(17f, 12f)
            curveTo(14.24f, 12f, 12f, 14.24f, 12f, 17f)
            curveTo(12f, 19.76f, 14.24f, 22f, 17f, 22f)
            curveTo(19.76f, 22f, 22f, 19.76f, 22f, 17f)
            curveTo(22f, 14.24f, 19.76f, 12f, 17f, 12f)
            close()
            moveTo(18.65f, 19.35f)
            lineTo(16.5f, 17.2f)
            verticalLineTo(14f)
            horizontalLineTo(17.5f)
            verticalLineTo(16.8f)
            lineTo(19.35f, 18.65f)
            lineTo(18.65f, 19.35f)
            close()
            moveTo(19f, 3f)
            horizontalLineTo(14.82f)
            curveTo(14.4f, 1.84f, 13.3f, 1f, 12f, 1f)
            curveTo(10.7f, 1f, 9.6f, 1.84f, 9.18f, 3f)
            horizontalLineTo(5f)
            curveTo(3.9f, 3f, 3f, 3.9f, 3f, 5f)
            verticalLineTo(19f)
            curveTo(3f, 20.1f, 3.9f, 21f, 5f, 21f)
            horizontalLineTo(10.35f)
            curveTo(10.13f, 20.38f, 10f, 19.7f, 10f, 19f)
            horizontalLineTo(5f)
            verticalLineTo(5f)
            horizontalLineTo(7f)
            verticalLineTo(7f)
            horizontalLineTo(17f)
            verticalLineTo(5f)
            horizontalLineTo(19f)
            verticalLineTo(10.35f)
            curveTo(19.7f, 10.35f, 20.38f, 10.48f, 21f, 10.7f)
            verticalLineTo(5f)
            curveTo(21f, 3.9f, 20.1f, 3f, 19f, 3f)
            close()
            moveTo(12f, 3f)
            curveTo(12.55f, 3f, 13f, 3.45f, 13f, 4f)
            curveTo(13f, 4.55f, 12.55f, 5f, 12f, 5f)
            curveTo(11.45f, 5f, 11f, 4.55f, 11f, 4f)
            curveTo(11f, 3.45f, 11.45f, 3f, 12f, 3f)
            close()
        }
    }.build()
}

val CheckCircleIcon: ImageVector by lazy {
    ImageVector.Builder(name = "CheckCircle", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 2f)
            curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
            curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
            curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
            curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
            close()
            moveTo(10f, 16.5f)
            lineTo(5.5f, 12f)
            lineTo(6.91f, 10.59f)
            lineTo(10f, 13.67f)
            lineTo(17.09f, 6.58f)
            lineTo(18.5f, 8f)
            lineTo(10f, 16.5f)
            close()
        }
    }.build()
}

val GroupIcon: ImageVector by lazy {
    ImageVector.Builder(name = "Group", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(16f, 11f)
            curveTo(17.66f, 11f, 18.99f, 9.66f, 18.99f, 8f)
            curveTo(18.99f, 6.34f, 17.66f, 5f, 16f, 5f)
            curveTo(14.34f, 5f, 13f, 6.34f, 13f, 8f)
            curveTo(13f, 9.66f, 14.34f, 11f, 16f, 11f)
            close()
            moveTo(8f, 11f)
            curveTo(9.66f, 11f, 11f, 9.66f, 11f, 8f)
            curveTo(11f, 6.34f, 9.66f, 5f, 8f, 5f)
            curveTo(6.34f, 5f, 5f, 6.34f, 5f, 8f)
            curveTo(5f, 9.66f, 6.34f, 11f, 8f, 11f)
            close()
            moveTo(8f, 13f)
            curveTo(5.33f, 13f, 0f, 14.34f, 0f, 17f)
            verticalLineTo(20f)
            horizontalLineTo(16f)
            verticalLineTo(17f)
            curveTo(16f, 14.34f, 10.67f, 13f, 8f, 13f)
            close()
            moveTo(16f, 13f)
            curveTo(15.71f, 13f, 15.38f, 13.02f, 15.03f, 13.05f)
            curveTo(16.19f, 13.89f, 17f, 15.02f, 17f, 16.5f)
            verticalLineTo(20f)
            horizontalLineTo(24f)
            verticalLineTo(17f)
            curveTo(24f, 14.67f, 19.33f, 13.5f, 16.5f, 13f)
            close()
        }
    }.build()
}

val SecurityIcon: ImageVector by lazy {
    ImageVector.Builder(name = "Security", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 1f)
            lineTo(3f, 5f)
            verticalLineTo(11f)
            curveTo(3f, 16.55f, 6.84f, 21.74f, 12f, 23f)
            curveTo(17.16f, 21.74f, 21f, 16.55f, 21f, 11f)
            verticalLineTo(5f)
            lineTo(12f, 1f)
            close()
        }
    }.build()
}

val PaletteIcon: ImageVector by lazy {
    ImageVector.Builder(name = "Palette", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 3f)
            curveTo(7.03f, 3f, 3f, 7.03f, 3f, 12f)
            curveTo(3f, 16.97f, 7.03f, 21f, 12f, 21f)
            curveTo(12.83f, 21f, 13.5f, 20.33f, 13.5f, 19.5f)
            curveTo(13.5f, 19.11f, 13.35f, 18.76f, 13.11f, 18.49f)
            curveTo(12.88f, 18.23f, 12.73f, 17.88f, 12.73f, 17.5f)
            curveTo(12.73f, 16.67f, 13.4f, 16f, 14.23f, 16f)
            horizontalLineTo(16f)
            curveTo(18.76f, 16f, 21f, 13.76f, 21f, 11f)
            curveTo(21f, 6.58f, 16.97f, 3f, 12f, 3f)
            close()
            moveTo(6.5f, 12f)
            curveTo(5.67f, 12f, 5f, 11.33f, 5f, 10.5f)
            curveTo(5f, 9.67f, 5.67f, 9f, 6.5f, 9f)
            curveTo(7.33f, 9f, 8f, 9.67f, 8f, 10.5f)
            curveTo(8f, 11.33f, 7.33f, 12f, 6.5f, 12f)
            close()
            moveTo(9.5f, 8f)
            curveTo(8.67f, 8f, 8f, 7.33f, 8f, 6.5f)
            curveTo(8f, 5.67f, 8.67f, 5f, 9.5f, 5f)
            curveTo(10.33f, 5f, 11f, 5.67f, 11f, 6.5f)
            curveTo(11f, 7.33f, 10.33f, 8f, 9.5f, 8f)
            close()
            moveTo(14.5f, 8f)
            curveTo(13.67f, 8f, 13f, 7.33f, 13f, 6.5f)
            curveTo(13f, 5.67f, 13.67f, 5f, 14.5f, 5f)
            curveTo(15.33f, 5f, 16f, 5.67f, 16f, 6.5f)
            curveTo(16f, 7.33f, 15.33f, 8f, 14.5f, 8f)
            close()
            moveTo(17.5f, 12f)
            curveTo(16.67f, 12f, 16f, 11.33f, 16f, 10.5f)
            curveTo(16f, 9.67f, 16.67f, 9f, 17.5f, 9f)
            curveTo(18.33f, 9f, 19f, 9.67f, 19f, 10.5f)
            curveTo(19f, 11.33f, 18.33f, 12f, 17.5f, 12f)
            close()
        }
    }.build()
}

val ChevronRightIcon: ImageVector by lazy {
    ImageVector.Builder(name = "ChevronRight", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(10f, 6f)
            lineTo(8.59f, 7.41f)
            lineTo(13.17f, 12f)
            lineTo(8.59f, 16.59f)
            lineTo(10f, 18f)
            lineTo(16f, 12f)
            lineTo(10f, 6f)
            close()
        }
    }.build()
}

val LightModeIcon: ImageVector by lazy {
    ImageVector.Builder(name = "LightMode", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 7f)
            curveTo(9.24f, 7f, 7f, 9.24f, 7f, 12f)
            curveTo(7f, 14.76f, 9.24f, 17f, 12f, 17f)
            curveTo(14.76f, 17f, 17f, 14.76f, 17f, 12f)
            curveTo(17f, 9.24f, 14.76f, 7f, 12f, 7f)
            close()
            moveTo(12f, 2f)
            verticalLineTo(4f)
            moveTo(12f, 20f)
            verticalLineTo(22f)
            moveTo(4.22f, 4.22f)
            lineTo(5.64f, 5.64f)
            moveTo(18.36f, 18.36f)
            lineTo(19.78f, 19.78f)
            moveTo(2f, 12f)
            horizontalLineTo(4f)
            moveTo(20f, 12f)
            horizontalLineTo(22f)
            moveTo(4.22f, 19.78f)
            lineTo(5.64f, 18.36f)
            moveTo(18.36f, 5.64f)
            lineTo(19.78f, 4.22f)
        }
    }.build()
}

val DarkModeIcon: ImageVector by lazy {
    ImageVector.Builder(name = "DarkMode", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12.3f, 2f)
            curveTo(6.58f, 2.38f, 2f, 7.15f, 2f, 13f)
            curveTo(2f, 19.08f, 6.92f, 24f, 13f, 24f)
            curveTo(18.85f, 24f, 23.62f, 19.42f, 24f, 13.7f)
            curveTo(20.35f, 15.35f, 15.65f, 13.93f, 13.7f, 10.3f)
            curveTo(12.25f, 7.58f, 12.8f, 4.25f, 14.8f, 2.1f)
            curveTo(14f, 2f, 13.15f, 1.95f, 12.3f, 2f)
            close()
        }
    }.build()
}

val SystemModeIcon: ImageVector by lazy {
    ImageVector.Builder(name = "SystemMode", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(20.71f, 5.63f)
            lineTo(18.37f, 3.29f)
            curveTo(17.98f, 2.9f, 17.35f, 2.9f, 16.96f, 3.29f)
            lineTo(15.12f, 5.13f)
            lineTo(14.06f, 4.07f)
            lineTo(12.65f, 5.48f)
            lineTo(13.71f, 6.54f)
            lineTo(12.05f, 8.2f)
            lineTo(10.64f, 6.79f)
            lineTo(9.23f, 8.2f)
            lineTo(10.64f, 9.61f)
            lineTo(3f, 17.25f)
            verticalLineTo(21f)
            horizontalLineTo(6.75f)
            lineTo(14.39f, 13.36f)
            lineTo(15.8f, 14.77f)
            lineTo(17.21f, 13.36f)
            lineTo(15.8f, 11.95f)
            lineTo(17.46f, 10.29f)
            lineTo(18.52f, 11.35f)
            lineTo(19.93f, 9.94f)
            lineTo(18.87f, 8.88f)
            lineTo(20.71f, 7.04f)
            curveTo(21.1f, 6.65f, 21.1f, 6.02f, 20.71f, 5.63f)
            close()
        }
    }.build()
}

val CheckIcon: ImageVector by lazy {
    ImageVector.Builder(name = "Check", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(9f, 16.17f)
            lineTo(4.83f, 12f)
            lineTo(3.41f, 13.41f)
            lineTo(9f, 19f)
            lineTo(21f, 7f)
            lineTo(19.59f, 5.59f)
            close()
        }
    }.build()
}

enum class ThemeMode(
    val title: String,
    val icon: ImageVector
) {
    LIGHT("Light", LightModeIcon),
    DARK("Dark", DarkModeIcon),
    SYSTEM("System", SystemModeIcon)
}

data class ThemeColorItem(
    val id: String,
    val name: String,
    val color: Color
)

@Composable
fun AppearanceSection(
    selectedTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    selectedColorId: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val themeColors = remember {
        listOf(
            ThemeColorItem("dynamic", "Dynamic", Color(0xFF90CAF9)),
            ThemeColorItem("ocean", "Ocean", Color(0xFF29B6F6)),
            ThemeColorItem("purple", "Purple", Color(0xFF7E57C2)),
            ThemeColorItem("forest", "Forest", Color(0xFF388E3C)),
            ThemeColorItem("slate", "Slate", Color(0xFF546E7A))
        )
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "APPEARANCE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B5E4C),
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF161C24))
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeMode.values().forEach { mode ->
                    val isSelected = mode == selectedTheme
                    ThemeModeSelectorItem(
                        mode = mode,
                        isSelected = isSelected,
                        onClick = { onThemeSelected(mode) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF161C24))
                .padding(vertical = 16.dp)
        ) {
            Column {
                Text(
                    text = "Theme Color",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    themeColors.forEach { item ->
                        val isSelected = item.id == selectedColorId
                        ThemeColorSelectorItem(
                            item = item,
                            isSelected = isSelected,
                            onClick = { onColorSelected(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeModeSelectorItem(
    mode: ThemeMode,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Color(0xFF025684) else Color.Transparent
    val contentColor = if (isSelected) Color.White else Color(0xFF919EAB)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = mode.icon,
                contentDescription = mode.title,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = mode.title,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = contentColor
            )
        }
    }
}

@Composable
private fun ThemeColorSelectorItem(
    item: ThemeColorItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .then(
                    if (isSelected) {
                        Modifier
                            .border(2.dp, Color(0xFF0288D1), CircleShape)
                            .padding(3.dp)
                    } else Modifier
                )
                .clip(CircleShape)
                .background(item.color),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = CheckIcon,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = item.name,
            fontSize = 12.sp,
            color = if (isSelected) Color(0xFF64B5F6) else Color(0xFF919EAB)
        )
    }
}

// ----------------------------------------------------
// FLOATING PILL BOTTOM NAVIGATION BAR (Matching Photo)
// ----------------------------------------------------
sealed class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val screen: Screen
) {
    object Repairs : BottomNavItem("Ремонт", HandymanIcon, Screen.Dashboard)
    object Orders : BottomNavItem("Замовлення", OrdersIcon, Screen.Orders)
    object Settings : BottomNavItem("Налаштування", SettingsIcon, Screen.Settings)
}

@Composable
fun AppBottomNavigationBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    val items = listOf(
        BottomNavItem.Repairs,
        BottomNavItem.Orders,
        BottomNavItem.Settings
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp, start = 36.dp, end = 36.dp)
            .shadow(elevation = 12.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(Color.White)
            .border(width = 1.dp, color = Color(0xFFE6E2DB), shape = CircleShape)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = when (currentScreen) {
                    is Screen.Dashboard -> item.screen is Screen.Dashboard
                    is Screen.Orders -> item.screen is Screen.Orders
                    is Screen.Settings -> item.screen is Screen.Settings
                    else -> false
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onNavigate(item.screen) }
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = if (isSelected) Color(0xFF181511) else Color(0xFF9CA3AF),
                        modifier = Modifier.size(24.dp)
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF181511)
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        // Navigation & Master Ticket State
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
        var showStatusModal by remember { mutableStateOf(false) }
        var activeTicketId by remember { mutableStateOf("#R-89") }

        // Double back to exit banner state
        var backPressTimestamp by remember { mutableStateOf(0L) }
        var showExitBanner by remember { mutableStateOf(false) }

        // Master List of Tickets
        val tickets = remember {
            mutableStateListOf(
                RepairTicket(
                    id = "#R-89",
                    status = "В роботі",
                    deviceModel = "iPhone 13 Pro",
                    createdDate = "24 Жов 2024",
                    price = "145.00",
                    clientName = "Іван Дерев'янко",
                    clientPhone = "+380 (67) 123-45-67",
                    issueDescription = "Заміна екрану після падіння. Сенсор реагує частково.",
                    assignedWorker = "Михайло Стівенсон",
                    devicePassword = "1234",
                    activityHistory = listOf(
                        ActivityEvent(
                            title = "Оновлення статусу: В роботі",
                            date = "24 Жов, 14:30",
                            by = "Михайло Стівенсон",
                            note = "Розпочато розбирання. Очікуємо на доставку нового акумулятора.",
                            isPrimary = true
                        ),
                        ActivityEvent(
                            title = "Талон створено",
                            date = "24 Жов, 10:15",
                            by = "Адмін"
                        )
                    )
                ),
                RepairTicket(
                    id = "#R-1092",
                    status = "На узгодженні",
                    deviceModel = "iPhone 13 Pro",
                    createdDate = "23 Жов 2024",
                    price = "120.00",
                    clientName = "Олексій Коваленко",
                    clientPhone = "+380 (50) 987-65-43",
                    issueDescription = "Не заряджається, потрібна діагностика порту",
                    assignedWorker = "Олександр Петренко",
                    devicePassword = "0000",
                    activityHistory = listOf(
                        ActivityEvent(title = "Талон створено", date = "23 Жов, 11:00", by = "Адмін")
                    )
                ),
                RepairTicket(
                    id = "#R-1091",
                    status = "В роботі",
                    deviceModel = "MacBook Air M2",
                    createdDate = "22 Жов 2024",
                    price = "350.00",
                    clientName = "Марія Шевченко",
                    clientPhone = "+380 (93) 456-78-90",
                    issueDescription = "Потрапляння рідини на клавіатуру",
                    assignedWorker = "Михайло Стівенсон",
                    devicePassword = "mac2024",
                    activityHistory = listOf(
                        ActivityEvent(title = "Чистка та відновлення плати", date = "22 Жов, 16:20", by = "Михайло Стівенсон")
                    )
                ),
                RepairTicket(
                    id = "#R-1088",
                    status = "Готовий до видачі",
                    deviceModel = "iPad Pro 11-inch",
                    createdDate = "20 Жов 2024",
                    price = "180.00",
                    clientName = "Дмитро Бондар",
                    clientPhone = "+380 (68) 321-65-49",
                    issueDescription = "Заміна акумулятора",
                    assignedWorker = "Михайло Стівенсон",
                    devicePassword = "1111",
                    activityHistory = listOf(
                        ActivityEvent(title = "Ремонт завершено", date = "21 Жов, 18:00", by = "Михайло Стівенсон")
                    )
                ),
                RepairTicket(
                    id = "#R-1085",
                    status = "Очікує запчастин",
                    deviceModel = "AirPods Max",
                    createdDate = "18 Жов 2024",
                    price = "95.00",
                    clientName = "Анна Ткаченко",
                    clientPhone = "+380 (97) 555-12-34",
                    issueDescription = "Проблеми з підключенням Bluetooth",
                    assignedWorker = "Олександр Петренко",
                    devicePassword = "-",
                    activityHistory = listOf(
                        ActivityEvent(title = "Замовлено плату Bluetooth", date = "19 Жов, 09:15", by = "Олександр Петренко")
                    )
                )
            )
        }

        // Function for Back Action
        val handleBack = {
            if (currentScreen != Screen.Dashboard) {
                currentScreen = Screen.Dashboard
            } else {
                if (!showExitBanner) {
                    showExitBanner = true
                } else {
                    // Second back press → exit the app
                    exitApp()
                }
            }
        }

        // Auto-hide the exit banner after 2 seconds
        LaunchedEffect(showExitBanner) {
            if (showExitBanner) {
                delay(2000)
                showExitBanner = false
            }
        }

        // Connect Android system Back handler natively
        SystemBackHandler(enabled = true) {
            handleBack()
        }

        val activeTicket = tickets.find { it.id == activeTicketId } ?: tickets.first()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F7F5))
        ) {
            // Screen Transitions with AnimatedContent
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) + slideInHorizontally(animationSpec = tween(220)) { fullWidth -> fullWidth / 4 } togetherWith
                            fadeOut(animationSpec = tween(200)) + slideOutHorizontally(animationSpec = tween(200)) { fullWidth -> -fullWidth / 4 }
                }
            ) { screen ->
                when (screen) {
                    is Screen.Dashboard -> {
                        DashboardScreen(
                            tickets = tickets,
                            onNewTicketClick = { currentScreen = Screen.NewTicket },
                            onSearchClick = { currentScreen = Screen.Search },
                            onTicketClick = { ticketId ->
                                activeTicketId = ticketId
                                currentScreen = Screen.TicketDetails(ticketId)
                            }
                        )
                    }
                    is Screen.Orders -> {
                        OrdersScreen(onBackClick = handleBack)
                    }
                    is Screen.Settings -> {
                        SettingsScreen(tickets = tickets)
                    }
                    is Screen.Search -> {
                        SearchTicketsScreen(
                            tickets = tickets,
                            onBackClick = handleBack,
                            onTicketClick = { ticketId ->
                                activeTicketId = ticketId
                                currentScreen = Screen.TicketDetails(ticketId)
                            }
                        )
                    }
                    is Screen.NewTicket -> {
                        NewTicketScreen(
                            onCloseClick = handleBack,
                            onCreateTicket = { newTicket ->
                                tickets.add(0, newTicket)
                                activeTicketId = newTicket.id
                                currentScreen = Screen.TicketDetails(newTicket.id)
                            }
                        )
                    }
                    is Screen.TicketDetails -> {
                        TicketDetailsScreen(
                            ticket = activeTicket,
                            onBackClick = handleBack,
                            onEditClick = {
                                currentScreen = Screen.EditTicket(activeTicket.id)
                            },
                            onChangeStatusClick = { showStatusModal = true }
                        )
                    }
                    is Screen.EditTicket -> {
                        EditTicketScreen(
                            ticket = activeTicket,
                            onBackClick = { currentScreen = Screen.TicketDetails(activeTicket.id) },
                            onSaveTicket = { updatedTicket ->
                                val index = tickets.indexOfFirst { it.id == updatedTicket.id }
                                if (index != -1) {
                                    tickets[index] = updatedTicket
                                }
                                currentScreen = Screen.TicketDetails(updatedTicket.id)
                            },
                            onDeleteTicket = { ticketId ->
                                tickets.removeAll { it.id == ticketId }
                                currentScreen = Screen.Dashboard
                            }
                        )
                    }
                }
            }

            // Pill Floating Bottom Navigation Bar (Visible on primary screens)
            if (currentScreen is Screen.Dashboard || currentScreen is Screen.Orders || currentScreen is Screen.Settings || currentScreen is Screen.Search || currentScreen is Screen.NewTicket) {
                Box(
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    AppBottomNavigationBar(
                        currentScreen = currentScreen,
                        onNavigate = { targetScreen ->
                            currentScreen = targetScreen
                        }
                    )
                }
            }

            // Exit Banner Toast
            if (showExitBanner) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF181511).copy(alpha = 0.9f))
                        .clickable { showExitBanner = false }
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Ще раз, аби вийти з додатку",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }

            // Update Status Modal Overlay Dialog
            if (showStatusModal) {
                UpdateStatusModal(
                    currentStatus = activeTicket.status,
                    onDismiss = { showStatusModal = false },
                    onStatusUpdated = { newStatus, noteText ->
                        val newEvent = ActivityEvent(
                            title = "Оновлення статусу: $newStatus",
                            date = "Сьогодні, 12:00",
                            by = activeTicket.assignedWorker,
                            note = if (noteText.isNotBlank()) noteText else null,
                            isPrimary = true
                        )
                        val updatedList = listOf(newEvent) + activeTicket.activityHistory
                        val updatedTicket = activeTicket.copy(
                            status = newStatus,
                            activityHistory = updatedList
                        )
                        val index = tickets.indexOfFirst { it.id == activeTicket.id }
                        if (index != -1) {
                            tickets[index] = updatedTicket
                        }
                        showStatusModal = false
                    }
                )
            }
        }
    }
}

// ----------------------------------------------------
// 1. DASHBOARD SCREEN (Сервіс Ремонту) - NO NAVBAR
// ----------------------------------------------------
@Composable
fun DashboardScreen(
    tickets: List<RepairTicket>,
    onNewTicketClick: () -> Unit,
    onSearchClick: () -> Unit,
    onTicketClick: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 88.dp)
        ) {
            // Header with Camera Notch Offset (top padding = 28.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp, start = 20.dp, end = 20.dp, bottom = 24.dp)
            ) {
                Text(
                    text = "Сервіс Ремонту",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF181511),
                    lineHeight = 38.sp
                )
            }

            // Action Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Yellow Card: Новий талон
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(176.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF9A20B))
                        .clickable { onNewTicketClick() }
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AddIcon,
                            contentDescription = "Створити",
                            tint = Color(0xFF181511),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Новий талон",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF181511),
                            lineHeight = 24.sp
                        )
                        Text(
                            text = "Створити запис",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF181511).copy(alpha = 0.8f)
                        )
                    }
                }

                // Dark Card: Пошук
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(176.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF181511))
                        .clickable { onSearchClick() }
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = SearchIcon,
                            contentDescription = "Пошук",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Пошук",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 24.sp
                        )
                        Text(
                            text = "Знайти за ID",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }
            }

            // Recent Activity Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Останні активності",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF181511)
                )
                Text(
                    text = "Дивитись всі",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFF9A20B),
                    modifier = Modifier.clickable { onSearchClick() }
                )
            }

            // List of Recent Tickets
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                tickets.take(5).forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(elevation = 1.dp, shape = RoundedCornerShape(12.dp))
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .clickable { onTicketClick(item.id) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.deviceModel,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF181511),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                // Status Badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(getStatusBgColor(item.status))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = item.status,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = getStatusTextColor(item.status)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${item.id} • ${item.issueDescription}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF6B7280),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 2. NEW TICKET SCREEN (Новий талон на ремонт)
// ----------------------------------------------------
@Composable
fun NewTicketScreen(
    onCloseClick: () -> Unit,
    onCreateTicket: (RepairTicket) -> Unit
) {
    var selectedStatus by remember { mutableStateOf("В роботі") }
    var deviceModel by remember { mutableStateOf("") }
    var issueDescription by remember { mutableStateOf("") }
    var devicePassword by remember { mutableStateOf("") }
    var clientName by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("") }
    var assignedWorker by remember { mutableStateOf("Marcus Thorne") }
    var price by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F7F5))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
        ) {
            // Top Bar with camera notch offset (top padding = 28.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(top = 28.dp, bottom = 16.dp, start = 20.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF3F4F6))
                            .clickable { onCloseClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = CloseIcon, contentDescription = "Закрити", tint = Color(0xFF181511))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Новий талон на ремонт",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF181511)
                    )
                }
                Text(text = "🐾", fontSize = 20.sp)
            }

            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                // Initial Status Selector (Auto width by text size, no equal weight)
                Column {
                    Text(
                        text = "Початковий статус",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF6B7280)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("На узгодженні", "В роботі", "Очікує запчастин").forEach { st ->
                            val isSelected = selectedStatus == st
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) getStatusBgColor(st) else Color.White)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) getStatusTextColor(st) else Color(0xFFE5E7EB),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable { selectedStatus = st }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = st,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) getStatusTextColor(st) else Color(0xFF4B5563)
                                )
                            }
                        }
                    }
                }

                // Section 1: Деталі пристрою (no emoji)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 1.dp, shape = RoundedCornerShape(16.dp))
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Деталі пристрою",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF181511)
                    )

                    Column {
                        Text("Модель пристрою", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                        Spacer(modifier = Modifier.height(6.dp))
                        FormTextField(
                            value = deviceModel,
                            onValueChange = { deviceModel = it },
                            placeholder = "наприклад, iPhone 14 Pro Max"
                        )
                    }

                    Column {
                        Text("Опис проблеми", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                        Spacer(modifier = Modifier.height(6.dp))
                        FormTextField(
                            value = issueDescription,
                            onValueChange = { issueDescription = it },
                            placeholder = "Опишіть проблему, фізичні пошкодження тощо...",
                            minLines = 3
                        )
                    }

                    Column {
                        Text("Пароль пристрою / Ключ", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                        Spacer(modifier = Modifier.height(6.dp))
                        FormTextField(
                            value = devicePassword,
                            onValueChange = { devicePassword = it },
                            placeholder = "1234, 0000 або графічний ключ"
                        )
                    }
                }

                // Section 2: Інформація про клієнта (no emoji)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 1.dp, shape = RoundedCornerShape(16.dp))
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Інформація про клієнта",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF181511)
                    )

                    Column {
                        Text("Ім'я клієнта", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                        Spacer(modifier = Modifier.height(6.dp))
                        FormTextField(
                            value = clientName,
                            onValueChange = { clientName = it },
                            placeholder = "ПІБ"
                        )
                    }

                    Column {
                        Text("Номер телефону", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                        Spacer(modifier = Modifier.height(6.dp))
                        FormTextField(
                            value = clientPhone,
                            onValueChange = { clientPhone = it },
                            placeholder = "(0XX) XXX-XX-XX",
                            leadingIcon = { Icon(imageVector = PhoneCallIcon, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(18.dp)) }
                        )
                    }
                }

                // Section 3: Деталі виконання (renamed from Логістика, no emoji)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 1.dp, shape = RoundedCornerShape(16.dp))
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Деталі виконання",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF181511)
                    )

                    Column {
                        Text("Виконавець", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                        Spacer(modifier = Modifier.height(6.dp))
                        DropdownSelector(
                            value = assignedWorker,
                            options = listOf("Marcus Thorne", "Михайло Стівенсон", "Олександр Петренко"),
                            onSelect = { assignedWorker = it }
                        )
                    }

                    Column {
                        Text("Орієнтовна вартість", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                        Spacer(modifier = Modifier.height(6.dp))
                        FormTextField(
                            value = price,
                            onValueChange = { price = it },
                            placeholder = "0.00",
                            leadingText = "₴"
                        )
                    }
                }
            }
        }

        // Sticky Create Button with library_add vector icon
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .shadow(16.dp)
                .background(Color.White)
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 78.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF9A20B))
                    .clickable {
                        val newId = "#R-${(100..999).random()}"
                        val newTicket = RepairTicket(
                            id = newId,
                            status = selectedStatus,
                            deviceModel = deviceModel.ifBlank { "iPhone 14 Pro Max" },
                            createdDate = "Сьогодні",
                            price = price.ifBlank { "0.00" },
                            clientName = clientName.ifBlank { "Клієнт" },
                            clientPhone = clientPhone.ifBlank { "+380 (50) 000-00-00" },
                            issueDescription = issueDescription.ifBlank { "Опис відсутній" },
                            assignedWorker = assignedWorker,
                            devicePassword = devicePassword.ifBlank { "-" },
                            activityHistory = listOf(
                                ActivityEvent(title = "Талон створено", date = "Сьогодні", by = "Адмін")
                            )
                        )
                        onCreateTicket(newTicket)
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = LibraryAddIcon,
                        contentDescription = null,
                        tint = Color(0xFF181511),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Створити талон",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF181511)
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// 3. SEARCH TICKETS SCREEN (Пошук Талонів)
// ----------------------------------------------------
@Composable
fun SearchTicketsScreen(
    tickets: List<RepairTicket>,
    onBackClick: () -> Unit,
    onTicketClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("Усі статуси") }
    var techFilter by remember { mutableStateOf("Усі виконавці") }
    var dateFilter by remember { mutableStateOf("Усі дати") }
    var showDatePickerModal by remember { mutableStateOf(false) }
    var customDateRange by remember { mutableStateOf<String?>(null) }

    val rawDigitsQuery = searchQuery.replace(Regex("[^0-9]"), "")

    // Filter tickets logic (default: all tickets ordered newest to oldest)
    val filteredTickets = tickets.filter { ticket ->
        val matchesQuery = searchQuery.isBlank() ||
                ticket.id.contains(searchQuery, ignoreCase = true) ||
                ticket.clientName.contains(searchQuery, ignoreCase = true) ||
                ticket.deviceModel.contains(searchQuery, ignoreCase = true) ||
                (rawDigitsQuery.length >= 3 && ticket.clientPhone.replace(Regex("[^0-9]"), "").contains(rawDigitsQuery))

        val matchesStatus = statusFilter == "Усі статуси" || ticket.status == statusFilter
        val matchesTech = techFilter == "Усі виконавці" || ticket.assignedWorker == techFilter
        val matchesDate = when (dateFilter) {
            "Останній день" -> ticket.createdDate.contains("24 Жов") || ticket.createdDate.contains("Сьогодні")
            "Останні 7 днів" -> true
            "Останні 30 днів" -> true
            "Останній рік" -> true
            "Обрати проміжок..." -> customDateRange == null || ticket.createdDate.contains("Жов")
            else -> true
        }

        matchesQuery && matchesStatus && matchesTech && matchesDate
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F7F5))) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar with camera notch offset (top padding = 28.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(top = 28.dp, bottom = 16.dp, start = 20.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = ArrowBackIcon, contentDescription = "Назад", tint = Color(0xFF181511))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Пошук Талонів",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF181511)
                )
            }

            // Search Bar & Floating Filter Chips Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Shortened placeholder: "Пошук за ID, клієнтом, назвою..."
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF3F4F6))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = SearchIcon, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text("Пошук за ID, клієнтом, назвою...", color = Color(0xFF9CA3AF), fontSize = 14.sp)
                            }
                            innerTextField()
                        }
                    )
                }

                // Filter Dropdown Chips Row (Floating Dropdown without shifting container height)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Status Filter Chip
                    FloatingDropdownChip(
                        label = if (statusFilter == "Усі статуси") "Статус ▼" else "Статус: $statusFilter ▼",
                        selectedOption = statusFilter,
                        options = listOf("Усі статуси", "На узгодженні", "В роботі", "Очікує запчастин", "Готовий до видачі", "Завершений"),
                        onSelect = { statusFilter = it }
                    )

                    // Tech Filter Chip
                    FloatingDropdownChip(
                        label = if (techFilter == "Усі виконавці") "Виконавець ▼" else "Виконавець: ${formatWorkerName(techFilter)} ▼",
                        selectedOption = techFilter,
                        options = listOf("Усі виконавці", "Михайло Стівенсон", "Олександр Петренко", "Marcus Thorne"),
                        onSelect = { techFilter = it }
                    )

                    // Date Filter Chip
                    FloatingDropdownChip(
                        label = if (customDateRange != null) "Дата: $customDateRange ▼" else if (dateFilter == "Усі дати") "Дата ▼" else "Дата: $dateFilter ▼",
                        selectedOption = dateFilter,
                        options = listOf("Усі дати", "Останній день", "Останні 7 днів", "Останні 30 днів", "Останній рік", "Обрати проміжок..."),
                        onSelect = { choice ->
                            dateFilter = choice
                            if (choice == "Обрати проміжок...") {
                                showDatePickerModal = true
                            } else {
                                customDateRange = null
                            }
                        }
                    )
                }
            }

            // Results Section
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "ОСТАННІ РЕЗУЛЬТАТИ (${filteredTickets.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280),
                    letterSpacing = 1.sp
                )

                filteredTickets.forEach { ticket ->
                    // Ticket Card matching photo layout & format
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(1.dp, shape = RoundedCornerShape(16.dp))
                            .background(Color.White, RoundedCornerShape(16.dp))
                            .clickable { onTicketClick(ticket.id) }
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header Row: Device Title & Status Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ticket.deviceModel,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF181511)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${ticket.id} • ${ticket.issueDescription}",
                                    fontSize = 14.sp,
                                    color = Color(0xFF6B7280),
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 18.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(getStatusBgColor(ticket.status))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = ticket.status,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = getStatusTextColor(ticket.status)
                                )
                            }
                        }

                        Divider(color = Color(0xFFF3F4F6))

                        // Footer Row: Клієнт | Дата | Виконавець (with Tech Name as First Name + First letter of Last Name)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Клієнт", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9CA3AF))
                                Text(ticket.clientName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF181511))
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Дата", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9CA3AF))
                                Text(ticket.createdDate, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF6B7280))
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Виконавець", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9CA3AF))
                                Text("• ${formatWorkerName(ticket.assignedWorker)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2563EB))
                            }
                        }
                    }
                }
            }
        }

        // Custom Date Range Picker Modal Dialog
        if (showDatePickerModal) {
            DateRangePickerModal(
                onDismiss = { showDatePickerModal = false },
                onRangeSelected = { start, end ->
                    customDateRange = "$start - $end"
                    showDatePickerModal = false
                }
            )
        }
    }
}

// ----------------------------------------------------
// 4. TICKET DETAILS SCREEN (Деталі талону)
// ----------------------------------------------------
@Composable
fun TicketDetailsScreen(
    ticket: RepairTicket,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onChangeStatusClick: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var showCopyToast by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F7F5))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
        ) {
            // Top Bar with camera notch offset (top padding = 28.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(top = 28.dp, bottom = 16.dp, start = 20.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { onBackClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = ArrowBackIcon, contentDescription = "Назад", tint = Color(0xFF181511))
                    }
                    Spacer(modifier = Modifier.width(12.dp))

                    // Title Restructuring: "Деталі талону" small on top, Ticket ID large below
                    Column {
                        Text(
                            text = "Деталі талону",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6B7280)
                        )
                        Text(
                            text = ticket.id,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF181511)
                        )
                    }
                }

                // Outlined printer icon print_outlined inside a light gray circle background (#F3F4F6)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3F4F6))
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = PrintOutlinedIcon,
                        contentDescription = "Друк",
                        tint = Color(0xFF181511),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Header overview card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, shape = RoundedCornerShape(16.dp))
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = ticket.deviceModel,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF181511)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Створено ${ticket.createdDate}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF6B7280)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(getStatusBgColor(ticket.status))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = ticket.status,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = getStatusTextColor(ticket.status)
                            )
                        }
                    }

                    Divider(color = Color(0xFFF3F4F6))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Орієнтовна вартість", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF6B7280))
                        Text("${ticket.price} ₴", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF181511))
                    }
                }

                // Client Card with COPY Phone Number Button (Black icon on Gray Circle)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, shape = RoundedCornerShape(16.dp))
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("КЛІЄНТ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9CA3AF), letterSpacing = 1.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(getInitials(ticket.clientName), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(ticket.clientName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF181511))
                            Text(ticket.clientPhone, fontSize = 14.sp, color = Color(0xFF6B7280))
                        }

                        // COPY Phone Number Button: Black vector icon on gray circle background (#F3F4F6)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF3F4F6))
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(ticket.clientPhone))
                                    showCopyToast = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = CopyIcon,
                                contentDescription = "Копіювати номер",
                                tint = Color(0xFF181511),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Issue & Tech Details Card (Without green dot next to tech name)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, shape = RoundedCornerShape(16.dp))
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("ДЕТАЛІ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9CA3AF), letterSpacing = 1.sp)

                    Column {
                        Text("ОПИС ПРОБЛЕМИ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(ticket.issueDescription, fontSize = 15.sp, color = Color(0xFF181511), lineHeight = 20.sp)
                    }

                    if (ticket.devicePassword.isNotBlank() && ticket.devicePassword != "-") {
                        Column {
                            Text("ПАРОЛЬ ПРИСТРОЮ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(ticket.devicePassword, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF181511))
                        }
                    }

                    Column {
                        Text("ВИКОНАВЕЦЬ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDBEAFE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(getInitials(ticket.assignedWorker), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(ticket.assignedWorker, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF181511))
                        }
                    }
                }

                // Activity History Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, shape = RoundedCornerShape(16.dp))
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("ІСТОРІЯ АКТИВНОСТІ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9CA3AF), letterSpacing = 1.sp)

                    ticket.activityHistory.forEachIndexed { index, event ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(if (event.isPrimary) Color(0xFFF9A20B) else Color(0xFFD1D5DB))
                                )
                                if (index < ticket.activityHistory.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(48.dp)
                                            .background(Color(0xFFE5E7EB))
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(event.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF181511))
                                Text(event.date, fontSize = 13.sp, color = Color(0xFF6B7280))

                                if (!event.note.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFF9FAFB))
                                            .padding(10.dp)
                                    ) {
                                        Text("\"${event.note}\"", fontSize = 13.sp, fontStyle = FontStyle.Italic, color = Color(0xFF4B5563))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Copy Phone Confirmation Toast — auto-dismisses after 1 second
        if (showCopyToast) {
            LaunchedEffect(showCopyToast) {
                delay(1000)
                showCopyToast = false
            }
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF181511).copy(alpha = 0.9f))
                    .clickable { showCopyToast = false }
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text("Номер телефону скопійовано", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Medium)
            }
        }

        // Bottom Action Bar
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .shadow(16.dp)
                .background(Color.White)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Edit button (White)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                    .clickable { onEditClick() },
                contentAlignment = Alignment.Center
            ) {
                Text("Редагувати", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))
            }

            // Change status button (Yellow) with autorenew vector icon
            Box(
                modifier = Modifier
                    .weight(1.5f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF9A20B))
                    .clickable { onChangeStatusClick() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = AutorenewIcon,
                        contentDescription = null,
                        tint = Color(0xFF181511),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Змінити статус", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF181511))
                }
            }
        }
    }
}

// ----------------------------------------------------
// 5. UPDATE STATUS MODAL (Оновити статус)
// ----------------------------------------------------
@Composable
fun UpdateStatusModal(
    currentStatus: String,
    onDismiss: () -> Unit,
    onStatusUpdated: (String, String) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(currentStatus) }
    var noteText by remember { mutableStateOf("") }

    val options = listOf(
        "На узгодженні" to Color(0xFF2563EB),
        "В роботі" to Color(0xFFD97706),
        "Очікує запчастин" to Color(0xFFEA580C),
        "Готовий до видачі" to Color(0xFF059669),
        "Завершений" to Color(0xFF4B5563)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color.White)
                .clickable(enabled = false) {}
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(Color(0xFFE5E7EB))
            )

            Text("Оновити статус", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF181511))

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { (statusLabel, dotColor) ->
                    val isSelected = selectedStatus == statusLabel
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFFF9FAFB) else Color.Transparent)
                            .clickable { selectedStatus = statusLabel }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(dotColor))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(statusLabel, fontSize = 16.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = Color(0xFF181511))
                        }

                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .border(2.dp, if (isSelected) Color(0xFFF9A20B) else Color(0xFFD1D5DB), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFF9A20B)))
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Row {
                    Text("Коментар ", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF374151))
                    Text("(Не обов'язково)", fontSize = 14.sp, color = Color(0xFF9CA3AF))
                }
                Spacer(modifier = Modifier.height(6.dp))
                FormTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = "Додайте деталі про цю зміну статусу...",
                    minLines = 3
                )
            }

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF9A20B))
                        .clickable { onStatusUpdated(selectedStatus, noteText) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Оновити статус", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF181511))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Скасувати", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF6B7280))
                }
            }
        }
    }
}

// ----------------------------------------------------
// 6. EDIT TICKET SCREEN (Редагувати талон)
// ----------------------------------------------------
@Composable
fun EditTicketScreen(
    ticket: RepairTicket,
    onBackClick: () -> Unit,
    onSaveTicket: (RepairTicket) -> Unit,
    onDeleteTicket: (String) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(ticket.status) }
    var deviceModel by remember { mutableStateOf(ticket.deviceModel) }
    var issueDescription by remember { mutableStateOf(ticket.issueDescription) }
    var devicePassword by remember { mutableStateOf(ticket.devicePassword) }
    var clientName by remember { mutableStateOf(ticket.clientName) }
    var clientPhone by remember { mutableStateOf(ticket.clientPhone) }
    var assignedWorker by remember { mutableStateOf(ticket.assignedWorker) }
    var price by remember { mutableStateOf(ticket.price) }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F7F5))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
        ) {
            // Top Bar with camera notch offset (top padding = 28.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(top = 28.dp, bottom = 16.dp, start = 20.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { onBackClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = ArrowBackIcon, contentDescription = "Назад", tint = Color(0xFF181511))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Редагувати талон",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6B7280)
                        )
                        Text(
                            text = ticket.id,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF181511)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3F4F6))
                        .clickable { onDeleteTicket(ticket.id) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = DeleteOutlinedIcon, contentDescription = "Видалити", tint = Color(0xFF181511), modifier = Modifier.size(20.dp))
                }
            }

            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                // Section: Поточний статус
                Column {
                    Text(
                        text = "Поточний статус",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF6B7280)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("На узгодженні", "В роботі", "Очікує запчастин", "Готовий до видачі", "Завершений").forEach { st ->
                            val isSelected = selectedStatus == st
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) getStatusBgColor(st) else Color.White)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) getStatusTextColor(st) else Color(0xFFE5E7EB),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable { selectedStatus = st }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = st,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) getStatusTextColor(st) else Color(0xFF4B5563)
                                )
                            }
                        }
                    }
                }

                // Section 1: Деталі пристрою
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 1.dp, shape = RoundedCornerShape(16.dp))
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Деталі пристрою", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF181511))

                    Column {
                        Text("Модель пристрою", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                        Spacer(modifier = Modifier.height(6.dp))
                        FormTextField(value = deviceModel, onValueChange = { deviceModel = it })
                    }

                    Column {
                        Text("Опис проблеми", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                        Spacer(modifier = Modifier.height(6.dp))
                        FormTextField(value = issueDescription, onValueChange = { issueDescription = it }, minLines = 3)
                    }

                    Column {
                        Text("Пароль пристрою / Ключ", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                        Spacer(modifier = Modifier.height(6.dp))
                        FormTextField(
                            value = devicePassword,
                            onValueChange = { devicePassword = it },
                            placeholder = "1234, 0000 або графічний ключ (літери/цифри)"
                        )
                    }
                }

                // Section 2: Інформація про клієнта
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 1.dp, shape = RoundedCornerShape(16.dp))
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Інформація про клієнта", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF181511))

                    Column {
                        Text("Ім'я клієнта", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                        Spacer(modifier = Modifier.height(6.dp))
                        FormTextField(value = clientName, onValueChange = { clientName = it })
                    }

                    Column {
                        Text("Номер телефону", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                        Spacer(modifier = Modifier.height(6.dp))
                        FormTextField(
                            value = clientPhone,
                            onValueChange = { clientPhone = it },
                            leadingIcon = { Icon(imageVector = PhoneCallIcon, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(18.dp)) }
                        )
                    }
                }

                // Section 3: Деталі виконання
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 1.dp, shape = RoundedCornerShape(16.dp))
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Деталі виконання", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF181511))

                    Column {
                        Text("Виконавець", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                        Spacer(modifier = Modifier.height(6.dp))
                        DropdownSelector(
                            value = assignedWorker,
                            options = listOf("Михайло Стівенсон", "Олександр Петренко", "Marcus Thorne"),
                            onSelect = { assignedWorker = it }
                        )
                    }

                    Column {
                        Text("Орієнт. вартість", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                        Spacer(modifier = Modifier.height(6.dp))
                        FormTextField(
                            value = price,
                            onValueChange = { price = it },
                            leadingText = "₴"
                        )
                    }
                }
            }
        }

        // Sticky Save Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .shadow(16.dp)
                .background(Color.White)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF9A20B))
                    .clickable {
                        val updated = ticket.copy(
                            status = selectedStatus,
                            deviceModel = deviceModel,
                            issueDescription = issueDescription,
                            devicePassword = devicePassword,
                            clientName = clientName,
                            clientPhone = clientPhone,
                            assignedWorker = assignedWorker,
                            price = price
                        )
                        onSaveTicket(updated)
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = SaveIcon,
                        contentDescription = null,
                        tint = Color(0xFF181511),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Зберегти зміни",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF181511)
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// DATE RANGE PICKER MODAL DIALOG
// ----------------------------------------------------
@Composable
fun DateRangePickerModal(
    onDismiss: () -> Unit,
    onRangeSelected: (String, String) -> Unit
) {
    var startDay by remember { mutableStateOf(10) }
    var endDay by remember { mutableStateOf(24) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .clickable(enabled = false) {}
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Обрати проміжок дат", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF181511))

            // Calendar Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("‹", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280), modifier = Modifier.clickable { })
                Text("Жовтень 2024", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF181511))
                Text("›", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280), modifier = Modifier.clickable { })
            }

            // Day Headers Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                listOf("Пн", "Вв", "Ср", "Чт", "Пт", "Сб", "Нд").forEach { day ->
                    Text(day, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9CA3AF), textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                }
            }

            // Grid of Days (31 Days simulation)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val totalDays = 31
                val rows = (totalDays + 6) / 7
                for (r in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        for (c in 0 until 7) {
                            val dayNum = r * 7 + c + 1
                            if (dayNum <= totalDays) {
                                val isSelected = dayNum in startDay..endDay
                                val isStartOrEnd = dayNum == startDay || dayNum == endDay

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when {
                                                isStartOrEnd -> Color(0xFFF9A20B)
                                                isSelected -> Color(0xFFFEF3C7)
                                                else -> Color.Transparent
                                            }
                                        )
                                        .clickable {
                                            if (dayNum < startDay) {
                                                startDay = dayNum
                                            } else {
                                                endDay = dayNum
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$dayNum",
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isStartOrEnd) Color(0xFF181511) else if (isSelected) Color(0xFFD97706) else Color(0xFF374151)
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Range preview text
            Text(
                text = "Обрано: $startDay Жов - $endDay Жов",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFD97706)
            )

            // Dialog Actions
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF3F4F6))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Скасувати", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4B5563))
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF9A20B))
                        .clickable { onRangeSelected("$startDay.10", "$endDay.10") },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Застосувати", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF181511))
                }
            }
        }
    }
}

// ----------------------------------------------------
// UI HELPERS & FLOATING DROPDOWN COMPONENT
// ----------------------------------------------------
@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    minLines: Int = 1,
    leadingIcon: (@Composable () -> Unit)? = null,
    leadingText: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF9FAFB))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = if (minLines > 1) 12.dp else 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(modifier = Modifier.width(10.dp))
        }
        if (leadingText != null) {
            Text(leadingText, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280))
            Spacer(modifier = Modifier.width(8.dp))
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            minLines = minLines,
            singleLine = minLines == 1,
            decorationBox = { innerTextField ->
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(placeholder, color = Color(0xFF9CA3AF), fontSize = 14.sp)
                }
                innerTextField()
            }
        )
    }
}

@Composable
fun DropdownSelector(
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF9FAFB))
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                .clickable { expanded = !expanded }
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF181511))
            Icon(imageVector = KeyboardArrowDownIcon, contentDescription = null, tint = Color(0xFF6B7280))
        }

        if (expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
            ) {
                options.forEach { option ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(option)
                                expanded = false
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Text(option, fontSize = 14.sp, color = Color(0xFF374151))
                    }
                }
            }
        }
    }
}

// FLOATING DROPDOWN CHIP - Rendered overlay without layout height shifts
@Composable
fun FloatingDropdownChip(
    label: String,
    selectedOption: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        // Chip Trigger Button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (selectedOption != "Усі статуси" && selectedOption != "Усі виконавці" && selectedOption != "Усі дати")
                        Color(0xFFFEF3C7)
                    else
                        Color(0xFFF3F4F6)
                )
                .border(
                    width = 1.dp,
                    color = if (selectedOption != "Усі статуси" && selectedOption != "Усі виконавці" && selectedOption != "Усі дати")
                        Color(0xFFF9A20B)
                    else
                        Color(0xFFE5E7EB),
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable { expanded = !expanded }
                .padding(horizontal = 14.dp, vertical = 7.dp)
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selectedOption != "Усі статуси" && selectedOption != "Усі виконавці" && selectedOption != "Усі дати")
                    Color(0xFFD97706)
                else
                    Color(0xFF374151)
            )
        }

        // Floating Popup Dropdown Menu
        if (expanded) {
            Popup(
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true)
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(min = 210.dp)
                        .shadow(elevation = 12.dp, shape = RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(14.dp))
                        .padding(vertical = 4.dp)
                ) {
                    options.forEach { option ->
                        val isSelected = option == selectedOption
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isSelected) Color(0xFFFEF3C7).copy(alpha = 0.6f) else Color.Transparent)
                                .clickable {
                                    onSelect(option)
                                    expanded = false
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (option.contains(" ") && (option == "Михайло Стівенсон" || option == "Олександр Петренко" || option == "Marcus Thorne")) formatWorkerName(option) else option,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFFD97706) else Color(0xFF181511)
                            )
                            if (isSelected) {
                                Text("✓", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Divider(color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color)
    )
}

// ----------------------------------------------------
// 9. ORDERS SCREEN (Замовлення - Скоро буде)
// ----------------------------------------------------
@Composable
fun OrdersScreen(
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7F5))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .border(width = 1.dp, color = Color(0xFFE6E2DB))
                    .padding(top = 28.dp, bottom = 16.dp, start = 20.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Замовлення",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF181511),
                    letterSpacing = (-0.5).sp
                )
            }

            // Central Placeholder Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE6E2DB), RoundedCornerShape(24.dp))
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = OrdersIcon,
                            contentDescription = null,
                            tint = Color(0xFFF9A20B),
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Text(
                        text = "Розділ замовлень",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF181511)
                    )

                    Text(
                        text = "Цей функціонал знаходиться у розробці та незабаром буде доступний!",
                        fontSize = 14.sp,
                        color = Color(0xFF6B5E4C),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFFEF3C7))
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "СКОРО",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFD97706),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 10. SETTINGS SCREEN (Профіль та Налаштування)
// ----------------------------------------------------
@Composable
fun SettingsScreen(
    tickets: List<RepairTicket>,
    onManageTeamClick: () -> Unit = {}
) {
    val activeCount = tickets.count { it.status == "В роботі" || it.status == "На узгодженні" || it.status == "Очікує запчастин" }
    val completedCount = tickets.count { it.status == "Готовий до видачі" || it.status == "Завершений" }

    var selectedTheme by remember { mutableStateOf(ThemeMode.DARK) }
    var selectedColorId by remember { mutableStateOf("ocean") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .border(width = 1.dp, color = Color(0xFFE6E2DB))
                    .padding(top = 28.dp, bottom = 16.dp, start = 20.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Налаштування",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF181511),
                    letterSpacing = (-0.5).sp
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Stats Section (2 Cards Grid)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Active Stat Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(128.dp)
                            .shadow(1.dp, shape = RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE6E2DB), RoundedCornerShape(16.dp))
                            .padding(20.dp)
                    ) {
                        // Background Watermark Icon
                        Icon(
                            imageVector = PendingActionsIcon,
                            contentDescription = null,
                            tint = Color(0xFF181511).copy(alpha = 0.05f),
                            modifier = Modifier
                                .size(88.dp)
                                .align(Alignment.BottomEnd)
                        )

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "$activeCount",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF181511)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFEF3C7)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = PendingActionsIcon,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Text(
                                text = "В РОБОТІ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B5E4C),
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // Completed Stat Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(128.dp)
                            .shadow(1.dp, shape = RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE6E2DB), RoundedCornerShape(16.dp))
                            .padding(20.dp)
                    ) {
                        // Background Watermark Icon
                        Icon(
                            imageVector = CheckCircleIcon,
                            contentDescription = null,
                            tint = Color(0xFF181511).copy(alpha = 0.05f),
                            modifier = Modifier
                                .size(88.dp)
                                .align(Alignment.BottomEnd)
                        )

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "$completedCount",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF181511)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFDCFCE7)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = CheckCircleIcon,
                                        contentDescription = null,
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Text(
                                text = "ВИКОНАНО",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B5E4C),
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                // Admin Console Section
                Column {
                    Text(
                        text = "АДМІНІСТРУВАННЯ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B5E4C),
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, shape = RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE6E2DB), RoundedCornerShape(20.dp))
                            .clickable { onManageTeamClick() }
                    ) {
                        // Left Accent Bar
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(5.dp)
                                .align(Alignment.CenterStart)
                                .background(Color(0xFFF9A20B))
                        )

                        // Watermark Security Icon
                        Icon(
                            imageVector = SecurityIcon,
                            contentDescription = null,
                            tint = Color(0xFF181511).copy(alpha = 0.04f),
                            modifier = Modifier
                                .size(110.dp)
                                .align(Alignment.BottomEnd)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF9A20B).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = GroupIcon,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Керування командою",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF181511)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Підтвердження нових користувачів та доступ.",
                                    fontSize = 13.sp,
                                    color = Color(0xFF6B5E4C)
                                )
                            }
                        }
                    }
                }

                // Appearance Section
                AppearanceSection(
                    selectedTheme = selectedTheme,
                    onThemeSelected = { selectedTheme = it },
                    selectedColorId = selectedColorId,
                    onColorSelected = { selectedColorId = it }
                )
            }
        }
    }
}
