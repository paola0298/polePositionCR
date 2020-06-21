package Client.Logic;

import Client.Gui.Game;
import Client.Sprites.*;
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
    private Boolean gameFinished;

    private final ObjectMapper mapper;
    private final Connection connection;
    private static GameController instance;

    private String actualCarColor;
    private Integer segmentLength;
    private HashMap<Integer, Hole> holeSprites;
    private HashMap<Integer, Turbo> turboSprites;
    private HashMap<Integer, Live> liveSprites;

    private Player actualPlayer;

    /**
     * Constructor de la clase GameController
     */
    public GameController() {
        mapper = new ObjectMapper();
        connection = new Connection("192.168.0.18", 8080);
        holeSprites = new HashMap<>();
        turboSprites = new HashMap<>();
        liveSprites = new HashMap<>();
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
    public HashMap<Integer, Player> getPlayerList() {
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
     * Método para obtener el jugador actual
     * @return Retorna un objeto tipo Player del jugador actual
     */
    public Player getActualPlayer() {
        return actualPlayer;
    }

    /**
     * Método que coloca el jugador actual
     * @param player Recibe un objeto tipo Player
     */
    public void setActualPlayer(Player player) {
        actualPlayer = player;
    }

    /**
     * Método para parsear el Json con la información de los jugadores
     * @param players Json con los datos de los jugadores
     * @return Lista con los jugadores
     */
//    private ArrayList<Player> getPlayersArray(JsonNode players) {
//        ArrayList<Player> playersL = new ArrayList<>();
//        for (JsonNode player : players) {
//            Integer pos = player.get("pos").asInt();
//            Integer playerX = player.get("playerX").asInt();
//            Integer lives = player.get("lives").asInt();
//            String carColor = player.get("carColor").textValue();
//
//            if (carColor.equals(actualCarColor)) continue;
//
//            Player playerObject = new Player(new Car(carColor));
//            playerObject.setLives(lives);
//            playerObject.setPlayerX(playerX.floatValue());
//            playerObject.setPos(pos);
//
//            playersL.add(playerObject);
//        }
//
//        return playersL;
//    }
    private HashMap<Integer, Player> getPlayersArray(JsonNode players) {
        HashMap<Integer, Player> playerHashMap = new HashMap<>();
        for(JsonNode jsonNode: players) {
            String carColor = jsonNode.get("carColor").textValue();
            if (carColor.equals(actualCarColor)) continue;

            Integer pos = jsonNode.get("pos").asInt();
            Integer playerX = jsonNode.get("playerX").asInt();
            Integer lives = jsonNode.get("lives").asInt();
            Integer points = jsonNode.get("points").asInt();

            Car playerCar = new Car(carColor);
            playerCar.setImage("/res/Carro"+carColor+".png", 100, 180);
            Player playerObject = new Player(playerCar);
            playerObject.setLives(lives);
            playerObject.setPlayerX(playerX.floatValue());
            playerObject.setPos(pos);
            playerObject.setPoints(points);

            playerCar.setProjectedPosX(playerX.doubleValue());
            playerCar.setProjectedPosY(pos.doubleValue());
            playerCar.setProjectedWidth(200d);
            playerCar.setProjectedHeight(120d);

            playerHashMap.put((pos / segmentLength), playerObject);
        }

        return playerHashMap;
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

    /**
     * Método para obtener un diccionario con los huecos desde el servidor
     * @return Retorna un diccionario con los huecos como valores y las llaves la posición en y
     */
    public HashMap<Integer, Hole> getHolesList() {
        return this.holeSprites;
    }

    /**
     * Método para obtener un diccionario con los turbos desde el servidor
     * @return Retorna un diccionario con los turbos como valores y las llaves la posición en y
     */
    public HashMap<Integer, Turbo> getTurbosList() {
        return this.turboSprites;
    }

    /**
     * Método para obtener un diccionario con los turbos desde el servidor
     * @return Retorna un diccionario con los turbos como valores y las llaves la posición en y
     */
    public HashMap<Integer, Live> getLiveList() {
        return this.liveSprites;
    }

    /**
     * Método para actualizar el estado de un turbo, si esta disponible o no
     * @param id Recibe el id del turbo
     */
    public void updateTurbo(Integer id) {
        ObjectNode request = mapper.createObjectNode();
        request.put("action", "update_turbo");
        request.put("turbo_id", id);

        try {
            connection.connect(mapper.writeValueAsString(request));
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }
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
        JsonNode lives = data.get("lives");

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

            Integer id = jsonNode.get("id").asInt();
            Integer posX = jsonNode.get("posX").asInt();
            Integer posY = jsonNode.get("posY").asInt();

            hole.setId(id);
            hole.setPosition(posX.doubleValue(), posY.doubleValue());

            holeSprites.put(posY, hole);
        });

        turbos.forEach(jsonNode -> {
            Turbo turbo = new Turbo();
            Integer id = jsonNode.get("id").asInt();
            Integer posX = jsonNode.get("posX").asInt();
            Integer posY = jsonNode.get("posY").asInt();
            Integer taken = jsonNode.get("taken").asInt();

            turbo.setId(id);
            turbo.setPosition(posX.doubleValue(), posY.doubleValue());
            turbo.setTaken(taken == 1);

            turboSprites.put(posY, turbo);
        });

        lives.forEach(jsonNode -> {
            Live live = new Live();

            Integer id = jsonNode.get("id").asInt();
            Integer posX = jsonNode.get("posX").asInt();
            Integer posY = jsonNode.get("posY").asInt();
            Integer taken = jsonNode.get("taken").asInt();

            live.setId(id);
            live.setPosition(posX.doubleValue(), posY.doubleValue());
            live.setTaken(taken == 1);

            liveSprites.put(posY, live);
        });

        return trackLines;
    }

    /**
     * Método para verificar si un índice dado está dentro de algún rango en una lista
     * @param i Indice actual
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
     * @return Cantidad de vidas del jugador
     */
    public Integer getPlayerLives() {
        ObjectNode request = mapper.createObjectNode();
        request.put("action", "get_player_lives");
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
     * Método para obtener los puntos del jugador del jugador actual
     * @return Retorna un entero con los puntos del jugador
     */
    public Integer getPlayerPoints() {
        ObjectNode request = mapper.createObjectNode();
        request.put("action", "get_points");
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

        return response.get("points").asInt();
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

    /**
     * Método que coloca todos los turbos como disponibles
     */
    public void resetTurbos() {
        ObjectNode request = mapper.createObjectNode();
        request.put("action", "reset_turbos");

        try {
            connection.connect(mapper.writeValueAsString(request));
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Método para obtener los turbos del servidor
     * @return Retorna un diccionario con los turbos como valores y las llaves la posición en y
     */
    public HashMap<Integer, Turbo> updateTurboList() {
        ObjectNode request = mapper.createObjectNode();
        request.put("action", "get_turbos");

        String data = null;
        try {
            data = connection.connect(mapper.writeValueAsString(request));
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }

        if (data == null) return null;

        JsonNode response;
        try {
            response = mapper.readTree(data);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            return null;
        }

        JsonNode turbos = response.get("turbos");

//        turboSprites.clear();
        HashMap<Integer, Turbo> hashMap = new HashMap<>();


        turbos.forEach(jsonNode -> {
            Turbo turbo = new Turbo();
            Integer id = jsonNode.get("id").asInt();
            Integer posX = jsonNode.get("posX").asInt();
            Integer posY = jsonNode.get("posY").asInt();
            Integer taken = jsonNode.get("taken").asInt();

            turbo.setId(id);
            turbo.setPosition(posX.doubleValue(), posY.doubleValue());
            turbo.setTaken(taken == 1);

            hashMap.put(posY, turbo);
        });

        return  hashMap;
    }

    /**
     * Método para obtener las vidas del servidor
     * @return Retorna un diccionario con los turbos como valores y las llaves la posición en y
     */
    public HashMap<Integer, Live> updateLiveList() {
        ObjectNode request = mapper.createObjectNode();
        request.put("action", "get_lives");

        String data = null;
        try {
            data = connection.connect(mapper.writeValueAsString(request));
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }

        if (data == null) return null;

        JsonNode response;
        try {
            response = mapper.readTree(data);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            return null;
        }

        JsonNode lives = response.get("lives");

//        liveSprites.clear();
        HashMap<Integer, Live> hashMap = new HashMap<>();

        lives.forEach(jsonNode -> {
            Live live = new Live();

            Integer id = jsonNode.get("id").asInt();
            Integer posX = jsonNode.get("posX").asInt();
            Integer posY = jsonNode.get("posY").asInt();
            Integer taken = jsonNode.get("taken").asInt();

            live.setId(id);
            live.setPosition(posX.doubleValue(), posY.doubleValue());
            live.setTaken(taken == 1);

//            liveSprites.put(posY, live);
            hashMap.put(posY, live);
        });

//        return liveSprites;
        return hashMap;
    }

    /**
     * Método para colocar todas las vidas como disponibles
     */
    public void resetLives() {
        ObjectNode request = mapper.createObjectNode();
        request.put("action", "reset_lives");

        try {
            connection.connect(mapper.writeValueAsString(request));
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Método para colocar una vida como no disponible
     * @param id Identificador de la vida a modificar
     */
    public void updateLive(Integer id) {
        ObjectNode request = mapper.createObjectNode();
        request.put("action", "update_live");
        request.put("live_id", id);

        try {
            connection.connect(mapper.writeValueAsString(request));
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Método que finaliza el juego en el servidor
     */
    public void finishGame() {
        ObjectNode request = mapper.createObjectNode();
        request.put("action", "finish_game");

        try {
            connection.connect(mapper.writeValueAsString(request));
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Método que le consulta al servidor si el juego está finalizado
     * @return Retorna un booleano indicando el resultado
     */
    public Boolean isGameFinished() {
        ObjectNode request = mapper.createObjectNode();
        request.put("action", "is_game_finished");

        String data = null;
        try {
            data = connection.connect(mapper.writeValueAsString(request));
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }

        if (data == null) return null;

        JsonNode response;
        try {
            response = mapper.readTree(data);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            return null;
        }

        Integer status = response.get("game_state").asInt();

        gameFinished = (status == 1);

        return gameFinished;
    }

    /**
     * Método para obtener el booleano que indica si el juego se ha terminado
     * @return Boolenao true si el juego ya se terminó, false en caso contrario
     */
    public Boolean getGameFinished() {
        return this.gameFinished;
    }
}
