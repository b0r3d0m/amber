package haven;

import java.io.*;
import java.util.*;

import org.mozilla.javascript.*;

public class Evaluator {

    public Evaluator(String scriptPath) {

        evaluatorThread = new Thread(() -> {

            initContext();

            boolean loaded = loadScript(scriptPath);
            synchronized (isValidSync) {
                isValid = loaded;
                if (!isValid) {
                    return;
                }
            }

            while (true) {

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    return;
                }

                synchronized (tasksSync) {
                    Runnable task = tasks.poll();
                    if (task != null) {
                        task.run();
                    }
                }

            }

        });
        evaluatorThread.start();

    }

    private void initContext() {

        cx = Context.enter();
        scope = cx.initStandardObjects();

    }

    private boolean loadScript(String scriptPath) {

        try {

            FileReader fileReader = new FileReader(scriptPath);
            cx.evaluateReader(scope, fileReader, scriptPath, 1, null);

        } catch (IOException io) {

            return false;

        }

        return true;

    }

    public void runDelayedTask(Runnable task, long millisecsDelay) {

        synchronized (isValidSync) {
            if (!isValid) {
                return;
            }
        }

        new Timer().schedule(
            new TimerTask() {
                @Override
                public void run() {
                    addTaskToQueue(task);
                }
            },
            millisecsDelay
        );

    }

    private void addTaskToQueue(Runnable task) {

        synchronized (tasksSync) {
            tasks.add(task);
        }

    }

    protected void finalize() throws Throwable {

        // TODO: interrupt and join evaluatorThread
        Context.exit();
        super.finalize();

    }

    public Object call(String functionName, Object[] args) {

        Object fObj = scope.get(functionName, scope);
        Function f = (Function) fObj;
        Object result = f.call(cx, scope, scope, args);

        if (result instanceof NativeObject) {
            return ((NativeObject) result).entrySet();
        } else if (result instanceof Wrapper) {
            return ((Wrapper) result).unwrap();
        }

        return result;

    }

    public Object call(String functionName) {

        return call(functionName, null);

    }

    private boolean isValid = true;
    private final Object isValidSync = new Object();

    private Queue<Runnable> tasks = new LinkedList<Runnable>();
    private final Object tasksSync = new Object();

    private Thread evaluatorThread;

    private Context cx;
    private ScriptableObject scope;

}

