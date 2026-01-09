-- SQL commands to clean user data from pre-populated database
-- Run these on: app/src/main/assets/database/pokemon_tracker_prepopulated.db

-- Remove all user-specific data (keep only Pokemon and Cards data)
DELETE FROM favorite_pokemon;
DELETE FROM wishlist_cards;
DELETE FROM user_cards;

-- Verify tables are empty
SELECT COUNT(*) as favorite_count FROM favorite_pokemon;
SELECT COUNT(*) as wishlist_count FROM wishlist_cards;
SELECT COUNT(*) as user_cards_count FROM user_cards;

-- Verify the data we want to keep is still there
SELECT COUNT(*) as pokemon_count FROM pokemon;
SELECT COUNT(*) as cards_count FROM cards;
