package Client.Sprites;

/**
 * Clase del sprite Vida que hereda de Sprite
 */
public class Live extends Sprite {

    public Integer id;
    public Boolean taken;

    /**
     * Constructor de la clase Live
     */
    public Live() { }

    /**
     * Método para conocer si la vida la han agarradp
     * @return Booleano true si ya han tomado la vida, false en caso contrario
     */
    public Boolean isTaken() {
        return taken;
    }

    /**
     * Método para colocar una vida como tomada
     * @param taken Booleano que indica el nuevo estado
     */
    public void setTaken(Boolean taken) {
        this.taken = taken;
    }

    /**
     * Método para obtener el identificador de la vida
     * @return Retorna el id de la vida
     */
    public Integer getId() {
        return id;
    }

    /**
     * Método para colocar el id de la vida
     * @param id El identificador de la vida
     */
    public void setId(Integer id) {
        this.id = id;
    }
}
