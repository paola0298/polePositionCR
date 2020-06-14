package Client.Sprites;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Sprite {
    private Double posX;
    private Double posY;
    private Double velocityX;
    private Double velocityY;
    private Image image;
    private Integer width;
    private Integer height;
    private final String cwd = System.getProperty("user.dir");

    /**
     *
     */
    public Sprite() {
        this.velocityX = 0.0;
        this.velocityY = 0.0;
        this.posX = 0.0;
        this.posY = 0.0;
    }

    /**
     *
     * @param path Ruta de la imagen
     * @param height Altura de la imagen
     * @param width Ancho de la imagen
     */
    public void setImage(String path, Integer height, Integer width) {
        this.image = imageLoader(cwd.replaceAll("\\\\", "/") + path);
        this.height = height;
        this.width = width;
    }

    public Image getImage() {
        return this.image;
    }

    public Double getPosX() {
        return this.posX;
    }

    public Double getPosY() {
        return this.posY;
    }

    public Integer getWidth() {
        return this.width;
    }

    public Integer getHeight() {
        return this.height;
    }

    /**
     *
     * @param x Posicion en x
     * @param y Posicion en y
     */
    public void setPosition(Double x, Double y) {
        this.posX = x;
        this.posY = y;
    }

    /**
     *
     * @param x Velocidad en x
     * @param y Velocidad en y
     */
    public void setVelocity(Double x, Double y) {
        this.velocityX = x;
        this.velocityY = y;
    }

    public Double getVelocityX() {
        return this.velocityX;
    }

    public Double getVelocityY() {
        return this.velocityY;
    }

    /**
     *
     * @param x Valor a incrementar en la velocidad de x
     * @param y Valor a incrementar en la velocidad de y
     */
    public void increaseVelocity(Double x, Double y) {
        this.velocityX += x;
        this.velocityY += y;
    }

    /**
     *
     * @param time tiempo transcurrido
     */
    public void update(Double time) {
        this.posX += this.velocityX * time;
        this.posY += this.velocityY * time;
    }

    /**
     *
     * @param gc contexto grafico donde dibujar
     */

    public void render(GraphicsContext gc) {
        gc.drawImage(this.image, this.posX, this.posY);
    }

    /**
     *
     * @return los boundaries del sprite
     */
    public Rectangle2D getBoundary() {
        return new Rectangle2D(this.posX, this.posY, this.width, this.height);
    }

    /**
     *
     * @param s otra sprite
     * @return si intersecan
     */
    public Boolean intersects(Sprite s) {
        return s.getBoundary().intersects( this.getBoundary() );
    }

    /**
     *
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
    private Image imageLoader(String path){
        try{
            FileInputStream i = new FileInputStream(path);
            return new Image(i);
        }catch (FileNotFoundException e){
            System.out.println("Couldn't load images!");
        }
        System.out.println("Could not find " + path);
        return null;
    }
}
