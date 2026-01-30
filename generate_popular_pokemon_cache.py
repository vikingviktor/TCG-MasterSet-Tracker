#!/usr/bin/env python3
"""
Popular Pokemon Card Cache Generator
Fetches card data for popular Pokemon from TCGdex API and generates a local JSON cache file.
This file is used to pre-populate the app with commonly searched Pokemon cards.

Usage:
    python generate_popular_pokemon_cache.py

Requirements:
    pip install requests
"""

import requests
import json
import sys
from pathlib import Path
from typing import List, Dict, Any

# List of most popular Pokemon to cache
POPULAR_POKEMON = [
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
    "Tauros", "Nidoking", "Nidoqueen", "Ninetales", "Vulpix",
    "Growlithe", "Psyduck", "Abra", "Mankey", "Pidgeot",
    "Spearow", "Jigglypuff", "Zubat", "Odish", "Bellsprout"
]

# TCGdex API base URL
TCGDEX_API_BASE = "https://api.tcgdex.net/v2/en/cards"

def fetch_pokemon_cards(pokemon_name: str) -> List[Dict[str, Any]]:
    """
    Fetch cards for a specific Pokemon from TCGdex API.
    
    Args:
        pokemon_name: Name of the Pokemon to search for
        
    Returns:
        List of card dictionaries
    """
    try:
        print(f"Fetching cards for {pokemon_name}...", end=" ")
        url = f"{TCGDEX_API_BASE}?name={pokemon_name}"
        response = requests.get(url, timeout=10)
        response.raise_for_status()
        
        cards = response.json()
        print(f"✓ ({len(cards)} cards found)")
        return cards
    except requests.exceptions.RequestException as e:
        print(f"✗ Error: {e}")
        return []
    except json.JSONDecodeError:
        print(f"✗ Invalid JSON response")
        return []

def generate_cache_file(output_path: Path = None) -> bool:
    """
    Generate the popular Pokemon cache JSON file.
    
    Args:
        output_path: Path where to save the JSON file. 
                    Defaults to app/src/main/assets/popular_pokemon.json
                    
    Returns:
        True if successful, False otherwise
    """
    if output_path is None:
        # Default to app assets folder
        output_path = Path(__file__).parent / "app" / "src" / "main" / "assets" / "popular_pokemon.json"
    
    # Ensure directory exists
    output_path.parent.mkdir(parents=True, exist_ok=True)
    
    print(f"\n{'='*60}")
    print(f"Popular Pokemon Card Cache Generator")
    print(f"{'='*60}\n")
    print(f"Target: {output_path}")
    print(f"Pokemon to cache: {len(POPULAR_POKEMON)}")
    print(f"\n{'Fetching card data from TCGdex API...'}\n")
    
    all_cards: Dict[str, List[Dict[str, Any]]] = {}
    total_cards = 0
    
    try:
        for pokemon in POPULAR_POKEMON:
            cards = fetch_pokemon_cards(pokemon)
            if cards:
                all_cards[pokemon] = cards
                total_cards += len(cards)
        
        if not all_cards:
            print("\n❌ Failed to fetch any card data. Please check your internet connection.")
            return False
        
        # Create the cache structure
        cache_data = {
            "version": "1.0",
            "generated_at": "2026-01-30",
            "description": "Pre-cached popular Pokemon card data for instant app startup",
            "total_pokemon": len(all_cards),
            "total_cards": total_cards,
            "pokemon": all_cards
        }
        
        # Write to file
        with open(output_path, 'w', encoding='utf-8') as f:
            json.dump(cache_data, f, indent=2, ensure_ascii=False)
        
        print(f"\n{'='*60}")
        print(f"✓ Cache file generated successfully!")
        print(f"{'='*60}")
        print(f"Location: {output_path}")
        print(f"File size: {output_path.stat().st_size / 1024:.2f} KB")
        print(f"Pokemon cached: {len(all_cards)}")
        print(f"Total cards: {total_cards}")
        print(f"Average cards per Pokemon: {total_cards / len(all_cards):.1f}")
        print(f"\nThe app will use this cache on startup for instant results.")
        print(f"Run this script anytime to refresh the cache with latest data.\n")
        
        return True
        
    except Exception as e:
        print(f"\n❌ Error generating cache file: {e}")
        return False

def main():
    """Main entry point."""
    try:
        success = generate_cache_file()
        sys.exit(0 if success else 1)
    except KeyboardInterrupt:
        print("\n\n⚠️  Cache generation cancelled by user.")
        sys.exit(1)

if __name__ == "__main__":
    main()
