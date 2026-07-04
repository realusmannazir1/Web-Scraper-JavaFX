import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ScrapeMasterSingle {

    public static void main(String[] args) {
        String url = "https://books.toscrape.com/";
        String outputFile = "scraped_single.txt";

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .followRedirects(true)
                    .get();

            Elements links     = doc.select("a[href]");
            Elements headlines = doc.select("h1, h2, h3");
            String   metaDesc  = doc.select("meta[name=description]").attr("content");

            try (PrintWriter pw = new PrintWriter(new FileWriter(outputFile))) {
                pw.println("Source Website: " + url);
                pw.println();

                pw.println("=== LINKS ===");
                for (Element link : links) {
                    String href = link.attr("abs:href");
                    if (!href.isEmpty()) pw.println(href);
                }

                pw.println();
                pw.println("=== HEADLINES ===");
                for (Element h : headlines) {
                    pw.println("[" + h.tagName().toUpperCase() + "] " + h.text());
                }

                pw.println();
                pw.println("=== META DESCRIPTION ===");
                pw.println(metaDesc.isEmpty() ? "(none)" : metaDesc);
            }

            System.out.println("Done! Data saved to: " + outputFile);

        } catch (IOException e) {
            System.err.println("Error scraping " + url + ": " + e.getMessage());
        }
    }
}
