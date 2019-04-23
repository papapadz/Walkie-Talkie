package com.example.murtaza.walkietalkie;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MicRecorder implements Runnable {
    private static final int SAMPLE_RATE = 16000;
    public static volatile boolean keepRecording = true;

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

        /*int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);*/


        /*if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = SAMPLE_RATE * 2;
        }*/
        int bufferSize = 1280;
        Log.e("AUDIO", "buffersize = "+bufferSize);

        bufferSize = 1000;

        try {
            final byte[] audioBuffer = new byte[bufferSize];

            AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize);

            if (record.getState() != AudioRecord.STATE_INITIALIZED) {
                Log.e("AUDIO", "Audio Record can't initialize!");
                return;
            }
            record.startRecording();
            Log.e("AUDIO", "STARTED RECORDING");

            final DatagramSocket socket = SocketHandler.getSocket();
            final InetAddress inetAddress = SocketHandler.getInetAddress();
            final int port = SocketHandler.getPORT();

            while(keepRecording) {
                int numberOfBytes = record.read(audioBuffer, 0, audioBuffer.length);
                Runnable writeToOutputStream = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DatagramPacket dp = new DatagramPacket(
                                    audioBuffer,
                                    audioBuffer.length,
                                    inetAddress,
                                    port
                                    );
                            socket.send(dp);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                Thread thread = new Thread(writeToOutputStream);
                thread.start();
            }

            record.stop();
            record.release();
//            outputStream.close();
            Log.e("AUDIO", "Streaming stopped");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
