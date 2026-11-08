package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/** Exercise the production menu state machine and text input without OpenGL. */
public final class TitleMenuTest {
    private static int key = -1;
    private static com.badlogic.gdx.InputProcessor input;
    private static final class JoinGame extends Game {
        String endpoint, name;
        @Override public void joinCoop(String endpoint, String name) {
            LanOptions.forJoin(endpoint, name);
            this.endpoint = endpoint; this.name = name;
        }
    }
    private static Object get(TitleMenu menu, String field) throws Exception {
        Field f = TitleMenu.class.getDeclaredField(field); f.setAccessible(true); return f.get(menu);
    }
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    private static void press(TitleMenu menu, int value) {
        key = value; menu.updateInput(); key = -1;
    }
    private static void type(String value) {
        for (char c : value.toCharArray()) input.keyTyped(c);
    }
    private static void go(TitleMenu menu, int row) throws Exception {
        for (int i = 0; i < 20 && (Integer)get(menu, "selected") != row; i++) press(menu, Input.Keys.DOWN);
        check((Integer)get(menu, "selected") == row, "Unreachable row " + row);
    }
    public static void main(String[] args) throws Exception {
        Gdx.input = (Input)Proxy.newProxyInstance(Input.class.getClassLoader(), new Class<?>[]{Input.class}, (proxy, method, values) -> {
            if (method.getName().equals("setInputProcessor")) { input = (com.badlogic.gdx.InputProcessor)values[0]; return null; }
            if (method.getName().equals("isKeyJustPressed") || method.getName().equals("isKeyPressed")) return (Integer)values[0] == key;
            if (method.getReturnType() == boolean.class) return false;
            if (method.getReturnType() == int.class) return 0;
            return null;
        });
        JoinGame game = new JoinGame();
        ServerList list = new ServerList();
        TitleMenu menu = new TitleMenu(game, list);
        game.actionStack.add(menu); menu.firstStep(game);
        press(menu, Input.Keys.ENTER);
        check(get(menu, "screen").toString().equals("TITLE"), "Opening confirm must not activate destination");
        press(menu, Input.Keys.DOWN); press(menu, Input.Keys.ENTER);
        check(get(menu, "screen").toString().equals("SERVERS"), "Title to servers");
        check((Integer)get(menu, "selected") == 2, "Empty list defaults to Add");
        // Empty connect is recoverable, and Back always returns to title.
        go(menu, 1); press(menu, Input.Keys.ENTER);
        check(!get(menu, "error").equals(""), "Empty connect needs feedback");
        press(menu, Input.Keys.ESCAPE); press(menu, Input.Keys.ENTER);
        press(menu, Input.Keys.ENTER);
        check(get(menu, "screen").toString().equals("ADD"), "Add screen");
        press(menu, Input.Keys.ENTER); // Empty name: next field, no accidental confirmation.
        type("127.0.0.1"); press(menu, Input.Keys.ENTER); press(menu, Input.Keys.ENTER); press(menu, Input.Keys.ENTER);
        check(list.entries.isEmpty() && !get(menu, "error").equals(""), "Invalid save stays editable");
        go(menu, 0); type("Local zx"); // confirmation/cancel letters must stay in the field
        press(menu, Input.Keys.ENTER); press(menu, Input.Keys.ENTER); press(menu, Input.Keys.ENTER); press(menu, Input.Keys.ENTER);
        check(list.entries.size() == 1 && list.entries.get(0).name.equals("Local zx"), "Valid save");
        ServerList reloaded = new ServerList(); reloaded.load();
        check(reloaded.entries.size() == 1 && reloaded.entries.get(0).endpoint().equals("127.0.0.1:54555"), "JSON round trip");
        check(get(menu, "screen").toString().equals("SERVERS"), "Save returns to list");
        // Many servers: all rows remain reachable, mouse targets match the visible page.
        for (int i = 1; i < 5; i++) list.entries.add(new ServerList.Entry("Server " + i, "localhost:" + (54555 + i)));
        go(menu, 5);
        Method click = TitleMenu.class.getDeclaredMethod("clickedRow", float.class, float.class); click.setAccessible(true);
        check((Integer)click.invoke(menu, 40f, 96f) == 5, "Last page click selects visible server");
        check((Integer)click.invoke(menu, 40f, 72f) == -1, "Empty page row is not clickable");
        for (int i = 0; i < 4; i++) check((Integer)click.invoke(menu, 40f, (float)(48 - i * 12)) == 6 + i, "Action hit target");
        go(menu, 7); press(menu, Input.Keys.ENTER); press(menu, Input.Keys.ESCAPE);
        check(get(menu, "screen").toString().equals("SERVERS") && (Integer)get(menu, "selected") == 7, "Cancel restores Add selection");
        go(menu, 8); press(menu, Input.Keys.ENTER);
        check(list.entries.size() == 4 && (Integer)get(menu, "selectedServer") == 3, "Delete clamps server selection");
        go(menu, 5); press(menu, Input.Keys.ENTER);
        check(game.endpoint.equals("localhost:54558") && game.name.equals("player"), "Connect passes selected endpoint and name");
        check(!game.actionStack.contains(menu) && input == null, "Connect releases menu and input");
        System.out.println("PASS server menu transitions, validation, pagination, persistence and connection handoff");
    }
}
