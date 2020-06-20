package Client.Logic;

import Client.Gui.Game;
import Client.Sprites.Car;
import Client.Sprites.Hole;
import Client.Sprites.Turbo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Clase que controla el juego
 */
public class GameController {

    private Game game;

    private final ObjectMapper mapper;
    private final Connection connection;
    private static GameController instance;

    private String actualCarColor;
    private Integer segmentLength;
    private HashMap<Integer, Hole> holeSprites;
    private HashMap<Integer, Turbo> turboSprites;
    //private ArrayList<Live> liveSprites;

    /**
     * Constructor de la clase GameController
     */
    public GameController() {
        mapper = new ObjectMapper();
        connection = new Connection("localhost", 8080);
        holeSprites = new HashMap<>();
        turboSprites = new HashMap<>();
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

        return parseGameInfo(response);
    }

    public HashMap<Integer, Hole> getHolesList() {
        return this.holeSprites;
    }

    public HashMap<Integer, Turbo> getTurbosList() {
        return this.turboSprites;
    }

    /**
     * Método para parsear el Json recibido del servidor para la pista
     * @param data Json con la información de la pista
     * @return Lista con los elementos de la pista
     */
    private ArrayList<Line> parseGameInfo(JsonNode data) {
        ArrayList<Line> trackLines = new ArrayList<>();

        JsonNode track = data.get("track");
        Integer length = track.get("length").asInt();

        JsonNode curves = track.get("curves");
        JsonNode holes = data.get("holes");
        JsonNode turbos = data.get("turbos");

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


            trackLines.add(line);
        }

        holes.forEach(jsonNode -> {

            Hole hole = new Hole();
            hole.setImage("/res/hole1.png", 180, 100);

            Integer id = jsonNode.get("id").asInt();
            Integer posX = jsonNode.get("posX").asInt();
            Integer posY = jsonNode.get("posY").asInt();

            hole.setId(id);
            hole.setPosition(posX.doubleValue(), posY.doubleValue());

            holeSprites.put(posY, hole);
        });

        turbos.forEach(jsonNode -> {
            Turbo turbo = new Turbo();
            turbo.setImage("/res/rayo.png", 150, 150);

            Integer id = jsonNode.get("id").asInt();
            Integer posX = jsonNode.get("posX").asInt();
            Integer posY = jsonNode.get("posY").asInt();

            turbo.setId(id);
            turbo.setPosition(posX.doubleValue(), posY.doubleValue());

            turboSprites.put(posY, turbo);
        });

        return trackLines;
    }

    /**
     * Método para verificar si un índice dado está dentro de algún rango en una lista
     * @param i Índice actual
     * @param ranges Lista de rangos a verificar
     * @return El valor del rango si i pertenece a ese rango, 0 en caso contrario
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
     * Método que se llama al cerrar la ventana de juego
     */
    public void onExit() {
        ObjectNode request = mapper.createObjectNode();
        request.put("action", "exit");
        request.put("car_color", actualCarColor);

        try {
            connection.connect(mapper.writeValueAsString(request));
        } catch (JsonProcessingException ex) {
            System.err.println("[Error] Could not connect to server!");
        }
    }

    /**
     * Metodo para obtener las vidas del jugador actual
     * @return
     */
    public int getPlayerLives() {
        ObjectNode request = mapper.createObjectNode();
        request.put("action", "get_lives");
        request.put("car_color", actualCarColor);

        String data;
        try {
            data = connection.connect(mapper.writeValueAsString(request));
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            return -1;
        }

        if (data == null) {
            return -1;
        }

        JsonNode response;
        try {
            response = mapper.readTree(data);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            return -1;
        }

        return response.get("lives").asInt();
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
     * Método para setear la instancia de Game
     * @param game Instancia actual de game
     */
    public void setGame(Game game) {
        this.game = game;
    }
}
