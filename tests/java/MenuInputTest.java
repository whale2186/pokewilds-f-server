package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import java.lang.reflect.Proxy;

/** Confirmation aliases and saved bookmark round trip without graphics. */
public final class MenuInputTest {
    public static void main(String[] args) throws Exception {
        final int[] pressed = {-1};
        Gdx.input = (Input) Proxy.newProxyInstance(Input.class.getClassLoader(), new Class<?>[]{Input.class},
            (proxy, method, values) -> {
                if (method.getName().equals("isKeyJustPressed")) return ((Integer) values[0]) == pressed[0];
                if (method.getReturnType() == boolean.class) return false;
                if (method.getReturnType() == int.class) return 0;
                return null;
            });
        InputProcessor.keyboardA = Input.Keys.C;
        for (int key : new int[]{Input.Keys.C, Input.Keys.ENTER, Input.Keys.Z}) {
            pressed[0] = key;
            if (!InputProcessor.menuConfirmJustPressed()) throw new AssertionError("Ignored confirmation " + key);
        }
        pressed[0] = -1;
        if (InputProcessor.menuConfirmJustPressed()) throw new AssertionError("Spurious confirmation");
        InputProcessor.aJustPressed = true;
        if (!InputProcessor.menuConfirmJustPressed()) throw new AssertionError("Ignored controller A");
        InputProcessor.aJustPressed = false;
        InputProcessor.startJustPressed = true;
        if (!InputProcessor.menuConfirmJustPressed()) throw new AssertionError("Ignored Start");
        // The Python runner supplies an isolated directory for the bookmark store.
        ServerList list = new ServerList();
        list.entries.add(new ServerList.Entry("Local game", "127.0.0.1:54555"));
        list.save();
        ServerList loaded = new ServerList();
        loaded.load();
        if (loaded.entries.size() != 1 || !loaded.entries.get(0).endpoint().equals("127.0.0.1:54555"))
            throw new AssertionError("Saved server cannot be selected for joining");
        System.out.println("PASS menu confirmation and server bookmarks");
    }
}
