package com.github.tuna;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;


public class FundamentalFreqEstimator extends Object{
  public double threshold = 0.0;

  public List<Integer> extractPeaks(double data[]){
    List<Integer> peaks = new ArrayList<Integer>();
    for (int i=1;i<data.length-1;i++){
      if (data[i] > threshold){
        if ((data[i] > data[i-1]) && (data[i] > data[i+1])){
          peaks.add(i);
        }
      }
    }
    return peaks;
  }

  public double estimateFundamental(double data[], double group_threshold){
    List<Integer> peaks = extractPeaks(data);

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

    double median = 0.0;
    if (differences.length % 2 == 0){
      median = (differences[differences.length/2] + differences[differences.length/2 - 1])/2;
    }
    else{
      median = differences[differences.length/2];
    }
    return median;
  }
}
