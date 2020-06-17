package Client.Logic;

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
     * TODO hacer documentación
     * @param cameraDepth
     * @param width
     * @param height
     * @param roadWidth
     */
    public static void setValues(Float cameraDepth, Integer width, Integer height, Integer roadWidth) {
        Line.cameraDepth = cameraDepth;
        Line.width = width;
        Line.height = height;
        Line.roadWidth = roadWidth;
    }

    /**
     * TODO hacer documentacion
     */
    public Line() {
        curve = x = y = z = 0f;
        spriteX = clip = 0f;
        renderImage = true;
    }

    /**
     * TODO hacer documentacion
     * @param camX
     * @param camY
     * @param camZ
     */
    public void project(Integer camX, Integer camY, Integer camZ) {
        scale = cameraDepth / (z - camZ);
        X = (1 + scale * (x - camX)) * width / 2;
        Y = (1 - scale * (y - camY)) * height / 2;
        W = scale * roadWidth * width / 2;

    }

    /**
     * TODO hacer documentacion
     * @param context
     * @param img
     */
    public void drawSprite(GraphicsContext context, Image img) {
//        Sprite sprite = new Sprite();
//        sprite.setImage("/res/tree.png", 266, 266);

        Double w = img.getWidth();
        Double h = img.getHeight();

        float destX = X + scale * spriteX * width / 2;
        float destY = Y + 4;
        float destW = w.floatValue() * W / 266f;
        float destH = h.floatValue() * W / 266f;

        destX += destW * spriteX;
        destY += destH * -1;

        float clipH = destY + destH - clip;
        if (clipH < 0) clipH = 0;

        if (clipH >= destH) {
            return;
        }
        context.drawImage(img, destX, destY, destW, destH);
    }
}