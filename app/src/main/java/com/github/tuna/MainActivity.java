package com.github.tuna;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;


public class MainActivity extends Activity {
  private Recorder rec = null;
  private RealTimeFrequencySpectrum spectrum = null;
  private boolean recording_is_running = false;

  private final Handler mHandler = new Handler(){
    @Override
    public void handleMessage(Message msg){
      switch (msg.what){
        case HandlerMessages.recording_finished:
          if (recording_is_running){
            spectrum.setData(rec.buffer);
            spectrum.updateChartInThread();
            rec.recordAudio();
          }
          break;
      }
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    requestRecordAudioPermission();

    // Initialize the recorder
    rec = new Recorder();
    rec.handler = mHandler;
    spectrum = new RealTimeFrequencySpectrum();
    spectrum.sampling_rate = rec.sampling_rate;

    Button record_button =(Button)findViewById(R.id.record_button);
    record_button.setText("Record");

    GraphView raw_audio_graph = (GraphView) findViewById(R.id.freq_graph);

    GraphView graph = (GraphView) findViewById(R.id.freq_graph);
    graph.getViewport().setXAxisBoundsManual(true);
    graph.getViewport().setMinX(0);
    graph.getViewport().setMaxX(2000.0);
    GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
    gridLabel.setHorizontalAxisTitle("Frequency (Hz)");
    gridLabel.setVerticalAxisTitle("Log amplitude");

    // // Add the graph to the realtime frequency spectrum
    spectrum.setGraph(graph);

    // // Set on click listener for record button
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
            //rec.start();
            recording_is_running = true;
            rec.recordAudio();
          }
          else
          {
            recording_is_running = false;
            record_button.setText("Record");
          }
        }
      }
    );
  }

  private void requestRecordAudioPermission(){
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
          != PackageManager.PERMISSION_GRANTED) {
    // Permission is not granted
    // Should we show an explanation?
      if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {

          // Show an expanation to the user *asynchronously* -- don't block
          // this thread waiting for the user's response! After the user
          // sees the explanation, try again to request the permission.

      } else {

          // No explanation needed, we can request the permission.
          ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
      }
    }
  }
}
