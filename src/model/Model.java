package model;

import control.home.HomeController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.stage.Stage;
import javafx.util.Duration;
import msg.SliderChangedVDuration;
import msg.VideoChangedVDuration;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;

public class Model extends Observable implements Serializable {
    public static final String START_WINDOW = "/view/start/Start.fxml";
    public static final String HOME_WINDOW = "/view/home/Home.fxml";

    float[][] magX;
    float[][] magY;
    float[][] magZ;

    String[] labels, certainties;
    String VPath, LPath;
    String VStart, LStart;
    transient Duration duration;
    Date start, clock;
    int SequenceLength;
    int delta;
    transient Media media;

    public int getDelta() {
        return delta;
    }
    public void setDelta(int delta) {
        this.delta = delta;
    }

    public void calculateDelta() {
        try {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
            Date date1 = format.parse(VStart);
            Date date2 = format.parse(LStart);
            delta = (int) ((date2.getTime() - date1.getTime()) / 1000); // Milliseconds -> Seconds
        } catch (ParseException e){
            System.out.println("Cannot parse time");
        }
    }

    public float[][] getMagX() {
        return magX;
    }

    public void setMagX(float[][] magX) {
        this.magX = magX;
    }

    public float[][] getMagY() {
        return magY;
    }

    public void setMagY(float[][] magY) {
        this.magY = magY;
    }

    public float[][] getMagZ() {
        return magZ;
    }

    public void setMagZ(float[][] magZ) {
        this.magZ = magZ;
    }

    public int getSequenceLength() {
        return SequenceLength;
    }

    public void setSequenceLength(int sequenceLength) {
        SequenceLength = sequenceLength;
    }

    public String[] getLabels() {
        return labels;
    }

    public void setLabels(String[] labels) {
        this.labels = labels;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public String[] getCertainties() {
        return certainties;
    }

    public void setCertainties(String[] certainties) {
        this.certainties = certainties;
    }

    public void setVPath(String VPath) {
        this.VPath = VPath;
        media = new Media(this.getVPath());
    }

    public void setLPath(String LPath) {
        this.LPath = LPath;
    }

    public void setVStart(String VStart) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        this.VStart = VStart;
        try {
            this.start = format.parse(VStart);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    public String getVPath() {
        return VPath;
    }

    public String getLPath() {
        return LPath;
    }

    public String getVStart() {
        return VStart;
    }


    public String getLStart() {
        return LStart;
    }


    public void setLStart(String LStart) {
        this.LStart = LStart;
    }


    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getClock() {
        return clock;
    }

    public void setClock(Date clock) {
        this.clock = clock;
    }

    public void openHome() {
        clock = new Date(0);

        Stage stage = new Stage();

        FXMLLoader loader = new FXMLLoader(Model.class.getResource(HOME_WINDOW));

        Parent root = null;
        try {
            root = loader.load();
            stage.setScene(new Scene(root));
            HomeController homeController = (HomeController) loader.getController();
            homeController.setModel(this);
            homeController.initMediaPlayer();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public synchronized void sliderSetVDuration(Duration newValue) {
        clock.setTime(start.getTime() + (int) (newValue.toSeconds()* 1000));
        this.duration = newValue;
        setChanged();
        notifyObservers(new SliderChangedVDuration(newValue));
    }

    public synchronized void videoSetVDuration(Duration newValue) {
        clock.setTime(start.getTime() + (int) (newValue.toSeconds()* 1000));
        this.duration = newValue;
        setChanged();
        notifyObservers(new VideoChangedVDuration(newValue));
    }
}
