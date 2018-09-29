package com.github.tuna;

import android.os.Handler;
import android.os.Message;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class Recorder extends Object{
  private AudioRecord ar = null;
  private int minSize;
  public int sampling_rate = 44100;
  private int buffer_length = 8192;
  public double[] buffer = null;
  public boolean buffer_ready = false;
  public Handler handler = null;
  private ExponentialFilter low_pass = null;

  public void start(){
    if (handler == null){
      throw new IllegalArgumentException("Handler has not been set!");
    }
    minSize = AudioRecord.getMinBufferSize(sampling_rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

    buffer = new double[buffer_length];
    ar = new AudioRecord(MediaRecorder.AudioSource.MIC, sampling_rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minSize);

    if (ar.getState() != AudioRecord.STATE_INITIALIZED){
      ar.release();
      ar = null;
      throw new RuntimeException("Audio record was not initialized!");
    }
  }

  public void stop(){
    if (ar != null){
      ar.release();
    }
    ar = null;
    buffer_ready = false;
  }

  public void setLowPass(double cut_off_freq){
    low_pass = new ExponentialFilter();
    low_pass.setDamping(cut_off_freq/sampling_rate);
  }

  public void applyLowPassFilter(){
    if (low_pass == null){
      return;
    }

    for (int i=0;i<buffer.length;i++){
      low_pass.addSample(buffer[i]);
      buffer[i] = low_pass.get();
    }
  }

  public void recordAudio(){
    if (ar == null)
    {
      start();
    }
    buffer_ready = false;

    // Read data in a separate thread
    new Thread(new Runnable(){
      @Override
      public void run(){
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
        short min_buffer[] = new short[minSize];
        ar.startRecording();
        int num_samples_read = 0;
        while (num_samples_read < buffer.length)
        {
          int num_new_samples = ar.read(min_buffer, 0, min_buffer.length);

          if (num_new_samples < 0){
            throw new RuntimeException("Error during read occured!");
          }

          for (int i=0;i<num_new_samples;i++){

            if (num_samples_read+i < buffer.length){
              buffer[num_samples_read+i] = min_buffer[i];
            }
          }
          num_samples_read += num_new_samples;
        }

        stop();
        applyLowPassFilter();
        buffer_ready = true;
        Message msg = Message.obtain();
        msg.what = HandlerMessages.recording_finished;
        handler.sendMessage(msg);
      }
    }).start();
  }

  static int log2(int x){
    return (int)(Math.log(x)/Math.log(2));
  }
}
