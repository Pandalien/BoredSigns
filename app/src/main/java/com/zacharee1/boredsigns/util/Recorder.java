package com.zacharee1.boredsigns.util;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import java.io.File;

public class Recorder {
    MediaRecorder mediaRecorder = null;
    MediaPlayer mediaPlayer = null;
    String AudioSavePathInDevice;


    public Recorder() {
        AudioSavePathInDevice =
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "AudioRecording.3gp";
    }

    public boolean record() {
        stop();
        return startRecording();
    }

    public boolean play() {
        stop();
        return startPlaying();
    }

    public void stop() {
        releaseRecorder();
        releasePlayer();
    }

    private void releaseRecorder(){
        try{
            if (mediaRecorder != null){
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
            }
        } catch (Exception e){
            Log.e("Recorder", "Failed to release recorder", e);
        }
    }

    private void releasePlayer(){
        try{
            if (mediaPlayer != null){
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Exception e){
            Log.e("Recorder", "Failed to release player", e);
        }
    }

    private boolean startRecording(){
        logDebug("startRecording");
        try {
            deleteLastFile();
            MediaRecorderReady();
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean startPlaying(){
        logDebug("startPlaying");
        try {
            File file = new File(AudioSavePathInDevice);
            if (!file.exists()){
                logDebug("Last record doesn't exist");
                return false;
            }
            MediaPlayReady();
            mediaPlayer.setDataSource(AudioSavePathInDevice);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void MediaRecorderReady(){
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setMaxDuration(1000*60);
        mediaRecorder.setOutputFile(AudioSavePathInDevice);
    }

    private void MediaPlayReady(){
        if (mediaPlayer != null) {
            return;
        }
        mediaPlayer = new MediaPlayer();
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

    private void logDebug(String msg){
        Log.d("RecorderInfo", msg);
    }
}
