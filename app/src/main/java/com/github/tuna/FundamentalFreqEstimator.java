package com.github.tuna;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;


public class FundamentalFreqEstimator extends Object{
  public double threshold = 0.0;
  public List<Integer> peaks = new ArrayList<Integer>();

  public List<Integer> extractPeaks(double data[]){
    peaks = new ArrayList<Integer>();
    ExponentialFilter filter = new ExponentialFilter();
    filter.setDamping(0.1);
    filter.addSample(data[0]);
    for (int i=1;i<data.length-1;i++){
      if (data[i] > threshold+filter.get()){
        if ((data[i] > data[i-1]) && (data[i] > data[i+1])){
          peaks.add(i);
        }
      }
      filter.addSample(data[i]);
    }
    return peaks;
  }

  public double estimateFundamental(double data[], double group_threshold){
    peaks = extractPeaks(data);

    // Group the peaks in group where the maximum spread is given by
    // group threshold
    List<Double> group_sums = new ArrayList<Double>();
    List<Integer> num_in_group = new ArrayList<Integer>();
    List<Double> group_means = new ArrayList<Double>();
    for (int p : peaks){
      boolean found_group = false;
      for (int i=0;i<group_means.size();i++){
        if (Math.abs(p-group_means.get(i)) < group_threshold){
          num_in_group.set(i, num_in_group.get(i)+1);
          group_sums.set(i, group_sums.get(i)+p);
          group_means.set(i, group_sums.get(i)/num_in_group.get(i));
          found_group = true;
          break;
        }
      }

      if (!found_group){
        // The peak did not fit into any group. We create a new group
        group_sums.add((double) p);
        num_in_group.add(1);
        group_means.add((double) p);
      }
    }

    if (group_means.size() == 0){
      return 0.0;
    }
    else if (group_means.size() == 1){
      return group_means.get(0);
    }

    // As the peaks are returned in order the group means are also sorted
    double differences[] = new double[group_means.size()-1];
    for (int i=1;i<group_means.size();i++){
      differences[i-1] = group_means.get(i) - group_means.get(i-1);
    }

    // Find the median difference
    Arrays.sort(differences);

    // Use the middle third to estimate
    int start = differences.length/3;
    int end = 2*differences.length/3;
    double mean = 0.0;
    int counter = 0;
    for (int i=start;i<end+1;i++){
      mean += differences[i];
      counter += 1;
    }
    return mean/counter;
  }
}
