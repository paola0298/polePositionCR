package Client.Sprites;

/**
 * Clase del turbo
 */
public class Turbo extends Sprite{

    public Integer id;
    public Boolean taken;

    /**
     * Constructor de la clase Turbo
     */
    public Turbo () { }

    public Boolean isTaken() {
        return taken;
    }

    public void setTaken(Boolean taken) {
        this.taken = taken;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
