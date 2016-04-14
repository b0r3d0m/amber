package haven;

import java.io.*;

import org.mozilla.javascript.*;

public class Evaluator {

    public Evaluator(String scriptPath) {

        isValid = true;

        try {

            cx = Context.enter();
            scope = cx.initStandardObjects();

            FileReader fileReader = new FileReader(scriptPath);
            cx.evaluateReader(scope, fileReader, scriptPath, 1, null);

        } catch (IOException io) {

            isValid = false;

        }

    }

    protected void finalize() throws Throwable {

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

    public boolean isValid;

    private Context cx;
    private ScriptableObject scope;

}

