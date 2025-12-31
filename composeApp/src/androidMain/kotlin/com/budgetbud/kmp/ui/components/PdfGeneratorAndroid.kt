package com.budgetbud.kmp.ui.components

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream

class AndroidPdfCanvas(private val canvas: Canvas) : PdfCanvas {
    private val paint = Paint().apply {
        isAntiAlias = true
    }

    override fun drawText(text: String, x: Float, y: Float, fontSize: Float) {
        paint.textSize = fontSize
        canvas.drawText(text, x, y, paint)
    }
}

@Composable
actual fun rememberPdfGenerator(
    fileName: String,
    onResult: (Boolean, String) -> Unit,
    onDraw: (PdfCanvas) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri == null) {
            onResult(false, "Cancelled")
            return@rememberLauncherForActivityResult
        }

        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openFileDescriptor(uri, "w")?.use { pfd ->
                        FileOutputStream(pfd.fileDescriptor).use { output ->
                            val document = PdfDocument()
                            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                            val page = document.startPage(pageInfo)

                            // Wrap the native canvas in our common interface
                            val wrapper = AndroidPdfCanvas(page.canvas)

                            // Execute the custom drawing logic passed from commonMain
                            onDraw(wrapper)

                            document.finishPage(page)
                            document.writeTo(output)
                            document.close()
                        }
                    }
                }
                onResult(true, "Successfully saved $fileName")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to generate PDF")
            }
        }
    }

    return { launcher.launch(fileName) }
}