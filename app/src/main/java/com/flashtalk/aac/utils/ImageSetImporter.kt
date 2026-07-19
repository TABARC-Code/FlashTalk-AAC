package com.flashtalk.aac.utils

import android.content.Context
import android.net.Uri
import com.flashtalk.aac.data.Category
import com.flashtalk.aac.data.FlashCard
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.util.UUID
import java.util.zip.ZipInputStream

class ImageSetImporter(private val context: Context) {

    data class ImportManifest(
        @SerializedName("set_name") val setName: String,
        @SerializedName("category_name") val categoryName: String,
        @SerializedName("category_icon") val categoryIcon: String = "📦",
        @SerializedName("category_color") val categoryColor: String = "#95E1D3",
        val cards: List<CardData>
    )

    data class CardData(
        val text: String,
        @SerializedName("image_filename") val imageFilename: String = "",
        // Optional per-card glyph for image-less (JSON) sets — falls back to
        // category_icon when omitted, so existing manifests need no change.
        val icon: String = ""
    )

    sealed class ImportResult {
        data class Success(
            val category: Category,
            val cards: List<FlashCard>,
            val warnings: List<String> = emptyList()
        ) : ImportResult()

        data class Error(val message: String) : ImportResult()
    }

    suspend fun importFromJson(uri: Uri): ImportResult = withContext(Dispatchers.IO) {
        try {
            val json = openStream(uri)?.bufferedReader()?.use { it.readText() }
                ?: return@withContext ImportResult.Error("Could not open the selected file.")
            val manifest = parseManifest(json)
                ?: return@withContext ImportResult.Error(
                    "That file isn't a valid FlashTalk import — check manifest.json format."
                )
            // JSON-only import ships no images; every card falls back to the
            // category's emoji glyph rather than a missing drawable.
            buildResult(manifest, imageFiles = emptyMap())
        } catch (e: Exception) {
            ImportResult.Error("Import failed: ${e.message ?: e.javaClass.simpleName}")
        }
    }

    suspend fun importFromZip(uri: Uri): ImportResult = withContext(Dispatchers.IO) {
        val tempDir = File(context.cacheDir, "import_tmp_${UUID.randomUUID()}")
        try {
            tempDir.mkdirs()
            var manifestJson: String? = null
            val extractedImages = mutableMapOf<String, File>()
            var totalBytes = 0L

            val stream = openStream(uri)
                ?: return@withContext ImportResult.Error("Could not open the selected file.")

            ZipInputStream(stream).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    val baseName = File(entry.name).name
                    if (!entry.isDirectory && baseName.isNotBlank()) {
                        val bytes = zip.readBytes()
                        totalBytes += bytes.size
                        if (totalBytes > MAX_IMPORT_BYTES) {
                            return@withContext ImportResult.Error(
                                "That set is larger than the 50MB import limit."
                            )
                        }
                        if (baseName.equals("manifest.json", ignoreCase = true)) {
                            manifestJson = String(bytes, Charsets.UTF_8)
                        } else {
                            // Extract under a sanitised basename only — never the raw
                            // entry path, which could contain "../" (zip-slip).
                            val outFile = File(tempDir, baseName)
                            if (outFile.canonicalPath.startsWith(tempDir.canonicalPath)) {
                                outFile.writeBytes(bytes)
                                extractedImages[baseName] = outFile
                            }
                        }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }

            val json = manifestJson
                ?: return@withContext ImportResult.Error("ZIP file must contain a manifest.json.")
            val manifest = parseManifest(json)
                ?: return@withContext ImportResult.Error(
                    "manifest.json isn't valid — check it against example_imports/README.md."
                )

            buildResult(manifest, extractedImages)
        } catch (e: Exception) {
            ImportResult.Error("Import failed: ${e.message ?: e.javaClass.simpleName}")
        } finally {
            tempDir.deleteRecursively()
        }
    }

    private fun buildResult(manifest: ImportManifest, imageFiles: Map<String, File>): ImportResult {
        if (manifest.cards.isEmpty()) {
            return ImportResult.Error("manifest.json has no cards to import.")
        }

        val warnings = mutableListOf<String>()
        val destDir = File(context.filesDir, "custom_images").apply { mkdirs() }

        val cards = manifest.cards.mapIndexed { index, cardData ->
            val sourceImage = imageFiles[cardData.imageFilename]
            if (cardData.imageFilename.isNotBlank() && sourceImage == null) {
                warnings.add("${cardData.text}: image \"${cardData.imageFilename}\" was not found in the set.")
            }

            if (sourceImage != null) {
                val destFile = File(destDir, "${UUID.randomUUID()}_${sourceImage.name}")
                sourceImage.copyTo(destFile, overwrite = true)
                FlashCard(
                    categoryId = 0, // caller fills in the real id once the category is inserted
                    text = cardData.text,
                    imagePath = destFile.absolutePath,
                    isCustom = true,
                    order = index
                )
            } else {
                FlashCard(
                    categoryId = 0,
                    text = cardData.text,
                    emoji = cardData.icon.ifBlank { manifest.categoryIcon },
                    isCustom = false,
                    order = index
                )
            }
        }

        val category = Category(
            name = manifest.categoryName,
            icon = manifest.categoryIcon,
            color = manifest.categoryColor,
            isCustom = true
        )

        return ImportResult.Success(category, cards, warnings)
    }

    private fun parseManifest(json: String): ImportManifest? {
        return try {
            val manifest = Gson().fromJson(json, ImportManifest::class.java)
            if (manifest?.categoryName.isNullOrBlank() || manifest?.cards == null) null else manifest
        } catch (e: JsonSyntaxException) {
            null
        }
    }

    private fun openStream(uri: Uri): InputStream? = context.contentResolver.openInputStream(uri)

    companion object {
        private const val MAX_IMPORT_BYTES = 50L * 1024 * 1024
    }
}
