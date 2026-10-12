package hr.gearmory.app.remote

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors

internal object StaffSession {
    var credentials: StaffCredentials? = null
        private set

    var cachedActive: List<Reservation>? = null
        private set

    var cachedPending: List<Reservation>? = null
        private set

    var cachedInventory: List<InventoryPiece>? = null
        private set

    private val io = Executors.newFixedThreadPool(2)
    private val main = Handler(Looper.getMainLooper())

    fun signIn(username: String, password: String, onResult: (String?) -> Unit) {
        val next = StaffCredentials(username.trim(), password.replace(" ", ""))
        io.execute {
            val loaded = try {
                ReservationClient.listActive(next)
            } catch (thrown: Throwable) {
                main.post { onResult(reservationNotice(thrown)) }
                return@execute
            }
            main.post {
                credentials = next
                cachedActive = loaded
                onResult(null)
                request(ReservationClient::listPending) { result ->
                    result.onSuccess { cachedPending = it }
                }
                request(ReservationClient::listInventory) { result ->
                    result.onSuccess { cachedInventory = it }
                }
            }
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

    fun signOut() {
        credentials = null
        cachedActive = null
        cachedPending = null
        cachedInventory = null
    }

    fun <T> request(block: (StaffCredentials) -> T, onResult: (Result<T>) -> Unit) {
        val current = credentials
        if (current == null) {
            onResult(Result.failure(ReservationException(401, "rkg_forbidden")))
            return
        }
        io.execute {
            val result = runCatching { block(current) }
            main.post { onResult(result) }
        }
    }
}
