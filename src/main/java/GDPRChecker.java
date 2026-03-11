import org.openqa.selenium.*;
import java.util.List;
import java.util.ArrayList;

public class GDPRChecker {
    //Κύρια μέθοδος που εκτελεί τον έλεγχο GDPR συμμόρφωσης για μια ιστοσελίδα
    public static GDPRResult checkGDPRCompliance(WebDriver driver) {

        GDPRResult result = new GDPRResult();
        StringBuilder report = new StringBuilder();
        List<String> recommendations = new ArrayList<>();
        int totalScore = 0;
        int maxScore = 100;

        report.append("\nGDPR COMPLIANCE CHECK\n");

        try {
            // 1. ΕΛΕΓΧΟΣ ΠΟΛΙΤΙΚΗΣ ΑΠΟΡΡΗΤΟΥ-- Privacy Policy Check (25 points)
            int privacyScore = checkPrivacyPolicy(driver);
            totalScore += privacyScore;
            report.append("Privacy Policy: ").append(privacyScore).append("/25\n");
            if (privacyScore < 15) {
                recommendations.add("• Προσθέστε σελίδα Πολιτικής Απορρήτου");
                recommendations.add("• Σύνδεσμος για Πολιτική Απορρήτου πρέπει να είναι ορατός");
            }

            // 2. ΕΛΕΓΧΟΣ Cookies Consent Banner (25 points)
            int cookiesScore = checkCookiesConsent(driver);
            totalScore += cookiesScore;
            report.append("Cookies Consent: ").append(cookiesScore).append("/25\n");
            if (cookiesScore < 15) {
                recommendations.add("• Προσθέστε cookie consent banner");
                recommendations.add("• Οι χρήστες πρέπει να μπορούν να αποδεχτούν/απορρίψουν cookies");
            }

            // 3. ΕΛΕΓΧΟΣ ΠΛΗΡΟΦΟΡΙΩΝ ΕΠΕΞΕΡΓΑΣΙΑΣ ΔΕΔΟΜΕΝΩΝ -- Data Processing Information (20 points)
            int dataProcessingScore = checkDataProcessingInfo(driver);
            totalScore += dataProcessingScore;
            report.append("Data Processing Info: ").append(dataProcessingScore).append("/20\n");
            if (dataProcessingScore < 12) {
                recommendations.add("• Καταγράψτε πώς επεξεργάζεστε τα δεδομένα");
                recommendations.add("• Ορίστε σκοπό επεξεργασίας για κάθε κατηγορία δεδομένων");
            }

            // 4.ΕΛΕΓΧΟΣ ΠΛΗΡΟΦΟΡΙΩΝ ΓΙΑ ΔΙΚΑΙΩΜΑΤΑ ΧΡΗΣΤΩΝ -- User Rights Information (20 points)
            int userRightsScore = checkUserRightsInfo(driver);
            totalScore += userRightsScore;
            report.append("User Rights Info: ").append(userRightsScore).append("/20\n");
            if (userRightsScore < 12) {
                recommendations.add("• Ενημερώστε τους χρήστες για τα δικαιώματά τους (πρόσβαση, διόρθωση, διαγραφή)");
                recommendations.add("• Παροχή πληροφοριών για άσκηση δικαιωμάτων");
            }

            // 5.ΕΛΕΓΧΟΣ ΣΤΟΙΧΕΙΩΝ ΕΠΙΚΟΙΝΩΝΙΑΣ -- Contact Information (10 points)
            int contactScore = checkContactInformation(driver);
            totalScore += contactScore;
            report.append("Contact Information: ").append(contactScore).append("/10\n");
            if (contactScore < 6) {
                recommendations.add("• Προσθέστε επαφή για Υπεύθυνο Προστασίας Δεδομένων (DPO)");
                recommendations.add("• Προσφέρετε έγκυρες επαφές για ερωτήσεις περί προστασίας δεδομένων");
            }

            // Αποθήκευση συνολικής βαθμολογίας
            result.score = totalScore;
            result.recommendations = recommendations;

            // Προσθήκη συνολικής βαθμολογίας στο report
            report.append("\n").append("═".repeat(40)).append("\n");
            report.append("GDPR TOTAL SCORE: ").append(totalScore).append("/").append(maxScore).append("\n");

            String gdprRating = getGDPRRating(totalScore);

            report.append("GDPR RATING: ").append(gdprRating).append("\n");

            result.report = report.toString();

        } catch (Exception e) {
            //χειρισμός σφαλμάτων κατά τον έλεγχο
            result.score = 0;
            result.report = "\nGDPR COMPLIANCE CHECK\nΣφάλμα κατά τον έλεγχο GDPR: " + e.getMessage() + "\n";
            result.recommendations = new ArrayList<>();
        }

        return result;
    }
    // Μέθοδος για έλεγχο ύπαρξης και ορατότητας πολιτικής απορρήτου
    public static int checkPrivacyPolicy(WebDriver driver) {
        int score = 0;

        try {
            // Βρίσκουμε όλους τους συνδέσμους στη σελίδα
            List<WebElement> allLinks = driver.findElements(By.tagName("a"));
            List<WebElement> privacyLinks = new ArrayList<>();
            String[] privacyKeywords = {
                    "privacy", "confidentiality", "gdpr", "data protection",
                    "privacy policy", "πολιτική απορρήτου", "απορρήτου",
                    "προστασία δεδομένων", "προστασία προσωπικών δεδομένων"
            };
            // Εύρεση αυτών που αφορούν την πολιτική απορρήτου
            for (WebElement link : allLinks) {
                String linkText = link.getText().toLowerCase();
                String href = link.getAttribute("href");

                if (href != null) {
                    href = href.toLowerCase();
                    for (String keyword : privacyKeywords) {
                        if (linkText.contains(keyword) || href.contains(keyword)) {
                            privacyLinks.add(link);
                            break;
                        }
                    }
                }
            }
            // Αν βρέθηκαν σύνδεσμοι για πολιτική απορρήτου
            if (!privacyLinks.isEmpty()) {
                score += 15; // Βασικοί βαθμοί για ύπαρξη συνδέσμου

                // Έλεγχος αν ο σύνδεσμος είναι ορατός και ενεργός
                for (WebElement link : privacyLinks) {
                    if (link.isDisplayed() && link.isEnabled()) {
                        score += 10; // Επιπλέον βαθμοί για απλότητα
                        break;
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Σφάλμα κατά τον έλεγχο ύπαρξης πολιτικής απορρήτου:" + e.getMessage());

        }
        return score;

    }
    // Μέθοδος για έλεγχο ύπαρξης και λειτουργικότητας cookie consent banner
    public static int checkCookiesConsent(WebDriver driver) {
        int score = 0;
        // CSS selectors για κοινά cookie banners
        try {
            String[] cookieSelectors = {
                    "[class*='cookie']", "[id*='cookie']",
                    "[class*='gdpr']", "[id*='gdpr']",
                    "[class*='consent']", "[id*='consent']",
                    "[class*='banner']", "[id*='banner']",
                    "#cookie-banner", ".cookie-consent",
                    "#gdpr-banner", ".gdpr-consent"
            };
            //Έλεγχος για ύπαρξη banner με χρήση διαφορετικών selectors
            boolean foundBanner = false;
            for (String selector : cookieSelectors) {
                try {
                    List<WebElement> elements = driver.findElements(By.cssSelector(selector));
                    if (!elements.isEmpty()) {
                        foundBanner = true;
                        break;
                    }
                } catch (Exception e) {
                    System.err.println("Σφάλμα στη σύνταξη του selector:" + e.getMessage());
                }
            }

            String pageText = driver.findElement(By.tagName("body")).getText().toLowerCase();
            String[] cookieKeywords = {
                    "cookie", "cookies", "συμφωνώ", "αποδέχομαι",
                    "accept", "agree", "συγκατάθεση", "αποδοχή",
                    "ψήφισα", "συναινώ", "αποδοχή cookies"
            };

            boolean foundCookieText = false;
            for (String keyword : cookieKeywords) {
                if (pageText.contains(keyword)) {
                    foundCookieText = true;
                    break;
                }
            }
            //Υπολογισμός βαθμολογίας
            if (foundBanner) {
                score += 20; // Υψηλή βαθμολογία για ύπαρξη banner
            }

            if (foundCookieText) {
                score += 5; // Επιπρόσθετοι βαθμοί για αναφορά σε cookies
            }

        } catch (Exception e) {
            System.err.println("Σφάλμα κατά τον έλεγχο ύπαρξης cookie consent banner" + e.getMessage());

        }
        return score;

    }
    // Μέθοδος για έλεγχο πληροφοριών σχετικά με επεξεργασία δεδομένων
    public static int checkDataProcessingInfo(WebDriver driver) {
        int score = 0;

        try {
            //Ανάγνωση όλου του κειμένου της σελίδας
            String pageText = driver.findElement(By.tagName("body")).getText().toLowerCase();

            String[] dataProcessingKeywords = {
                    "data processing", "process data", "collect data", "store data",
                    "personal data", "data collection", "data storage", "data retention",
                    "data security", "data protection", "privacy by design",
                    "επεξεργασία δεδομένων", "συλλογή δεδομένων", "αποθήκευση δεδομένων",
                    "προσωπικά δεδομένα", "βάση δεδομένων", "πληροφορίες", "στοιχεία",
                    "αποθήκευση", "επεξεργασία", "διαχείριση δεδομένων", "αρχείο",
                    "διατήρηση δεδομένων", "ασφάλεια δεδομένων", "προστασία δεδομένων",
                    "συμπληρωματικά δεδομένα", "ανώνυμα δεδομένα", "κωδικοποίηση δεδομένων"
            };

            // Μέτρηση πόσες από τις λέξεις-κλειδιά βρέθηκαν
            int foundKeywords = 0;
            for (String keyword : dataProcessingKeywords) {
                if (pageText.contains(keyword)) {
                    foundKeywords++;
                }
            }

            // 4 βαθμοί για κάθε λέξη-κλειδί που βρέθηκε, μέχρι 20 συνολικά
            score = Math.min(foundKeywords * 4, 20);

        } catch (Exception e) {
            System.err.println("Σφάλμα κατά τον έλεγχο επεξεργασίας δεδομένων:" + e.getMessage());

        }

        return score;
    }
    //Μέθοδος για έλεγχο πληροφοριών σχετικά με τα δικαιώματα χρηστών
    public static int checkUserRightsInfo(WebDriver driver) {
        int score = 0;

        try {

            String pageText = driver.findElement(By.tagName("body")).getText().toLowerCase();

            String[] userRightsKeywords = {
                    "right to access", "right to rectification", "right to erasure",
                    "right to be forgotten", "data portability", "right to object",
                    "right to restrict processing", "right to data portability",
                    "user rights", "data subject rights", "individual rights",
                    "δικαίωμα πρόσβασης", "δικαίωμα διόρθωσης", "δικαίωμα διαγραφής",
                    "δικαίωμα στην λήθη", "δικαίωμα αντίρρησης", "δικαιώματα του υποκειμένου",
                    "δικαίωμα περιορισμού της επεξεργασίας",
                    "δικαιώματα", "πρόσβαση", "διόρθωση", "διαγραφή", "λήθη", "αντίρρηση",
                    "δικαίωμα ενημέρωσης", "δικαίωμα ενστάσεως", "δικαίωμα προσφυγής"
            };
            //Μέτρηση πόσες λέξεις βρέθηκαν
            int foundKeywords = 0;
            for (String keyword : userRightsKeywords) {
                if (pageText.contains(keyword)) {
                    foundKeywords++;
                }
            }
            //4 βαθμοί για κάθε λέξη που βρέθηκε, σύνολο μέχρι 20
            score = Math.min(foundKeywords * 4, 20);

        } catch (Exception e) {
            System.err.println("Σφάλμα κατά τον έλεγχο για τα δικαιώματα χρηστών:" + e.getMessage());

        }

        return score;
    }
    //Μέθοδος για έλεγχο στοιχείων επικοινωνίας και Υπεύθυνο προστασίας δεδομένων
    public static int checkContactInformation(WebDriver driver) {
        int score = 0;

        try {

            String pageText = driver.findElement(By.tagName("body")).getText().toLowerCase();

            //Έλεγχος για ύπαρξη email(είτε στο κείμενο είτε στο mailto link)
            boolean hasEmail = pageText.contains("@") ||
                    !driver.findElements(By.xpath("//a[contains(@href, 'mailto:')]")).isEmpty();

            // Έλεγχος για ύπαρξη φόρμας επικοινωνίας
            boolean hasContactForm = !driver.findElements(By.tagName("form")).isEmpty();

            // Έλεγχος για αναφορά σε DPO(Data Protection Officer)
            boolean hasDPO = pageText.contains("dpo") ||
                    pageText.contains("data protection officer") ||
                    pageText.contains("υπεύθυνος προστασίας δεδομένων");

            //Έλεγχος για σύνδεσμο σε σελίδα επικοινωνίας
            boolean hasContactPage = false;
            List<WebElement> links = driver.findElements(By.tagName("a"));
            for (WebElement link : links) {
                String linkText = link.getText().toLowerCase();
                if (linkText.contains("contact") || linkText.contains("επικοινωνία") ||
                        linkText.contains("get in touch") || linkText.contains("επαφή")) {
                    hasContactPage = true;
                    break;
                }
            }

            // Υπολογισμός βαθμολογίας με βάση τα αποτελέσματα από παραπάνω
            if (hasEmail) score += 3;
            if (hasContactForm) score += 3;
            if (hasDPO) score += 2;
            if (hasContactPage) score += 2;

        } catch (Exception e) {
            System.err.println("Σφάλμα κατά τον έλεγχο για στοιχεία επικοινωνίας και DPO:" + e.getMessage());

        }
        return score;

    }
    // Βοηθητική μέθοδος για μετατροπή βαθμολογίας σε κείμενο rating
    static String getGDPRRating(int score) {
        if (score >= 80) {
            return "EXCELLENT"; //80-100: Άριστη συμμόρφωση
        } else if (score >= 60) {
            return "GOOD";      //60-79: Καλή συμμόρφωση
        } else if (score >= 40) {
            return "FAIR";      // 40-59: Μέτρια συμμόρφωση
        } else if (score >= 20) {
            return "NEEDS IMPROVEMENT"; //20-39: Χρειάζεται βελτίωση
        } else {
            return "POOR";      //0-19: Κακή συμμόρφωση
        }
    }
    // Εσωτερική κλάση για αποθήκευση αποτελεσμάτων του ελέγχου
    public static class GDPRResult {
        public int score; // Συνολική βαθμολογία(0-100)
        public String report; // Αναφορά με αναλυτικά αποτελέσματα
        public List<String> recommendations; // Λίστα με συστάσεις βελτίωσης
    }
}