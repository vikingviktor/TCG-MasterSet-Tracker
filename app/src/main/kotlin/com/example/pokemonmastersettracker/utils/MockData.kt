import com.example.pokemonmastersettracker.data.models.Card
import com.example.pokemonmastersettracker.data.models.UserCard
import com.example.pokemonmastersettracker.data.models.CardCondition

object MockData {
    fun createMockCard(
        id: String = "sv04pt-1",
        name: String = "Pikachu",
        number: String = "001",
        rarity: String = "Holo"
    ): Card {
        return Card(
            id = id,
            name = name,
            supertype = "Pokémon",
            subtypes = listOf("Basic"),
            hp = "45",
            types = listOf("Electric"),
            rarity = rarity,
            set = "sv04pt",
            image = null,
            number = number,
            artist = "Ken Sugimori",
            tcgplayer = null
        )
    }

    fun createMockUserCard(
        userId: String = "user1",
        cardId: String = "sv04pt-1",
        isOwned: Boolean = true,
        condition: CardCondition = CardCondition.NEAR_MINT
    ): UserCard {
        return UserCard(
            id = 1,
            userId = userId,
            cardId = cardId,
            isOwned = isOwned,
            condition = condition,
            isGraded = false,
            gradingCompany = null,
            grade = null,
            purchasePrice = 25.99,
            currentPrice = 35.50
        )
    }
}
