package com.example.pokemonmastersettracker.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object DatabaseExporter {
    
    /**
     * Export the current database to the Downloads folder
     * This can be used to create a pre-populated database for bundling with the app
     */
    fun exportDatabase(context: Context): String? {
        return try {
            // Export the current active database (v7)
            val currentDBPath = context.getDatabasePath("pokemon_tracker_v7.db")
            
            if (!currentDBPath.exists()) {
                Log.e("DatabaseExporter", "Database file does not exist at: ${currentDBPath.absolutePath}")
                return null
            }
            
            // Check if database has data
            val dbSize = currentDBPath.length()
            if (dbSize < 1024) { // Less than 1KB means empty/corrupt
                Log.e("DatabaseExporter", "Database is too small (${dbSize} bytes) - likely empty!")
                return null
            }
            
            // Export to app's external files directory (accessible via USB/file manager)
            val exportDir = File(context.getExternalFilesDir(null), "database_export")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            
            val exportFile = File(exportDir, "pokemon_tracker_prepopulated.db")
            
            // Copy database file
            FileInputStream(currentDBPath).use { input ->
                FileOutputStream(exportFile).use { output ->
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (input.read(buffer).also { length = it } > 0) {
                        output.write(buffer, 0, length)
                    }
                }
            }
            
            Log.d("DatabaseExporter", "✓ Database exported successfully!")
            Log.d("DatabaseExporter", "Location: ${exportFile.absolutePath}")
            Log.d("DatabaseExporter", "Size: ${exportFile.length() / 1024} KB")
            
            exportFile.absolutePath
        } catch (e: Exception) {
            Log.e("DatabaseExporter", "Failed to export database: ${e.message}", e)
            null
        }
    }
}
