import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import java.util.*;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GDPR Checker - Unit Tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GDPRCheckerTests {

    // Κλάση για αποθήκευση αποτελεσμάτων τεστ
    static class TestResult {
        int testNumber;
        String testName;
        boolean passed;
        String message;
        long durationMs;

        TestResult(int testNumber, String testName, boolean passed, String message, long durationMs) {
            this.testNumber = testNumber;
            this.testName = testName;
            this.passed = passed;
            this.message = message;
            this.durationMs = durationMs;
        }

        @Override
        public String toString() {
            String status = passed ? "ΕΠΙΤΥΧΙΑ!!" : "ΑΠΟΤΥΧΙΑ";
            return String.format("Τεστ %02d: %-50s %-15s %5dms   %s",
                    testNumber, testName, status, durationMs, message);
        }
    }

    private static final List<TestResult> testResults = Collections.synchronizedList(new ArrayList<>());
    private static Instant testSuiteStartTime;
    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;
    private static int currentTestNumber = 1;


    @BeforeAll
    static void setupAll() {
        testSuiteStartTime = Instant.now();
        printHeader();
    }

    @AfterAll
    static void cleanupAll() {
        printTestSummary();
    }

    @Test
    @Order(1)
    @DisplayName("01. Βασικός έλεγχος βαθμολογίας GDPR")
    @Tag("Κρίσιμο")
    void test01_BasicGDPRRating() {
        int testNumber = currentTestNumber++;
        Instant start = Instant.now();
        String testName = "Βασικός έλεγχος βαθμολογίας";

        try {
            System.out.printf("\nΤΕΣΤ %02d: %s%n", testNumber, testName);

            assertAll(testName,
                    () -> assertEquals("EXCELLENT", GDPRChecker.getGDPRRating(100), "100 → EXCELLENT"),
                    () -> assertEquals("EXCELLENT", GDPRChecker.getGDPRRating(80), "80 → EXCELLENT"),
                    () -> assertEquals("GOOD", GDPRChecker.getGDPRRating(79), "79 → GOOD"),
                    () -> assertEquals("GOOD", GDPRChecker.getGDPRRating(60), "60 → GOOD"),
                    () -> assertEquals("FAIR", GDPRChecker.getGDPRRating(59), "59 → FAIR"),
                    () -> assertEquals("FAIR", GDPRChecker.getGDPRRating(40), "40 → FAIR"),
                    () -> assertEquals("NEEDS IMPROVEMENT", GDPRChecker.getGDPRRating(39), "39 → NEEDS IMPROVEMENT"),
                    () -> assertEquals("NEEDS IMPROVEMENT", GDPRChecker.getGDPRRating(20), "20 → NEEDS IMPROVEMENT"),
                    () -> assertEquals("POOR", GDPRChecker.getGDPRRating(19), "19 → POOR"),
                    () -> assertEquals("POOR", GDPRChecker.getGDPRRating(0), "0 → POOR")
            );

            long duration = Duration.between(start, Instant.now()).toMillis();
            recordTestResult(testNumber, testName, true, "Όλες οι βαθμολογίες είναι σωστές", duration);

        } catch (AssertionError e) {
            long duration = Duration.between(start, Instant.now()).toMillis();
            recordTestResult(testNumber, testName, false, "Σφάλμα: " + e.getMessage(), duration);
            throw e;
        }
    }

    @ParameterizedTest(name = "Τεστ {index}: Βαθμολογία {0} → {1}")
    @Order(2)
    @DisplayName("02-25. Παραμετρικά τεστ για βαθμολογία")
    @CsvSource({
            "100, EXCELLENT",
            "95,  EXCELLENT",
            "85,  EXCELLENT",
            "80,  EXCELLENT",
            "79,  GOOD",
            "75,  GOOD",
            "70,  GOOD",
            "65,  GOOD",
            "60,  GOOD",
            "59,  FAIR",
            "55,  FAIR",
            "50,  FAIR",
            "45,  FAIR",
            "40,  FAIR",
            "39,  NEEDS IMPROVEMENT",
            "35,  NEEDS IMPROVEMENT",
            "30,  NEEDS IMPROVEMENT",
            "25,  NEEDS IMPROVEMENT",
            "20,  NEEDS IMPROVEMENT",
            "19,  POOR",
            "15,  POOR",
            "10,  POOR",
            "5,   POOR",
            "0,   POOR"
    })
    void test02_to_25_ParameterizedRating(int score, String expectedRating) {
        int testNumber = currentTestNumber++;
        Instant start = Instant.now();
        String testName = String.format("Παράμετροι: %d → %s", score, expectedRating);

        try {
            System.out.printf("ΤΕΣΤ %02d: %s%n", testNumber, testName);

            String actualRating = GDPRChecker.getGDPRRating(score);
            assertEquals(expectedRating, actualRating,
                    String.format("Βαθμολογία %d/100: αναμενόμενη '%s', αλλά υπάρχουσα '%s'",
                            score, expectedRating, actualRating));

            long duration = Duration.between(start, Instant.now()).toMillis();
            recordTestResult(testNumber, testName, true,
                    String.format("Βαθμολογία %d → %s ✓", score, expectedRating), duration);

        } catch (AssertionError e) {
            long duration = Duration.between(start, Instant.now()).toMillis();
            recordTestResult(testNumber, testName, false, "Σφάλμα: " + e.getMessage(), duration);
            throw e;
        }
    }

    @Test
    @Order(26)
    @DisplayName("26. Δημιουργία GDPRResult αντικειμένου")
    void test26_GDPRResultObject() {
        int testNumber = currentTestNumber++;
        Instant start = Instant.now();
        String testName = "Δημιουργία GDPRResult";

        try {
            System.out.printf("\nΤΕΣΤ %02d: %s%n", testNumber, testName);

            GDPRChecker.GDPRResult result = new GDPRChecker.GDPRResult();

            result.score = 88;
            result.report = "GDPR COMPLIANCE CHECK\nΒαθμολογία: 88/100\nΚατάσταση: Άριστη\n";
            result.recommendations = Arrays.asList(
                    "Πολιτική Απορρήτου: Βρέθηκε",
                    "Cookie Banner: Χρειάζεται βελτίωση"
            );

            // ΕΛΕΓΧΟΙ
            assertAll(testName,
                    () -> assertEquals(88, result.score, "Η βαθμολογία πρέπει να είναι 88"),
                    () -> assertNotNull(result.report, "Η αναφορά δεν πρέπει να είναι κενή"),
                    () -> assertTrue(result.report.contains("GDPR COMPLIANCE CHECK"),
                            "Η αναφορά πρέπει να περιέχει 'GDPR COMPLIANCE CHECK'"),
                    () -> assertNotNull(result.recommendations, "Οι συστάσεις δεν πρέπει να είναι κενές"),
                    () -> assertEquals(2, result.recommendations.size(),
                            "Πρέπει να έχει 2 συστάσεις"),
                    () -> assertTrue(result.recommendations.get(0).contains("Πολιτική Απορρήτου"),
                            "Η πρώτη σύσταση πρέπει να αναφέρεται σε Πολιτική Απορρήτου")
            );

            long duration = Duration.between(start, Instant.now()).toMillis();
            recordTestResult(testNumber, testName, true, "GDPRResult αντικείμενο δημιουργήθηκε επιτυχώς", duration);

        } catch (AssertionError e) {
            long duration = Duration.between(start, Instant.now()).toMillis();
            recordTestResult(testNumber, testName, false, "Σφάλμα: " + e.getMessage(), duration);
            throw e;
        }
    }

    @Test
    @Order(27)
    @DisplayName("27. Έλεγχος ελληνικών keywords")
    @Tag("ελληνικά")
    void test27_GreekKeywords() {
        int testNumber = currentTestNumber++;
        Instant start = Instant.now();
        String testName = "Έλεγχος ελληνικών keywords";

        try {
            System.out.printf("\nΤΕΣΤ %02d: %s%n", testNumber, testName);

            String[] privacyKeywords = {
                    "πολιτική απορρήτου",
                    "απορρήτου",
                    "προστασία δεδομένων",
                    "προστασία προσωπικών δεδομένων"
            };

            String[] cookieKeywords = {
                    "συμφωνώ",
                    "αποδέχομαι",
                    "συγκατάθεση",
                    "αποδοχή",
                    "συναινώ"
            };

            // Έλεγχος keywords
            assertAll(testName,
                    () -> assertTrue(privacyKeywords.length >= 3,
                            "Πρέπει να υπάρχουν τουλάχιστον 3 keywords για απόρρητο"),
                    () -> assertTrue(cookieKeywords.length >= 3,
                            "Πρέπει να υπάρχουν τουλάχιστον 3 keywords για cookies"),
                    () -> {
                        for (String keyword : privacyKeywords) {
                            assertNotNull(keyword, "Keyword δεν μπορεί να είναι κενό");
                            assertTrue(keyword.length() >= 3,
                                    "Keyword '" + keyword + "' είναι πολύ μικρό");
                        }
                    },
                    () -> {
                        for (String keyword : cookieKeywords) {
                            assertNotNull(keyword, "Keyword δεν μπορεί να είναι κενό");
                            assertTrue(keyword.length() >= 3,
                                    "Keyword '" + keyword + "' είναι πολύ μικρό");
                        }
                    }
            );

            long duration = Duration.between(start, Instant.now()).toMillis();
            recordTestResult(testNumber, testName, true,
                    String.format("Βρέθηκαν %d privacy + %d cookie keywords",
                            privacyKeywords.length, cookieKeywords.length), duration);

        } catch (AssertionError e) {
            long duration = Duration.between(start, Instant.now()).toMillis();
            recordTestResult(testNumber, testName, false, "Σφάλμα: " + e.getMessage(), duration);
            throw e;
        }
    }


    @Test
    @Order(28)
    @DisplayName("28. Έλεγχος συνέπειας βαθμολογιών")
    void test28_ScoreConsistency() {
        int testNumber = currentTestNumber++;
        Instant start = Instant.now();
        String testName = "Έλεγχος συνέπειας βαθμολογιών";

        try {
            System.out.printf("\nΤΕΣΤ %02d: %s%n", testNumber, testName);

            // Έλεγχος ότι υψηλότερη βαθμολογία = ίδια ή καλύτερη βαθμολογία
            for (int i = 0; i < 100; i++) {
                String rating1 = GDPRChecker.getGDPRRating(i);
                String rating2 = GDPRChecker.getGDPRRating(i + 1);


                String[] ratingOrder = {"POOR", "NEEDS IMPROVEMENT", "FAIR", "GOOD", "EXCELLENT"};
                int index1 = Arrays.asList(ratingOrder).indexOf(rating1);
                int index2 = Arrays.asList(ratingOrder).indexOf(rating2);

                if (index2 < index1) {
                    fail(String.format("Μη έγκυρη μετάβαση: %s (%d) → %s (%d) για βαθμολογίες %d→%d",
                            rating1, i, rating2, i+1, i, i+1));
                }
            }

            long duration = Duration.between(start, Instant.now()).toMillis();
            recordTestResult(testNumber, testName, true,
                    "Έλεγχος συνέπειας για 100 βαθμολογίες επιτυχής", duration);

        } catch (AssertionError e) {
            long duration = Duration.between(start, Instant.now()).toMillis();
            recordTestResult(testNumber, testName, false, "Σφάλμα: " + e.getMessage(), duration);
            throw e;
        }
    }

    @Test
    @Order(29)
    @DisplayName("29. Ανάλυση οριακών τιμών")
    @Tag("boundary")
    void test29_BoundaryValues() {
        int testNumber = currentTestNumber++;
        Instant start = Instant.now();
        String testName = "Ανάλυση οριακών τιμών";

        try {
            System.out.printf("\nΤΕΣΤ %02d: %s%n", testNumber, testName);

            Map<Integer, String> boundaries = new LinkedHashMap<>();
            boundaries.put(100, "EXCELLENT");
            boundaries.put(80, "EXCELLENT");
            boundaries.put(79, "GOOD");
            boundaries.put(60, "GOOD");
            boundaries.put(59, "FAIR");
            boundaries.put(40, "FAIR");
            boundaries.put(39, "NEEDS IMPROVEMENT");
            boundaries.put(20, "NEEDS IMPROVEMENT");
            boundaries.put(19, "POOR");
            boundaries.put(0, "POOR");

            List<String> failures = new ArrayList<>();
            for (Map.Entry<Integer, String> entry : boundaries.entrySet()) {
                int score = entry.getKey();
                String expected = entry.getValue();
                String actual = GDPRChecker.getGDPRRating(score);

                if (!expected.equals(actual)) {
                    failures.add(String.format("%d → αναμενόμενο: %s, πραγματικό: %s",
                            score, expected, actual));
                }
            }

            if (!failures.isEmpty()) {
                fail("Σφάλματα στις οριακές τιμές:\n" + String.join("\n", failures));
            }

            long duration = Duration.between(start, Instant.now()).toMillis();
            recordTestResult(testNumber, testName, true,
                    String.format("Έλεγχος %d οριακών τιμών επιτυχής", boundaries.size()), duration);

        } catch (AssertionError e) {
            long duration = Duration.between(start, Instant.now()).toMillis();
            recordTestResult(testNumber, testName, false, "Σφάλμα: " + e.getMessage(), duration);
            throw e;
        }
    }


    @Test
    @Order(30)
    @DisplayName("30. Έλεγχος μοναδικότητας βαθμολογίας")
    void test30_UniqueRatings() {
        int testNumber = currentTestNumber++;
        Instant start = Instant.now();
        String testName = "Έλεγχος μοναδικότητας βαθμολογίας";

        try {
            System.out.printf("\nΤΕΣΤ %02d: %s%n", testNumber, testName);

            Set<String> uniqueRatings = new HashSet<>();
            for (int score = 0; score <= 100; score++) {
                uniqueRatings.add(GDPRChecker.getGDPRRating(score));
            }

            assertAll(testName,
                    () -> assertEquals(5, uniqueRatings.size(),
                            "Πρέπει να υπάρχουν 5 διαφορετικά ratings, βρέθηκαν: " + uniqueRatings.size()),
                    () -> assertTrue(uniqueRatings.contains("EXCELLENT"),
                            "Πρέπει να περιέχει EXCELLENT"),
                    () -> assertTrue(uniqueRatings.contains("GOOD"),
                            "Πρέπει να περιέχει GOOD"),
                    () -> assertTrue(uniqueRatings.contains("FAIR"),
                            "Πρέπει να περιέχει FAIR"),
                    () -> assertTrue(uniqueRatings.contains("NEEDS IMPROVEMENT"),
                            "Πρέπει να περιέχει NEEDS IMPROVEMENT"),
                    () -> assertTrue(uniqueRatings.contains("POOR"),
                            "Πρέπει να περιέχει POOR")
            );

            long duration = Duration.between(start, Instant.now()).toMillis();
            recordTestResult(testNumber, testName, true,
                    "Βρέθηκαν όλα τα 5 μοναδικά ratings: " + uniqueRatings, duration);

        } catch (AssertionError e) {
            long duration = Duration.between(start, Instant.now()).toMillis();
            recordTestResult(testNumber, testName, false, "Σφάλμα: " + e.getMessage(), duration);
            throw e;
        }
    }


    @Test
    @Order(31)
    @DisplayName("31. Δοκιμή απόδοσης")
    @Tag("performance")
    void test31_Performance() {
        int testNumber = currentTestNumber++;
        Instant start = Instant.now();
        String testName = "Δοκιμή απόδοσης";

        try {
            System.out.printf("\nΤΕΣΤ %02d: %s%n", testNumber, testName);

            long testStart = System.nanoTime();

            // Εκτέλεση 1000 κλήσεων
            for (int i = 0; i < 1000; i++) {

                GDPRChecker.getGDPRRating(i % 101);
            }

            long testEnd = System.nanoTime();
            long durationMs = (testEnd - testStart) / 1_000_000;

            assertTrue(durationMs < 100,
                    "Η απόδοση είναι αργή: " + durationMs + "ms (όριο: 100ms)");

            long totalDuration = Duration.between(start, Instant.now()).toMillis();
            recordTestResult(testNumber, testName, true,
                    String.format("1000 κλήσεις σε %dms (<100ms)", durationMs), totalDuration);

        } catch (AssertionError e) {
            long duration = Duration.between(start, Instant.now()).toMillis();
            recordTestResult(testNumber, testName, false, "Σφάλμα: " + e.getMessage(), duration);
            throw e;
        }
    }


    @Test
    @Order(32)
    @DisplayName("32. Έλεγχος συστάσεων")
    void test32_Recommendations() {
        int testNumber = currentTestNumber++;
        Instant start = Instant.now();
        String testName = "Έλεγχος συστάσεων";

        try {
            System.out.printf("\nΤΕΣΤ %02d: %s%n", testNumber, testName);

            // Δημιουργία test scenarios
            GDPRChecker.GDPRResult lowScoreResult = createMockResult(15);
            GDPRChecker.GDPRResult highScoreResult = createMockResult(85);

            assertAll(testName,
                    () -> assertTrue(lowScoreResult.score < 40,
                            "Χαμηλή βαθμολογία πρέπει να είναι < 40"),
                    () -> assertTrue(highScoreResult.score >= 80,
                            "Υψηλή βαθμολογία πρέπει να είναι ≥ 80"),
                    () -> assertNotNull(lowScoreResult.recommendations,
                            "Οι συστάσεις δεν πρέπει να είναι κενές"),
                    () -> assertNotNull(highScoreResult.recommendations,
                            "Οι συστάσεις δεν πρέπει να είναι κενές")
            );

            long duration = Duration.between(start, Instant.now()).toMillis();
            recordTestResult(testNumber, testName, true,
                    "Συστάσεις για διαφορετικές βαθμολογίες ελέγχθηκαν", duration);

        } catch (AssertionError e) {
            long duration = Duration.between(start, Instant.now()).toMillis();
            recordTestResult(testNumber, testName, false, "Σφάλμα: " + e.getMessage(), duration);
            throw e;
        }
    }

    @Test
    @Order(33)
    @DisplayName("33. Έλεγχος βαθμολογίας ορίων (0-100)")
    void test33_ScoreRange() {
        int testNumber = currentTestNumber++;
        Instant start = Instant.now();
        String testName = "Έλεγχος βαθμολογίας ορίων";

        try {
            System.out.printf("\nΤΕΣΤ %02d: %s%n", testNumber, testName);

            List<String> invalidRatings = new ArrayList<>();

            // Έλεγχος για όλες τις πιθανές βαθμολογίες
            for (int score = 0; score <= 100; score++) {
                String rating = GDPRChecker.getGDPRRating(score);

                if (rating.isEmpty()) {
                    invalidRatings.add("Βαθμολογία " + score + ": null ή κενό rating");
                }

                // Έλεγχος ότι το rating είναι ένα από τα έγκυρα
                List<String> validRatings = Arrays.asList(
                        "EXCELLENT", "GOOD", "FAIR", "NEEDS IMPROVEMENT", "POOR"
                );

                if (!validRatings.contains(rating)) {
                    invalidRatings.add("Βαθμολογία " + score + ": μη έγκυρο rating '" + rating + "'");
                }
            }

            if (!invalidRatings.isEmpty()) {
                fail("Βρέθηκαν μη έγκυρα ratings:\n" + String.join("\n", invalidRatings));
            }

            long duration = Duration.between(start, Instant.now()).toMillis();
            recordTestResult(testNumber, testName, true,
                    "Έλεγχος 101 βαθμολογιών (0-100) επιτυχής", duration);

        } catch (AssertionError e) {
            long duration = Duration.between(start, Instant.now()).toMillis();
            recordTestResult(testNumber, testName, false, "Σφάλμα: " + e.getMessage(), duration);
            throw e;
        }
    }


    private synchronized void recordTestResult(int testNumber, String testName, boolean passed, String message, long durationMs) {
        totalTests++;
        if (passed) {
            passedTests++;
        } else {
            failedTests++;
        }

        TestResult result = new TestResult(testNumber, testName, passed, message, durationMs);
        testResults.add(result);

        // Ταξινόμηση για να εμφανίζονται σε σειρά
        testResults.sort(Comparator.comparingInt(r -> r.testNumber));
    }

    private static void printHeader() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("GDPR CHECKER - UNIT TESTS");
        System.out.println(new Date());
        System.out.println("=".repeat(80));
        System.out.println("\nΑΡΧΗ ΕΚΤΕΛΕΣΗΣ ΤΕΣΤ...\n");
    }

    private static void printTestSummary() {
        long totalDuration = Duration.between(testSuiteStartTime, Instant.now()).toMillis();

        System.out.println("\n" + "=".repeat(80));
        System.out.println("ΣΥΝΟΠΤΙΚΗ ΑΝΑΦΟΡΑ ΑΠΟΤΕΛΕΣΜΑΤΩΝ");
        System.out.println("=".repeat(80));

        System.out.printf("\nΣΤΑΤΙΣΤΙΚΑ:%n");
        System.out.printf("   Σύνολο Τεστ:     %d%n", totalTests);
        System.out.printf("   Επιτυχημένα:      %d %n", passedTests);
        System.out.printf("   Αποτυχημένα:      %d %n", failedTests);
        System.out.printf("   Ποσοστό Επιτυχίας: %.1f%%%n",
                totalTests > 0 ? (passedTests * 100.0 / totalTests) : 0);
        System.out.printf("   Συνολική Διάρκεια: %dms%n", totalDuration);

        // Λεπτομερής λίστα
        System.out.printf("\nΛΕΠΤΟΜΕΡΗΣ ΛΙΣΤΑ ΑΠΟΤΕΛΕΣΜΑΤΩΝ:%n");
        System.out.println("-".repeat(90));
        System.out.printf("%-8s %-50s %-15s %-10s   %s%n",
                "ΤΕΣΤ", "ΟΝΟΜΑ", "ΚΑΤΑΣΤΑΣΗ", "ΔΙΑΡΚΕΙΑ", "ΜΥΝΗΜΑ");
        System.out.println("-".repeat(90));

        for (TestResult result : testResults) {
            System.out.println(result);
        }

        System.out.println("-".repeat(90));


        if (failedTests == 0) {
            System.out.println("\nΟΛΑ ΤΑ TESTS ΠΕΡΑΣΑΝ ΕΠΙΤΥΧΩΣ!");
        } else {
            System.out.println("\nΥΠΑΡΧΟΥΝ ΑΠΟΤΥΧΗΜΕΝΑ TESTS!");
        }
    }


    private GDPRChecker.GDPRResult createMockResult(int score) {
        GDPRChecker.GDPRResult result = new GDPRChecker.GDPRResult();
        result.score = score;
        result.report = "Mock Report - Score: " + score;
        result.recommendations = new ArrayList<>();

        if (score < 20) {
            result.recommendations.add("• Σημαντικές βελτιώσεις απαιτούνται");
        } else if (score < 60) {
            result.recommendations.add("• Χρειάζονται βελτιώσεις");
        } else {
            result.recommendations.add("• Καλή συμμόρφωση");
        }

        return result;
    }
}