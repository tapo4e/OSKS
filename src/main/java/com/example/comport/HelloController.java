package com.example.comport;

import com.example.ports.Serialports;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class HelloController  {

    private final Serialports serialports = new Serialports();
    @FXML
    private TextArea textArea=new TextArea();
    @FXML
    private TextArea textArea1 = new TextArea();
    @FXML
    private Label baudRate;
    @FXML
    private Label bytes=new Label();
    @FXML
    private ComboBox<String> choiceComPort=new ComboBox<>();
    @FXML
    private ComboBox<Number> stopBits = new ComboBox<>();

    private final Alert alert=new Alert(Alert.AlertType.ERROR);



    @FXML
    public void initialize() {
        ObservableList<String> observableList= FXCollections.observableList(serialports.getList());
        stopBits.setItems(list());
        stopBits.setValue(1);
        choiceComPort.setItems(observableList);
        choiceComPort.setValue(serialports.getSerialPort().getSystemPortName().replace("COM",""));
        choiceComPort.getSelectionModel().selectedItemProperty().addListener((observable,oldValue,newValue) -> {if(!serialports.OpenPort("COM"+ choiceComPort.getValue())) {
            choiceComPort.setValue(serialports.getSerialPort().getSystemPortName().replace("COM",""));
            alert.setContentText("Port is already open");
            alert.show();
        }
        else {
            textArea.clear();
            textArea1.clear();
            bytes.setText(serialports.getCounterByte());
        }
        });
        textArea1.setEditable(false);
        bytes.setText(serialports.getCounterByte());
        baudRate.setText(serialports.getBaudRate());
        Position();
        textArea.textProperty().addListener((obs, oldText, newText) -> {if(newText.length()>=oldText.length()) {
            try {
                serialports.sendStringToComm(newText.substring(oldText.length()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        });
        ScheduledExecutorService scheduledService = Executors.newSingleThreadScheduledExecutor();
        Runnable task = () -> {
            if (serialports.getOutput() != null) {
                Platform.runLater(() -> bytes.setText(serialports.getCounterByte()));
                textArea1.setText(textArea1.getText() + serialports.getOutput());
                textArea1.positionCaret(textArea1.getText().length());
                serialports.setOutput();

            }
        };
        scheduledService.scheduleAtFixedRate(task, 0, 1, TimeUnit.MILLISECONDS);
    }

    private void Position(){
        textArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.UP ||
                    event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.DOWN || event.getCode()== KeyCode.BACK_SPACE) {
                event.consume();
            }
        });
        textArea.addEventFilter(MouseEvent.MOUSE_CLICKED,event-> textArea.positionCaret(textArea.getText().length()));
    }

    private ObservableList<Number> list(){
        return FXCollections.observableList(Arrays.asList(new Number[]{1,1.5,2}));
    }

}



