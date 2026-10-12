package hr.gearmory.app.feature.returns

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import hr.gearmory.app.remote.Reservation
import hr.gearmory.app.remote.ReservationClient
import hr.gearmory.app.remote.StaffSession
import hr.gearmory.app.remote.openPieces
import hr.gearmory.app.remote.reservationNotice

internal class ReturnsViewModel : ViewModel() {
    val reservations = mutableStateListOf<Reservation>()
    val marks = mutableStateMapOf<String, Int>()
    var notice by mutableStateOf<String?>(null)
    var loading by mutableStateOf(true)
    var saving by mutableStateOf(false)

    fun reload() {
        if (reservations.isEmpty()) {
            StaffSession.cachedActive?.let { reservations.addAll(it) }
        }
        loading = reservations.isEmpty()
        notice = null
        StaffSession.request(ReservationClient::listActive) { result ->
            loading = false
            result.fold(
                onSuccess = { loaded ->
                    StaffSession.rememberActive(loaded)
                    reservations.clear()
                    reservations.addAll(loaded)
                },
                onFailure = { if (reservations.isEmpty()) notice = reservationNotice(it) },
            )
        }
    }

    fun mark(reservationId: Long, key: String, returned: Int) {
        val mark = "$reservationId:$key"
        if (marks[mark] == returned) marks.remove(mark) else marks[mark] = returned
    }

    fun clearMarks() {
        marks.clear()
    }

    fun save(reservationId: Long, onDone: (stillOpen: Boolean) -> Unit) {
        val changes = marks
            .filterKeys { it.startsWith("$reservationId:") }
            .mapKeys { it.key.substringAfter(':') }
        if (changes.isEmpty() || saving) return
        saving = true
        notice = null
        StaffSession.request({ credentials ->
            ReservationClient.updateReturned(credentials, reservationId, changes)
        }) { result ->
            saving = false
            result.fold(
                onSuccess = { updated ->
                    val index = reservations.indexOfFirst { it.id == updated.id }
                    if (index >= 0) reservations[index] = updated
                    StaffSession.rememberActive(reservations.toList())
                    changes.keys.forEach { marks.remove("$reservationId:$it") }
                    onDone(updated.openPieces().isNotEmpty())
                },
                onFailure = {
                    notice = reservationNotice(it)
                    onDone(true)
                },
            )
        }
    }
}
