package com.github.tuna;

public class ExponentialFilter extends Object{
  private double damping = 0.01;
  private int num_samples = 0;
  private double signal = 0.0;

  public void reset(){
    num_samples = 0;
    signal = 0.0;
  }
  
  /** Set a new damping factor */
  public void setDamping(double decay){damping = Math.exp(-decay);};

  /** Add a new sample to the signal */
  public void addSample(double value){
    num_samples += 1;
    signal = signal*damping + value;
  }

  private double normalization(){
    return (1.0 - Math.pow(damping, num_samples))/(1.0 - damping);
  }

  public double get(){
    return signal/normalization();
  }
}
