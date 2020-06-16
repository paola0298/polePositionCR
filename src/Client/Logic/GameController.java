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

    private String actualCarColor;

    public GameController() {
        mapper = new ObjectMapper();
        connection = new Connection("localhost", 8080);
    }

    /**
     * Metodo para mostrarle al jugador actual los colores de carros disponibles
     * @return Devuelve una lista con los colores disponibles
     */
    public ArrayList<String> getAvailableCars() {
        ObjectNode request = mapper.createObjectNode();
        request.put("action", "get_cars");

        String data;
        try {
            data = connection.connect(mapper.writeValueAsString(request));
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            return null;
        }

        if (data == null)
            return null;

        JsonNode response;
        try {
            response = mapper.readTree(data);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            return null;
        }

        return parseResponse(response.get("cars"));
    }

    /**
     * Metodo para convertir el string recibido a un arrayList
     * @param cars String recibido del servidor
     * @return ArrayList con los carros disponibles
     */
    private ArrayList<String> parseResponse(JsonNode cars) {
        ArrayList<String> carsA = new ArrayList<>();
        JsonNode array_cars = cars.get("array_cars");
        for (JsonNode car : array_cars) {
//            System.out.println("Carro " + car.textValue());
            carsA.add(car.textValue());
        }
        return carsA;
    }

    /**
     * Funcion para enviar al servidor que un carro se utilizo
     * @param carColor Color del carro utilizado
     */
    public void setAvailableCars(String carColor) {
        actualCarColor = carColor;
        ObjectNode request = mapper.createObjectNode();
        request.put("carColor", carColor);
        request.put("action", "set_cars");

        try {
            connection.connect(mapper.writeValueAsString(request));
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }
    }

    public String getActualColorCar() {
        return actualCarColor;
    }

    public void addPlayer(Integer pos, Integer playerX, String carColor, Integer lives) {
        ObjectNode request = mapper.createObjectNode();
        request.put("action", "add_player");
        request.put("pos", pos);
        request.put("playerX", playerX);
        request.put("carColor", carColor);
        request.put("lives", lives);

        try {
            connection.connect(mapper.writeValueAsString(request));
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }
    }

    public ArrayList<Player> getPlayerList() {
        ObjectNode request = mapper.createObjectNode();
        request.put("action", "get_players");

        String data;
        try {
            data = connection.connect(mapper.writeValueAsString(request));
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            return null;
        }

        if (data == null)
            return null;

        JsonNode response;
        try {
            response = mapper.readTree(data);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            return null;
        }

        return getPlayersArray(response.get("players"));
    }

    private ArrayList<Player> getPlayersArray(JsonNode players) {
        ArrayList<Player> playersL = new ArrayList<>();
        for (JsonNode player : players) {
            Integer pos = player.get("pos").asInt();
            Integer playerX = player.get("playerX").asInt();
            Integer lives = player.get("lives").asInt();
            String carColor = player.get("carColor").textValue();

            Player playerObject = new Player(new Car(carColor));
            playerObject.setLives(lives);
            playerObject.setPlayerX(playerX);
            playerObject.setPos(pos);

            playersL.add(playerObject);
        }

        return playersL;
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
