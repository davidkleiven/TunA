package com.github.tuna;

import android.app.Fragment;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

public class RealTimeFrequencySpectrum extends Fragment{
  private final Handler mHandler = new Handler();
  private Runnable timer;
  private LineGraphSeries<DataPoint> dataSeries;
  private double max_freq = 2000.0;
  public Recorder rec = null;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState){

   View rootView = inflater.inflate(R.layout.realtime_graph, container, false);

   GraphView graph = (GraphView) rootView.findViewById(R.id.freq_graph);
   dataSeries = new LineGraphSeries<>(getData());
   graph.addSeries(dataSeries);
   graph.getViewport().setXAxisBoundsManual(true);
   graph.getViewport().setMinX(0);
   graph.getViewport().setMaxX(max_freq);
   return rootView;
  }

  private double indx2frequency(int indx){
    double PI = 3.14159265359;
    return 2.0*PI*indx*rec.sample_rate/rec.buffer.length;
  }

  private int datasetSize(){
    double PI = 3.14159265359;
    return (int)(max_freq*rec.buffer.length/(2.0*PI*rec.sample_rate));
  }

  private DataPoint[] getData(){

    if (rec == null){
      throw new RuntimeException("Recorder has not been set!");
    }

    DataPoint values[] = new DataPoint[datasetSize()];
    return values;
  }

}
