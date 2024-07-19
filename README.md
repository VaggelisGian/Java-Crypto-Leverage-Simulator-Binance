# Java-Crypto-Leverage-Simulator-Binance
A Java application for simulating leveraged cryptocurrency trades on Binance Futures testnet. It calculates trade quantities, sets leverage, and places orders, using secure API requests for educational purposes.

# Trading Simulator Spring

A Java-based trading simulator for educational purposes, designed to simulate leveraged cryptocurrency trades on the Binance Futures testnet. It demonstrates how to calculate trade quantities, set leverage, and place orders securely.

## Features

- Simulates buying cryptocurrency on leverage.
- Calculates trade quantities based on a fixed USD amount.
- Sets leverage for trades.
- Places market orders with calculated quantities and leverage.
- Secure API communication through HMAC SHA256 signature.

## Potential Enhancements

- **API Key Configuration**: Implement a more secure way to handle API keys, such as using environment variables or a configuration file.
- **User Interface**: Develop a simple GUI or web interface for a more user-friendly experience.
- **Error Handling**: Improve error handling to manage and log exceptions more effectively.
- **Extended Functionality**: Add features like selling, stop loss, take profit, and more detailed trade analysis.
- **Performance Optimization**: Optimize the code for better performance, especially in handling HTTP requests and JSON parsing.
- **Unit Tests**: Write unit tests to ensure the reliability and accuracy of the calculations and API interactions.
- **Documentation**: Enhance the documentation with more detailed setup instructions, usage examples, and API endpoint descriptions.

## Getting Started

1. Ensure Java JDK 11 and Maven are installed.
2. Clone the repository and navigate to the project directory.
3. Build the project using Maven: `mvn clean install`.
4. Run the application: `java -jar target/trading-simulator-spring-1.0-SNAPSHOT.jar`.
5. Follow the console prompts to simulate trades.

## Configuration

- Update the `APISECRET` and `SECRET` variables in `SimulatorApplication.java` with your Binance API and Secret keys.
- Ensure you are using the Binance Futures testnet API endpoints for simulation.

## Disclaimer

This project is for educational purposes only and should not be used with real funds in a live trading environment.

## Contributing

Contributions are welcome. Feel free to fork the repository, make changes, and submit a pull request.
