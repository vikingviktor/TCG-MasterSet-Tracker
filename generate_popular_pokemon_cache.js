#!/usr/bin/env node

/**
 * Popular Pokemon Card Cache Generator (Node.js)
 * Fetches card data for popular Pokemon from TCGdex API and generates a local JSON cache file.
 * 
 * Usage:
 *   npm install (if needed - uses built-in fetch in Node 18+)
 *   node generate_popular_pokemon_cache.js
 * 
 * Or with npm:
 *   npm install node-fetch
 *   node generate_popular_pokemon_cache.js
 */

const fs = require('fs');
const path = require('path');

// List of most popular Pokemon to cache
const POPULAR_POKEMON = [
    "Pikachu", "Charizard", "Blastoise", "Venusaur", "Dragonite",
    "Lapras", "Gengar", "Alakazam", "Machamp", "Golem",
    "Arcanine", "Exeggutor", "Marowak", "Hitmonlee", "Hitmonchan",
    "Vileplume", "Bellossom", "Wigglytuff", "Golduck", "Kangaskhan",
    "Rhydon", "Magneton", "Farfetchd", "Dodrio", "Electrode",
    "Cloyster", "Kingler", "Haunter", "Gengar", "Arbok",
    "Weezing", "Victreebel", "Muk", "Jynx", "Porygon",
    "Snorlax", "Articuno", "Zapdos", "Moltres", "Ditto",
    "Mewtwo", "Mew", "Gyarados", "Tentacruel", "Primeape",
    "Seaking", "Goldeen", "Staryu", "Starmie", "Slowbro",
    "Hypno", "Poliwrath", "Grimer", "Kadabra", "Graveler",
    "Shellder", "Rapidash", "Onix", "Dugtrio", "Persian",
    "Tauros", "Nidoking", "Nidoqueen", "Ninetales", "Vulpix"
];

const TCGDEX_API_BASE = "https://api.tcgdex.net/v2/en/cards";

// For Node versions < 18, use node-fetch
let fetch;
if (typeof globalThis.fetch === 'undefined') {
    try {
        fetch = require('node-fetch');
    } catch (e) {
        console.error('❌ Error: node-fetch not found. Please install it with:');
        console.error('   npm install node-fetch');
        process.exit(1);
    }
} else {
    fetch = globalThis.fetch;
}

async function fetchPokemonCards(pokemonName) {
    try {
        process.stdout.write(`Fetching cards for ${pokemonName}... `);
        const url = `${TCGDEX_API_BASE}?name=${pokemonName}`;
        const response = await fetch(url, { timeout: 10000 });
        
        if (!response.ok) {
            console.log(`✗ HTTP ${response.status}`);
            return null;
        }
        
        const cards = await response.json();
        console.log(`✓ (${cards.length} cards found)`);
        return cards;
    } catch (error) {
        console.log(`✗ Error: ${error.message}`);
        return null;
    }
}

async function generateCacheFile(outputPath = null) {
    const filePath = outputPath || path.join(__dirname, 'app', 'src', 'main', 'assets', 'popular_pokemon.json');
    
    // Ensure directory exists
    const dir = path.dirname(filePath);
    if (!fs.existsSync(dir)) {
        fs.mkdirSync(dir, { recursive: true });
    }
    
    console.log('\n' + '='.repeat(60));
    console.log('Popular Pokemon Card Cache Generator (Node.js)');
    console.log('='.repeat(60) + '\n');
    console.log(`Target: ${filePath}`);
    console.log(`Pokemon to cache: ${POPULAR_POKEMON.length}`);
    console.log('\nFetching card data from TCGdex API...\n');
    
    const allCards = {};
    let totalCards = 0;
    
    try {
        for (const pokemon of POPULAR_POKEMON) {
            const cards = await fetchPokemonCards(pokemon);
            if (cards && cards.length > 0) {
                allCards[pokemon] = cards;
                totalCards += cards.length;
            }
        }
        
        if (Object.keys(allCards).length === 0) {
            console.log('\n❌ Failed to fetch any card data. Please check your internet connection.');
            return false;
        }
        
        // Create the cache structure
        const cacheData = {
            version: "1.0",
            generated_at: new Date().toISOString().split('T')[0],
            description: "Pre-cached popular Pokemon card data for instant app startup",
            total_pokemon: Object.keys(allCards).length,
            total_cards: totalCards,
            pokemon: allCards
        };
        
        // Write to file
        fs.writeFileSync(filePath, JSON.stringify(cacheData, null, 2), 'utf8');
        
        const fileSizeKB = (fs.statSync(filePath).size / 1024).toFixed(2);
        const avgCards = (totalCards / Object.keys(allCards).length).toFixed(1);
        
        console.log('\n' + '='.repeat(60));
        console.log('✓ Cache file generated successfully!');
        console.log('='.repeat(60));
        console.log(`Location: ${filePath}`);
        console.log(`File size: ${fileSizeKB} KB`);
        console.log(`Pokemon cached: ${Object.keys(allCards).length}`);
        console.log(`Total cards: ${totalCards}`);
        console.log(`Average cards per Pokemon: ${avgCards}`);
        console.log('\nThe app will use this cache on startup for instant results.');
        console.log('Run this script anytime to refresh the cache with latest data.\n');
        
        return true;
    } catch (error) {
        console.log(`\n❌ Error generating cache file: ${error.message}`);
        console.error(error);
        return false;
    }
}

// Run the generator
generateCacheFile()
    .then(success => process.exit(success ? 0 : 1))
    .catch(error => {
        console.error('Fatal error:', error);
        process.exit(1);
    });
