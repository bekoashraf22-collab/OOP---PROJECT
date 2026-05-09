package GUI.CODE;

import GUI.Services.AsyncService;
import GUI.Services.AppSession;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HotelApp extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        primaryStage.setTitle("Luxora Hotel Management System");
        primaryStage.setMinWidth(1180);
        primaryStage.setMinHeight(720);
        show("/GUI/FXML/LoginView.fxml");
    }

    public static void show(String fxmlPath) {
    try {
        boolean wasMaximized = primaryStage.isMaximized();
        boolean wasFullScreen = primaryStage.isFullScreen();

        double currentWidth = primaryStage.getWidth();
        double currentHeight = primaryStage.getHeight();
        double currentX = primaryStage.getX();
        double currentY = primaryStage.getY();

        Parent root = FXMLLoader.load(HotelApp.class.getResource(fxmlPath));

        Scene scene = primaryStage.getScene();
        if (scene == null) {
            scene = new Scene(root, 1280, 760);
            String css = HotelApp.class.getResource("/GUI/CSS/hotel.css").toExternalForm();
            scene.getStylesheets().add(css);
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(root);
        }

        if (!scene.getStylesheets().contains(HotelApp.class.getResource("/GUI/CSS/hotel.css").toExternalForm())) {
            scene.getStylesheets().add(HotelApp.class.getResource("/GUI/CSS/hotel.css").toExternalForm());
        }

        primaryStage.setWidth(currentWidth);
        primaryStage.setHeight(currentHeight);
        primaryStage.setX(currentX);
        primaryStage.setY(currentY);
        primaryStage.setMaximized(wasMaximized);
        primaryStage.setFullScreen(wasFullScreen);

        primaryStage.show();
    } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Could not load " + fxmlPath, e);
    }
}

    public static void logout() {
        AppSession.clear();
        show("/GUI/FXML/LoginView.fxml");
    }

    @Override
    public void stop() {
        AsyncService.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
