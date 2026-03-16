import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.openqa.selenium.*;
import java.util.*;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    // WebDriver mock για χρήση στα τεστ
    private WebDriver mockDriver;
    private WebElement mockBody;
    private List<WebElement> mockLinks;

    @BeforeAll
    static void setupAll() {
        testSuiteStartTime = Instant.now();
        printHeader();
    }

    @BeforeEach
    void setUp() {
        // Δημιουργία mock WebDriver πριν από κάθε τεστ
        mockDriver = mock(WebDriver.class);
        mockBody = mock(WebElement.class);
        mockLinks = new ArrayList<>();

        when(mockDriver.findElement(By.tagName("body"))).thenReturn(mockBody);
        when(mockDriver.findElements(By.tagName("a"))).thenReturn(mockLinks);
    }

    @AfterAll
    static void cleanupAll() {
        printTestSummary();
    }

    private static void printHeader() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println(" GDPR CHECKER - UNIT TESTS SUITE");
        System.out.println("=".repeat(80));
    }

    private static void recordTestResult(int testNumber, String testName, boolean passed, String message, long durationMs) {
        TestResult result = new TestResult(testNumber, testName, passed, message, durationMs);
        testResults.add(result);
        totalTests++;
        if (passed) {
            passedTests++;
        } else {
            failedTests++;
        }
        System.out.printf("Αποτέλεσμα: %s (%dms)%n", passed ? "ΕΠΙΤΥΧΙΑ" : "ΑΠΟΤΥΧΙΑ", durationMs);
    }

    @Test
    @Order(1)
    @DisplayName("01. Βασικός έλεγχος βαθμολογίας GDPR")
    @Tag("Κρίσιμο")
    void BasicGDPRRating() {
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
            "100, EXCELLENT", "95, EXCELLENT", "85, EXCELLENT", "80, EXCELLENT",
            "79, GOOD", "75, GOOD", "70, GOOD", "65, GOOD", "60, GOOD",
            "59, FAIR", "55, FAIR", "50, FAIR", "45, FAIR", "40, FAIR",
            "39, NEEDS IMPROVEMENT", "35, NEEDS IMPROVEMENT", "30, NEEDS IMPROVEMENT",
            "25, NEEDS IMPROVEMENT", "20, NEEDS IMPROVEMENT",
            "19, POOR", "15, POOR", "10, POOR", "5, POOR", "0, POOR"
    })
    void ParameterizedRating(int score, String expectedRating) {
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
    @DisplayName("26. Έλεγχος δημιουργίας GDPRResult")
    void test_GDPRResult() {
        int testNumber = currentTestNumber++;
        Instant start = Instant.now();
        String testName = "Δημιουργία GDPRResult";

        try {
            System.out.printf("\nΤΕΣΤ %02d: %s%n", testNumber, testName);

            // Προετοιμασία mock για μια βασική ιστοσελίδα
            when(mockBody.getText()).thenReturn("This is a test page with some content");

            // Mock για links (κανένα link πολιτικής απορρήτου)
            WebElement mockLink = mock(WebElement.class);
            when(mockLink.getText()).thenReturn("Home");
            when(mockLink.getAttribute("href")).thenReturn("http://example.com");
            when(mockLink.isDisplayed()).thenReturn(true);
            when(mockLink.isEnabled()).thenReturn(true);
            mockLinks.add(mockLink);

            // Εκτέλεση της πραγματικής μεθόδου
            GDPRChecker.GDPRResult result = GDPRChecker.checkGDPRCompliance(mockDriver);

            // Έλεγχοι
            assertAll(testName,
                    () -> assertNotNull(result, "Το αποτέλεσμα δεν πρέπει να είναι κενό"),
                    () -> assertTrue(result.score >= 0 && result.score <= 100,
                            "Η βαθμολογία πρέπει να είναι μεταξύ 0-100, ήταν: " + result.score),
                    () -> assertNotNull(result.report, "Η αναφορά δεν πρέπει να είναι κενή"),
                    () -> assertTrue(result.report.contains("GDPR COMPLIANCE CHECK"),
                            "Η αναφορά πρέπει να περιέχει 'GDPR COMPLIANCE CHECK'"),
                    () -> assertNotNull(result.recommendations, "Οι συστάσεις δεν πρέπει να είναι κενές")
            );

            long duration = Duration.between(start, Instant.now()).toMillis();
            recordTestResult(testNumber, testName, true,
                    "GDPRResult δημιουργήθηκε επιτυχώς από checkGDPRCompliance()", duration);

        } catch (AssertionError e) {
            long duration = Duration.between(start, Instant.now()).toMillis();
            recordTestResult(testNumber, testName, false, "Σφάλμα: " + e.getMessage(), duration);
            throw e;
        }
    }

    @Test
    @Order(27)
    @DisplayName("27. Έλεγχος ανίχνευσης ελληνικών keywords από τον κώδικα")
    void GreekKeywordsDetection() {
        int testNumber = currentTestNumber++;
        Instant start = Instant.now();
        String testName = "Έλεγχος ανίχνευσης ελληνικών keywords";

        try {
            System.out.printf("\nΤΕΣΤ %02d: %s%n", testNumber, testName);

            // Test 1: Έλεγχος checkPrivacyPolicy με ελληνικό κείμενο
            when(mockBody.getText()).thenReturn("Η πολιτική απορρήτου της εταιρείας μας");

            // Mock για link που οδηγεί σε πολιτική απορρήτου
            WebElement mockPrivacyLink = mock(WebElement.class);
            when(mockPrivacyLink.getText()).thenReturn("Πολιτική Απορρήτου");
            when(mockPrivacyLink.getAttribute("href")).thenReturn("/privacy");
            when(mockPrivacyLink.isDisplayed()).thenReturn(true);
            when(mockPrivacyLink.isEnabled()).thenReturn(true);

            // Mock για άσχετο link
            WebElement mockOtherLink = mock(WebElement.class);
            when(mockOtherLink.getText()).thenReturn("Home");
            when(mockOtherLink.getAttribute("href")).thenReturn("/home");

            mockLinks.clear();
            mockLinks.add(mockPrivacyLink);
            mockLinks.add(mockOtherLink);

            int privacyScore = GDPRChecker.checkPrivacyPolicy(mockDriver);
            assertTrue(privacyScore >= 15,
                    "Η checkPrivacyPolicy δεν ανίχνευσε ελληνικά keywords. Βαθμολογία: " + privacyScore);

            // Test 2: Έλεγχος checkCookiesConsent με ελληνικό κείμενο
            when(mockBody.getText()).thenReturn("Συμφωνώ με τη χρήση cookies για καλύτερη εμπειρία");

            // Mock για cookie banner
            WebElement mockCookieBanner = mock(WebElement.class);
            when(mockDriver.findElements(By.cssSelector("[class*='cookie']")))
                    .thenReturn(Collections.singletonList(mockCookieBanner));

            int cookiesScore = GDPRChecker.checkCookiesConsent(mockDriver);
            assertTrue(cookiesScore >= 5,
                    "Η checkCookiesConsent δεν ανίχνευσε ελληνικά keywords. Βαθμολογία: " + cookiesScore);

            long duration = Duration.between(start, Instant.now()).toMillis();
            recordTestResult(testNumber, testName, true,
                    "Ελληνικά keywords ανιχνεύονται σωστά από τον κώδικα", duration);

        } catch (AssertionError e) {
            long duration = Duration.between(start, Instant.now()).toMillis();
            recordTestResult(testNumber, testName, false, "Σφάλμα: " + e.getMessage(), duration);
            throw e;
        }
    }

    @Test
    @Order(28)
    @DisplayName("28. Έλεγχος συνέπειας βαθμολογιών")
    void ScoreConsistency() {
        int testNumber = currentTestNumber++;
        Instant start = Instant.now();
        String testName = "Έλεγχος συνέπειας βαθμολογιών";

        try {
            System.out.printf("\nΤΕΣΤ %02d: %s%n", testNumber, testName);

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
    void BoundaryValues() {
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
    @DisplayName("30. Δοκιμή απόδοσης checkGDPRCompliance")
    @Tag("performance")
    void test_Performance() {
        int testNumber = currentTestNumber++;
        Instant start = Instant.now();
        String testName = "Δοκιμή απόδοσης checkGDPRCompliance";

        try {
            System.out.printf("\nΤΕΣΤ %02d: %s%n", testNumber, testName);

            // Προετοιμασία mock για μια σχετικά μεγάλη ιστοσελίδα
            StringBuilder largeText = new StringBuilder();
            for (int i = 0; i < 1000; i++) {
                largeText.append("This is a test line ").append(i).append("\n");
            }
            when(mockBody.getText()).thenReturn(largeText.toString());

            // Προσθήκη πολλών mock links
            for (int i = 0; i < 50; i++) {
                WebElement mockLink = mock(WebElement.class);
                when(mockLink.getText()).thenReturn("Link " + i);
                when(mockLink.getAttribute("href")).thenReturn("http://example.com/link" + i);
                mockLinks.add(mockLink);
            }

            long testStart = System.nanoTime();

            GDPRChecker.checkGDPRCompliance(mockDriver);

            long testEnd = System.nanoTime();
            long durationMs = (testEnd - testStart) / 1_000_000;

            assertTrue(durationMs < 2000,
                    "Η απόδοση είναι αργή: " + durationMs + "ms (όριο: 2000ms)");

            long totalDuration = Duration.between(start, Instant.now()).toMillis();
            recordTestResult(testNumber, testName, true,
                    String.format("checkGDPRCompliance σε %dms (<2000ms)", durationMs), totalDuration);

        } catch (AssertionError e) {
            long duration = Duration.between(start, Instant.now()).toMillis();
            recordTestResult(testNumber, testName, false, "Σφάλμα: " + e.getMessage(), duration);
            throw e;
        }
    }

    @Test
    @Order(31)
    @DisplayName("31. Έλεγχος συστάσεων ανάλογα με τη βαθμολογία")
    void test_Recommendations() {
        int testNumber = currentTestNumber++;
        Instant start = Instant.now();
        String testName = "Έλεγχος συστάσεων ανάλογα με τη βαθμολογία";

        try {
            System.out.printf("\nΤΕΣΤ %02d: %s%n", testNumber, testName);

            // Σενάριο 1: Χαμηλή βαθμολογία (καμία συμμόρφωση)
            when(mockBody.getText()).thenReturn("Just some text without any GDPR keywords");
            mockLinks.clear(); // Κανένα link

            GDPRChecker.GDPRResult lowScoreResult = GDPRChecker.checkGDPRCompliance(mockDriver);

            // Σενάριο 2: Υψηλή βαθμολογία (πλήρης συμμόρφωση)
            when(mockBody.getText()).thenReturn(
                    "GDPR compliance privacy policy data protection " +
                            "right to access right to erasure data portability " +
                            "contact@example.com dpo@company.com"
            );

            // Προσθήκη link πολιτικής απορρήτου
            WebElement mockPrivacyLink = mock(WebElement.class);
            when(mockPrivacyLink.getText()).thenReturn("Privacy Policy");
            when(mockPrivacyLink.getAttribute("href")).thenReturn("/privacy");
            when(mockPrivacyLink.isDisplayed()).thenReturn(true);
            when(mockPrivacyLink.isEnabled()).thenReturn(true);

            // Προσθήκη cookie banner
            WebElement mockCookieBanner = mock(WebElement.class);
            when(mockDriver.findElements(By.cssSelector("[class*='cookie']")))
                    .thenReturn(Collections.singletonList(mockCookieBanner));

            mockLinks.clear();
            mockLinks.add(mockPrivacyLink);

            GDPRChecker.GDPRResult highScoreResult = GDPRChecker.checkGDPRCompliance(mockDriver);

            assertAll(testName,
                    () -> assertTrue(lowScoreResult.score < 40,
                            "Χαμηλή βαθμολογία πρέπει να είναι < 40, ήταν: " + lowScoreResult.score),
                    () -> assertTrue(highScoreResult.score >= 60,
                            "Υψηλή βαθμολογία πρέπει να είναι ≥ 60, ήταν: " + highScoreResult.score),
                    () -> assertFalse(lowScoreResult.recommendations.isEmpty(),
                            "Η χαμηλή βαθμολογία πρέπει να έχει συστάσεις"),
                    () -> assertTrue(lowScoreResult.recommendations.size() >= highScoreResult.recommendations.size(),
                            "Η χαμηλή βαθμολογία πρέπει να έχει περισσότερες ή ίσες συστάσεις από την υψηλή"),
                    () -> {
                        // Έλεγχος ότι οι συστάσεις είναι σχετικές
                        boolean hasPrivacyRecommendation = lowScoreResult.recommendations.stream()
                                .anyMatch(r -> r.contains("Πολιτική Απορρήτου") || r.contains("Privacy"));
                        assertTrue(hasPrivacyRecommendation,
                                "Θα έπρεπε να υπάρχει σύσταση για Πολιτική Απορρήτου");
                    }
            );

            long duration = Duration.between(start, Instant.now()).toMillis();
            recordTestResult(testNumber, testName, true,
                    "Συστάσεις ανάλογες με τη βαθμολογία", duration);

        } catch (AssertionError e) {
            long duration = Duration.between(start, Instant.now()).toMillis();
            recordTestResult(testNumber, testName, false, "Σφάλμα: " + e.getMessage(), duration);
            throw e;
        }
    }

    private static void printTestSummary() {
        long totalDuration = Duration.between(testSuiteStartTime, Instant.now()).toMillis();

        System.out.println("\n" + "=".repeat(80));
        System.out.println(" ΑΠΟΤΕΛΕΣΜΑΤΑ ΤΕΣΤ");
        System.out.println("=".repeat(80));

        System.out.printf("Σύνολο τεστ: %d%n", totalTests);
        System.out.printf("Επιτυχίες:   %d%n", passedTests);
        System.out.printf("Αποτυχίες:   %d%n", failedTests);
        System.out.printf("Ποσοστό:     %.1f%%%n",
                (totalTests > 0) ? (passedTests * 100.0 / totalTests) : 0);
        System.out.printf("Συνολικός χρόνος: %dms%n", totalDuration);

        System.out.println("\n" + "-".repeat(80));
        System.out.println(" ΑΝΑΛΥΤΙΚΑ ΑΠΟΤΕΛΕΣΜΑΤΑ");
        System.out.println("-".repeat(80));

        for (TestResult result : testResults) {
            System.out.println(result);
        }

        System.out.println("=".repeat(80));
    }
}