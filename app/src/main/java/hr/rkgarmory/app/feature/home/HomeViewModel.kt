package hr.rkgarmory.app.feature.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import hr.rkgarmory.app.remote.Reservation
import hr.rkgarmory.app.remote.ReservationClient
import hr.rkgarmory.app.remote.StaffSession
import hr.rkgarmory.app.remote.reservationNotice

internal class HomeViewModel : ViewModel() {
    val pending: List<Reservation>? get() = StaffSession.cachedPending
    val active: List<Reservation>? get() = StaffSession.cachedActive
    var notice by mutableStateOf<String?>(null)
    var refreshing by mutableStateOf(false)

    /** The lists come from sign-in and saves. The server is asked only for a missing one. */
    fun open() {
        // The notice explains a missing list; another screen may have loaded it since.
        if (pending == null || active == null) reload() else notice = null
    }

    fun reload() {
        if (refreshing) return
        refreshing = true
        notice = null
        var left = 2
        val done = {
            left -= 1
            if (left == 0) refreshing = false
        }
        StaffSession.request(ReservationClient::listPending) { result ->
            result.fold(
                onSuccess = { StaffSession.rememberPending(it) },
                onFailure = { if (pending == null) notice = reservationNotice(it) },
            )
            done()
        }
        StaffSession.request(ReservationClient::listActive) { result ->
            result.fold(
                onSuccess = { StaffSession.rememberActive(it) },
                onFailure = { if (active == null) notice = reservationNotice(it) },
            )
            done()
        }
    }
}
