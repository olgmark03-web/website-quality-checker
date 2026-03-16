import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MainControllerTests{

    private static final String TEST_HISTORY_FILE = "website_checker_history_test.txt";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private MainController controller;

    // Βοηθητική μέθοδος
    private String createHistoryEntry(String url, String analysis) {
        String timestamp = LocalDateTime.now().format(formatter);
        return String.format("[%s] %s\n%s\n%s\n",
                timestamp, url, analysis, "-".repeat(50));
    }

    @BeforeEach
    public void setUp() throws IOException {
        controller = new MainController();
        Files.deleteIfExists(Paths.get("website_checker_history.txt"));
    }

    @Test
    @Order(1)
    @DisplayName("1. Έλεγχος saveHistory")
    public void testSaveHistory() throws IOException {
        System.out.println("\n1. Έλεγχος saveHistory...");

        String entry = createHistoryEntry("https://example.com", "Test analysis");
        controller.saveHistory(entry);

        File file = new File("website_checker_history.txt");
        assertTrue(file.exists(), "Το αρχείο ιστορικού πρέπει να δημιουργηθεί");

        String content = Files.readString(file.toPath());
        assertTrue(content.contains("https://example.com"), "Πρέπει να περιέχει το URL");
        assertTrue(content.contains("Test analysis"), "Πρέπει να περιέχει την ανάλυση");
        assertTrue(content.contains("-".repeat(50)), "Πρέπει να περιέχει διαχωριστή");

        System.out.println("  ✓ saveHistory λειτουργεί σωστά");
    }

    @Test
    @Order(2)
    @DisplayName("2. Έλεγχος loadHistory")
    public void testLoadHistory() throws IOException {
        System.out.println("\n2. Έλεγχος loadHistory...");

        String entry1 = createHistoryEntry("https://site1.com", "First analysis");
        String entry2 = createHistoryEntry("https://site2.com", "Second analysis");

        controller.saveHistory(entry1);
        controller.saveHistory(entry2);

        controller.loadHistory();

        File file = new File("website_checker_history.txt");
        assertTrue(file.exists(), "Το αρχείο πρέπει να υπάρχει");

        String content = Files.readString(file.toPath());

        // Έλεγχοι περιεχομένου
        assertTrue(content.contains("site1.com"), "Πρέπει να περιέχει site1.com");
        assertTrue(content.contains("site2.com"), "Πρέπει να περιέχει site2.com");
        assertTrue(content.contains("First analysis"), "Πρέπει να περιέχει First analysis");
        assertTrue(content.contains("Second analysis"), "Πρέπει να περιέχει Second analysis");

        // Μέτρηση διαχωριστών
        long separatorCount = content.lines()
                .filter(line -> line.equals("-".repeat(50)))
                .count();
        assertEquals(2, separatorCount, "Πρέπει να έχει 2 διαχωριστές");

        System.out.println("  ✓ loadHistory λειτουργεί σωστά");
    }

    @Test
    @Order(3)
    @DisplayName("3. Έλεγχος loadHistory με ανύπαρκτο αρχείο")
    public void testLoadHistoryWithNoFile() {
        System.out.println("\n3. Έλεγχος loadHistory με ανύπαρκτο αρχείο...");
        controller.loadHistory();

        // Δεν υπάρχει αρχείο για έλεγχο, αλλά η μέθοδος δεν πρέπει να αποτύχει
        File file = new File("website_checker_history.txt");
        assertFalse(file.exists(), "Το αρχείο δεν πρέπει να υπάρχει");

        System.out.println("  ✓ loadHistory διαχειρίζεται σωστά ανύπαρκτο αρχείο");
    }

    @Test
    @Order(4)
    @DisplayName("4. Έλεγχος πολλαπλών αποθηκεύσεων")
    public void testMultipleSaves() throws IOException {
        System.out.println("\n4. Έλεγχος πολλαπλών αποθηκεύσεων...");

        for (int i = 1; i <= 3; i++) {
            String entry = createHistoryEntry(
                    "https://site" + i + ".com",
                    "Analysis " + i
            );
            controller.saveHistory(entry);
        }

        controller.loadHistory();

        File file = new File("website_checker_history.txt");
        String content = Files.readString(file.toPath());

        // Μέτρηση διαχωριστών
        long separatorCount = content.lines()
                .filter(line -> line.equals("-".repeat(50)))
                .count();
        assertEquals(3, separatorCount, "Πρέπει να έχει 3 διαχωριστές");

        // Έλεγχος URLs
        for (int i = 1; i <= 3; i++) {
            assertTrue(content.contains("site" + i + ".com"),
                    "Πρέπει να περιέχει site" + i + ".com");
        }

        System.out.println("  ✓ Πολλαπλές εγγραφές αποθηκεύονται σωστά");
    }

    @Test
    @Order(5)
    @DisplayName("5. Έλεγχος απόδοσης - 50 εγγραφές")
    public void testPerformance() throws IOException {
        System.out.println("\n5. Έλεγχος απόδοσης - 50 εγγραφές...");

        long startWrite = System.currentTimeMillis();

        for (int i = 1; i <= 50; i++) {
            String entry = createHistoryEntry(
                    "https://perftest" + i + ".com",
                    "Performance test " + i
            );
            controller.saveHistory(entry);

            if (i % 10 == 0) {
                System.out.println("    Αποθηκεύτηκαν " + i + " εγγραφές...");
            }
        }

        long writeTime = System.currentTimeMillis() - startWrite;

        long startRead = System.currentTimeMillis();
        controller.loadHistory();
        long readTime = System.currentTimeMillis() - startRead;

        // Έλεγχος μεγέθους αρχείου
        File file = new File("website_checker_history.txt");
        long fileSize = file.length();
        assertTrue(fileSize > 1000, "Το αρχείο πρέπει να είναι > 1KB, είναι: " + fileSize + " bytes");

        String content = Files.readString(file.toPath());
        long separatorCount = content.lines()
                .filter(line -> line.equals("-".repeat(50)))
                .count();

        assertEquals(50, separatorCount, "Πρέπει να έχει 50 εγγραφές");

        System.out.println("  ✓ 50 εγγραφές αποθηκεύτηκαν σε " + writeTime + "ms");
        System.out.println("  ✓ 50 εγγραφές φορτώθηκαν σε " + readTime + "ms");
        System.out.println("  ✓ Μέγεθος αρχείου: " + fileSize + " bytes");
    }

    @Test
    @Order(6)
    @DisplayName("6. Έλεγχος μορφής ιστορικού")
    public void testHistoryFormat() throws IOException {
        System.out.println("\n6. Έλεγχος μορφής ιστορικού...");

        String url = "https://example.com";
        String analysis = "Load Time: 1500ms\nSSL: Secure\nScore: 85/100";

        String entry = createHistoryEntry(url, analysis);
        controller.saveHistory(entry);

        File file = new File("website_checker_history.txt");
        String content = Files.readString(file.toPath());


        String[] lines = content.split("\n");

        assertTrue(lines[0].startsWith("["), "Πρώτη γραμμή πρέπει να ξεκινά με [");
        assertTrue(lines[0].contains("] " + url), "Πρώτη γραμμή πρέπει να περιέχει το URL");

        boolean foundAnalysis = false;
        for (int i = 1; i < lines.length - 1; i++) {
            if (lines[i].contains("Load Time: 1500ms")) {
                foundAnalysis = true;
                break;
            }
        }
        assertTrue(foundAnalysis, "Η ανάλυση πρέπει να υπάρχει στο αρχείο");
        assertTrue(content.contains("-".repeat(50)), "Πρέπει να υπάρχει διαχωριστής");

        System.out.println("  ✓ Η μορφή του ιστορικού είναι σωστή");
    }
}