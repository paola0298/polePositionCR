package Client.Logic;

import Client.Sprites.Car;

public class Player {
    private Integer lives;
    private Integer points;
    private Car carSelected;

    public Player(Car car) {
        this.lives = 3;
        this.points = 0;
        this.carSelected = car;
    }

    /**
     *
     * @return vidas actuales del jugador
     */
    public Integer getLives() {
        return lives;
    }

    /**
     *
     * @param flag Verdadero indica que se le suma 1 vida y Falso que se resta
     */
    public void updateLives(Boolean flag) {
        if (flag)
            this.lives++;
        else
            this.lives--;
    }

    /**
     *
     * @return Cantidad de puntos actuales
     */
    public Integer getPoints() {
        return points;
    }

    /**
     *
     * @param points Cantidad de puntos a sumar
     */
    public void updatePoints(Integer points) {
        this.points += points;
    }

    /**
     *
     * @return El carro del jugador
     */
    public Car getCarSelected() {
        return carSelected;
    }

    /**
     *
     * @param carSelected El carro que el jugador seleccionó
     */
    public void setCarSelected(Car carSelected) {
        this.carSelected = carSelected;
    }
}
