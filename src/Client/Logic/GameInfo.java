package Client.Logic;

/**
 * Clase que tiene la información del juego
 */
public class GameInfo {
    private Double time;

    /**
     * Constructor de la clase GameInfo
     */
    public GameInfo() {
        this.time = 0.0;
    }

    /**
     * Método para obtener el tiempo del juego
     * @return tiempo actual del juego
     */
    public Double getTime() {
        return time;
    }

    /**
     * Método para actualizar el tiempo del juego
     * @param time tiempo a actualizar
     */
    public void updateTime(Double time) {
        this.time += time;
    }
}
