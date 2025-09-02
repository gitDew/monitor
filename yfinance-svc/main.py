import logging
from flask import Flask, request, jsonify
import yfinance as yf

app = Flask(__name__)

def get_latest_rsi(symbol: str, interval: str):
    lookback_map = {
        "1m": "1d", 
        "5m": "1d", 
        "15m": "1d", 
        "30m": "1wk", 
        "1h": "1wk",
        "1d": "1mo", 
        "1wk": "5mo", 
        "1mo": "5y"
    }

    lookback = lookback_map.get(interval)

    ticker = yf.Ticker(symbol)
    data = ticker.history(period=lookback, interval=interval)

    if data.empty:
        raise ValueError("No data returned. Check the ticker or network.")

    close = data["Close"]
    logging.log(logging.DEBUG, data)
    delta = close.diff()

    gain = delta.where(delta > 0, 0.0)
    loss = -delta.where(delta < 0, 0.0)

    period = 14
    avg_gain = gain.ewm(alpha=1/period, adjust=False).mean()
    avg_loss = loss.ewm(alpha=1/period, adjust=False).mean()

    rs = avg_gain / avg_loss
    rsi = 100 - (100 / (1 + rs))

    return rsi.iloc[-1]

@app.route("/rsi", methods=["GET"])
def rsi_endpoint():
    symbol = request.args.get("symbol", type=str)
    interval = request.args.get("interval", type=str)
    if not symbol:
        return jsonify({"error": "Missing 'symbol' query parameter"}), 400
    try:
        if not interval:
            interval = "1d"
        rsi_value = get_latest_rsi(symbol, interval)
        logging.log(logging.DEBUG, "RSI: " + str(rsi_value))
        return jsonify({"symbol": symbol.upper(), "rsi": round(rsi_value, 2)})
    except ValueError as e:
        if "no data" in str(e).lower():
            return jsonify({"error": str(e)}), 404
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    logging.basicConfig(level=logging.DEBUG)

    file_handler = logging.FileHandler('app.log')
    file_handler.setLevel(logging.DEBUG)  # capture all messages in file

    logging.getLogger().addHandler(file_handler)
    app.run(host="0.0.0.0", port=5000)
