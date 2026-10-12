package hr.gearmory.app.feature.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import hr.gearmory.app.remote.Reservation
import hr.gearmory.app.remote.ReservationClient
import hr.gearmory.app.remote.StaffSession
import hr.gearmory.app.remote.reservationNotice

internal class HomeViewModel : ViewModel() {
    var pending by mutableStateOf<List<Reservation>?>(null)
    var active by mutableStateOf<List<Reservation>?>(null)
    var notice by mutableStateOf<String?>(null)

    fun reload() {
        if (active == null) active = StaffSession.cachedActive
        if (pending == null) pending = StaffSession.cachedPending
        notice = null
        StaffSession.request(ReservationClient::listPending) { result ->
            result.fold(
                onSuccess = {
                    pending = it
                    StaffSession.rememberPending(it)
                },
                onFailure = { if (pending == null) notice = reservationNotice(it) },
            )
        }
        StaffSession.request(ReservationClient::listActive) { result ->
            result.fold(
                onSuccess = {
                    active = it
                    StaffSession.rememberActive(it)
                },
                onFailure = { if (active == null) notice = reservationNotice(it) },
            )
        }
    }
}
