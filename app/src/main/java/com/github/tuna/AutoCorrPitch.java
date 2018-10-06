package com.github.tuna;

import java.util.ArrayList;
import android.os.Handler;
import android.os.Message;

public class AutoCorrPitch extends Object{

    public double threshold = 0.1;
    public double fund_freq= 0.0;
    public int step_size = 20;
    private boolean idle = true;
    private double data[] = null;
    private int sampling_rate = 44100;
    public Handler handler = null;

    public double[] diffFunction(double buffer[]){
        double diff[] = new double[buffer.length/(2*step_size)];
        for (int lag=0;lag<diff.length;lag++){
            diff[lag] = 0.0;
            for (int i=0;i<buffer.length-lag*step_size;i++){
                diff[lag] += Math.pow(buffer[i] - buffer[i+lag*step_size], 2);
            }
        }
        return diff;
    }

    public double[] cummulativeMeanNormalized(double buffer[]){
        double diff[] = diffFunction(buffer);
        double normalized[] = new double[diff.length];
        normalized[0] = 1.0;
        double cumsum = diff[0];
        for (int i=1;i<diff.length;i++){
            cumsum += diff[i];
            normalized[i] = diff[i]/(cumsum/(i+1));
        }
        return normalized;
    }

    public int fundamentalPeriod(double buffer[]){
        double norm_diff[] = cummulativeMeanNormalized(buffer);
        ArrayList<Integer> minima = new ArrayList<Integer>();
        ArrayList<Double> values = new ArrayList<Double>();
        double minimum_peak_value = 10000.0;
        int minimum_peak = 0;
        for (int i=1;i<norm_diff.length-1;i++){
            if ((norm_diff[i-1] > norm_diff[i]) && (norm_diff[i+1] > norm_diff[i])){
                minima.add(i);
                values.add(norm_diff[i]);
                if (norm_diff[i] < minimum_peak){
                    minimum_peak_value = norm_diff[i];
                    minimum_peak = i;
                }
            }
        }

        // Extract the first minima smaller than the threhold
        for (int i=0;i<minima.size();i++){
            if (values.get(i) < threshold){
                return minima.get(i)*step_size;
            }
        }

        // No peaks lower than the threshold was found
        // return the global minima
        return minimum_peak*step_size;
    }

    private void copy2buffer(double buffer[]){
        if (data == null){
            data = new double[buffer.length];
        }
        else if (data.length != buffer.length){
            data = new double[buffer.length];
        }

        for (int i=0;i<buffer.length;i++){
            data[i] = buffer[i];
        }
    }

    public void fundamentalFreq(double buffer[], int srate){
        if (!idle){
            // Avoid starting a lot of threads
            return;
        }

        sampling_rate = srate;
        idle = false;
        copy2buffer(buffer);

        Thread thread = new Thread(
        new Runnable(){
            @Override
            public void run(){
                int fund_per = fundamentalPeriod(data);
                if (fund_per > 0){
                    fund_freq = sampling_rate/fund_per;
                }
                
                idle = true;

                Message msg = Message.obtain();
                msg.what = HandlerMessages.time_domain_freq_estimated;
                handler.sendMessage(msg);
            }
        }
        );
        thread.start();
    }
}