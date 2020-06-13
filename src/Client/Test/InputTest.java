package Client.Test;

import Client.Sprites.Car;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.animation.AnimationTimer;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class InputTest extends Application {

    private GraphicsContext graphicsContext;
    private ArrayList<String> input;
    private Scene mainScene;
    private Image car;
    private Car carSprite;
    private Long lastNanoTime;

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Input Test");

        Group root = new Group();
        mainScene = new Scene(root);
        stage.setScene(mainScene);

        Canvas canvas = new Canvas(750, 450);
        root.getChildren().add(canvas);

        prepareActionHandlers();

        graphicsContext = canvas.getGraphicsContext2D();

        loadSprite();

        lastNanoTime = System.nanoTime();

        /**
         * Game Loop
         */
        new AnimationTimer() {

            @Override
            public void handle(long l) {
                tickAndRender(l);
            }
        }.start();

        stage.show();
    }

    private void prepareActionHandlers() {
        input = new ArrayList<>();
        mainScene.setOnKeyPressed(keyEvent -> {
            String code = keyEvent.getCode().toString();
            if (!input.contains(code))
                input.add(code);
        });
        mainScene.setOnKeyReleased(keyEvent -> input.remove(keyEvent.getCode().toString()));
    }

    private void loadSprite() {
        car = new Image(new File("res/car.png").toURI().toString());
        carSprite = new Car("Rojo");
        carSprite.setImage("/res/car.png", 100, 100);
        carSprite.setPosition(300.0, 400.0);
    }

    private void tickAndRender(long currentNanoTime) {
        Double elapsedTime = (currentNanoTime - lastNanoTime) / 1000000000.0;
        lastNanoTime = currentNanoTime;

        //game logic

        carSprite.setVelocity(0.0, 0.0);
        if (input.contains("LEFT"))
            carSprite.increaseVelocity(-50.0, 0.0);
        if (input.contains("RIGHT"))
            carSprite.increaseVelocity(50.0, 0.0);
        if (input.contains("UP"))
            carSprite.increaseVelocity(0.0, -50.0);
        if (input.contains("DOWN"))
            carSprite.increaseVelocity(0.0, 50.0);
        if (input.contains("SPACE"))
            System.out.println("Disparar... ");

        carSprite.update(elapsedTime);

        // TODO detectar colisiones

        // render
        graphicsContext.clearRect(0, 0, 750, 450);
        carSprite.render(graphicsContext);

    }

    public static void main(String[] args) {
        launch(args);
    }
}
