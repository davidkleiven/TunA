package com.github.tuna;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class Recorder extends Object{
  private AudioRecord ar = null;
  private int minSize;
  private int sample_rate = 44100;
  public short[] buffer = null;

  public void start(){
    minSize = AudioRecord.getMinBufferSize(sample_rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
    buffer = new short[sample_rate * 2];
    ar = new AudioRecord(MediaRecorder.AudioSource.MIC, sample_rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minSize);
  }

  public void stop(){
    if (ar != null){
      ar.release();
    }
    ar = null;
  }

  public void recordAudio(){
    if (ar == null)
    {
      start();
    }

    // Read data in a separate thread
    new Thread(new Runnable(){
      @Override
      public void run(){
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
        short min_buffer[] = new short[minSize];

        ar.startRecording();
        int num_samples_read = 0;
        while (num_samples_read < 2 * sample_rate)
        {
          int num_new_samples = ar.read(min_buffer, 0, minSize);

          for (int i=0;i<num_new_samples;i++){

            if (num_new_samples+i >= buffer.length){
              break;
            }
            buffer[num_samples_read+i] = min_buffer[i];
          }
          num_samples_read += num_new_samples;
        }

        ar.stop();
      }
    }).start();
  }
}