package com.example;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.InputMismatchException;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

public class App 
{
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        Double lat = getDoubleArgument(scanner, "Введите широту: ");
        Double lon = getDoubleArgument(scanner, "Введите долготу: ");
        int limit = getIntArgument(scanner, "Введите количество дней прогноза: ");

        System.out.print("Введите ключ доступа к api: ");
        String key = scanner.next();

        HttpClient client = HttpClient.newHttpClient();
        
        HttpRequest request = buildReq(lat,lon, limit, key);
        
        try {
            HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Тело ответа: "+response.body());
            System.out.println("Температура сейчас: "+getTemp(response.body().toString()));
            System.out.println("Средняя температура по прогнозу: "+getAvgTemp(response.body().toString()));

        } catch (Exception e) {
            System.err.println("Ошибка получения ответа: " + e.getMessage());
            System.exit(1);
        }
    }

    private static Double getDoubleArgument(Scanner scanner, String text) {

        System.out.print(text);

        Double arg;
        for (;;) {
            try {
                arg = scanner.nextDouble();
            } catch (InputMismatchException e) {
                System.out.print("Неверный формат");
                continue;
            }

            break;
        }

        return arg;
    }

    private static int getIntArgument(Scanner scanner, String text) {

        System.out.print(text);

        int arg;
        for (;;) {
            try {
                arg = scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.print("Неверный формат");
                continue;
            }

            break;
        }

        return arg;
    }

    private static HttpRequest buildReq(Double lat, Double lon, int limit, String key) {
        URI uri = URI.create(
            String.format(
                "https://api.weather.yandex.ru/v2/forecast?lat=%f&lon=%f&limit=%d", 
                lat, lon, limit
            )
        );

        return HttpRequest.newBuilder().
        uri(uri).
        header("X-Yandex-Weather-Key", key).
        build();
    }

    private static int getTemp(String jsonBody) {
        JSONObject obj = new JSONObject(jsonBody);

        try {
            return obj.getJSONObject("fact").getInt("temp");
        } catch (Exception e) {
            System.err.println("Температура не найдена.");
        }

        return 0;
    }

    private static Double getAvgTemp(String jsonBody) {
        JSONObject obj = new JSONObject(jsonBody);

        try {
            JSONArray forecasts = obj.getJSONArray("forecasts");

            int daysCount = forecasts.length();

            Double tempSum = 0.;

            for (int i = 0; i < daysCount; i++) {
                JSONObject forecast = forecasts.getJSONObject(i);
                tempSum += forecast.getJSONObject("parts").getJSONObject("day_short").getDouble("temp");
            }

            return tempSum / daysCount;
        } catch (Exception e) {
            System.err.println("Температура не найдена.");
        }

        return 0.;
    }
}
