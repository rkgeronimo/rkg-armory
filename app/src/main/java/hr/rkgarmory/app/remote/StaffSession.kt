package hr.rkgarmory.app.remote

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.concurrent.Executors

/**
 * Signed-in staff member and the lists every screen shares. The lists are
 * loaded at sign-in and again after each save ([refreshAll]); entering a
 * screen only shows them. They are Compose state, so screens follow when a
 * refresh lands.
 */
internal object StaffSession {
    var credentials: StaffCredentials? = null
        private set

    var cachedActive: List<Reservation>? by mutableStateOf(null)
        private set

    var cachedPending: List<Reservation>? by mutableStateOf(null)
        private set

    var cachedInventory: List<InventoryPiece>? by mutableStateOf(null)
        private set

    /** Every reservation that is not archived, for the Reservations screen. */
    var cachedReservations: List<Reservation>? by mutableStateOf(null)
        private set

    private val io = Executors.newFixedThreadPool(2)
    private val main = Handler(Looper.getMainLooper())

    /**
     * The active list checks the credentials. Home opens first and shows both
     * lists, so sign-in waits for the pending one too; the two load side by side.
     */
    fun signIn(username: String, password: String, onResult: (String?) -> Unit) {
        val next = StaffCredentials(username.trim(), password.replace(" ", ""))
        var active: List<Reservation>? = null
        var pending: Result<List<Reservation>>? = null
        fun finish() {
            val loaded = active ?: return
            val other = pending ?: return
            credentials = next
            cachedActive = loaded
            cachedPending = other.getOrNull()
            onResult(null)
            load(ReservationClient::listInventory) { cachedInventory = it }
            load(ReservationClient::listAll) { cachedReservations = it }
        }
        background({ ReservationClient.listActive(next) }) { result ->
            result.fold(
                onSuccess = {
                    active = it
                    finish()
                },
                onFailure = { onResult(reservationNotice(it)) },
            )
        }
        background({ ReservationClient.listPending(next) }) {
            pending = it
            finish()
        }
    }

    fun rememberActive(list: List<Reservation>) {
        cachedActive = list
    }

    fun rememberPending(list: List<Reservation>) {
        cachedPending = list
    }

    fun rememberInventory(list: List<InventoryPiece>) {
        cachedInventory = list
    }

    fun rememberReservations(list: List<Reservation>) {
        cachedReservations = list
    }

    /**
     * After a save: a reservation or a piece changed on the server, which
     * can move it between lists, so every shared list is asked for again.
     */
    fun refreshAll() {
        load(ReservationClient::listPending) { cachedPending = it }
        load(ReservationClient::listActive) { cachedActive = it }
        load(ReservationClient::listInventory) { cachedInventory = it }
        load(ReservationClient::listAll) { cachedReservations = it }
    }

    /** Background load into a cache. An answer that lands after sign-out is dropped. */
    private fun <T> load(block: (StaffCredentials) -> T, store: (T) -> Unit) {
        val owner = credentials
        request(block) { result ->
            result.onSuccess { if (credentials === owner) store(it) }
        }
    }

    fun signOut() {
        credentials = null
        cachedActive = null
        cachedPending = null
        cachedInventory = null
        cachedReservations = null
    }

    fun <T> request(block: (StaffCredentials) -> T, onResult: (Result<T>) -> Unit) {
        val current = credentials
        if (current == null) {
            onResult(Result.failure(ReservationException(401, "rkg_forbidden")))
            return
        }
        background({ block(current) }, onResult)
    }

    private fun <T> background(block: () -> T, onResult: (Result<T>) -> Unit) {
        io.execute {
            val result = runCatching(block)
            main.post { onResult(result) }
        }
    }
}
