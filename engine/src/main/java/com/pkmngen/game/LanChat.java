package com.pkmngen.game;

import com.pkmngen.game.multiplayer.LanSession;
import com.pkmngen.game.util.Json;
import java.util.*;

/** Bounded, transient chat. Identity and display names come only from the socket roster. */
final class LanChat {
    static final int LIMIT = 160, HISTORY = 40;
    static final long HOLD = 1_000_000_000L, FADE = 500_000_000L;
    static float opacity(long received, long now, boolean history) {
        if (history) return 1;
        long age = Math.max(0, now-received);
        return age <= HOLD ? 1 : Math.max(0, 1-(age-HOLD)/(float)FADE);
    }
    boolean hasVisibleLines(long now, boolean history) {
        for (Line line : lines) if (opacity(line.time, now, history) > 0) return true;
        return false;
    }
    static final class Packet { String type = "chat", text; }
    static final class Line {
        final String text; final long time;
        Line(String text, long time) { this.text = text; this.time = time; }
    }
    final ArrayDeque<Line> lines = new ArrayDeque<>();
    private final Map<String, Long> received = new HashMap<>();
    static String clean(String text) {
        if (text == null) return "";
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < text.length() && out.length() < LIMIT; i++) {
            char c = text.charAt(i);
            if (c >= 32 && c <= 126) out.append(c);
        }
        return out.toString().trim();
    }
    static String encode(String text) {
        Packet packet = new Packet(); packet.text = clean(text); return Json.gson.toJson(packet);
    }
    boolean receive(LanSession.Message message, Map<String, LanSession.Pose> peers, long now) {
        try {
            Packet packet = Json.gson.fromJson(message.payload, Packet.class);
            // Gson invokes initializers: require an explicit discriminator on the wire.
            com.google.gson.JsonObject object = Json.gson.fromJson(message.payload, com.google.gson.JsonObject.class);
            if (object == null || !object.has("type") || !"chat".equals(object.get("type").getAsString())) return false;
            LanSession.Pose sender = peers.get(message.sender);
            if (sender == null || packet.text == null || packet.text.length() > LIMIT) return true;
            String text = clean(packet.text);
            received.keySet().retainAll(peers.keySet());
            Long last = received.get(message.sender);
            if (!text.isEmpty() && (last == null || now - last >= 750_000_000L)) {
                received.put(message.sender, now);
                add(clean(sender.name) + " #" + sender.id.substring(0, Math.min(4, sender.id.length())) + ": " + text, now);
            }
            return true;
        } catch (RuntimeException malformed) { return false; }
    }
    void add(String text, long now) {
        lines.addLast(new Line(text, now));
        while (lines.size() > HISTORY) lines.removeFirst();
    }
}
