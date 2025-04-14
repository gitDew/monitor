# 🖥️ Monitor

A simple market monitor & alert system built with **Spring Boot**.

## 🧠 What It Does

**Monitor** allows you to subscribe to some financial indicators, and sends you alerts on Telegram
once certain thresholds have been reached.

## 🚀 Features

✅ Data from [TwelveData](https://twelvedata.com/)  
✅ Data from [AlphaVantage](https://www.alphavantage.co/)  
✅ Telegram bot integration  
✅ Docker-ready

## 🛠️ Tech Stack

- **Java 17**
- **Spring Boot**
- **Gradle**
- **Docker**

## 📦 Getting Started

### 1. Clone the repo

```bash
git clone https://github.com/gitDew/monitor.git
cd monitor
```

### 2. Set environment variables

Create a `.env` file in the root of your project with the following content:

```env
TELEGRAM_TOKEN=your-telegram-bot-token
ALPHAVANTAGE_TOKEN=your-alphavantage-api-key
TWELVEDATA_TOKEN=your-twelvedata-api-key
```

For help on how to acquire your API tokens check
out [this](https://core.telegram.org/bots/features#creating-a-new-bot), [this](https://www.alphavantage.co/support/#api-key)
and [this](https://twelvedata.com/pricing).

### 3. Run with Docker

Build the Docker image:

```bash
docker build -t monitor-app .
```

Run the app with environment variables:

```bash
docker run --env-file .env monitor-app
```
