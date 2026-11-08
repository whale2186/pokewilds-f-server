package com.pkmngen.game;

import com.badlogic.gdx.audio.Music;
import java.util.ArrayList;
import java.util.HashMap;

public class Route {
   String name;
   int level;
   ArrayList<Pokemon> storedPokemon = new ArrayList<>();
   Music music;
   int musicsIndex = 0;
   boolean isDungeon = false;
   boolean dontSpawnOverworlds = false;
   public static HashMap<String, ArrayList<String>> allowedPokemon = new HashMap<>();
   public static HashMap<String, ArrayList<String>> musics = new HashMap<>();

   public Route(Network.RouteData routeData) {
      this.name = routeData.name;
      this.level = routeData.level;
      this.musicsIndex = routeData.musicsIndex;

      for (Network.PokemonDataBase pokemonData : routeData.pokemon) {
         pokemonData.isInterior = false;
         this.storedPokemon.add(new Pokemon(pokemonData));
      }

      this.init();
   }

   public Route(String name, int level) {
      this.name = name;
      this.level = level;
      this.init();
   }

   public void init() {
      if (this.name.equals("pkmnmansion1")) {
         this.isDungeon = true;
      }

      if (this.name.equals("fossil_lab1")) {
         this.isDungeon = true;
      }

      if (this.name.equals("regi_cave1")) {
         this.isDungeon = true;
      }

      if (this.name.equals("ruins1_inner")) {
         this.isDungeon = true;
      }
   }

   public String type() {
      if (this.name.equals("desert1") || this.name.equals("ruins1_outer") || this.name.equals("oasis1") || this.name.equals("sand_pit1")) {
         return "desert";
      } else if (this.name.equals("graveyard1")) {
         return "graveyard";
      } else if (this.name.equals("volcano1")) {
         return "volcano";
      } else {
         return this.name.equals("deep_forest") ? "deep_forest" : "";
      }
   }

   public ArrayList<String> musics() {
      if (!Route.musics.containsKey(this.name)) {
         ArrayList<String> musics = new ArrayList<>();
         if (this.name.equals("ruins1_inner")) {
            musics.add("relic_castle2");
            musics.add("relic_castle2");
         } else if (this.name.equals("fossil_lab1")) {
            musics.add("silence1");
            musics.add("silence1");
         } else if (this.name.equals("graveyard1")) {
            musics.add("old_chateau3");
            musics.add("old_chateau3");
         } else if (this.name.equals("pkmnmansion1")) {
            musics.add("pkmnmansion");
            musics.add("pkmnmansion");
         } else if (this.name.equals("regi_cave1")) {
            musics.add("sealed_chamber2");
            musics.add("sealed_chamber2");
         } else if (this.name.equals("desert1") || this.name.equals("ruins1_outer") || this.name.equals("oasis1") || this.name.equals("sand_pit1")) {
            musics.add("route_111-2");
            musics.add("route_111-2");
         } else if (this.name.equals("volcano1")) {
            musics.add("ambient_rumbling");
            musics.add("ambient_rumbling");
         } else if (this.name.equals("deep_forest")) {
            musics.add("DP_EternaForest");
            musics.add("DP_EternaForest");
         } else if (this.name.equals("wooded_lake1") || this.name.equals("wooded_lake_water1")) {
            musics.add("lake6");
            musics.add("BW_Dreamyard-stitched");
            musics.add("national_park1");
            musics.add("pokemon_tcg_gym1");
            musics.add("overw3");
            musics.add("DP_League");
         } else if (this.name.equals("snow1")) {
            musics.add("pokemon_tcg_gym1");
            musics.add("overw3");
            musics.add("DP_Route216-stitched");
            musics.add("national_park1");
         } else {
            musics.add("pokemon_tcg_gym1");
            musics.add("overw3");
            musics.add("route_42");
            musics.add("national_park1");
            musics.add("viridian_forest_gs");
            musics.add("route_3_gs");
            musics.add("route_3_rb");
            musics.add("route_1");
            musics.add("route_idk1");
         }

         Route.musics.put(this.name, musics);
      }

      return Route.musics.get(this.name);
   }

   public ArrayList<String> allowedPokemon() {
      if (!Route.allowedPokemon.containsKey(this.name)) {
         ArrayList<String> allowedPokemon = new ArrayList<>();
         if (this.name.equals("mountain1")) {
            allowedPokemon.add("rhydon");
            allowedPokemon.add("rhyhorn");
            allowedPokemon.add("onix");
            allowedPokemon.add("machop");
            allowedPokemon.add("machoke");
            allowedPokemon.add("cubone");
            allowedPokemon.add("phanpy");
            allowedPokemon.add("skarmory");
            allowedPokemon.add("whismur");
            allowedPokemon.add("makuhita");
            allowedPokemon.add("duraludon");
         } else if (this.name.equals("mountain1_water1")) {
            allowedPokemon.add("magikarp");
            allowedPokemon.add("poliwag");
            allowedPokemon.add("goldeen");
         } else if (this.name.equals("deep_forest")) {
            allowedPokemon.add("zubat");
            allowedPokemon.add("golbat");
            allowedPokemon.add("houndour");
            allowedPokemon.add("murkrow");
            allowedPokemon.add("nidorina");
            allowedPokemon.add("nidorino");
            allowedPokemon.add("scyther");
            allowedPokemon.add("pinsir");
            allowedPokemon.add("growlithe");
            allowedPokemon.add("vulpix");
            allowedPokemon.add("breloom");
            allowedPokemon.add("sableye");
            allowedPokemon.add("ralts");
            allowedPokemon.add("gastly");
            allowedPokemon.add("volbeat");
            allowedPokemon.add("noibat");
         } else if (this.name.equals("forest1")) {
            allowedPokemon.add("oddish");
            allowedPokemon.add("gloom");
            allowedPokemon.add("pidgey");
            allowedPokemon.add("spearow");
            allowedPokemon.add("taillow");
            allowedPokemon.add("hoppip");
            allowedPokemon.add("bulbasaur");
            allowedPokemon.add("charmander");
            allowedPokemon.add("chikorita");
            allowedPokemon.add("paras");
            allowedPokemon.add("pikachu");
            allowedPokemon.add("weedle");
            allowedPokemon.add("caterpie");
            allowedPokemon.add("spinarak");
            allowedPokemon.add("ledyba");
            allowedPokemon.add("hoothoot");
            allowedPokemon.add("shroomish");
            allowedPokemon.add("combee");
         } else if (this.name.equals("savanna1")) {
            allowedPokemon.add("ponyta");
            allowedPokemon.add("miltank");

            for (int i = 0; i < 2; i++) {
               allowedPokemon.add("drowzee");
               allowedPokemon.add("oddish");
               allowedPokemon.add("chikorita");
               allowedPokemon.add("bulbasaur");
               allowedPokemon.add("hoppip");
               allowedPokemon.add("pidgey");
               allowedPokemon.add("taillow");
               allowedPokemon.add("charmander");
               allowedPokemon.add("cyndaquil");
               allowedPokemon.add("mareep");
               allowedPokemon.add("ekans");
               allowedPokemon.add("doduo");
               allowedPokemon.add("sentret");
               allowedPokemon.add("rattata");
               allowedPokemon.add("yanma");
               allowedPokemon.add("poochyena");
               allowedPokemon.add("eevee");
               allowedPokemon.add("cutiefly");
               allowedPokemon.add("shinx");
            }
         } else if (this.name.equals("savanna2")) {
            allowedPokemon.add("ponyta");
            allowedPokemon.add("miltank");
            allowedPokemon.add("tauros");
            allowedPokemon.add("drowzee");
            allowedPokemon.add("spearow");
            allowedPokemon.add("girafarig");
            allowedPokemon.add("aipom");
            allowedPokemon.add("doduo");
            allowedPokemon.add("sentret");
            allowedPokemon.add("rattata");
            allowedPokemon.add("yanma");
            allowedPokemon.add("eevee");
            allowedPokemon.add("shinx");
            allowedPokemon.add("chansey");
            allowedPokemon.add("scyther");
            allowedPokemon.add("pinsir");
            allowedPokemon.add("rhyhorn");
            allowedPokemon.add("nidoran_m");
            allowedPokemon.add("nidoran_f");
            allowedPokemon.add("kangaskhan");
            allowedPokemon.add("phanpy");
            allowedPokemon.add("natu");
            allowedPokemon.add("mankey");
            allowedPokemon.add("mareep");
            allowedPokemon.add("plusle");
            allowedPokemon.add("minun");
         } else if (this.name.equals("beach1")) {
            allowedPokemon.add("squirtle");
            allowedPokemon.add("krabby");
            allowedPokemon.add("totodile");
            allowedPokemon.add("shellder");
            allowedPokemon.add("wooper");
            allowedPokemon.add("staryu");
            allowedPokemon.add("marill");
            allowedPokemon.add("slowpoke");
            allowedPokemon.add("psyduck");
            allowedPokemon.add("wingull");
            allowedPokemon.add("corphish");
         } else if (this.name.equals("beach2")) {
            allowedPokemon.add("squirtle");
            allowedPokemon.add("krabby");
            allowedPokemon.add("totodile");
            allowedPokemon.add("shellder");
            allowedPokemon.add("wooper");
            allowedPokemon.add("staryu");
            allowedPokemon.add("marill");
            allowedPokemon.add("slowpoke");
            allowedPokemon.add("psyduck");
            allowedPokemon.add("corphish");
            allowedPokemon.add("octillery");
            allowedPokemon.add("araichu");
            allowedPokemon.add("aexeggutor");
            allowedPokemon.add("sandygast");
            allowedPokemon.add("pikipek");
         } else if (this.name.equals("beach2_rocks")) {
            allowedPokemon.add("corsola");
            allowedPokemon.add("corsola");
            allowedPokemon.add("corsola");
            allowedPokemon.add("corsola");
            allowedPokemon.add("squirtle");
            allowedPokemon.add("krabby");
            allowedPokemon.add("totodile");
            allowedPokemon.add("shellder");
            allowedPokemon.add("staryu");
            allowedPokemon.add("corphish");
         } else if (this.name.equals("beach2_plateau")) {
            allowedPokemon.add("corsola");
            allowedPokemon.add("corsola");
            allowedPokemon.add("corsola");
            allowedPokemon.add("corsola");
            allowedPokemon.add("squirtle");
            allowedPokemon.add("krabby");
            allowedPokemon.add("totodile");
            allowedPokemon.add("shellder");
            allowedPokemon.add("staryu");
            allowedPokemon.add("corphish");
            allowedPokemon.add("wingull");
         } else if (!this.name.equals("beach2_water")) {
            if (this.name.equals("desert1")) {
               allowedPokemon.add("kangaskhan");
               allowedPokemon.add("cubone");
               allowedPokemon.add("numel");
               allowedPokemon.add("camerupt");
               allowedPokemon.add("drapion");
            } else if (this.name.equals("wooded_lake1")) {
               allowedPokemon.add("pidgey");
               allowedPokemon.add("ledyba");
               allowedPokemon.add("cutiefly");
               allowedPokemon.add("hoothoot");
               allowedPokemon.add("zubat");
               allowedPokemon.add("paras");
               allowedPokemon.add("scyther");
               allowedPokemon.add("pinsir");
               allowedPokemon.add("spinarak");
               allowedPokemon.add("murkrow");
               allowedPokemon.add("chingling");
               allowedPokemon.add("pichu");
               allowedPokemon.add("ekans");
               allowedPokemon.add("oddish");
               allowedPokemon.add("natu");
               allowedPokemon.add("taillow");
               allowedPokemon.add("ralts");
               allowedPokemon.add("shroomish");
               allowedPokemon.add("phanpy");
               allowedPokemon.add("wooper");
               allowedPokemon.add("yanma");
               allowedPokemon.add("azurill");
               allowedPokemon.add("exeggcute");
               allowedPokemon.add("slowpoke");
               allowedPokemon.add("poliwag");
               allowedPokemon.add("makuhita");
            } else if (this.name.equals("wooded_lake_water1")) {
               allowedPokemon.add("magikarp");
               allowedPokemon.add("surskit");
               allowedPokemon.add("lotad");
               allowedPokemon.add("wooper");
               allowedPokemon.add("azurill");
               allowedPokemon.add("goldeen");
               allowedPokemon.add("buizel");
            } else if (this.name.equals("wooded_lake_fishing1")) {
               allowedPokemon.add("magikarp");
               allowedPokemon.add("poliwag");
               allowedPokemon.add("goldeen");
               allowedPokemon.add("dratini");
               allowedPokemon.add("feebas");
               allowedPokemon.add("corphish");
            } else if (this.name.equals("graveyard1")) {
               allowedPokemon.add("gastly");
               allowedPokemon.add("litwick");
               allowedPokemon.add("murkrow");
               allowedPokemon.add("drifloon");
               allowedPokemon.add("misdreavus");
               allowedPokemon.add("zubat");
               allowedPokemon.add("chingling");
               allowedPokemon.add("chimecho");
               allowedPokemon.add("cubone");
               allowedPokemon.add("houndour");
               allowedPokemon.add("hoothoot");
               allowedPokemon.add("spinarak");
               allowedPokemon.add("absol");
               allowedPokemon.add("duskull");
            } else if (this.name.equals("sand_pit1")) {
               allowedPokemon.add("diglett");
               allowedPokemon.add("sandile");
            } else if (this.name.equals("oasis1")) {
               allowedPokemon.add("surskit");
               allowedPokemon.add("poliwag");
               allowedPokemon.add("lotad");
               allowedPokemon.add("lombre");
               allowedPokemon.add("zigzagoon");
               allowedPokemon.add("sandshrew");
               allowedPokemon.add("exeggcute");
            } else if (this.name.equals("ruins1_outer")) {
               allowedPokemon.add("sigilyph");
               allowedPokemon.add("natu");
               allowedPokemon.add("nosepass");
               allowedPokemon.add("beldum");
               allowedPokemon.add("metang");
               allowedPokemon.add("smeargle");
               allowedPokemon.add("solrock");
               allowedPokemon.add("bronzor");
            } else if (this.name.equals("ruins1_inner")) {
               allowedPokemon.add("lunatone");
               allowedPokemon.add("elgyem");
               allowedPokemon.add("duskull");
               allowedPokemon.add("sandile");
               allowedPokemon.add("sandshrew");
            } else if (this.name.equals("snow1")) {
               allowedPokemon.add("larvitar");
               allowedPokemon.add("sneasel");
               allowedPokemon.add("aron");
               allowedPokemon.add("swinub");
               allowedPokemon.add("piloswine");
               allowedPokemon.add("jynx");
               allowedPokemon.add("delibird");
               allowedPokemon.add("snorunt");
               allowedPokemon.add("riolu");
               allowedPokemon.add("gdarumaka");
            } else if (!this.name.equals("fossil_lab1")) {
               if (this.name.equals("pkmnmansion1")) {
                  allowedPokemon.add("magmar");
                  allowedPokemon.add("grimer");
                  allowedPokemon.add("rattata");
                  allowedPokemon.add("koffing");
                  allowedPokemon.add("vulpix");
                  allowedPokemon.add("growlithe");
                  allowedPokemon.add("ponyta");
                  allowedPokemon.add("ditto");
               } else if (this.name.equals("regi_cave1")) {
                  allowedPokemon.clear();
               } else if (this.name.equals("oasis_pond1")) {
                  allowedPokemon.add("poliwag");
                  allowedPokemon.add("goldeen");
                  allowedPokemon.add("feebas");
                  allowedPokemon.add("remoraid");
               } else if (this.name.equals("sea1")) {
                  allowedPokemon.add("tentacool");
                  allowedPokemon.add("magikarp");
                  allowedPokemon.add("corsola");
                  allowedPokemon.add("horsea");
                  allowedPokemon.add("qwilfish");
               } else if (this.name.equals("ocean1")) {
                  allowedPokemon.add("carvanha");
               } else if (this.name.equals("river1")) {
                  allowedPokemon.add("magikarp");
                  allowedPokemon.add("poliwag");
                  allowedPokemon.add("goldeen");
                  allowedPokemon.add("remoraid");
               } else if (this.name.equals("sand_fishing1")) {
                  allowedPokemon.add("trapinch");
                  allowedPokemon.add("trapinch");
                  allowedPokemon.add("sandile");
                  allowedPokemon.add("sandile");
                  allowedPokemon.add("gible");
               } else if (this.name.equals("rock_smash1")) {
                  allowedPokemon.add("geodude");
                  allowedPokemon.add("geodude");
                  allowedPokemon.add("shuckle");
               } else if (this.name.equals("")) {
                  allowedPokemon.clear();
               } else if (this.name.equals("volcano1")) {
                  allowedPokemon.add("slugma");
                  allowedPokemon.add("geodude");
                  allowedPokemon.add("charmander");
                  allowedPokemon.add("magmar");
                  allowedPokemon.add("torkoal");
                  allowedPokemon.add("cubone");
                  allowedPokemon.add("koffing");
                  allowedPokemon.add("diglett");
                  allowedPokemon.add("numel");
                  allowedPokemon.add("amarowak");
               } else {
                  allowedPokemon.clear();
                  allowedPokemon.add("mamoswine");
                  allowedPokemon.add("weavile");
                  allowedPokemon.add("magmortar");
                  allowedPokemon.add("electivire");
                  allowedPokemon.add("metagross");
               }
            }
         } else {
            for (int i = 0; i < 3; i++) {
               allowedPokemon.add("staryu");
               allowedPokemon.add("squirtle");
               allowedPokemon.add("mantine");
               allowedPokemon.add("totodile");
               allowedPokemon.add("tentacool");
               allowedPokemon.add("magikarp");
            }

            allowedPokemon.add("lapras");
            allowedPokemon.add("araichu");
         }

         Route.allowedPokemon.put(this.name, allowedPokemon);
      }

      return Route.allowedPokemon.get(this.name);
   }

   public String getNextMusic(boolean random) {
      int nextIndex = 0;
      if (random) {
         nextIndex = this.musicsIndex;

         while (nextIndex == this.musicsIndex) {
            nextIndex = Game.staticGame.map.rand.nextInt(this.musics().size());
         }
      } else {
         nextIndex = (this.musicsIndex + 1) % this.musics().size();
      }

      this.musicsIndex = nextIndex;
      return this.musics().get(this.musicsIndex);
   }

   public ArrayList<String> goodRodPokemon() {
      ArrayList<String> pokemon = new ArrayList<>();
      pokemon.add("corsola");
      pokemon.add("horsea");
      return pokemon;
   }

   public ArrayList<String> superRodPokemon() {
      ArrayList<String> pokemon = new ArrayList<>();
      pokemon.add("feebas");
      pokemon.add("remoraid");
      pokemon.add("qwilfish");
      pokemon.add("gible");
      return pokemon;
   }

   public ArrayList<String> dayPokemon() {
      ArrayList<String> dayPokemon = new ArrayList<>();
      dayPokemon.add("ledyba");
      dayPokemon.add("ledian");
      dayPokemon.add("sunkern");
      dayPokemon.add("sunflora");
      dayPokemon.add("spearow");
      dayPokemon.add("fearow");
      dayPokemon.add("pidgey");
      dayPokemon.add("pidgeotto");
      dayPokemon.add("pidgeot");
      dayPokemon.add("ponyta");
      dayPokemon.add("rapidash");
      dayPokemon.add("growlithe");
      dayPokemon.add("arcanine");
      dayPokemon.add("doduo");
      dayPokemon.add("dodrio");
      dayPokemon.add("hoppip");
      dayPokemon.add("skiploom");
      dayPokemon.add("jumpluff");
      dayPokemon.add("miltank");
      dayPokemon.add("tauros");
      dayPokemon.add("solrock");
      dayPokemon.add("sentret");
      dayPokemon.add("furret");
      dayPokemon.add("bellossom");
      dayPokemon.add("mawile");
      dayPokemon.add("yanma");
      return dayPokemon;
   }

   public ArrayList<String> nightPokemon() {
      ArrayList<String> nightPokemon = new ArrayList<>();
      nightPokemon.add("hoothoot");
      nightPokemon.add("noctowl");
      nightPokemon.add("spinarak");
      nightPokemon.add("ariados");
      nightPokemon.add("houndour");
      nightPokemon.add("houndoom");
      nightPokemon.add("poochyena");
      nightPokemon.add("mightyena");
      nightPokemon.add("oddish");
      nightPokemon.add("gloom");
      nightPokemon.add("vileplume");
      nightPokemon.add("murkrow");
      nightPokemon.add("honchkrow");
      nightPokemon.add("sneasel");
      nightPokemon.add("zubat");
      nightPokemon.add("golbat");
      nightPokemon.add("crobat");
      nightPokemon.add("lunatone");
      nightPokemon.add("cleffa");
      nightPokemon.add("clefairy");
      nightPokemon.add("clefable");
      nightPokemon.add("igglybuff");
      nightPokemon.add("jigglypuff");
      nightPokemon.add("wigglytuff");
      nightPokemon.add("sableye");
      nightPokemon.add("gastly");
      nightPokemon.add("haunter");
      nightPokemon.add("gengar");
      return nightPokemon;
   }
}
