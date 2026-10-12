package hr.gearmory.app.feature.inventory

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import java.io.File

internal class InventoryFileStore(private val context: Context) {
    fun ensure(): Boolean {
        if (!canUseDownloads()) return false
        if (exists()) return true
        return writeBytes(inventoryBytes(emptyList()))
    }

    fun read(): InventoryLoad {
        if (!canUseDownloads()) return InventoryLoad.Unavailable
        val bytes = readBytes() ?: return InventoryLoad.Unavailable
        val text = decodeInventory(bytes) ?: return InventoryLoad.Corrupt
        val read = parseInventory(text) ?: return InventoryLoad.Corrupt
        return InventoryLoad.Ready(read.entries, read.dropped)
    }

    fun write(entries: List<InventoryEntry>): Boolean {
        if (!canUseDownloads()) return false
        return writeBytes(inventoryBytes(entries))
    }

    private fun canUseDownloads(): Boolean {
        if (Build.VERSION.SDK_INT >= 29) return true
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    private fun exists(): Boolean = if (Build.VERSION.SDK_INT >= 29) {
        mediaUri() != null
    } else {
        legacyFile().isFile
    }

    private fun readBytes(): ByteArray? = if (Build.VERSION.SDK_INT >= 29) {
        val uri = mediaUri() ?: return null
        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
    } else {
        val file = legacyFile()
        if (!file.isFile) null else file.readBytes()
    }

    private fun writeBytes(bytes: ByteArray): Boolean = if (Build.VERSION.SDK_INT >= 29) {
        writeMedia(bytes)
    } else {
        writeLegacy(bytes)
    }

    private fun writeLegacy(bytes: ByteArray): Boolean {
        val file = legacyFile()
        val folder = file.parentFile ?: return false
        if (!folder.exists() && !folder.mkdirs()) return false
        val temp = File(folder, ".$FileName.tmp")
        return try {
            temp.writeBytes(bytes)
            if (!temp.renameTo(file)) {
                file.writeBytes(bytes)
                temp.delete()
            }
            file.isFile
        } catch (_: Exception) {
            false
        }
    }

    private fun writeMedia(bytes: ByteArray): Boolean {
        val resolver = context.contentResolver
        val existing = mediaUri()
        if (existing == null) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, FileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return false
            val wrote = writeToUri(uri, bytes)
            val done = ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }
            resolver.update(uri, done, null, null)
            if (!wrote) resolver.delete(uri, null, null)
            return wrote
        }
        return writeToUri(existing, bytes)
    }

    private fun writeToUri(uri: android.net.Uri, bytes: ByteArray): Boolean = try {
        context.contentResolver.openOutputStream(uri, "wt")?.use { stream ->
            stream.write(bytes)
            stream.flush()
        } != null
    } catch (_: Exception) {
        false
    }

    private fun mediaUri(): android.net.Uri? {
        val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Downloads._ID, MediaStore.MediaColumns.RELATIVE_PATH)
        val selection = "${MediaStore.MediaColumns.DISPLAY_NAME}=?"
        val args = arrayOf(FileName)
        context.contentResolver.query(collection, projection, selection, args, null)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH)
            var fallback: android.net.Uri? = null
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val uri = ContentUris.withAppendedId(collection, id)
                val path = cursor.getString(pathColumn).orEmpty()
                if (path.startsWith(Environment.DIRECTORY_DOWNLOADS)) return uri
                if (fallback == null) fallback = uri
            }
            return fallback
        }
        return null
    }

    private fun legacyFile(): File {
        val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return File(downloads, FileName)
    }

    private companion object {
        const val FileName = "inventura.csv"
    }
}

internal sealed class InventoryLoad {
    data class Ready(val entries: List<InventoryEntry>, val dropped: Int) : InventoryLoad()
    data object Corrupt : InventoryLoad()
    data object Unavailable : InventoryLoad()
}
