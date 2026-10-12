package hr.gearmory.app.feature.issuance

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import hr.gearmory.app.feature.equipment.typedNumberLimit
import hr.gearmory.app.feature.inventory.regulatorTypes
import hr.gearmory.app.remote.Reservation
import hr.gearmory.app.remote.ReservationClient
import hr.gearmory.app.remote.ReservedPiece
import hr.gearmory.app.remote.StaffSession
import hr.gearmory.app.remote.StaffUser
import hr.gearmory.app.remote.reservationNotice

internal const val LoanMaskaDisalica = "Maska i Disalica"
internal const val LoanOlovo = "Olovo"

internal val loanApiKey = mapOf(
    "Odijelo" to "suit",
    "Čizmice" to "boots",
    "Peraje" to "fins",
    "Kompenzator" to "bcd",
    "Rukavice" to "gloves",
    "Regulator" to "regulator",
    LoanMaskaDisalica to "mask",
    LoanOlovo to "lead",
)

internal fun requestedMark(piece: ReservedPiece?): String {
    if (piece == null || !piece.needed) return "—"
    return piece.size?.takeIf { it.isNotBlank() } ?: "Da"
}

internal val loanEquipmentTypes = listOf(
    "Odijelo",
    "Čizmice",
    "Peraje",
    "Kompenzator",
    "Rukavice",
    "Regulator",
    LoanMaskaDisalica,
    LoanOlovo,
)

internal fun loanField(type: String, size: String, number: String): String = when (type) {
    "Čizmice" -> "B$number"
    "Kompenzator" -> "J$size$number"
    "Rukavice" -> "G$size$number"
    "Peraje", "Regulator" -> size + number
    LoanMaskaDisalica -> "MD$number"
    else -> number
}

internal fun loanShown(type: String, size: String, number: String): String {
    if (type == LoanOlovo) return number
    if (!loanReady(type, size, number)) return ""
    return loanField(type, size, number)
}

internal fun loanReady(type: String, size: String, number: String): Boolean {
    if (number.isBlank()) return false
    return when (type) {
        "Peraje", "Kompenzator", "Rukavice" -> size.isNotBlank()
        "Regulator" -> regulatorTypes.any { it.second == size }
        else -> true
    }
}

internal class IssuanceViewModel : ViewModel() {
    val reservations = mutableStateListOf<Reservation>()
    val users = mutableStateListOf<StaffUser>()
    var notice by mutableStateOf<String?>(null)
    var loading by mutableStateOf(true)
    var saving by mutableStateOf(false)

    fun reload() {
        if (reservations.isEmpty()) {
            StaffSession.cachedPending?.let { reservations.addAll(it) }
        }
        loading = reservations.isEmpty()
        notice = null
        StaffSession.request(ReservationClient::listPending) { result ->
            loading = false
            result.fold(
                onSuccess = { loaded ->
                    StaffSession.rememberPending(loaded)
                    reservations.clear()
                    reservations.addAll(loaded)
                },
                onFailure = { if (reservations.isEmpty()) notice = reservationNotice(it) },
            )
        }
    }

    val equipmentValues = mutableStateMapOf<String, String>().apply {
        loanEquipmentTypes.forEach { put(it, "") }
    }
    val equipmentSizes = mutableStateMapOf<String, String>()

    fun pressKey(equipment: String, key: String) {
        val current = equipmentValues[equipment].orEmpty()
        val candidate = when (key) {
            "⌫" -> current.dropLast(1)
            "X" -> if (equipment == LoanOlovo) current else current + key
            else -> current + key
        }
        equipmentValues[equipment] = if (equipment == LoanOlovo) {
            candidate.filter { it.isDigit() }.trimStart('0').take(9)
        } else {
            candidate
                .uppercase()
                .filter { it.isDigit() || it == 'X' }
                .take(typedNumberLimit(equipment))
        }
    }

    fun clearEquipment(equipment: String) {
        equipmentValues[equipment] = ""
        equipmentSizes.remove(equipment)
    }

    fun clearAll() {
        loanEquipmentTypes.forEach { equipmentValues[it] = "" }
        equipmentSizes.clear()
    }

    fun loadUsers() {
        StaffSession.request(ReservationClient::listUsers) { result ->
            result.fold(
                onSuccess = { loaded ->
                    users.clear()
                    users.addAll(loaded.sortedBy { it.userName })
                },
                onFailure = { notice = reservationNotice(it) },
            )
        }
    }

    fun createWalkIn(userId: Long, onDone: () -> Unit) {
        val codes = issuedCodes()
        if (codes.isEmpty() || saving) return
        saving = true
        notice = null
        StaffSession.request({ credentials ->
            ReservationClient.createReservation(credentials, userId, codes)
        }) { result ->
            saving = false
            result.fold(
                onSuccess = { created ->
                    if (created.state == 0) {
                        reservations.add(0, created)
                        StaffSession.rememberPending(reservations.toList())
                    } else {
                        val active = StaffSession.cachedActive.orEmpty()
                            .filter { it.id != created.id }
                        StaffSession.rememberActive(listOf(created) + active)
                    }
                    clearAll()
                    onDone()
                },
                onFailure = { notice = reservationNotice(it) },
            )
        }
    }

    fun issue(reservationId: Long, onDone: () -> Unit) {
        val codes = issuedCodes()
        if (codes.isEmpty() || saving) return
        saving = true
        notice = null
        StaffSession.request({ credentials ->
            ReservationClient.assignIssued(credentials, reservationId, codes)
        }) { result ->
            saving = false
            result.fold(
                onSuccess = { updated ->
                    val index = reservations.indexOfFirst { it.id == updated.id }
                    if (index >= 0) {
                        if (updated.state == 0) reservations[index] = updated
                        else reservations.removeAt(index)
                    }
                    StaffSession.rememberPending(reservations.toList())
                    if (updated.state != 0) {
                        val active = StaffSession.cachedActive.orEmpty()
                            .filter { it.id != updated.id }
                        StaffSession.rememberActive(active + updated)
                    }
                    clearAll()
                    onDone()
                },
                onFailure = { notice = reservationNotice(it) },
            )
        }
    }

    private fun issuedCodes(): Map<String, String> = buildMap {
        loanEquipmentTypes.forEach { type ->
            val key = loanApiKey[type] ?: return@forEach
            val number = equipmentValues[type].orEmpty()
            val size = equipmentSizes[type].orEmpty()
            if (!loanReady(type, size, number)) return@forEach
            val code = loanField(type, size, number)
            if (code.isNotBlank()) put(key, code)
        }
    }
}
