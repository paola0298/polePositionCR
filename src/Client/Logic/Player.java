package Client.Logic;

import Client.Sprites.Car;

public class Player {
    private Integer lives;
    private Integer points;
    private Car carSelected;
    private Integer pos;
    private Float playerX;
    private Double offroadMaxSpeed;
    private Double maxSpeedNormal;
    private Double maxSpeedTurbo;
    private boolean hasTurbo;

    public Player(Car car) {
        this.lives = 3;
        this.points = 0;
        this.carSelected = car;
        this.pos = 0;
        this.playerX = 0f;

        this.offroadMaxSpeed = 70d;
        this.maxSpeedNormal = 240d;
        this.maxSpeedTurbo = 300d;
        this.hasTurbo = false;
    }

    /**
     *
     * @return vidas actuales del jugador
     */
    public Integer getLives() {
        return lives;
    }

    public void setLives(Integer lives) {
        this.lives = lives;
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

    public Integer getPos() {
        return pos;
    }

    public void setPos(Integer pos) {
        this.pos = pos;
    }

    public void manualUpdatePos(Integer update) {
        this.pos += update;
    }

    public void updatePos() {
        this.pos += carSelected.getVelocityY().intValue();
    }

    public Float getPlayerX() {
        return playerX;
    }

    public void setPlayerX(Float playerX) {
        this.playerX = playerX;
    }

    public void updatePlayerX(Float update) {
        this.playerX += update;
    }

    public void updateOffroadSpeedY() {
        if (carSelected.getVelocityY() > offroadMaxSpeed) {
            carSelected.increaseVelocity(0d, -0.7d);
        }
    }

    public void updateSpeedY(boolean accelerating) {
        if (accelerating) {
            var accel = hasTurbo ? 0.65d : 0.4d;
            carSelected.increaseVelocity(0d, accel);
        } else {
            carSelected.increaseVelocity(0d, -0.6d);
        }

        if (carSelected.getVelocityY() > 240d) {
            carSelected.setVelocity(carSelected.getVelocityX(), 240d);
        } else if (carSelected.getVelocityY() < 0d) {
            carSelected.setVelocity(carSelected.getVelocityX(), 0d);
        }
    }

    public void updateBrakingSpeedY() {
        carSelected.increaseVelocity(0d, -1.1d);

        if (carSelected.getVelocityY() < 0d) {
            carSelected.setVelocity(carSelected.getVelocityX(), 0d);
        }
    }


    public void updateSpeedX(boolean left) {
        if (left) {
            carSelected.setVelocity(-40d, carSelected.getVelocityY());
        } else {
            carSelected.setVelocity(40d, carSelected.getVelocityY());
        }
        this.playerX += carSelected.getVelocityX().floatValue();
    }
}
