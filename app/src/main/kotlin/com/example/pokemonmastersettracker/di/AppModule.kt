package com.example.pokemonmastersettracker.di

import android.content.Context
import com.example.pokemonmastersettracker.data.api.PokemonTCGApi
import com.example.pokemonmastersettracker.data.database.PokemonTrackerDatabase
import com.example.pokemonmastersettracker.data.database.CardDao
import com.example.pokemonmastersettracker.data.database.UserCardDao
import com.example.pokemonmastersettracker.data.database.FavoritePokemonDao
import com.example.pokemonmastersettracker.data.database.UserDao
import com.example.pokemonmastersettracker.data.database.PokemonDao
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
    fun providePokemonDao(database: PokemonTrackerDatabase): PokemonDao {
        return database.pokemonDao()
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC // Changed from BODY to reduce overhead
        }

        // API Key interceptor for Pokemon TCG API
        val apiKeyInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val requestWithApiKey = originalRequest.newBuilder()
                .header("X-Api-Key", "99c671d7-ddc9-44c8-a843-d128e8596463")
                .build()
            android.util.Log.d("OkHttp", "Making request to: ${requestWithApiKey.url}")
            val startTime = System.currentTimeMillis()
            try {
                val response = chain.proceed(requestWithApiKey)
                val duration = System.currentTimeMillis() - startTime
                android.util.Log.d("OkHttp", "Request completed in ${duration}ms with code ${response.code}")
                response
            } catch (e: Exception) {
                val duration = System.currentTimeMillis() - startTime
                android.util.Log.e("OkHttp", "Request failed after ${duration}ms: ${e.message}")
                throw e
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(apiKeyInterceptor)
            .addInterceptor(loggingInterceptor)
            .callTimeout(120, TimeUnit.SECONDS)     // Increased to 2 minutes
            .connectTimeout(30, TimeUnit.SECONDS)    // 30 seconds to establish connection
            .readTimeout(120, TimeUnit.SECONDS)      // 2 minutes to read response
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)          // Auto retry on connection failure
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
