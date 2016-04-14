package haven;

import java.io.*;

import org.mozilla.javascript.*;

public class Evaluator {

    public Evaluator(String scriptPath) throws IOException {

        cx = Context.enter();
        scope = cx.initStandardObjects();

        FileReader fileReader = new FileReader(scriptPath);
        cx.evaluateReader(scope, fileReader, scriptPath, 1, null);

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

    private Context cx;
    private ScriptableObject scope;

}

