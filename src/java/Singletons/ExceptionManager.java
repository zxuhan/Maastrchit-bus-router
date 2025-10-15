package src.java.Singletons;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class ExceptionManager {
    public static void showError(String title, String headerText, String content, AlertType alertType) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(headerText);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    public static void showInstantError(String title, String headerText, String content, AlertType alertType) {
        Alert alert = new Alert(alertType, content);
        alert.showAndWait();
    }
}
