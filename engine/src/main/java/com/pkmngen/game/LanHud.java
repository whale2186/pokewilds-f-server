package com.pkmngen.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.*;
import com.pkmngen.game.multiplayer.LanSession;
import java.util.*;

/** Small native-resolution overlay; all UI and input ownership stays on the render thread. */
final class LanHud extends InputAdapter {
    private final Game game;
    private final LanCoop coop;
    final LanChat chat = new LanChat();
    private final Set<String> known = new HashSet<>();
    private final Map<String, String> names = new HashMap<>();
    private boolean rosterKnown, typing, roster, details, releasing;
    private boolean hidden = true;
    private int page, scroll;
    private String draft = "";
    private long lastSent;
    private com.badlogic.gdx.InputProcessor previousInput;
    private InputMultiplexer ownedInput;
    private Texture pixel;
    private LanHudLayout screen;
    private float wrappedWidth = -1;
    private final Map<LanChat.Line,java.util.List<String>> wrapped = new IdentityHashMap<>();
    private final com.badlogic.gdx.math.Matrix4 projection = new com.badlogic.gdx.math.Matrix4().setToOrtho2D(0,0,160,144);
    private final com.badlogic.gdx.math.Matrix4 previousProjection = new com.badlogic.gdx.math.Matrix4();
    private final com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
    LanHud(Game game, LanCoop coop) { this.game = game; this.coop = coop; }
    boolean typing() { return typing; }
    boolean visible() { return !hidden || typing || roster; }
    boolean chatHistoryVisible() { return !hidden || typing; }
    void toggleVisibility() { hidden = !hidden; if (hidden) roster = false; }
    static com.badlogic.gdx.math.Vector2 canvasSize(int width, int height) {
        LanHudLayout layout = new LanHudLayout(width,height);
        return new com.badlogic.gdx.math.Vector2(layout.width,layout.height);
    }
    boolean blocksInput() { return typing || releasing; }
    private boolean held() {
        return Gdx.input.isKeyPressed(Keys.ENTER) || Gdx.input.isKeyPressed(Keys.ESCAPE)
                || Gdx.input.isKeyPressed(Keys.T) || Gdx.input.isKeyPressed(InputProcessor.keyboardA)
                || Gdx.input.isKeyPressed(InputProcessor.keyboardB)
                || Gdx.input.isKeyPressed(InputProcessor.keyboardStart);
    }
    void update() {
        if (typing && (!coop.ready() || !game.playerCanMove)) closeEditor();
        if (releasing && !held()) releasing = false;
        if (!coop.ready()) return;
        if (Gdx.input.isKeyJustPressed(Keys.T) && !typing && !releasing && game.playerCanMove) openEditor();
        if (!typing && Gdx.input.isKeyJustPressed(Keys.F3)) { roster = !roster; page = 0; }
        if (!typing && Gdx.input.isKeyJustPressed(Keys.F4)) { details = !details; hidden = false; }
        if (!typing && Gdx.input.isKeyJustPressed(Keys.F1)) toggleVisibility();
        if (!typing && roster && Gdx.input.isKeyJustPressed(Keys.PAGE_DOWN)) page++;
        if (!typing && roster && Gdx.input.isKeyJustPressed(Keys.PAGE_UP)) page = Math.max(0, page - 1);
        Map<String, LanSession.Pose> peers = players();
        long now = System.nanoTime();
        for (LanSession.Pose peer : peers.values()) {
            if (rosterKnown && !known.contains(peer.id)) chat.add(LanChat.clean(peer.name) + " joined", now);
            names.put(peer.id, peer.name);
        }
        if (rosterKnown) for (String id : known) if (!peers.containsKey(id)) chat.add(LanChat.clean(names.get(id)) + " left", now);
        known.clear(); known.addAll(peers.keySet()); names.keySet().retainAll(known); rosterKnown = true;
    }
    Map<String, LanSession.Pose> players() { return players(coop.session(), coop.isDedicated()); }
    static Map<String, LanSession.Pose> players(LanSession session, boolean dedicated) {
        Map<String, LanSession.Pose> out = new LinkedHashMap<>(session.peers());
        if (dedicated) out.remove(session.authorityId());
        return out;
    }
    void openEditor() {
        if (typing || !coop.ready() || !game.playerCanMove) return;
        typing = true; roster = false; scroll = 0;
        previousInput = Gdx.input.getInputProcessor();
        ownedInput = previousInput == null ? new InputMultiplexer(this) : new InputMultiplexer(this, previousInput);
        Gdx.input.setInputProcessor(ownedInput);
        InputProcessor.clearControls();
    }
    void closeEditor() {
        if (!typing) return;
        typing = false; releasing = true;
        if (Gdx.input.getInputProcessor() == ownedInput) Gdx.input.setInputProcessor(previousInput);
        previousInput = null; ownedInput = null; InputProcessor.clearControls();
    }
    private void submit() {
        String text = LanChat.clean(draft);
        if (text.isEmpty()) { closeEditor(); return; }
        if (text.equalsIgnoreCase("/clear")) { chat.lines.clear(); draft = ""; return; }
        if (text.equalsIgnoreCase("/players")) { closeEditor(); roster = true; draft = ""; return; }
        if (text.equalsIgnoreCase("/help")) { chat.add("T chat | F3 players | F4 details", System.nanoTime()); chat.add("/clear /players /help | PgUp/PgDn history", System.nanoTime()); draft = ""; return; }
        if (text.startsWith("/")) { chat.add("Unknown command. Try /help", System.nanoTime()); return; }
        long now = System.nanoTime();
        if (lastSent != 0 && now - lastSent < 1_000_000_000L) { chat.add("Wait a moment before sending again", now); return; }
        boolean sent = true;
        for (LanSession.Pose peer : players().values()) if (!peer.id.equals(coop.session().id()))
            sent &= coop.session().message(peer.id, LanChat.encode(text));
        if (!sent) { chat.add("Chat queue busy; message may be partially delivered", now); lastSent = now; return; }
        chat.add("You: " + text, now); lastSent = now; draft = ""; closeEditor();
    }
    @Override public boolean keyDown(int key) {
        if (!typing) return false;
        if (key == Keys.ESCAPE) closeEditor();
        else if (key == Keys.ENTER) submit();
        else if (key == Keys.BACKSPACE && !draft.isEmpty()) draft = draft.substring(0, draft.length()-1);
        else if (key == Keys.PAGE_UP) scroll++;
        else if (key == Keys.PAGE_DOWN) scroll = Math.max(0, scroll-1);
        else if (key == Keys.V && (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT)))
            draft = LanChat.clean(draft + Gdx.app.getClipboard().getContents());
        return true;
    }
    @Override public boolean keyUp(int key) { return typing || releasing; }
    @Override public boolean keyTyped(char c) {
        if (!typing) return releasing;
        if (c >= 32 && c <= 126 && draft.length() < LanChat.LIMIT
                && !Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) && !Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT)) draft += c;
        return true;
    }
    private void box(float x, float y, float width, float height) { box(x,y,width,height,1); }
    private void box(float x, float y, float width, float height, float alpha) {
        if (pixel == null) {
            Pixmap image = new Pixmap(1, 1, Pixmap.Format.RGBA8888); image.setColor(Color.WHITE); image.fill();
            pixel = new Texture(image); image.dispose();
        }
        Color before = new Color(game.uiBatch.getColor());
        game.uiBatch.setColor(.035f, .055f, .09f, .9f*alpha); game.uiBatch.draw(pixel, x,y,width,height); game.uiBatch.setColor(before);
    }
    private void text(String text, float x, float y) {
        float sx = game.font.getData().scaleX, sy = game.font.getData().scaleY;
        layout.setText(game.font, text);
        if (layout.width > screen.contentWidth()) {
            float fit = screen.contentWidth()/layout.width;
            game.font.getData().setScale(sx*fit, sy*fit);
        }
        game.font.draw(game.uiBatch, text, x, y);
        game.font.getData().setScale(sx,sy);
    }
    private double measure(String text) { layout.setText(game.font,text); return layout.width; }
    void draw() {
        if (!coop.ready() || (!visible() && !chat.hasVisibleLines(System.nanoTime(), false))) return;
        screen = new LanHudLayout(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        projection.setToOrtho2D(0,0,screen.width,screen.height);
        float x=screen.margin, top=screen.top(), w=screen.panelWidth, pad=screen.padding;
        previousProjection.set(game.uiBatch.getProjectionMatrix());
        game.uiBatch.setProjectionMatrix(projection);
        float sx = game.font.getData().scaleX, sy = game.font.getData().scaleY;
        Color old = new Color(game.font.getColor()); game.font.getData().setScale(1.1f); game.font.setColor(Color.WHITE);
        try {
            if (!visible()) { drawChat(); return; }
            Map<String, LanSession.Pose> peers = players();
            box(x,top-screen.headerHeight,w,screen.headerHeight);
            text("ONLINE  " + peers.size() + " player" + (peers.size()==1 ? "" : "s") + " | " + game.map.timeOfDay,x+pad,top-8);
            if (details) {
                box(x,top-screen.headerHeight-44,w,44);
                text("FPS " + Gdx.graphics.getFramesPerSecond() + " | " + (coop.isGuest() ? "Guest" : "Host") + " | " + (coop.editPending() ? "Syncing edit" : "Synced"),x+pad,top-36);
                text("X " + (int)(game.player.position.x/16) + " Y " + (int)(game.player.position.y/16) + " | " + (game.map.tiles==game.map.overworldTiles ? "Outside" : "Interior"),x+pad,top-56);
            }
            if (roster) {
                java.util.List<LanSession.Pose> sorted = new ArrayList<>(peers.values());
                sorted.sort(Comparator.comparing((LanSession.Pose p) -> p.name).thenComparing(p -> p.id));
                int perPage=screen.rosterRows(details);
                int pages=Math.max(1,(sorted.size()+perPage-1)/perPage); page=Math.min(page,pages-1);
                float rx=screen.rosterX(), rt=screen.rosterTop(details);
                float height=screen.headerHeight+perPage*22;
                box(rx,rt-height,w,height);
                text("PLAYERS " + (page+1) + "/" + pages + "  PgUp/PgDn",rx+pad,rt-8);
                for(int i=page*perPage;i<Math.min(sorted.size(),page*perPage+perPage);i++) {
                    LanSession.Pose p=sorted.get(i);
                    text(LanChat.clean(p.name) + " #" + p.id.substring(0,Math.min(4,p.id.length()))
                            + (p.id.equals(coop.session().id()) ? " (you)" : p.id.equals(coop.session().authorityId()) ? " (host)" : ""),
                            rx+pad,rt-screen.headerHeight-8-(i%perPage)*22);
                }
            } else drawChat();
            if (coop.editNotice()!=null && !roster) {
                float nt=top-screen.headerHeight-(details ? 44 : 0);
                box(x,nt-28,w,28); text("Edit changed on server; try again",x+pad,nt-8);
            }
            float footer=typing ? screen.editorHeight : screen.footerHeight;
            box(x,screen.margin,w,footer);
            if (typing) {
                text("CHAT  " + draft.length() + "/" + LanChat.LIMIT,x+pad,screen.margin+footer-8);
                String cursor=(System.nanoTime()/500_000_000L)%2==0 ? "_" : " ";
                String tail=LanHudLayout.tail(draft,screen.contentWidth()-(float)measure("> _"),this::measure);
                text("> " + tail + cursor,x+pad,screen.margin+footer-28);
                text("Enter send | Esc close",x+pad,screen.margin+16);
            } else {
                text("T Chat     F3 Players",x+pad,screen.margin+screen.footerHeight-8);
                text("F4 Info    F1 Hide",x+pad,screen.margin+16);
            }
        } finally {
            game.font.getData().setScale(sx,sy); game.font.setColor(old);
            game.uiBatch.setProjectionMatrix(previousProjection);
        }
    }
    private static final class ChatRow {
        final String text; final float alpha;
        ChatRow(String text, float alpha) { this.text=text; this.alpha=alpha; }
    }
    private void drawChat() {
        java.util.List<ChatRow> rows = new ArrayList<>(); long now=System.nanoTime();
        boolean history = chatHistoryVisible();
        for (LanChat.Line line : chat.lines) {
            float alpha = LanChat.opacity(line.time, now, history);
            if (alpha <= 0) continue;
            if (wrappedWidth != screen.contentWidth()) { wrapped.clear(); wrappedWidth=screen.contentWidth(); }
            java.util.List<String> parts=wrapped.computeIfAbsent(line,
                    entry -> LanHudLayout.wrap(entry.text,screen.contentWidth(),this::measure));
            for (String part : parts) rows.add(new ChatRow(part,alpha));
        }
        wrapped.keySet().retainAll(chat.lines);
        int count=screen.chatRows(typing,history,details,coop.editNotice()!=null,visible());
        scroll=Math.max(0,Math.min(scroll,Math.max(0,rows.size()-count)));
        int end=rows.size()-(typing ? scroll : 0), begin=Math.max(0,end-count);
        float bottom=screen.chatBottom(typing,visible());
        Color before = new Color(game.font.getColor());
        for (int i=begin;i<end;i++) {
            ChatRow row = rows.get(i);
            float y=bottom+screen.padding+(end-i)*screen.lineHeight;
            box(screen.margin,y-screen.lineHeight+4,screen.panelWidth,screen.lineHeight,row.alpha);
            game.font.setColor(before.r,before.g,before.b,before.a*row.alpha);
            text(row.text,screen.margin+screen.padding,y);
        }
        game.font.setColor(before);
    }
    void dispose() { closeEditor(); if (pixel!=null) { pixel.dispose(); pixel=null; } }
}
