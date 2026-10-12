package hr.rkgarmory.app.feature.reservations

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import hr.rkgarmory.app.remote.Reservation
import hr.rkgarmory.app.remote.ReservationClient
import hr.rkgarmory.app.remote.StaffSession
import hr.rkgarmory.app.remote.reservationNotice

internal class ReservationsViewModel : ViewModel() {
    val reservations = mutableStateListOf<Reservation>()
    var notice by mutableStateOf<String?>(null)
    var loading by mutableStateOf(true)
    var saving by mutableStateOf(false)

    fun reload() {
        loading = reservations.isEmpty()
        notice = null
        StaffSession.request(ReservationClient::listAll) { result ->
            loading = false
            result.fold(
                onSuccess = { loaded ->
                    reservations.clear()
                    reservations.addAll(loaded)
                },
                onFailure = { notice = reservationNotice(it) },
            )
        }
    }

    /**
     * Saves the edit, then archives when [archive] is set. The edit goes
     * first because the server refuses to edit an archived reservation.
     */
    fun save(
        reservationId: Long,
        codeByKey: Map<String, String>,
        returnedByKey: Map<String, Int>,
        comment: String?,
        donation: String?,
        archive: Boolean,
        onDone: () -> Unit,
    ) {
        if (saving) return
        val edited = codeByKey.isNotEmpty() || returnedByKey.isNotEmpty() ||
            comment != null || donation != null
        if (!edited && !archive) return
        saving = true
        notice = null
        StaffSession.request({ credentials ->
            var updated: Reservation? = null
            var failure: Throwable? = null
            try {
                if (edited) {
                    updated = ReservationClient.updateReservation(
                        credentials,
                        reservationId,
                        codeByKey,
                        returnedByKey,
                        comment,
                        donation,
                    )
                }
                if (archive) updated = ReservationClient.archive(credentials, reservationId)
            } catch (thrown: Throwable) {
                failure = thrown
            }
            updated to failure
        }) { result ->
            saving = false
            val (updated, failure) = result.getOrElse { null to it }
            if (updated != null) {
                val index = reservations.indexOfFirst { it.id == updated.id }
                if (index >= 0) {
                    if (updated.archived) reservations.removeAt(index) else reservations[index] = updated
                }
            }
            if (failure != null) notice = reservationNotice(failure) else onDone()
        }
    }
}
