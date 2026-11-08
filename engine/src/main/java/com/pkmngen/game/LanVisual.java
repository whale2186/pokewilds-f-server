package com.pkmngen.game;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.Json;

/** Ephemeral appearance only: never installs a follower in the shared Pokemon map. */
final class LanVisual {
    int texture, x, y, width, height;
    float offsetX, offsetY;
    boolean flipX, flipY;
    String species, direction, avatarSpecies;
    boolean avatarShiny, avatarSurf;
    transient Pokemon avatar;
    float followerX, followerY;
    boolean shiny;
    int phase;
    String emote;
    transient Sprite emoteSprite;
    transient Pokemon follower;
    boolean humanFollower;
    transient Sprite humanFollowerSprite;
    static String capture(Game game) {
        LanVisual value = new LanVisual();
        Player player = game.player;
        for (Action action : game.actionStack) if (action instanceof Player.Emote) value.emote = ((Player.Emote)action).type;
        Sprite sprite = player.isSleeping ? player.sleepingSprite : player.currSprite;
        if (player.isFishing) {
            value.offsetX = "left".equals(player.dirFacing) ? -8 : 0;
            value.offsetY = "down".equals(player.dirFacing) ? -8 : 0;
        }
        for (Action action : game.actionStack) if (action instanceof Player.Sitting) {
            Player.Sitting sitting = (Player.Sitting)action;
            sprite = sitting.sittingSprite;
            value.offsetX = sitting.sittingOn.position.x - player.position.x + sitting.offsetX;
            value.offsetY = sitting.sittingOn.position.y - player.position.y + sitting.offsetY - 4;
        }
        if (player.hmPokemon != null && !player.currFieldMove.isEmpty()
                && !"FLY".equals(player.currFieldMove)) {
            value.avatarSpecies = player.hmPokemon.specie.name;
            value.avatarShiny = player.hmPokemon.isShiny;
            value.avatarSurf = "SURF".equals(player.currFieldMove);
        }
        if (sprite != null && sprite.getTexture() != null) {
            value.texture = value.avatarSpecies != null ? 0 : sprite.getTexture() == player.fishingSprites.getTexture() ? 2
                    : sprite.getTexture() == player.sittingSprites.getTexture() ? 3
                    : sprite.getTexture() == player.sleepingSprite.getTexture() ? 4
                    : player.standingSprites.get("down_running") != null && sprite.getTexture() == player.standingSprites.get("down_running").getTexture() ? 1 : 0;
            value.x = Math.round(Math.min(sprite.getU(), sprite.getU2()) * sprite.getTexture().getWidth());
            value.y = Math.round(Math.min(sprite.getV(), sprite.getV2()) * sprite.getTexture().getHeight());
            value.width = sprite.getRegionWidth(); value.height = sprite.getRegionHeight();
            value.flipX = sprite.isFlipX(); value.flipY = sprite.isFlipY();
        }
        Pokemon pokemon = player.hmPokemon;
        if (value.avatarSpecies == null && pokemon != null && player.pokemon.contains(pokemon)
                && pokemon.currOwSprite != null && (pokemon.drawLower != null || !player.currFieldMove.isEmpty())) {
            value.species = pokemon.specie.name; value.shiny = pokemon.isShiny;
            value.followerX = pokemon.position.x; value.followerY = pokemon.position.y; value.direction = pokemon.dirFacing;
            value.phase = pokemon.currOwSprite == pokemon.movingSprites.get(pokemon.dirFacing) ? 1
                    : pokemon.currOwSprite == pokemon.altMovingSprites.get(pokemon.dirFacing) ? 2 : 0;
        }
        if (value.avatarSpecies != null && pokemon != null && pokemon.drawLower != null && pokemon.drawLower.following) {
            value.humanFollower = true;
            value.followerX = pokemon.position.x; value.followerY = pokemon.position.y;
            value.direction = pokemon.dirFacing;
            value.phase = pokemon.currOwSprite == pokemon.movingSprites.get(pokemon.dirFacing) ? 1
                    : pokemon.currOwSprite == pokemon.altMovingSprites.get(pokemon.dirFacing) ? 2 : 0;
        }
        return Json.gson.toJson(value);
    }
    static LanVisual read(String json, LanVisual previous, Player player) {
        if (json == null || json.isEmpty()) return null;
        try {
            LanVisual value = Json.gson.fromJson(json, LanVisual.class);
            if (value == null || !Float.isFinite(value.offsetX) || !Float.isFinite(value.offsetY)
                    || Math.abs(value.offsetX) > 64 || Math.abs(value.offsetY) > 64 || value.texture < 0 || value.texture > 4 || value.x < 0 || value.y < 0
                    || value.width < 1 || value.width > 64 || value.height < 1 || value.height > 64) return null;
            Sprite base = value.texture == 2 ? player.fishingSprites : value.texture == 3 ? player.sittingSprites
                    : value.texture == 4 ? player.sleepingSprite : player.standingSprites.get(value.texture == 1 ? "down_running" : "down");
            if (value.avatarSpecies != null) {
                if (!value.avatarSpecies.matches("[a-z0-9_]{1,40}")) return null;
                value.avatar = previous != null && value.avatarSpecies.equals(previous.avatarSpecies)
                        && value.avatarShiny == previous.avatarShiny ? previous.avatar
                        : new Pokemon(value.avatarSpecies, 1, Pokemon.Generation.CRYSTAL, value.avatarShiny);
                base = value.avatarSurf && value.avatar.surfSprites != null
                        ? value.avatar.surfSprites.get("down") : value.avatar.standingSprites.get("down");
            }
            if (value.x + value.width > base.getTexture().getWidth() || value.y + value.height > base.getTexture().getHeight()) return null;
            Sprite frame = new Sprite(base.getTexture(), value.x, value.y, value.width, value.height);
            frame.flip(value.flipX, value.flipY); player.currSprite = frame;
            if (value.species != null && value.species.matches("[a-z0-9_]{1,40}")
                    && ("up".equals(value.direction) || "down".equals(value.direction) || "left".equals(value.direction) || "right".equals(value.direction))
                    && Float.isFinite(value.followerX) && Float.isFinite(value.followerY)
                    && Vector2.dst2(value.followerX, value.followerY, player.position.x, player.position.y) < 256 * 256) {
                if (previous != null && value.species.equals(previous.species) && value.shiny == previous.shiny) value.follower = previous.follower;
                else value.follower = new Pokemon(value.species, 1, Pokemon.Generation.CRYSTAL, value.shiny);
            }
            if (value.humanFollower && value.avatarSpecies != null && Float.isFinite(value.followerX)
                    && Float.isFinite(value.followerY) && Vector2.dst2(value.followerX, value.followerY,
                    player.position.x, player.position.y) < 256 * 256) {
                Sprite human = value.phase == 1 ? player.movingSprites.get(value.direction)
                        : value.phase == 2 ? player.altMovingSprites.get(value.direction) : player.standingSprites.get(value.direction);
                if (human != null) value.humanFollowerSprite = new Sprite(human);
            }
            if (value.emote != null) {
                String[] types = {"!", "?", "happy", "skull", "heart", "bolt", "sleep", "fish", "uncomfortable", "..."};
                for (int i = 0; i < types.length; i++) if (types[i].equals(value.emote)) {
                    value.emoteSprite = new Sprite(com.pkmngen.game.util.TextureCache.get(com.badlogic.gdx.Gdx.files.internal("emotes.png")), i * 16, 0, 16, 16);
                }
            }
            return value;
        } catch (RuntimeException invalid) { return null; }
    }
    Vector2 playerOffset() { return new Vector2(offsetX, offsetY); }
    void drawEmote(Game game, Vector2 position) {
        if (emoteSprite != null) { emoteSprite.setPosition(position.x, position.y + 20); emoteSprite.draw(game.mapBatch); }
    }
    void draw(Game game, Vector2 offset) {
        if (humanFollowerSprite != null) {
            humanFollowerSprite.setPosition(followerX + offset.x, followerY + offset.y + 4);
            humanFollowerSprite.draw(game.mapBatch);
        }
        if (follower == null) return;
        Sprite sprite = phase == 1 ? follower.movingSprites.get(direction) : phase == 2 ? follower.altMovingSprites.get(direction) : follower.standingSprites.get(direction);
        if (sprite == null) return;
        Sprite frame = new Sprite(sprite);
        frame.setPosition(followerX + offset.x, followerY + offset.y + 4);
        frame.draw(game.mapBatch);
    }
}
