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
import android.graphics.Color;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

public class MainActivity extends Activity {
  private Recorder rec = null;
  private RealTimeFrequencySpectrum spectrum = null;
  private boolean recording_is_running = false;
  private TextView peak_frequency = null;
  private EqualTemperedScale scale = new EqualTemperedScale();
  private PitchTracker pitch = new PitchTracker();
  private GraphView stat_graph = null;
  private PointsGraphSeries<DataPoint> stat_series = new PointsGraphSeries<>();
  private DataPoint stat_values[] = null;
  private boolean stat_graph_finished_updating = true;
  private double max_freq = 4000.0;

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
        case HandlerMessages.graph_finished:
          peak_frequency.setText(String.format("Fundamental frequency: %,.1f Hz (%s)", spectrum.fundamental_freq, scale.tone(spectrum.fundamental_freq)));
          if (!spectrum.only_noise){
            pitch.update(spectrum.peak_freq);
            updateStatistics();
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
    spectrum.handler = mHandler;
    spectrum.max_freq = max_freq;

    Button record_button =(Button)findViewById(R.id.record_button);
    record_button.setText("Record");

    Button clear_data_button = (Button)findViewById(R.id.clear_data_button);

    peak_frequency = (TextView)findViewById(R.id.peak_frequency);

    GraphView raw_audio_graph = (GraphView) findViewById(R.id.freq_graph);

    GraphView graph = (GraphView) findViewById(R.id.freq_graph);
    graph.getViewport().setXAxisBoundsManual(true);
    graph.getViewport().setMinX(0);
    graph.getViewport().setMaxX(max_freq);
    graph.getViewport().setYAxisBoundsManual(true);
    graph.getViewport().setMinY(20);
    graph.getViewport().setMaxY(40);
    GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
    gridLabel.setHorizontalAxisTitle("Frequency (Hz)");
    gridLabel.setVerticalAxisTitle("Log amplitude");

    // // Add the graph to the realtime frequency spectrum
    spectrum.setGraph(graph);

    // Initialize the statistics graph
    stat_graph = (GraphView) findViewById(R.id.statistics);
    stat_graph.addSeries(stat_series);
    stat_graph.getViewport().setXAxisBoundsManual(true);
    stat_graph.getViewport().setMinX(0);
    stat_graph.getViewport().setMaxX(1000.0);
    gridLabel = stat_graph.getGridLabelRenderer();
    gridLabel.setHorizontalAxisTitle("Frequency (Hz)");
    gridLabel.setVerticalAxisTitle("Average deviation (Hz)");
    stat_series.setSize(10);

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

    // Set click listener for record button
    clear_data_button.setOnClickListener(
      new View.OnClickListener(){
        @Override
        public void onClick(View v){
          pitch.reset();
          updateStatistics();
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

  private void updateStatistics(){
    if (!stat_graph_finished_updating){
      // Don't start a new update of the graph if it is not finished
      return;
    }

    stat_graph_finished_updating = false;
    Thread thread = new Thread(
      new Runnable(){
        @Override
        public void run(){
          stat_values = new DataPoint[pitch.numTonesTracked()];
          ComparableDatapoint dpoints[] = new ComparableDatapoint[stat_values.length];
          for (int i=0;i<stat_values.length;i++){
            try{
              String tone = pitch.tracked_tones.get(i);
              Map<String, Double> stat = pitch.statistics(tone);
              String tone_name = tone.substring(0, tone.length()-1);
              int octave = Integer.parseInt(tone.substring(tone.length()-1));
              double target_freq = scale.frequency(tone_name, octave);
              double diff = stat.get("mean") - target_freq;

              dpoints[i] = new ComparableDatapoint(target_freq, diff);
            } catch (NumberFormatException e) {
              dpoints[i] = new ComparableDatapoint(0.0, 0.0);
            }
          }

          // Sort the data points based on the x value
          Arrays.sort(dpoints);

          for (int i=0;i<dpoints.length;i++){
            stat_values[i] = new DataPoint(dpoints[i].x, dpoints[i].y);
          }


          stat_graph.post(new Runnable(){
            @Override
            public void run(){
              stat_series.resetData(stat_values);
              stat_graph_finished_updating = true;
            }
          });
        }
      }
    );
    thread.start();
  }
}
