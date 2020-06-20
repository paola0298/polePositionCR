package Client.Logic;

import Client.Gui.Game;
import Client.Sprites.Car;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;

/**
 * Clase que controla el juego
 */
public class GameController {

    private Game game;

    private final ObjectMapper mapper;
    private final Connection connection;

    private Integer segmentLength;

    private static GameController instance;

    private String actualCarColor;

    /**
     * Constructor de la clase GameController
     */
    public GameController() {
        mapper = new ObjectMapper();
        connection = new Connection("192.168.0.18", 8080);
    }

    /**
     * Método para mostrarle al jugador actual los colores de carros disponibles
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

        System.out.println(response.toPrettyString());
        return parseResponse(response.get("cars"));
    }

    /**
     * Método para convertir el string recibido a un arrayList
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
     * Método para enviar al servidor que un carro se utilizo
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

    /**
     * Método para obtener el color del carro del jugador actual
     * @return Color del carro
     */
    public String getActualColorCar() {
        return actualCarColor;
    }

    /**
     * Método para enviar una solicitud al servidor para agregar un jugador
     * @param player Jugador a agregar
     */
    public void addPlayer(Player player) {
        ObjectNode request = mapper.createObjectNode();
        request.put("action", "add_player");
        request.put("pos", player.getPos());
        request.put("playerX", player.getPlayerX().intValue());
        request.put("carColor", actualCarColor);
        request.put("lives", player.getLives());
        request.put("points", player.getPoints());

        try {
            connection.connect(mapper.writeValueAsString(request));
        } catch (JsonProcessingException ex) {
            System.err.println("[Error] Could not connect to server");
        }
    }

    /**
     * Método para obtener la lista de jugadores desde el servidor
     * @return Lista con los jugadores
     */
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

    /**
     * Método para enviar una solicitud al servidor para modificar la información de un jugador
     * @param player Jugador a modificar
     */
    public void updatePlayerInfo(Player player) {
        ObjectNode request = mapper.createObjectNode();
        request.put("action", "update_player");
        request.put("pos", player.getPos());
        request.put("playerX", player.getPlayerX());
        request.put("carColor", actualCarColor);
        request.put("lives", player.getLives());
        request.put("points", player.getPoints());

        try {
            connection.connect(mapper.writeValueAsString(request));
        } catch (JsonProcessingException ex) {
            System.err.println("[Error] Could not connect to server!");
        }

    }

    /**
     * Método para parsear el Json con la información de los jugadores
     * @param players Json con los datos de los jugadores
     * @return Lista con los jugadores
     */
    private ArrayList<Player> getPlayersArray(JsonNode players) {
        ArrayList<Player> playersL = new ArrayList<>();
        for (JsonNode player : players) {
            Integer pos = player.get("pos").asInt();
            Integer playerX = player.get("playerX").asInt();
            Integer lives = player.get("lives").asInt();
            String carColor = player.get("carColor").textValue();

            if (carColor.equals(actualCarColor)) continue;

            Player playerObject = new Player(new Car(carColor));
            playerObject.setLives(lives);
            playerObject.setPlayerX(playerX.floatValue());
            playerObject.setPos(pos);

            playersL.add(playerObject);
        }

        return playersL;
    }

    /**
     * Método para colocar el tamaño de la pista
     * @param segmentLength Tamaño de la pista
     */
    public void setValues(Integer segmentLength) {
        this.segmentLength = segmentLength;
    }

    /**
     * Método que hace una solicitud al servidor para obtener la pista
     * @return Lista con los elementos de la pista
     */
    public ArrayList<Line> getGameInfo() {
        ObjectNode request = mapper.createObjectNode();
        request.put("action", "get_game_info");

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

        return parseGameInfo(response.get("track"));
    }

    /**
     * Método para parsear el Json recibido del servidor para la pista
     * @param data Json con la información de la pista
     * @return Lista con los elementos de la pista
     */
    private ArrayList<Line> parseGameInfo(JsonNode data) {
        ArrayList<Line> track = new ArrayList<>();
        Integer length = data.get("length").asInt();
        System.out.println(data.toPrettyString());

        JsonNode curves = data.get("curves");

        for (Integer i = 0; i < length; i++) {
            Line line = new Line();
            line.z = i * segmentLength.floatValue();

            line.curve = checkInRange(i, curves);

            if (i%20 == 0) {
                line.spriteX = -3.5f;
            }
            //Usar para cuestas
//            if (i > 700) {
//                Double value = Math.sin(i / 30.0);
//                line.y = value.floatValue() * camDefaultHeight;
//            }

            track.add(line);
        }

        return track;
    }

    /**
     * TODO hacer documentación
     * @param i
     * @param ranges
     * @return
     */
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

    /**
     * Método para obtener la instancia del controlador
     * @return Retorna la instancia de la clase
     */
    public static synchronized GameController getInstance() {
        if (instance == null) {
            instance = new GameController();
        }
        return instance;
    }

    /**
     * TODO hacer documentación
     * @param game
     */
    public void setGame(Game game) {
        this.game = game;
    }
}
