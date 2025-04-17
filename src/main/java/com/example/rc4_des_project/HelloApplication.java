package com.example.rc4_des_project;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;

public class HelloApplication extends Application {

    private File selectedFileForEncryption;
    private File selectedFileForDecryption;
    private String originalFileSignature;
    private String decryptedFileSignature;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("File Encryption Tool");

        TabPane tabPane = new TabPane();
        Tab encryptTab = new Tab("Encrypt");
        Tab decryptTab = new Tab("Decrypt");
        tabPane.getTabs().addAll(encryptTab, decryptTab);

        encryptTab.setContent(createEncryptLayout(primaryStage));
        decryptTab.setContent(createDecryptLayout(primaryStage));

        VBox root = new VBox(10, tabPane);
        root.setPadding(new Insets(10));
        Scene scene = new Scene(root, 500, 400);

        scene.getStylesheets().add(getClass().getResource("/com/example/rc4_des_project/style.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createEncryptLayout(Stage primaryStage) {
        FileChooser fileChooserEncrypt = new FileChooser();
        Button fileButtonEncrypt = new Button("Select File for Encryption");
        Label fileLabelEncrypt = new Label("No file selected");
        Label signatureLabelEncrypt = new Label("Original File Signature: Not Available");

        fileButtonEncrypt.setOnAction(e -> {
            selectedFileForEncryption = fileChooserEncrypt.showOpenDialog(primaryStage);
            if (selectedFileForEncryption != null) {
                fileLabelEncrypt.setText("Selected File: " + selectedFileForEncryption.getName());
                try {
                    originalFileSignature = generateFileSignature(selectedFileForEncryption);
                    signatureLabelEncrypt.setText("Original File Signature: " + originalFileSignature);
                } catch (Exception ex) {
                    showAlert("Error", "Failed to generate file signature: " + ex.getMessage());
                }
            }
        });

        Button encryptButton = new Button("Encrypt and Save");
        encryptButton.setOnAction(e -> {
            if (selectedFileForEncryption != null) {
                createPasswordAlert(password -> {
                    try {
                        String keyPassword = "MohammadKhanafsa";
                        String encryptedKey = DES.encryptPassword(password, keyPassword);

                        RC4.encryptFile(selectedFileForEncryption, encryptedKey);

                        showAlert("Encryption Successful", "File has been encrypted and saved.");
                    } catch (Exception ex) {
                        showAlert("Error", "Failed to encrypt file: " + ex.getMessage());
                    }
                });
            } else {
                showAlert("No File Selected", "Please select a file to encrypt.");
            }
        });

        VBox layout = new VBox(10, fileButtonEncrypt, fileLabelEncrypt, signatureLabelEncrypt, encryptButton);
        layout.setPadding(new Insets(10));
        return layout;
    }

    private VBox createDecryptLayout(Stage primaryStage) {
        FileChooser fileChooserDecrypt = new FileChooser();
        Button fileButtonDecrypt = new Button("Select File for Decryption");
        Label fileLabelDecrypt = new Label("No file selected");
        Label signatureLabelDecrypt = new Label("Decrypted File Signature: Not Available");

        fileButtonDecrypt.setOnAction(e -> {
            selectedFileForDecryption = fileChooserDecrypt.showOpenDialog(primaryStage);
            if (selectedFileForDecryption != null) {
                fileLabelDecrypt.setText("Selected File: " + selectedFileForDecryption.getName());
            }
        });

        Button decryptButton = new Button("Decrypt and Save");
        decryptButton.setOnAction(e -> {
            if (selectedFileForDecryption != null) {
                createPasswordAlert(password -> {
                    try {
                        String keyPassword = "MohammadKhanafsa";
                        String encryptedKey = DES.encryptPassword(password, keyPassword);

                        RC4.decryptFile(selectedFileForDecryption, encryptedKey);

                        File decryptedFile = new File(selectedFileForDecryption.getParent(),
                                "decrypted_" + selectedFileForDecryption.getName());
                        decryptedFileSignature = generateFileSignature(decryptedFile);
                        signatureLabelDecrypt.setText("Decrypted File Signature: " + decryptedFileSignature);

                        compareSignatures(originalFileSignature, decryptedFileSignature);

                        showAlert("Decryption Successful", "File has been decrypted and saved.");
                    } catch (Exception ex) {
                        showAlert("Error", "Failed to decrypt file: " + ex.getMessage());
                    }
                });
            } else {
                showAlert("No File Selected", "Please select a file to decrypt.");
            }
        });

        VBox layout = new VBox(10, fileButtonDecrypt, fileLabelDecrypt, signatureLabelDecrypt, decryptButton);
        layout.setPadding(new Insets(10));
        return layout;
    }

    private void compareSignatures(String originalSignature, String decryptedSignature) {
        if (originalSignature == null || decryptedSignature == null) {
            showAlert("Signature Comparison", "Signatures are not available for comparison.");
            return;
        }

        if (originalSignature.equals(decryptedSignature)) {
            showAlert("Signature Match", "The decrypted file matches the original file.");
        } else {
            showAlert("Signature Mismatch", "The decrypted file does not match the original file.");
        }
    }

    private String generateFileSignature(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        Path filePath = Paths.get(file.getAbsolutePath());
        byte[] fileBytes = Files.readAllBytes(filePath);
        byte[] hashBytes = digest.digest(fileBytes);
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private void createPasswordAlert(PasswordHandler handler) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Enter Password");
        dialog.setHeaderText("Password Required");
        dialog.setContentText("Please enter a password:");
        dialog.showAndWait().ifPresent(handler::handle);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FunctionalInterface
    interface PasswordHandler {
        void handle(String password);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
