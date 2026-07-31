package lionstore.app

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

const val BASE_URL = "https://z3c4nudkitpel5ch3lcpmpxtii0wfgsj.lambda-url.eu-north-1.on.aws"

@Serializable
data class ApiStatusReceiptRepair(
    val receipt: String = "Активний",
    val repair: String = "На узгодженні"
)

@Serializable
data class ApiDatesInfo(
    @SerialName("repair_period") val repairPeriod: String,
    @SerialName("completed_date") val completedDate: String? = null,
    @SerialName("completed_time") val completedTime: String? = null
)

@Serializable
data class ApiCommentItem(
    val id: String,
    val text: String,
    val author: String,
    val timestamp: String
)

@Serializable
data class ApiTicket(
    val id: String,
    @SerialName("repair_id") val repairId: Int,
    @SerialName("created_at") val createdAt: String,
    @SerialName("date_creation") val dateCreation: String,
    @SerialName("time_creation") val timeCreation: String,
    @SerialName("model_name") val modelName: String,
    @SerialName("repair_description") val repairDescription: String,
    val password: String = "",
    @SerialName("client_name") val clientName: String,
    @SerialName("client_phone_number") val clientPhoneNumber: String,
    val status: ApiStatusReceiptRepair,
    val worker: String,
    @SerialName("final_price") val finalPrice: Int = 0,
    val dates: ApiDatesInfo,
    val comments: List<ApiCommentItem> = emptyList()
)

@Serializable
data class ApiWorker(
    val id: String,
    @SerialName("full_name") val fullName: String,
    val role: String,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class ApiDashboardStats(
    @SerialName("active_count") val activeCount: Int,
    @SerialName("in_progress_count") val inProgressCount: Int,
    @SerialName("waiting_parts_count") val waitingPartsCount: Int,
    @SerialName("completed_count") val completedCount: Int
)

@Serializable
data class ApiRecentActivity(
    @SerialName("repair_id") val repairId: Int,
    @SerialName("model_name") val modelName: String,
    @SerialName("short_description") val shortDescription: String,
    @SerialName("created_at") val createdAt: String,
    val worker: String
)

@Serializable
data class CreateTicketRequest(
    @SerialName("model_name") val modelName: String,
    @SerialName("repair_description") val repairDescription: String,
    val password: String = "",
    @SerialName("client_name") val clientName: String,
    @SerialName("client_phone_number") val clientPhoneNumber: String,
    @SerialName("status_repair") val statusRepair: String = "На узгодженні",
    val worker: String? = null,
    @SerialName("final_price") val finalPrice: Int = 0
)

@Serializable
data class UpdateStatusRequest(
    @SerialName("new_status") val newStatus: String? = null,
    @SerialName("comment_text") val commentText: String? = null,
    val author: String = "Юрій Коломієць"
)

@Serializable
data class EditTicketRequest(
    @SerialName("model_name") val modelName: String? = null,
    @SerialName("repair_description") val repairDescription: String? = null,
    val password: String? = null,
    @SerialName("client_name") val clientName: String? = null,
    @SerialName("client_phone_number") val clientPhoneNumber: String? = null,
    @SerialName("status_repair") val statusRepair: String? = null,
    @SerialName("status_receipt") val statusReceipt: String? = null,
    val worker: String? = null,
    @SerialName("final_price") val finalPrice: Int? = null,
    @SerialName("repair_period") val repairPeriod: String? = null
)

@Serializable
data class CreateWorkerRequest(
    @SerialName("full_name") val fullName: String,
    val role: String = "Майстер"
)

fun ApiTicket.toRepairTicket(): RepairTicket {
    return RepairTicket(
        id = "#R-${repairId}",
        status = status.repair,
        deviceModel = modelName,
        createdDate = dateCreation,
        price = "$finalPrice.00",
        clientName = clientName,
        clientPhone = clientPhoneNumber,
        issueDescription = repairDescription,
        assignedWorker = worker,
        devicePassword = password,
        activityHistory = comments.map { c ->
            ActivityEvent(
                title = c.text,
                date = c.timestamp,
                by = c.author
            )
        }
    )
}

object ApiClient {
    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun getDashboardStats(): ApiDashboardStats {
        return client.get("$BASE_URL/api/v1/dashboard/stats").body()
    }

    suspend fun getRecentActivities(): List<ApiRecentActivity> {
        return client.get("$BASE_URL/api/v1/dashboard/recent-activities").body()
    }

    suspend fun getWorkers(): List<ApiWorker> {
        return client.get("$BASE_URL/api/v1/workers").body()
    }

    suspend fun createWorker(fullName: String, role: String = "Майстер"): ApiWorker {
        return client.post("$BASE_URL/api/v1/workers") {
            contentType(ContentType.Application.Json)
            setBody(CreateWorkerRequest(fullName, role))
        }.body()
    }

    suspend fun searchTickets(query: String? = null, status: String? = null, worker: String? = null): List<ApiTicket> {
        return client.get("$BASE_URL/api/v1/tickets/search") {
            query?.let { parameter("q", it) }
            status?.let { parameter("status", it) }
            worker?.let { parameter("worker", it) }
        }.body()
    }

    suspend fun createTicket(request: CreateTicketRequest): ApiTicket {
        return client.post("$BASE_URL/api/v1/tickets") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun getTicketDetails(repairId: Int): ApiTicket {
        return client.get("$BASE_URL/api/v1/tickets/$repairId").body()
    }

    suspend fun updateTicketStatus(repairId: Int, newStatus: String? = null, commentText: String? = null, author: String = "Юрій Коломієць"): ApiTicket {
        return client.post("$BASE_URL/api/v1/tickets/$repairId/update-status") {
            contentType(ContentType.Application.Json)
            setBody(UpdateStatusRequest(newStatus, commentText, author))
        }.body()
    }

    suspend fun editTicket(repairId: Int, request: EditTicketRequest): ApiTicket {
        return client.put("$BASE_URL/api/v1/tickets/$repairId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
