package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import java.nio.file.Files;
import java.nio.file.Paths;

/** Real graphical integration driver. Uses production input and map/network adapter. */
public final class LanGameE2E extends Game {
    private long readyAt;
    private long saveStarted;
    private int stage;
    private Vector2 initialPosition;
    private String movementDirection;
    private boolean photographed;
    private boolean hudChecked, chatSent, chatReceived;
    private long threePeersAt;
    private long firstPeerAt;
    private Vector2 lastRemote;
    private int peerFrames;
    private final String role;
    private final long launched = System.nanoTime();
    LanGameE2E(String[] args, String role) { super(args, 2); this.role = role; }
    private void mark(String event) { System.out.println("E2E " + role + " " + event); }
    @Override public void create() { super.create(); InputProcessor.acceptInput = false; Game.musicDisabled = true; }
    @Override public void render() {
        try {
            super.render();
            if ((System.nanoTime() - launched) / 1_000_000_000L > 240) throw new AssertionError("Graphical integration deadline");
            if (coop != null && coop.problem() != null) throw new AssertionError(coop.problem());
            if (coop != null && coop.simulationBlocked()) return;
            if (coop == null || !coop.ready()) return;
            if (readyAt == 0) {
                readyAt = System.nanoTime();
                verifyFieldVisual();
                // A loaded host may have saved far from the island's join spawn.
                if (role.equals("host")) player.position.set(coop.sharedSpawn());
                if (map.overworldTiles.size() < 100) throw new AssertionError("Not a real island");
                if (!coop.worldMatchesAuthority()) throw new AssertionError("Joined world differs from authority");
                if (!role.equals("host")) {
                    for (Pokemon pokemon : map.pokemon.values()) {
                        if (pokemon.drawLower == null || pokemon.drawUpper == null || pokemon.standingAction == null)
                            throw new AssertionError("Guest world Pokemon lacks drawing/interaction state: " + pokemon.nickname);
                    }
                    mark("REPLICAS_INITIALIZED");
                }
                mark("WORLD_READY tiles=" + map.overworldTiles.size() + " pokemon=" + map.pokemon.size());
            }
            if (role.equals("host") && stage == 3 && coop.session().peers().size() == 1 && players.isEmpty()) {
                mark("DISCONNECTS_CLEANED"); Gdx.app.exit(); return;
            }
            if (coop.session().peers().size() < 2) return;
            if (firstPeerAt == 0) firstPeerAt = System.nanoTime();
            // Socket roster updates can arrive between afterFrame() and this check.
            // Require bounded eventual render-thread installation, not an atomic cross-thread read.
            if (players.isEmpty()) {
                if (System.nanoTime() - firstPeerAt > 45_000_000_000L) throw new AssertionError("Connected peer missing rendered avatar");
                return;
            }
            if (role.equals("host")) for (Player remote : players.values()) {
                if ("guest".equals(remote.name) && (lastRemote == null || !lastRemote.equals(remote.position))) {
                    lastRemote = remote.position.cpy(); mark("REMOTE_POSE pos=" + lastRemote);
                }
            }
            if (!chatSent) {
                for (com.pkmngen.game.multiplayer.LanSession.Pose peer : coop.hud.players().values())
                    if (!peer.id.equals(coop.session().id())) coop.session().message(peer.id, LanChat.encode("Hello from " + role));
                chatSent = true;
            }
            if (!chatReceived) for (LanChat.Line line : coop.hud.chat.lines) if (line.text.contains("Hello from ")) {
                chatReceived = true; mark("CHAT_RECEIVED"); break;
            }
            if (!hudChecked && playerCanMove) {
                com.badlogic.gdx.InputProcessor previousInput = Gdx.input.getInputProcessor();
                boolean previouslyVisible = coop.hud.visible();
                coop.hud.openEditor();
                if (!coop.hud.blocksInput()) throw new AssertionError("Chat did not capture input");
                InputProcessor.aPressed = InputProcessor.leftPressed = true;
                new InputProcessor().step(this);
                if (InputProcessor.aPressed || InputProcessor.leftPressed) throw new AssertionError("Chat leaked gameplay input");
                Gdx.input.getInputProcessor().keyDown(com.badlogic.gdx.Input.Keys.T);
                if (!coop.hud.typing()) throw new AssertionError("T closed chat instead of allowing text");
                Gdx.input.getInputProcessor().keyTyped('t');
                Gdx.input.getInputProcessor().keyTyped('h');
                Gdx.input.getInputProcessor().keyTyped('i');
                coop.hud.closeEditor();
                if (!previouslyVisible && coop.hud.visible()) throw new AssertionError("Closing chat enabled hidden HUD");
                if (Gdx.input.getInputProcessor() != previousInput) throw new AssertionError("Chat did not restore input processor");
                if (coop.hud.players().size() != coop.session().peers().size()) throw new AssertionError("HUD player count incorrect");
                hudChecked = true; mark("HUD_INPUT_OK");
            }
            peerFrames++;
            if (!photographed && peerFrames > 2 && coop.renderedAvatars() > 0) {
                boolean wasVisible = coop.hud.visible();
                if (!wasVisible) coop.hud.toggleVisibility();
                uiBatch.begin(); coop.hud.draw(); uiBatch.end();
                com.badlogic.gdx.graphics.Pixmap picture = ScreenUtils.getFrameBufferPixmap(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                try { PixmapIO.writePNG(Gdx.files.local("peers.png"), picture, -1, true); } finally { picture.dispose(); }
                if (!wasVisible) coop.hud.toggleVisibility();
                photographed = true;
                mark("PEERS_VISIBLE=" + players.size() + " drawCalls=" + coop.renderedAvatars());
            }
            Vector2 edit = coop.sharedSpawn();
            Tile tile = map.overworldTiles.get(edit);
            if (role.equals("host")) {
                if (stage == 0) {
                    tile.hasItem = "log"; tile.hasItemAmount = 1;
                    mark("HOST_EDIT log=1 at=" + edit);
                    stage = 1;
                } else if (stage == 1 && "stone".equals(tile.hasItem) && tile.hasItemAmount == 2) {
                    mark("GUEST_EDIT_OBSERVED stone=2");
                    saveGame(); saveStarted = System.nanoTime(); stage = 10;
                } else if (stage == 10) {
                    if (!Files.isRegularFile(Paths.get(map.id + ".sav/game.json.zip"))) {
                        if (System.nanoTime() - saveStarted > 15_000_000_000L) throw new AssertionError("Host save missing");
                        return;
                    }
                    mark("HOST_SAVE_OK");
                    stage = 2;
                } else if (stage == 2 && coop.session().peers().size() >= 3) {
                    mark("LATE_JOIN_OBSERVED");
                    stage = 3;
                } else if (stage == 3 && coop.session().peers().size() == 1 && players.isEmpty()) {
                    mark("DISCONNECTS_CLEANED");
                    Gdx.app.exit();
                }
            } else if (role.equals("guest")) {
                if (stage == 0 && "log".equals(tile.hasItem) && tile.hasItemAmount == 1) {
                    mark("HOST_EDIT_OBSERVED log=1");
                    tile.hasItem = "stone"; tile.hasItemAmount = 2;
                    initialPosition = player.position.cpy();
                    String[] dirs = {"right", "left", "up", "down"};
                    for (String dir : dirs) {
                        Vector2 target = player.facingPos(dir);
                        Tile candidate = map.tiles.get(target);
                        if (candidate != null && !candidate.isSolid && !candidate.isLedge && !candidate.isWater && !map.pokemon.containsKey(target)) { movementDirection = dir; break; }
                    }
                    if (movementDirection == null) throw new AssertionError("No legal walking neighbor at spawn");
                    player.dirFacing = movementDirection;
                    InputProcessor.rightPressed = movementDirection.equals("right"); InputProcessor.leftPressed = movementDirection.equals("left");
                    InputProcessor.upPressed = movementDirection.equals("up"); InputProcessor.downPressed = movementDirection.equals("down");
                    stage = 1;
                } else if (stage == 1 && !player.position.equals(initialPosition)) {
                    InputProcessor.rightPressed = InputProcessor.leftPressed = InputProcessor.upPressed = InputProcessor.downPressed = false;
                    if (player.position.dst(initialPosition) >= 16) { mark("LOCAL_MOVE_OK pos=" + player.position); stage = 2; }
                } else if (stage == 2 && coop.session().peers().size() >= 3) {
                    if (threePeersAt == 0) threePeersAt = System.nanoTime();
                    if (System.nanoTime() - threePeersAt > 2_000_000_000L) {
                        if (coop.session().rejectedMutations() != 0) throw new AssertionError("Unexpected background edit conflicts: " + coop.session().rejectedMutations());
                        mark("PASS_GUEST"); Gdx.app.exit();
                    }
                }
            } else if (role.equals("late") && "stone".equals(tile.hasItem) && tile.hasItemAmount == 2) {
                mark("LATE_JOIN_STATE_OK");
                if ((System.nanoTime() - readyAt) > 5_000_000_000L) { mark("PASS_LATE"); Gdx.app.exit(); }
            }
        } catch (Throwable failure) { failure.printStackTrace(); System.exit(1); }
    }
    private void verifyFieldVisual() {
        if (player.pokemon.isEmpty()) return;
        player.skinColor = new com.badlogic.gdx.graphics.Color(.65f, .35f, .2f, 1);
        player.setColor(new com.badlogic.gdx.graphics.Color(.2f, .7f, .9f, 1));
        player.currSprite = player.standingSprites.get("down");
        Pokemon member = player.pokemon.get(0);
        Pokemon oldHm = player.hmPokemon;
        String oldMove = player.currFieldMove;
        com.badlogic.gdx.graphics.g2d.Sprite oldSprite = player.currSprite;
        Player remote = new Player();
        remote.character = player.character;
        remote.skinColor = new com.badlogic.gdx.graphics.Color(player.skinColor);
        remote.setColor(new com.badlogic.gdx.graphics.Color(player.color));
        LanVisual normal = LanVisual.read(LanVisual.capture(this), null, remote);
        if (normal == null) throw new AssertionError("Normal avatar visual rejected");
        com.badlogic.gdx.graphics.Texture a = player.currSprite.getTexture(), b = remote.currSprite.getTexture();
        if (!a.getTextureData().isPrepared()) a.getTextureData().prepare();
        if (!b.getTextureData().isPrepared()) b.getTextureData().prepare();
        com.badlogic.gdx.graphics.Pixmap ap = a.getTextureData().consumePixmap(), bp = b.getTextureData().consumePixmap();
        for (int x = 0; x < ap.getWidth(); x++) for (int y = 0; y < ap.getHeight(); y++)
            if (ap.getPixel(x,y) != bp.getPixel(x,y)) throw new AssertionError("Remote avatar colors differ");
        mark("AVATAR_COLORS_OK");
        player.swapSprites(member);
        try {
            for (String move : new String[]{"CUT", "BUILD", "DIG"}) {
                player.currFieldMove = move;
                player.currSprite = player.movingSprites.get("right");
                LanVisual visual = LanVisual.read(LanVisual.capture(this), null, remote);
                if (visual == null || visual.avatar == null || !member.specie.name.equals(visual.avatarSpecies)
                        || remote.currSprite.getRegionX() != player.currSprite.getRegionX()
                        || remote.currSprite.getRegionY() != player.currSprite.getRegionY())
                    throw new AssertionError("Missing field move sprite: " + move);
            }
            mark("FIELD_VISUALS_OK");
        } finally {
            player.swapSprites(member); player.hmPokemon = oldHm;
            player.currFieldMove = oldMove; player.currSprite = oldSprite;
        }
    }
    public static void main(String[] args) {
        String role = System.getProperty("e2e.role");
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("PokeWilds LAN E2E " + role); config.setWindowedMode(Integer.getInteger("e2e.width",480), Integer.getInteger("e2e.height",432)); config.setForegroundFPS(60); config.setIdleFPS(60); config.disableAudio(true);
        new Lwjgl3Application(new LanGameE2E(args, role), config);
        System.out.println("E2E " + role + " EXIT_CLEAN");
    }
}
