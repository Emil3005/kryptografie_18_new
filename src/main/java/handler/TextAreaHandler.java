package handler;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.util.logging.LogRecord;

import static javafx.application.Platform.*;

public class TextAreaHandler extends java.util.logging.Handler {

    private TextArea textArea;

    public TextAreaHandler (TextArea textArea){
        this.textArea = textArea;
    }

    @Override
    public void publish(LogRecord record) {
        runLater(() -> {textArea.appendText(record.getMessage()); textArea.appendText("\n");});
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {

    }
}
