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
    object Search : Screen()
    object NewTicket : Screen()
    data class TicketDetails(val ticketId: String) : Screen()
    data class EditTicket(val ticketId: String) : Screen()
    object UserRegistration : Screen()
    object AccountPending : Screen()
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

val PetsIcon: ImageVector by lazy {
    ImageVector.Builder(name = "Pets", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(4.5f, 11f)
            curveTo(5.88f, 11f, 7f, 9.88f, 7f, 8.5f)
            curveTo(7f, 7.12f, 5.88f, 6f, 4.5f, 6f)
            curveTo(3.12f, 6f, 2f, 7.12f, 2f, 8.5f)
            curveTo(2f, 9.88f, 3.12f, 11f, 4.5f, 11f)
            close()
            moveTo(8.5f, 7.5f)
            curveTo(9.88f, 7.5f, 11f, 6.38f, 11f, 5f)
            curveTo(11f, 3.62f, 9.88f, 2.5f, 8.5f, 2.5f)
            curveTo(7.12f, 2.5f, 6f, 3.62f, 6f, 5f)
            curveTo(6f, 6.38f, 7.12f, 7.5f, 8.5f, 7.5f)
            close()
            moveTo(15.5f, 7.5f)
            curveTo(16.88f, 7.5f, 18f, 6.38f, 18f, 5f)
            curveTo(18f, 3.62f, 16.88f, 2.5f, 15.5f, 2.5f)
            curveTo(14.12f, 2.5f, 13f, 3.62f, 13f, 5f)
            curveTo(13f, 6.38f, 14.12f, 7.5f, 15.5f, 7.5f)
            close()
            moveTo(19.5f, 11f)
            curveTo(20.88f, 11f, 22f, 9.88f, 22f, 8.5f)
            curveTo(22f, 7.12f, 20.88f, 6f, 19.5f, 6f)
            curveTo(18.12f, 6f, 17f, 7.12f, 17f, 8.5f)
            curveTo(17f, 9.88f, 18.12f, 11f, 19.5f, 11f)
            close()
            moveTo(17.34f, 14.86f)
            curveTo(16.48f, 13.72f, 14.93f, 13f, 12f, 13f)
            curveTo(9.07f, 13f, 7.52f, 13.72f, 6.66f, 14.86f)
            curveTo(5.61f, 16.24f, 5.75f, 18.11f, 7.08f, 19.26f)
            curveTo(8.42f, 20.41f, 10.33f, 21.5f, 12f, 21.5f)
            curveTo(13.67f, 21.5f, 15.58f, 20.41f, 16.92f, 19.26f)
            curveTo(18.25f, 18.11f, 18.39f, 16.24f, 17.34f, 14.86f)
            close()
        }
    }.build()
}

val PersonIcon: ImageVector by lazy {
    ImageVector.Builder(name = "Person", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 12f)
            curveTo(14.21f, 12f, 16f, 10.21f, 16f, 8f)
            curveTo(16f, 5.79f, 14.21f, 4f, 12f, 4f)
            curveTo(9.79f, 4f, 8f, 5.79f, 8f, 8f)
            curveTo(8f, 10.21f, 9.79f, 12f, 12f, 12f)
            close()
            moveTo(12f, 14f)
            curveTo(9.33f, 14f, 4f, 15.34f, 4f, 18f)
            verticalLineTo(20f)
            horizontalLineTo(20f)
            verticalLineTo(18f)
            curveTo(20f, 15.34f, 14.67f, 14f, 12f, 14f)
            close()
        }
    }.build()
}

val BadgeIcon: ImageVector by lazy {
    ImageVector.Builder(name = "Badge", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(20f, 7f)
            horizontalLineTo(14f)
            verticalLineTo(4f)
            curveTo(14f, 2.9f, 13.1f, 2f, 12f, 2f)
            curveTo(10.9f, 2f, 10f, 2.9f, 10f, 4f)
            verticalLineTo(7f)
            horizontalLineTo(4f)
            curveTo(2.9f, 7f, 2f, 7.9f, 2f, 9f)
            verticalLineTo(19f)
            curveTo(2f, 20.1f, 2.9f, 21f, 4f, 21f)
            horizontalLineTo(20f)
            curveTo(21.1f, 21f, 22f, 20.1f, 22f, 19f)
            verticalLineTo(9f)
            curveTo(22f, 7.9f, 21.1f, 7f, 20f, 7f)
            close()
            moveTo(12f, 4f)
            curveTo(12.55f, 4f, 13f, 4.45f, 13f, 5f)
            verticalLineTo(7f)
            horizontalLineTo(11f)
            verticalLineTo(5f)
            curveTo(11f, 4.45f, 11.45f, 4f, 12f, 4f)
            close()
            moveTo(20f, 19f)
            horizontalLineTo(4f)
            verticalLineTo(9f)
            horizontalLineTo(20f)
            verticalLineTo(19f)
            close()
            moveTo(9f, 12f)
            curveTo(9f, 13.1f, 9.9f, 14f, 11f, 14f)
            curveTo(12.1f, 14f, 13f, 13.1f, 13f, 12f)
            curveTo(13f, 10.9f, 12.1f, 10f, 11f, 10f)
            curveTo(9.9f, 10f, 9f, 10.9f, 9f, 12f)
            close()
            moveTo(15f, 16.5f)
            curveTo(15f, 15.17f, 12.33f, 14.5f, 11f, 14.5f)
            curveTo(9.67f, 14.5f, 7f, 15.17f, 7f, 16.5f)
            verticalLineTo(17f)
            horizontalLineTo(15f)
            verticalLineTo(16.5f)
            close()
        }
    }.build()
}

val ArrowForwardIcon: ImageVector by lazy {
    ImageVector.Builder(name = "ArrowForward", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 4f)
            lineTo(10.59f, 5.41f)
            lineTo(16.17f, 11f)
            horizontalLineTo(4f)
            verticalLineTo(13f)
            horizontalLineTo(16.17f)
            lineTo(10.59f, 18.59f)
            lineTo(12f, 20f)
            lineTo(20f, 12f)
            lineTo(12f, 4f)
            close()
        }
    }.build()
}

val HourglassTopIcon: ImageVector by lazy {
    ImageVector.Builder(name = "HourglassTop", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(6f, 2f)
            verticalLineTo(8f)
            lineTo(10f, 12f)
            lineTo(6f, 16f)
            verticalLineTo(22f)
            horizontalLineTo(18f)
            verticalLineTo(16f)
            lineTo(14f, 12f)
            lineTo(18f, 8f)
            verticalLineTo(2f)
            horizontalLineTo(6f)
            close()
            moveTo(16f, 16.5f)
            verticalLineTo(20f)
            horizontalLineTo(8f)
            verticalLineTo(16.5f)
            lineTo(12f, 12.5f)
            lineTo(16f, 16.5f)
            close()
            moveTo(12f, 11.5f)
            lineTo(8f, 7.5f)
            verticalLineTo(4f)
            horizontalLineTo(16f)
            verticalLineTo(7.5f)
            lineTo(12f, 11.5f)
            close()
        }
    }.build()
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
                            },
                            onRegistrationClick = { currentScreen = Screen.UserRegistration },
                            onAccountPendingClick = { currentScreen = Screen.AccountPending }
                        )
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
                    is Screen.UserRegistration -> {
                        UserRegistrationScreen(
                            onCloseClick = handleBack,
                            onContinueClick = { firstName, lastName ->
                                currentScreen = Screen.AccountPending
                            }
                        )
                    }
                    is Screen.AccountPending -> {
                        AccountPendingScreen(
                            onCheckStatusClick = {
                                currentScreen = Screen.Dashboard
                            }
                        )
                    }
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
    onTicketClick: (String) -> Unit,
    onRegistrationClick: () -> Unit = {},
    onAccountPendingClick: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // Header with Camera Notch Offset (top padding = 28.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp, start = 20.dp, end = 20.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Сервіс Ремонту",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF181511),
                    lineHeight = 38.sp
                )

                // Quick auth / registration link button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(20.dp))
                        .clickable { onRegistrationClick() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = PetsIcon, contentDescription = null, tint = Color(0xFFF9A20B), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Реєстрація", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))
                    }
                }
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
                .padding(16.dp)
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
                    .padding(20.dp),
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
                                Text("${event.date} • ${event.by}", fontSize = 13.sp, color = Color(0xFF6B7280))

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
// 7. USER REGISTRATION SCREEN (LionStore - User Registration)
// ----------------------------------------------------
@Composable
fun UserRegistrationScreen(
    onCloseClick: () -> Unit,
    onContinueClick: (firstName: String, lastName: String) -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7F5))
    ) {
        // Decorative Watermark Background (Paw print icon)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = PetsIcon,
                contentDescription = null,
                tint = Color(0xFFF9A20B).copy(alpha = 0.05f),
                modifier = Modifier.size(320.dp)
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp, bottom = 16.dp, start = 20.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.size(36.dp))

                // Center logo
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = PetsIcon,
                        contentDescription = "LionStore Logo",
                        tint = Color(0xFFF9A20B),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LionStore",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF181511)
                    )
                }

                // Close Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE5E7EB), CircleShape)
                        .clickable { onCloseClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = CloseIcon,
                        contentDescription = "Закрити",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Main Content Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Let's get to know you",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF181511),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Enter your details to start your journey with LionStore.",
                    fontSize = 15.sp,
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Form Container
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // First Name Field
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "First Name",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF374151)
                        )
                        FormTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            placeholder = "Enter your first name",
                            leadingIcon = {
                                Icon(
                                    imageVector = PersonIcon,
                                    contentDescription = null,
                                    tint = Color(0xFF9CA3AF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )
                    }

                    // Last Name Field
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Last Name",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF374151)
                        )
                        FormTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            placeholder = "Enter your last name",
                            leadingIcon = {
                                Icon(
                                    imageVector = BadgeIcon,
                                    contentDescription = null,
                                    tint = Color(0xFF9CA3AF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )
                    }
                }
            }

            // Sticky Footer CTA
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp)
                    .background(Color(0xFFF8F7F5).copy(alpha = 0.95f))
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF9A20B))
                        .clickable { onContinueClick(firstName, lastName) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Continue",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF181511)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = ArrowForwardIcon,
                            contentDescription = null,
                            tint = Color(0xFF181511),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = "By continuing, you agree to LionStore's Terms of Service and Privacy Policy.",
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

// ----------------------------------------------------
// 8. ACCOUNT PENDING SCREEN (LionStore - Account Pending)
// ----------------------------------------------------
@Composable
fun AccountPendingScreen(
    onCheckStatusClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7F7))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = PetsIcon,
                            contentDescription = "LionStore Logo",
                            tint = Color(0xFFF5A10F),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LionStore",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF181511)
                        )
                    }
                }
            }

            // Main Content Area with Centered Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 4.dp, shape = RoundedCornerShape(28.dp))
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(28.dp))
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Glowing Icon Container
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF3C7))
                                .border(1.dp, Color(0xFFFDE68A), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = HourglassTopIcon,
                                contentDescription = "Hourglass",
                                tint = Color(0xFFF5A10F),
                                modifier = Modifier.size(44.dp)
                            )
                        }
                        // Top Right Accent Dot
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .align(Alignment.TopEnd)
                                .clip(CircleShape)
                                .background(Color(0xFFF5A10F))
                                .border(2.dp, Color.White, CircleShape)
                        )
                    }

                    // Text Content
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Account Pending",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF181511),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Welcome to LionStore! Your account is currently under review. Please wait for an administrator to grant you access.",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }

                    // Status Badge Pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF3F4F6))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFB923C))
                        )
                        Text(
                            text = "IN REVIEW",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B7280),
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action to enter CRM / check status
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(14.dp))
                        .clickable { onCheckStatusClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "До Головної (Демо доступу)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF374151)
                    )
                }
            }
        }
    }
}
