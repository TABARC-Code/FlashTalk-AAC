package com.flashtalk.aac.utils

import android.content.Context
import android.net.Uri
import com.flashtalk.aac.data.Category
import com.flashtalk.aac.data.FlashCard
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Writes a category out in the exact ZIP+manifest format ImageSetImporter
 * reads, so export -> import round-trips. Reuses ImageSetImporter's own
 * ImportManifest/CardData rather than a second copy of that schema —
 * two definitions of the same file format drifting apart is exactly how
 * "export produces something import can't read" bugs happen.
 */
class ImageSetExporter(private val context: Context) {

    sealed class ExportResult {
        data class Success(val cardCount: Int) : ExportResult()
        data class Error(val message: String) : ExportResult()
    }

    suspend fun exportCategory(category: Category, cards: List<FlashCard>, destUri: Uri): ExportResult =
        withContext(Dispatchers.IO) {
            try {
                val manifest = buildManifest(category, cards)
                val json = Gson().toJson(manifest)

                val output = context.contentResolver.openOutputStream(destUri)
                    ?: return@withContext ExportResult.Error("Could not open the destination file.")

                output.use { out ->
                    ZipOutputStream(out).use { zip ->
                        zip.putNextEntry(ZipEntry("manifest.json"))
                        zip.write(json.toByteArray(Charsets.UTF_8))
                        zip.closeEntry()

                        cards.forEach { card ->
                            val imageFile = customPhotoFileFor(card) ?: return@forEach
                            zip.putNextEntry(ZipEntry(imageFilenameFor(card)))
                            imageFile.inputStream().use { it.copyTo(zip) }
                            zip.closeEntry()
                        }
                    }
                }

                ExportResult.Success(cards.size)
            } catch (e: Exception) {
                ExportResult.Error(e.message ?: e.javaClass.simpleName)
            }
        }

    companion object {
        // Pure, Context-free, and public on the companion so unit tests can
        // call them without an Android runtime — ImageSetExporterTest does.

        fun buildManifest(category: Category, cards: List<FlashCard>): ImageSetImporter.ImportManifest {
            return ImageSetImporter.ImportManifest(
                setName = category.name,
                categoryName = category.name,
                categoryIcon = category.icon,
                categoryColor = category.color,
                cards = cards.map { card ->
                    val hasPhoto = card.isCustom && card.imagePath.isNotBlank()
                    ImageSetImporter.CardData(
                        text = card.text,
                        imageFilename = if (hasPhoto) imageFilenameFor(card) else "",
                        icon = if (hasPhoto) "" else card.emoji
                    )
                }
            )
        }

        fun imageFilenameFor(card: FlashCard): String {
            val extension = File(card.imagePath).extension.ifBlank { "jpg" }
            return "card_${card.id}.$extension"
        }

        private fun customPhotoFileFor(card: FlashCard): File? {
            if (!card.isCustom || card.imagePath.isBlank()) return null
            return File(card.imagePath).takeIf { it.exists() }
        }
    }
}
