package Client.Gui;

import Client.Logic.GameController;
import Client.Logic.Line;
import Client.Logic.Player;
import Client.Sprites.Car;
import Client.Sprites.Sprite;
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

public class Game extends Application {
    private final Integer width = 1024;
    private final Integer height = 668;

    private final Integer roadWidth = 1500;
    private final Integer segmentLength = 200;
    private final Float cameraDepth = 0.84f;
    private final Integer camDefaultHeight = 1200;
    private Integer lineCount;

    //Para evitar sobrecargar el servidor.
    private final Integer defaultSendDelay = 8;
    private Integer sendDelay = defaultSendDelay;

    private Player actualPlayer;

    private ArrayList<Line> trackLines;
    //TODO: recuperar obstaculos del servidor.
    private ArrayList<Sprite> obstacles;
    private ArrayList<Player> players;
    private ArrayList<Player> otherPlayers;
    //TODO: recuperar power-ups del servidor.
    private ArrayList<Sprite> powerUps;
    private ArrayList<String> input;

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

        //Hacer en la petición al servidor
        obstacles = new ArrayList<>();
        players = new ArrayList<>();
        otherPlayers = new ArrayList<>();
        powerUps = new ArrayList<>();

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

        waitText  = new Text("Esperando a otros jugadores");
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

        this.trackLines = controller.getTrack();

        if (trackLines == null) {
            Platform.exit();
            return;
        }
        lineCount = trackLines.size();

        Line.setValues(cameraDepth, width, height, roadWidth);
        configureGameLoop();
        gameLoop.start();
        stage.show();
    }

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
                }

                //Evitar que startpos sea menor a cero.
                if (actualPlayer.getPos() < 0) {
                    actualPlayer.manualUpdatePos(lineCount * segmentLength);
                }

                Integer startpos = (actualPlayer.getPos() / segmentLength);

                //Esperar a otros jugadores para empezar
                //if (otherPlayers.size() > 0) {
                //    waitText.setText("");
                    manageInput(input);
                //}

                Float x = 0f, dx = 0f;
                Double maxY = height.doubleValue();

                //Para cuestas
//                Integer camHeight = camDefaultHeight + trackLines.get(startpos).y.intValue();

                actualPlayer.updatePos();

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

                    //TEST
                    line.clip = maxY.floatValue();

                    //TEST
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

                //TODO: renderizar los demás sprites
//                for (Integer i = 0; i < otherPlayers.size(); i++) {
//                    Player actual = otherPlayers.get(i);
//                    //System.out.println("Color del carro a ingresar" + actual.getCarSelected().getCarColor());
//                    Double posX = 0d;
//                    Double posY = 0d;
//                    switch (i) {
//                        case 0 -> {
//                            posX = 200d;
//                            posY = 500d;
//                        }
//                        case 1 -> {
//                            posX = 600d;
//                            posY = 300d;
//                        }
//                        case 2 -> {
//                            posX = 250d;
//                            posY = 300d;
//                        }
//                    }
//
//                    String path = "";
//                    switch (actual.getCarSelected().getCarColor()) {
//                        case "Rojo" -> path = "/res/CarroRojo.png";
//                        case "Morado" -> path =  "/res/CarroMorado.png";
//                        case "Blanco" -> path =  "/res/CarroBlanco.png";
//                        case "Azul" -> path =  "/res/CarroAzul.png";
//                    }
//                    actual.getCarSelected().setPosition(posX, posY);
//                    actual.getCarSelected().setImage(path, 100, 100);
//                    actual.getCarSelected().render(context);
//                }
//
//                for (Integer i = 0; i < obstacles.size(); i++) {
//
//                }
//                for (Integer i = 0; i < powerUps.size(); i++) {
//
//                }

                //Dibujar árboles
                for (Integer n = startpos + 299; n > startpos; n--) {
                    Integer currentIndex = n % lineCount;
                    Line line  = trackLines.get(currentIndex);
                    if (line.spriteX < 0) {
                        line.drawSprite(context, treeImage);
                    }

                    for (Player p : players) {
                        Integer playerPos = p.getPos() / segmentLength;
                        Float playerXPos = p.getPlayerX();

                        if (playerPos.intValue() == n) {
                            //TODO: arreglar posición lateral del jugador.
//                            line.spriteX = playerXPos / 500;

                            //TODO: verificar el color del carro y enviar la imagen correspondiente.
                            line.drawSprite(context, treeImage);
                        }
                    }
                }

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


                if (sendDelay == 0) {
                    sendDelay = defaultSendDelay;

                    //Se actualiza la info del jugador
                    controller.updatePlayerInfo(actualPlayer);

                    //Se obtiene la info de los demás jugadores
                    players = controller.getPlayerList();
                } else {
                    //System.out.println("Delay: " + sendDelay);
                    sendDelay--;
                }
            }
        };
    }

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

    private void loadSpeedometer() {
        gauge = new Gauge();
        gauge.setSkin(new SpaceXSkin(gauge));
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

    private void prepareActionHandlers() {
        input = new ArrayList<>();
        scene.setOnKeyPressed(keyEvent -> {
            String code = keyEvent.getCode().toString();
            if (!input.contains(code))
                input.add(code);
        });
        scene.setOnKeyReleased(keyEvent -> input.remove(keyEvent.getCode().toString()));
    }

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
        carSprite.setImage(path, 100, 100);
        carSprite.setPosition(400.0, 500.0);
        actualPlayer = new Player(carSprite);
    }

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

    public static void show() {
        new Game().start(new Stage());
    }
}
