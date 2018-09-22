package com.github.tuna;

public class ComparableDatapoint implements Comparable<ComparableDatapoint>{
  public double x;
  public double y;

  public ComparableDatapoint(double newX, double newY){
    x = newX;
    y = newY;
  }

  @Override
  public int compareTo(ComparableDatapoint other){
    if (x < other.x){
      return -1;
    }
    else if ( x > other.x){
      return 1;
    }
    return 0;
  }
}
