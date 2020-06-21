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

    /**
     * Método para desactivar el hueco para el jugador actual, hasta la siguiente vuelta
     * @param carCrashed Booleano, true si se ha chocado, false en caso contrario
     */
    public void setCarCrashed(Boolean carCrashed) {
        this.carCrashed = carCrashed;
    }

    /**
     * Método para obtener el id del hueco
     * @return Retorna un entero con el identificador del juego
     */
    public Integer getId() {
        return id;
    }

    /**
     * Método para colocar el identificador del hueco
     * @param id Recibe el identificador
     */
    public void setId(Integer id) {
        this.id = id;
    }
}
