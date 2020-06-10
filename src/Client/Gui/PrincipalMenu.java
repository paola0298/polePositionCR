package Client.Gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class PrincipalMenu extends Application {

    private final String cwd = System.getProperty("user.dir");
    private VBox menuWindow; // Ventana del menu principal
    private VBox carMenuWindow; // Venta donde se elige el auto

    @Override
    public void start(Stage stage) throws Exception {
        StackPane mainLayout = new StackPane();

        init_Menu();
        initCarSelection();

        mainLayout.getChildren().addAll(menuWindow, carMenuWindow);
        Scene scene = new Scene(mainLayout, 750, 450);
        scene.getStylesheets().add("file:///" + cwd.replaceAll("\\\\", "/") + "/res/style.css");
        stage.setTitle("Pole Position CR");
        stage.setScene(scene);
        stage.getIcons().add(imageLoader(cwd.replaceAll("\\\\", "/") + "/res/icon-2.png"));
        menuWindow.toFront();
        stage.show();
    }

    /**
     * Carga el menu de inicio
     */
    private void init_Menu() {
        menuWindow = new VBox();
        menuWindow.setSpacing(170);
        menuWindow.setAlignment(Pos.CENTER);
        Text title = new Text("Pole Position CR");
        VBox.setMargin(title, new Insets(0,0,50,0));
        ImageView startButton = loadImageView("/res/startButton.png", 80, 80);
        startButton.setOnMouseClicked(mouseEvent -> {
            System.out.println("Iniciar juego...");
            carMenuWindow.toFront();

        });
        DropShadow shadow = new DropShadow();
        startButton.setOnMouseEntered(mouseEvent -> startButton.setEffect(shadow));
        startButton.setOnMouseExited(mouseEvent -> startButton.setEffect(null));
        menuWindow.getChildren().addAll(title, startButton);

        title.getStyleClass().add("fancytext");
        menuWindow.getStyleClass().add("background-pane");

    }

    /**
     * Carga el menu de seleccion de los carros
     */

    private void initCarSelection() {
        carMenuWindow = new VBox();
        HBox carArray = new HBox();

        carMenuWindow.setSpacing(50);
        carMenuWindow.setAlignment(Pos.CENTER);

        carArray.setSpacing(40);
        carArray.setAlignment(Pos.BOTTOM_CENTER);

        // TODO obtener la lista de colores disponibles para colocarlos en la ventana
        // TODO actualizar los carros segun la se vayan escogiendo

        Text title = new Text("Seleccione un carro");

        ImageView redCar = loadImageView("/res/redCar.png", 150, 150);
        ImageView blueCar = loadImageView("/res/blueCar.png", 150, 150);
        ImageView purpleCar = loadImageView("/res/purpleCar.png", 150, 150);
        ImageView whiteCar = loadImageView("/res/whiteCar.png", 150, 150);
        ImageView[] carArrayS = new ImageView[4];
        carArrayS[0] = blueCar;
        carArrayS[1] = purpleCar;
        carArrayS[2] = redCar;
        carArrayS[3] = whiteCar;
        // TODO recibir array del sprite car e iterarlo para conocer el color del carro

        for (int i = 0; i < 4; i++) {
            System.out.println("Agregando carro...");
            ImageView tmp = carArrayS[i];
            DropShadow shadow = new DropShadow();

            tmp.setOnMouseEntered(mouseEvent -> tmp.setEffect(shadow));
            tmp.setOnMouseExited(mouseEvent -> tmp.setEffect(null));
            tmp.setOnMouseClicked(mouseEvent -> {
                System.out.println("Carro seleccionado");
                // TODO llamar a la ventana principal del juego
            });
            carArray.getChildren().add(tmp);
        }
        title.getStyleClass().add("fancytext1");
        carMenuWindow.getChildren().addAll(title, carArray);
        carMenuWindow.getStyleClass().add("background-car-pane");

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
        ImageView addTokenImage = new ImageView(tokenImage);
        addTokenImage.setFitHeight(height);
        addTokenImage.setFitWidth(width);

        return addTokenImage;
    }


    public static void main(String[] args) {
        launch(args);
    }
}
