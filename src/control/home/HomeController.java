package control.home;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import model.Model;
import msg.SliderChangedVDuration;
import msg.VideoChangedVDuration;

import java.text.SimpleDateFormat;
import java.util.Observable;
import java.util.Observer;

public class HomeController implements Observer {
    Model model;

    @FXML
    MediaView mvVideo;
    @FXML
    private Slider slSeek;
    @FXML
    private Label lblDuration;
    @FXML
    private Label lblClock;
    @FXML
    private Label lblLabel;
    @FXML
    private Label lblSoftmax;
    @FXML
    private Label lblIndex;

    private MediaPlayer mediaPlayer;

    public void setModel(Model model) {
        this.model = model;
        model.addObserver(this);
    }

    public void initMediaPlayer() {
        mediaPlayer = new MediaPlayer(model.getMedia());
        mvVideo.setMediaPlayer(mediaPlayer);

        DoubleProperty width = mvVideo.fitWidthProperty();
        DoubleProperty height = mvVideo.fitHeightProperty();

        width.bind(Bindings.selectDouble(mvVideo.sceneProperty(), "width"));
        height.bind(Bindings.selectDouble(mvVideo.sceneProperty(), "height"));

        mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                model.videoSetVDuration(newValue);
            }
        });

        slSeek.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                model.sliderSetVDuration(Duration.seconds(slSeek.getValue()));
            }
        });

        mediaPlayer.setOnReady(new Runnable() {
            @Override
            public void run() {
//                System.out.println("Duration: " + model.getMedia().getDuration().toSeconds());
                slSeek.setMax(model.getMedia().getDuration().toSeconds());
            }
        });
    }

    @FXML
    public void onPlay() {
        Platform.runLater(() -> mediaPlayer.play());
    }

    @FXML
    void onPause() {
        Platform.runLater(() -> mediaPlayer.pause());
    }


    private void updateUI(int seconds) {
        this.lblDuration.setText("" + seconds);

        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        this.lblClock.setText(format.format(model.getClock()));

        int currentLabelSecond = (int) model.getDuration().toSeconds() - model.getDelta();

        if (currentLabelSecond > 0) {
            int index = (int) currentLabelSecond / model.getSequenceLength();

            if (index >= model.getLabels().length){
                this.lblIndex.setText("Index: OOB");
                this.lblSoftmax.setText("UNKNOWN");
                this.lblLabel.setText("UNKNOWN");
                return;
            }

            String prediction = model.getLabels()[index];
            this.lblLabel.setText(prediction);
            if(prediction.contains("INSIDE")){
                this.lblLabel.setTextFill(Color.GREEN);
            } else {
                this.lblLabel.setTextFill(Color.RED);
            }
            this.lblSoftmax.setText(model.getCertainties()[index]);
            this.lblIndex.setText("Index: " + index);
        } else {
            this.lblIndex.setText("Index: OOB");
            this.lblSoftmax.setText("UNKNOWN");
            this.lblLabel.setText("UNKNOWN");
        }
    }


    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof SliderChangedVDuration) {
            SliderChangedVDuration msg = (SliderChangedVDuration) arg;
            Platform.runLater(() -> {
                this.mediaPlayer.seek(msg.getDuration());
                updateUI((int) msg.getDuration().toSeconds());
            });
        } else if (arg instanceof VideoChangedVDuration) {
            VideoChangedVDuration msg = (VideoChangedVDuration) arg;
            Platform.runLater(() -> {
                this.slSeek.setValue(msg.getDuration().toSeconds());
                updateUI((int) msg.getDuration().toSeconds());
            });
        }
    }
}
