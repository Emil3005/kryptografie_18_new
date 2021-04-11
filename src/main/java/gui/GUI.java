package gui;

import config.Config;
import handler.TextAreaHandler;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import cryption.parser.Parser;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


public class GUI extends Application {
    public void start(Stage primaryStage) {
        primaryStage.setTitle("MSA | Mergentheim/Mosbach Security Agency");

        HBox hBox = new HBox();
        hBox.setPadding(new Insets(15, 12, 15, 12));
        hBox.setSpacing(10);
        hBox.setStyle("-fx-background-color: #336699;");

        Button executeButton = new Button("Execute");
        executeButton.setPrefSize(100, 20);

        Button closeButton = new Button("Close");
        closeButton.setPrefSize(100, 20);

        TextArea commandLineArea = new TextArea();
        commandLineArea.setWrapText(true);

        TextArea outputArea = new TextArea();
        outputArea.setWrapText(true);
        outputArea.setEditable(false);

        Config.instance.textArea.addHandler(new TextAreaHandler(outputArea));

        executeButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                execute(commandLineArea.getText());
                commandLineArea.clear();
            }
        });

        closeButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent actionEvent) {
                System.out.println("[close] pressed");
                System.exit(0);
            }
        });

        hBox.getChildren().addAll(executeButton, closeButton);

        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(25, 25, 25, 25));
        vbox.getChildren().addAll(hBox, commandLineArea, outputArea);

        Scene scene = new Scene(vbox, 950, 500);
        primaryStage.setScene(scene);
        primaryStage.show();

        scene.setOnKeyPressed(new EventHandler<KeyEvent>()
        {
            public void handle(final KeyEvent keyEvent)
            {
                if(keyEvent.getCode() == KeyCode.F3){
                    Config.instance.loggingHandler.switchHandlerForLogging();
                } else if(keyEvent.getCode() == KeyCode.F8) {
                    loadLogfile(outputArea);
                } else if(keyEvent.getCode().equals(KeyCode.F5)){
                    execute(commandLineArea.getText());
                    commandLineArea.clear();
                }
            }
        });
    }

    private void execute(String command){
        Parser.evaluateCommand(command);
    }

    private void loadLogfile(TextArea logArea){
        List<String> lines = new ArrayList<>();
        BufferedReader br;
        try {

            File logDir = new File(Config.instance.logDir);
            var files = logDir.listFiles();
            assert files != null;
            var latest = Arrays.stream(files).max(Comparator.comparing(f -> f.lastModified()));
            assert latest.isPresent();
            br = new BufferedReader(new FileReader(latest.get()));
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            br.close();

        } catch (FileNotFoundException e) {
            Config.instance.textArea.info("no logfile present");
            return;
        } catch (Exception e) {
            Config.instance.textArea.info("problems occurred while loading log file");
            return;
        }

        for (String line : lines){
            logArea.appendText(line);
            logArea.appendText("\n");
        }

    }
}