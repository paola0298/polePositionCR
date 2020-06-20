package Client.Sprites;

/**
 * Clase del hueco
 */
public class Hole extends Sprite{

    public Integer id;
    public Boolean carCrashed;

    /**
     * Constructor de la clase del hueco
     */
    public Hole() {
        carCrashed = false;
    }

    public void setCarCrashed(Boolean carCrashed) {
        this.carCrashed = carCrashed;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
