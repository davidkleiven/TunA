package com.github.tuna;

import android.os.Handler;
import android.os.Message;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

public class RealTimeFrequencySpectrum extends Object{
  private LineGraphSeries<DataPoint> dataSeries = null;
  private double raw_data[] = null;
  private double raw_data_imag[] = null;
  private boolean graph_finished = true;
  private DataPoint values[] = null;
  private double raw_log_amplitude[] = null;
  private double noise_level = 30;
  public Handler handler = null;

  private GraphView graph = null;
  public double sampling_rate = 44100;
  public double peak_freq = 0.0;
  public double fundamental_freq = 0.0;
  public boolean only_noise = false;
  public double max_freq = 2000.0;

  public void setData(double data[]){
    if (raw_data == null){
      raw_data = new double[data.length];
      raw_data_imag = new double[data.length];
      raw_log_amplitude = new double[data.length];
    }

    for (int i=0;i<data.length;i++){
      raw_data[i] = data[i];
      raw_data_imag[i] = 0.0;
    }
  }

  public void setGraph(GraphView new_graph){
    graph = new_graph;
    dataSeries = new LineGraphSeries<DataPoint>();
    graph.addSeries(dataSeries);
  }

  private double indx2frequency(int indx){
    double PI = 3.14159265359;
    return indx*sampling_rate/raw_data.length;
    //return 2.0*PI*indx*sampling_rate/raw_data.length;
  }

  private int freq2indx(double freq){
    return (int)(freq*raw_data.length/sampling_rate);
  }

  private int datasetSize(){
    double PI = 3.14159265359;
    //int size = (int)(max_freq*raw_data.length/(2.0*PI*sampling_rate));
    int size = (int)(max_freq*raw_data.length/(sampling_rate));
    return size;
  }

  private void updateChart(){

    if (raw_data == null){
      throw new RuntimeException("No data given!");
    }

    if (graph == null){
      throw new RuntimeException("Graph has not been set!");
    }

    if (handler == null){
      throw new RuntimeException("No message handler set!");
    }

    graph_finished = false;

    Fft fourier_transform = new Fft();
    fourier_transform.transformRadix2(raw_data, raw_data_imag);
    values = new DataPoint[datasetSize()];
    double peak_value = 0.0;
    //values = new DataPoint[raw_data.length];
    for (int i=0;i<values.length;i++){
      double amp = Math.pow(raw_data[i], 2) + Math.pow(raw_data_imag[i], 2);
      raw_log_amplitude[i] = Math.log(amp);
      values[i] = new DataPoint(indx2frequency(i), Math.log(amp));

      if (amp > peak_value){
        peak_value = amp;
        peak_freq = indx2frequency(i);
      }
    }

    only_noise = (Math.log(peak_value) < noise_level);

    if (!only_noise){
      estimateFundamental();
    }

    Message msg = Message.obtain();
    msg.what = HandlerMessages.graph_finished;
    handler.sendMessage(msg);

    graph.post(new Runnable(){
    @Override
    public void run(){
      dataSeries.resetData(values);
      graph_finished = true;
    }
  }
    );
  }

  public void estimateFundamental(){
    FundamentalFreqEstimator estimator = new FundamentalFreqEstimator();
    estimator.threshold = noise_level;
    double min_group_sep = freq2indx(40.0);
    fundamental_freq = indx2frequency((int) estimator.estimateFundamental(raw_log_amplitude, min_group_sep));
  }

  public void updateChartInThread(){
    if (!graph_finished){
      return;
    }

    Thread thread = new Thread(
      new Runnable(){
        @Override
        public void run(){
          updateChart();
        }
      }
    );
    thread.start();
  }

}
