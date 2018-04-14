package msg;

import javafx.util.Duration;

public class VideoChangedVDuration {
    Duration duration;

    public Duration getDuration() {
        return duration;
    }

    public VideoChangedVDuration(Duration newVal) {
        this.duration = newVal;
    }
}
