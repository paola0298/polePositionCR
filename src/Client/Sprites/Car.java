package Client.Sprites;

import javafx.scene.canvas.GraphicsContext;
import java.util.Random;

/**
 * Clase Car que hereda de Sprite
 */
public class Car extends Sprite{
    private String carColor;

    /**
     * Constructor de la clase Car
     * @param carColor
     */
    public Car (String carColor) {
        this.carColor = carColor;
    }

    /**
     * Método para obtener el color del carro
     * @return Retorna un string indicando el color
     */
    public String getCarColor() {
        return this.carColor;
    }

    /**
     * Método para colocar el color del carro
     * @param carColor Color del carro
     */
    public void setCarColor(String carColor) {
        this.carColor = carColor;
    }

    /**
     * Método para dibujar el carro en la interfaz
     * @param gc contexto grafico donde dibujar
     */
    @Override
    public void render(GraphicsContext gc) {
        Random rand = new Random();
        //Double offsetX = (rand.nextDouble() * getVelocityX()) / 60f;
        Double offsetX = 0d;
        Double offsetY = (rand.nextDouble() * getVelocityY()) / 40f;
        gc.drawImage(getImage(), getPosX()  + offsetX, getPosY() + offsetY);
    }

}
