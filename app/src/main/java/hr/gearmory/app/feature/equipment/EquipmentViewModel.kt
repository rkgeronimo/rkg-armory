package hr.gearmory.app.feature.equipment

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import hr.gearmory.app.remote.InventoryPiece
import hr.gearmory.app.remote.ReservationClient
import hr.gearmory.app.remote.ReservationException
import hr.gearmory.app.remote.StaffSession
import hr.gearmory.app.remote.reservationNotice
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal data class Piece(
    val id: String,
    val type: String,
    val code: String,
    val condition: String,
    val status: String,
    val holder: String? = null,
    val issuedOn: String? = null,
    val size: String = "",
    val apiType: String = "",
    val state: Int = 0,
    val note: String = "",
)

internal val inventoryStates = listOf(
    0 to StatusInStockLabel,
    1 to StatusIssuedLabel,
    2 to "Neispravno",
    3 to "Izgubljeno",
    4 to "Otpisano",
    5 to "Obrisano",
)

internal fun typeTabLabel(name: String): String = when (name) {
    "Kompenzator" -> "KPL"
    "Regulator" -> "Reg"
    else -> name
}

internal val inventoryTypes = listOf(
    "suit" to "Odijelo",
    "boots" to "Čizmice",
    "fins" to "Peraje",
    "bcd" to "Kompenzator",
    "gloves" to "Rukavice",
    "regulator" to "Regulator",
    "mask" to "Maska",
    "lead" to "Olovo",
)

internal val inventoryStatuses = listOf(
    StatusInStockLabel,
    StatusIssuedLabel,
    "Neispravno",
    "Izgubljeno",
    "Otpisano",
)

internal val equipmentTypes = listOf(
    "Odijelo",
    "Čizmice",
    "Peraje",
    "Kompenzator",
    "Rukavice",
    "Regulator",
)

internal val pieceConditions = listOf(
    "Novo",
    "Dobro",
    "Za otpis",
    "Neispravno",
)

internal const val StatusInStockLabel = "Na stanju"
internal const val StatusIssuedLabel = "Izdano"

internal fun typedNumberLimit(type: String): Int = when (type) {
    "Odijelo", "Čizmice" -> 4
    else -> 2
}

internal fun codeLetter(type: String): String = when (type) {
    "Čizmice" -> "B"
    "Kompenzator" -> "J"
    "Rukavice" -> "G"
    else -> ""
}

internal fun pieceSize(type: String, code: String): String {
    val letter = codeLetter(type)
    val body = if (letter.isEmpty() || code.startsWith(letter)) code.removePrefix(letter) else return ""
    val prefixes = when (type) {
        "Peraje" -> listOf("XL", "S", "R")
        "Kompenzator" -> listOf("XXL", "XL", "XS", "S", "M", "L")
        "Rukavice" -> listOf("XL", "S", "M", "L")
        "Odijelo", "Čizmice" -> return digitSize(body)
        else -> return ""
    }
    return prefixes.firstOrNull { body.startsWith(it) }.orEmpty()
}

private fun digitSize(body: String): String {
    if (body.length >= 2 && body[0].isDigit() && body[1].isDigit()) {
        return body.take(2).trimStart('0').ifEmpty { "0" }
    }
    return ""
}

private val issueDateFormat = DateTimeFormatter.ofPattern("dd.MM.uuuu")

internal class EquipmentViewModel : ViewModel() {
    val pieces = mutableStateListOf<Piece>()
    var notice by mutableStateOf<String?>(null)
    var loading by mutableStateOf(true)
    var saving by mutableStateOf(false)

    fun reload() {
        if (pieces.isEmpty()) {
            StaffSession.cachedInventory?.let { cached ->
                pieces.addAll(cached.map { it.toPiece() })
            }
        }
        loading = pieces.isEmpty()
        notice = null
        StaffSession.request(ReservationClient::listInventory) { result ->
            loading = false
            result.fold(
                onSuccess = { loaded ->
                    StaffSession.rememberInventory(loaded)
                    pieces.clear()
                    pieces.addAll(loaded.map { it.toPiece() })
                },
                onFailure = { if (pieces.isEmpty()) notice = reservationNotice(it) },
            )
        }
    }

    fun save(
        piece: Piece,
        state: Int,
        note: String,
        onDone: () -> Unit,
    ) {
        if (saving || piece.id.isBlank()) return
        val nextState = state.takeIf { it != piece.state }
        val nextNote = note.takeIf { it != piece.note }
        if (nextState == null && nextNote == null) {
            onDone()
            return
        }
        saving = true
        notice = null
        StaffSession.request({ credentials ->
            ReservationClient.updateInventory(
                credentials,
                piece.id,
                null,
                null,
                nextState,
                nextNote,
            )
        }) { result ->
            saving = false
            result.fold(
                onSuccess = { updated ->
                    val index = pieces.indexOfFirst { it.id == piece.id }
                    if (updated.state == 5) {
                        if (index >= 0) pieces.removeAt(index)
                    } else if (index >= 0) {
                        pieces[index] = updated.toPiece()
                    }
                    StaffSession.rememberInventory(
                        StaffSession.cachedInventory.orEmpty().let { cached ->
                            if (updated.state == 5) {
                                cached.filter { it.id != updated.id }
                            } else {
                                cached.map { if (it.id == updated.id) updated else it }
                            }
                        },
                    )
                    onDone()
                },
                onFailure = { notice = saveNotice(it) },
            )
        }
    }
}

private fun saveNotice(error: Throwable): String = when (error) {
    is ReservationException -> when (error.http) {
        404 -> "Šifra nije pronađena."
        400 -> "Unos nije valjan."
        else -> reservationNotice(error)
    }
    else -> reservationNotice(error)
}

private fun InventoryPiece.toPiece(): Piece {
    val label = typeLabel.ifBlank {
        inventoryTypes.firstOrNull { it.first == type }?.second ?: type
    }
    return Piece(
        id = id,
        type = label,
        code = id,
        condition = "",
        status = status,
        holder = userName?.takeIf { it.isNotBlank() },
        issuedOn = shownDate(issueDate),
        size = size.trim().takeUnless { it.equals("null", true) }.orEmpty(),
        apiType = type,
        state = state,
        note = note?.trim().orEmpty(),
    )
}

private fun shownDate(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    val day = raw.take(10)
    return try {
        LocalDate.parse(day).format(issueDateFormat)
    } catch (_: Exception) {
        day
    }
}
