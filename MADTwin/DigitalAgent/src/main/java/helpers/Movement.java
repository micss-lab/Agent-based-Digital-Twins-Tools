package helpers;

public class Movement {
    private Point2D location;
    private int angle;

    public Movement(Point2D location, int angle) {
        this.location = location;
        this.angle = angle;
    }

    public int getX() {
        return location.x;
    }

    public void setX(int x) {
        this.location.x = x;
    }

    public int getY() {
        return location.y;
    }

    public void setY(int y) {
        this.location.y = y;
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public Point2D getLocation() {
        return location;
    }

    public void setLocation(Point2D location) {
        this.location = location;
    }

    public void forward(int distance) {
        this.location.x += distance * Math.cos(Math.toRadians(angle));
        this.location.y += distance * Math.sin(Math.toRadians(angle));
    }

    public Point2D getForward(int distance) {
        return new Point2D((int) (location.x + distance * Math.cos(Math.toRadians(angle))),
                (int) (location.y + distance * Math.sin(Math.toRadians(angle))));
    }

    public void backward(int distance) {
        this.location.x -= distance * Math.cos(Math.toRadians(angle));
        this.location.y -= distance * Math.sin(Math.toRadians(angle));
    }

    public Point2D getBackward(int distance) {
        return new Point2D((int) (location.x - distance * Math.cos(Math.toRadians(angle))),
                (int) (location.y - distance * Math.sin(Math.toRadians(angle))));
    }

    public void left(int angle) {
        this.angle += angle;
    }

    public void right(int angle) {
        this.angle -= angle;
    }

    public void rotate(int angle) {
        this.angle = angle;
    }

}
