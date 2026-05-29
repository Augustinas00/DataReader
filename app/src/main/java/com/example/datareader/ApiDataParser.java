package com.example.datareader;

import android.util.Xml;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.Locale;

public class ApiDataParser {

    private ApiDataParser() {
        // Utility class should not be instantiated.
    }

    public static String parseCurrencyRate(String xmlResponse, String currencyCode)
            throws XmlPullParserException, IOException {

        String selectedCurrency = currencyCode.toUpperCase(Locale.ROOT);

        if (selectedCurrency.equals("EUR")) {
            return "ECB valiutos kursas\n\n1 EUR = 1 EUR";
        }

        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new StringReader(xmlResponse));

        String rateDate = "";
        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG
                    && parser.getName().equals("Cube")) {

                String time = parser.getAttributeValue(null, "time");
                String currency = parser.getAttributeValue(null, "currency");
                String rate = parser.getAttributeValue(null, "rate");

                if (time != null) {
                    rateDate = time;
                }

                if (currency != null
                        && currency.equalsIgnoreCase(selectedCurrency)
                        && rate != null) {

                    return "ECB valiutos kursas\n\n"
                            + "Data: " + rateDate + "\n"
                            + "1 EUR = " + rate + " " + selectedCurrency;
                }
            }

            eventType = parser.next();
        }

        throw new IllegalArgumentException(
                "Valiutos kodas nerastas. Bandykite USD, GBP arba PLN."
        );
    }

    public static String parseWeatherForecast(String jsonResponse) throws JSONException {
        JSONObject rootObject = new JSONObject(jsonResponse);

        JSONObject placeObject = rootObject.getJSONObject("place");
        String placeName = placeObject.getString("name");

        JSONArray forecastArray = rootObject.getJSONArray("forecastTimestamps");

        if (forecastArray.length() == 0) {
            throw new IllegalArgumentException("Prognozės duomenų nėra.");
        }

        JSONObject forecastObject = forecastArray.getJSONObject(0);

        String forecastTime = forecastObject.getString("forecastTimeUtc");
        double airTemperature = forecastObject.getDouble("airTemperature");
        double feelsLikeTemperature = forecastObject.getDouble("feelsLikeTemperature");
        int windSpeed = forecastObject.getInt("windSpeed");
        String conditionCode = forecastObject.getString("conditionCode");

        return "Meteo.lt prognozė\n\n"
                + "Vieta: " + placeName + "\n"
                + "Laikas UTC: " + forecastTime + "\n"
                + "Temperatūra: " + airTemperature + " °C\n"
                + "Jaučiama temperatūra: " + feelsLikeTemperature + " °C\n"
                + "Vėjo greitis: " + windSpeed + " m/s\n"
                + "Oro sąlygos: " + translateConditionCode(conditionCode);
    }

    public static String parseCatFact(String jsonResponse) throws JSONException {
        JSONObject rootObject = new JSONObject(jsonResponse);
        String fact = rootObject.getString("fact");

        return "Cat Fact API rezultatas\n\n" + fact;
    }

    private static String translateConditionCode(String conditionCode) {
        switch (conditionCode) {
            case "clear":
                return "Giedra";
            case "partly-cloudy":
                return "Mažai debesuota";
            case "cloudy-with-sunny-intervals":
                return "Debesuota su pragiedruliais";
            case "cloudy":
                return "Debesuota";
            case "light-rain":
                return "Nedidelis lietus";
            case "rain":
                return "Lietus";
            case "heavy-rain":
                return "Smarkus lietus";
            case "sleet":
                return "Šlapdriba";
            case "snow":
                return "Sniegas";
            case "fog":
                return "Rūkas";
            default:
                return conditionCode;
        }
    }
}