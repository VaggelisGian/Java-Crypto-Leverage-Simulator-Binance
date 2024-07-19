package com.example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
//import org.json.JSONTokener;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class SimulatorApplication {
	private static final String SECRET = "";
	private static final String APISECRET = "";
	private static final Mac sha256HMAC;

	static {
		try {
			sha256HMAC = Mac.getInstance("HmacSHA256");
			SecretKeySpec secretKey = new SecretKeySpec(SECRET.getBytes(), "HmacSHA256");
			sha256HMAC.init(secretKey);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		try (Scanner scanner = new Scanner(System.in)) {
			HttpClient client = HttpClient.newBuilder()
					.version(HttpClient.Version.HTTP_2) // Use HTTP/2
					.build();
			long timestamp;
			while (true) {

				System.out.println("Enter the coin name:");
				String coinName = scanner.nextLine();
				// Get the current timestamp in milliseconds
				timestamp = System.currentTimeMillis();
				String baseCurrency = "USDT";

				String symbol = coinName.toUpperCase() + baseCurrency.toUpperCase();

				String action = "BUY";

				// Prepare the query string
				String queryString = "symbol=" + symbol + "&timestamp=" + timestamp;

				try {
					// Generate the signature
					String signature = generateSignature(queryString);

					// Add the signature to the query string
					queryString += "&signature=" + signature;
				} catch (Exception e) {
					e.printStackTrace();
				}
				// Create the request
				// HttpRequest leverageRequest = HttpRequest.newBuilder()
				// .uri(URI.create("https://testnet.binancefuture.com/fapi/v1/leverageBracket?"
				// + queryString))
				// .header("X-MBX-APIKEY", APISECRET)
				// .build();
				// System.out.println("Leverage Request: " + leverageRequest);
				// HttpResponse<String> leverageResponse = client.send(leverageRequest,
				// HttpResponse.BodyHandlers.ofString());

				// // Check if the response is a JSON array or a JSON object
				// String responseBody = leverageResponse.body();
				// Object json = new JSONTokener(responseBody).nextValue();

				// // The response is a JSON array
				// JSONArray brackets = (JSONArray) json;
				// // Process the array...

				// JSONObject bracket = brackets.getJSONObject(0);
				// JSONArray bracketDetails = bracket.getJSONArray("brackets");

				// JSONObject detail = bracketDetails.getJSONObject(0);
				// System.out.println("Bracket " + 1 + ": " + detail.getInt("initialLeverage"));
				// leverage = Integer.toString(detail.getInt("initialLeverage"));
				String leverage = "2";
				// Fetch the symbol information
				HttpRequest symbolInfoRequest = HttpRequest.newBuilder()
						.uri(URI.create("https://testnet.binancefuture.com/fapi/v1/exchangeInfo"))
						.build();
				HttpResponse<String> symbolInfoResponse = client.send(symbolInfoRequest,
						HttpResponse.BodyHandlers.ofString());

				// Parse the response to get the symbol information
				JSONObject symbolInfoJson = new JSONObject(symbolInfoResponse.body());
				JSONArray symbols = symbolInfoJson.getJSONArray("symbols");
				double stepSize = 0.0;
				for (int i = 0; i < symbols.length(); i++) {
					JSONObject symbolObj = symbols.getJSONObject(i);
					if (symbolObj.getString("symbol").equals(symbol)) {
						JSONArray filters = symbolObj.getJSONArray("filters");
						for (int j = 0; j < filters.length(); j++) {
							JSONObject filter = filters.getJSONObject(j);
							if (filter.getString("filterType").equals("LOT_SIZE")) {
								stepSize = filter.getDouble("stepSize");
							}
						}
					}
				}

				// Fetch the current price of the coin
				HttpRequest priceRequest = HttpRequest.newBuilder()
						.uri(URI.create("https://testnet.binancefuture.com/fapi/v1/ticker/price?symbol=" + symbol))
						.build();
				;
				HttpResponse<String> priceResponse = client.send(priceRequest, HttpResponse.BodyHandlers.ofString());

				// Parse the response to get the price
				JSONObject priceJson = new JSONObject(priceResponse.body());
				double price = priceJson.getDouble("price");

				// Calculate the quantity of the coin that corresponds to 15 USDT
				double quantity = 15 / price;
				// Adjust the quantity to meet the LOT_SIZE filter requirements
				quantity = Math.floor(quantity / stepSize) * stepSize;
				// If the notional value is less than 100, increase the quantity
				if (quantity * price < 100) {
					quantity += stepSize;
				}
				// Determine the number of decimal places from stepSize
				int numDecimalPlaces = Math.max(0, (int) Math.ceil(-Math.log10(stepSize)));

				// Round the quantity to the correct number of decimal places
				BigDecimal bd = new BigDecimal(Double.toString(quantity));
				bd = bd.setScale(numDecimalPlaces, RoundingMode.HALF_UP);
				quantity = bd.doubleValue();

				// Calculate the leveraged quantity
				double leveragedQuantity = quantity * Double.parseDouble(leverage);
				// Round the leveraged quantity to the correct number of decimal places
				bd = new BigDecimal(Double.toString(leveragedQuantity));
				bd = bd.setScale(numDecimalPlaces, RoundingMode.HALF_UP);
				leveragedQuantity = bd.doubleValue();

				System.out.println("Quantity: " + quantity);
				System.out.println("Leveraged Quantity: " + leveragedQuantity);

				// Set the cross leverage
				long leverageTimestamp = System.currentTimeMillis();
				String leverageQuery = "symbol=" + symbol + "&leverage=" + leverage + "&timestamp=" + leverageTimestamp;
				String leverageSignature = "";
				try {
					leverageSignature = generateSignature(leverageQuery);
				} catch (Exception e) {
					e.printStackTrace();
				}
				System.out.println("Leverage query: " + leverageQuery + "&signature=" + leverageSignature);
				HttpRequest leverageSetRequest = HttpRequest.newBuilder()
						.uri(URI.create("https://testnet.binancefuture.com/fapi/v1/leverage"))
						.header("X-MBX-APIKEY", APISECRET)
						.POST(HttpRequest.BodyPublishers.ofString(leverageQuery + "&signature=" + leverageSignature))
						.build();
				System.out.println("Leverage Set Request: " + leverageSetRequest);
				HttpResponse<String> leverageSetResponse = client.send(leverageSetRequest,
						HttpResponse.BodyHandlers.ofString());

				if (leverageSetResponse.statusCode() != 200) {
					System.out.println("Failed to set leverage: " + leverageSetResponse.body());
					continue;
				}
				System.out.println("Leverage set response: " + leverageSetResponse.body());

				// Now make the order
				// Prepare the order request parameters
				// Prepare the order request parameters
				String orderParams = "symbol=" + symbol + "&side=" + action + "&type=MARKET" + "&quantity="
						+ leveragedQuantity
						+ "&timestamp=" + System.currentTimeMillis();

				String orderSignature = "";
				try {
					// Generate the signature for the order request
					orderSignature = generateSignature(orderParams);
				} catch (Exception e) {
					e.printStackTrace();
				}

				// Create the order request
				HttpRequest orderRequest = HttpRequest.newBuilder()
						.uri(URI.create("https://testnet.binancefuture.com/fapi/v1/order"))
						.header("X-MBX-APIKEY", APISECRET)
						.POST(HttpRequest.BodyPublishers.ofString(orderParams + "&signature=" + orderSignature))
						.build();

				// Send the order request
				HttpResponse<String> orderResponse = client.send(orderRequest, HttpResponse.BodyHandlers.ofString());

				System.out.println("Order response: " + orderResponse.body());
				long endTime = System.currentTimeMillis();
				long timeElapsed = endTime - timestamp;
				System.out.println("Execution time in milliseconds: " + timeElapsed);
				System.out.println("Execution time in seconds: " + timeElapsed / 1000.0);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static String hmacSha256(String message, String secret) throws Exception {
		Mac sha256HMAC = Mac.getInstance("HmacSHA256");
		SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
		sha256HMAC.init(secretKey);

		byte[] bytes = sha256HMAC.doFinal(message.getBytes());
		return String.format("%032x", new BigInteger(1, bytes));
	}

	public static String generateSignature(String queryString) throws Exception {
		return hmacSha256(queryString, SECRET);
	}
}
