package com.example.pokemonmastersettracker.di

import android.content.Context
import com.example.pokemonmastersettracker.data.database.PokemonTrackerDatabase
import com.example.pokemonmastersettracker.data.database.CardDao
import com.example.pokemonmastersettracker.data.database.UserCardDao
import com.example.pokemonmastersettracker.data.database.FavoritePokemonDao
import com.example.pokemonmastersettracker.data.database.WishlistCardDao
import com.example.pokemonmastersettracker.data.database.UserDao
import com.example.pokemonmastersettracker.data.database.PokemonDao
import com.example.pokemonmastersettracker.data.repository.PokemonRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ): PokemonTrackerDatabase {
        return PokemonTrackerDatabase.getInstance(context)
    }

    @Singleton
    @Provides
    fun provideCardDao(database: PokemonTrackerDatabase): CardDao {
        return database.cardDao()
    }

    @Singleton
    @Provides
    fun provideUserCardDao(database: PokemonTrackerDatabase): UserCardDao {
        return database.userCardDao()
    }

    @Singleton
    @Provides
    fun provideFavoritePokemonDao(database: PokemonTrackerDatabase): FavoritePokemonDao {
        return database.favoritePokemonDao()
    }

    @Singleton
    @Provides
    fun provideWishlistCardDao(database: PokemonTrackerDatabase): WishlistCardDao {
        return database.wishlistCardDao()
    }

    @Singleton
    @Provides
    fun provideUserDao(database: PokemonTrackerDatabase): UserDao {
        return database.userDao()
    }

    @Singleton
    @Provides
    fun providePokemonDao(database: PokemonTrackerDatabase): PokemonDao {
        return database.pokemonDao()
    }

    @Singleton
    @Provides
    fun providePokemonRepository(
        cardDao: CardDao,
        userCardDao: UserCardDao,
        favoritePokemonDao: FavoritePokemonDao,
        wishlistCardDao: WishlistCardDao,
        userDao: UserDao,
        pokemonDao: PokemonDao
    ): PokemonRepository {
        return PokemonRepository(cardDao, userCardDao, favoritePokemonDao, wishlistCardDao, userDao, pokemonDao)
    }
}
