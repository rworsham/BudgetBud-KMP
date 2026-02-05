package com.budgetbud.kmp.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.PDPageContentStream
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import org.apache.pdfbox.pdmodel.font.PDType1Font

class DesktopPdfCanvas(
    private val contentStream: PDPageContentStream
) : PdfCanvas {

    override fun drawText(text: String, x: Float, y: Float, fontSize: Float) {
        contentStream.beginText()
        contentStream.setFont(PDType1Font.HELVETICA, fontSize)
        contentStream.newLineAtOffset(x, y)
        contentStream.showText(text)
        contentStream.endText()
    }
}

@Composable
actual fun rememberPdfGenerator(
    fileName: String,
    onResult: (Boolean, String) -> Unit,
    onDraw: (PdfCanvas) -> Unit
): () -> Unit {
    val scope = rememberCoroutineScope()

    return {
        val fileDialog = FileDialog(null as Frame?, "Save PDF", FileDialog.SAVE).apply {
            file = fileName
            isVisible = true
        }

        val directory = fileDialog.directory
        val selectedFile = fileDialog.file

        if (directory != null && selectedFile != null) {
            val fullPath = File(directory, selectedFile)

            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        val document = PDDocument()
                        val page = PDPage(PDRectangle.A4)
                        document.addPage(page)

                        val contentStream = PDPageContentStream(document, page)
                        val wrapper = DesktopPdfCanvas(contentStream)

                        onDraw(wrapper)

                        contentStream.close()
                        document.save(fullPath)
                        document.close()
                    }
                    onResult(true, "Successfully saved to ${fullPath.absolutePath}")
                } catch (e: Exception) {
                    onResult(false, e.message ?: "Failed to generate PDF")
                }
            }
        } else {
            onResult(false, "Cancelled")
        }
    }
}