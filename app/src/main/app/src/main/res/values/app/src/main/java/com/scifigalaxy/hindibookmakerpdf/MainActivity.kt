package com.scifigalaxy.hindibookmakerpdf

import android.app.Activity
import android.os.Bundle
import android.os.CancellationSignal
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class MainActivity : Activity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
        }

        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()

        webView.addJavascriptInterface(PdfBridge(), "AndroidPDF")

        setContentView(webView)

        webView.loadUrl("file:///android_asset/index.html")
    }

    inner class PdfBridge {

        @JavascriptInterface
        fun exportFullBook() {
            runOnUiThread {
                createPdf("Simple_Hindi_Book_Full")
            }
        }

        @JavascriptInterface
        fun exportCurrentPage() {
            runOnUiThread {
                createPdf("Simple_Hindi_Book_Current_Page")
            }
        }

        @JavascriptInterface
        fun pdfTest() {
            runOnUiThread {
                createPdf("Simple_Hindi_Book_PDF_Test")
            }
        }
    }

    private fun createPdf(fileName: String) {

        Toast.makeText(
            this,
            "PDF तैयार किया जा रहा है…",
            Toast.LENGTH_SHORT
        ).show()

        webView.postDelayed({

            val printAdapter =
                webView.createPrintDocumentAdapter(fileName)

            val mediaSize = PrintAttributes.MediaSize(
                "HINDI_BOOK_5_5X8_5",
                "5.5 x 8.5 inch",
                5500,
                8500
            )

            val printAttributes = PrintAttributes.Builder()
                .setMediaSize(mediaSize)
                .setResolution(
                    PrintAttributes.Resolution(
                        "HINDI_BOOK_RESOLUTION",
                        "Hindi Book",
                        300,
                        300
                    )
                )
                .setColorMode(
                    PrintAttributes.COLOR_MODE_COLOR
                )
                .build()

            val outputFile = File(
                cacheDir,
                "$fileName.pdf"
            )

            try {

                if (outputFile.exists()) {
                    outputFile.delete()
                }

                val descriptor = android.os.ParcelFileDescriptor.open(
                    outputFile,
                    android.os.ParcelFileDescriptor.MODE_CREATE or
                        android.os.ParcelFileDescriptor.MODE_TRUNCATE or
                        android.os.ParcelFileDescriptor.MODE_READ_WRITE
                )

                printAdapter.onLayout(
                    null,
                    printAttributes,
                    CancellationSignal(),
                    object : PrintDocumentAdapter.LayoutResultCallback() {

                        override fun onLayoutFinished(
                            info: PrintDocumentInfo?,
                            changed: Boolean
                        ) {

                            printAdapter.onWrite(
                                arrayOf(PageRange.ALL_PAGES),
                                descriptor,
                                CancellationSignal(),
                                object : PrintDocumentAdapter.WriteResultCallback() {

                                    override fun onWriteFinished(
                                        pages: Array<PageRange>
                                    ) {
                                        try {
                                            descriptor.close()
                                        } catch (_: Exception) {
                                        }

                                        savePdfToDownloads(
                                            outputFile,
                                            fileName
                                        )
                                    }

                                    override fun onWriteFailed(
                                        error: CharSequence?
                                    ) {
                                        try {
                                            descriptor.close()
                                        } catch (_: Exception) {
                                        }

                                        showError(
                                            "PDF बनाते समय समस्या: $error"
                                        )
                                    }
                                }
                            )
                        }

                        override fun onLayoutFailed(
                            error: CharSequence?
                        ) {
                            try {
                                descriptor.close()
                            } catch (_: Exception) {
                            }

                            showError(
                                "PDF layout समस्या: $error"
                            )
                        }
                    },
                    null
                )

            } catch (e: Exception) {
                showError(
                    "PDF error: ${e.message}"
                )
            }

        }, 1000)
    }

    private fun savePdfToDownloads(
        sourceFile: File,
        fileName: String
    ) {

        try {

            val downloads =
                android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_DOWNLOADS
                )

            val folder = File(
                downloads,
                "Simple Hindi Book Maker"
            )

            if (!folder.exists()) {
                folder.mkdirs()
            }

            val destination =
                File(folder, "$fileName.pdf")

            FileInputStream(sourceFile).use { input ->
                FileOutputStream(destination).use { output ->

                    val buffer = ByteArray(8192)

                    while (true) {
                        val count = input.read(buffer)

                        if (count <= 0) {
                            break
                        }

                        output.write(buffer, 0, count)
                    }

                    output.flush()
                }
            }

            runOnUiThread {

                Toast.makeText(
                    this,
                    "PDF तैयार है:\n${destination.absolutePath}",
                    Toast.LENGTH_LONG
                ).show()
            }

        } catch (e: Exception) {

            showError(
                "PDF save नहीं हो सका: ${e.message}"
            )
        }
    }

    private fun showError(message: String) {

        runOnUiThread {

            Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onDestroy() {

        webView.destroy()

        super.onDestroy()
    }
}
