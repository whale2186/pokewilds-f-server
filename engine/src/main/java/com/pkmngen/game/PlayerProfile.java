package com.pkmngen.game;

import com.badlogic.gdx.graphics.Color;
import com.pkmngen.game.util.Json;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.IOException;

/** Appearance preferences are local and independent of a world or display-name identity. */
final class PlayerProfile {
    String name, character;
    int color, skinColor;
    boolean valid() {
        return name!=null && name.length()>0 && name.length()<=24 && LanChat.clean(name).equals(name)
            && character!=null && character.matches("[A-Za-z0-9_-]{1,40}") && (color&255)==255 && (skinColor&255)==255;
    }
    static PlayerProfile read(Path file) {
        try {
            if(!Files.exists(file) || Files.size(file)>8192) return null;
            PlayerProfile value=Json.gson.fromJson(new String(Files.readAllBytes(file),StandardCharsets.UTF_8),PlayerProfile.class);
            return value!=null && value.valid()?value:null;
        } catch(IOException|RuntimeException invalid) { System.err.println("Cannot read player appearance: "+invalid.getMessage()); return null; }
    }
    void write(Path file) throws IOException {
        if(!valid()) throw new IOException("Invalid player appearance");
        Path absolute=file.toAbsolutePath(); Files.createDirectories(absolute.getParent());
        Path temporary=Files.createTempFile(absolute.getParent(),"player-profile-",".tmp");
        try {
            Files.write(temporary,Json.gson.toJson(this).getBytes(StandardCharsets.UTF_8));
            try { Files.move(temporary,absolute,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING); }
            catch(AtomicMoveNotSupportedException unsupported) { Files.move(temporary,absolute,StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporary); }
    }
    static PlayerProfile of(Player player,String name) {
        PlayerProfile value=new PlayerProfile(); value.name=name; value.character=player.character;
        value.color=Color.rgba8888(player.color); value.skinColor=Color.rgba8888(player.skinColor); return value;
    }
    boolean same(PlayerProfile other) {
        return other!=null && name.equals(other.name) && character.equals(other.character) && color==other.color && skinColor==other.skinColor;
    }
    void apply(Player player) {
        player.name=name; player.character=character; player.skinColor=new Color(skinColor); player.setColor(new Color(color));
        if (player.currFieldMove.isEmpty()) player.currSprite=player.standingSprites.get(player.dirFacing);
    }
}
