package control.start;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import model.Model;

import java.io.*;
import java.util.ArrayList;

public class StartController {
    private Model model;

    @FXML
    TextField tfVStart;
    @FXML
    TextField tfLStart;
    @FXML
    TextField tfSeqLength;
    @FXML
    CheckBox cbVideo;
    @FXML
    CheckBox cbLabel;

    @FXML
    public void onStart() {
        setModelParams();
        tfVStart.getScene().getWindow().hide();
        model.openHome();
    }

    @FXML
    public void onRestore() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.setInitialDirectory(new File("models"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Model Files", "*.mdl"));
        File selectedFile = fileChooser.showOpenDialog(tfVStart.getScene().getWindow());
        if (selectedFile != null) {
            Model temp = loadModel(selectedFile.getName());

            if (model == null)
                return;
            this.model.setVPath(temp.getVPath());
            this.model.setLabels(temp.getLabels());
            this.model.setCertainties(temp.getCertainties());
            this.model.setSequenceLength(temp.getSequenceLength());
            this.model.setVStart(temp.getVStart());
            this.model.setLStart(temp.getVStart());
            this.model.calculateDelta();

            tfVStart.getScene().getWindow().hide();
            model.openHome();
        }
    }

    private Model loadModel(String name) {
        FileInputStream fs;
        ObjectInputStream is = null;
        try {
            fs = new FileInputStream("models/" + name);
            is = new ObjectInputStream(fs);
            return (Model) is.readObject();
        } catch (ClassNotFoundException e) {
            System.err.println(e.toString());
        } catch (IOException e) {
            System.err.println(e.toString());
        } catch (Throwable e) {
            System.err.println(e.toString());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }


    @FXML
    public void onSaveAndStart() {
        setModelParams();
        if (saveModel(this.model)) {
            tfVStart.getScene().getWindow().hide();
            model.openHome();
        }
    }

    boolean saveModel(Model model) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.setInitialDirectory(new File("models"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Model Files", "*.mdl"));
        File selectedFile = fileChooser.showSaveDialog(tfVStart.getScene().getWindow());

        if (selectedFile != null) {
            try {
                FileOutputStream fs = new FileOutputStream("models/" + selectedFile.getName());
                ObjectOutputStream os = new ObjectOutputStream(fs);
                os.writeObject(model);
                os.close();
                return true;
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Model konnte nicht serialisiert werden!");
                alert.show();
                return false;
            }
        }
        return false;
    }

    public void setModelParams() {
        model.setVStart(tfVStart.getText());
        model.setLStart(tfLStart.getText());
        if (!tfSeqLength.getText().equals(""))
            model.setSequenceLength(Integer.parseInt(tfSeqLength.getText()) / 100); // Convert from datapoints to seconds (100 Hz)
        model.calculateDelta();
    }

    @FXML
    public void onVideoLoad() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Choose MP4-File", "*.mp4"));
        File file = fc.showOpenDialog(tfVStart.getScene().getWindow());
        if (file != null) {
            model.setVPath(file.toURI().toString());
            this.cbVideo.setSelected(true);
        }
    }

    @FXML
    public void onLabelLoad() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Choose TXT-File", "*.txt"));
        File file = fc.showOpenDialog(tfVStart.getScene().getWindow());

        if (file == null)
            return;

        ArrayList<String> lines = getLines(file);

        model.setLabels(getLabels(lines));
        model.setCertainties(getCertainties(lines));

        this.cbLabel.setSelected(true);
    }

    public ArrayList<String> getLines(File file) {
        ArrayList<String> lines = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            try {
                String line = br.readLine();

                while (line != null) {
                    lines.add(line);
                    line = br.readLine();
                }
            } finally {
                br.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lines;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public String[] getLabels(ArrayList<String> lines) {
        ArrayList<String> temp = new ArrayList<>();

        for (String line : lines) {
            if (line.contains("Result")) {
                String[] parts = line.split(":");
                temp.add(parts[1].substring(1));
            }
        }

        String[] out = temp.toArray(new String[temp.size()]);

        return out;
    }

    public String[] getCertainties(ArrayList<String> lines) {
        ArrayList<String> temp = new ArrayList<>();

        for (String line : lines) {
            if (line.contains("Softmax")) {
                String[] parts = line.split(":");
                temp.add(parts[2]);
            }
        }

        String[] out = temp.toArray(new String[temp.size()]);

        return out;
    }
}
