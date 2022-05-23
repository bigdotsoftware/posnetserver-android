package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.myapplication.databinding.ActivityMainBinding;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import eu.bigdotsoftware.posnetserver.AssetHelper;
import eu.bigdotsoftware.posnetserver.CancelRequest;
import eu.bigdotsoftware.posnetserver.CommandRequest;
import eu.bigdotsoftware.posnetserver.FakturaHeaderExInfo;
import eu.bigdotsoftware.posnetserver.FakturaOnlineTaxIdInfo;
import eu.bigdotsoftware.posnetserver.FakturaTaxIdInfo;
import eu.bigdotsoftware.posnetserver.FormFooter;
import eu.bigdotsoftware.posnetserver.FormHeader;
import eu.bigdotsoftware.posnetserver.FormsAztecCode;
import eu.bigdotsoftware.posnetserver.FormsAztecCodeRequest;
import eu.bigdotsoftware.posnetserver.FormsAztecCodeResponse;
import eu.bigdotsoftware.posnetserver.FormsBarcodeRequest;
import eu.bigdotsoftware.posnetserver.FormsBarcodeResponse;
import eu.bigdotsoftware.posnetserver.FormsDmCode;
import eu.bigdotsoftware.posnetserver.FormsDmCodeRequest;
import eu.bigdotsoftware.posnetserver.FormsDmCodeResponse;
import eu.bigdotsoftware.posnetserver.FormsLine;
import eu.bigdotsoftware.posnetserver.FormsPdf417Code;
import eu.bigdotsoftware.posnetserver.FormsPdf417CodeRequest;
import eu.bigdotsoftware.posnetserver.FormsPdf417CodeResponse;
import eu.bigdotsoftware.posnetserver.FormsQrCode;
import eu.bigdotsoftware.posnetserver.FormsQrCodeRequest;
import eu.bigdotsoftware.posnetserver.FormsQrCodeResponse;
import eu.bigdotsoftware.posnetserver.InvoiceOnlineRequest;
import eu.bigdotsoftware.posnetserver.InvoiceOnlineResponse;
import eu.bigdotsoftware.posnetserver.InvoiceRequest;
import eu.bigdotsoftware.posnetserver.InvoiceResponse;
import eu.bigdotsoftware.posnetserver.LicenseInfo;
import eu.bigdotsoftware.posnetserver.LicenseRegistrationInfo;
import eu.bigdotsoftware.posnetserver.LoginRequest;
import eu.bigdotsoftware.posnetserver.LogoutRequest;
import eu.bigdotsoftware.posnetserver.MaintenanceRequest;
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
import eu.bigdotsoftware.posnetserver.ReportCustomRequest;
import eu.bigdotsoftware.posnetserver.ReportEndOfDayRequest;
import eu.bigdotsoftware.posnetserver.ReportEndOfMonthRequest;
import eu.bigdotsoftware.posnetserver.ReportPeriodicRequest;
import eu.bigdotsoftware.posnetserver.ReportShiftRequest;
import eu.bigdotsoftware.posnetserver.StatusLicznikowRequest;
import eu.bigdotsoftware.posnetserver.StatusLicznikowResponse;
import eu.bigdotsoftware.posnetserver.StatusTotalizerowRequest;
import eu.bigdotsoftware.posnetserver.StatusTotalizerowResponse;
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

        m_binding.buttonPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    printFiscalPrintout();
                } catch (PosnetException e) {
                    e.printStackTrace();
                }
            }
        });

        //---------------------------------------------------------------------------
        //Initialize
        m_host = "192.168.0.68";
        m_port = 12346;
        m_posnetServerAndroid = new PosnetServerAndroid();
        m_posnetServerAndroid.setReadTimeout(30000L);
        m_posnetServerAndroid.setSocketTimeout(30000L);
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
                    ParagonResponse result = (ParagonResponse) response;
                    Log.i(TAG, String.format("ParagonResponse [%s]", result.toString()));
                }else if( response instanceof InvoiceResponse) {
                    InvoiceResponse result = (InvoiceResponse)response;
                    Log.i(TAG, String.format("InvoiceResponse [%s]", result.toString()));
                }else if( response instanceof InvoiceOnlineResponse) {
                    InvoiceOnlineResponse result = (InvoiceOnlineResponse) response;
                    Log.i(TAG, String.format("InvoiceOnlineResponse [%s]", result.toString()));
                }else if(response instanceof FormsBarcodeResponse) {
                    FormsBarcodeResponse result = (FormsBarcodeResponse) response;
                    Log.i(TAG, String.format("FormsBarcodeResponse [%s]", result.toString()));
                }else if(response instanceof FormsAztecCodeResponse) {
                    FormsAztecCodeResponse result = (FormsAztecCodeResponse) response;
                    Log.i(TAG, String.format("FormsAztecCodeResponse [%s]", result.toString()));
                }else if(response instanceof FormsDmCodeResponse) {
                    FormsDmCodeResponse result = (FormsDmCodeResponse) response;
                    Log.i(TAG, String.format("FormsDmCodeResponse [%s]", result.toString()));
                }else if(response instanceof FormsQrCodeResponse) {
                    FormsQrCodeResponse result = (FormsQrCodeResponse) response;
                    Log.i(TAG, String.format("FormsQrCodeResponse [%s]", result.toString()));
                }else if(response instanceof FormsPdf417CodeResponse) {
                    FormsPdf417CodeResponse result = (FormsPdf417CodeResponse) response;
                    Log.i(TAG, String.format("FormsPdf417CodeResponse [%s]", result.toString()));
                }else if (response instanceof VatRatesGetResponse) {
                    VatRatesGetResponse result = (VatRatesGetResponse)response;
                    for(VatRate vatRate : result.getRates()) {
                        Log.i(TAG, String.format("Received VAT rate [%d]: %s with value %s ", vatRate.getIndex(), vatRate.getName(), vatRate.getValue()));
                    }
                }else if( response instanceof StatusLicznikowResponse) {
                    StatusLicznikowResponse result = (StatusLicznikowResponse)response;
                    Log.i(TAG, String.format("Received Counters [%s]", result.toString()));
                }else if( response instanceof StatusTotalizerowResponse) {
                    StatusTotalizerowResponse result = (StatusTotalizerowResponse)response;
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
        //m_posnetServerAndroid.setDebugListener(new DebugWatcher() {
        //    @Override
        //    public void onMessage(String message) {
        //        Log.i(TAG, message);
        //    }
        //});


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


        //---------------------------------------------------------------------------
        //Register license file
        //using file name from assets registerLicenseFile(name)
        //LicenseRegistrationInfo licenseRegistrationInfo = m_posnetServerAndroid.registerLicenseFile(this, "Test.lic", Arrays.asList(m_host+":"+m_port));
        //alternatively using registerLicenseFile(bytes[])
        byte[] licbytes = AssetHelper.readAssetContent(this, "Test.lic");
        LicenseRegistrationInfo licenseRegistrationInfo = m_posnetServerAndroid.registerLicenseFile(this, licbytes, Arrays.asList(m_host+":"+m_port));

        if( !licenseRegistrationInfo.isOk())
            Log.e(TAG, "Cannot register license file: " + licenseRegistrationInfo.getError());
        else
            Log.e(TAG, "License file registered: OK");

        try {
            // printFiscalPrintoutSimple1();
            // printFiscalPrintoutSimple2();
            printFiscalPrintout();
            // printFiscalPrintoutWithRest();
            // printInvoiceSimple1();
            // printInvoice();
            // printInvoiceOnline();
            // printBarcodes();

            // VatRatesGetRequest vatRatesGetRequest = new VatRatesGetRequest();
            // m_posnetServerAndroid.sendRequest(m_host, m_port, vatRatesGetRequest);

            // StatusLicznikowRequest statusLicznikowRequest = new StatusLicznikowRequest();
            // m_posnetServerAndroid.sendRequest(m_host, m_port, statusLicznikowRequest);

            // StatusTotalizerowRequest statusTotalizerowRequest = new StatusTotalizerowRequest();
            // m_posnetServerAndroid.sendRequest(m_host, m_port, statusTotalizerowRequest);

            // VatRatesSetRequest vatRatesSetRequest = new VatRatesSetRequest();
            // vatRatesSetRequest.setRate(0,"A", "23");
            // vatRatesSetRequest.setRate(1,"B", "8");
            // vatRatesSetRequest.setRate(2,"C", "0");
            // vatRatesSetRequest.setRate(3,"D", "3");
            // vatRatesSetRequest.setRate(4,"G", "100");
            // m_posnetServerAndroid.sendRequest(m_host, m_port, vatRatesSetRequest);

            //MaintenanceRequest maintenanceRequest = new MaintenanceRequest("2022-11-01");
            //m_posnetServerAndroid.sendRequest(m_host, m_port, maintenanceRequest);

            // HeaderSetRequest headerSetRequest = new HeaderSetRequest("My Company", true);
            // m_posnetServerAndroid.sendRequest(m_host, m_port, headerSetRequest);

            // Date currentTime = Calendar.getInstance().getTime();
            // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            // ReportEndOfDayRequest reportEndOfDayRequest = new ReportEndOfDayRequest(sdf.format(currentTime));
            // m_posnetServerAndroid.sendRequest(m_host, m_port, reportEndOfDayRequest);

            // ReportEndOfDayRequest reportEndOfDayRequest = new ReportEndOfDayRequest();
            // m_posnetServerAndroid.sendRequest(m_host, m_port, reportEndOfDayRequest);

            // ReportEndOfMonthRequest reportEndOfMonthRequest = new ReportEndOfMonthRequest(sdf.format(currentTime), false);
            // m_posnetServerAndroid.sendRequest(m_host, m_port, reportEndOfMonthRequest);

            // ReportPeriodicRequest reportPeriodicRequest = new ReportPeriodicRequest("2022-01-01", "2022-01-31", false);
            // m_posnetServerAndroid.sendRequest(m_host, m_port, reportPeriodicRequest);

            // LoginRequest loginRequest = new LoginRequest("Kowalski", "Cash #2", true);
            // m_posnetServerAndroid.sendRequest(m_host, m_port, loginRequest);

            // LogoutRequest logoutRequest = new LogoutRequest("Kowalski", "Cash #2");
            // m_posnetServerAndroid.sendRequest(m_host, m_port, logoutRequest);

            // ReportShiftRequest reportShiftRequest = new ReportShiftRequest("Shift #3", true);
            // m_posnetServerAndroid.sendRequest(m_host, m_port, reportShiftRequest);

            // CommandRequest commandRequest = new CommandRequest();
            // commandRequest.addCommand("servicerep");
            // commandRequest.addCommand("cash", "kw,30850\nwp,T");
            // m_posnetServerAndroid.sendRequest(m_host, m_port, commandRequest);

            /*
            ReportCustomRequest reportCustomRequest = ReportCustomRequest.Builder()
                    .setHeader(new FormHeader(FormHeader.FormHeaderType.BON_RABATOWY))
                    .addLine(FormsLine.Builder()
                            .setType(0)
                            .addParams("0")
                            .addParams("123")
                            .addParams("123")
                            .addParams("123")
                            .build())
                    .addLine(FormsLine.Builder()
                            .setType(1)
                            .addParams("#################################")
                            .build())
                    .addLine(FormsLine.Builder()
                            .setType(2)
                            .addParams("Kowalski")
                            .build())
                    .addLine(FormsLine.Builder()
                            .setType(3)
                            .addParams("TOWAR 1")
                            .addParams("7777")
                            .build())
                    .addLine(FormsLine.Builder()
                            .setType(3)
                            .addParams("TOWAR 2")
                            .addParams("1111")
                            .build())
                    .addLine(FormsLine.Builder()
                            .setType(4)
                            .addParams("8888")
                            .build())
                    .addLine(FormsLine.Builder()
                            .setType(5)
                            .addParams("X")
                            .addParams("ABCD1234")
                            .build())
                    .addLine(FormsLine.Builder()
                            .setType(6)
                            .build())
                    .addLine(FormsLine.Builder()
                            .setType(7)
                            .addParams("2020-06-01")
                            .build())
                    .build();
            m_posnetServerAndroid.sendRequest(m_host, m_port, reportCustomRequest);
            */
        } catch (PosnetException e) {
            Log.e(TAG, "Posnet exception: " + e.getMessage());
        }
    }

    private void printBarcodes() throws PosnetException {

        FormsBarcodeRequest formsBarcodeRequest = FormsBarcodeRequest.Builder()
                .setCode("Hello")
                .setHeader(new FormHeader(FormHeader.FormHeaderType.DOKUMENT_NIEFISKALNY))
                .setFooter(FormFooter.Builder()
                        .setAction(FormFooter.FormFooterAction.cut_move)
                        .setCashier("Kowalski")
                        .setCashregisterNumber("ABCD")
                        .setSystemNumber("SSS12345")
                        .build())
                .addExtraLine(" ")
                .addExtraLine("   \\O/   ")
                .addExtraLine("    W    ")
                .addExtraLine("   / \\   ")
                .build();
        m_posnetServerAndroid.sendRequest(m_host, m_port, formsBarcodeRequest);

        FormsAztecCodeRequest formsAztecCodeRequest = FormsAztecCodeRequest.Builder()
                .setCode("Hello")
                .setWidth(10)
                .setCorrectionLevel(3)
                .setInputType(FormsAztecCode.FormsAztecCodeInputType.ascii)
                .setHeader(new FormHeader(FormHeader.FormHeaderType.DOKUMENT_NIEFISKALNY))
                .setFooter(FormFooter.Builder()
                        .setAction(FormFooter.FormFooterAction.cut_move)
                        .setCashier("Kowalski")
                        .setCashregisterNumber("ABCD")
                        .setSystemNumber("SSS12345")
                        .build())
                .addExtraLine(" ")
                .addExtraLine("   \\O/   ")
                .addExtraLine("    W    ")
                .addExtraLine("   / \\   ")
            .build();
        m_posnetServerAndroid.sendRequest(m_host, m_port, formsAztecCodeRequest);

        FormsQrCodeRequest formsQrCodeRequest = FormsQrCodeRequest.Builder()
                .setCode("Hello")
                .setWidth(10)
                .setCorrectionLevel(3)
                .setInputType(FormsQrCode.FormsQrCodeInputType.ascii)
                .setHeader(new FormHeader(FormHeader.FormHeaderType.DOKUMENT_NIEFISKALNY))
                .setFooter(FormFooter.Builder()
                        .setAction(FormFooter.FormFooterAction.cut_move)
                        .setCashier("Kowalski")
                        .setCashregisterNumber("ABCD")
                        .setSystemNumber("SSS12345")
                        .build())
                .addExtraLine(" ")
                .addExtraLine("   \\O/   ")
                .addExtraLine("    W    ")
                .addExtraLine("   / \\   ")
                .build();
        m_posnetServerAndroid.sendRequest(m_host, m_port, formsQrCodeRequest);

        FormsDmCodeRequest formsDmCodeRequest = FormsDmCodeRequest.Builder()
                .setCode("Hello")
                .setWidth(10)
                .setInputType(FormsDmCode.FormsDmCodeInputType.ascii)
                .setHeader(new FormHeader(FormHeader.FormHeaderType.DOKUMENT_NIEFISKALNY))
                .setFooter(FormFooter.Builder()
                        .setAction(FormFooter.FormFooterAction.cut_move)
                        .setCashier("Kowalski")
                        .setCashregisterNumber("ABCD")
                        .setSystemNumber("SSS12345")
                        .build())
                .addExtraLine(" ")
                .addExtraLine("   \\O/   ")
                .addExtraLine("    W    ")
                .addExtraLine("   / \\   ")
                .build();
        m_posnetServerAndroid.sendRequest(m_host, m_port, formsDmCodeRequest);

        FormsPdf417CodeRequest formsPdf417CodeRequest = FormsPdf417CodeRequest.Builder()
                .setCode("Hello")
                .setWidth(2)
                .setCorrectionLevel(3)
                .setProportion(3)
                .setColumns(1)
                .setVertical(false)
                .setInputType(FormsPdf417Code.FormsPdf417CodeInputType.ascii)
                .setHeader(new FormHeader(FormHeader.FormHeaderType.DOKUMENT_NIEFISKALNY))
                .setFooter(FormFooter.Builder()
                        .setAction(FormFooter.FormFooterAction.cut_move)
                        .setCashier("Kowalski")
                        .setCashregisterNumber("ABCD")
                        .setSystemNumber("SSS12345")
                        .build())
                .addExtraLine(" ")
                .addExtraLine("   \\O/   ")
                .addExtraLine("    W    ")
                .addExtraLine("   / \\   ")
                .build();
        m_posnetServerAndroid.sendRequest(m_host, m_port, formsPdf417CodeRequest);

    }
    private void printInvoiceSimple1() throws PosnetException {
        InvoiceRequest invoice = InvoiceRequest.Builder()
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
                .setHeader(FakturaTaxIdInfo.Builder()
                        .setNumber("56/2020")
                        .setTaxId("584-222-98-89")
                        .setBuyerName(new ArrayList<>(Arrays.asList("Nazwa firmy", "ul. Miejska 56", "88-888 Miasto")))
                        .setPaymentDate("2020-02-15")
                        .setPaymentForm("electronic transfer")
                        .build()
                )
                .setTotal(1400)
                .build();

        m_posnetServerAndroid.sendRequest(m_host, m_port, invoice);
    }

    private void printInvoice() throws PosnetException {
        InvoiceRequest invoice = InvoiceRequest.Builder()
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
                .addExtraLine(" ")
                .addExtraLine("+---------+")
                .addExtraLine("|   \\O/   |")
                .addExtraLine("|    W    |")
                .addExtraLine("|   / \\   |")
                .addExtraLine("+---------+")
                //.addExtraLine1("Pole dodatkowe pod datą #1")          //WARNING! Some devices may not support it
                //.addExtraLine1("Pole dodatkowe pod datą #2")          //WARNING! Some devices may not support it
                //.addExtraLine2("Pole dodatkowe pod kwotą #1")         //WARNING! Some devices may not support it
                //.addExtraLine2("Pole dodatkowe pod kwotą #2")         //WARNING! Some devices may not support it
                //.addExtraLine3("Pole dodatkowe #1", "top")            //WARNING! Some devices may not support it
                //.addExtraLine3("Pole dodatkowe #2", "bottom")         //WARNING! Some devices may not support it
                //.addExtraLine3("Pole dodatkowe #3", "middle")         //WARNING! Some devices may not support it
                .setHeader(FakturaTaxIdInfo.Builder()
                        .setNumber("56/2020")
                        .setTaxId("584-222-98-89")
                        .setBuyerName(new ArrayList<>(Arrays.asList("Nazwa firmy", "ul. Miejska 56", "88-888 Miasto")))
                        .setPaymentDate("2020-02-15")
                        .setPaymentForm("electronic transfer")
                        .setClientName("Kowalski Jan")
                        .setSellerName("Nowak Tomasz")
                        .setCopies(0)
                        .setIssuingCollectingPersonsFlag(true)
                        .setFormat(0)
                        .setOriginalCopyHeadline(true)
                        .setPrintCopy(true)
                        .build()
                )
                .setHeaderEx(FakturaHeaderExInfo.Builder()
                        .setCarPlateNumber("WX 12345")
                        .setOrderNumber("45/25/2000358")
                        .setOrderPerson("Mike")
                        .setClientOrderNumber("789/75CGX")
                        .setClientIdent("DX12")
                        .setDeliveryConditions("Kolejny dzień roboczy")
                        .setDeliveryType("Kurier")
                        .build()
                )
                .setFooter(ParagonFakturaFooter.Builder()
                        .setAction(ParagonFakturaFooter.ParagonFakturaFooterAction.cut_move)
                        .setCashier("Jan Kowalski")
                        .setSystemNumber("ABC1234")
                        .setCashregisterNumber("Kasa 5")
                        .setBarcode(FormsQrCode.Builder()
                                .setCode("Hello")
                                .setWidth(10)
                                .setCorrectionLevel(3)
                                .setInputType(FormsQrCode.FormsQrCodeInputType.ascii)
                                .build())
                        .build()
                )
                .setTotal(1400)
                .build();

        m_posnetServerAndroid.sendRequest(m_host, m_port, invoice);
    }

    private void printInvoiceOnline() throws PosnetException {
        InvoiceOnlineRequest invoice = InvoiceOnlineRequest.Builder()
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
                .addExtraLine3("Pole dodatkowe #1", "top")
                .addExtraLine3("Pole dodatkowe #2", "bottom")
                .addExtraLine3("Pole dodatkowe #3", "middle")
                .setHeader(FakturaOnlineTaxIdInfo.Builder()
                        .setInvoiceName("Nazwa Faktury")
                        .setCopies(0)
                        .setOriginalCopyHeadline(true)
                        .setLineWidth(40)
                        .setFiscalLineWidth(40)
                        .setBuyerName(new ArrayList<>(Arrays.asList("Nazwa firmy")))
                        .setTaxId("584-222-98-89")
                        .setBuyerAddress(new ArrayList<>(Arrays.asList("ul. Miejska 56", "88-888 Miasto")))
                        .setBuyerSection(0)
                        .setBuyerAttributes(0)
                        .setNumber("56/2020")
                        .setInvoiceNumberSection(0)
                        .setInvoiceNumberAttributes(0)
                        .build()
                )
                .setHeaderEx(FakturaHeaderExInfo.Builder()
                        .setCarPlateNumber("WX 12345")
                        .setOrderNumber("45/25/2000358")
                        .setOrderPerson("Mike")
                        .setClientOrderNumber("789/75CGX")
                        .setClientIdent("DX12")
                        .setDeliveryConditions("Kolejny dzień roboczy")
                        .setDeliveryType("Kurier")
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
                .setFooter(ParagonFakturaFooter.Builder()
                        .setAction(ParagonFakturaFooter.ParagonFakturaFooterAction.cut_move)
                        .setCashier("Jan Kowalski")
                        .setSystemNumber("ABC1234")
                        .setCashregisterNumber("Kasa 5")
                        .setBarcode(FormsQrCode.Builder()
                                .setCode("Hello")
                                .setWidth(10)
                                .setCorrectionLevel(3)
                                .setInputType(FormsQrCode.FormsQrCodeInputType.ascii)
                                .build())
                        .build()
                )
                .setTotal(1400)
                .setPaymentFormsTotal(1400)
                .build();

        m_posnetServerAndroid.sendRequest(m_host, m_port, invoice);
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
    private void printFiscalPrintoutWithRest() throws PosnetException {
        ParagonRequest paragon = ParagonRequest.Builder()
                .addLine(ParagonFakturaLine.Builder()
                        .setName("Towar 1")
                        .setVatName("A", m_posnetServerAndroid.getVatCache())
                        .setPrice(200)
                        .setQuantity(1.0f)
                        .build()
                )
                .setTotal(200)
                .setRest(300)
                .setPaymentFormsTotal(500)
                .addPayment(PaymentObject.Builder()
                        .setType(2)
                        .setValue(500)
                        .setName("By cash")
                        .setRest(false)
                        .build()
                )
                .addPayment(PaymentObject.Builder()
                        .setType(0)
                        .setValue(300)
                        .setName("By cash")
                        .setRest(true)
                        .build()
                )
                .build();

        m_posnetServerAndroid.sendRequest(m_host, m_port, paragon);
    }
    private void printFiscalPrintout() throws PosnetException {

        //---------------------------------------------------------------------------
        //Cancel request if any in progress
        Log.i(TAG, "Cancel request in progress");
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
                //.setType(0)
                .setType(PaymentObject.PaymentType.cash)
                .setValue(150)
                .setName("By cash")
                .setRest(false)
                .build()
            )
            .addPayment(PaymentObject.Builder()
                //.setType(2)
                .setType(PaymentObject.PaymentType.card)
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
                //.setBarcode(FormsBarcode.Builder()
                //    .setCode("Hello")
                //    .build())
                //.setBarcode(FormsAztecCode.Builder()
                //    .setCode("Hello")
                //    .setWidth(10)
                //    .setCorrectionLevel(3)
                //    .setInputType(FormsAztecCode.FormsAztecCodeInputType.ascii)
                //    .build())
                // .setBarcode(FormsDmCode.Builder()
                //     .setCode("Hello")
                //     .setWidth(10)
                //     .setInputType(FormsDmCode.FormsDmCodeInputType.ascii)
                //     .build())
                .setBarcode(FormsQrCode.Builder()
                    .setCode("Hello")
                    .setWidth(10)
                    .setCorrectionLevel(3)
                    .setInputType(FormsQrCode.FormsQrCodeInputType.ascii)
                    .build())
                //.setBarcode(FormsPdf417Code.Builder()
                //    .setCode("Hello")
                //    .setWidth(2)
                //    .setCorrectionLevel(3)
                //    .setProportion(3)
                //    .setColumns(1)
                //    .setVertical(false)
                //    .setInputType(FormsPdf417Code.FormsPdf417CodeInputType.ascii)
                //    .build())
                .build()
            )
            .build();

        m_posnetServerAndroid.sendRequest(m_host, m_port, paragon);
    }

}
