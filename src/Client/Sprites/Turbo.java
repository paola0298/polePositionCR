package Client.Sprites;

/**
 * Clase del turbo
 */
public class Turbo extends Sprite{

    public Integer id;
    public Boolean turboGot;

    /**
     * Constructor de la clase Turbo
     */
    public Turbo () { }

    public Boolean getTurboGot() {
        return turboGot;
    }

    public void setTurboGot(Boolean turboGot) {
        this.turboGot = turboGot;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
