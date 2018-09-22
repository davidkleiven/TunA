package com.github.tuna;

public class EqualTemperedScale extends Object{
  private double f0 = 442;
  private final double a = Math.pow(2.0, 1.0/12.0);
  private Note notes = new Note();

  double frequency(String note, int octave){
    int num_half = notes.num_half_steps_toA4(note, octave);
    return f0 * Math.pow(a, num_half);
  }

  String tone(double frequency){
    int n = (int)(Math.round(Math.log(frequency/f0)/Math.log(a)));
    return notes.getTone(n);
  }
}
