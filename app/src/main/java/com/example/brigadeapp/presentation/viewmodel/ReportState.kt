import android.net.Uri

data class ReportState(
    val type: String = "",
    val place: String = "",
    val time: String = "",
    val description: String = "",
    val followUp: Boolean = false,
    val imageUri: String? = null,
    val audioUri: Uri? = null,
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)
