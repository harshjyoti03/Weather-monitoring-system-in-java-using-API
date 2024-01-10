import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

public class WeatherApp {
    private JTextField locationInput;
    private JTextArea weatherOutput;
    private JButton retrieveButton;
    private Timer fadeTimer;

    private static final String API_KEY = "bd5e378503939ddaee76f12ad7a97608";
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather";

    private HashMap<String, String> weatherDataCache;

    public WeatherApp() {
        weatherDataCache = new HashMap<>();

        JFrame frame = new JFrame("Weather App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(173, 216, 230));

        locationInput = new JTextField();
        weatherOutput = new JTextArea(10, 40);
        retrieveButton = new JButton("Retrieve Weather");
        fadeTimer = new Timer(10, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int alpha = weatherOutput.getBackground().getAlpha();
                if (alpha < 255) {
                    alpha += 5;
                    weatherOutput.setBackground(new Color(240, 248, 255, alpha));
                } else {
                    fadeTimer.stop();
                }
            }
        });

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(locationInput, BorderLayout.CENTER);
        inputPanel.add(retrieveButton, BorderLayout.EAST);

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(weatherOutput), BorderLayout.CENTER);

        retrieveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String location = locationInput.getText();

                if (weatherDataCache.containsKey(location)) {
                    startFadeInAnimation(weatherDataCache.get(location));
                } else {
                    String weatherData = getWeatherData(location);
                    weatherDataCache.put(location, weatherData);
                    startFadeInAnimation(weatherData);
                }
            }
        });

        frame.pack();
        frame.setVisible(true);
    }

    private void startFadeInAnimation(String weatherData) {
        weatherOutput.setBackground(new Color(240, 248, 255, 0));
        weatherOutput.setText(weatherData);
        fadeTimer.start();
    }

    private String getWeatherData(String location) {
        try {
            String encodedLocation = URLEncoder.encode(location, "UTF-8");
            URL url = new URL(API_URL + "?q=" + encodedLocation + "&appid=" + API_KEY);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder data = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    data.append(line);
                }
                reader.close();

                String cityName = extractValue(data.toString(), "\"name\":\"", "\"");
                double temperatureKelvin = Double.parseDouble(extractValue(data.toString(), "\"temp\":", ","));
                double temperatureCelsius = temperatureKelvin - 273.15;
                double temperatureFahrenheit = (temperatureCelsius * 9/5) + 32;
                String weatherDescription = extractValue(data.toString(), "\"description\":\"", "\"");

                String recommendations = getRecommendations(temperatureCelsius);

                return "City: " + cityName + "\nTemperature: " + String.format("%.2f", temperatureCelsius) + "°C / "
                        + String.format("%.2f", temperatureFahrenheit) + "°F\nWeather: " + weatherDescription
                        + "\n\nRecommendations:\n" + recommendations;
            } else {
                return "Error: HTTP response code " + responseCode;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Error retrieving weather data.";
        }
    }

    private String extractValue(String data, String startTag, String endTag) {
        int startIndex = data.indexOf(startTag);
        int endIndex = data.indexOf(endTag, startIndex + startTag.length());
        if (startIndex != -1 && endIndex != -1) {
            return data.substring(startIndex + startTag.length(), endIndex);
        } else {
            return "N/A";
        }
    }

    private String getRecommendations(double temperature) {
        if (temperature > 25) {
            return "It's warm! Wear light clothing and sunglasses. Don't forget sunscreen!";
        } else if (temperature > 15) {
            return "It's mild. A light jacket or sweater might be comfortable.";
        } else {
            return "It's cold! Bundle up with a warm coat, hat, and gloves.";
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WeatherApp());
    }
}