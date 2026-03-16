import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.application.Platform;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// Κύριο controller για την εφαρμογή JavaFX
public class MainController {
    @FXML private TextField urlField; // Πεδίο εισαγωγής URL
    @FXML private TextArea output; // Περιοχή εξόδου για εμφάνιση αποτελεσμάτων
    @FXML private ProgressBar progressBar; // Μπάρα προόδου
    @FXML private StackPane historyPopup;
    @FXML private TextArea historyArea;

    private final List<String> history = new ArrayList<>(); // Λίστα για αποθήκευση ιστορικού αναλύσεων στη μνήμη
    private static final String HISTORY_FILE = "website_checker_history.txt"; // Αρχείο για μόνιμη αποθήκευση ιστορικού
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // Μορφή ημερομηνία + ώρα

    // Μέθοδος INITIALIZE - καλείται αυτόματα από το javaFX όταν φορτωθεί το fxml
    @FXML
    public void initialize() {
        loadHistory();
        // Απόκρυψη της μπάρας προόδου μέχρι να ξεκινήσει κάποια ανάλυση
        progressBar.setVisible(false);
    }

    // Μέθοδος ανάλυσης ιστοσελίδας
    @FXML
    public void analyze() {
        // Λήψη URL από το πεδίο εισαγωγής και αφαίρεση περιττών κενών
        String originalUrl = urlField.getText().trim();

        // Έλεγχος αν το πεδίο είναι κενό
        if (originalUrl.isEmpty()) {
            output.setText("Please enter a website URL.");
            return;
        }

        final String urlToAnalyze;
        //Έλεγχος και προσθήκη πρωτοκόλλου(http/https) αν λείπει
        if (!originalUrl.startsWith("http://") && !originalUrl.startsWith("https://")) {
            urlToAnalyze = "https://" + originalUrl;
            Platform.runLater(() -> urlField.setText(urlToAnalyze));
        } else {
            urlToAnalyze = originalUrl;
        }
        // Προετοιμασία διεπαφής για ανάλυση
        progressBar.setVisible(true);
        output.clear();
        output.setText("Analyzing " + urlToAnalyze + "...\n");

        // Δημιουργία και εκκίνηση νέου thread για να μην μπλοκάρεται η εκτέλεση
        new Thread(() -> {
            try {
                //Κλήση της μεθόδου ανάλυσης(WebsiteChecker.checkWebsite)
                String result = WebsiteChecker.checkWebsite(urlToAnalyze);

                //Δημιουργία χρονικής σήμανσης για το ιστορικό
                String timestamp = LocalDateTime.now().format(formatter);

                String historyEntry = String.format("[%s] %s\n%s\n%s\n",
                        timestamp, urlToAnalyze, result, "-".repeat(50));

                history.add(historyEntry);
                saveHistory(historyEntry);
                //Επιστροφή στο GUI thread για ενημέρωση της διεπαφής
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    output.setText(result);
                    showAlert(Alert.AlertType.INFORMATION, "Analysis Complete",
                            "Website analysis saved to history.");
                });

            } catch (Exception e) {
                //Χειρισμός σφαλμάτων στο GUI thread
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    output.setText("Error: " + e.getMessage() + "\n\nPlease check:\n" +
                            "1. You have internet connection\n" +
                            "2. The website URL is correct\n" +
                            "3. chromedriver.exe exists in project folder");
                });
            }
        }).start(); // Εκκίνηση του thread
    }

    //Μέθοδος εμφάνισης ιστορικού αναλύσεων
    @FXML
    public void showHistory() {
     loadHistoryToTextArea(); // Φόρτωσε το ιστορικό
        if (historyPopup != null) {
            historyPopup.setVisible(true);
            historyPopup.setManaged(true);
            historyPopup.toFront(); // Φέρε μπροστά το popup παράθυρο
        }
    }
    @FXML
    public void closeHistoryPopup() {
    if (historyPopup != null) {
        historyPopup.setVisible(false);
        historyPopup.setManaged(false);
        }
    }
    // Μέθοδος εξαγωγής των αποτελεσμάτων σε αρχείο
    @FXML
    public void exportResults() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Analysis Results");
        // Αυτόματο όνομα αρχείου με χρονική σήμανση
        fileChooser.setInitialFileName("website_analysis_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );

        File file = fileChooser.showSaveDialog(urlField.getScene().getWindow());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("Website Analysis Report");
                writer.println("Generated: " + LocalDateTime.now().format(formatter));
                writer.println("URL: " + urlField.getText());
                writer.println("\n" + "-".repeat(50) + "\n");
                writer.println(output.getText());

                showAlert(Alert.AlertType.INFORMATION, "Export Successful",
                        "Results saved to: " + file.getAbsolutePath());
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Export Failed",
                        "Could not save file: " + e.getMessage());
            }
        }
    }

    // Μέθοδος αποθήκευσης ιστορικού
    public void saveHistory(String entry) {
        try (FileWriter fw = new FileWriter(HISTORY_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(entry);
        } catch (IOException e) {
            System.err.println("Error saving history: " + e.getMessage());
        }
    }

    // Μέθοδος φόρτωσης ιστορικού από αρχείο στη μνήμη
    public void loadHistory() {
        File file = new File(HISTORY_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                StringBuilder entry = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    entry.append(line).append("\n");
                    if (line.contains("-".repeat(50))) {
                        history.add(entry.toString()); // Αποθήκευση ολόκληρης εισαγωγής
                        entry = new StringBuilder();//Για νέα εισαγωγή
                    }
                }
            } catch (IOException e) {
                System.err.println("Error loading history: " + e.getMessage());
            }
        }
    }

    // Μέθοδος εξαγωγής ολόκληρου του ιστορικού σε αρχείο
    @FXML
    private void exportHistoryToFile() {
         FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export History");
        fileChooser.setInitialFileName("analysis_history_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );

        // Εμφάνιση διαλογικού παραθύρου
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                //Γραφή κεφαλαίου και στατιστικών
                writer.println("--- Website Analysis History ---\n");
                writer.println("Generated: " + LocalDateTime.now().format(formatter));
                writer.println("Total entries: " + history.size());
                writer.println("\n" + "-".repeat(60) + "\n");

                // Γραφή κάθε εισαγωγής του ιστορικού
                for (String entry : history) {
                    writer.println(entry);
                }

                showAlert(Alert.AlertType.INFORMATION, "History Exported",
                        "History saved to: " + file.getAbsolutePath());
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Export Failed",
                        "Could not save history: " + e.getMessage());
            }
        }
    }
    private void loadHistoryToTextArea() {
        StringBuilder historyText = new StringBuilder();
        historyText.append("Website Analysis History\n");
        historyText.append("-".repeat(50)).append("\n\n");

        File file = new File(HISTORY_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    historyText.append(line).append("\n");
                }
            } catch (IOException e) {
                historyText.append("Error loading history.\n");
            }
        } else {
            historyText.append("No history found.\n");
        }

        if (historyArea != null) {
            historyArea.setText(historyText.toString());
        }
    }
    // Μέθοδος διαγραφής ολόκληρου του ιστορικού
    @FXML
    public void clearHistory() {

        //Δημιουργία παραθύρου για επιβεβαίωση
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear History");
        alert.setHeaderText("Are you sure you want to clear all history?");
        alert.setContentText("This action cannot be undone.");
        //Αναμονή απάντησης χρήστη και εκτέλεση εαν επιβεβαιωθεί
        if (alert.showAndWait().get() == ButtonType.OK) {
            history.clear();//καθαρισμός μνήμης
            File file = new File(HISTORY_FILE);
            if (file.exists()) {
                file.delete();//διαγραφή φυσικού αρχείου
            }
            historyArea.setText("History cleared successfully.");
            showAlert(Alert.AlertType.INFORMATION, "History Cleared",
                    "All history entries have been deleted.");
        }
    }

    //Μέθοδος για εμφάνιση ενημερωτικών μηνυμάτων
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
