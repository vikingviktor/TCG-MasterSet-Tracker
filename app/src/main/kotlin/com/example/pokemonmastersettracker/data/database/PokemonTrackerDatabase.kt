package com.example.pokemonmastersettracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.pokemonmastersettracker.data.models.Card
import com.example.pokemonmastersettracker.data.models.UserCard
import com.example.pokemonmastersettracker.data.models.FavoritePokemon
import com.example.pokemonmastersettracker.data.models.WishlistCard
import com.example.pokemonmastersettracker.data.models.User
import com.example.pokemonmastersettracker.data.models.Pokemon
import com.example.pokemonmastersettracker.data.converters.TypeConverters as RoomTypeConverters

@Database(
    entities = [
        Card::class,
        UserCard::class,
        FavoritePokemon::class,
        WishlistCard::class,
        User::class,
        Pokemon::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(RoomTypeConverters::class)
abstract class PokemonTrackerDatabase : RoomDatabase() {
    
    abstract fun cardDao(): CardDao
    abstract fun userCardDao(): UserCardDao
    abstract fun favoritePokemonDao(): FavoritePokemonDao
    abstract fun wishlistCardDao(): WishlistCardDao
    abstract fun userDao(): UserDao
    abstract fun pokemonDao(): PokemonDao

    companion object {
        @Volatile
        private var instance: PokemonTrackerDatabase? = null

        fun getInstance(context: Context): PokemonTrackerDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    PokemonTrackerDatabase::class.java,
                    "pokemon_tracker_db"
                )
                // Use pre-populated database ONLY on fresh install
                .createFromAsset("database/pokemon_tracker_prepopulated.db")
                // Don't use fallbackToDestructiveMigration - it wipes pre-populated data
                .build().also { instance = it }
            }
        }
    }
}
