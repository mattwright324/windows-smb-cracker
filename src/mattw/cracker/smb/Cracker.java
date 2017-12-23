package mattw.cracker.smb;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class Cracker extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) {
        Label lbl = new Label("Address");
        lbl.setFont(Font.font("Tahoma", FontWeight.BOLD, 13));
        lbl.setTextFill(Color.WHITE);

        TextField address = new TextField();
        address.setPromptText("192.168.1.112");

        Label lbl1 = new Label("Usernames");
        lbl1.setFont(Font.font("Tahoma", FontWeight.SEMI_BOLD, 13));
        lbl1.setTextFill(Color.WHITE);

        Button chooseU = new Button("...");
        Label lblU = new Label("username-list.txt");
        lblU.setTextFill(Color.LIGHTGRAY);
        Label lblUC = new Label("");
        HBox hbox0 = new HBox(10);
        hbox0.setAlignment(Pos.CENTER_LEFT);
        hbox0.getChildren().addAll(chooseU,lblU, lblUC);

        Label lbl2 = new Label("Passwords");
        lbl2.setFont(Font.font("Tahoma", FontWeight.SEMI_BOLD, 13));
        lbl2.setTextFill(Color.WHITE);

        Button chooseP = new Button("...");
        Label lblP = new Label("password-list.txt");
        lblP.setTextFill(Color.LIGHTGRAY);
        Label lblPC = new Label("");
        HBox hbox1 = new HBox(10);
        hbox1.setAlignment(Pos.CENTER_LEFT);
        hbox1.getChildren().addAll(chooseP,lblP,lblPC);

        Label lbl3 = new Label("Threads");
        lbl3.setFont(Font.font("Tahoma", FontWeight.SEMI_BOLD, 13));
        lbl3.setTextFill(Color.WHITE);

        Label prog = new Label("0% complete");
        prog.setFont(Font.font("Tahoma", FontWeight.SEMI_BOLD, 13));
        prog.setTextFill(Color.LIGHTGRAY);

        Button start = new Button("Start");
        start.setMinWidth(100);
        start.setDisable(true);
        start.setStyle("-fx-base: ivory");

        HBox hbox2 = new HBox(20);
        hbox2.setAlignment(Pos.CENTER_RIGHT);
        hbox2.getChildren().addAll(prog, start);

        ColumnConstraints cc0 = new ColumnConstraints();
        cc0.setPrefWidth(100);
        ColumnConstraints cc1 = new ColumnConstraints();
        cc1.setHgrow(Priority.ALWAYS);

        GridPane grid = new GridPane();
        grid.getColumnConstraints().addAll(cc0,cc1);
        grid.setVgap(5);
        grid.setHgap(10);
        grid.setPadding(new Insets(8));
        grid.setAlignment(Pos.TOP_LEFT);
        grid.setStyle("-fx-background-color: #5588bb;");
        grid.addRow(0, lbl, address);
        grid.addRow(1, lbl1, hbox0);
        grid.addRow(2, lbl2, hbox1);
        grid.addRow(3, new Label(), hbox2);

        Label msg = new Label("No logins found");
        msg.setFont(Font.font("Tahoma", FontWeight.SEMI_BOLD, 14));

        ListView<Node> results = new ListView<>();
        results.setDisable(true);
        results.setMaxHeight(Double.MAX_VALUE);
        results.setStyle("-fx-stroke: red; -fx-stroke-width: 5px; -fx-stroke-dash-array: 12 4 6 4;");
        // results.setPadding(new Insets(8));
        results.getItems().add(msg);
        VBox.setVgrow(results, Priority.ALWAYS);

        Label tps = new Label("0 tps");
        tps.setTooltip(new Tooltip("Tries Per Second (TPS)"));
        tps.setTextFill(Color.GRAY);

        Label space = new Label("");
        space.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(space, Priority.ALWAYS);

        Hyperlink export = new Hyperlink("Save as CSV");
        export.setDisable(true);

        HBox hbox3 = new HBox(10);
        hbox3.setAlignment(Pos.CENTER_RIGHT);
        hbox3.setPadding(new Insets(5,10,5,10));
        hbox3.getChildren().addAll(tps,space,export);

        VBox vbox = new VBox();
        vbox.setFillWidth(true);
        vbox.getChildren().addAll(grid, results, hbox3);

        Scene scene = new Scene(vbox, 500, 400);
        stage.setTitle("Windows SMB Cracker");
        stage.setScene(scene);
        stage.getIcons().add(new Image("mattw/cracker/smb/img/icon2.png"));
        stage.setOnCloseRequest(we -> {
            Platform.exit();
            System.exit(0);
        });
        stage.show();
        chooseU.requestFocus();
    }

}