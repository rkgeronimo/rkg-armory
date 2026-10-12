package hr.gearmory.app.remote

import android.net.Uri
import android.os.Build
import android.util.Base64
import hr.gearmory.app.BuildConfig
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

internal val ReservationBase: String = if (BuildConfig.DEBUG) {
    val host = if (isEmulator()) "10.0.2.2" else "10.1.2.161"
    "http://$host:8080/wp-json/rkg/v1"
} else {
    "https://rkgeronimo.hr/wp-json/rkg/v1"
}

private fun isEmulator(): Boolean {
    val fingerprint = Build.FINGERPRINT
    return fingerprint.startsWith("generic")
        || fingerprint.contains("emulator")
        || Build.MODEL.contains("Emulator")
        || Build.MODEL.contains("Android SDK built for")
        || Build.HARDWARE.contains("goldfish")
        || Build.HARDWARE.contains("ranchu")
        || Build.PRODUCT.contains("sdk")
}

internal val equipmentKeys = listOf(
    "mask",
    "regulator",
    "suit",
    "boots",
    "gloves",
    "fins",
    "bcd",
    "lead",
)

internal data class StaffCredentials(
    val username: String,
    val applicationPassword: String,
) {
    fun header(): String {
        val token = "$username:${applicationPassword.replace(" ", "")}"
        val encoded = Base64.encodeToString(token.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        return "Basic $encoded"
    }
}

internal data class ReservedPiece(
    val label: String,
    val size: String?,
    val code: String?,
    val needed: Boolean,
    val returned: Int?,
)

internal data class StaffUser(
    val id: Long,
    val userName: String,
)

internal data class Reservation(
    val id: Long,
    val userId: Long,
    val userName: String,
    val excursionId: Long?,
    val excursion: String?,
    val state: Int,
    val status: String,
    val comment: String?,
    val equipment: Map<String, ReservedPiece>,
)

internal class ReservationException(
    val http: Int,
    val code: String?,
    val missing: List<String> = emptyList(),
) : Exception(code ?: http.toString())

internal fun reservationNotice(error: Throwable): String = when (error) {
    is ReservationException -> when (error.code) {
        "rkg_forbidden" -> "Nema ovlasti."
        "rkg_invalid_return" -> "Oznaka nije valjana."
        "rkg_not_found" -> "Zahtjev nije pronađen."
        "rkg_invalid" -> "Korisnik nije valjan."
        "rkg_unavailable" -> unavailableNotice(error)
        else -> "Stranica nije odgovorila."
    }
    else -> "Stranica nije dostupna."
}

private fun unavailableNotice(error: ReservationException): String {
    val labels = error.missing.mapNotNull { key -> pieceLabels[key] }
    return if (labels.isEmpty()) {
        "Zahtjev nije dostupan."
    } else {
        "Oprema nije dostupna: ${labels.joinToString(", ")}."
    }
}

private val pieceLabels = mapOf(
    "mask" to "Maska i disalica",
    "regulator" to "Regulator",
    "suit" to "Odijelo",
    "boots" to "Čizmice",
    "gloves" to "Rukavice",
    "fins" to "Peraje",
    "bcd" to "Kompenzator",
    "lead" to "Olovo",
)

internal fun Reservation.listLabel(): String {
    val trip = excursion?.takeIf { it.isNotBlank() }
    return if (trip == null) userName else "$userName  $trip"
}

internal fun Reservation.openPieces(): List<Pair<String, ReservedPiece>> =
    equipmentKeys.mapNotNull { key ->
        val piece = equipment[key] ?: return@mapNotNull null
        if (piece.code.isNullOrBlank()) return@mapNotNull null
        if (piece.returned == 0 || piece.returned == 3) return@mapNotNull null
        key to piece
    }

internal fun pieceTitle(key: String, piece: ReservedPiece): String =
    if (key == "lead") "Olovo" else piece.label

internal fun pieceMark(key: String, piece: ReservedPiece): String =
    if (key == "lead") piece.size ?: piece.code.orEmpty() else piece.code.orEmpty()

internal fun pieceSize(key: String, piece: ReservedPiece): String? =
    if (key == "lead") null else piece.size?.takeIf { it.isNotBlank() }

internal data class InventoryPiece(
    val id: String,
    val type: String,
    val typeLabel: String,
    val size: String,
    val state: Int,
    val status: String,
    val userName: String?,
    val issueDate: String?,
    val note: String?,
)

internal object ReservationClient {
    fun listActive(credentials: StaffCredentials): List<Reservation> = list(credentials, 1)

    fun listPending(credentials: StaffCredentials): List<Reservation> = list(credentials, 0)

    fun listUsers(credentials: StaffCredentials): List<StaffUser> {
        val body = call(credentials, "GET", "/users", null)
        val array = JSONArray(body)
        return List(array.length()) { index ->
            val json = array.getJSONObject(index)
            StaffUser(
                id = json.getLong("id"),
                userName = json.optString("user_name"),
            )
        }
    }

    fun createReservation(
        credentials: StaffCredentials,
        userId: Long,
        codeByKey: Map<String, String>,
    ): Reservation {
        val equipment = JSONObject()
        codeByKey.forEach { (key, code) ->
            if (key !in equipmentKeys || code.isBlank()) return@forEach
            equipment.put(key, JSONObject().put("code", code))
        }
        val body = JSONObject().put("user_id", userId)
        if (equipment.length() > 0) body.put("equipment", equipment)
        val text = call(credentials, "POST", "/reservations", body.toString())
        return parseReservation(JSONObject(text))
    }

    fun listInventory(credentials: StaffCredentials): List<InventoryPiece> {
        val body = call(credentials, "GET", "/inventory", null)
        val array = JSONArray(body)
        return List(array.length()) { index -> parseInventory(array.getJSONObject(index)) }
    }

    fun updateInventory(
        credentials: StaffCredentials,
        id: String,
        type: String?,
        size: String?,
        state: Int?,
        note: String?,
    ): InventoryPiece {
        val body = JSONObject()
        if (type != null) body.put("type", type)
        if (size != null) body.put("size", size)
        if (state != null) body.put("state", state)
        if (note != null) body.put("note", note)
        if (body.length() == 0) throw ReservationException(400, null)
        val text = call(
            credentials,
            "POST",
            "/inventory/${Uri.encode(id)}",
            body.toString(),
        )
        return parseInventory(JSONObject(text))
    }

    private fun list(credentials: StaffCredentials, state: Int): List<Reservation> {
        val body = call(credentials, "GET", "/reservations?state=$state", null)
        val array = JSONArray(body)
        return List(array.length()) { index -> parseReservation(array.getJSONObject(index)) }
    }

    fun updateReturned(
        credentials: StaffCredentials,
        id: Long,
        returnedByKey: Map<String, Int>,
    ): Reservation {
        val equipment = JSONObject()
        returnedByKey.forEach { (key, returned) ->
            if (key !in equipmentKeys || returned !in setOf(0, 1, 3)) return@forEach
            equipment.put(key, JSONObject().put("returned", returned))
        }
        val body = call(
            credentials,
            "POST",
            "/reservations/$id",
            JSONObject().put("equipment", equipment).toString(),
        )
        return parseReservation(JSONObject(body))
    }

    fun assignIssued(
        credentials: StaffCredentials,
        id: Long,
        codeByKey: Map<String, String>,
    ): Reservation {
        val equipment = JSONObject()
        codeByKey.forEach { (key, code) ->
            if (key !in equipmentKeys || code.isBlank()) return@forEach
            equipment.put(
                key,
                JSONObject().put("code", code).put("returned", 1),
            )
        }
        val body = call(
            credentials,
            "POST",
            "/reservations/$id",
            JSONObject().put("equipment", equipment).toString(),
        )
        return parseReservation(JSONObject(body))
    }

    private fun call(
        credentials: StaffCredentials,
        method: String,
        path: String,
        json: String?,
    ): String {
        val url = URL(ReservationBase + path)
        val https = url.protocol.equals("https", ignoreCase = true)
        val localHttp = BuildConfig.DEBUG && url.protocol.equals("http", ignoreCase = true)
        if (!https && !localHttp) {
            throw ReservationException(0, null)
        }
        val connection = url.openConnection() as HttpURLConnection
        connection.instanceFollowRedirects = false
        connection.requestMethod = method
        connection.connectTimeout = 20_000
        connection.readTimeout = 20_000
        connection.setRequestProperty("Authorization", credentials.header())
        connection.setRequestProperty("Accept", "application/json")
        try {
            if (json != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.outputStream.use { stream ->
                    stream.write(json.toByteArray(Charsets.UTF_8))
                }
            }
            val http = connection.responseCode
            val text = (if (http in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader(Charsets.UTF_8)
                ?.use { it.readText() }
                .orEmpty()
            if (http in 300..399) {
                throw ReservationException(http, null)
            }
            if (http !in 200..299) {
                throw ReservationException(http, errorCode(text), errorEquipment(text))
            }
            return text
        } finally {
            connection.disconnect()
        }
    }

    private fun errorCode(text: String): String? = try {
        JSONObject(text).optNullableString("code")
    } catch (_: Exception) {
        null
    }

    private fun errorEquipment(text: String): List<String> = try {
        val array = JSONObject(text).optJSONObject("data")?.optJSONArray("equipment") ?: JSONArray()
        List(array.length()) { array.getString(it) }
    } catch (_: Exception) {
        emptyList()
    }

    private fun parseReservation(json: JSONObject): Reservation {
        val equipmentJson = json.optJSONObject("equipment") ?: JSONObject()
        val equipment = equipmentKeys.mapNotNull { key ->
            val item = equipmentJson.optJSONObject(key) ?: return@mapNotNull null
            key to ReservedPiece(
                label = item.optString("label"),
                size = item.optNullableString("size"),
                code = item.optNullableString("code"),
                needed = item.optBoolean("needed", false),
                returned = if (item.isNull("returned")) null else item.getInt("returned"),
            )
        }.toMap()
        return Reservation(
            id = json.getLong("id"),
            userId = json.getLong("user_id"),
            userName = json.optString("user_name"),
            excursionId = if (json.isNull("excursion_id")) null else json.getLong("excursion_id"),
            excursion = json.optNullableString("excursion"),
            state = json.getInt("state"),
            status = json.optString("status"),
            comment = json.optNullableString("comment"),
            equipment = equipment,
        )
    }

    private fun parseInventory(json: JSONObject): InventoryPiece = InventoryPiece(
        id = json.optString("id"),
        type = json.optString("type"),
        typeLabel = json.optString("type_label"),
        size = json.optNullableString("size").orEmpty().takeUnless { it.equals("null", true) }.orEmpty(),
        state = json.optInt("state"),
        status = json.optString("status"),
        userName = json.optNullableString("user_name"),
        issueDate = json.optNullableString("issue_date"),
        note = json.optNullableString("note"),
    )
}

private fun JSONObject.optNullableString(name: String): String? {
    if (!has(name) || isNull(name)) return null
    return optString(name)
}
