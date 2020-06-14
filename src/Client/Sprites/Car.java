package Client.Sprites;

import javafx.scene.canvas.GraphicsContext;

import java.util.Random;

public class Car extends Sprite{
    private String carColor;

    public Car (String carColor) {
        this.carColor = carColor;
    }


    public String getCarColor() {
        return this.carColor;
    }

    public void setCarColor(String carColor) {
        this.carColor = carColor;
    }

    @Override
    public void render(GraphicsContext gc) {
        Random rand = new Random();
        //Double offsetX = (rand.nextDouble() * getVelocityX()) / 60f;
        Double offsetX = 0d;
        Double offsetY = (rand.nextDouble() * getVelocityY()) / 40f;

        gc.drawImage(getImage(), getPosX()  + offsetX, getPosY() + offsetY);
    }

}
