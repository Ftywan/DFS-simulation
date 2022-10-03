package ipfs;

public class Coordinate implements peersim.core.Protocol {
    private double x, y;

    public Coordinate() {
        x = -1;
        y = -1;
    }

    @Override
    public Object clone() {
        Coordinate point = null;
        try {
            point = (Coordinate) super.clone();
        } catch (CloneNotSupportedException ignored) {
        }
        return point;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }
}
