package Client.Logic;

import Client.Sprites.Sprite;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Line {
    public Float x, y, z;
    public Float X, Y, W;
    public Float scale;
    public Float curve;
    public Float spriteX, clip;
    public boolean renderImage;

    private static Float cameraDepth;
    private static Integer width;
    private static Integer height;
    private static Integer roadWidth;

    /**
     * Método para setear los valores generales de las líneas
     * @param cameraDepth Profundidad de campo de la cámara
     * @param width Ancho de la ventana
     * @param height Alto de la ventana
     * @param roadWidth Ancho de la pista
     */
    public static void setValues(Float cameraDepth, Integer width, Integer height, Integer roadWidth) {
        Line.cameraDepth = cameraDepth;
        Line.width = width;
        Line.height = height;
        Line.roadWidth = roadWidth;
    }

    /**
     * Constructor de la clase Line
     */
    public Line() {
        curve = x = y = z = 0f;
        spriteX = clip = 0f;
        renderImage = true;
    }

    /**
     * Método utilizado para convertir las coordenadas 3d a 2d para poder visualizar
     * correctamente las líneas en el canvas 2d
     * @param camX Posición X de la cámara
     * @param camY Posición Y de la cámara
     * @param camZ Posición Z de la cámara
     */
    public void project(Integer camX, Integer camY, Integer camZ) {
        scale = cameraDepth / (z - camZ);
        X = (1 + scale * (x - camX)) * width / 2;
        Y = (1 - scale * (y - camY)) * height / 2;
        W = scale * roadWidth * width / 2;

    }

    /**
     * Método para dibujar un sprite en una posición dada de la pista
     * @param context Contexto gráfico para dibujar en canvas
     * @param img Sprite a dibujar.
     */
    public void drawSprite(GraphicsContext context, Image img) {
//        Sprite sprite = new Sprite();
//        sprite.setImage("/res/tree.png", 266, 266);

        Double w = img.getWidth();
        Double h = img.getHeight();

        Float destX = X + scale * spriteX * width / 2;
        Float destY = Y + 4;
        Float destW = w.floatValue() * W / 266f;
        Float destH = h.floatValue() * W / 266f;

        destX += destW * spriteX;
        destY += destH * -1;

        Float clipH = destY + destH - clip;
        if (clipH < 0) clipH = 0f;

        if (clipH >= destH) {
            return;
        }
        context.drawImage(img, destX, destY, destW, destH);
    }

    /**
     * Método utilizado para dibujar un sprite según su posición y tamaño
     * @param context Contexto gráfico para dibujar en canvas
     * @param sprite El sprite a dibujar
     * @param img Imagen del sprite
     * @return Sprite con datos de proyección actualizados
     */
    public Sprite drawSprite(GraphicsContext context, Sprite sprite, Image img) {
        Double w = img.getWidth();
        Double h = img.getHeight();

        Float destX = X + scale * spriteX * width / 2;
        Float destY = Y + 4;
        Float destW = w.floatValue() * W / 266f;
        Float destH = h.floatValue() * W / 266f;

        destX += destW * spriteX;
        destY += (destH * -1);

        Float clipH = destY + destH - clip;
        if (clipH < 0) clipH = 0f;

        if (clipH >= destH) {
            sprite.setProjectionValid(false);
            return sprite;
        }

        context.drawImage(img, destX, destY, destW, destH);

        sprite.setProjectedPosX(destX.doubleValue());
        sprite.setProjectedPosY(destY.doubleValue());
        sprite.setProjectedWidth(destW.doubleValue());
        sprite.setProjectedHeight(destH.doubleValue());
        sprite.setProjectionValid(true);

        return sprite;
    }
}