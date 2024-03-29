package run.workout.entities;

import android.location.Location;

public class Point {

    private static int counter = 0;
    private int id;
    private int bpm;
    private Long timestamp;
    private Location location;
    private Boolean pause = false;

    public Point() {
        this.id = ++counter;
        this.setTimestamp(System.currentTimeMillis());
    }

    public int getId() {
        return this.id;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return this.location;
    }

    public boolean hasLocation() {
        return this.location != null;
    }

    public void setTimestamp(Long value) {
        this.timestamp = value;
    }

    public Long getTimestamp() {
        return this.timestamp;
    }

    public Boolean isPause() {
        return this.pause;
    }

    public void setPause(Boolean value) {
        this.pause = value;
    }

    public void setBPM(int value) {
        this.bpm = value;
    }

    public int getBPM() {
        return this.bpm;
    }
}