import com.pkmngen.game.multiplayer.LanSession;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
public class LanInspect {
    public static void main(String[] args) throws Exception {
        try (LanSession session = LanSession.join(args[0], Integer.parseInt(args[1]), "inspector")) {
            JsonObject value = new JsonParser().parse(session.bootstrap()).getAsJsonObject();
            for (String key : new String[]{"worldId","mapId","timeOfDay","dayTimer","interiorIndex","bottomLeft","topRight","spawn"}) System.out.println(key + "=" + value.get(key));
            System.out.println("routes=" + value.getAsJsonObject("routes").size());
            System.out.println("edges=" + value.getAsJsonArray("edges").size());
            System.out.println("resources=" + session.resources().size());
        }
    }
}
