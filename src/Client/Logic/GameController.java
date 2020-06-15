package Client.Logic;

import Client.Gui.Game;
import Client.Sprites.Car;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;

public class GameController {

    private Game game;

    private final ObjectMapper mapper;
    private final Connection connection;

    private Integer segmentLength;

    private static GameController instance;

    public GameController() {
        mapper = new ObjectMapper();
        connection = new Connection("localhost", 8080);
    }

    public void setValues(Integer segmentLength) {
        this.segmentLength = segmentLength;
    }

    public ArrayList<Line> getTrack() {
        ObjectNode request = mapper.createObjectNode();
        request.put("action", "get_track");

        String data;
        try {
            data = connection.connect(mapper.writeValueAsString(request));
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            return null;
        }

        if (data == null) {
            return null;
        }

        JsonNode response;
        try {
            response = mapper.readTree(data);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            return null;
        }

        return parseTrack(response.get("track"));
    }

    private ArrayList<Line> parseTrack(JsonNode data) {
        ArrayList<Line> track = new ArrayList<>();
        Integer length = data.get("length").asInt();
        System.out.println(data.toPrettyString());

        JsonNode curves = data.get("curves");

        for (Integer i = 0; i < length; i++) {
            Line line = new Line();
            line.z = i * segmentLength.floatValue();

            line.curve = checkInRange(i, curves);

            //Usar para cuestas
//            if (i > 700) {
//                Double value = Math.sin(i / 30.0);
//                line.y = value.floatValue() * camDefaultHeight;
//            }

            track.add(line);
        }

        return track;
    }

    private Float checkInRange(Integer i, JsonNode ranges) {
        for (JsonNode curve : ranges) {
            Integer from = curve.get("from").asInt();
            Integer to = curve.get("to").asInt();

            if (from <= i && i < to) {
                Double intensity = curve.get("intensity").asDouble();
                return intensity.floatValue();
            }
        }

        return 0f;
    }

    public static synchronized GameController getInstance() {
        if (instance == null) {
            instance = new GameController();
        }
        return instance;
    }

    public void setGame(Game game) {
        this.game = game;
    }
}
