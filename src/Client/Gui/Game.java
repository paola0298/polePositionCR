package Client.Gui;

import Client.Logic.Connection;
import Client.Logic.GameController;
import Client.Logic.Line;
import Client.Sprites.Car;
import Client.Sprites.Sprite;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.skins.*;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Skin;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class Game extends Application {
    private Integer width = 1024;
    private Integer height = 668;

    private Integer roadWidth = 1500;
    private Integer segmentLength = 200;
    private Float cameraDepth = 0.84f;

    Integer camDefaultHeight = 1200;

    Integer pos = 0;
    Float playerX = 0f;

    private Integer lineCount;
    private ArrayList<Line> trackLines;
    //TODO: recuperar obstaculos del servidor.
    private ArrayList<Sprite> obstacles;
    //TODO: recuperar jugadores del servidor.
    private ArrayList<Sprite> players;
    //TODO: recuperar power-ups del servidor.
    private ArrayList<Sprite> powerUps;

    private final Color grass = Color.rgb(68, 157, 15);
    private final Color trackBorder1 = Color.rgb(224, 224, 224);
    private final Color trackBorder2 = Color.rgb(224, 0, 1);
    private final Color track = Color.rgb(67, 81, 81);

    private GraphicsContext context;

    private ArrayList<String> input;

    private Scene scene;
    private Car carSprite;
    private Integer laps = 0;
    private Gauge gauge;
    private Text lapsLives;
    private Image background;
    private AnimationTimer gameLoop;

    private GameController controller;
    private final String cwd = System.getProperty("user.dir");

    @Override
    public void start(Stage stage) {
        controller = GameController.getInstance();
        controller.setGame(this);
        controller.setValues(segmentLength);

        //Hacer en la petición al servidor
        obstacles = new ArrayList<>();
        players = new ArrayList<>();
        powerUps = new ArrayList<>();

        stage.setTitle("Pole Position CR");
        Group root = new Group();
        scene = new Scene(root);
        stage.setScene(scene);

        Canvas canvas = new Canvas(width, height);
        context = canvas.getGraphicsContext2D();
        loadSpeedometer();
        prepareActionHandlers();
        loadSprite();
        carSprite.setVelocity(0.0, 0.0);

        Text textLives = new Text("Vidas: 3"); // TODO cuando se cree el jugador, obtener las vidas del jugador actual
        textLives.setLayoutX(850);
        textLives.setLayoutY(50);
        textLives.getStyleClass().add("text-game");

        lapsLives = new Text("Vueltas " + laps + "/3");
        lapsLives.setLayoutX(20);
        lapsLives.setLayoutY(50);
        lapsLives.getStyleClass().add("text-game");

        background = imageLoader(cwd.replaceAll("\\\\", "/") + "/res/mountain.png", 340d, 1024d);

        root.getChildren().addAll(canvas, gauge, textLives, lapsLives);

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

                //Evitar que startpos sea mayor a la cantidad de líneas.
                if (pos >= lineCount * segmentLength) {
                    pos -= lineCount * segmentLength;
                    laps += 1;
                }

                //Evitar que startpos sea menor a cero.
                if (pos < 0) {
                    pos += lineCount * segmentLength;
                }

                Integer startpos = (pos / segmentLength);

                manageInput(input);

                Float x = 0f, dx = 0f;
                Double maxY = height.doubleValue();

                //Para cuestas
//                Integer camHeight = camDefaultHeight + trackLines.get(startpos).y.intValue();

                pos += carSprite.getVelocityY().intValue();

                //Se dibuja la pista, bordes y pasto
                for (Integer n = startpos; n < startpos + 300; n++) {
                    Line line = trackLines.get(n % lineCount);

                    //Proyectar la línea en 2d
                    Integer camZ = pos - (n >= lineCount ? lineCount * segmentLength : 0);
                    line.project(playerX.intValue() - x.intValue(), camDefaultHeight, camZ);

                    //Procesar las curvas
                    x += dx;
                    dx += line.curve;

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
                for (Integer i = 0; i < players.size(); i++) {

                }
                for (Integer i = 0; i < obstacles.size(); i++) {

                }
                for (Integer i = 0; i < powerUps.size(); i++) {

                }

                //Para que el carro se salga en las curvas.
                Line currentLine = trackLines.get(startpos % lineCount);
                Float curve = ((currentLine.curve * -1f) / 20f) * carSprite.getVelocityY().floatValue();
                playerX += curve;

                //Limita que tan lejos se puede desviar el jugador
                if (playerX <= -3000) {
                    playerX =  -3000f;
                } else if (playerX > 3000) {
                    playerX = 3000f;
                }

                // Verificar cuando el carro se sale de la pista
                if (!(playerX > -1280d && playerX < 1232d) && (carSprite.getVelocityY() > 70d)) {
                        carSprite.increaseVelocity(0d, -0.9d);
                }

                Double speed = carSprite.getVelocityY() * 0.7d;
                lapsLives.setText("Vueltas " + laps + "/3");
                gauge.setValue(speed);
                carSprite.render(context);
                context.drawImage(background, 0, 0);

                //TODO: mandar estado del jugador al servidor para actualizarlo
            }
        };
    }

    public void manageInput(ArrayList<String> input) {
        if (input.contains("LEFT")){
            if (carSprite.getVelocityY() > 0) {
                carSprite.setVelocity(-40d, carSprite.getVelocityY());
                playerX += carSprite.getVelocityX().intValue();

            }
        }

        if (input.contains("RIGHT")) {
            if (carSprite.getVelocityY() > 0) {
                carSprite.setVelocity(40d, carSprite.getVelocityY());
                playerX += carSprite.getVelocityX().intValue();
            }
        }

        if (input.contains("UP")) {
            carSprite.increaseVelocity(0d, 0.4d);
            if (carSprite.getVelocityY() >= 240d) {
                carSprite.setVelocity(carSprite.getVelocityX(), 240d);
            }
        } else {
            carSprite.increaseVelocity(0d, -0.6d);
            if (carSprite.getVelocityY() <= 0) {
                carSprite.setVelocity(carSprite.getVelocityX(), 0d);
            }
        }

        if (input.contains("DOWN")) {
            carSprite.increaseVelocity(0d, -1.1d);
            if (carSprite.getVelocityY() <= 0) {
                carSprite.setVelocity(carSprite.getVelocityX(), 0d);
            }
        }

        if (input.contains("SPACE"))
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

    private void loadSprite() { //TODO cargar al jugador
        carSprite = new Car("Rojo");
        carSprite.setImage("/res/car.png", 100, 100);
        carSprite.setPosition(400.0, 500.0);
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

    public static void main(String[] args) {
        launch(Game.class);
    }
}
