import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WebsiteCheckerTests {
    @Test
    @Order(1)
    @DisplayName("1. Έλεγχος ιστοσελίδας example.com")
    public void testExampleCom() {
        System.out.println("1. Έλεγχος ιστοσελίδας example.com...");
        String url = "https://example.com";

        try {
            String result = WebsiteChecker.checkWebsite(url);

            assertNotNull(result);
            assertTrue(result.contains("WEBSITE ANALYSIS REPORT"));

            int score = extractScore(result);
            String rating = extractRating(result);

            System.out.println("URL: " + url);
            System.out.println("Βαθμολογία: " + score + "/100");
            System.out.println("Αξιολόγηση: " + rating);
            System.out.println("ΤΕΣΤ ΠΕΤΥΧΕ");

        } catch (Exception e) {
            System.err.println("Σφάλμα: " + e.getMessage());
            fail("Το τεστ απέτυχε: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @DisplayName("2. Δοκιμή μεθόδου getRating")
    public void testGetRatingMethod() {
        System.out.println("\n2. Δοκιμή μεθόδου getRating...");

        try {
            String excellent = WebsiteChecker.getRating(90);
            String good = WebsiteChecker.getRating(75);
            String fair = WebsiteChecker.getRating(60);
            String needsImprovement = WebsiteChecker.getRating(40);
            String poor = WebsiteChecker.getRating(20);

            assertTrue(excellent.contains("EXCELLENT"));
            assertTrue(good.contains("GOOD"));
            assertTrue(fair.contains("FAIR"));
            assertTrue(needsImprovement.contains("NEEDS IMPROVEMENT"));
            assertTrue(poor.contains("POOR"));

            System.out.println("90/100 → " + excellent);
            System.out.println("75/100 → " + good);
            System.out.println("60/100 → " + fair);
            System.out.println("40/100 → " + needsImprovement);
            System.out.println("20/100 → " + poor);
            System.out.println("ΤΕΣΤ ΠΕΤΥΧΕ");

        } catch (Exception e) {
            System.err.println("Σφάλμα: " + e.getMessage());
            fail("Το τεστ απέτυχε: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    @DisplayName("3. Έλεγχος μη έγκυρου URL")
    public void testInvalidUrl() {
        System.out.println("\n3. Έλεγχος μη έγκυρου URL...");
        String url = "https://thiswebsitedoesnotexist12345.com";

        try {
            String result = WebsiteChecker.checkWebsite(url);
            assertNotNull(result);
            assertTrue(result.contains("Error:") || result.contains("error"));
            System.out.println("URL: " + url);
            System.out.println("Το σύστημα εντόπισε σωστά το σφάλμα");
            System.out.println("ΤΕΣΤ ΠΕΤΥΧΕ");

        } catch (Exception e) {
            System.out.println("URL: " + url);
            System.out.println("Αναμενόμενο σφάλμα: " + e.getMessage());
            System.out.println("ΤΕΣΤ ΠΕΤΥΧΕ");
        }
    }

    private int extractScore(String report) {
        String[] lines = report.split("\n");
        for (String line : lines) {
            if (line.contains("BASIC SCORE:") || line.contains("FINAL COMBINED SCORE:")) {
                int colonIndex = line.indexOf(":");
                if (colonIndex > 0) {
                    String afterColon = line.substring(colonIndex + 1).trim();
                    String[] parts = afterColon.split("/");
                    String scoreStr = parts[0].trim().replaceAll("[^0-9]", "");
                    if (!scoreStr.isEmpty()) {
                        return Integer.parseInt(scoreStr);
                    }
                }
            }
        }
        return 0;
    }

    private String extractRating(String report) {
        String[] lines = report.split("\n");
        for (String line : lines) {
            if (line.contains("BASIC RATING:") || line.contains("FINAL RATING:")){
                return line.substring(line.indexOf(":") + 1).trim();
            }
        }
        return "N/A";
    }
}

