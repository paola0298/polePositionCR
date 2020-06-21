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

    /**
     * Métod para conocer si se ha tomado un turbo
     * @return True si el turbo se ha tomado, False en caso contrario
     */
    public Boolean isTaken() {
        return taken;
    }

    /**
     * Método para actualizar el estado de un turbi
     * @param taken True indica que esta tomado, False en caso contrario
     */
    public void setTaken(Boolean taken) {
        this.taken = taken;
    }

    /**
     * Método para obtener el identificador del turbo
     * @return el id del turbo
     */
    public Integer getId() {
        return id;
    }

    /**
     * Método para colocar el id del turbo
     * @param id Identificador del turbo
     */
    public void setId(Integer id) {
        this.id = id;
    }
}
