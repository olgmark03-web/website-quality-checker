import java.io.*;
import java.nio.file.*;
import java.util.*;

public class WebsiteTester {

    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║         ΣΥΓΚΡΙΤΙΚΗ ΑΞΙΟΛΟΓΗΣΗ        ║");
        System.out.println("║            60 ΙΣΤΟΣΕΛΙΔΩΝ            ║");
        System.out.println("╚══════════════════════════════════════╝");

        try {
            // ΒΗΜΑ 1: Φόρτωσε τις ιστοσελίδες από αρχείο
            System.out.println("\nΒΗΜΑ 1: Φόρτωση ιστοσελίδων...");
            List<Website> websites = loadWebsites();
            System.out.println("Φορτώθηκαν " + websites.size() + " ιστοσελίδες");

            // ΒΗΜΑ 2: Δημιούργησε το CSV αρχείο
            System.out.println("\nΒΗΜΑ 2: Δημιουργία CSV αρχείου...");
            File csvFile = new File("website_analysis_results.csv");
            createCSVFile(csvFile, websites);

            // ΒΗΜΑ 3: Ανάλυσε κάθε ιστοσελίδα
            System.out.println("\nΒΗΜΑ 3: Εκτέλεση αναλύσεων...");
            analyzeWebsites(websites, csvFile);

            System.out.println("\n" + "-".repeat(60));
            System.out.println("ΟΛΟΚΛΗΡΩΘΗΚΕ Η ΑΝΑΛΥΣΗ!");
            System.out.println("=".repeat(60));
            System.out.println("\nΤΑ ΑΠΟΤΕΛΕΣΜΑΤΑ ΕΙΝΑΙ ΣΤΟ ΑΡΧΕΙΟ:");
            System.out.println(csvFile.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("ΣΦΑΛΜΑ: " + e.getMessage());
        }
    }

    // Κλάση για να αποθηκεύουμε τις πληροφορίες κάθε ιστοσελίδας
    static class Website {
        int id;
        String url;
        String category;
        String country;

        Website(int id, String url, String category, String country) {
            this.id = id;
            this.url = url;
            this.category = category;
            this.country = country;
        }
    }

    // ΜΕΘΟΔΟΣ 1: Φόρτωση ιστοσελίδων
    private static List<Website> loadWebsites() throws IOException {
        List<Website> websites = new ArrayList<>();

        File websitesFile = new File("websites_to_analyze.txt");
        if (!websitesFile.exists()) {
            System.out.println("Δημιουργία αρχείου με 50 δείγματα...");
            createSampleWebsitesFile(websitesFile);
        }

        //Διάβασε το αρχείο γραμμή-γραμμή
        List<String> lines = Files.readAllLines(websitesFile.toPath());
        int id = 1;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            String[] parts = line.split(",");
            if (parts.length >= 3) {
                String url = parts[0].trim();
                String category = parts[1].trim();
                String country = parts[2].trim();

                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://" + url;
                }

                websites.add(new Website(id++, url, category, country));
            }
        }

        return websites;
    }

    // ΜΕΘΟΔΟΣ 2: Δημιουργία δείγματος 50 ιστοσελίδων
    private static void createSampleWebsitesFile(File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("# ΜΟΡΦΗ: URL, ΚΑΤΗΓΟΡΙΑ, ΧΩΡΑ");
            writer.println("# Δημιουργήθηκε αυτόματα από WebsiteTester");
            writer.println();

            // 50 ΔΕΙΓΜΑΤΑ ΙΣΤΟΣΕΛΙΔΩΝ
            String[] sampleWebsites = {
                    "amazon.com,ecommerce,US",
                    "ebay.com,ecommerce,US",
                    "skroutz.gr,ecommerce,GR",
                    "e-food.gr,ecommerce,GR",
                    "public.gr,technology,GR",
                    "bbc.com,news,UK",
                    "nytimes.com,news,US",
                    "efsyn.gr,news,GR",
                    "kathimerini.gr,news,GR",
                    "protothema.gr,news,GR",
                    "harvard.edu,education,US",
                    "mit.edu,education,US",
                    "uoa.gr,education,GR",
                    "auth.gr,education,GR",
                    "ntua.gr,education,GR",
                    "aade.gr,government,GR",
                    "presidency.gr,government,GR",
                    "astynomia.gr,government,GR",
                    "whitehouse.gov,government,US",
                    "gov.uk,government,UK",
                    "cosmote.gr,telecom,GR",
                    "vodafone.gr,telecom,GR",
                    "nova.gr,telecom,GR",
                    "nbg.gr,banking,GR",
                    "eurobank.gr,banking,GR",
                    "piraeusbank.gr,banking,GR",
                    "oracle.com,technology,US",
                    "apple.com,technology,US",
                    "microsoft.com,technology,US",
                    "google.com,technology,US",
                    "plaisio.gr,technology,GR",
                    "kotsovolos.gr,technology,GR",
                    "pharmacyshop.gr,health,GR",
                    "medicity.gr,health,GR",
                    "eopyy.gov.gr,health,GR",
                    "booking.com,travel,NL",
                    "airbnb.com,travel,US",
                    "ferryhopper.com,travel,GR",
                    "aegeanair.com,travel,GR",
                    "facebook.com,social,US",
                    "instagram.com,social,US",
                    "x.com,social,US",
                    "linkedin.com,social,US",
                    "netflix.com,entertainment,US",
                    "youtube.com,entertainment,US",
                    "spotify.com,entertainment,SE",
                    "wikipedia.org,education,INT",
                    "stackoverflow.com,technology,US",
                    "github.com,technology,US",
                    "adobe.com,technology,US"
            };

            for (String site : sampleWebsites) {
                writer.println(site);
            }

            System.out.println("Δημιουργήθηκε το αρχείο: " + file.getName());
        }
    }

    // ΜΕΘΟΔΟΣ 3: Δημιουργία CSV αρχείου
    private static void createCSVFile(File csvFile, List<Website> websites) throws IOException {
        try (PrintWriter writer = new PrintWriter(csvFile)) {
            writer.println("ID,URL,CATEGORY,COUNTRY," +
                    "BASIC_SCORE,YOUR_SCORE,YOUR_TIME," +
                    "PERFORMANCE,SSL,SEO_STATUS," +
                    "BROKEN_LINKS,LIGHTHOUSE_PERFORMANCE,LIGHTHOUSE_SEO,CHECK_MY_LINKS,SSL," +
                    "GDPR_SCORE," +
                    "ANALYSIS_DATE,NOTES");

            System.out.println("Δημιουργήθηκε CSV αρχείο με " + websites.size() + " γραμμές");
        }
    }


    private static void analyzeWebsites(List<Website> websites, File csvFile) throws IOException {
        int total = websites.size();
        int successful = 0;
        int failed = 0;

        try (PrintWriter writer = new PrintWriter(new FileWriter(csvFile, true))) {
            for (int i = 0; i < total; i++) {
                Website site = websites.get(i);

                System.out.printf("\n[%d/%d] Αναλύεται: %s",
                        i + 1, total, site.url);

                try {
                    System.out.print("\n ▶ Εκτέλεση WebsiteChecker... ");
                    String analysisResult = WebsiteChecker.checkWebsite(site.url);

                    // Εξαγωγή όλων των μετρικών
                    int basicScore = extractBasicScore(analysisResult);
                    int finalScore = extractFinalScore(analysisResult);
                    long yourTime = extractLoadTime(analysisResult);
                    int gdprScore = extractGDPRScore(analysisResult);

                    String performance = extractPerformance(analysisResult);
                    boolean ssl = extractSSL(analysisResult);
                    String seoStatus = extractSEOStatus(analysisResult);
                    int brokenLinks = extractBrokenLinks(analysisResult);

                    System.out.printf(" Basic Score: %d, Final Score: %d, Time: %dms, GDPR: %d, Broken: %d\n",
                            basicScore, finalScore, yourTime, gdprScore, brokenLinks);

                    // ΓΡΑΨΕ ΣΤΟ CSV
                    String csvLine = String.format("%d,\"%s\",\"%s\",\"%s\",%d,%d,%d,\"%s\",%b,%s,%d,0, 0, 0, 0,%d,\"%s\"",
                            site.id,
                            site.url,
                            site.category,
                            site.country,
                            basicScore,
                            finalScore,
                            yourTime,
                            performance,
                            ssl,
                            seoStatus,
                            brokenLinks,
                            gdprScore,
                            java.time.LocalDate.now()
                    );

                    writer.println(csvLine);
                    writer.flush();

                    successful++;

                    // ΠΑΥΣΗ 3 ΔΕΥΤΕΡΟΛΕΠΤΩΝ ΜΕΤΑΞΥ ΑΝΑΛΥΣΕΩΝ
                    if (i < total - 1) {
                        System.out.print(" Αναμονή 3 δευτερολέπτων...");
                        Thread.sleep(3000);
                        System.out.println(" Συνέχεια!");
                    }

                } catch (Exception e) {
                    System.out.printf("\n ΣΦΑΛΜΑ: %s\n", e.getMessage());

                    writer.printf("%d,\"%s\",\"%s\",\"%s\",ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,0,0,0,ERROR,%s,\"Σφάλμα: %s\"\n",
                            site.id,
                            site.url,
                            site.category,
                            site.country,
                            java.time.LocalDate.now(),
                            e.getMessage().replace(",", ";")
                    );

                    failed++;
                }
            }

            System.out.println("\nΣΥΝΟΨΗ ΑΝΑΛΥΣΗΣ:");
            System.out.printf(" Επιτυχείς: %d\n", successful);
            System.out.printf(" Αποτυχίες: %d\n", failed);
            System.out.printf(" Ποσοστό Επιτυχίας: %.1f%%\n",
                    (successful * 100.0) / total);
        }
    }



    private static int extractBasicScore(String result) {
        if (result.contains("BASIC SCORE:")) {
            try {
                String scorePart = result.split("BASIC SCORE:")[1].trim();
                String score = scorePart.split("/")[0].trim();
                return Integer.parseInt(score);
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }

    private static int extractFinalScore(String result) {
        if (result.contains("FINAL SCORE:")) {

            String scorePart = result.split("FINAL SCORE:")[1].trim();
            String score = scorePart.split("/")[0].trim();
            return Integer.parseInt(score);

        }

        if (result.contains("COMBINED SCORE:")) {

            String scorePart = result.split("COMBINED SCORE:")[1].trim();
            String score = scorePart.split("/")[0].trim();
            return Integer.parseInt(score);

        }

        if (result.contains("SCORE:")) {

            String[] lines = result.split("\n");
                for (String line : lines) {
                    if (line.contains("SCORE:") && line.contains("/100")) {
                        String score = line.split("SCORE:")[1].trim().split("/")[0].trim();
                        return Integer.parseInt(score);
                    }
                }
        }

        return 0;
    }

    // Εξαγωγή χρόνου φόρτωσης
    private static long extractLoadTime(String result) {
        if (result.contains("Load Time:")) {

            String timePart = result.split("Load Time:")[1].trim();
            String time = timePart.split(" ")[0].trim();
            return Long.parseLong(time);

        }

        if (result.contains("ms")) {
                String[] words = result.split(" ");
                for (int i = 0; i < words.length; i++) {
                    if (words[i].endsWith("ms") && i > 0) {
                        String time = words[i-1];
                        return Long.parseLong(time);
                    }
                }
        }

        return 0;
    }

    // Εξαγωγή GDPR βαθμολογίας
    private static int extractGDPRScore(String result) {
        if (result.contains("GDPR TOTAL SCORE:")) {
                String scorePart = result.split("GDPR TOTAL SCORE:")[1].trim();
                String score = scorePart.split("/")[0].trim();
                return Integer.parseInt(score);

        }

        if (result.contains("GDPR SCORE:")) {

            String scorePart = result.split("GDPR SCORE:")[1].trim();
            String score = scorePart.split("/")[0].trim();
            return Integer.parseInt(score);

        }

        return 0;
    }
    private static String extractPerformance(String result) {
        if (result.contains("Performance:")) {
            String[] lines = result.split("\n");
            for (String line : lines) {
                if (line.contains("Performance:")) {
                    return line.split("Performance:")[1].trim();
                }
            }
        }
        return "Unknown";
    }

    private static boolean extractSSL(String result) {
        return result.contains("SSL: Secure");
    }
    private static String extractSEOStatus(String result) {
        if (result.contains("SEO Title:")) {
            String[] lines = result.split("\n");
            for (String line : lines) {
                if (line.contains("SEO Title:")) {
                    return line.split("SEO Title:")[1].trim();
                }
            }
        }
        return "Unknown";
    }
    private static int extractBrokenLinks(String result) {
        if (result.contains("Broken Links")) {
            try {
                String[] lines = result.split("\n");
                for (String line : lines) {
                    if (line.contains("Broken Links")) {
                        // Broken Links (checked 5 of 10): 2
                        String[] parts = line.split(":");
                        return Integer.parseInt(parts[parts.length - 1].trim());
                    }
                }
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }
}