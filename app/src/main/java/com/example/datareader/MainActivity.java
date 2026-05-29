package com.example.datareader;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final int SOURCE_CURRENCY = 0;
    private static final int SOURCE_WEATHER = 1;
    private static final int SOURCE_CAT_FACT = 2;

    private static final String ECB_URL =
            "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";

    private static final String METEO_URL =
            "https://api.meteo.lt/v1/places/vilnius/forecasts/long-term";

    private static final String CAT_FACT_URL =
            "https://catfact.ninja/fact";

    private Spinner spnDataSource;
    private TextView txtCurrencyCodeLabel;
    private EditText txtCurrencyCode;
    private Button btnLoadData;
    private ProgressBar progressLoading;
    private TextView txtResult;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private interface ResponseFormatter {
        String format(String response) throws Exception;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spnDataSource = findViewById(R.id.spnDataSource);
        txtCurrencyCodeLabel = findViewById(R.id.txtCurrencyCodeLabel);
        txtCurrencyCode = findViewById(R.id.txtCurrencyCode);
        btnLoadData = findViewById(R.id.btnLoadData);
        progressLoading = findViewById(R.id.progressLoading);
        txtResult = findViewById(R.id.txtResult);

        spnDataSource.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(
                    AdapterView<?> parent,
                    View view,
                    int position,
                    long id
            ) {
                updateInputVisibility(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updateInputVisibility(SOURCE_CURRENCY);
            }
        });

        btnLoadData.setOnClickListener(view -> loadSelectedData());

        updateInputVisibility(SOURCE_CURRENCY);
    }

    private void updateInputVisibility(int selectedSource) {
        boolean currencySelected = selectedSource == SOURCE_CURRENCY;

        txtCurrencyCodeLabel.setVisibility(currencySelected ? View.VISIBLE : View.GONE);
        txtCurrencyCode.setVisibility(currencySelected ? View.VISIBLE : View.GONE);

        txtResult.setText(R.string.message_choose_source);
    }

    private void loadSelectedData() {
        int selectedSource = spnDataSource.getSelectedItemPosition();

        if (selectedSource == SOURCE_CURRENCY) {
            String currencyCode = txtCurrencyCode.getText()
                    .toString()
                    .trim()
                    .toUpperCase(Locale.ROOT);

            if (currencyCode.isEmpty()) {
                txtCurrencyCode.setError(getString(R.string.error_currency_empty));
                txtCurrencyCode.requestFocus();
                return;
            }

            if (!currencyCode.matches("[A-Z]{3}")) {
                txtCurrencyCode.setError(getString(R.string.error_currency_format));
                txtCurrencyCode.requestFocus();
                return;
            }

            loadData(
                    ECB_URL,
                    response -> ApiDataParser.parseCurrencyRate(response, currencyCode)
            );
            return;
        }

        if (selectedSource == SOURCE_WEATHER) {
            loadData(
                    METEO_URL,
                    ApiDataParser::parseWeatherForecast
            );
            return;
        }

        if (selectedSource == SOURCE_CAT_FACT) {
            loadData(
                    CAT_FACT_URL,
                    ApiDataParser::parseCatFact
            );
        }
    }

    private void loadData(String urlAddress, ResponseFormatter formatter) {
        setLoadingState(true);

        executorService.execute(() -> {
            try {
                String downloadedResponse = NetworkUtils.downloadText(urlAddress);
                String formattedResult = formatter.format(downloadedResponse);

                mainThreadHandler.post(() -> {
                    txtResult.setText(formattedResult);
                    setLoadingState(false);
                });

            } catch (Exception exception) {
                mainThreadHandler.post(() -> {
                    String errorMessage = getString(R.string.error_loading_data);

                    if (exception.getMessage() != null) {
                        errorMessage += "\n" + exception.getMessage();
                    }

                    txtResult.setText(errorMessage);
                    setLoadingState(false);
                });
            }
        });
    }

    private void setLoadingState(boolean isLoading) {
        progressLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnLoadData.setEnabled(!isLoading);
        spnDataSource.setEnabled(!isLoading);
        txtCurrencyCode.setEnabled(!isLoading);

        if (isLoading) {
            txtResult.setText(R.string.message_loading);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdownNow();
    }
}