package Client.Logic;

public class Line {
    public Float x,y,z;
    public Float X,Y,W;
    public Float scale;
    public Float curve;

    private static Float cameraDepth;
    private static Integer width;
    private static Integer height;
    private static Integer roadWidth;

    public static void setValues(Float cameraDepth, Integer width, Integer height, Integer roadWidth) {
        Line.cameraDepth = cameraDepth;
        Line.width = width;
        Line.height = height;
        Line.roadWidth = roadWidth;
    }

    public Line() {
        curve = x = y = z = 0f;
    }

    public void project(Integer camX, Integer camY, Integer camZ) {
        scale = cameraDepth / (z - camZ);
        X = (1 + scale * (x - camX)) * width / 2;
        Y = (1 - scale * (y - camY)) * height / 2;
        W = scale * roadWidth * width / 2;
    }
}