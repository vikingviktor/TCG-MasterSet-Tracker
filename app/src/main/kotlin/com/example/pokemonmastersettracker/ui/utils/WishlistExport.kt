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

// Export wishlist as image
fun exportWishlistAsImage(context: Context, wishlistCards: List<Card>): File? {
    return try {
        // Create bitmap (width, height)
        val width = 1080
        val height = 200 + (wishlistCards.size * 150) // Dynamic height based on cards
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
            
            yPosition += 150f
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
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "image/png"
        }
        
        context.startActivity(Intent.createChooser(shareIntent, "Share Wishlist"))
    } catch (e: Exception) {
        android.util.Log.e("WishlistShare", "Error sharing wishlist", e)
    }
}
