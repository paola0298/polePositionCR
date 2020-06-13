package Client.Gui;

import Client.Logic.Connection;
import Client.Logic.Line;
import Client.Sprites.Car;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
    Integer playerX = 0;

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
    private Long lastNanoTime;


    @Override
    public void start(Stage stage) {
        mapper = new ObjectMapper();
        connection = new Connection("localhost", 8080);

        stage.setTitle("Pole Position CR");

        Group root = new Group();
        scene = new Scene(root);
        stage.setScene(scene);

        Canvas canvas = new Canvas(width, height);
        root.getChildren().add(canvas);

        prepareActionHandlers();

        context = canvas.getGraphicsContext2D();

        loadSprite();

        lastNanoTime = System.nanoTime();

        //canvas.setOnKeyPressed((this::handleKeyEvent));

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                context.clearRect(0,0,width, height);

                //Evitar que startpos sea mayor a la cantidad de líneas.
                if (pos >= lineCount * segmentLength) { //Vuelta completada
                    pos -= lineCount * segmentLength;
                    //TODO: vuelta completada.
                }

                //Evitar que startpos sea menor a cero.
                if (pos < 0) {
                    pos += lineCount * segmentLength;
                }

                Integer startpos = (pos / segmentLength);

                //Quitar luego, esto va a ser manejado por el input del usuario.
                //pos += 175;
                manageInput(l);

                Float x = 0f, dx = 0f;
                Double maxY = height.doubleValue();

                //Para cuestas
//                Integer camHeight = camDefaultHeight + trackLines.get(startpos).y.intValue();

                for (Integer n = startpos; n < startpos + 300; n++) {
                    Line line = trackLines.get(n % lineCount);

                    //Proyectar la línea en 2d
                    Integer camZ = pos - (n >= lineCount ? lineCount * segmentLength : 0);
                    line.project(playerX - x.intValue(), camDefaultHeight, camZ);

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
                carSprite.render(context);
            }
        };

        getTrack();
        stage.show();
        timer.start();
    }

    private void manageInput(long currentNanoTime) {
        Double elapsedTime = (currentNanoTime - lastNanoTime) / 1000000000.0;
        lastNanoTime = currentNanoTime;

        //game logic

        carSprite.setVelocity(200.0, 0.0);
        if (input.contains("LEFT"))
            playerX -= carSprite.getVelocityX().intValue();
//            carSprite.increaseVelocity(-50.0, 0.0);
        if (input.contains("RIGHT"))
            playerX += carSprite.getVelocityX().intValue();
//            carSprite.increaseVelocity(50.0, 0.0);
        if (input.contains("UP"))
            pos += carSprite.getVelocityX().intValue();
//            carSprite.increaseVelocity(0.0, -50.0);
        if (input.contains("DOWN"))
            pos -= carSprite.getVelocityX().intValue();
//            carSprite.increaseVelocity(0.0, 50.0);
        if (input.contains("SPACE"))
            System.out.println("Disparar... ");

        //carSprite.update(elapsedTime);
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

    private void handleKeyEvent(KeyEvent event) {
        String type = event.getEventType().getName();
        KeyCode keyCode = event.getCode();

        System.out.println(type + ": KeyCode=" + keyCode.getName() + ", Text=" + event.getText());

        if (event.getEventType() == KeyEvent.KEY_PRESSED) {
            switch (event.getCode()) {
                case W -> pos += 200;
                case S -> pos -= 200;
            }
        }
    }

    private void getTrack() {
        ObjectNode request = mapper.createObjectNode();
        request.put("action", "get_track");

        String data;
        try {
            data = connection.connect(mapper.writeValueAsString(request));
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            return;
        }

        JsonNode response;
        try {
            response = mapper.readTree(data);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            return;
        }

        parseTrack(response.get("track"));
    }

    private void parseTrack(JsonNode track) {
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
