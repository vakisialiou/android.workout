package run.workout;

import android.location.Location;

import run.workout.lib.Pace;
import run.workout.entities.Point;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;


class Workout extends TimerTask {
    private static final int TIMER_INTERVAL = 1000;
    private int time = 0;
    private float distance = 0;
    private Runnable eventListener;
    private Timer timer = new Timer();
    private Boolean startActivity = false;
    private Boolean pauseActivity = false;
    private SimpleDateFormat formatter;
    private List<Point> points = new ArrayList<Point>();

    private int cachePaceCount = 0;
    private int cachePaceSum = 0;

    Workout() {
        this.formatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        this.formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Найти последнюю активную точку (которая не на паузе).
     *
     * @param index - числовое значение которое говорит какую активную точку с конца брать.
     *              Например: 1 - Взять первую активную точку с конца массива.
     *                        2 - Взять вторую активную точку с конца массива.
     * @return Point
     */
    private Point findActivePoint(int index) {
        int counterIndex = 0;
        int size = this.points.size() - 1;
        for (int i = size; i >= 0; i--) {
            Point point = this.points.get(i);
            if (point.isPause()) {
                continue;
            }
            counterIndex++;
            if (counterIndex == index) {
                return point;
            }
        }
        return null;
    }

    /**
     * Получение последней точки. Не зависимо от статуса.
     *
     * @return Point|null
     */
    private Point getLastPoint() {
        if (this.points.size() == 0) {
            return null;
        }

        return this.points.get(this.points.size() - 1);
    }

    String getDistance() {
        return "" +  this.distance;
    }

    void setLocation(Location location) {
        Point point = this.getLastPoint();
        if (point == null) {
            return;
        }

        point.setLocation(location);

        if (!this.pauseActivity) {
            // Последняя точка.
            Point lastPoint = this.findActivePoint(1);
            // Предпоследняя точка
            Point prevPoint = this.findActivePoint(2);
            if (lastPoint != null && prevPoint != null) {
                // Расчет растояния между последней и предпоследней точками.
                float distance = prevPoint.getLocation().distanceTo(lastPoint.getLocation());
                this.distance += distance;

                float time = lastPoint.getTimestamp() - prevPoint.getTimestamp();
                Float pace = Pace.calculatePace(time, distance);
                lastPoint.setPace(pace);
                this.cachePaceCount++;
                this.cachePaceSum += pace;
            }
        }

        this.eventListener.run();
    }

    /**
     *
     * @return int - средний темп всех точек.
     */
    String getAVGPace() {
        if (this.cachePaceCount == 0) {
            return "0";
        }
        return "" + (this.cachePaceSum / this.cachePaceCount);
    }

    /**
     *
     * @return - средний темп за последние 10 точек.
     */
    String getPace() {
        int sum = 0;
        int count = 0;
        int size = this.points.size() - 1;
        for (int i = size; i >= 0; i--) {
            Point point = this.points.get(i);
            if (point.isPause()) {
                continue;
            }

            float pace = point.getPace();
            if (pace == 0) {
                continue;
            }

            count++;
            sum += pace;
            if (count >= 10) {
                break;
            }
        }

        if (size == 0) {
            return "0";
        }
        return "" + (sum / size);
    }

    String getBPM() {
        Point point = this.getLastPoint();
        if (point == null) {
            return "0";
        }
        return  "" + point.getBPM();
    }

    /**
     * Время активности в виде строки: HH:mm:ss
     *
     * @return String|?
     */
    String timeAsString() {
        if (this.time > 0) {
            return this.formatter.format(new Date(this.time));
        }
        return this.formatter.format(new Date(0));
    }

    @Override
    public void run() {
        if (!this.startActivity) {
            return;
        }
        if (!this.pauseActivity) {
            this.time += TIMER_INTERVAL;
        }
        Point point = new Point();
        point.setPause(this.pauseActivity);
        point.setTimestamp(System.currentTimeMillis());
        this.points.add(point);
        this.eventListener.run();
    }

    /**
     *
     * @param action это действие будет выполняться прикаждом тике таймера при каждом добавлении местоположения пользователя.
     */
    void setPointEventListener(Runnable action) {
        this.eventListener = action;
    }

    /**
     * Начать тренировку.
     */
    void start() {
        this.startActivity = true;
        timer.scheduleAtFixedRate(this, TIMER_INTERVAL, TIMER_INTERVAL);
    }

    /**
     * Поставить тренировку на паузу.
     */
    void pause() {
        this.pauseActivity = true;
    }

    /**
     * Продолжить тренировку после паузы.
     */
    void next() {
        this.pauseActivity = false;
    }

    /**
     * Полная остоновка таймера.
     */
    void stop() {
        this.startActivity = false;
        this.pauseActivity = false;
        timer.cancel();
        timer.purge();
    }
}