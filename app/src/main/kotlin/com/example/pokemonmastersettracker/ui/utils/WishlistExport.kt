package com.example.pokemonmastersettracker.ui.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.FileProvider
import com.example.pokemonmastersettracker.data.models.Card
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Export wishlist as CSV file
fun exportWishlistAsCSV(context: Context, wishlistCards: List<Card>): File? {
    return try {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(context.cacheDir, "wishlist_$timestamp.csv")
        
        val csvContent = StringBuilder()
        // Header row
        csvContent.append("Card Name,Set Name,Card Number,Rarity,Type,HP,Avg Price\n")
        
        // Data rows
        for (card in wishlistCards) {
            val name = card.name?.replace(",", " ") ?: ""
            val setName = card.set?.name?.replace(",", " ") ?: ""
            val number = card.number ?: ""
            val rarity = card.rarity?.replace(",", " ") ?: ""
            val types = card.types?.joinToString(";") ?: ""
            val hp = card.hp ?: ""
            val avgPrice = card.cardmarket?.avg?.let { "${'$'}%.2f".format(it) } ?: "N/A"
            
            csvContent.append("\"$name\",\"$setName\",\"$number\",\"$rarity\",\"$types\",\"$hp\",\"$avgPrice\"\n")
        }
        
        FileOutputStream(file).use { out ->
            out.write(csvContent.toString().toByteArray())
            out.flush()
        }
        
        file
    } catch (e: Exception) {
        android.util.Log.e("WishlistExportCSV", "Error exporting wishlist as CSV", e)
        null
    }
}

// Export wishlist as text file
fun exportWishlistAsText(context: Context, wishlistCards: List<Card>): File? {
    return try {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(context.cacheDir, "wishlist_$timestamp.txt")
        
        val textContent = StringBuilder()
        textContent.append("=== MY POKEMON WISHLIST ===\n")
        textContent.append("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}\n")
        textContent.append("Total Cards: ${wishlistCards.size}\n")
        textContent.append("=====================================\n\n")
        
        var totalValue = 0.0
        for ((index, card) in wishlistCards.withIndex()) {
            textContent.append("${index + 1}. ${card.name}\n")
            textContent.append("   Set: ${card.set?.name ?: "Unknown"}\n")
            textContent.append("   Card #: ${card.number ?: "N/A"}\n")
            textContent.append("   Rarity: ${card.rarity ?: "N/A"}\n")
            if (!card.types.isNullOrEmpty()) {
                textContent.append("   Type: ${card.types.joinToString(", ")}\n")
            }
            if (!card.hp.isNullOrEmpty()) {
                textContent.append("   HP: ${card.hp}\n")
            }
            card.cardmarket?.avg?.let {
                textContent.append("   Market Avg Price: ${'$'}%.2f\n".format(it))
                totalValue += it
            }
            textContent.append("\n")
        }
        
        textContent.append("=====================================\n")
        if (totalValue > 0) {
            textContent.append("Total Estimated Value: ${'$'}%.2f\n".format(totalValue))
        }
        textContent.append("End of Wishlist")
        
        FileOutputStream(file).use { out ->
            out.write(textContent.toString().toByteArray())
            out.flush()
        }
        
        file
    } catch (e: Exception) {
        android.util.Log.e("WishlistExportText", "Error exporting wishlist as text", e)
        null
    }
}

// Export wishlist as image
fun exportWishlistAsImage(context: Context, wishlistCards: List<Card>): File? {
    return try {
        // Create bitmap (width, height)
        val width = 1080
        val height = 200 + (wishlistCards.size * 180) // Increased height for price info
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        val canvas = Canvas(bitmap)
        
        // Background
        val bgPaint = Paint().apply {
            color = android.graphics.Color.WHITE
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
        
        // Header text
        val headerPaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 48f
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("My Pokemon Wishlist", 40f, 80f, headerPaint)
        
        // Draw cards info
        val cardPaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 32f
            typeface = Typeface.DEFAULT
        }
        
        val subPaint = Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = 24f
            typeface = Typeface.DEFAULT
        }
        
        val pricePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#E63946") // Red for price
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
        }
        
        var yPosition = 150f
        for (card in wishlistCards) {
            // Card name
            canvas.drawText(
                "${card.name} - ${card.set?.name ?: "Unknown Set"}",
                40f,
                yPosition,
                cardPaint
            )
            
            // Card number and set
            canvas.drawText(
                "Card #${card.number ?: "?"} | ${card.set?.id ?: ""}",
                40f,
                yPosition + 40f,
                subPaint
            )
            
            // Market average price
            card.cardmarket?.avg?.let {
                canvas.drawText(
                    "Market Avg: \$%.2f".format(it),
                    40f,
                    yPosition + 80f,
                    pricePaint
                )
            } ?: run {
                canvas.drawText(
                    "Market Avg: N/A",
                    40f,
                    yPosition + 80f,
                    pricePaint
                )
            }
            
            yPosition += 180f
        }
        
        // Save bitmap to file
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(context.cacheDir, "wishlist_$timestamp.png")
        
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
        }
        
        bitmap.recycle()
        file
    } catch (e: Exception) {
        android.util.Log.e("WishlistExport", "Error exporting wishlist", e)
        null
    }
}

// Share the exported image
fun shareWishlistImage(context: Context, imageFile: File) {
    try {
        val uri = try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
        } catch (e: Exception) {
            android.util.Log.e("WishlistShare", "FileProvider error, trying direct file URI: ${e.message}")
            // Fallback to direct file URI (less secure but will work)
            android.net.Uri.fromFile(imageFile)
        }
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "image/png"
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        
        context.startActivity(Intent.createChooser(shareIntent, "Share Wishlist"))
    } catch (e: Exception) {
        android.util.Log.e("WishlistShare", "Error sharing wishlist: ${e.message}", e)
    }
}

// Generic share function for any file
fun shareFile(context: Context, file: File, mimeType: String) {
    try {
        val uri = try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            android.util.Log.e("FileShare", "FileProvider error, trying direct file URI: ${e.message}")
            android.net.Uri.fromFile(file)
        }
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = mimeType
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        
        context.startActivity(Intent.createChooser(shareIntent, "Share File"))
    } catch (e: Exception) {
        android.util.Log.e("FileShare", "Error sharing file: ${e.message}", e)
    }
}
