import java.awt.*;
import java.util.*;

public class Main {

    public static Dimension size = new Dimension(700, 500);
    public static Random generator = new Random();

    public static Gui.Frame frame = new Gui.Frame();
    public static Gui.Panel panel = new Gui.Panel(size);
    public static Gui.Listener listener = new Gui.Listener();

    public static Game.Player player = new Game.Player(50, 50);

    public static ArrayList<Game.Laser> lasers = new ArrayList<>();
    public static ArrayList<Game.Asteroid> asteroids = new ArrayList<>();
    
    public static Utilities.ExtendableThread painter = new Utilities.ExtendableThread() {
        @Override
        public void execute() throws InterruptedException {
            panel.repaint();
        }
        
        @Override
        public boolean condition() {
            return true;
        }
    };

    public static Utilities.ExtendableThread main = new Utilities.ExtendableThread() {
        @Override
        public void execute() throws InterruptedException {
            try {
                ArrayList<Integer> currentInputs = listener.getInputs();

                currentInputs.forEach((input) -> player.move(input));

                player.act();

                lasers.forEach(Game.Laser::act);

                asteroids.forEach(Game.Asteroid::act);

                renderPanel(panel.getBufferGraphics());

                wait(1000 / 60);
            } catch (NoSuchElementException | ConcurrentModificationException e) {
                System.out.println(e + "obj act error");
            }
        }

        @Override
        public boolean condition() {
            return true;
        }
    };

    public static Utilities.ExtendableThread asteroidGenerator = new Utilities.ExtendableThread() {
        @Override
        public void execute() throws InterruptedException {
            generateAsteroid();
            wait(generator.nextInt(1000)+500);
        }

        @Override
        public boolean condition() {
            return true;
        }
    };

    public static Utilities.ExtendableThread collider = new Utilities.ExtendableThread() {
        @Override
        public void execute() throws InterruptedException {
            try {
                asteroids.forEach(asteroid -> lasers.removeIf(laser -> asteroid.intersecting(laser.getLine())));

            } catch (ConcurrentModificationException | NoSuchElementException | NullPointerException e) {
                System.out.println(e + " collision error");
            }
        }

        @Override
        public boolean condition() {
            return true;
        }
    };

    public static void main(String[] args) {
        frame.addKeyListener(listener);
        frame.setTitle("space-shoot l/r-turn u/d-move");

        frame.add(panel);
        frame.pack();

        main.start();
        asteroidGenerator.start();
        collider.start();
        painter.start();
    }

    public static void generateAsteroid() {
        if (generator.nextBoolean()) {
            if (generator.nextBoolean()) {
                asteroids.add(new Game.Asteroid(generator.nextInt(size.width) + 1, 0, generator.nextInt(91) + 45, 5));
            } else {
                asteroids.add(new Game.Asteroid(generator.nextInt(size.width) + 1, size.height, generator.nextInt(91) + 45 + 180, 5));
            }
        } else {
            if (generator.nextBoolean()) {
                asteroids.add(new Game.Asteroid(0, generator.nextInt(size.height) + 1, generator.nextInt(91) - 45, 5));
            } else {
                asteroids.add(new Game.Asteroid(size.width, generator.nextInt(size.height) + 1, generator.nextInt(91) - 45 - 180, 5));

            }
        }
    }


    public static void renderPanel(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, panel.getWidth(), panel.getHeight());
        g.setColor(Color.WHITE);
        player.drawSelf(g);

        try {
            for (Game.Laser laser : Main.lasers) {
                laser.drawSelf(g);
            }

            for (Game.Asteroid asteroid : Main.asteroids) {
                asteroid.drawSelf(g);
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("paint error");
        }
    }
}
