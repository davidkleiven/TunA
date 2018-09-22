package com.github.tuna;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

public class RealTimeFrequencySpectrum extends Object{
  private Runnable timer;
  private LineGraphSeries<DataPoint> dataSeries;
  private double max_freq = 2000.0;
  private double raw_data[] = null;
  private double raw_data_imag[] = null;
  private double sampling_rate = 44100;

  public GraphView graph = null;

  public void set_data(double data[]){
    if (raw_data == null){
      raw_data = new double[data.length];
      raw_data_imag = new double[data.length];
    }

    for (int i=0;i<data.length;i++){
      raw_data[i] = data[i];
      raw_data_imag[i] = 0.0;
    }
  }

  private double indx2frequency(int indx){
    double PI = 3.14159265359;
    return 2.0*PI*indx*sampling_rate/raw_data.length;
  }

  private int datasetSize(){
    double PI = 3.14159265359;
    int size = (int)(max_freq*raw_data.length/(2.0*PI*sampling_rate));
    return size;
  }

  private DataPoint[] getData(){

    if (raw_data == null){
      throw new RuntimeException("No data given!");
    }


    Fft fourier_transform = new Fft();
    fourier_transform.transformRadix2(raw_data, raw_data_imag);
    DataPoint values[] = new DataPoint[datasetSize()];

    for (int i=0;i<values.length;i++){
      double amp = Math.pow(raw_data[i], 2) + Math.pow(raw_data_imag[i], 2);
      values[i] = new DataPoint(indx2frequency(i), amp);
    }
    return values;
  }

}
