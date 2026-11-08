import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Client;
import com.google.gson.JsonObject;
import com.pkmngen.game.Network;
import com.pkmngen.game.util.Json;
import com.pkmngen.game.util.Save;
import java.nio.file.Paths;

/** Headless differential checks run against both authority and reconstruction. */
public final class EngineSmoke {
    public static void main(String[] args) throws Exception {
        int checks = 0;
        int[] values = {-32768, -1, 0, 1, 32767};
        for (int x : values) {
            for (int y : values) {
                GridPoint2 point = new GridPoint2(x, y);
                String encoded = Json.gson.toJson(point);
                if (!point.equals(Json.gson.fromJson(encoded, GridPoint2.class))) {
                    throw new AssertionError("GridPoint2 round trip: " + point);
                }
                Vector2 vector = new Vector2(x, y);
                if (!vector.equals(Json.gson.fromJson(Json.gson.toJson(vector), Vector2.class))) {
                    throw new AssertionError("Vector2 round trip: " + vector);
                }
                System.out.println("coordinate " + x + "," + y + "=" + encoded);
                checks += 2;
            }
        }
        com.badlogic.gdx.Gdx.files = new com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files();
        JsonObject data = new JsonObject();
        data.addProperty("world", "development-α");
        data.addProperty("seed", 123456789);
        data.addProperty("enabled", false);
        String path = "smoke-save";
        Save.saveData(data, path);
        if (!data.equals(Save.readData(path, JsonObject.class))) {
            throw new AssertionError("Save JSON/ZIP round trip failed");
        }
        checks++;
        try {
            Save.readData("absent-save", JsonObject.class);
            throw new AssertionError("Missing save must raise ReadFailure");
        } catch (Save.ReadFailure expected) {
            checks++;
        }
        Client client = new Client();
        try {
            Network.register(client);
            String[] names = {"com.pkmngen.game.Network$Login", "com.pkmngen.game.Network$MovePlayer",
                    "com.pkmngen.game.Network$PlayerData", "com.pkmngen.game.Network$MapSaveData"};
            for (String name : names) {
                System.out.println("registration " + name + "=" + client.getKryo().getRegistration(Class.forName(name)).getId());
                checks++;
            }
        } finally {
            client.close();
        }
        System.out.println("PASS " + checks + " headless checks");
    }
}
