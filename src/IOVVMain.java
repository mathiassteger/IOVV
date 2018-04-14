import control.start.StartController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Model;

public class IOVVMain extends Application {
    public static final Model MODEL = new Model();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(IOVVMain.class.getResource(MODEL.START_WINDOW));

        Parent root = loader.load();
        stage.setScene(new Scene(root));
        StartController startController = (StartController) loader.getController();
        startController.setModel(MODEL);
        stage.show();
    }
}
