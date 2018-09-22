package com.github.tuna;

import java.util.Map;
import java.util.HashMap;

public class Note extends Object{
  private Map<String, Integer> half_steps_toA= null;

  public Note(){
    half_steps_toA = new HashMap<String, Integer>();
    half_steps_toA.put("C", -9);
    half_steps_toA.put("C#", -8);
    half_steps_toA.put("D", -7);
    half_steps_toA.put("D#", -6);
    half_steps_toA.put("E", -5);
    half_steps_toA.put("F", -4);
    half_steps_toA.put("F#", -3);
    half_steps_toA.put("G", -2);
    half_steps_toA.put("G#", -1);
    half_steps_toA.put("A", 0);
    half_steps_toA.put("A#", 1);
    half_steps_toA.put("B", 2);
  }

  public int num_half_steps_toA4(String tone, int octave){
    return (octave-4)*12 + half_steps_toA.get(tone);
  }

  public String getTone(int num_half_steps){
    int octave = (num_half_steps+9)/12;
    if (num_half_steps < -9){
      octave -= 1;
    }
    octave += 4;
    int num_in_octave = num_half_steps - 12*(octave - 4);

    for (Map.Entry<String,Integer> entry : half_steps_toA.entrySet() ){
      if (entry.getValue() == num_in_octave){
        return String.format("%s%d", entry.getKey(), octave);
      }
    }
    return String.format("Unknown");
  }
}
