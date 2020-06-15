package Client.Gui;

import Client.Logic.Connection;
import Client.Logic.Line;
import Client.Sprites.Car;
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
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;

public class Game extends Application {
    private final Integer width = 1024;
    private final Integer height = 668;

    Integer roadWidth = 1000;
    Integer segmentLength = 200;
    Integer camDefaultHeight = 1200;
    Float cameraDepth = 0.84f;

    Integer pos = 0;
    Float playerX = 0f;

    private ArrayList<Line> trackLines;
    private Integer lineCount;

    private final Color grass = Color.rgb(68, 157, 15);
    private final Color trackBorder1 = Color.rgb(224, 224, 224);
    private final Color trackBorder2 = Color.rgb(224, 0, 1);
    private final Color track = Color.rgb(67, 81, 81);

    private GraphicsContext context;
    private ObjectMapper mapper;
    private Connection connection;

    private ArrayList<String> input;
    private Scene scene;
    private Car carSprite;
    private Integer laps;
    private Gauge gauge;

    @Override
    public void start(Stage stage) {
        mapper = new ObjectMapper();
        connection = new Connection("localhost", 8080);

        stage.setTitle("Pole Position CR");

        Group root = new Group();
        scene = new Scene(root);
        stage.setScene(scene);

        Canvas canvas = new Canvas(width, height);
        
        loadSpeedometer();
        
        root.getChildren().addAll(canvas, gauge);

        prepareActionHandlers();

        context = canvas.getGraphicsContext2D();

        loadSprite();

        carSprite.setVelocity(0.0, 0.0);

        laps = 0;

        new AnimationTimer() {
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

                manageInput();

                Float x = 0f, dx = 0f;
                Double maxY = height.doubleValue();

                //Para cuestas
//                Integer camHeight = camDefaultHeight + trackLines.get(startpos).y.intValue();

                pos += carSprite.getVelocityY().intValue();

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

                    if ((n/3) % 2 == 0) {
                        drawPolygon(trackBorder1, prev.X.intValue(), prev.Y.intValue(), prevTrackWidth.intValue(), line.X.intValue(), line.Y.intValue(), trackWidth.intValue());
                    } else {
                        drawPolygon(trackBorder2, prev.X.intValue(), prev.Y.intValue(), prevTrackWidth.intValue(), line.X.intValue(), line.Y.intValue(), trackWidth.intValue());
                    }

                    //Dibujar pista
                    drawPolygon(track, prev.X.intValue(), prev.Y.intValue(), prev.W.intValue(), line.X.intValue(), line.Y.intValue(), line.W.intValue());

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

                context.setFill(Color.BLACK);

                Double speed = carSprite.getVelocityY() * 0.7d;
                context.fillText(String.format("SPEED: %.1f KPH", speed), 100d, 100d);
                context.fillText("LAPS: " + laps, 100d, 120d);
                gauge.setValue(speed);

                carSprite.render(context);
            }
        }.start();

        if (!getTrack())  {
            System.err.println("[Error] Failed to connect to server.");
            Platform.exit();
            return;
        }

        stage.show();
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

    private void manageInput() {

        //game logic
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

    private void prepareActionHandlers() {
        input = new ArrayList<>();
        scene.setOnKeyPressed(keyEvent -> {
            String code = keyEvent.getCode().toString();
            if (!input.contains(code))
                input.add(code);
        });
        scene.setOnKeyReleased(keyEvent -> input.remove(keyEvent.getCode().toString()));
    }

    private void loadSprite() {
        carSprite = new Car("Rojo");
        carSprite.setImage("/res/car.png", 100, 100);
        carSprite.setPosition(400.0, 500.0);
    }

    private boolean getTrack() {
        ObjectNode request = mapper.createObjectNode();
        request.put("action", "get_track");

        String data;
        try {
            data = connection.connect(mapper.writeValueAsString(request));
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            return false;
        }

        if (data == null) {
            return false;
        }

        JsonNode response;
        try {
            response = mapper.readTree(data);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            return false;
        }

        return parseTrack(response.get("track"));
    }

    private boolean parseTrack(JsonNode track) {
        Line.setValues(cameraDepth, width, height, roadWidth);

        trackLines = new ArrayList<>();


        Integer length = track.get("length").asInt();
        System.out.println(track.toPrettyString());

        JsonNode curves = track.get("curves");

        for (Integer i = 0; i < length; i++) {
            Line line = new Line();
            line.z = i * segmentLength.floatValue();

            line.curve = checkInRange(i, curves);

            //Usar para cuestas
//            if (i > 700) {
//                Double value = Math.sin(i / 30.0);
//                line.y = value.floatValue() * camDefaultHeight;
//            }

            trackLines.add(line);
        }

        lineCount = trackLines.size();
        return true;
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
        launch();
    }
}
