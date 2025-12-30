package com.example.pokemonmastersettracker.di

import android.content.Context
import com.example.pokemonmastersettracker.data.api.PokemonTCGApi
import com.example.pokemonmastersettracker.data.database.PokemonTrackerDatabase
import com.example.pokemonmastersettracker.data.database.CardDao
import com.example.pokemonmastersettracker.data.database.UserCardDao
import com.example.pokemonmastersettracker.data.database.FavoritePokemonDao
import com.example.pokemonmastersettracker.data.database.UserDao
import com.example.pokemonmastersettracker.data.repository.PokemonRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
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
    fun provideUserDao(database: PokemonTrackerDatabase): UserDao {
        return database.userDao()
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // API Key interceptor for Pokemon TCG API
        val apiKeyInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val requestWithApiKey = originalRequest.newBuilder()
                .header("X-Api-Key", "99c671d7-ddc9-44c8-a843-d128e8596463")
                .build()
            chain.proceed(requestWithApiKey)
        }

        return OkHttpClient.Builder()
            .addInterceptor(apiKeyInterceptor)
            .addInterceptor(loggingInterceptor)
            .callTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Singleton
    @Provides
    fun providePokemonTCGApi(okHttpClient: OkHttpClient): PokemonTCGApi {
        return Retrofit.Builder()
            .baseUrl(PokemonTCGApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PokemonTCGApi::class.java)
    }

    @Singleton
    @Provides
    fun providePokemonRepository(
        api: PokemonTCGApi,
        cardDao: CardDao,
        userCardDao: UserCardDao,
        favoritePokemonDao: FavoritePokemonDao,
        userDao: UserDao
    ): PokemonRepository {
        return PokemonRepository(api, cardDao, userCardDao, favoritePokemonDao, userDao)
    }
}
