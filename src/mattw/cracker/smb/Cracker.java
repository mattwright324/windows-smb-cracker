package mattw.cracker.smb;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Cracker extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) {

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        Scene scene = new Scene(vbox, 400, 400);
        stage.setTitle("Windows Cracker");
        stage.setScene(scene);
        stage.setOnCloseRequest(we -> {
            Platform.exit();
            System.exit(0);
        });
        stage.show();
    }

}