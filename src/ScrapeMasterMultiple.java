import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScrapeMasterMultiple {

    // ── Data container ────────────────────────────────────────────────────────
    static class PageData {
        String       url;
        List<String> links       = new ArrayList<>();
        List<String> headlines   = new ArrayList<>();
        String       metaDesc    = "";

        PageData(String url) { this.url = url; }
    }

    // ── Entry point ───────────────────────────────────────────────────────────
    public static void main(String[] args) {
        String[] seedUrls = {
            "https://books.toscrape.com/",
            "https://quotes.toscrape.com/",
            "https://toscrape.com/"
        };

        List<PageData> allData = new ArrayList<>();

        for (String url : seedUrls) {
            System.out.println("Scraping: " + url);
            allData.addAll(scrapeWithPagination(url));
        }

        saveAsCSV(allData,  "scraped_data.csv");
        saveAsJSON(allData, "scraped_data.json");

        System.out.println("Done! Files: scraped_data.csv / scraped_data.json");
    }

    // ── Scrape a site and follow pagination ───────────────────────────────────
    private static List<PageData> scrapeWithPagination(String startUrl) {
        List<PageData> results = new ArrayList<>();
        String currentUrl = startUrl;
        int    maxPages   = 5;   // safety cap

        while (currentUrl != null && maxPages-- > 0) {
            try {
                Document doc = Jsoup.connect(currentUrl)
                        .userAgent("Mozilla/5.0")
                        .timeout(10000)
                        .followRedirects(true)
                        .get();

                PageData pd = new PageData(currentUrl);

                for (Element a : doc.select("a[href]")) {
                    String href = a.attr("abs:href");
                    if (!href.isEmpty()) pd.links.add(href);
                }

                for (Element h : doc.select("h1, h2, h3"))
                    pd.headlines.add("[" + h.tagName().toUpperCase() + "] " + h.text());

                pd.metaDesc = doc.select("meta[name=description]").attr("content");

                results.add(pd);
                System.out.println("  Scraped page: " + currentUrl);

                // ── Pagination: look for "next" link ──────────────────────────
                currentUrl = detectNextPage(doc, currentUrl);

            } catch (IOException e) {
                System.err.println("  Failed: " + currentUrl + " — " + e.getMessage());
                break;
            }
        }
        return results;
    }

    // ── Detect "Next" / "Page 2" link ─────────────────────────────────────────
    private static String detectNextPage(Document doc, String currentUrl) {
        // Common patterns: rel="next", text "next", "»", "page 2", etc.
        for (Element a : doc.select("a[href]")) {
            String text = a.text().trim().toLowerCase();
            String rel  = a.attr("rel").toLowerCase();
            if (rel.equals("next") || text.equals("next") || text.equals("»")
                    || text.matches(".*page\\s*2.*") || text.equals(">")) {
                String next = a.attr("abs:href");
                if (!next.isEmpty() && !next.equals(currentUrl)) return next;
            }
        }
        return null; // no next page found
    }

    // ── Save as CSV ───────────────────────────────────────────────────────────
    private static void saveAsCSV(List<PageData> data, String filename) {
        try (FileWriter fw = new FileWriter(filename)) {
            fw.write("Website URL,Links,Headlines,Meta Description\n");
            for (PageData pd : data) {
                fw.write(csvField(pd.url) + ","
                       + csvField(pd.links.toString()) + ","
                       + csvField(pd.headlines.toString()) + ","
                       + csvField(pd.metaDesc) + "\n");
            }
            System.out.println("CSV saved: " + filename);
        } catch (IOException e) {
            System.err.println("CSV write error: " + e.getMessage());
        }
    }

    // Wrap value in quotes and escape internal quotes
    private static String csvField(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    // ── Save as JSON ──────────────────────────────────────────────────────────
    private static void saveAsJSON(List<PageData> data, String filename) {
        try (FileWriter fw = new FileWriter(filename)) {
            fw.write("[\n");
            for (int i = 0; i < data.size(); i++) {
                PageData pd = data.get(i);
                fw.write("  {\n");
                fw.write("    \"website_url\": "     + jsonStr(pd.url)      + ",\n");
                fw.write("    \"links\": "           + jsonArray(pd.links)  + ",\n");
                fw.write("    \"headlines\": "       + jsonArray(pd.headlines) + ",\n");
                fw.write("    \"meta_description\": "+ jsonStr(pd.metaDesc) + "\n");
                fw.write("  }" + (i < data.size() - 1 ? "," : "") + "\n");
            }
            fw.write("]\n");
            System.out.println("JSON saved: " + filename);
        } catch (IOException e) {
            System.err.println("JSON write error: " + e.getMessage());
        }
    }

    private static String jsonStr(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static String jsonArray(List<String> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(jsonStr(list.get(i)));
            if (i < list.size() - 1) sb.append(", ");
        }
        return sb.append("]").toString();
    }
}
