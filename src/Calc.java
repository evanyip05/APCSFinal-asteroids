import java.awt.*;
import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Calc {
    public static Function<Double, Double> degToRad = (deg) -> (deg*Math.PI)/180;
    public static Function<Double, Double> radToDeg = (rad) -> (rad*180)/Math.PI;
    public static BiFunction<Double, Double, Double> yComp = (deg, radius) -> radius*Math.sin(degToRad.apply(deg));
    public static BiFunction<Double, Double, Double> xComp = (deg, radius) -> radius*Math.cos(degToRad.apply(deg));

    // check if a point is within the x and y bounds of a lines 2 points
    public static boolean pointInBoundingBox(Line line, P2D point) {
        P2D a = line.getA(), b = line.getB();
        double minY, maxY, minX, maxX;

        maxX = (line.getA().getX() > line.getB().getX())?a.getX():b.getX();
        minX = (line.getA().getX() > line.getB().getX())?b.getX():a.getX();

        maxY = (line.getA().getY() > line.getB().getY())?a.getY():b.getY();
        minY = (line.getA().getY() > line.getB().getY())?b.getY():a.getY();

        return point.getX() >= minX && point.getX() <= maxX && point.getY() >= minY && point.getY() <= maxY;
    }

    public static double degFromPoint(P2D center, P2D point) {
        if (point.getY()-center.getY() >= 0) {
            return radToDeg.apply(Math.atan2(point.getY()-center.getY(), point.getX()-center.getX()));
        } else {
            return 360 + radToDeg.apply(Math.atan2(point.getY()-center.getY(), point.getX()-center.getX()));
        }
    }

    public static void rotateAroundPoint(P2D center, P2D applicant, double deg) {
        double radius = new Line(center, applicant).getLength();
        double newDeg = degFromPoint(center,applicant)+deg;

        double newX = xComp.apply(newDeg, radius)+center.getX();
        double newY = yComp.apply(newDeg, radius)+center.getY();

        applicant.setXY(newX, newY);
    }
    public static class P2D extends Game.MovableObject implements Gui.Drawable {
        public P2D(double x, double y) {
            super(x, y);
        }

        @Override
        public void drawSelf(Graphics g) {
            g.drawRoundRect((int) (getX()-1), (int)(getY()-1),2,2, 2, 2);
        }
    }

    public static class Polygon extends Game.MovableObject implements Gui.Drawable {

        private final ArrayList<P2D> vertices = new ArrayList<>();

        public Polygon(double x, double y, int sides) {
            super(x, y);

            double dx, dy, totalAngle = 0, dTheta = (double) 360 / sides;

            for (int i = 0; i < sides; ++i) {
                dx = xComp.apply(totalAngle, Main.generator.nextInt(sides*10)+20.0);
                dy = yComp.apply(totalAngle, Main.generator.nextInt(sides*10)+20.0);

                vertices.add(new P2D(getX()+dx, getY() + dy));
                totalAngle += dTheta;
            }
        }

        public boolean intersecting(Line line) {
            for (Line edge : getEdges()) {
                P2D intersect = line.getIntersection(edge);
                if (pointInBoundingBox(line, intersect) && pointInBoundingBox(edge, intersect)) {
                    return true;
                }
            }

            return false;
        }

        public ArrayList<Line> getEdges() {
            ArrayList<Line> edges = new ArrayList<>();

            P2D prev = vertices.get(0);
            for (int i = 1; i < vertices.size(); ++i) {
                edges.add(new Line(prev, vertices.get(i)));
                prev = vertices.get(i);
            }

            edges.add(new Line(vertices.get(vertices.size()-1), vertices.get(0)));

            return edges;
        }

        public void rotate(double deg) {
            for (P2D vertex : vertices) {
                P2D center = new P2D(getPoint().getX(), getPoint().getY());
                rotateAroundPoint(center,vertex, deg);
            }
        }

        @Override
        public void moveCartesian(double dx, double dy) {
            super.moveCartesian(dx, dy);

            for (P2D vertex : vertices) {
                vertex.moveCartesian(dx,dy);
            }
        }

        @Override
        public void drawSelf(Graphics g) {
            P2D prev = vertices.get(0);
            for (int i = 1; i < vertices.size(); ++i) {
                new Line(prev, vertices.get(i)).drawSelf(g);
                prev = vertices.get(i);
            }

            new Line(vertices.get(vertices.size()-1), vertices.get(0)).drawSelf(g);
        }
    }

    // line, defined by two points
    public static class Line implements Gui.Drawable {

        private final Utilities.Tuple<P2D> line;
        public final boolean isVertical;

        public Line(P2D a, P2D b) {
            line = new Utilities.Tuple<>(a, b);
            isVertical = (a.getX() == b.getX());
        }

        public double getSlope() {
            return (line.b.getY() - line.a.getY()) / (line.b.getX() - line.a.getX());
        }

        public double getYInt() {
            return line.a.getY() - (line.a.getX() * getSlope());
        }

        public P2D getA() {
            return line.a;
        }

        public P2D getB() {
            return line.b;
        }

        // get the intersection of two points, with a check for vertical lines
        public P2D getIntersection(Line other) {
            double x = (getYInt() - other.getYInt()) / (other.getSlope() - getSlope());
            double y = (getSlope() * x) + getYInt();

            if (!isVertical && !other.isVertical) {
                return new P2D(x, y);
            } else {
                if (other.isVertical) {
                    return new P2D(other.getA().getX(), getValue(other.getA().getX()));
                } else {
                    return new P2D(line.a.getX(), other.getValue(line.a.getX()));
                }
            }
        }

        public double getLength() {
            P2D a, b;

            a = (getA().getX() > getB().getX()) ? getB() : getA();
            b = (getA().getX() > getB().getX()) ? getA() : getB();

            return Math.sqrt(Math.pow(b.getX()-a.getX(),2) + Math.pow(b.getY()-a.getY(),2));
        }

        // get a value of a curve
        public double getValue(double x) {
            return (getSlope() * x) + getYInt();
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof Line) {
                return (line.a.equals(((Line) other).getA()) && line.b.equals(((Line) other).getB()));
            } else {
                return false;
            }
        }

        @Override
        public void drawSelf(Graphics g) {
            g.drawLine((int) line.a.getX(),(int) line.a.getY(),(int) line.b.getX(),(int) line.b.getY());
        }
    }
}
