import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.time.Duration;

//Αυτή η κλάση υλοποιεί τον κύριο έλεγχο της εφαρμογής
//Χρησιμοποιεί Selenium WebDriver για αυτοματοποιημένη περιήγηση και HTTP connections για έλεγχο broken links
public class WebsiteChecker {

    //Κύρια μέθοδος ελέγχου ιστοσελίδας:
    public static String checkWebsite(String url) {
        //StringBuilder για συσσώρευση της αναφοράς
        StringBuilder report = new StringBuilder();

        WebDriver driver = null;
        int basicScore = 100;

        try {

            // 1.ΡΥΘΜΙΣΕΙΣ ΓΙΑ ΝΑ ΜΗ ΒΓΑΖΕΙ WARNINGS
            System.setProperty("webdriver.chrome.silentOutput", "true");
            System.setProperty("webdriver.chrome.silentLogging", "true");
            System.setProperty("webdriver.chrome.verboseLogging", "false");

            // ΑΠΑΓΟΡΕΥΕΙ όλα τα Selenium logs
            java.util.logging.Logger.getLogger("org.openqa.selenium").setLevel(java.util.logging.Level.SEVERE);
            java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.SEVERE);


            // 2. ΡΥΘΜΙΣΕΙΣ CHROME BROWSER
            ChromeOptions options = new ChromeOptions();


            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");

            // Απόκρυψη automation(για να μην ανιχνεύεται ως bot)
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
            options.setExperimentalOption("useAutomationExtension", false);

            // Απενεργοποίηση όλων των logs
            options.addArguments("--log-level=OFF");
            options.addArguments("--silent");
            options.addArguments("--disable-logging");

            // Προσθήκη capabilities για να μην εμφανίζει warnings
            options.setCapability("goog:loggingPrefs",
                    new java.util.HashMap<String, String>() {{
                        put("browser", "OFF");
                        put("driver", "OFF");
                        put("performance", "OFF");
                    }});


            // 3. ΕΚΚΙΝΗΣΗ CHROME DRIVER
            driver = new ChromeDriver(options);

            // Ρύθμιση timeouts
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(15));


            // 4. ΕΛΕΓΧΟΣ ΕΠΙΔΟΣΗΣ
            long start = System.currentTimeMillis();
            //Πλοήγηση στη σελίδα
            driver.get(url);
            //Υπολογισμός χρόνου φόρτωσης σε milliseconds
            long loadTime = System.currentTimeMillis() - start;


            report.append("=== WEBSITE ANALYSIS REPORT ===\n\n");
            report.append("URL: ").append(url).append("\n");
            report.append("Title: ").append(driver.getTitle()).append("\n");
            report.append("Load Time: ").append(loadTime).append(" ms\n");

            // Αξιολόγηση επίδοσης με βάση το χρόνο φόρτωσης
            if (loadTime > 4000) {
                basicScore -= 20;
                report.append("Performance: Slow\n");
            }
            else if (loadTime > 2000) {
                basicScore -= 10;
                report.append("Performance: Acceptable\n");
            }
            else {
                report.append("Performance: Excellent\n");
            }


            // 5. ΕΛΕΓΧΟΣ SSL/ΑΣΦΑΛΕΙΑΣ
            if (url.startsWith("https")) {
                report.append("SSL: Secure\n");
            } else {
                report.append("SSL: Not Secure\n");
                basicScore -= 20;
            }


            // 6. ΕΛΕΓΧΟΣ SEO (SEARCH ENGINE OPTIMIZATION)
            String title = driver.getTitle();

            if (title == null || title.isEmpty()) {
                report.append("SEO Title: Missing\n");
                basicScore -= 10; //Ποινή για ελλείποντα τίτλο
            } else if (title.length() > 60) {
                //Ιδανικό μήκος τίτλου SEO: 50-60 χαρακτήρες
                report.append("SEO Title: Too Long (").append(title.length()).append(" chars)\n");
                basicScore -= 5;
            } else {
                report.append("SEO Title: OK\n");
            }


            // 7. ΕΛΕΓΧΟΣ BROKEN LINKS (Σπασμένων συνδέσμων)
            int broken = 0;
            int linksChecked = 0;
            List<WebElement> links = driver.findElements(By.tagName("a"));
           //Έλεγχος μόνο των πρώτων 5 links για απόδοση
            int maxLinksToCheck = Math.min(links.size(), 5);

            for (int i = 0; i < maxLinksToCheck; i++) {
                 try {
                     WebElement link = links.get(i);
                     String linkUrl = link.getAttribute("href");
                     if (linkUrl == null || linkUrl.isEmpty()) continue;

                     if (linkUrl.startsWith("javascript:") || linkUrl.startsWith("#")) {
                         continue;
                     }

                     linksChecked++;
                     HttpURLConnection conn = (HttpURLConnection) new URL(linkUrl).openConnection();
                     conn.setRequestMethod("HEAD");
                     conn.setConnectTimeout(3000);
                     conn.connect();

                     if (conn.getResponseCode() >= 400) broken++;
                 } catch (Exception ignored) {}
            }
            //Εφαρμογή ποινής για broken links
            if (broken > 0) basicScore -= Math.min(broken * 4, 20);

            report.append("Broken Links (checked ").append(linksChecked)
                    .append(" of ").append(links.size()).append("): ").append(broken).append("\n");



            // 8. ΒΑΣΙΚΗ ΒΑΘΜΟΛΟΓΙΑ
            int finalBasicScore = Math.max(basicScore, 0);
            report.append("\nBASIC SCORE: ").append(finalBasicScore).append("/100\n");

            String basicRating = getRating(finalBasicScore);
            report.append("BASIC RATING: ").append(basicRating).append("\n");


            // 9. ΕΛΕΓΧΟΣ GDPR ΣΥΜΜΟΡΦΩΣΗΣ


            report.append("\n").append("-".repeat(50)).append("\n");
            GDPRChecker.GDPRResult gdprResult = GDPRChecker.checkGDPRCompliance(driver);
            report.append(gdprResult.report);


            // 10. ΤΕΛΙΚΗ ΣΥΝΔΥΑΣΜΕΝΗ ΒΑΘΜΟΛΟΓΙΑ
            //60% βασική βαθμολογία + 40% GDPR βαθμολογία
            int gdprScore = gdprResult.score;
            int combinedScore = (int)(finalBasicScore * 0.6 + gdprScore * 0.4);

            report.append("\n").append("-".repeat(50)).append("\n");
            report.append("FINAL COMBINED SCORE: ").append(combinedScore).append("/100\n");

            String finalRating = getRating(combinedScore);
            report.append("FINAL RATING: ").append(finalRating).append("\n");


            // 11. ΣΥΣΤΑΣΕΙΣ ΒΕΛΤΙΩΣΗΣ
            if (!gdprResult.recommendations.isEmpty()) {
                report.append("\nGDPR RECOMMENDATIONS:\n");
                for (String recommendation : gdprResult.recommendations) {
                    report.append(recommendation).append("\n");
                }
            }

            // Προτάσεις για γενική βελτίωση
            report.append("\nGENERAL RECOMMENDATIONS:\n");
            if (loadTime > 4000) {
                report.append("• Optimize website loading speed\n");
            }
            if (!url.startsWith("https")) {
                report.append("• Enable HTTPS/SSL for security\n");
            }
            if (broken > 0) {
                report.append("• Fix broken links\n");
            }


        } catch (Exception e) {
            report.append("Error: ").append(e.getMessage()).append("\n");
        } finally {


            //Κλείσιμο του browser ακόμα και αν προέκυψε σφάλμα
            if (driver != null) {
                driver.quit();
            }
        }

        return report.toString();
    }

    static String getRating(int score) {
        if (score >= 85) {
            return "EXCELLENT";
        } else if (score >= 70) {
            return "GOOD";
        } else if (score >= 50) {
            return "FAIR";
        } else if (score >= 30) {
            return "NEEDS IMPROVEMENT";
        } else {
            return "POOR";
        }
    }
}