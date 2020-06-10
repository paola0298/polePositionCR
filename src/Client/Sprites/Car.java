package Client.Sprites;

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
}
