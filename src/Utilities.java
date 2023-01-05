import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class Utilities {

    public static <E> void doAndRemoveIf(ArrayList<E> list, Consumer<E> action, Predicate<E> condition) {
        list.forEach((item) -> {
            if (condition.test(item)) {
                action.accept(item);
            }
        });

        list.removeIf(condition);
    }

    public static class Tuple<E> {
        public final E a, b;

        public Tuple(E a, E b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Tuple) {
                if (((Tuple<?>) o).a.getClass() == a.getClass()) {
                    return ((((Tuple<?>) o).a == a) && (((Tuple<?>) o).b == b)) || ((((Tuple<?>) o).b == a) && (((Tuple<?>) o).a == b));
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return a + " " + b;
        }
    }

    public abstract static class ExtendableThread extends Thread {
        @Override
        public final void run() {
            while (condition()) {
                synchronized (this) {
                    try {
                        execute();

                        if (waitCondition()) {
                            wait();
                        }

                    } catch (InterruptedException ignore) {
                    }
                }
            }
        }

        public final void restart() {
            synchronized (this) {
                if (getState().equals(State.NEW)) {
                    start();
                }

                executeOnRestart();
                notify();
            }
        }

        public void executeOnRestart() {}

        public boolean waitCondition() {return false;}

        public abstract void execute() throws InterruptedException;

        public abstract boolean condition();
    }

}
