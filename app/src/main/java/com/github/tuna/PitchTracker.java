package com.github.tuna;

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;

public class PitchTracker extends Object{
  private Map<String, Double> sum_freq = new HashMap<String, Double>();
  private Map<String, Double> count_freq = new HashMap<String, Double>();
  private Map<String, Double> var_freq = new HashMap<String, Double>();
  private EqualTemperedScale scale = new EqualTemperedScale();
  private int num_keys = 0;
  public LinkedList<String> tracked_tones = new LinkedList<String>();

  public String update(double freq){
    String key = scale.tone(freq);
    if (key == "Unknown"){
      return key;
    }
    
    if (sum_freq.get(key) == null){
      // New tone, that we don't track yet
      sum_freq.put(key, freq);
      count_freq.put(key, 1.0);
      var_freq.put(key, freq*freq);
      tracked_tones.addLast(key);
      num_keys += 1;
    }
    else{
      sum_freq.put(key, sum_freq.get(key)+freq);
      count_freq.put(key, count_freq.get(key)+1.0);
      var_freq.put(key, var_freq.get(key) + freq*freq);
    }
    return key;
  }

  public Map<String, Double> statistics(String key){
    Map<String, Double> stat = new HashMap<String, Double>();
    if (count_freq.get(key) == null){
      // Some unknown key is given
      stat.put("mean", -1.0);
      stat.put("std", 0.0);
      return stat;
    }
    double N = count_freq.get(key);
    double mean = sum_freq.get(key)/N;
    stat.put("mean", mean);
    stat.put("std", Math.sqrt(var_freq.get(key)/N - Math.pow(mean, 2)));
    return stat;
  }

  public int numTonesTracked(){return num_keys;};
}
