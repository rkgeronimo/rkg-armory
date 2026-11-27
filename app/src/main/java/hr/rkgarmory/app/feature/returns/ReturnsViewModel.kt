package hr.rkgarmory.app.feature.returns

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import hr.rkgarmory.app.remote.Reservation
import hr.rkgarmory.app.remote.ReservationClient
import hr.rkgarmory.app.remote.StaffSession
import hr.rkgarmory.app.remote.openPieces
import hr.rkgarmory.app.remote.reservationNotice

internal class ReturnsViewModel : ViewModel() {
    val reservations = mutableStateListOf<Reservation>()
    val marks = mutableStateMapOf<String, Int>()
    var notice by mutableStateOf<String?>(null)
    var loading by mutableStateOf(true)
    var refreshing by mutableStateOf(false)
    var saving by mutableStateOf(false)

    /** Shows the shared list. The server is asked only while there is none yet. */
    fun open() {
        val cached = StaffSession.cachedActive ?: return reload()
        reservations.clear()
        reservations.addAll(cached)
        loading = false
    }

    fun reload() {
        if (refreshing) return
        // Full-screen spinner only until the first answer, later reloads show the pull indicator.
        loading = loading && reservations.isEmpty()
        refreshing = true
        notice = null
        StaffSession.request(ReservationClient::listActive) { result ->
            loading = false
            refreshing = false
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

    /** [donation] null keeps the stored amount, blank clears it. */
    fun save(reservationId: Long, donation: String?, onDone: (stillOpen: Boolean) -> Unit) {
        val changes = marks
            .filterKeys { it.startsWith("$reservationId:") }
            .mapKeys { it.key.substringAfter(':') }
        if ((changes.isEmpty() && donation == null) || saving) return
        saving = true
        notice = null
        StaffSession.request({ credentials ->
            ReservationClient.updateReturned(credentials, reservationId, changes, donation)
        }) { result ->
            saving = false
            result.fold(
                onSuccess = { updated ->
                    val index = reservations.indexOfFirst { it.id == updated.id }
                    if (index >= 0) reservations[index] = updated
                    StaffSession.rememberActive(reservations.toList())
                    StaffSession.refreshAll()
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
