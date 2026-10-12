package hr.gearmory.app.feature.inventory

import hr.gearmory.app.feature.equipment.codeLetter
import hr.gearmory.app.feature.equipment.typedNumberLimit
import hr.gearmory.app.feature.equipment.equipmentTypes
import hr.gearmory.app.feature.equipment.pieceConditions
import hr.gearmory.app.feature.equipment.pieceSize
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

internal const val CsvHeader = "Vrsta;Sifra;Velicina;Debljina;Stanje;Kolicina;Napomena;Datum"
private const val LegacyThicknessHeader = "Vrsta;Sifra;Velicina;Debljina;Stanje;Kolicina;Napomena"
private const val LegacyNoteHeader = "Vrsta;Sifra;Velicina;Stanje;Kolicina;Napomena"
private const val LegacyCsvHeader = "Vrsta;Sifra;Velicina;Stanje;Kolicina"
internal val suitThicknesses = listOf("3mm", "5mm", "7mm")
private val enteredOnFormat = DateTimeFormatter.ofPattern("dd.MM.uuuu")
    .withResolverStyle(ResolverStyle.STRICT)

internal fun todayEntered(): String = LocalDate.now().format(enteredOnFormat)

internal fun enteredOnOk(raw: String): Boolean {
    if (raw.isEmpty()) return true
    if (raw.length != 10) return false
    return try {
        LocalDate.parse(raw, enteredOnFormat)
        true
    } catch (_: DateTimeParseException) {
        false
    }
}
internal const val CsvReadError = "Csv se ne da čitati."

internal fun skippedRowsNotice(count: Int): String = "$count redaka maknuto. Ne daju se čitati."

internal data class InventoryRead(
    val entries: List<InventoryEntry>,
    val dropped: Int,
)
internal const val CsvWriteError = "Csv nije zapisan."
internal const val ChipOstalo = "Ostalo"
internal const val ChipRazno = "Razno"

internal val quantityTypes = listOf(
    "Olovo 1kg",
    "Olovo 2kg",
    "Pojas",
    "Maska",
    "Disalica",
    "Kadica",
)

internal val inventoryChips = equipmentTypes + ChipOstalo + ChipRazno

internal val regulatorTypes = listOf(
    "Apeks" to "RA",
    "Mares" to "RM",
    "Scubapro" to "RS",
)

internal fun thicknessOk(type: String, thickness: String): Boolean =
    if (type == "Odijelo") thickness in suitThicknesses else thickness.isEmpty()

internal fun sizeOk(type: String, size: String): Boolean =
    if (type == "Kompenzator") size in inventorySizes(type) else size.isEmpty()

internal fun shownSize(type: String, code: String, stored: String): String =
    if (type == "Kompenzator") stored else pieceSize(type, code)

internal fun inventorySizes(type: String): List<String> = when (type) {
    "Peraje" -> listOf("S", "R", "XL")
    "Kompenzator" -> listOf("XS", "S", "M", "L", "XL", "XXL")
    "Rukavice" -> listOf("S", "M", "L", "XL")
    else -> emptyList()
}

private val blockedRaznoNames = buildSet {
    addAll(equipmentTypes)
    addAll(quantityTypes)
    add("Cizmice")
    add(ChipOstalo)
    add(ChipRazno)
}

internal fun InventoryEntry.isQuantity(): Boolean = type !in equipmentTypes

internal fun toFileType(type: String): String = when (type) {
    "Čizmice" -> "Cizmice"
    "Dišalica" -> "Disalica"
    else -> type
}

internal fun fromFileType(type: String): String = when (type) {
    "Cizmice" -> "Čizmice"
    "Dišalica" -> "Disalica"
    else -> type
}

private fun numberOk(text: String, limit: Int): Boolean =
    text.isNotEmpty() && text.length <= limit && text.all { it.isDigit() || it == 'X' }

internal fun pieceCodeOk(type: String, code: String): Boolean {
    if (code.isEmpty() || type !in equipmentTypes) return false
    val sizes = inventorySizes(type).sortedByDescending { it.length }
    val limit = typedNumberLimit(type)
    return when (type) {
        "Peraje" -> {
            val size = sizes.firstOrNull { code.startsWith(it) } ?: return false
            val rest = code.removePrefix(size)
            rest.length in 1..2 && rest.all { it.isDigit() || it == 'X' }
        }
        "Rukavice" -> {
            val letter = codeLetter(type)
            if (!code.startsWith(letter)) return false
            val body = code.removePrefix(letter)
            val size = sizes.firstOrNull { body.startsWith(it) } ?: return false
            numberOk(body.removePrefix(size), limit)
        }
        "Čizmice" -> {
            val letter = codeLetter(type)
            if (!code.startsWith(letter)) return false
            numberOk(code.removePrefix(letter), limit)
        }
        "Kompenzator" -> {
            val letter = codeLetter(type)
            if (!code.startsWith(letter)) return false
            val body = code.removePrefix(letter)
            val size = sizes.firstOrNull { body.startsWith(it) } ?: return false
            numberOk(body.removePrefix(size), limit)
        }
        "Regulator" -> {
            val prefix = regulatorTypes.firstOrNull { code.startsWith(it.second) }?.second ?: return false
            numberOk(code.removePrefix(prefix), limit)
        }
        else -> numberOk(code, limit)
    }
}

// Stari zapis kompenzatora: J + broj, uz veličinu u zasebnom stupcu (prije CJV formata).
private fun legacyCompensatorOk(code: String): Boolean {
    val body = code.removePrefix(codeLetter("Kompenzator"))
    return body != code && numberOk(body, typedNumberLimit("Kompenzator"))
}

internal fun normalizeQuantity(raw: String): String? {
    val text = raw.trim()
    if (text.isEmpty() || text.any { !it.isDigit() }) return null
    if (text.length > 1 && text.startsWith('0')) return null
    val value = text.toLongOrNull() ?: return null
    if (value <= 0L) return null
    return value.toString()
}

internal fun raznoNameOk(raw: String): String? {
    val name = raw.trim()
    if (name.isEmpty()) return null
    if (name.any { it.code > 127 || it == ';' || it == '"' || it == '\n' || it == '\r' }) return null
    if (name in blockedRaznoNames) return null
    return name
}

internal fun napomenaOk(raw: String): String {
    val folded = raw
        .replace('č', 'c')
        .replace('ć', 'c')
        .replace('ž', 'z')
        .replace('š', 's')
        .replace('đ', 'd')
        .replace('Č', 'C')
        .replace('Ć', 'C')
        .replace('Ž', 'Z')
        .replace('Š', 'S')
        .replace('Đ', 'D')
    return folded
        .filter { it.code <= 127 && it != ';' && it != '"' && it != '\n' && it != '\r' }
        .trim()
}

internal fun popisSorted(entries: List<InventoryEntry>): List<InventoryEntry> {
    val rank = (equipmentTypes + quantityTypes).withIndex().associate { it.value to it.index }
    return entries.sortedWith(
        compareBy<InventoryEntry> { rank[it.type] ?: Int.MAX_VALUE }
            .thenBy { if (it.type in rank) "" else it.type }
            .thenBy { it.code },
    )
}

internal fun encodeInventory(entries: List<InventoryEntry>): String = buildString {
    append(CsvHeader)
    append("\r\n")
    entries.forEach { entry ->
        val size = if (entry.isQuantity()) "" else shownSize(entry.type, entry.code, entry.size)
        val thickness = if (entry.type == "Odijelo") entry.thickness else ""
        append(toFileType(entry.type))
        append(';')
        append(entry.code)
        append(';')
        append(size)
        append(';')
        append(thickness)
        append(';')
        append(entry.condition)
        append(';')
        append(entry.quantity)
        append(';')
        append(entry.note)
        append(';')
        append(entry.enteredOn)
        append("\r\n")
    }
}

internal fun inventoryBytes(entries: List<InventoryEntry>): ByteArray {
    val bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
    return bom + encodeInventory(entries).toByteArray(Charsets.UTF_8)
}

internal fun decodeInventory(bytes: ByteArray): String? {
    val start = if (
        bytes.size >= 3 &&
        bytes[0] == 0xEF.toByte() &&
        bytes[1] == 0xBB.toByte() &&
        bytes[2] == 0xBF.toByte()
    ) {
        3
    } else {
        0
    }
    return try {
        Charsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes, start, bytes.size - start))
            .toString()
    } catch (_: CharacterCodingException) {
        null
    }
}

internal fun parseInventory(text: String): InventoryRead? {
    val lines = text.split('\n').map { it.removeSuffix("\r") }
    var end = lines.size
    while (end > 0 && lines[end - 1].isBlank()) end--
    if (end == 0) return null
    val columns = when (lines[0]) {
        CsvHeader -> 8
        LegacyThicknessHeader -> 7
        LegacyNoteHeader -> 6
        LegacyCsvHeader -> 5
        else -> return null
    }
    val hasThickness = lines[0] == CsvHeader || lines[0] == LegacyThicknessHeader
    val hasDate = lines[0] == CsvHeader
    val parsed = mutableListOf<InventoryEntry>()
    var dropped = 0
    for (index in 1 until end) {
        val line = lines[index]
        val fields = if (line.isBlank()) null else parseCsvRow(line)
        val entry = if (fields == null || fields.size != columns) {
            null
        } else {
            rowToEntry(fields.map { it.trim() }, index, hasThickness, hasDate)
        }
        if (entry == null || conflicts(parsed, entry)) {
            dropped++
            continue
        }
        parsed += entry
    }
    return InventoryRead(parsed, dropped)
}

private fun rowToEntry(
    fields: List<String>,
    index: Int,
    hasThickness: Boolean,
    hasDate: Boolean,
): InventoryEntry? {
    val type = fromFileType(fields[0])
    val code = fields[1]
    val size = fields[2]
    val rawThickness = if (hasThickness) fields[3] else ""
    val thickness = if (rawThickness == "-") "" else rawThickness
    val shift = if (hasThickness) 1 else 0
    val condition = fields[3 + shift]
    val quantity = fields[4 + shift]
    val note = fields.getOrElse(5 + shift) { "" }
    val enteredOn = if (hasDate) fields[6 + shift] else ""
    if (!enteredOnOk(enteredOn)) return null
    return when {
        type in equipmentTypes ->
            pieceEntry(type, code, size, condition, quantity, note, thickness, enteredOn, index)
        type in quantityTypes ->
            quantityEntry(type, code, size, thickness, condition, quantity, note, enteredOn, false, index)
        else -> {
            val name = raznoNameOk(type) ?: return null
            quantityEntry(name, code, size, thickness, condition, quantity, note, enteredOn, true, index)
        }
    }
}

private fun pieceEntry(
    type: String,
    code: String,
    size: String,
    condition: String,
    quantity: String,
    note: String,
    thickness: String,
    enteredOn: String,
    index: Int,
): InventoryEntry? {
    if (!pieceCodeOk(type, code) && !(type == "Kompenzator" && legacyCompensatorOk(code))) return null
    if (condition !in pieceConditions) return null
    if (quantity.isNotEmpty() || note.isNotEmpty()) return null
    if (type == "Odijelo") {
        if (thickness.isNotEmpty() && thickness !in suitThicknesses) return null
    } else if (thickness.isNotEmpty()) {
        return null
    }
    val storedSize = if (type == "Kompenzator") {
        if (size !in inventorySizes(type)) return null
        size
    } else {
        ""
    }
    return InventoryEntry(
        index.toString(),
        type,
        code,
        condition,
        "",
        thickness = thickness,
        size = storedSize,
        enteredOn = enteredOn,
    )
}

private fun quantityEntry(
    type: String,
    code: String,
    size: String,
    thickness: String,
    condition: String,
    quantity: String,
    note: String,
    enteredOn: String,
    allowNote: Boolean,
    index: Int,
): InventoryEntry? {
    if (code.isNotEmpty() || size.isNotEmpty() || thickness.isNotEmpty() || condition.isNotEmpty()) return null
    val count = normalizeQuantity(quantity) ?: return null
    val storedNote = if (allowNote) napomenaOk(note) else note
    if (!allowNote && storedNote.isNotEmpty()) return null
    return InventoryEntry(index.toString(), type, "", "", count, storedNote, enteredOn = enteredOn)
}

private fun conflicts(entries: List<InventoryEntry>, entry: InventoryEntry): Boolean = if (entry.isQuantity()) {
    entries.any { it.isQuantity() && it.type == entry.type }
} else {
    entries.any { !it.isQuantity() && it.type == entry.type && it.code == entry.code }
}

private fun parseCsvRow(line: String): List<String>? {
    val fields = mutableListOf<String>()
    val current = StringBuilder()
    var quoted = false
    var index = 0
    while (index < line.length) {
        val char = line[index]
        if (quoted) {
            if (char == '"') {
                if (index + 1 < line.length && line[index + 1] == '"') {
                    current.append('"')
                    index += 2
                    continue
                }
                quoted = false
                index++
                continue
            }
            current.append(char)
            index++
            continue
        }
        when (char) {
            ';' -> {
                fields += current.toString()
                current.clear()
                index++
            }
            '"' -> {
                quoted = true
                index++
            }
            else -> {
                current.append(char)
                index++
            }
        }
    }
    if (quoted) return null
    fields += current.toString()
    return fields
}
