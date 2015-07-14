package com.tasora.replete;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import static com.tasora.replete.Util.convertStreamToString;

public class MainActivity extends AppCompatActivity {
    public static Context rhino_context;
    public static Scriptable rhino_scope;
    public String goog_base_source;
    public String deps_source;
    public String macros_source;

    public void updateUi(String msg) {
        TextView tv = (TextView) findViewById(R.id.mysrc);
        tv.setText(msg);
    }

    public static void evalJs(String src) {
        rhino_context.evaluateString(rhino_scope, src, "Main Activity", 1, null);
    }

    public void setUpRhino() {
        rhino_context = Context.enter();
        rhino_context.setOptimizationLevel(-1);
        rhino_scope = rhino_context.initStandardObjects();
        ScriptableObject.putProperty(rhino_scope, "javaContext", Context.javaToJS(this, rhino_scope));
    }

    public void setUpConsoleLog() {
        evalJs(Util.REPLICATOR_LOG);
    }

    public void setUpGlobalContext() {
        evalJs(Util.GLOBAL_CTX);
    }

    public void setUpImportClosureScript() {
        evalJs(Util.REPLICATOR_IMPORT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setting up js context
        setUpRhino();
        setUpConsoleLog();
        setUpGlobalContext();
        setUpImportClosureScript();

        //Reading cljs source and converting them to string so that we can eval them from Rhino
        try {
            goog_base_source = convertStreamToString(getAssets().open("out/goog/base.js"));
            deps_source = convertStreamToString(getAssets().open("out/deps.js"));
            macros_source = convertStreamToString(getAssets().open("out/cljs/core$macros.js"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        evalJs(goog_base_source);
        evalJs(deps_source);
        rhino_context.evaluateString(rhino_scope, "goog.isProvided_ = function(x) { return false; };", "MainActivity", 1, null);
        rhino_context.evaluateString(rhino_scope, "goog.require = function (name) { return CLOSURE_IMPORT_SCRIPT(goog.dependencies_.nameToPath[name]); };", "MainActivity", 1, null);
        rhino_context.evaluateString(rhino_scope, "goog.require('cljs.core');", "MainActivity", 1, null);
        rhino_context.evaluateString(rhino_scope, "cljs.core._STAR_loaded_libs_STAR_ = cljs.core.into.call(null, cljs.core.PersistentHashSet.EMPTY, [\"cljs.core\"]);\n" +
                "goog.require = function (name, reload) {\n" +
                "    if(!cljs.core.contains_QMARK_(cljs.core._STAR_loaded_libs_STAR_, name) || reload) {\n" +
                "        var AMBLY_TMP = cljs.core.PersistentHashSet.EMPTY;\n" +
                "        if (cljs.core._STAR_loaded_libs_STAR_) {\n" +
                "            AMBLY_TMP = cljs.core._STAR_loaded_libs_STAR_;\n" +
                "        }\n" +
                "        cljs.core._STAR_loaded_libs_STAR_ = cljs.core.into.call(null, AMBLY_TMP, [name]);\n" +
                "        CLOSURE_IMPORT_SCRIPT(goog.dependencies_.nameToPath[name]);\n" +
                "    }\n" +
                "};", "MainActivity", 1, null);
        rhino_context.evaluateString(rhino_scope, "goog.require('replete.core');", "MainActivity", 1, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void calljs(View view) {
        rhino_context.evaluateString(rhino_scope, macros_source, "MainActivity", 1, null);
        Object res = rhino_context.evaluateString(rhino_scope, "replete.core.read-eval-print.call(null, '(+ 1 2)');", "MainActivity", 1, null);
        Log.d("Result", res.toString());
    }
}