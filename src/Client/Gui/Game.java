package Client.Gui;

import Client.Logic.GameController;
import Client.Logic.Line;
import Client.Logic.Player;
import Client.Sprites.Car;
import Client.Sprites.Hole;
import Client.Sprites.Turbo;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.skins.SpaceXSkin;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

public class Game extends Application {
    private final Integer width = 1024;
    private final Integer height = 668;

    private final Integer roadWidth = 1500;
    private final Integer segmentLength = 200;
    private final Float cameraDepth = 0.84f;
    private final Integer camDefaultHeight = 1200;
    private Integer lineCount;

    private Player actualPlayer;

    private ArrayList<Line> trackLines;
    private HashMap<Integer, Hole> holes;
    private ArrayList<Player> players;
    private HashMap<Integer, Turbo> turbos;
    private ArrayList<String> input;

    private ArrayList<Hole> visibleHoles;
    private ArrayList<Turbo> visibleTurbos;

    private final Color grass = Color.rgb(68, 157, 15);
    private final Color trackBorder1 = Color.rgb(224, 224, 224);
    private final Color trackBorder2 = Color.rgb(224, 0, 1);
    private final Color track = Color.rgb(67, 81, 81);

    private GraphicsContext context;
    private Scene scene;
    private Integer laps = 0;
    private Gauge gauge;
    private Text lapsLives;
    private Text waitText;
    private Image background;
    private AnimationTimer gameLoop;

    private Image treeImage;

    private GameController controller;
    private final String cwd = System.getProperty("user.dir");


    @Override
    public void start(Stage stage) {
        controller = GameController.getInstance();
        controller.setGame(this);
        controller.setValues(segmentLength);

        loadPlayer();

        controller.addPlayer(actualPlayer);

        players = new ArrayList<>();
        holes = new HashMap<>();
        turbos = new HashMap<>();

        stage.setTitle("Pole Position CR");
        Group root = new Group();
        scene = new Scene(root);
        stage.setScene(scene);

        Canvas canvas = new Canvas(width, height);
        context = canvas.getGraphicsContext2D();
        loadSpeedometer();
        prepareActionHandlers();

        treeImage = imageLoader(cwd.replaceAll("\\\\", "/") + "/res/tree.png", 300d, 300d);

        actualPlayer.getCarSelected().setVelocity(0d, 0d);

        Text textLives = new Text("Vidas: " + actualPlayer.getLives()); // TODO cuando se cree el jugador, obtener las vidas del jugador actual
        textLives.setLayoutX(850);
        textLives.setLayoutY(50);
        textLives.getStyleClass().add("text-game");

        waitText = new Text("Esperando a otros jugadores");
        waitText.setLayoutX(400.0);
        waitText.setLayoutY(100);
        waitText.getStyleClass().add("text-game");

        lapsLives = new Text("Vueltas " + laps + "/3");
        lapsLives.setLayoutX(20);
        lapsLives.setLayoutY(50);
        lapsLives.getStyleClass().add("text-game");

        background = imageLoader(cwd.replaceAll("\\\\", "/") + "/res/mountain.png", 340d, 1024d);

        root.getChildren().addAll(canvas, gauge, textLives, lapsLives, waitText);

        scene.getStylesheets().add("file:///" + cwd.replaceAll("\\\\", "/") + "/res/style.css");

        this.trackLines = controller.getGameInfo();

        if (trackLines == null) {
            Platform.exit();
            return;
        }
        lineCount = trackLines.size();
        holes = controller.getHolesList();
        turbos = controller.getTurbosList();
        visibleHoles = new ArrayList<>();
        visibleTurbos = new ArrayList<>();

        Line.setValues(cameraDepth, width, height, roadWidth);
        configureGameLoop();
        gameLoop.start();
        stage.show();
    }

    /**
     * Método para manejar el ciclo del juego
     */
    public void configureGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long l) {
                context.clearRect(0,0,width, height);

                context.drawImage(background, 0, 0);

                //Evitar que startpos sea mayor a la cantidad de líneas.
                if (actualPlayer.getPos() >= lineCount * segmentLength) {
                    actualPlayer.manualUpdatePos(lineCount * segmentLength * -1);
                    laps += 1;
                    resetHoles();
                }

                //Evitar que startpos sea menor a cero.
                if (actualPlayer.getPos() < 0) {
                    actualPlayer.manualUpdatePos(lineCount * segmentLength);
                }

                Integer startpos = (actualPlayer.getPos() / segmentLength);

                //Esperar a otros jugadores para empezar
                if (players.size() > 0) {
                    waitText.setText("");
                    manageInput(input);
                }

                actualPlayer.updatePos();

                //Para cuestas
//                Integer camHeight = camDefaultHeight + trackLines.get(startpos).y.intValue();

                Float x = 0f, dx = 0f;
                Double maxY = height.doubleValue();

                //Se dibuja la pista, bordes y pasto
                for (Integer n = startpos; n < startpos + 300; n++) {
                    Integer currentIndex = n % lineCount;

                    Line line = trackLines.get(currentIndex);

                    //Proyectar la línea en 2d
                    Integer camZ = actualPlayer.getPos() - (n >= lineCount ? lineCount * segmentLength : 0);
                    line.project(actualPlayer.getPlayerX().intValue() - x.intValue(), camDefaultHeight, camZ);

                    //Procesar las curvas
                    x += dx;
                    dx += line.curve;

                    line.clip = maxY.floatValue();
                    trackLines.set(currentIndex, line);

                    //Evita glitches gráficos
                    if (line.Y <= 0 || line.Y >= maxY) continue;
                    maxY = line.Y.doubleValue();

                    Line prev;
                    if (n == 0) {
                        prev = line;
                    } else {
                        prev = trackLines.get((n - 1) % lineCount);
                    }

                    //Dibujar pasto.
                    drawPolygon(grass, 0, prev.Y.intValue(), width, 0, line.Y.intValue(), width);

                    //Dibujar borde de la pista.
                    Double prevTrackWidth = (prev.W * 1.3d);
                    Double trackWidth = (line.W * 1.3d);

                    Double middleLinePrev = prev.W * 0.05;
                    Double middleLine = line.W * 0.05;

                    if ((n/3) % 2 == 0) {
                        drawPolygon(trackBorder1, prev.X.intValue(), prev.Y.intValue(), prevTrackWidth.intValue(), line.X.intValue(), line.Y.intValue(), trackWidth.intValue());
                    } else {
                        drawPolygon(trackBorder2, prev.X.intValue(), prev.Y.intValue(), prevTrackWidth.intValue(), line.X.intValue(), line.Y.intValue(), trackWidth.intValue());
                    }

                    //Dibujar pista
                    drawPolygon(track, prev.X.intValue(), prev.Y.intValue(), prev.W.intValue(), line.X.intValue(), line.Y.intValue(), line.W.intValue());

                    if ((n / 3) % 2 == 0) {
                        drawPolygon(Color.WHITE, prev.X.intValue(), prev.Y.intValue(), middleLinePrev.intValue(), line.X.intValue(), line.Y.intValue(), middleLine.intValue());
                    }
                }

                //Dibujar sprites
                for (Integer n = startpos + 299; n > startpos; n--) {
                    Integer currentIndex = n % lineCount;
                    Line line  = trackLines.get(currentIndex);
                    if (line.spriteX < 0) {
                        line.drawSprite(context, treeImage);
                    }

                    Hole hole = holes.get(n);
                    Turbo turbo = turbos.get(n);

                    if (hole != null) {
                        hole = (Hole) line.drawSprite(context, hole);
                        holes.put(hole.getPosY().intValue(), hole);
                        visibleHoles.add(hole);
                    }
                    if (turbo != null) {
                        turbo = (Turbo) line.drawSprite(context, turbo);
                        turbos.put(turbo.getPosY().intValue(), turbo);
                        visibleTurbos.add(turbo);
                    }

                    for (Player p : players) {
                        Integer playerPos = p.getPos() / segmentLength;
                        //Float playerXPos = p.getPlayerX();
                        if (playerPos.intValue() == n) {
                            //TODO: arreglar posición lateral del jugador.
//                            line.spriteX = playerXPos / 500;
                            //TODO: verificar el color del carro y enviar la imagen correspondiente.
                            line.drawSprite(context, treeImage);
                        }
                    }
                }

                processCollitions();

                //Para que el carro se salga en las curvas.
                Line currentLine = trackLines.get(startpos % lineCount);
                Float curve = ((currentLine.curve * -1f) / 20f) * actualPlayer.getCarSelected().getVelocityY().floatValue();
                actualPlayer.updatePlayerX(curve);

                //Limita que tan lejos se puede desviar el jugador
                if (actualPlayer.getPlayerX() <= -3000f) {
                    actualPlayer.setPlayerX(-3000f);
                } else if (actualPlayer.getPlayerX() > 3000f) {
                    actualPlayer.setPlayerX(3000f);
                }

                // Verificar cuando el carro se sale de la pista
                if (!(actualPlayer.getPlayerX() > -1280d && actualPlayer.getPlayerX() < 1232d)) {
                    actualPlayer.updateOffroadSpeedY();
                }

                Double speed = actualPlayer.getCarSelected().getVelocityY() * 0.7d;
                lapsLives.setText("Vueltas " + laps + "/3");
                gauge.setValue(speed);

                actualPlayer.getCarSelected().render(context);

                if (actualPlayer.isCrashed()) {
                    actualPlayer.decreaseCrashTimeout();
                }

                //Se actualiza la info del jugador
                controller.updatePlayerInfo(actualPlayer);

                //Se obtiene la info de los demás jugadores
                players = controller.getPlayerList();

                visibleHoles.clear();
                visibleTurbos.clear();
            }
        };
    }

    /**
     * Método para manejar la entrada del usuario mediante el teclado
     * @param input Array de las teclas presionadas
     */
    public void manageInput(ArrayList<String> input) {
        if (input.contains("LEFT")){
            if (actualPlayer.getCarSelected().getVelocityY() > 0) {
                actualPlayer.updateSpeedX(true);
            }
        }

        if (input.contains("RIGHT")) {
            if (actualPlayer.getCarSelected().getVelocityY() > 0) {
                actualPlayer.updateSpeedX(false);
            }
        }

        actualPlayer.updateSpeedY(input.contains("UP"));

        if (input.contains("DOWN")) {
            actualPlayer.updateBrakingSpeedY();
        }

        if (input.contains("SPACE"))
            //TODO: disparar a los demás jugadores.
            System.out.println("Disparar... ");
    }

    public void resetHoles() {
        for(Hole hole: holes.values()) {
            hole.setCarCrashed(false);
            holes.put(hole.getPosY().intValue(), hole);
        }
    }

    public void processCollitions() {
        //Verificar si el jugador choca con un hueco
        for (Hole hole : visibleHoles) {
            if (!actualPlayer.isCrashed() && actualPlayer.getCarSelected().intersectsProjected(hole)) {
                if (!hole.carCrashed) {
                    actualPlayer.crashed();
                    hole.setCarCrashed(true);
                    holes.put(hole.getPosY().intValue(), hole);
                }
            }
        }

        //Verificar si el jugador toma un turbo
        for (Turbo turbo : visibleTurbos) {
            if (actualPlayer.getCarSelected().intersectsProjected(turbo)) {
                System.out.println("Aceleró!");
            }
        }
    }

    /**
     * @param path Ruta de la imagen
     * @return El objeto de la imagen creada
     */
    private Image imageLoader(String path, Double height, Double width){
        try{
            FileInputStream i = new FileInputStream(path);
            return new Image(i, width, height, false, false);
        }catch (FileNotFoundException e){
            System.out.println("Couldn't load images!");
        }
        System.out.println("Could not find " + path);
        return null;
    }

    /**
     * Método para cargar y configurar el velocimetro
     */
    private void loadSpeedometer() {
        gauge = new Gauge();
        gauge.setSkin(new SpaceXSkin(gauge));
//        gauge.setSkin(new TileTextKpiSkin(gauge));
        gauge.setUnit("km / h");
        gauge.setUnitColor(Color.BLACK);
        gauge.setDecimals(0);
        gauge.setValue(0d); //deafult position of needle on gauage
        gauge.setAnimated(true);
        gauge.setThresholdColor(Color.RED);  //color will become red if it crosses thereshold value
        gauge.setThreshold(168);
        gauge.setMinValue(0d);
        gauge.setMaxValue(200d);

        gauge.setLayoutX(800);
        gauge.setLayoutY(400);
        gauge.setPrefSize(200, 200);
    }

    /**
     * Método para colocar en el array de inputs del usuario, las teclas que se presionen
     */
    private void prepareActionHandlers() {
        input = new ArrayList<>();
        scene.setOnKeyPressed(keyEvent -> {
            String code = keyEvent.getCode().toString();
            if (!input.contains(code))
                input.add(code);
        });
        scene.setOnKeyReleased(keyEvent -> input.remove(keyEvent.getCode().toString()));
    }

    /**
     * Método para cargar la información del jugador actual
     */
    private void loadPlayer() {
        String colorCar = controller.getActualColorCar();
        String path = "";
        Car carSprite = new Car(colorCar);
        switch (colorCar) {
            case "Rojo" -> path = "/res/CarroRojo.png";
            case "Morado" -> path =  "/res/CarroMorado.png";
            case "Blanco" -> path =  "/res/CarroBlanco.png";
            case "Azul" -> path =  "/res/CarroAzul.png";
        }

        carSprite.setImage(path, 110, 200);

        Integer center = (width / 2) - (carSprite.getWidth() / 2);
        carSprite.setPosition(center.doubleValue(), 500.0);

        carSprite.setProjectedPosX(center.doubleValue());
        carSprite.setProjectedPosY(500d);
        carSprite.setProjectedWidth(200d);
        carSprite.setProjectedHeight(110d);

        actualPlayer = new Player(carSprite);
    }

    /**
     * Función principal para dibujar un poligono en el canvas, utilizando las coordenadas de la línea actual y anterior.
     * @param color Color con el que dibujar el poligono.
     * @param x1 Posición x de la línea anterior
     * @param y1 Posición y de la línea anterior
     * @param w1 Valor w de la línea anterior
     * @param x2 Posición x de de línea actual
     * @param y2 Posición y de la línea actual
     * @param w2 Valor w de la línea actual
     */
    private void drawPolygon(Color color, Integer x1, Integer y1, Integer w1, Integer x2, Integer y2, Integer w2) {
        double[] pointsX = {
                x1 - w1,
                x2 - w2,
                x2 + w2,
                x1 + w1
        };
        double[] pointsY = {
                y1,
                y2,
                y2,
                y1
        };
        context.setFill(color);
        context.fillPolygon(pointsX, pointsY, 4);
    }

    /**
     * Método para mostrar la ventana de juego
     */
    public static void show() {
        new Game().start(new Stage());
    }
}
