import java.awt.*;
import java.util.ArrayList;

public final class Game {

    public static boolean asteroidIntersecting(Asteroid a, ArrayList<Laser> lasers) {
        for (Laser l : lasers) {
            if (a.intersecting(l.getLine())) {
                return true;
            }
        }

        return false;
    }

    public static final Utilities.ExtendableThread laserProcess = new Utilities.ExtendableThread() {
        @Override
        public void execute() throws InterruptedException {
            Main.lasers.add(new Laser(Main.player.getTip().getX(), Main.player.getTip().getY(), Main.player.direction));

            wait(200);
        }

        @Override
        public boolean waitCondition() {
            return true;
        }

        @Override
        public boolean condition() {
            return true;
        }
    };

    public static class Player extends MovableObject implements Gui.Drawable, Actor {

        private double direction = 0;
        private double velocity = 0;

        public Player(double x, double y) {
            super(x, y);
        }

        public void move(Integer input) {
            double terminal = 4;
            switch (input) {
                case 38: if (velocity < terminal) {velocity += .2;} break; //fwd
                case 40: if (velocity > -terminal) {velocity -= .2;} break; //bck
                case 37: direction -= 3; break; //turn left
                case 39: direction += 3; break; //turn right
                case 32: if (!laserProcess.getState().equals(Thread.State.TIMED_WAITING)) {laserProcess.restart();}break;
                default: System.out.println(input); Main.generateAsteroid();
            }
        }

        @Override
        public void act() {
            movePolar(velocity, direction);

            if (getX() > Main.size.width) {
                setXY(0, getY());
            }

            if (getX() < 0) {
                setXY(Main.size.width, getY());
            }

            if (getY() > Main.size.height) {
                setXY(getX(), 0);
            }

            if (getY() < 0) {
                setXY(getX(), Main.size.height);
            }
        }

        public Calc.P2D getTip() {
            return new Calc.P2D(getX() + Calc.xComp.apply(direction, 20.0), getY() + Calc.yComp.apply(direction, 20.0));
        }

        @Override
        public void drawSelf(Graphics g) {
            Calc.P2D tip    = new Calc.P2D(getX() + Calc.xComp.apply(direction, 20.0), getY() + Calc.yComp.apply(direction, 20.0));
            Calc.P2D left   = new Calc.P2D(getX() + Calc.xComp.apply(direction-130, 20.0), getY() + Calc.yComp.apply(direction-130, 20.0));
            Calc.P2D right  = new Calc.P2D(getX() + Calc.xComp.apply(direction+130, 20.0), getY() + Calc.yComp.apply(direction+130, 20.0));
            Calc.P2D origin = new Calc.P2D(getX(), getY());

            new Calc.Line(tip, left).drawSelf(g);
            new Calc.Line(tip, right).drawSelf(g);
            new Calc.Line(origin, left).drawSelf(g);
            new Calc.Line(origin, right).drawSelf(g);
        }
    }

    public static class Laser extends MovableObject implements Gui.Drawable, Actor {

        private final double direction, length = 20;

        public Laser(double x, double y, double direction) {
            super(x, y);
            this.direction = direction;
        }

        public Calc.Line getLine() {
            return new Calc.Line(
                    new Calc.P2D(getX(), getY()),
                    new Calc.P2D(
                            (getX() + Calc.xComp.apply(direction, length)),
                            (getY() + Calc.yComp.apply(direction, length))
                    )
            );
        }

        @Override
        public void drawSelf(Graphics g) {
            g.drawLine(
                    (int) getX(),
                    (int) getY(),
                    (int) (getX() + Calc.xComp.apply(direction, length)),
                    (int) (getY() + Calc.yComp.apply(direction, length))
            );
        }

        @Override
        public void act() {
            movePolar(10, direction);
            if ((getX() > Main.size.width && getY() > Main.size.height)||(getX()<0 && getY()<0)) {
                Main.lasers.remove(this);
            }
        }
    }

    public static class Asteroid extends MovableObject implements Gui.Drawable, Actor{

        private final int speed, angularSpeed, direction, size;
        private final Calc.Polygon asteroid;

        public Asteroid(double x, double y, int direction, int size) {
            super(x,y);
            this.direction = direction;
            this.size = size;
            asteroid = new Calc.Polygon(x,y,Main.generator.nextInt(size)+size);
            speed = Main.generator.nextInt(5)+1;
            angularSpeed = Main.generator.nextInt(5) + 5;

        }

        public boolean intersecting(Calc.Line line) {
            return asteroid.intersecting(line);
        }

        @Override
        public void drawSelf(Graphics g) {
            asteroid.drawSelf(g);
        }

        @Override
        public void act() {
            asteroid.movePolar(speed, direction);
            movePolar(speed,direction);

            if ((getX() > Main.size.width && getY() > Main.size.height)||(getX()<0 && getY()<0)) {
                Main.asteroids.remove(this);
            }

            asteroid.rotate(angularSpeed);
        }

        public void split() {
            if (size > 3) {
                Main.asteroids.add(new Asteroid(getX(),getY(),Main.generator.nextInt(360)+1, size-1));
                Main.asteroids.add(new Asteroid(getX(),getY(),Main.generator.nextInt(360)+1, size-1));
            }

            Main.asteroids.remove(this);
        }
    }

    public static abstract class MovableObject {
        private double x, y;

        public MovableObject(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public Calc.P2D getPoint() {
            return new Calc.P2D(x, y);
        }

        public void movePolar(double mag, double deg) {
            double dx = Calc.xComp.apply(deg, mag);
            double dy = Calc.yComp.apply(deg, mag);

            moveCartesian(dx, dy);
        }

        public void setXY(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public void moveCartesian(double dx, double dy) {
            x+=dx;
            y+=dy;
        }
    }

    public interface Actor {
        void act();
    }
}
