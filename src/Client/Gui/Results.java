package Client.Gui;

import Client.Logic.GameController;
import Client.Logic.Player;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

public class Results extends Application {
    private final String cwd = System.getProperty("user.dir");
    private GameController controller;
    private VBox mainLayout;
    @Override
    public void start(Stage stage) throws Exception {
        controller = GameController.getInstance();
        mainLayout = new VBox();
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setSpacing(30);

        showResults();
//
        Scene scene = new Scene(mainLayout, 550, 700);
        scene.getStylesheets().add("file:///" + cwd.replaceAll("\\\\", "/") + "/res/style.css");
        stage.setTitle("Resultados");
        stage.setScene(scene);
        stage.show();
        mainLayout.getStyleClass().add("results-background");
    }

    private void showResults() {
        Text resultsText = new Text("Resultados");
//        HashMap<Integer, Player> players = controller.getPlayerList();
//        ArrayList<Player> playerArrayList = new ArrayList<>();
//        Player first;
//        Player second;
//        for (Player player : players.values()) {
//            System.out.println("");
//            playerArrayList.add(player);
//        }
//        first = playerArrayList.get(1);
//        second = playerArrayList.get(0);
//
//        if (playerArrayList.get(0).getPoints() > playerArrayList.get(1).getPoints()) {
//            first = playerArrayList.get(0);
//            second = playerArrayList.get(1);
//        }

        HBox firstBox = new HBox();
        HBox secondBox = new HBox();

        firstBox.setAlignment(Pos.CENTER);
        secondBox.setAlignment(Pos.CENTER);

        firstBox.setSpacing(30);
        secondBox.setSpacing(30);

        ImageView firstImage = loadImageView("/res/first.png", 280, 216);
        ImageView secondImage = loadImageView("/res/second.png", 280, 216);

//        Text firstText = new Text(first.getCarSelected().getCarColor());
//        Text secondText = new Text(second.getCarSelected().getCarColor());
        Label firstText = new Label("    Rojo \n3500 Puntos");
        Label secondText = new Label("    Azul \n2000 Puntos");


        firstBox.getChildren().addAll(firstImage, firstText);
        secondBox.getChildren().addAll(secondImage, secondText);

        mainLayout.getChildren().addAll(resultsText, firstBox, secondBox);

        resultsText.getStyleClass().add("text-game");
        firstText.getStyleClass().add("text-game1");
        secondText.getStyleClass().add("text-game1");

    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * @param path Ruta de la imagen
     * @return El objeto de la imagen creada
     */
    private Image imageLoader(String path){
        try{
            FileInputStream i = new FileInputStream(path);
            return new Image(i);
        }catch (FileNotFoundException e){
            System.out.println("Couldn't load images!");
        }
        System.out.println("Could not find " + path);
        return null;
    }

    /**
     * @param path Ruta del archivo
     * @param height Altura de la imagen
     * @param width Ancho de la imagen
     * @return Un objeto ImageView de la imagen agregada
     */
    private ImageView loadImageView(String path, Integer height, Integer width){
        Image tokenImage = imageLoader(cwd.replaceAll("\\\\", "/") + path);
        ImageView addTokenImage = new javafx.scene.image.ImageView(tokenImage);
        addTokenImage.setFitHeight(height);
        addTokenImage.setFitWidth(width);

        return addTokenImage;
    }
}
