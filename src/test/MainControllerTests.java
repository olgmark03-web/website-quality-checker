import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MainControllerTests {


    // Κοινές μέθοδοι με τον MainController

    private static final String TEST_HISTORY_FILE = "website_checker_history_test.txt";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String processUrlLikeController(String url) {
        if (url == null || url.trim().isEmpty()) {
            return url;
        }

        String originalUrl = url.trim();
        if (!originalUrl.startsWith("http://") && !originalUrl.startsWith("https://")) {
            return "https://" + originalUrl;
        }
        return originalUrl;
    }

    private String createHistoryEntry(String url, String analysis) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        return String.format("[%s] %s\n%s\n%s\n",
                timestamp, url, analysis, "-".repeat(50));
    }

    private void saveHistoryEntry(String entry, String filename) throws IOException {
        try (FileWriter fw = new FileWriter(filename, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.print(entry);
        }
    }

    private List<String> loadHistory(String filename){
        List<String> history = new ArrayList<>();
        File file = new File(filename);

        if (!file.exists()) {
            return history;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder entry = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                entry.append(line).append("\n");
                if (line.contains("-----")) {
                    history.add(entry.toString());
                    entry = new StringBuilder();
                }
            }
            if (!entry.isEmpty()) {
                history.add(entry.toString());
            }
        }catch (IOException e) {
            System.err.println("Error loading history: " + e.getMessage());
        }
        return history;
    }

    @BeforeEach
    public void setUp() throws IOException {
        Files.deleteIfExists(Paths.get(TEST_HISTORY_FILE));
    }

    @Test
    @Order(1)
    @DisplayName("1. Έλεγχος URL επεξεργασίας")
    public void testUrlProcessing() {
        System.out.println("\n1. Έλεγχος URL επεξεργασίας...");

        String[][] testCases = {
                {"example.com", "https://example.com", "URL χωρίς πρωτόκολλο"},
                {"http://example.com", "http://example.com", "URL με http://"},
                {"https://example.com", "https://example.com", "URL με https://"},
                {"www.example.com", "https://www.example.com", "www domain"},
                {"example.com/path", "https://example.com/path", "URL με path"},
                {"sub.example.com:8080", "https://sub.example.com:8080", "URL με port"},
                {"", "", "Κενό URL"},
                {"   ", "   ", "Μόνο κενά"},
        };

        int passed = 0;
        for (String[] testCase : testCases) {
            String input = testCase[0];
            String expected = testCase[1];
            String description = testCase[2];

            String result = processUrlLikeController(input);

            if (expected.equals(result)) {
                System.out.println(description + ": " +
                        (input.isEmpty() ? "(κενό)" : input) + " → " +
                        (result.isEmpty() ? "(κενό)" : result));
                passed++;
            } else {
                System.out.println(description + ": " + input +
                        " → " + result + " (αναμενόμενο: " + expected + ")");
            }
        }

        assertEquals(testCases.length, passed,
                "Όλες οι URL δοκιμές πρέπει να περάσουν");

        System.out.println("Έλεγχος URL επεξεργασίας ΕΠΙΤΥΧΙΑ!!");
    }

    @Test
    @Order(2)
    @DisplayName("2. Έλεγχος αποθήκευσης ιστορικού")
    public void testHistorySaving() throws IOException {
        System.out.println("\n2. Έλεγχος αποθήκευσης ιστορικού...");

        // 1. Απλή αποθήκευση
        String simpleEntry = createHistoryEntry(
                "https://example.com",
                "Simple test analysis\nScore: 85/100"
        );

        saveHistoryEntry(simpleEntry, TEST_HISTORY_FILE);

        assertTrue(Files.exists(Paths.get(TEST_HISTORY_FILE)),
                "Το αρχείο ιστορικού πρέπει να δημιουργηθεί");

        // 2. Έλεγχος περιεχομένου
        String content = Files.readString(Paths.get(TEST_HISTORY_FILE));
        assertTrue(content.contains("https://example.com"), "Πρέπει να περιέχει URL");
        assertTrue(content.contains("Simple test analysis"), "Πρέπει να περιέχει ανάλυση");
        assertTrue(content.contains("-".repeat(50)), "Πρέπει να περιέχει διαχωριστή");


        long originalSize = Files.size(Paths.get(TEST_HISTORY_FILE));

        String secondEntry = createHistoryEntry(
                "https://test2.com",
                "Second analysis\nScore: 90/100"
        );

        saveHistoryEntry(secondEntry, TEST_HISTORY_FILE);

        long newSize = Files.size(Paths.get(TEST_HISTORY_FILE));
        assertTrue(newSize > originalSize,
                "Το αρχείο πρέπει να μεγαλώσει μετά τη δεύτερη εγγραφή");


        String updatedContent = Files.readString(Paths.get(TEST_HISTORY_FILE));
        assertTrue(updatedContent.contains("example.com") && updatedContent.contains("test2.com"),
                "Πρέπει να περιέχει και τις δύο εγγραφές");

        long separatorCount = updatedContent.lines()
                .filter(line -> line.equals("-".repeat(50)))
                .count();

        assertEquals(2, separatorCount, "Πρέπει να έχει 2 διαχωριστές για 2 εγγραφές");

        System.out.println("Έλεγχος αποθήκευσης ιστορικού ΕΠΙΤΥΧΙΑ!!");
    }


    @Test
    @Order(3)
    @DisplayName("3. Έλεγχος ανάγνωσης ιστορικού")
    public void testHistoryLoading() throws IOException {
        System.out.println("\n3. Έλεγχος ανάγνωσης ιστορικού...");

        // Δημιουργία τεστ αρχείου με 3 εγγραφές
        try (PrintWriter writer = new PrintWriter(new FileWriter(TEST_HISTORY_FILE, true))) {
            for (int i = 1; i <= 3; i++) {
                String entry = createHistoryEntry(
                        String.format("https://site%d.com", i),
                        String.format("Analysis for site %d\nScore: %d/100", i, 70 + i*5)
                );
                writer.print(entry);
                System.out.println("  Προστέθηκε εγγραφή " + i);
            }
        }

        List<String> history = loadHistory(TEST_HISTORY_FILE);


        assertEquals(3, history.size(), "Πρέπει να φορτώσει 3 εγγραφές");

        for (int i = 0; i < 3; i++) {
            String entry = history.get(i);

            assertTrue(entry.contains(String.format("site%d.com", i + 1)),
                    "Εγγραφή " + (i+1) + " πρέπει να περιέχει σωστό URL");

            assertTrue(entry.contains(String.format("Score: %d/100", 70 + (i+1)*5)),
                    "Εγγραφή " + (i+1) + " πρέπει να περιέχει σωστό σκορ");

            assertTrue(entry.contains("-".repeat(5)),
                    "Εγγραφή " + (i+1) + " πρέπει να περιέχει διαχωριστή");

            assertTrue(entry.startsWith("["),
                    "Εγγραφή " + (i+1) + " πρέπει να ξεκινάει με [");

            System.out.println("  Εγγραφή " + (i+1) + " φορτώθηκε σωστά");
        }

        for (String entry : history) {
            String firstLine = entry.split("\n")[0];
            String timestamp = firstLine.substring(
                    firstLine.indexOf('[') + 1,
                    firstLine.indexOf(']')
            );

            assertTrue(timestamp.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"),
                    "Χρονική σήμανση πρέπει να είναι σε σωστή μορφή: " + timestamp);
        }

        System.out.println("Έλεγχος ανάγνωσης ιστορικού ΕΠΙΤΥΧΙΑ!!");
    }

    @Test
    @Order(4)
    @DisplayName("4. Έλεγχος πραγματικής μορφής ιστορικού")
    public void testRealHistoryFormat() throws IOException {
        System.out.println("\n4. Έλεγχος πραγματικής μορφής ιστορικού...");

        // Βεβαιώνουμε ότι το αρχείο είναι ΚΕΝΟ
        Files.deleteIfExists(Paths.get(TEST_HISTORY_FILE));
        assertFalse(Files.exists(Paths.get(TEST_HISTORY_FILE)),
                "Το αρχείο πρέπει να μην υπάρχει πριν το test");

        String realUrl = "https://chicago.medicine.uic.edu/pathology/";
        String realAnalysis =
                "=== WEBSITE ANALYSIS REPORT ===\n\n" +
                        "URL: " + realUrl + "\n" +
                        "Title: Department of Pathology | University of Illinois College of Medicine\n" +
                        "Load Time: 1941 ms\n" +
                        "Performance: Excellent\n" +
                        "SSL: Secure\n" +
                        "SEO Title: Too Long (68 chars)\n" +
                        "Broken Links (checked 4 of 83): 0\n\n" +
                        "BASIC SCORE: 95/100\n" +
                        "BASIC RATING: EXCELLENT\n\n" +
                        "--------------------------------------------------\n\n" +
                        "GDPR COMPLIANCE CHECK\n" +
                        "Privacy Policy: 15/25\n" +
                        "Cookies Consent: 25/25\n" +
                        "Data Processing Info: 0/20\n" +
                        "User Rights Info: 0/20\n" +
                        "Contact Information: 5/10\n\n" +
                        "----------------------------------------\n" +
                        "GDPR TOTAL SCORE: 45/100\n" +
                        "GDPR RATING: FAIR\n\n" +
                        "--------------------------------------------------\n" +
                        "FINAL COMBINED SCORE: 75/100\n" +
                        "FINAL RATING: GOOD";

        // Δημιουργία και αποθήκευση εισόδου
        String historyEntry = createHistoryEntry(realUrl, realAnalysis);
        saveHistoryEntry(historyEntry, TEST_HISTORY_FILE);

        // Έλεγχος ότι το αρχείο δημιουργήθηκε
        assertTrue(Files.exists(Paths.get(TEST_HISTORY_FILE)),
                "Το αρχείο πρέπει να δημιουργηθεί");

        String fileContent = Files.readString(Paths.get(TEST_HISTORY_FILE));


        assertTrue(fileContent.contains("["), "Πρέπει να περιέχει [ για χρονική σήμανση");
        assertTrue(fileContent.contains("] " + realUrl), "Πρέπει να περιέχει URL με χρονική σήμανση");
        assertTrue(fileContent.contains("WEBSITE ANALYSIS REPORT"),
                "Πρέπει να περιέχει τίτλο αναφοράς");
        assertTrue(fileContent.contains("BASIC SCORE: 95/100"),
                "Πρέπει να περιέχει βασικό σκορ");
        assertTrue(fileContent.contains("GDPR COMPLIANCE CHECK"),
                "Πρέπει να περιέχει GDPR τμήμα");
        assertTrue(fileContent.contains("FINAL COMBINED SCORE: 75/100"),
                "Πρέπει να περιέχει τελικό σκορ");
        assertTrue(fileContent.contains("-".repeat(50)),
                "Πρέπει να περιέχει διαχωριστή 50 παυλών");

        String[] lines = fileContent.split("\n");
        assertTrue(lines.length > 20, "Το αρχείο πρέπει να έχει τουλάχιστον 20 γραμμές");

        System.out.println("Έλεγχος πραγματικής μορφής ιστορικού ΕΠΙΤΥΧΙΑ!!");
    }

    @Test
    @Order(5)
    @DisplayName("5. Έλεγχος ολοκληρωμένης ροής εργασίας")
    public void testCompleteWorkflow() throws IOException {
        System.out.println("\n5. Έλεγχος ολοκληρωμένης ροής εργασίας...");

        Files.deleteIfExists(Paths.get(TEST_HISTORY_FILE));

        System.out.println("   Προσομοίωση 4 αναλύσεων ιστοσελίδων...");

        // Προσομοίωση 4 αναλύσεων
        String[] websites = {
                "example.com",
                "http://http-example.com",
                "https://secure-example.com",
                "www.example.com"
        };

        for (int i = 0; i < websites.length; i++) {
            String originalUrl = websites[i];

            String processedUrl = processUrlLikeController(originalUrl);

            // 2. Προσομοίωση ανάλυσης
            String analysis = String.format(
                    "=== WEBSITE ANALYSIS REPORT ===\n" +
                            "URL: %s\n" +
                            "Title: Website %d\n" +
                            "Load Time: %d ms\n" +
                            "Performance: %s\n" +
                            "SSL: %s\n\n" +
                            "BASIC SCORE:  %d/100\n" +
                            "BASIC RATING:  %s\n",
                    processedUrl,
                    i + 1,
                    1000 + i * 200,
                    i == 3 ? "Excellent" : i == 2 ? "Good" : "Fair",
                    processedUrl.startsWith("https") ? "Secure" : "Not Secure",
                    70 + i * 8,
                    i == 3 ? "EXCELLENT" : i == 2 ? "GOOD" : "FAIR"
            );

            String historyEntry = createHistoryEntry(processedUrl, analysis);
            saveHistoryEntry(historyEntry, TEST_HISTORY_FILE);

            System.out.println("    Αναλύθηκε: " + originalUrl +
                    " → " + processedUrl + " (Score: " + (70 + i * 8) + ")");
        }

        assertTrue(Files.exists(Paths.get(TEST_HISTORY_FILE)),
                "Το αρχείο πρέπει να δημιουργηθεί");

        long fileSize = Files.size(Paths.get(TEST_HISTORY_FILE));
        assertTrue(fileSize > 500,
                "Το αρχείο πρέπει να είναι αρκετά μεγάλο");

        String content = Files.readString(Paths.get(TEST_HISTORY_FILE));
        assertTrue(content.contains("example.com"), "Πρέπει να περιέχει example.com");
        assertTrue(content.contains("http-example.com"), "Πρέπει να περιέχει http-example.com");
        assertTrue(content.contains("secure-example.com"), "Πρέπει να περιέχει secure-example.com");
        assertTrue(content.contains("www.example.com"), "Πρέπει να περιέχει www.example.com");


        long separatorCount = content.lines()
                .filter(line -> line.equals("-".repeat(50)))
                .count();

        assertEquals(4, separatorCount, "Πρέπει να έχει 4 διαχωριστές για 4 εγγραφές");

        System.out.println(" Έλεγχος ολοκληρωμένης ροής εργασίας ΕΠΙΤΥΧΙΑ!!");
    }

    @Test
    @Order(6)
    @DisplayName("6. Έλεγχος διαχείρισης λαθών")
    public void testErrorHandling() throws IOException {
        System.out.println("\n6.Έλεγχος διαχείρισης λαθών...");

        // 1. Ανάγνωση από αρχείο που δεν υπάρχει
        List<String> emptyHistory = loadHistory("non_existent_file_12345.txt");
        assertTrue(emptyHistory.isEmpty(),
                "Ανάγνωση από ανύπαρκτο αρχείο πρέπει να επιστρέφει κενή λίστα");

        // 2. Έλεγχος κενών URLs
        assertEquals("", processUrlLikeController(""), "Κενό URL πρέπει να επιστρέφεται κενό");
        assertEquals("   ", processUrlLikeController("   "), "Μόνο κενά πρέπει να επιστρέφονται ως έχουν");

        // 3. Δημιουργία αρχείου με κακή μορφή χωρίς διαχωριστή
        File badFormatFile = new File("bad_format_test.txt");
        try (PrintWriter writer = new PrintWriter(badFormatFile)) {
            writer.println("Κακή μορφοποίηση χωρίς διαχωριστή");
        }

        List<String> badHistory = loadHistory("bad_format_test.txt");
        assertEquals(1, badHistory.size(),
                "Αρχείο χωρίς διαχωριστή πρέπει να φορτώνεται ως μια εγγραφή");

        // 4. Έλεγχος με κενό αρχείο
        File emptyFile = new File("empty_test.txt");
        emptyFile.createNewFile();

        List<String> fromEmptyFile = loadHistory("empty_test.txt");
        assertTrue(fromEmptyFile.isEmpty(),
                "Κενό αρχείο πρέπει να επιστρέφει κενή λίστα");

        badFormatFile.delete();
        emptyFile.delete();

        System.out.println(" Έλεγχος διαχείρισης λαθών ΕΠΙΤΥΧΙΑ!!");
    }


    @Test
    @Order(7)
    @DisplayName("7. Έλεγχος απόδοσης και επεκτασιμότητας")
    public void testPerformanceAndScalability() throws IOException {
        System.out.println("\n7. Έλεγχος απόδοσης και επεκτασιμότητας...");

        Files.deleteIfExists(Paths.get(TEST_HISTORY_FILE));

        System.out.println("Δημιουργία μεγάλου ιστορικού με 50 εγγραφών...");

        // Δημιουργία 50 εγγραφών
        for (int i = 1; i <= 50; i++) {
            String url = String.format("https://performance-test-%03d.com", i);
            String analysis = String.format(
                    "Performance test entry %d\n" +
                            "Generated for scalability testing\n" +
                            "Score: %d/100\n" +
                            "Timestamp: %s",
                    i, 50 + (i % 50), LocalDateTime.now().format(TIMESTAMP_FORMATTER)
            );

            String entry = createHistoryEntry(url, analysis);
            saveHistoryEntry(entry, TEST_HISTORY_FILE);

            if (i % 10 == 0) {
                System.out.println(" Δημιουργήθηκαν " + i + " εγγραφές...");
            }
        }

        String fileContent = Files.readString(Paths.get(TEST_HISTORY_FILE));
        long fileSize = Files.size(Paths.get(TEST_HISTORY_FILE));

        // Έλεγχος ότι το αρχείο δημιουργήθηκε και έχει περιεχόμενο
        assertTrue(Files.exists(Paths.get(TEST_HISTORY_FILE)),
                "Το αρχείο πρέπει να δημιουργηθεί");
        assertTrue(fileSize > 1000,
                "Το αρχείο πρέπει να είναι αρκετά μεγάλο (>1KB), τώρα είναι: " + fileSize + " bytes");

        // Καταμέτρηση εγγραφών από το αρχείο (με βάση τους διαχωριστές)
        long separatorCount = fileContent.lines()
                .filter(line -> line.equals("-".repeat(50)))
                .count();

        assertEquals(50, separatorCount,
                "Πρέπει να έχει 50 διαχωριστές");

        for (int i = 1; i <= 50; i++) {
            String expectedUrl = String.format("performance-test-%03d.com", i);
            assertTrue(fileContent.contains(expectedUrl),
                    "Πρέπει να περιέχει URL: " + expectedUrl);
        }

        System.out.println(" Έλεγχος απόδοσης και επεκτασιμότητας ΕΠΙΤΥΧΙΑ!!");

    }
}