    package com.example.comport;

    import com.example.ports.Serialports;
    import javafx.animation.PauseTransition;
    import javafx.application.Platform;
    import javafx.collections.FXCollections;
    import javafx.collections.ObservableList;
    import javafx.concurrent.Service;
    import javafx.concurrent.Task;
    import javafx.fxml.FXML;
    import javafx.scene.control.*;
    import javafx.scene.input.KeyCode;
    import javafx.scene.input.KeyEvent;
    import javafx.scene.input.MouseEvent;
    import javafx.scene.web.WebView;
    import javafx.util.Duration;

    import java.io.IOException;
    import java.io.UnsupportedEncodingException;
    import java.util.Arrays;
    import java.util.Random;
    import java.util.concurrent.Executors;
    import java.util.concurrent.LinkedBlockingDeque;
    import java.util.concurrent.ScheduledExecutorService;
    import java.util.concurrent.TimeUnit;

    import static java.lang.Thread.currentThread;


    public class HelloController {

        private final Serialports serialports = new Serialports();
        @FXML
        private TextArea textArea = new TextArea();
        @FXML
        private TextArea textArea1 = new TextArea();
        @FXML
        private Label baudRate;
        @FXML
        private Label bytes = new Label();
        @FXML
        private ComboBox<String> choiceComPort = new ComboBox<>();
        @FXML
        private ComboBox<Number> stopBits = new ComboBox<>();
        @FXML
        private WebView webView;
        @FXML
        private Label fcs;
        @FXML
        private Label countCD;


        private final Alert alert = new Alert(Alert.AlertType.ERROR);


        @FXML
        public void initialize() {
            PauseTransition pause = new PauseTransition();
            ObservableList<String> observableList = FXCollections.observableList(serialports.getList());
            stopBits.setItems(list());
            stopBits.setValue(1);
            choiceComPort.setItems(observableList);
            choiceComPort.setValue(serialports.getSerialPort().getSystemPortName().replace("COM", ""));
            choiceComPort.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (!serialports.OpenPort("COM" + choiceComPort.getValue())) {
                    choiceComPort.setValue(serialports.getSerialPort().getSystemPortName().replace("COM", ""));
                    alert.setContentText("Port is already open");
                    alert.show();
                } else {
                    textArea.clear();
                    textArea1.clear();
                    bytes.setText(serialports.getCounterByte());
                }
            });
            textArea1.setEditable(false);
            StringBuilder str = new StringBuilder();
            bytes.setText(serialports.getCounterByte());
            baudRate.setText(serialports.getBaudRate());


            Position();

                    textArea.textProperty().addListener((obs, oldText, newText) -> {
                        countCD.setText("");
                        if (newText.length() >= oldText.length() ) {
                            str.append(newText.substring(oldText.length()));
                            try {
                                if (str.toString().getBytes("windows-1251").length == 7) {

                                    int count=0;
                                        String result;
                                        while (true) {
                                            result = "<p style='font-style:regular;font-size:12'>" + serialports.sendStringToComm(str.toString(),count).replace("\n", "/n") + "</p>";
                                            if (!serialports.getLock() && !serialports.getCD()) {
                                                break;
                                            } else {
                                                count++;
                                                textArea.setEditable(false);
                                                pause.setDuration(Duration.seconds(getDuration(count)));
                                                pause.setOnFinished(e -> textArea.setEditable(true));
                                                pause.play();
                                                //str.delete(0, str.length());
                                            }
                                        }
                                        int i =0;
                                        while (count!=i) {
                                            countCD.setText(countCD.getText()+"c");
                                            i++;
                                        }
                                        System.out.println(count);
                                    result = result.replace("$&", "<html><font color='red'>$&</font></html>");
                                    result = result.replace("$!", "<html><font color='red'>$!</font></html>");
                                    webView.getEngine().loadContent(result);
                                    str.delete(0, str.length());

                                }
                            } catch (IOException | InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
            ScheduledExecutorService scheduledService = Executors.newSingleThreadScheduledExecutor();
            Runnable task = () -> {
                if (serialports.getOutput() != null) {
                    Platform.runLater(() -> {bytes.setText(serialports.getCounterByte());
                        if(!serialports.getFcs())
                            fcs.setText("*");
                        else
                            fcs.setText("");
                    });

                    textArea1.setText(textArea1.getText() + serialports.getOutput());
                    textArea1.positionCaret(textArea1.getText().length());
                    serialports.setOutput();
                }
            };
            scheduledService.scheduleAtFixedRate(task, 0, 1, TimeUnit.MILLISECONDS);

        }

        private void Position() {
            textArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.UP ||
                        event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.BACK_SPACE) {
                    event.consume();
                }
            });
            textArea.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> textArea.positionCaret(textArea.getText().length()));
        }

        private ObservableList<Number> list() {
            return FXCollections.observableList(Arrays.asList(new Number[]{1, 1.5, 2}));
        }
        private double getDuration(int count){
            Random random = new Random(System.currentTimeMillis());
            return random.nextInt((int)(Math.pow(2,count)-1));
        }

    }




