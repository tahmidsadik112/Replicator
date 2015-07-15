package com.tasora.replicator;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ViewSwitcher;

import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements JSEvaluator.Listener {

    @Bind(R.id.input) EditText input;
    @Bind(R.id.switcher) ViewSwitcher switcher;
    @Bind(R.id.repl_history) ReplHistory replHistory;

    @OnClick(R.id.eval) void triggerEval() {
        App.get(this).evaluator().evaluate(input.getText().toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        input.setOnKeyListener(new EnterListener());
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.get(this).evaluator().setListener(this);
        App.get(this).bus().register(this);
    }

    @Override
    protected void onPause() {
        App.get(this).evaluator().unsetListener();
        App.get(this).bus().unregister(this);
        super.onPause();
    }

    @Subscribe
    public void onAppReady(App.ReadyEvent event) {
        switcher.showNext();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.tasora.replicator.R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_clear_history) {
            replHistory.clear();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateUi(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            replHistory.push(msg);
            input.setText("");
        }
    }

    private class EnterListener implements View.OnKeyListener {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                triggerEval();
                return true;
            }
            return false;
        }
    }
}
