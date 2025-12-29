package com.example.pokemonmastersettracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.pokemonmastersettracker.data.models.Card
import com.example.pokemonmastersettracker.data.models.UserCard
import com.example.pokemonmastersettracker.data.models.FavoritePokemon
import com.example.pokemonmastersettracker.data.models.User
import com.example.pokemonmastersettracker.utils.CardImageTypeConverter
import com.example.pokemonmastersettracker.utils.TCGPlayerDataTypeConverter
import com.example.pokemonmastersettracker.utils.PriceDataTypeConverter

@Database(
    entities = [
        Card::class,
        UserCard::class,
        FavoritePokemon::class,
        User::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    CardImageTypeConverter::class,
    TCGPlayerDataTypeConverter::class,
    PriceDataTypeConverter::class
)
abstract class PokemonTrackerDatabase : RoomDatabase() {
    
    abstract fun cardDao(): CardDao
    abstract fun userCardDao(): UserCardDao
    abstract fun favoritePokemonDao(): FavoritePokemonDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var instance: PokemonTrackerDatabase? = null

        fun getInstance(context: Context): PokemonTrackerDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    PokemonTrackerDatabase::class.java,
                    "pokemon_tracker_db"
                ).build().also { instance = it }
            }
        }
    }
}
