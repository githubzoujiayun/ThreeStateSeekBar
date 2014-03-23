package pl.merhold.threestateseekbar.app;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Random;


public class MainActivity extends Activity implements ThreeStateButton.OnStateChangeListener, View.OnClickListener{

    private ThreeStateButton tsb;
    private Button stateStart, stateMiddle, stateEnd, randomColor;
    private TextView stateInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tsb = (ThreeStateButton) findViewById(R.id.stateBar);
        stateStart = (Button) findViewById(R.id.stateStart);
        stateMiddle = (Button) findViewById(R.id.stateMiddle);
        stateEnd = (Button) findViewById(R.id.stateEnd);
        randomColor = (Button) findViewById(R.id.randomColor);
        stateInfo = (TextView) findViewById(R.id.stateInfo);

        tsb.setListener(this);
        stateStart.setOnClickListener(this);
        stateMiddle.setOnClickListener(this);
        stateEnd.setOnClickListener(this);
        randomColor.setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.stateStart:
                tsb.setState(ThreeStateButton.State.START);
                break;
            case R.id.stateMiddle:
                tsb.setState(ThreeStateButton.State.MIDDLE);
                break;
            case R.id.stateEnd:
                tsb.setState(ThreeStateButton.State.END);
                break;
            case R.id.randomColor:
                Random rand = new Random();
                int color = Color.rgb(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
                tsb.setTextColor(color);
                break;
        }
    }

    @Override
    public void onStateChange(ThreeStateButton.State state) {
            stateInfo.setText(state.toString());
    }
}
