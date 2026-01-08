package com.example.pokemonmastersettracker.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.pokemonmastersettracker.data.models.Card
import com.example.pokemonmastersettracker.data.models.UserCard
import com.example.pokemonmastersettracker.data.models.FavoritePokemon
import com.example.pokemonmastersettracker.data.models.WishlistCard
import com.example.pokemonmastersettracker.data.models.User
import com.example.pokemonmastersettracker.data.models.Pokemon
import kotlinx.coroutines.flow.Flow

@Dao
interface PokemonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemon(pokemon: Pokemon)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemons(pokemons: List<Pokemon>)
    
    @Query("SELECT * FROM pokemon WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    suspend fun searchPokemon(query: String): List<Pokemon>
    
    @Query("SELECT * FROM pokemon ORDER BY name ASC")
    suspend fun getAllPokemon(): List<Pokemon>
    
    @Query("SELECT * FROM pokemon WHERE isFavorite = 1 ORDER BY name ASC")
    suspend fun getFavoritePokemon(): List<Pokemon>
    
    @Query("UPDATE pokemon SET isFavorite = :isFavorite WHERE name = :name")
    suspend fun updateFavoriteStatus(name: String, isFavorite: Boolean)
    
    @Query("UPDATE pokemon SET imageUrl = :imageUrl WHERE name = :name")
    suspend fun updatePokemonImage(name: String, imageUrl: String)
    
    @Query("SELECT COUNT(*) FROM pokemon")
    suspend fun getPokemonCount(): Int
}

@Dao
interface CardDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: Card)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<Card>)

    @Query("SELECT * FROM cards WHERE id = :cardId")
    suspend fun getCardById(cardId: String): Card?

    @Query("SELECT * FROM cards WHERE name LIKE '%' || :pokemonName || '%'")
    fun getCardsByPokemonName(pokemonName: String): Flow<List<Card>>
    
    @Query("SELECT * FROM cards WHERE name LIKE :pokemonName")
    suspend fun getCardsByPokemonNameSync(pokemonName: String): List<Card>

    @Query("SELECT * FROM cards")
    fun getAllCards(): Flow<List<Card>>

    @Delete
    suspend fun deleteCard(card: Card)
}

@Dao
interface UserCardDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserCard(userCard: UserCard)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserCards(userCards: List<UserCard>)

    @Update
    suspend fun updateUserCard(userCard: UserCard)

    @Query("SELECT * FROM user_cards WHERE userId = :userId AND cardId = :cardId")
    suspend fun getUserCard(userId: String, cardId: String): UserCard?

    @Query("SELECT * FROM user_cards WHERE userId = :userId")
    fun getUserCards(userId: String): Flow<List<UserCard>>
    
    @Query("SELECT * FROM user_cards WHERE userId = :userId")
    suspend fun getUserCardsSync(userId: String): List<UserCard>

    @Query("SELECT * FROM user_cards WHERE userId = :userId AND isOwned = 1")
    fun getUserOwnedCards(userId: String): Flow<List<UserCard>>

    @Query("SELECT COUNT(*) FROM user_cards WHERE userId = :userId AND isOwned = 1")
    suspend fun getOwnedCardsCount(userId: String): Int

    @Delete
    suspend fun deleteUserCard(userCard: UserCard)
}

@Dao
interface FavoritePokemonDao {
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavorite(favorite: FavoritePokemon)
    
    @Query("UPDATE favorite_pokemon SET totalCards = :totalCards WHERE userId = :userId AND pokemonName = :pokemonName")
    suspend fun updateTotalCards(userId: String, pokemonName: String, totalCards: Int)

    @Query("SELECT * FROM favorite_pokemon WHERE userId = :userId")
    fun getUserFavorites(userId: String): Flow<List<FavoritePokemon>>

    @Query("SELECT * FROM favorite_pokemon WHERE userId = :userId")
    suspend fun getUserFavoritesSync(userId: String): List<FavoritePokemon>

    @Query("SELECT COUNT(*) FROM favorite_pokemon WHERE userId = :userId AND pokemonName = :pokemonName")
    suspend fun isFavorite(userId: String, pokemonName: String): Int

    @Query("DELETE FROM favorite_pokemon WHERE userId = :userId AND pokemonName = :pokemonName")
    suspend fun removeFavorite(userId: String, pokemonName: String)

    @Delete
    suspend fun deleteFavorite(favorite: FavoritePokemon)
    
    // Single query that gets all favorite Pokemon with details via JOINs
    // Counts owned cards by joining with cards table and filtering by exact Pokemon name match
    @Query("""
        SELECT 
            fp.pokemonName as pokemonName,
            p.nationalPokedexNumber as pokedexNumber,
            p.imageUrl as imageUrl,
            fp.totalCards as totalCards,
            COUNT(DISTINCT CASE 
                WHEN c.name LIKE fp.pokemonName || ' %' 
                     OR c.name = fp.pokemonName 
                THEN uc.cardId 
                ELSE NULL 
            END) as ownedCount
        FROM favorite_pokemon fp
        LEFT JOIN pokemon p ON fp.pokemonName = p.name
        LEFT JOIN user_cards uc ON uc.userId = fp.userId
        LEFT JOIN cards c ON c.id = uc.cardId
        WHERE fp.userId = :userId
        GROUP BY fp.id, fp.pokemonName, p.nationalPokedexNumber, p.imageUrl, fp.totalCards
    """)
    fun getUserFavoritesWithDetails(userId: String): Flow<List<com.example.pokemonmastersettracker.data.models.FavoritePokemonWithDetails>>
}

@Dao
interface WishlistCardDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWishlist(wishlistCard: WishlistCard)
    
    @Query("SELECT * FROM wishlist_cards WHERE userId = :userId")
    fun getUserWishlist(userId: String): Flow<List<WishlistCard>>
    
    @Query("SELECT * FROM wishlist_cards WHERE userId = :userId")
    suspend fun getUserWishlistSync(userId: String): List<WishlistCard>
    
    @Query("SELECT COUNT(*) FROM wishlist_cards WHERE userId = :userId AND cardId = :cardId")
    suspend fun isInWishlist(userId: String, cardId: String): Int
    
    @Query("DELETE FROM wishlist_cards WHERE userId = :userId AND cardId = :cardId")
    suspend fun removeFromWishlist(userId: String, cardId: String)
    
    @Delete
    suspend fun deleteWishlistCard(wishlistCard: WishlistCard)
}

@Dao
interface UserDao {
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): User?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)
}
