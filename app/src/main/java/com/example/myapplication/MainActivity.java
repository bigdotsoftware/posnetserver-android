package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.myapplication.databinding.ActivityMainBinding;

import java.io.InputStream;
import java.util.concurrent.ExecutionException;

import eu.bigdotsoftware.posnetserver.CancelRequest;
import eu.bigdotsoftware.posnetserver.FormsAztecCodeRequest;
import eu.bigdotsoftware.posnetserver.FormsBarcodeRequest;
import eu.bigdotsoftware.posnetserver.FormsDmCodeRequest;
import eu.bigdotsoftware.posnetserver.FormsPdf417CodeRequest;
import eu.bigdotsoftware.posnetserver.FormsQrCodeRequest;
import eu.bigdotsoftware.posnetserver.LicenseInfo;
import eu.bigdotsoftware.posnetserver.LicenseRegistrationInfo;
import eu.bigdotsoftware.posnetserver.ParagonFakturaExtraLine;
import eu.bigdotsoftware.posnetserver.ParagonFakturaFooter;
import eu.bigdotsoftware.posnetserver.ParagonFakturaLine;
import eu.bigdotsoftware.posnetserver.ParagonRequest;
import eu.bigdotsoftware.posnetserver.ParagonResponse;
import eu.bigdotsoftware.posnetserver.ParagonTaxIdInfo;
import eu.bigdotsoftware.posnetserver.PaymentObject;
import eu.bigdotsoftware.posnetserver.PaymentObjectBuilder;
import eu.bigdotsoftware.posnetserver.PosnetException;
import eu.bigdotsoftware.posnetserver.PosnetRequest;
import eu.bigdotsoftware.posnetserver.PosnetResponse;
import eu.bigdotsoftware.posnetserver.PosnetServerAndroid;
import eu.bigdotsoftware.posnetserver.ProcessWatcher;
import eu.bigdotsoftware.posnetserver.StatusLicznikowResponse;
import eu.bigdotsoftware.posnetserver.VatRate;
import eu.bigdotsoftware.posnetserver.VatRatesGetResponse;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "PosnetServerAndroid";

    private ActivityMainBinding m_binding;

    private PosnetServerAndroid m_posnetServerAndroid;
    private String m_host;
    private int m_port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(m_binding.getRoot());

        //---------------------------------------------------------------------------
        //Initialize
        m_host = "192.168.0.68";
        m_port = 12346;
        m_posnetServerAndroid = new PosnetServerAndroid();
        m_posnetServerAndroid.setReadTimeout(6000L);
        m_posnetServerAndroid.setProcessListener(new ProcessWatcher() {
            @Override
            public void onStart(PosnetRequest request) {
                TextView tvErrorMessage = m_binding.tvErrorMessage;
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
                TextView tvErrorMessage = m_binding.tvErrorMessage;
                tvErrorMessage.setVisibility(View.VISIBLE);
                tvErrorMessage.setText(error);
                Log.e(TAG, error);
            }
        });



        InputStream ins = getResources().openRawResource(
                getResources().getIdentifier("cmbth_pl", "raw", getPackageName()));
        m_posnetServerAndroid.initializeErrorList(ins);

        //---------------------------------------------------------------------------
        //Vat cache initialization, use one of:
        // - initializeCache - async method call
        // - initializeCacheWait - blocking method
        //For demo purposes and code simplification initializeCacheWait is used
        try {
            Boolean isOk = m_posnetServerAndroid.initializeCacheWait(m_host, m_port);
            Log.i(TAG, "Cache initialization: " + isOk);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //---------------------------------------------------------------------------
        //License validation
        LicenseInfo info = m_posnetServerAndroid.validateLicenseFile(this, "Test.lic");
        Log.i(TAG, "License details | " + info.getCompanyName());
        for(int i=0;i<info.getExtrasCount();i++)
            Log.i(TAG, "License details | " + info.getExtras(i));

        LicenseRegistrationInfo licenseRegistrationInfo = m_posnetServerAndroid.registerLicenseFile(this, "Test.lic");
        if( !licenseRegistrationInfo.isOk())
            Log.e(TAG, "Cannot register license file: " + licenseRegistrationInfo.getError());
        else
            Log.e(TAG, "License file registered: OK");

        try {
            //printFiscalPrintoutSimple1();
            //printFiscalPrintoutSimple2();
            printFiscalPrintout();
        } catch (PosnetException e) {
            Log.e(TAG, "Posnet exception: " + e.getMessage());
        }


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
    private void printFiscalPrintoutSimple1() throws PosnetException {
        //---------------------------------------------------------------------------
        //Print
        ParagonRequest paragon = ParagonRequest.Builder()
                .addLine(ParagonFakturaLine.Builder()
                        .setName("Coca-Cola")
                        .setVatIndex(0)
                        .setPrice(550)
                        .setQuantity(2.0f)
                        .build())
                .addLine(ParagonFakturaLine.Builder()
                        .setName("Banana")
                        .setVatIndex(2)
                        .setPrice(100)
                        .setQuantity(3.0f)
                        .build()
                )
                .setTotal(1400)
                .build();

        m_posnetServerAndroid.sendRequest(m_host, m_port, paragon);
    }
    private void printFiscalPrintoutSimple2() throws PosnetException {
        //---------------------------------------------------------------------------
        //Print
        ParagonRequest paragon = ParagonRequest.Builder()
                .addLine(ParagonFakturaLine.Builder()
                        .setName("Coca-Cola")
                        .setVatIndex(0)
                        .setPrice(550)
                        .setQuantity(2.0f)
                        .build())
                .addLine(ParagonFakturaLine.Builder()
                        .setName("Banana")
                        .setVatIndex(2)
                        .setPrice(100)
                        .setQuantity(3.0f)
                        .build()
                )
                .addPayment(PaymentObject.Builder()
                        .setType(0)
                        .setValue(1000)
                        .setName("By cash")
                        .setRest(false)
                        .build()
                )
                .addPayment(PaymentObject.Builder()
                        .setType(2)
                        .setValue(400)
                        .setName("By VISA card")
                        .setRest(false)
                        .build()
                )
                .setTotal(1400)
                .setPaymentFormsTotal(1400)
                .build();

        m_posnetServerAndroid.sendRequest(m_host, m_port, paragon);
    }
    private void printFiscalPrintout() throws PosnetException {
        //---------------------------------------------------------------------------
        //Cancel request if any in progress
        CancelRequest cancelRequest = new CancelRequest();
        try {
            Boolean isOk = m_posnetServerAndroid.sendRequestWait(m_host, m_port, cancelRequest);
            Log.i(TAG, "Cancel request if any: " + isOk);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //---------------------------------------------------------------------------
        //Print
        ParagonRequest paragon = ParagonRequest.Builder()
            .addLine(ParagonFakturaLine.Builder()
                .setName("Towar 1")
                //.setVatPercent("23,00", m_posnetServerAndroid.getVatCache())
                .setVatName("A", m_posnetServerAndroid.getVatCache())
                //.setVatIndex(0)
                .setPrice(100)
                .setQuantity(2.0f)
                .build())
            .addLine(ParagonFakturaLine.Builder()
                .setName("Towar 2")
                //.setVatPercent("23,00", m_posnetServerAndroid.getVatCache())
                .setVatName("A", m_posnetServerAndroid.getVatCache())
                //.setVatIndex(0)
                .setPrice(100)
                .setQuantity(1.0f)
                .build()
            )
            .setTotal(300)
            .setPaymentFormsTotal(300)
            //.setTaxIdInfo(ParagonTaxIdInfo.Builder()
            //    .setTaxId("5558889944")
            //    .setHighlighted(false)
            //    .setDescription("Hello")
            //    .build()
            //)
            .addPayment(PaymentObject.Builder()
                .setType(0)
                .setValue(150)
                .setName("By cash")
                .setRest(false)
                .build()
            )
            .addPayment(PaymentObject.Builder()
                .setType(2)
                .setValue(150)
                .setName("By VISA card")
                .setRest(false)
                .build()
            )
            .addExtraLine(ParagonFakturaExtraLine.Builder()
                .setText("Sample line #1")
                .setDoubleHeight(true)
                .setDoubleWidth(true)
                .setIdent(1)
                .build())
            .addExtraLine(ParagonFakturaExtraLine.Builder()
                .setText("Sample line #2")
                .setDoubleHeight(false)
                .setDoubleWidth(false)
                .setIdent(5)
                .build())
            .setFooter(ParagonFakturaFooter.Builder()
                .setAction(ParagonFakturaFooter.ParagonFakturaFooterAction.cut_move)
                .setCashier("Jan Kowalski")
                .setSystemNumber("ABC1234")
                .setCashregisterNumber("Kasa 5")
                //.setBarcode(FormsBarcodeRequest.Builder()
                //    .setCode("Hello")
                //    .build())
                //.setBarcode(FormsAztecCodeRequest.Builder()
                //    .setCode("Hello")
                //    .setWidth(10)
                //    .setCorrectionlevel(3)
                //    .setInputtype(FormsAztecCodeRequest.FormsAztecCodeInputType.ascii)
                //    .build())
                .setBarcode(FormsDmCodeRequest.Builder()
                    .setCode("Hello")
                    .setWidth(10)
                    .setInputtype(FormsDmCodeRequest.FormsDmCodeInputType.ascii)
                    .build())
                //.setBarcode(FormsQrCodeRequest.Builder()
                //    .setCode("Hello")
                //    .setWidth(10)
                //    .setCorrectionlevel(3)
                //    .setInputtype(FormsQrCodeRequest.FormsQrCodeInputType.ascii)
                //    .build())
                //.setBarcode(FormsPdf417CodeRequest.Builder()
                //    .setCode("Hello")
                //    .setWidth(2)
                //    .setCorrectionlevel(3)
                //    .setProportion(3)
                //    .setColumns(1)
                //    .setVertical(false)
                //    .setInputtype(FormsPdf417CodeRequest.FormsPdf417CodeInputType.ascii)
                //    .build())
                .build()
            )
            .build();

        m_posnetServerAndroid.sendRequest(m_host, m_port, paragon);
    }

}