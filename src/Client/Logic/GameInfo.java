package Client.Logic;

public class GameInfo {
    private Double time;

    public GameInfo() {
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
