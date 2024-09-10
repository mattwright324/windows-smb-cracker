package io.mattw.smbcracker;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PerSecondCounter extends Thread {

    private double seconds = 0;
    private long total = 0;
    private double avg = 0;

    private long count = 0;
    private long tps = 0;
    private long lastTps = System.currentTimeMillis();

    public void increment() {
        count++;
    }

    public void run() {
        while (isAlive()) {
            if (System.currentTimeMillis() - lastTps > 1000) {
                seconds += (System.currentTimeMillis() - lastTps) / 1000.0;

                tps = count;
                count = 0;
                lastTps = System.currentTimeMillis();

                total += tps;
                avg = total / seconds;
                if (seconds > 60) {
                    resetAverage();
                }
            }
            try {
                Thread.sleep(25);
            } catch (InterruptedException ignored) {
            }
        }
    }

    public void resetAverage() {
        seconds = 0;
        total = 0;
        avg = 0;
    }

}
