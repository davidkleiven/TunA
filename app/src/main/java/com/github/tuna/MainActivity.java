package com.github.tuna;

import android.app.Activity;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import com.jjoe64.graphview.GraphView;


public class MainActivity extends Activity {
  private Recorder rec = null;
  private RealTimeFrequencySpectrum spectrum = null;
  private final Handler mHandler = new Handler();

  @Override
  protected void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Initialize the recorder
    rec = new Recorder();

    Button record_button =(Button)findViewById(R.id.record_button);
    record_button.setText("Record");

    GraphView raw_audio_graph = (GraphView) findViewById(R.id.freq_graph);

    GraphView graph = (GraphView) findViewById(R.id.freq_graph);
    graph.getViewport().setXAxisBoundsManual(true);
    graph.getViewport().setMinX(0);
    graph.getViewport().setMaxX(2000.0);

    // Set on click listener for record button
    record_button.setOnClickListener(
      new View.OnClickListener(){
        @Override
        public void onClick(View v)
        {
          Button record_button =(Button)findViewById(R.id.record_button);
          String current_text = (String)record_button.getText();

          if (current_text == "Record")
          {
            record_button.setText("Stop");
            rec.start();
          }
          else
          {
            record_button.setText("Record");
            rec.stop();
          }
        }
      }
    );
  }

}
