package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.myapplication.databinding.ActivityMainBinding;

import java.io.InputStream;

import eu.bigdotsoftware.posnetserver.LicenseInfo;
import eu.bigdotsoftware.posnetserver.LicenseRegistrationInfo;
import eu.bigdotsoftware.posnetserver.ParagonFakturaLine;
import eu.bigdotsoftware.posnetserver.ParagonRequest;
import eu.bigdotsoftware.posnetserver.ParagonResponse;
import eu.bigdotsoftware.posnetserver.PosnetRequest;
import eu.bigdotsoftware.posnetserver.PosnetResponse;
import eu.bigdotsoftware.posnetserver.PosnetServerAndroid;
import eu.bigdotsoftware.posnetserver.ProcessWatcher;
import eu.bigdotsoftware.posnetserver.StatusLicznikowResponse;
import eu.bigdotsoftware.posnetserver.VatRate;
import eu.bigdotsoftware.posnetserver.VatRatesGetResponse;


public class MainActivity extends AppCompatActivity {


    private static final String TAG = "PosnetServerAndroid";

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //---------------------------------------------------------------------------
        //Initialize
        PosnetServerAndroid posnet = new PosnetServerAndroid();
        posnet.addProcessListener(new ProcessWatcher() {
            @Override
            public void onStart(PosnetRequest request) {
                TextView tvErrorMessage = binding.tvErrorMessage;
                tvErrorMessage.setVisibility(View.GONE);
                tvErrorMessage.setText("");
            }

            @Override
            public void onProcess(String message) {
                Log.i(TAG, message);
            }

            @Override
            public void onDone(PosnetRequest request, String message) {
                PosnetResponse response = request.getResponse();
                if( response instanceof ParagonResponse) {
                    ParagonResponse result = (ParagonResponse)response;
                    Log.i(TAG, String.format("ParagonResponse [%s]", result.toString()));
                }else if (response instanceof VatRatesGetResponse) {
                    VatRatesGetResponse result = (VatRatesGetResponse)response;
                    for(VatRate vatRate : result.getRates()) {
                        Log.i(TAG, String.format("Received VAT rate [%d]: %s with value %s ", vatRate.getIndex(), vatRate.getName(), vatRate.getValue()));
                    }
                }else if( response instanceof StatusLicznikowResponse) {
                    StatusLicznikowResponse result = (StatusLicznikowResponse)response;
                    Log.i(TAG, String.format("Received Counters [%s]", result.toString()));
                }
                Log.i(TAG, message);
            }

            @Override
            public void onError(PosnetRequest request, String error) {
                TextView tvErrorMessage = binding.tvErrorMessage;
                tvErrorMessage.setVisibility(View.VISIBLE);
                tvErrorMessage.setText(error);
                Log.e(TAG, error);
            }
        });

        String host = "192.168.0.68";
        int port = 12346;

        InputStream ins = getResources().openRawResource(
                getResources().getIdentifier("cmbth_pl", "raw", getPackageName()));
        posnet.InitializeErrorList(ins);
        posnet.InitializeCache(host, port);

        //---------------------------------------------------------------------------
        //License validation
        LicenseInfo info = posnet.validateLicenseFile(this, "Test.lic");
        Log.i(TAG, "License details | " + info.getCompanyName());
        for(int i=0;i<info.getExtrasCount();i++)
            Log.i(TAG, "License details | " + info.getExtras(i));

        LicenseRegistrationInfo licenseRegistrationInfo = posnet.registerLicenseFile(this, "Test.lic");
        if( !licenseRegistrationInfo.isOk())
            Log.e(TAG, "Cannot register license file: " + licenseRegistrationInfo.getError());
        else
            Log.e(TAG, "License file registered: OK");

        //---------------------------------------------------------------------------
        //Print
        ParagonRequest paragon = ParagonRequest.Builder()
                .addLine(ParagonFakturaLine.Builder()
                        .setName("Towar 1")
                        //.setVatPercent("23.00")
                        .setVatIndex(0)
                        .setPrice(100)
                        .setQuantity(1.0f)
                        .build())
                .addLine(ParagonFakturaLine.Builder()
                        .setName("Towar 2")
                        //.setVatPercent("23.00")
                        .setVatIndex(0)
                        .setPrice(100)
                        .setQuantity(1.0f)
                        .build()
                )
                .setTotal(200)
                .build();

        posnet.sendRequest(host, port, paragon);

        // VatRatesGetRequest vatRatesGetRequest = new VatRatesGetRequest();
        // posnet.sendRequest(host, port, vatRatesGetRequest);

        // StatusLicznikowRequest statusLicznikowRequest = new StatusLicznikowRequest();
        // posnet.sendRequest(host, port, statusLicznikowRequest);

        // VatRatesSetRequest vatRatesSetRequest = new VatRatesSetRequest();
        // vatRatesSetRequest.setRate(0,"A", "23");
        // vatRatesSetRequest.setRate(1,"B", "8");
        // vatRatesSetRequest.setRate(2,"C", "0");
        // vatRatesSetRequest.setRate(3,"D", "3");
        // vatRatesSetRequest.setRate(4,"G", "100");
        // posnet.sendRequest(host, port, vatRatesSetRequest);

        // MaintenanceRequest maintenanceRequest = new MaintenanceRequest("2022-11-01");
        // posnet.sendRequest(host, port, maintenanceRequest);

        // HeaderSetRequest headerSetRequest = new HeaderSetRequest("My Company", true);
        // posnet.sendRequest(host, port, headerSetRequest);
    }
    
}