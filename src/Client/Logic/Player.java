package Client.Logic;

import Client.Sprites.Car;

/**
 * Clase del jugador
 */
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
    private Integer turboTimeout;
    private final Integer turboDefaultTimeout;

    private boolean isCrashed;
    private Integer crashTimeout;
    private final Integer crashDefaultTimeout;

    /**
     * Constructor de la clase Player
     * @param car Objeto tipo car que pertenece al jugador
     */
    public Player(Car car) {
        this.lives = 3;
        this.points = 0;
        this.carSelected = car;
        this.pos = 0;
        this.playerX = 0f;

        this.offroadMaxSpeed = 70d;
        this.maxSpeedNormal = 180d;
        this.maxSpeedTurbo = 270d;
        this.hasTurbo = false;
        this.turboDefaultTimeout = 200;
        this.turboTimeout = this.turboDefaultTimeout;

        this.isCrashed = false;
        this.crashDefaultTimeout = 80;
        this.crashTimeout = this.crashDefaultTimeout;
    }

    /**
     * Método para obtener las vidas del jugador
     * @return vidas actuales del jugador
     */
    public Integer getLives() {
        return lives;
    }

    /**
     * Método para colocar las vidas al jugador
     * @param lives Nuevas vidas
     */
    public void setLives(Integer lives) {
        this.lives = lives;
    }

    /**
     * Método para incrementar o reducir las vidas del jugador
     * @param flag Verdadero indica que se le suma 1 vida y Falso que se resta
     */
    public void updateLives(Boolean flag) {
        if (flag)
            this.lives++;
        else
            this.lives--;
    }

    /**
     * Método para obtener los puntos del jugador
     * @return Cantidad de puntos actuales
     */
    public Integer getPoints() {
        return points;
    }

    /**
     * Método para aumentar o disminuir los puntos del jugador
     * @param points Cantidad de puntos a sumar
     */
    public void updatePoints(Integer points) {
        this.points += points;
    }

    /**
     * Método para obtener el objeto del carro del jugar
     * @return El carro del jugador
     */
    public Car getCarSelected() {
        return carSelected;
    }

    /**
     * Método para obtener la posición del jugador
     * @return Posición del jugador
     */
    public Integer getPos() {
        return pos;
    }

    /**
     * Método para colocar una nueva posición al jugador
     * @param pos Nueva posición
     */
    public void setPos(Integer pos) {
        this.pos = pos;
    }

    /**
     * Método para incrementar la posición del jugador
     * @param update Valor a sumarle a la posición actual
     */
    public void manualUpdatePos(Integer update) {
        this.pos += update;
    }

    /**
     * Método para actualizar la posición del jugador con respecto al carro
     */
    public void updatePos() {
        this.pos += carSelected.getVelocityY().intValue();
    }

    /**
     * Método para obtener la posición en el eje X
     * @return Retorna la posición en el eje X del jugador
     */
    public Float getPlayerX() {
        return playerX;
    }

    /**
     * Método para obtener la posición en el eje X
     * @param playerX Retorna la posición en el eje X
     */
    public void setPlayerX(Float playerX) {
        this.playerX = playerX;
    }

    /**
     * Método para incrementar la posición en el eje X
     * @param update Valor a incrementar al eje X
     */
    public void updatePlayerX(Float update) {
        this.playerX += update;
    }

    /**
     * Método para actualizar la posición del jugador cuando se sale de la pista
     */
    public void updateOffroadSpeedY() {
        if (carSelected.getVelocityY() > offroadMaxSpeed) {
            carSelected.increaseVelocity(0d, -0.7d);
        }
    }

    /**
     * Método para actualizar la velocidad del jugador
     * @param accelerating Indica si el auto esta acelerando o frenando
     */
    public void updateSpeedY(Boolean accelerating) {
        if (!isCrashed) {
            if (accelerating) {
                var accel = hasTurbo ? 1.2d : 0.4d;
                carSelected.increaseVelocity(0d, accel);
            } else {
                carSelected.increaseVelocity(0d, -0.6d);
            }

            Double speedLimit = hasTurbo ? maxSpeedTurbo: maxSpeedNormal;

            if (carSelected.getVelocityY() > speedLimit) {
                carSelected.setVelocity(carSelected.getVelocityX(), speedLimit);
            } else if (carSelected.getVelocityY() < 0d) {
                carSelected.setVelocity(carSelected.getVelocityX(), 0d);
            }
        }
    }

    /**
     * Método para actualizar la velocidad cuando se está frenando
     */
    public void updateBrakingSpeedY() {
        carSelected.increaseVelocity(0d, -1.1d);

        if (carSelected.getVelocityY() < 0d) {
            carSelected.setVelocity(carSelected.getVelocityX(), 0d);
        }
    }

    /**
     * Método para identificar si el auto va a la izquierda o a la derecha
     * @param left Indica la dirección del auto
     */
    public void updateSpeedX(boolean left) {
        if (!isCrashed) {
            if (left) {
                carSelected.setVelocity(-40d, carSelected.getVelocityY());
            } else {
                carSelected.setVelocity(40d, carSelected.getVelocityY());
            }
            this.playerX += carSelected.getVelocityX().floatValue();
        }
    }

    public Boolean isCrashed() {
        return this.isCrashed;
    }

    public void crashed() {
        this.isCrashed = true;
        this.hasTurbo = false;
        carSelected.setVelocity(10d, 0d);
    }

    public void decreaseCrashTimeout() {
        this.crashTimeout--;

        if (crashTimeout < 0) {
            isCrashed = false;
            crashTimeout = crashDefaultTimeout;
        }
    }

    public void Turbo() {
        this.hasTurbo = true;
        this.turboTimeout = turboDefaultTimeout;
    }

    public Boolean hasTurbo() {
        return this.hasTurbo;
    }

    public void decreaseTurboTimeout() {
        this.turboTimeout--;

        if (this.turboTimeout < 0) {
            this.hasTurbo = false;
        }
    }

    @Override
    public String toString() {
        return "[" + "Pos:" + getPos() +
                "PlayerX: " + getPlayerX() +
                "]";
    }
}
