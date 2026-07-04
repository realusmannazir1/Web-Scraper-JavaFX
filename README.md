# ScrapeMaster — Web Scraper in Java

A fully-featured web scraper built in Java using **Jsoup**, with a modern dark-themed Swing GUI.

## Features

- **Single URL scraping** — paste any URL and extract instantly
- **Batch import** — import a CSV/TXT file with multiple URLs and scrape them all automatically
- **10 result tabs** — Links, Headlines, Meta, Prices, Jobs, Leads, News, Raw Output, History, Use Cases
- **Auto-save** — every scrape saves to `.txt`, `scraped_data.csv`, and `scraped_data.json`
- **Export** — save any result to a custom file via dialog
- **Pagination support** — detects and follows "Next" links automatically

## Project Structure

```
WEB-Scraper/
├── src/
│   ├── ScraperGUI.java           ← Main GUI application
│   ├── ScrapeMasterSingle.java   ← Assignment Q1 Part 1 (single site)
│   └── ScrapeMasterMultiple.java ← Assignment Q1 Part 2 (multiple sites)
├── lib/
│   └── jsoup-1.17.2.jar          ← Jsoup library
├── ScrapeMaster.bat              ← Double-click to run GUI
├── run_single.bat                ← Run single scraper
└── run_multiple.bat              ← Run multiple scraper
```

## How to Run

### Requirements
- Java JDK 8 or higher installed

### GUI App (recommended)
Just double-click `ScrapeMaster.bat`

### Command line
```bash
# Compile
javac -cp "lib\jsoup-1.17.2.jar" -d out src\ScraperGUI.java

# Run
java -cp "lib\jsoup-1.17.2.jar;out" ScraperGUI
```

## How to Import URLs

1. Create a `.csv` or `.txt` file with one URL per line:
```
https://books.toscrape.com/
https://quotes.toscrape.com/
https://toscrape.com/
```
2. Click **📂 Import URLs** in the app
3. Select your file → confirm → scraping starts automatically

## Output Files

| File | Format | Contents |
|------|--------|----------|
| `output_TIMESTAMP.txt` | Plain text | Full scraped data per session |
| `scraped_data.csv` | CSV | All sessions appended |
| `scraped_data.json` | JSON | All sessions appended |

## Technologies

| Technology | Purpose |
|-----------|---------|
| Java Swing | GUI |
| Jsoup 1.17.2 | HTML parsing & HTTP requests |
| SwingWorker | Background threading |
| FileWriter / PrintWriter | File output |

## Assignment Info

**CS-104 Object Oriented Programming (OOP) — Spring 2025**  
Assignment 01: Web Scraper (ScrapeMaster)
