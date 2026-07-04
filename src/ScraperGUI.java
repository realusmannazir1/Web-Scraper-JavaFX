import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class ScraperGUI extends JFrame {

    private static final long serialVersionUID = 1L;

    // ── Colours ───────────────────────────────────────────────────────────────
    private static final Color BG        = new Color(15,  17,  26);
    private static final Color PANEL_BG  = new Color(22,  25,  37);
    private static final Color CARD_BG   = new Color(30,  34,  50);
    private static final Color ACCENT    = new Color(99, 179, 237);
    private static final Color ACCENT2   = new Color(154, 117, 234);
    private static final Color SUCCESS   = new Color(72,  199, 142);
    private static final Color WARN      = new Color(255, 183,  77);
    private static final Color TEXT      = new Color(220, 225, 240);
    private static final Color SUBTEXT   = new Color(120, 130, 160);
    private static final Color INPUT_BG  = new Color(18,  21,  33);
    private static final Color BORDER_C  = new Color(45,  50,  75);

    // ── UI components ─────────────────────────────────────────────────────────
    private JTextField        urlField;
    private JButton           scrapeBtn, clearBtn, exportBtn, importBtn;
    private JLabel            statusLabel, statsLabel;
    private JProgressBar      progressBar;
    private JProgressBar      batchProgress;
    private JTabbedPane       tabs;
    private JTextArea         linksArea, headlinesArea, metaArea, rawArea;
    private JTextArea         pricesArea, jobsArea, leadsArea, newsArea;
    private DefaultTableModel tableModel;
    private JTable            historyTable;
    private JPanel            statsPanel;
    private JLabel            linkCount, headlineCount, sessionCount;

    private final transient List<ScrapeResult> history = new ArrayList<>();
    private int totalScrapes = 0;

    // ── Entry point ───────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ScraperGUI::create);
    }

    private static void create() {
        new ScraperGUI().setVisible(true);
    }

    // ── Constructor ───────────────────────────────────────────────────────────
    @SuppressWarnings("this-escape")
    public ScraperGUI() {
        super("ScrapeMaster  ·  Web Scraper");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 750);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(0, 0));
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildCenter(),  BorderLayout.CENTER);
        add(buildFooter(),  BorderLayout.SOUTH);
        setStatus("Ready — paste a URL and click Scrape", SUBTEXT);
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(PANEL_BG);
        header.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, BORDER_C),
            new EmptyBorder(14, 20, 14, 20)
        ));

        // Logo + title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        JLabel logo = new JLabel("⬡");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        logo.setForeground(ACCENT);
        JLabel title = new JLabel("ScrapeMaster");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT);
        JLabel badge = pill("v1.0", ACCENT2);
        left.add(logo); left.add(title); left.add(badge);

        // Stats row
        statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        statsPanel.setOpaque(false);
        sessionCount  = statChip("Sessions",  "0", ACCENT);
        linkCount     = statChip("Links",      "0", SUCCESS);
        headlineCount = statChip("Headlines",  "0", WARN);
        statsPanel.add(sessionCount);
        statsPanel.add(linkCount);
        statsPanel.add(headlineCount);

        header.add(left,       BorderLayout.WEST);
        header.add(statsPanel, BorderLayout.EAST);
        return header;
    }

    // ── Center ────────────────────────────────────────────────────────────────
    private JSplitPane buildCenter() {
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                buildInputPanel(), buildResultsPanel());
        split.setDividerLocation(130);
        split.setDividerSize(4);
        split.setBackground(BG);
        split.setBorder(null);
        return split;
    }

    // ── Input panel ───────────────────────────────────────────────────────────
    private JPanel buildInputPanel() {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setBackground(PANEL_BG);
        p.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, BORDER_C),
            new EmptyBorder(16, 20, 16, 20)
        ));

        JLabel lbl = new JLabel("URL");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(SUBTEXT);
        lbl.setBorder(new EmptyBorder(0, 0, 6, 0));

        urlField = new JTextField("https://");
        urlField.setFont(new Font("Segoe UI Mono", Font.PLAIN, 14));
        urlField.setBackground(INPUT_BG);
        urlField.setForeground(TEXT);
        urlField.setCaretColor(ACCENT);
        urlField.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        urlField.addActionListener(e -> triggerScrape());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(8, 0, 0, 0));

        scrapeBtn = fancyButton("▶  Scrape",       ACCENT,   BG);
        clearBtn  = fancyButton("✕  Clear",         CARD_BG,  SUBTEXT);
        exportBtn = fancyButton("↓  Export",        CARD_BG,  SUCCESS);
        importBtn = fancyButton("📂  Import URLs",  CARD_BG,  ACCENT2);
        exportBtn.setEnabled(false);

        scrapeBtn.addActionListener(e -> triggerScrape());
        clearBtn .addActionListener(e -> clearAll());
        exportBtn.addActionListener(e -> exportCurrent());
        importBtn.addActionListener(e -> importAndScrape());

        btnRow.add(scrapeBtn); btnRow.add(clearBtn); btnRow.add(exportBtn); btnRow.add(importBtn);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(lbl,      BorderLayout.NORTH);
        top.add(urlField, BorderLayout.CENTER);
        top.add(btnRow,   BorderLayout.SOUTH);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setBackground(INPUT_BG);
        progressBar.setForeground(ACCENT);
        progressBar.setBorderPainted(false);
        progressBar.setPreferredSize(new Dimension(0, 3));

        batchProgress = new JProgressBar();
        batchProgress.setIndeterminate(false);
        batchProgress.setBackground(INPUT_BG);
        batchProgress.setForeground(ACCENT2);
        batchProgress.setBorderPainted(false);
        batchProgress.setStringPainted(true);
        batchProgress.setFont(new Font("Segoe UI", Font.BOLD, 10));
        batchProgress.setForeground(ACCENT2);
        batchProgress.setPreferredSize(new Dimension(0, 16));
        batchProgress.setVisible(false);

        JPanel bars = new JPanel(new BorderLayout(0, 2));
        bars.setOpaque(false);
        bars.add(progressBar,  BorderLayout.NORTH);
        bars.add(batchProgress, BorderLayout.SOUTH);

        p.add(top,  BorderLayout.CENTER);
        p.add(bars, BorderLayout.SOUTH);
        return p;
    }

    // ── Results panel ─────────────────────────────────────────────────────────
    private JTabbedPane buildResultsPanel() {
        tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setBackground(BG);
        tabs.setForeground(TEXT);
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabs.setBorder(new EmptyBorder(0, 0, 0, 0));

        linksArea     = resultArea();
        headlinesArea = resultArea();
        metaArea      = resultArea();
        rawArea       = resultArea();
        pricesArea    = resultArea();
        jobsArea      = resultArea();
        leadsArea     = resultArea();
        newsArea      = resultArea();
        rawArea.setFont(new Font("Segoe UI Mono", Font.PLAIN, 12));

        tabs.addTab("🔗  Links",        wrapScroll(linksArea));
        tabs.addTab("📰  Headlines",    wrapScroll(headlinesArea));
        tabs.addTab("📋  Meta",         wrapScroll(metaArea));
        tabs.addTab("💰  Prices",       wrapScroll(pricesArea));
        tabs.addTab("💼  Jobs",         wrapScroll(jobsArea));
        tabs.addTab("📧  Leads",        wrapScroll(leadsArea));
        tabs.addTab("📡  News",         wrapScroll(newsArea));
        tabs.addTab("📄  Raw Output",   wrapScroll(rawArea));
        tabs.addTab("🕘  History",      buildHistoryPanel());
        tabs.addTab("ℹ️  Use Cases",    buildUseCasesPanel());

        styleTabPane(tabs);
        return tabs;
    }

    // ── Use Cases info panel ──────────────────────────────────────────────────
    private JPanel buildUseCasesPanel() {
        JTextArea area = resultArea();
        area.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        area.setText(
            "  WHAT IS A WEB SCRAPER FOR?\n" +
            "  ─────────────────────────────────────────────────────────────────\n\n" +
            "  A web scraper is used to automatically extract data from websites\n" +
            "  instead of copying it manually.\n\n" +
            "  Core Purpose:\n" +
            "  👉 Turn unstructured web content into structured data you can\n" +
            "     analyze, store, or use in apps.\n\n" +
            "  ─────────────────────────────────────────────────────────────────\n" +
            "  REAL USE CASES (what people actually do)\n" +
            "  ─────────────────────────────────────────────────────────────────\n\n" +
            "  1. DATA COLLECTION FOR ANALYSIS\n" +
            "     • Collect product prices from e-commerce sites\n" +
            "     • Gather job listings\n" +
            "     • Extract datasets for ML / data science\n" +
            "     👉 Used for: EDA (Exploratory Data Analysis), Training models\n\n" +
            "  2. PRICE MONITORING / COMPETITOR TRACKING  (see Prices tab)\n" +
            "     • Track price changes (Amazon, Daraz, etc.)\n" +
            "     • Monitor competitors\n" +
            "     👉 Used in: E-commerce, Business intelligence\n\n" +
            "  3. LEAD GENERATION  (see Leads tab)\n" +
            "     • Extract emails, company info, contacts\n" +
            "     👉 Used in: Marketing, Sales outreach\n\n" +
            "  4. NEWS & CONTENT AGGREGATION  (see News tab)\n" +
            "     • Collect news articles or posts\n" +
            "     • Build your own dashboard or app\n\n" +
            "  5. AUTOMATION (saving time)\n" +
            "     Instead of manually copying data → script does it in seconds\n\n" +
            "     Example:\n" +
            "     ❌ Manually copying 100 product prices\n" +
            "     ✔  Write scraper → get all prices in CSV in seconds\n\n" +
            "  ─────────────────────────────────────────────────────────────────\n" +
            "  TOOLS (relevant to you — Python)\n" +
            "  ─────────────────────────────────────────────────────────────────\n\n" +
            "     BeautifulSoup  →  basic scraping\n" +
            "     Requests       →  get webpage data\n" +
            "     Selenium       →  dynamic sites (buttons, login)\n" +
            "     Scrapy         →  large-scale scraping\n" +
            "     Jsoup (Java)   →  what ScrapeMaster uses\n\n" +
            "  ─────────────────────────────────────────────────────────────────\n" +
            "  ⚠️  IMPORTANT REALITY — DON'T IGNORE THIS\n" +
            "  ─────────────────────────────────────────────────────────────────\n\n" +
            "  Not all websites allow scraping. You must check:\n" +
            "     • Terms of Service\n" +
            "     • robots.txt  →  site.com/robots.txt\n\n" +
            "  ✅ Allowed : public data, robots.txt permits it, personal/research\n" +
            "  ❌ Not OK  : behind login, site blocks it, reselling scraped data\n" +
            "  👉 Otherwise: you can get blocked or banned\n"
        );
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(CARD_BG);
        p.add(wrapScroll(area), BorderLayout.CENTER);
        return p;
    }

    // ── History table ─────────────────────────────────────────────────────────
    private JPanel buildHistoryPanel() {
        String[] cols = {"#", "URL", "Links", "Headlines", "Meta", "Time", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        historyTable = new JTable(tableModel);
        historyTable.setBackground(CARD_BG);
        historyTable.setForeground(TEXT);
        historyTable.setGridColor(BORDER_C);
        historyTable.setRowHeight(28);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        historyTable.getTableHeader().setBackground(PANEL_BG);
        historyTable.getTableHeader().setForeground(SUBTEXT);
        historyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        historyTable.setSelectionBackground(new Color(50, 60, 90));
        historyTable.setSelectionForeground(TEXT);
        historyTable.getColumnModel().getColumn(0).setMaxWidth(40);
        historyTable.getColumnModel().getColumn(2).setMaxWidth(70);
        historyTable.getColumnModel().getColumn(3).setMaxWidth(90);
        historyTable.getColumnModel().getColumn(5).setMaxWidth(90);
        historyTable.getColumnModel().getColumn(6).setMaxWidth(80);

        // colour Status column
        historyTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setForeground("OK".equals(v) ? SUCCESS : new Color(255, 100, 100));
                setBackground(sel ? new Color(50, 60, 90) : CARD_BG);
                setHorizontalAlignment(CENTER);
                return this;
            }
        });

        JScrollPane sp = new JScrollPane(historyTable);
        sp.getViewport().setBackground(CARD_BG);
        sp.setBorder(BorderFactory.createEmptyBorder());

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(CARD_BG);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    // ── Footer ────────────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(PANEL_BG);
        footer.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 0, 0, BORDER_C),
            new EmptyBorder(6, 20, 6, 20)
        ));
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(SUBTEXT);

        statsLabel = new JLabel("Output saved to: —");
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statsLabel.setForeground(SUBTEXT);

        footer.add(statusLabel, BorderLayout.WEST);
        footer.add(statsLabel,  BorderLayout.EAST);
        return footer;
    }

    // ── Scrape trigger ────────────────────────────────────────────────────────
    private void triggerScrape() {
        String url = urlField.getText().trim();
        if (url.isEmpty() || url.equals("https://")) {
            setStatus("Please enter a URL", WARN); return;
        }
        if (!url.startsWith("http")) url = "https://" + url;
        final String finalUrl = url;

        scrapeBtn.setEnabled(false);
        progressBar.setIndeterminate(true);
        setStatus("Scraping " + finalUrl + " …", ACCENT);

        new SwingWorker<ScrapeResult, Void>() {
            protected ScrapeResult doInBackground() {
                return scrape(finalUrl);
            }
            protected void done() {
                try {
                    ScrapeResult r = get();
                    displayResult(r);
                    saveResult(r);
                    history.add(r);
                    addToHistory(r);
                    updateStats(r);
                    exportBtn.setEnabled(true);
                    setStatus("Done — " + r.links.size() + " links, "
                            + r.headlines.size() + " headlines", SUCCESS);
                } catch (Exception ex) {
                    setStatus("Error: " + ex.getMessage(), new Color(255, 100, 100));
                } finally {
                    scrapeBtn.setEnabled(true);
                    progressBar.setIndeterminate(false);
                }
            }
        }.execute();
    }

    // ── Import URLs from CSV / TXT / XLS ─────────────────────────────────────
    private void importAndScrape() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Import URLs — CSV, TXT, or Excel (.csv / .txt / .xls / .xlsx)");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "URL files (*.csv, *.txt, *.xls, *.xlsx)", "csv", "txt", "xls", "xlsx"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        List<String> urls = extractUrlsFromFile(file);

        if (urls.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No valid URLs found in the file.\n\n" +
                "Make sure each URL starts with http:// or https://",
                "No URLs Found", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Confirm dialog
        int confirm = JOptionPane.showConfirmDialog(this,
            "Found " + urls.size() + " URL(s) in: " + file.getName() +
            "\n\nScrape all of them now?",
            "Import & Scrape", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        runBatchScrape(urls);
    }

    // ── Parse URLs from file ──────────────────────────────────────────────────
    private List<String> extractUrlsFromFile(File file) {
        List<String> urls = new ArrayList<>();
        String name = file.getName().toLowerCase();

        // XLS/XLSX: read as plain text (basic cell extraction without Apache POI)
        // For proper Excel support, we treat .xls/.xlsx as text and regex-scan for URLs
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Split by common CSV delimiters and whitespace
                String[] parts = line.split("[,;\t\" ]+");
                for (String part : parts) {
                    String trimmed = part.trim().replaceAll("^\"|\"$", "");
                    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                        if (!urls.contains(trimmed)) urls.add(trimmed);
                    }
                }
            }
        } catch (IOException e) {
            setStatus("Could not read file: " + e.getMessage(), WARN);
        }
        return urls;
    }

    // ── Batch scrape list of URLs ─────────────────────────────────────────────
    private void runBatchScrape(List<String> urls) {
        scrapeBtn.setEnabled(false);
        importBtn.setEnabled(false);
        batchProgress.setVisible(true);
        batchProgress.setMinimum(0);
        batchProgress.setMaximum(urls.size());
        batchProgress.setValue(0);
        batchProgress.setString("Batch: 0 / " + urls.size());

        new SwingWorker<Void, ScrapeResult>() {
            protected Void doInBackground() throws Exception {
                for (int i = 0; i < urls.size(); i++) {
                    String url = urls.get(i);
                    SwingUtilities.invokeLater(() -> {
                        progressBar.setIndeterminate(true);
                        setStatus("Scraping " + url + " …", ACCENT);
                    });
                    ScrapeResult r = scrape(url);
                    publish(r);
                    final int idx = i + 1;
                    SwingUtilities.invokeLater(() -> {
                        batchProgress.setValue(idx);
                        batchProgress.setString("Batch: " + idx + " / " + urls.size());
                    });
                    Thread.sleep(600); // polite delay between requests
                }
                return null;
            }

            protected void process(List<ScrapeResult> chunks) {
                for (ScrapeResult r : chunks) {
                    displayResult(r);
                    saveResult(r);
                    history.add(r);
                    addToHistory(r);
                    updateStats(r);
                    exportBtn.setEnabled(true);
                }
            }

            protected void done() {
                scrapeBtn.setEnabled(true);
                importBtn.setEnabled(true);
                progressBar.setIndeterminate(false);
                batchProgress.setString("Batch complete: " + urls.size() + " URLs");
                setStatus("Batch done — scraped " + urls.size() + " URLs", SUCCESS);
                tabs.setSelectedIndex(4); // jump to History tab
            }
        }.execute();
    }

    // ── Core scrape logic ─────────────────────────────────────────────────────
    private ScrapeResult scrape(String url) {
        ScrapeResult r = new ScrapeResult(url);
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(12000)
                    .followRedirects(true)
                    .get();

            for (Element a : doc.select("a[href]")) {
                String href = a.attr("abs:href");
                if (!href.isEmpty()) r.links.add(href);
            }
            for (Element h : doc.select("h1, h2, h3"))
                r.headlines.add("[" + h.tagName().toUpperCase() + "] " + h.text());

            r.metaDesc = doc.select("meta[name=description]").attr("content");
            r.title    = doc.title();

            // 💰 Price hints — common price selectors + text matching currency symbols
            for (Element el : doc.select("[class*=price],[class*=cost],[class*=amount],[class*=rate]," +
                                         "[itemprop=price],span,p,div")) {
                String t = el.ownText().trim();
                if (t.matches(".*[\\$\\£\\€\\₨Rs\\.\\,0-9]{2,}.*") && t.length() < 120
                        && !r.priceHints.contains(t))
                    r.priceHints.add(t);
                if (r.priceHints.size() >= 50) break;
            }

            // 💼 Job hints — headings/links containing job keywords
            String jobKw = "(?i).*(job|position|vacancy|hiring|career|role|engineer|developer|analyst|intern).*";
            for (Element el : doc.select("h1,h2,h3,h4,a")) {
                String t = el.text().trim();
                if (t.matches(jobKw) && t.length() < 150 && !r.jobHints.contains(t))
                    r.jobHints.add(t);
                if (r.jobHints.size() >= 50) break;
            }

            // 📧 Leads — mailto, tel links + visible email-like text
            for (Element el : doc.select("a[href^=mailto:],a[href^=tel:]")) {
                String val = el.attr("href").replace("mailto:","").replace("tel:","").trim();
                if (!val.isEmpty() && !r.leads.contains(val)) r.leads.add(val);
            }
            for (Element el : doc.select("*")) {
                String t = el.ownText().trim();
                if (t.matches(".*[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}.*")
                        && t.length() < 100 && !r.leads.contains(t))
                    r.leads.add(t);
                if (r.leads.size() >= 50) break;
            }

            // 📡 News hints — article tags, time elements, news-like headings
            for (Element el : doc.select("article h1,article h2,article h3," +
                                         "[class*=article],[class*=news],[class*=post] h2," +
                                         "[class*=headline],time")) {
                String t = el.text().trim();
                if (!t.isEmpty() && t.length() < 200 && !r.newsHints.contains(t))
                    r.newsHints.add(t);
                if (r.newsHints.size() >= 50) break;
            }
            // fallback: all h2/h3 if nothing found
            if (r.newsHints.isEmpty())
                for (Element el : doc.select("h2,h3")) {
                    String t = el.text().trim();
                    if (!t.isEmpty() && !r.newsHints.contains(t)) r.newsHints.add(t);
                    if (r.newsHints.size() >= 30) break;
                }

            r.ok = true;
        } catch (Exception e) {
            r.error = e.getMessage();
        }
        return r;
    }

    // ── Display in tabs ───────────────────────────────────────────────────────
    private void displayResult(ScrapeResult r) {
        StringBuilder lb = new StringBuilder();
        for (String l : r.links) lb.append(l).append("\n");
        linksArea.setText(lb.toString());

        StringBuilder hb = new StringBuilder();
        for (String h : r.headlines) hb.append(h).append("\n");
        headlinesArea.setText(hb.toString());

        metaArea.setText(
            "Title:            " + r.title + "\n\n" +
            "Meta Description: " + (r.metaDesc.isEmpty() ? "(none)" : r.metaDesc)
        );

        // 💰 Prices — elements likely to contain price text
        StringBuilder pb = new StringBuilder("Extracted price-related content from: " + r.url + "\n\n");
        for (String t : r.priceHints) pb.append("  ").append(t).append("\n");
        if (r.priceHints.isEmpty()) pb.append("  (no price patterns detected on this page)");
        pricesArea.setText(pb.toString());

        // 💼 Jobs — job-like headings and apply links
        StringBuilder jb = new StringBuilder("Extracted job-related content from: " + r.url + "\n\n");
        for (String t : r.jobHints) jb.append("  ").append(t).append("\n");
        if (r.jobHints.isEmpty()) jb.append("  (no job patterns detected on this page)");
        jobsArea.setText(jb.toString());

        // 📧 Leads — emails, phones, contact links
        StringBuilder eb = new StringBuilder("Extracted contact/lead data from: " + r.url + "\n\n");
        for (String t : r.leads) eb.append("  ").append(t).append("\n");
        if (r.leads.isEmpty()) eb.append("  (no emails or phone numbers detected on this page)");
        leadsArea.setText(eb.toString());

        // 📡 News — article-style headlines + dates
        StringBuilder nb = new StringBuilder("Extracted news/article content from: " + r.url + "\n\n");
        for (String t : r.newsHints) nb.append("  ").append(t).append("\n");
        if (r.newsHints.isEmpty()) nb.append("  (no article/news patterns detected on this page)");
        newsArea.setText(nb.toString());

        rawArea.setText(buildRaw(r));
        linksArea.setCaretPosition(0);
        headlinesArea.setCaretPosition(0);
        pricesArea.setCaretPosition(0);
        jobsArea.setCaretPosition(0);
        leadsArea.setCaretPosition(0);
        newsArea.setCaretPosition(0);
    }

    // ── Save TXT + CSV + JSON ─────────────────────────────────────────────────
    private void saveResult(ScrapeResult r) {
        String ts   = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String base = "output_" + ts;

        try (PrintWriter pw = new PrintWriter(new FileWriter(base + ".txt"))) {
            pw.print(buildRaw(r));
        } catch (IOException ignored) {}

        try (FileWriter fw = new FileWriter("scraped_data.csv", true)) {
            boolean empty = new File("scraped_data.csv").length() == 0;
            if (empty) fw.write("Website URL,Title,Links,Headlines,Meta Description\n");
            fw.write(csv(r.url) + "," + csv(r.title) + "," +
                     csv(r.links.toString()) + "," +
                     csv(r.headlines.toString()) + "," +
                     csv(r.metaDesc) + "\n");
        } catch (IOException ignored) {}

        appendJSON(r, "scraped_data.json");

        statsLabel.setText("Saved: " + base + ".txt  |  scraped_data.csv  |  scraped_data.json");
    }

    private void appendJSON(ScrapeResult r, String file) {
        List<String> lines = new ArrayList<>();
        File f = new File(file);
        if (f.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null) lines.add(line);
            } catch (IOException ignored) {}
            // strip closing ]
            while (!lines.isEmpty() && lines.get(lines.size()-1).trim().matches("[\\]\\s]*"))
                lines.remove(lines.size()-1);
        }

        try (FileWriter fw = new FileWriter(file)) {
            if (lines.isEmpty()) {
                fw.write("[\n");
            } else {
                for (String l : lines) fw.write(l + "\n");
                fw.write(",\n");
            }
            fw.write("  {\n");
            fw.write("    \"website_url\": "      + js(r.url)      + ",\n");
            fw.write("    \"title\": "            + js(r.title)    + ",\n");
            fw.write("    \"links\": "            + ja(r.links)    + ",\n");
            fw.write("    \"headlines\": "        + ja(r.headlines)+ ",\n");
            fw.write("    \"meta_description\": " + js(r.metaDesc) + "\n");
            fw.write("  }\n]\n");
        } catch (IOException ignored) {}
    }

    // ── Export current result via dialog ──────────────────────────────────────
    private void exportCurrent() {
        if (history.isEmpty()) return;
        ScrapeResult r = history.get(history.size() - 1);
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Export scraped data");
        fc.setSelectedFile(new File("export.txt"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(fc.getSelectedFile()))) {
                pw.print(buildRaw(r));
                setStatus("Exported to " + fc.getSelectedFile().getName(), SUCCESS);
            } catch (IOException e) {
                setStatus("Export failed: " + e.getMessage(), WARN);
            }
        }
    }

    // ── History table row ─────────────────────────────────────────────────────
    private void addToHistory(ScrapeResult r) {
        totalScrapes++;
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String meta = r.metaDesc.isEmpty() ? "—" :
                      r.metaDesc.substring(0, Math.min(30, r.metaDesc.length())) + "…";
        tableModel.addRow(new Object[]{
            totalScrapes, r.url, r.links.size(), r.headlines.size(), meta, time,
            r.ok ? "OK" : "ERR"
        });
        int last = historyTable.getRowCount() - 1;
        historyTable.scrollRectToVisible(historyTable.getCellRect(last, 0, true));
    }

    // ── Stats chips ───────────────────────────────────────────────────────────
    private void updateStats(ScrapeResult r) {
        int tl = history.stream().mapToInt(x -> x.links.size()).sum();
        int th = history.stream().mapToInt(x -> x.headlines.size()).sum();
        updateChip(sessionCount,  "Sessions",  String.valueOf(history.size()));
        updateChip(linkCount,     "Links",     String.valueOf(tl));
        updateChip(headlineCount, "Headlines", String.valueOf(th));
    }

    // ── Clear ─────────────────────────────────────────────────────────────────
    private void clearAll() {
        linksArea.setText(""); headlinesArea.setText("");
        metaArea.setText(""); rawArea.setText("");
        urlField.setText("https://");
        exportBtn.setEnabled(false);
        setStatus("Cleared", SUBTEXT);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private String buildRaw(ScrapeResult r) {
        StringBuilder sb = new StringBuilder();
        sb.append("Source Website : ").append(r.url).append("\n");
        sb.append("Title          : ").append(r.title).append("\n");
        sb.append("Scraped At     : ").append(new Date()).append("\n");
        sb.append("\n=== LINKS (").append(r.links.size()).append(") ===\n");
        for (String l : r.links) sb.append(l).append("\n");
        sb.append("\n=== HEADLINES (").append(r.headlines.size()).append(") ===\n");
        for (String h : r.headlines) sb.append(h).append("\n");
        sb.append("\n=== META DESCRIPTION ===\n");
        sb.append(r.metaDesc.isEmpty() ? "(none)" : r.metaDesc).append("\n");
        return sb.toString();
    }

    private String csv(String s) { return "\"" + s.replace("\"","\"\"") + "\""; }
    private String js(String s)  { return "\"" + s.replace("\\","\\\\").replace("\"","\\\"") + "\""; }
    private String ja(List<String> l) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < l.size(); i++) {
            sb.append(js(l.get(i)));
            if (i < l.size()-1) sb.append(", ");
        }
        return sb.append("]").toString();
    }

    private void setStatus(String msg, Color c) {
        statusLabel.setText(msg);
        statusLabel.setForeground(c);
    }

    // ── Widget factories ──────────────────────────────────────────────────────
    private JTextArea resultArea() {
        JTextArea a = new JTextArea();
        a.setEditable(false);
        a.setBackground(CARD_BG);
        a.setForeground(TEXT);
        a.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setBorder(new EmptyBorder(10, 12, 10, 12));
        a.setCaretColor(ACCENT);
        return a;
    }

    private JScrollPane wrapScroll(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(CARD_BG);
        sp.getVerticalScrollBar().setBackground(CARD_BG);
        sp.getHorizontalScrollBar().setBackground(CARD_BG);
        return sp;
    }

    private JButton fancyButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(7, 18, 7, 18)
        ));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(bg.brighter()); }
            public void mouseExited (MouseEvent e) { b.setBackground(bg); }
        });
        return b;
    }

    private JLabel pill(String text, Color c) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(c);
        l.setBorder(new CompoundBorder(
            new LineBorder(c, 1, true),
            new EmptyBorder(2, 7, 2, 7)
        ));
        return l;
    }

    private JLabel statChip(String label, String val, Color c) {
        JLabel l = new JLabel(label + ": " + val);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(c);
        l.setBorder(new CompoundBorder(
            new LineBorder(c, 1, true),
            new EmptyBorder(3, 10, 3, 10)
        ));
        return l;
    }

    private void updateChip(JLabel chip, String label, String val) {
        chip.setText(label + ": " + val);
    }

    private void styleTabPane(JTabbedPane tp) {
        tp.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            protected void installDefaults() {
                super.installDefaults();
                highlight          = PANEL_BG;
                lightHighlight     = PANEL_BG;
                shadow             = BORDER_C;
                darkShadow         = BORDER_C;
                focus              = ACCENT;
            }
        });
    }

    // ── Data model ────────────────────────────────────────────────────────────
    static class ScrapeResult implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        String       url, title = "", metaDesc = "", error = "";
        transient List<String> links      = new ArrayList<>();
        transient List<String> headlines  = new ArrayList<>();
        transient List<String> priceHints = new ArrayList<>();
        transient List<String> jobHints   = new ArrayList<>();
        transient List<String> leads      = new ArrayList<>();
        transient List<String> newsHints  = new ArrayList<>();
        boolean      ok         = false;
        ScrapeResult(String url) { this.url = url; }
    }
}
