package msg;

import javafx.util.Duration;

public class SliderChangedVDuration {
    Duration duration;

    public Duration getDuration() {
        return duration;
    }

    public SliderChangedVDuration(Duration newVal) {
        this.duration = newVal;
    }
}
