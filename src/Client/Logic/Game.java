package Client.Logic;

public class Game {
    private Double time;

    public Game() {
        this.time = 0.0;
    }

    /**
     *
     * @return tiempo actual del juego
     */
    public Double getTime() {
        return time;
    }

    /**
     *
     * @param time tiempo a actualizar
     */
    public void updateTime(Double time) {
        this.time += time;
    }
}
