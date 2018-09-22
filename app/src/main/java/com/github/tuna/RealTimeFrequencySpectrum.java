package com.github.tuna;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

public class RealTimeFrequencySpectrum extends Object{
  private Runnable timer;
  private LineGraphSeries<DataPoint> dataSeries = null;
  private double max_freq = 2000.0;
  private double raw_data[] = null;
  private double raw_data_imag[] = null;
  public double sampling_rate = 44100;
  private boolean graph_finished = true;
  private DataPoint values[] = null;

  private GraphView graph = null;

  public void setData(double data[]){
    if (raw_data == null){
      raw_data = new double[data.length];
      raw_data_imag = new double[data.length];
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
    graph_finished = false;

    Fft fourier_transform = new Fft();
    fourier_transform.transformRadix2(raw_data, raw_data_imag);
    values = new DataPoint[datasetSize()];
    double peak_value = 0.0;
    double peak_freq = 0.0;
    //values = new DataPoint[raw_data.length];
    for (int i=0;i<values.length;i++){
      double amp = Math.pow(raw_data[i], 2) + Math.pow(raw_data_imag[i], 2);
      values[i] = new DataPoint(indx2frequency(i), Math.log(amp));

      if (amp > peak_value){
        peak_value = amp;
        peak_freq = indx2frequency(i);
      }
    }

    //graph.addSeries(dataSeries);
    graph.post(new Runnable(){
    @Override
    public void run(){
      dataSeries.resetData(values);
      graph_finished = true;
    }
  }
    );
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
