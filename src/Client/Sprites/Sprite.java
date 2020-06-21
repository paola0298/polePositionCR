package Client.Sprites;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Clase Sprite
 */
public class Sprite {
    private Double posX;
    private Double posY;
    private Double velocityX;
    private Double velocityY;
    private Image image;
    private Integer width;
    private Integer height;
    private final String cwd = System.getProperty("user.dir");

    private Double projectedPosX;
    private Double projectedPosY;
    private Double projectedWidth;
    private Double projectedHeight;
    private Boolean projectionValid;

    /**
     * Método para saber si el sprite actual tiene una proyección en el canvas válida.
     * @return True si tiene una proyección válida, False en caso contrario.
     */
    public Boolean isProjectionValid() {
        return this.projectionValid;
    }

    /**
     * Método para definir si la proyección del sprite actual es válida.
     * @param valid True si es válida, False en caso contrario.
     */
    public void setProjectionValid(Boolean valid) {
        this.projectionValid = valid;
    }

    /**
     * Método que devuelve la posición proyectada en X
     * @return Un entero con la posición proyectada en X
     */
    public Double getProjectedPosX() {
        return projectedPosX;
    }

    /**
     * Método para colocar la posición proyectada en X
     * @param projectedPosX nueva posición proyectada en X
     */
    public void setProjectedPosX(Double projectedPosX) {
        this.projectedPosX = projectedPosX;
    }

    /**
     * Método que devuelve la posición proyectada en Y
     * @return Un entero con la posición proyectada en Y
     */
    public Double getProjectedPosY() {
        return projectedPosY;
    }

    /**
     * Método para colocar la posición proyectada en Y
     * @param projectedPosY nueva posición proyectada en Y
     */
    public void setProjectedPosY(Double projectedPosY) {
        this.projectedPosY = projectedPosY;
    }

    /**
     * Método para obtener el ancho del sprite proyectado
     * @return Ancho del sprite
     */
    public Double getProjectedWidth() {
        return projectedWidth;
    }

    /**
     * Método para colocar el ancho del sprite proyectado
     * @param projectedWidth  Ancho del sprite
     */
    public void setProjectedWidth(Double projectedWidth) {
        this.projectedWidth = projectedWidth;
    }

    /**
     * Método para obtener el alto del sprite proyectado
     * @return Altura del sprite
     */
    public Double getProjectedHeight() {
        return projectedHeight;
    }

    /**
     * Método para colocar la altura proyectada del sprite
     * @param projectedHeight Altura del sprite
     */
    public void setProjectedHeight(Double projectedHeight) {
        this.projectedHeight = projectedHeight;
    }


    /**
     * Constructor de la clase
     */
    public Sprite() {
        this.velocityX = 0.0;
        this.velocityY = 0.0;
        this.posX = 0.0;
        this.posY = 0.0;
    }

    /**
     * Método para colocar una imagen al sprite
     * @param path Ruta de la imagen
     * @param height Altura de la imagen
     * @param width Ancho de la imagen
     */
    public void setImage(String path, Integer height, Integer width) {
        this.image = imageLoader(cwd.replaceAll("\\\\", "/") + path, height.doubleValue(), width.doubleValue());
        this.height = height;
        this.width = width;
    }

    /**
     * Método para colocar la imagen del sprite
     * @param image Imagen del sprite
     */
    public void setImage(Image image) {
        this.image = image;
    }

    /**
     * Método para obtener la imagen del sprite
     * @return Un objeto tipo image de la imagen
     */
    public Image getImage() {
        return this.image;
    }

    /**
     * Método para obtener la posición en el eje X
     * @return Posición en X
     */
    public Double getPosX() {
        return this.posX;
    }

    /**
     * Método para obtener la posición en el eje Y
     * @return Posición en Y
     */
    public Double getPosY() {
        return this.posY;
    }

    /**
     * Método para obtener el ancho de la imagen
     * @return Ancho de la imagen
     */
    public Integer getWidth() {
        return this.width;
    }

    /**
     * Método para obtener la altura de la imagen
     * @return Altura de la imagen
     */
    public Integer getHeight() {
        return this.height;
    }

    /**
     * Método para colocar la posición X y Y del sprite
     * @param x Posicion en X
     * @param y Posicion en Y
     */
    public void setPosition(Double x, Double y) {
        this.posX = x;
        this.posY = y;
    }

    /**
     * Método para colocar la velocidad en X y en Y
     * @param x Velocidad en x
     * @param y Velocidad en y
     */
    public void setVelocity(Double x, Double y) {
        this.velocityX = x;
        this.velocityY = y;
    }

    /**
     * Método para obtener la velocidad en X
     * @return Velocidad en X
     */
    public Double getVelocityX() {
        return this.velocityX;
    }

    /**
     * Método para obtener la velocidad en Y
     * @return Velocidad en Y
     */
    public Double getVelocityY() {
        return this.velocityY;
    }

    /**
     * Método para aumentar la velocidad del sprite
     * @param x Valor a incrementar en la velocidad de x
     * @param y Valor a incrementar en la velocidad de y
     */
    public void increaseVelocity(Double x, Double y) {
        this.velocityX += x;
        this.velocityY += y;
    }

    /**
     * Método para actualizar el tiempo trasncurrido
     * @param time tiempo transcurrido
     */
    public void update(Double time) {
        this.posX += this.velocityX * time;
        this.posY += this.velocityY * time;
    }

    /**
     * Método para dibujar en la interfaz
     * @param gc contexto grafico donde dibujar
     */
    public void render(GraphicsContext gc) {
        gc.drawImage(this.image, this.posX, this.posY);
    }

    /**
     * Método para obtener los boundaries del sprite
     * @return los boundaries del sprite
     */
    public Rectangle2D getBoundary() {
        return new Rectangle2D(this.posX, this.posY, this.width, this.height);
    }

    /**
     * Método para conocer si el sprite actual interseca con otro
     * @param other Sprite con el que se desea saber si hay intersección
     * @return True si hay intersección, False en caso contrario
     */
    public Boolean intersectsProjected(Sprite other) {
        return this.getProjectedBoundary().intersects(other.getProjectedBoundary());
    }

    /**
     * Método para obtener los boundaries proyectados del sprite
     * @return Boundaries del sprite
     */
    public Rectangle2D getProjectedBoundary() {
        return new Rectangle2D(this.projectedPosX, this.projectedPosY, this.projectedWidth, this.projectedHeight);
    }

    /**
     * Método para conocer si un Sprite choca con otro
     * @param s otra sprite
     * @return si intersecan
     */
    public Boolean intersects(Sprite s) {
        return s.getBoundary().intersects( this.getBoundary() );
    }

    /**
     * Método para conocer la representación del sprite
     * @return representación en string del sprite
     */
    public String toString()
    {
        return " Position: [" + this.posX + "," + this.posY + "]"
                + " Velocity: [" + this.velocityX + "," + this.velocityY + "]";
    }

    /**
     * @param path Ruta de la imagen
     * @return El objeto de la imagen creada
     */
    private Image imageLoader(String path, Double height, Double width){
        try{
            FileInputStream i = new FileInputStream(path);
            return new Image(i, width, height, false, false);
        }catch (FileNotFoundException e){
            System.out.println("Couldn't load images!");
        }
        System.out.println("Could not find " + path);
        return null;
    }
}
