package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.pkmngen.game.util.TextureCache;
import com.badlogic.gdx.math.Vector3;

/** Simple text menus on the same 160 x 144 grid as DrawSetupMenu. */
final class TitleMenu extends Action {
    private enum Screen { TITLE, SERVERS, ADD }
    private final Game game;
    private DrawSetupMenu returnMenu;
    private final ServerList servers;
    private boolean skipOpeningInput;
    private Sprite arrow;
    private Screen screen = Screen.TITLE;
    private int selected;
    private int selectedServer;
    private int cursor = Integer.MAX_VALUE;
    private String playerName = "player", serverName = "", endpoint = "", port = "54555";
    private String error = "";

    TitleMenu(Game game) {
        this(game, new ServerList());
        try { servers.load(); } catch (Exception invalid) { error = "Cannot load servers"; }
    }

    // Also allows navigation and persistence to be exercised without an OpenGL context.
    TitleMenu(Game game, ServerList servers) {
        this.game = game;
        this.servers = servers;
        layer = Layer.gui_5000;
    }

    TitleMenu(Game game, DrawSetupMenu returnMenu, String playerName) {
        this(game);
        this.returnMenu = returnMenu;
        this.playerName = playerName.trim().isEmpty() ? "player" : playerName.trim();
        screen = Screen.SERVERS;
        selected = servers.entries.isEmpty() ? addRow() : 1;
    }

    @Override public String getCamera() { return "gui"; }
    @Override public Action.Layer getLayer() { return layer; }
    @Override public void firstStep(Game ignored) {
        skipOpeningInput = true;
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override public boolean keyTyped(char c) { return type(c); }
        });
    }

    private boolean editing() { return screen == Screen.ADD && selected < 3 || screen == Screen.SERVERS && selected == 0; }
    private String value() { return screen == Screen.SERVERS ? playerName : selected == 0 ? serverName : selected == 1 ? endpoint : port; }
    private boolean type(char c) {
        if (!editing()) return false;
        String value = value();
        cursor = Math.min(cursor, value.length());
        if (c == '\b' && cursor > 0) {
            value = value.substring(0, cursor - 1) + value.substring(cursor--);
        } else if (c == 127 && cursor < value.length()) {
            value = value.substring(0, cursor) + value.substring(cursor + 1);
        } else if (c >= 32 && c < 127 && value.length() < (screen == Screen.SERVERS ? 24 : selected == 0 ? 32 : selected == 1 ? 253 : 5)) {
            if (screen == Screen.ADD && selected == 2 && !Character.isDigit(c)) return true;
            value = value.substring(0, cursor) + c + value.substring(cursor++);
        } else return false;
        if (screen == Screen.SERVERS) playerName = value;
        else if (selected == 0) serverName = value;
        else if (selected == 1) endpoint = value;
        else port = value;
        error = "";
        return true;
    }

    private int connectRow() { return servers.entries.size() + 1; }
    private int addRow() { return connectRow() + 1; }
    private int lastRow() { return screen == Screen.TITLE ? 2 : screen == Screen.ADD ? 4 : connectRow() + 3; }
    private int pageStart() { return selectedServer / 2 * 2; }
    private void select(int row) {
        selected = row;
        cursor = Integer.MAX_VALUE;
        if (screen == Screen.SERVERS && row > 0 && row < connectRow()) selectedServer = row - 1;
    }

    private void back() {
        error = "";
        if (screen == Screen.ADD) {
            screen = Screen.SERVERS;
            select(addRow());
        } else if (screen == Screen.SERVERS && returnMenu != null) {
            game.actionStack.remove(this);
            game.insertAction(returnMenu);
            returnMenu.bindInput();
        } else {
            screen = Screen.TITLE;
            select(1);
        }
    }

    private void activate() {
        error = "";
        if (screen == Screen.TITLE) {
            if (selected == 0) {
                Gdx.input.setInputProcessor(null);
                game.actionStack.remove(this);
                DrawSetupMenu.currIndex = 0;
                game.insertAction(new DrawSetupMenu(game, null));
            } else if (selected == 1) {
                screen = Screen.SERVERS;
                select(servers.entries.isEmpty() ? addRow() : 1);
            } else Gdx.app.exit();
        } else if (screen == Screen.ADD) {
            if (selected == 4) { back(); return; }
            if (selected < 3) { select(selected + 1); return; }
            try {
                ServerList.Entry entry = new ServerList.Entry(serverName, endpoint.trim() + ":" + port.trim());
                servers.entries.add(entry);
                try { servers.save(); }
                catch (Exception failed) { servers.entries.remove(entry); throw failed; }
                screen = Screen.SERVERS;
                selectedServer = servers.entries.size() - 1;
                select(connectRow());
            } catch (Exception invalid) { error = invalid.getMessage() == null ? "Cannot save servers" : invalid.getMessage(); }
        } else if (selected == lastRow()) {
            back();
        } else if (selected == addRow()) {
            screen = Screen.ADD;
            select(0);
            serverName = ""; endpoint = ""; port = "54555";
        } else if (selected == connectRow() + 2) {
            if (servers.entries.isEmpty()) { error = "No server selected"; return; }
            ServerList.Entry removed = servers.entries.remove(selectedServer);
            try { servers.save(); }
            catch (Exception failed) { servers.entries.add(selectedServer, removed); error = "Cannot save servers"; return; }
            selectedServer = Math.max(0, Math.min(selectedServer, servers.entries.size() - 1));
            select(connectRow() + 2);
        } else if (selected > 0 && selected < connectRow()) {
            select(connectRow());
        } else if (selected == connectRow()) {
            if (servers.entries.isEmpty()) { error = "Add a server first"; return; }
            try {
                game.joinCoop(servers.entries.get(selectedServer).endpoint(), playerName);
                Gdx.input.setInputProcessor(null);
                game.actionStack.remove(this);
            } catch (IllegalArgumentException invalid) { error = invalid.getMessage(); }
        } else select(servers.entries.isEmpty() ? addRow() : 1);
    }

    /** Process one event per frame so a transition cannot also activate its destination. */
    void updateInput() {
        if (skipOpeningInput) { skipOpeningInput = false; return; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
                || InputProcessor.bJustPressed && (!editing() || !Gdx.input.isKeyPressed(InputProcessor.keyboardB))) {
            back(); return;
        }
        if (InputProcessor.upJustPressed && (!editing() || !Gdx.input.isKeyPressed(InputProcessor.keyboardUp))
                || Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            select((selected + lastRow()) % (lastRow() + 1)); return;
        }
        if (InputProcessor.downJustPressed && (!editing() || !Gdx.input.isKeyPressed(InputProcessor.keyboardDown))
                || Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            select((selected + 1) % (lastRow() + 1)); return;
        }
        if (editing()) {
            cursor = Math.min(cursor, value().length());
            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) cursor = Math.max(0, cursor - 1);
            if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) cursor = Math.min(value().length(), cursor + 1);
            if (Gdx.input.isKeyJustPressed(Input.Keys.HOME)) cursor = 0;
            if (Gdx.input.isKeyJustPressed(Input.Keys.END)) cursor = value().length();
        }
        if (Gdx.input.justTouched()) {
            Vector3 point = new Vector3(2f * Gdx.input.getX() / Gdx.graphics.getWidth() - 1,
                    1 - 2f * Gdx.input.getY() / Gdx.graphics.getHeight(), 0);
            point.prj(game.uiBatch.getProjectionMatrix().cpy().inv());
            int row = clickedRow(point.x, point.y);
            if (row >= 0) {
                select(row);
                if (!editing() && !(screen == Screen.SERVERS && row > 0 && row < connectRow())) activate();
            }
            return;
        }
        boolean confirm = editing()
                ? Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                    || InputProcessor.startJustPressed && !Gdx.input.isKeyPressed(InputProcessor.keyboardStart)
                    || InputProcessor.aJustPressed && !Gdx.input.isKeyPressed(InputProcessor.keyboardA)
                : InputProcessor.menuConfirmJustPressed();
        if (confirm) activate();
    }

    @Override public void step(Game ignored) {
        updateInput();
        if (!game.actionStack.contains(this)) return;
        if (arrow == null) arrow = new Sprite(TextureCache.get(
                Gdx.files.internal("battle/arrow_right1.png")), 0, 0, 5, 7);
        if (screen == Screen.TITLE) {
            text("POKEWILDS", 40, 128);
            row("Singleplayer", 0, 96); row("Multiplayer", 1, 72); row("Quit", 2, 48);
        } else if (screen == Screen.ADD) {
            text("ADD SERVER", 40, 128);
            field("Server Name", serverName, 0, 112);
            field("IP or Hostname", endpoint, 1, 80);
            field("Port", port, 2, 48);
            row("Save Server", 3, 20); row("Cancel", 4, 8);
        } else {
            text("SERVERS", 48, 128);
            row("Player " + display(playerName, selected == 0, 11), 0, 112);
            if (servers.entries.isEmpty()) text("No servers saved", 8, 88);
            for (int i = pageStart(); i < Math.min(servers.entries.size(), pageStart() + 2); i++) {
                int y = 96 - (i - pageStart()) * 24;
                ServerList.Entry entry = servers.entries.get(i);
                if (i == selectedServer) game.uiBatch.draw(arrow, 0, y);
                text(window(entry.name, 18), 8, y);
                text(window(entry.endpoint(), 18), 8, y - 10);
            }
            row("Connect", connectRow(), 48);
            row("Add Server", addRow(), 36);
            row("Delete", connectRow() + 2, 24);
            row("Back", lastRow(), 12);
        }
        if (!error.isEmpty()) text(window(error.replace('\n', ' '), 20), 0, 0);
    }

    private int clickedRow(float x, float y) {
        if (x < 0 || x >= 160) return -1;
        if (screen == Screen.TITLE) {
            for (int i = 0; i < 3; i++) if (hit(y, 96 - i * 24)) return i;
        } else if (screen == Screen.ADD) {
            for (int i = 0; i < 3; i++) if (y >= 100 - i * 32 && y < 120 - i * 32) return i;
            if (hit(y, 20)) return 3;
            if (hit(y, 8)) return 4;
        } else {
            if (hit(y, 112)) return 0;
            for (int i = pageStart(); i < Math.min(servers.entries.size(), pageStart() + 2); i++) {
                int top = 96 - (i - pageStart()) * 24;
                if (y >= top - 10 && y < top + 8) return i + 1;
            }
            for (int i = 0; i < 4; i++) if (hit(y, 48 - i * 12)) return connectRow() + i;
        }
        return -1;
    }
    private static boolean hit(float y, int baseline) { return y >= baseline - 2 && y < baseline + 10; }
    private String display(String value, boolean active, int width) {
        if (!active) return window(value, width);
        int caret = Math.min(cursor, value.length());
        String marked = value.substring(0, caret) + "_" + value.substring(caret);
        int offset = Math.max(0, caret - width + 1);
        return marked.substring(offset, Math.min(marked.length(), offset + width));
    }
    private void field(String label, String value, int index, int y) {
        row(label, index, y);
        text(display(value, selected == index, 18), 8, y - 12);
    }
    private void row(String value, int index, int y) {
        if (selected == index) game.uiBatch.draw(arrow, 0, y);
        text(value, 8, y);
    }
    private static String window(String value, int width) {
        if (value.length() <= width) return value;
        int offset = (int)(com.badlogic.gdx.utils.TimeUtils.millis() / 500 % (value.length() - width + 1));
        return value.substring(offset, offset + width);
    }
    private void text(String value, float x, float y) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == ':') {
                game.uiBatch.draw(game.textDict.get('.'), x + i * 8, y + 2);
                game.uiBatch.draw(game.textDict.get('.'), x + i * 8, y + 5);
            } else game.uiBatch.draw(game.textDict.get(game.textDict.containsKey(c) ? c : '?'), x + i * 8, y);
        }
    }
}
