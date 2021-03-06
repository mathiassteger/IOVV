package control.home;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
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
    private Label lblIndex;
    @FXML
    private VBox vbBelowVideo;

    private MediaPlayer mediaPlayer;
    private XYChart.Data<Number, Number> verticalMarker;
    private LineChartWithMarkers<Number, Number> lineChart;
    private XYChart.Series seriesX;
    private XYChart.Series seriesY;
    private XYChart.Series seriesZ;

    private XYChart.Data[][] seriesXData;
    private XYChart.Data[][] seriesYData;
    private XYChart.Data[][] seriesZData;

    private XYChart.Series[] magXSeriesArray;
    private XYChart.Series[] magYSeriesArray;
    private XYChart.Series[] magZSeriesArray;

    private int currChartIndex = -1;
    private int currentSecond = -1;

    public void setModel(Model model) {
        this.model = model;
        model.addObserver(this);
        lblDuration.setStyle("-fx-background-color: white");
        lblClock.setStyle("-fx-background-color: white");
        lblLabel.setStyle("-fx-background-color: white");
        lblIndex.setStyle("-fx-background-color: white");
        verticalMarker = new XYChart.Data<>(0, 0);
        makeChart();
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


    private void updateUI(double seconds) {
        this.lblDuration.setText("" + (int) seconds);

        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        this.lblClock.setText(format.format(model.getClock()));

        int currentLabelSecond = (int) model.getDuration().toSeconds() - model.getDelta();

        if (currentLabelSecond > 0) {
            int index = (int) currentLabelSecond / model.getSequenceLength();

            if (index >= model.getLabels().length) {
                this.lblIndex.setText("Index: OOB");
                this.lblLabel.setText("UNKNOWN");
                return;
            }

            String prediction = model.getLabels()[index].equals("0") ? "Inside" : "Outside";
            this.lblLabel.setText(prediction);
            if (prediction.contains("0")) {
                this.lblLabel.setTextFill(Color.GREEN);
            } else {
                this.lblLabel.setTextFill(Color.RED);
            }
            this.lblIndex.setText("Index: " + index);
            if (currentSecond != (int) seconds) {
                verticalMarker.setXValue(((seconds-model.getDelta()) * 100) % (model.getSequenceLength() * 100));
                currentSecond = (int) seconds;
            }
            fillChart(index);
        } else {
            this.lblIndex.setText("Index: OOB");
            this.lblLabel.setText("UNKNOWN");
        }
    }

    public void makeChart() {

        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        lineChart = new LineChartWithMarkers<>(xAxis, yAxis);
        seriesX = new XYChart.Series();
        seriesY = new XYChart.Series();
        seriesZ = new XYChart.Series();

        seriesXData = new XYChart.Data[model.getMagX().length][model.getSequenceLength() * 100];
        seriesYData = new XYChart.Data[model.getMagY().length][model.getSequenceLength() * 100];
        seriesZData = new XYChart.Data[model.getMagZ().length][model.getSequenceLength() * 100];

        this.magXSeriesArray = new XYChart.Series[model.getMagX().length];
        this.magYSeriesArray = new XYChart.Series[model.getMagX().length];
        this.magZSeriesArray = new XYChart.Series[model.getMagX().length];

        for (int i = 0; i < model.getMagX().length; i++) {
            for (int j = 0; j < model.getMagX()[i].length; j++) {
                seriesXData[i][j] = new XYChart.Data(j, model.getMagX()[i][j]);
                seriesYData[i][j] = new XYChart.Data(j, model.getMagY()[i][j]);
                seriesZData[i][j] = new XYChart.Data(j, model.getMagZ()[i][j]);
            }

            this.magXSeriesArray[i] = new XYChart.Series();
            this.magXSeriesArray[i].getData().addAll(seriesXData[i]);
            this.magYSeriesArray[i] = new XYChart.Series();
            this.magYSeriesArray[i].getData().addAll(seriesYData[i]);
            this.magZSeriesArray[i] = new XYChart.Series();
            this.magZSeriesArray[i].getData().addAll(seriesZData[i]);
        }

        lineChart.addVerticalValueMarker(verticalMarker);
        lineChart.setAnimated(false);
        lineChart.setCreateSymbols(false);
        lineChart.setStyle("-fx-stroke-width: 2px;");
        vbBelowVideo.getChildren().add(lineChart);

    }

    void fillChart(int index) {

        if (index != currChartIndex) {
            lineChart.getData().clear();

            lineChart.getData().addAll(this.magXSeriesArray[index], this.magYSeriesArray[index], this.magZSeriesArray[index]);

            currChartIndex = index;
        }

    }


    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof SliderChangedVDuration) {
            SliderChangedVDuration msg = (SliderChangedVDuration) arg;
            Platform.runLater(() -> {
                this.mediaPlayer.seek(msg.getDuration());
                updateUI(msg.getDuration().toSeconds());
            });
        } else if (arg instanceof VideoChangedVDuration) {
            VideoChangedVDuration msg = (VideoChangedVDuration) arg;
            Platform.runLater(() -> {
                this.slSeek.setValue(msg.getDuration().toSeconds());
                updateUI(msg.getDuration().toSeconds());
            });
        }
    }
}
