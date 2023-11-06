package Pintu;

import javafx.application.Platform;
import javafx.scene.control.Label;

public class pTimer {
    Label label;
    public pTimer(Label label){
        this.label = label;
    }

    private class MyTimer extends Thread {
        @Override
        public void run() {

            while (isRunning) {
                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - startTime;

                int h = (int) (elapsedTime / (60 * 60 * 1000));
                int m = (int) ((elapsedTime / (60 * 1000)) % 60);
                int s = (int) ((elapsedTime / 1000) % 60);

                String formatTime = String.format("使用时间: %02d:%02d:%02d",h,m,s);

                //java.lang.IllegalStateException: Not on FX application thread;
                Platform.runLater(() -> label.setText(formatTime));


                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }

    private long startTime = 0;
    private long pauseTime = 0;

    private boolean isRunning = false;


    public void clearTimer() {
        isRunning = false;
        startTime = 0;
        pauseTime = 0;
        label.setText("使用时间: 00:00:00");
        RefreshTimer();
    }

    public void startTimer() {
        if (!isRunning) {
            isRunning = true;

            if (startTime == 0) {
                startTime = System.currentTimeMillis();
            } else {
                startTime += System.currentTimeMillis() - pauseTime;
            }

            RefreshTimer();
        }
    }

    private void RefreshTimer() {
        MyTimer myTimer = new MyTimer();
        myTimer.start();
    }

}

