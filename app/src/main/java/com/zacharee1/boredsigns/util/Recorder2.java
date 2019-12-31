package com.zacharee1.boredsigns.util;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static android.media.MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED;

public class Recorder2 {
    public enum State { Initialising, Ready, Recording, Playing, PlayStopped }
    MediaRecorder mediaRecorder = null;
    MediaPlayer mediaPlayer = null;
    Timer timer = null;
    String AudioSavePathInDevice;

    private State state;

    public Recorder2() {
        state = State.Initialising;
        AudioSavePathInDevice =
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "AudioRecording.3gp";

        state = State.Ready;
    }

    public boolean record() {
        if (state == State.Initialising) return false;

        if (state == State.Ready || state == State.PlayStopped){
            return startRecording();
        }

        if (state == State.Recording){
            return stopRecording() && startRecording();
        }

        if (state == State.Playing){
            return stopPlaying() && startRecording();
        }

        return false;
    }

    public boolean play() {
        if (state == State.Initialising) return false;

        if (state == State.Ready){
            return startPlaying();
        }

        if (state == State.PlayStopped){
            return restartPlaying();
        }

        if (state == State.Recording){
            return stopRecording() && startPlaying();
        }

        if (state == State.Playing){
            return stopPlaying() && startPlaying();
        }

        return false;
    }

    public boolean stop() {
        if (state == State.Recording){
            return stopRecording();
        }

        if (state == State.Playing){
            return stopPlaying();
        }

        return true;
    }

    public void release(){
        if (mediaRecorder != null){
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }

        if (mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private boolean startRecording(){
        logDebug("startRecording");
        try {
            deleteLastFile();
            MediaRecorderReady();
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        state = State.Recording;
        return true;
    }

    private boolean stopRecording(){
        logDebug("stopRecording");
        try {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return false;
        }
        state = State.Ready;
        return true;
    }

    private boolean startPlaying(){
        logDebug("startPlaying");
        try {
            MediaPlayReady();
            mediaPlayer.setDataSource(AudioSavePathInDevice);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        TimerReady();
        state = State.Playing;
        return true;
    }

    private boolean restartPlaying(){
        logDebug("restartPlaying");
        try {
            if (mediaPlayer == null) {
                MediaPlayReady();
                mediaPlayer.setDataSource(AudioSavePathInDevice);
                mediaPlayer.prepare();
            }
            mediaPlayer.seekTo(0);
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        TimerReady();
        state = State.Playing;
        return true;
    }

    private boolean stopPlaying(){
        logDebug("stopPlaying");
        if(mediaPlayer != null){
            mediaPlayer.stop();
        }
        TimerReady();
        state = State.Ready;
        return true;
    }

    private void MediaRecorderReady(){
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setMaxDuration(1000*60);

        OnInfoListener listener = (mr, what, extra) -> {
            if (what == MEDIA_RECORDER_INFO_MAX_DURATION_REACHED){
                logDebug("Maximum record duration reached");
                state = State.Ready;
            }
        };

        mediaRecorder.setOnInfoListener(listener);
        mediaRecorder.setOutputFile(AudioSavePathInDevice);
    }

    private void MediaPlayReady(){
        if (mediaPlayer != null) {
            return;
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mp -> {
            state = State.PlayStopped;
        });
    }

    private boolean deleteLastFile() {
        File file = new File(AudioSavePathInDevice);

        if (file.exists()){
            if (mediaPlayer != null) {
                mediaPlayer.reset();
            }
            return file.delete();
        }

        return true;
    }

    private void TimerReady(){
        if (timer != null) return;

        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                logDebug("Recorder is idle, releasing resources");
                release();
                timer.cancel();
                timer = null;
            }
        };
        long interval = 1000 * 60;
        timer.scheduleAtFixedRate(task, interval, interval);
    }

    private void logDebug(String msg){
        Log.d("RecorderInfo", msg);
    };
}
