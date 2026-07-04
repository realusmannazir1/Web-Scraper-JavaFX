# ScrapeMaster — Web Scraper in Java

A fully-featured web scraper built in Java using **Jsoup**, with a modern dark-themed Swing GUI. Supports single URL scraping, batch imports, and exports data to TXT, CSV, and JSON.

---

## Features

- **Single URL scraping** — paste any URL and extract data instantly
- **Batch import** — import a `.csv` or `.txt` file with multiple URLs and scrape them all automatically
- **10 result tabs** — Links, Headlines, Meta, Prices, Jobs, Leads, News, Raw Output, History, Use Cases
- **Auto-save** — every scrape saves to `.txt`, `scraped_data.csv`, and `scraped_data.json`
- **Export** — save any result to a custom file via dialog
- **Pagination support** — detects and follows "Next" links automatically
- **Session stats** — live counters for sessions, total links, and headlines scraped

---

## Project Structure

```
WEB-Scraper/
├── src/
│   ├── ScraperGUI.java           ← Main GUI application
│   ├── ScrapeMasterSingle.java   ← Single-site CLI scraper
│   └── ScrapeMasterMultiple.java ← Multi-site CLI scraper with pagination
├── lib/
│   └── jsoup-1.17.2.jar
├── ScrapeMaster.bat              ← Launch GUI (double-click)
├── run_single.bat                ← Run single-site scraper
└── run_multiple.bat              ← Run multi-site scraper
```

---

## Requirements

- Java JDK 8 or higher

---

## How to Run

### GUI (recommended)
Double-click `ScrapeMaster.bat`

### Command Line

```bash
# Compile
javac -cp "lib\jsoup-1.17.2.jar" -d out src\ScraperGUI.java

# Run
java -cp "lib\jsoup-1.17.2.jar;out" ScraperGUI
```

---

## Batch Import

1. Create a `.csv` or `.txt` file with one URL per line:
   ```
   https://books.toscrape.com/
   https://quotes.toscrape.com/
   https://toscrape.com/
   ```
2. Click **📂 Import URLs** in the app
3. Select your file — scraping starts automatically

---

## Output Files

| File | Format | Contents |
|------|--------|----------|
| `output_TIMESTAMP.txt` | Plain text | Full scraped data per session |
| `scraped_data.csv` | CSV | All sessions appended |
| `scraped_data.json` | JSON | All sessions appended |

---

## Tech Stack

| Technology | Purpose |
|-----------|---------|
| Java Swing | GUI |
| Jsoup 1.17.2 | HTML parsing & HTTP requests |
| SwingWorker | Background threading |
| FileWriter / PrintWriter | File output |

---

## License

MIT
