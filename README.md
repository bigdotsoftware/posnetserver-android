## PosnetServer for Android

### Overview
PosnetServer is a product of Big Dot Software. Product is distributed in two versions:
 - RESTful API (download from here: https://bigdotsoftware.pl/posnetserver-restful-service-dla-drukarek-posnet/ and swagger documentation is here: https://editor.swagger.io/?url=https://download2.bigdotsoftware.pl/github_posnet_swagger/swagger-3.11.yaml)
 - Android SDK

Android SDK doesn’t use RESTful API - it communicates directly with Posnet printer using Eth connection. RESTful API has a wider list of features than the current version of Android SDK, but we constantly work on full alignment of both of them. When you need a feature in the Android SDK which is not available yet, please contact us.


### SDK initialization

```
    m_posnetServerAndroid = new PosnetServerAndroid();
    m_posnetServerAndroid.setReadTimeout(6000L);
    m_posnetServerAndroid.setProcessListener(new ProcessWatcher() {
        @Override
        public void onStart(PosnetRequest request) {

        }

        @Override
        public void onProcess(String message) {

        }

        @Override
        public void onDone(PosnetRequest request, String message) {

        }

        @Override
        public void onError(PosnetRequest request, String error) {

        }
    });

    InputStream ins = getResources().openRawResource(
        getResources().getIdentifier("cmbth_pl", "raw", getPackageName()));
    m_posnetServerAndroid.initializeErrorList(ins);
```


### Simple fiscal printout (pol. Paragon fiskalny)
```
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
```
![posnetserver-android](https://github.com/bigdotsoftware/posnetserver-android/raw/master/img/print1.png)

Note that SDK doesn't automatically sum total value and setTotal() must be called explicitly. This is an intentional double-check to prevent accidental input of wrong VAT rate, quantity or price in line positions.

### With different/mixed payment methods (pol. Paragon fiskalny z typami płatności)
```
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
```
![posnetserver-android](https://github.com/bigdotsoftware/posnetserver-android/raw/master/img/print2.png)

Like previously, when using payments you must explicitly confirm the total using setPaymentFormsTotal() method.

### Full list of options

```
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
            .setTaxIdInfo(ParagonTaxIdInfo.Builder()
                .setTaxId("5558889944")
                .setHighlighted(false)
                .setDescription("Hello")
                .build()
            )
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
                .setBarcode(FormsQrCodeRequest.Builder()
                    .setCode("Hello")
                    .setWidth(10)
                    .setCorrectionlevel(3)
                    .setInputtype(FormsQrCodeRequest.FormsQrCodeInputType.ascii)
                    .build())
                .build()
            )
            .build();

        m_posnetServerAndroid.sendRequest(m_host, m_port, paragon);
```
![posnetserver-android](https://github.com/bigdotsoftware/posnetserver-android/raw/master/img/print4.png)
### Cancel current operation
```
CancelRequest cancelRequest = new CancelRequest();
m_posnetServerAndroid.sendRequest(m_host, m_port, cancelRequest);
```

### Simple invoice (pol. Faktura VAT)
```
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
```
![posnetserver-android](https://github.com/bigdotsoftware/posnetserver-android/raw/master/img/print-fv1.png)

### Full list of options
```
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
        .addExtraLine("")
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
                .setBarcode(FormsQrCodeRequest.Builder()
                        .setCode("Hello")
                        .setWidth(10)
                        .setCorrectionlevel(3)
                        .setInputtype(FormsQrCodeRequest.FormsQrCodeInputType.ascii)
                        .build())
                .build()
        )
        .setTotal(1400)
        .build();

m_posnetServerAndroid.sendRequest(m_host, m_port, invoice);
```
![posnetserver-android](https://github.com/bigdotsoftware/posnetserver-android/raw/master/img/print-fv2.png)


### Invoice on Posnet ONLINE devices (pol. Faktura VAT na urządzeniach ONLINE)
Posnet has two type of printers: regular printers and ONLINE printers. Typical fiscal recipe is printed by the same request on both devices, but situation is different with invoices. If you use ONLINE device, then previous example won't work. You must use different request as below:

```
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
```

### eParagon/eInvoice examples (pol. obsługa eParagonu i eFaktury)
#### eParagon example
```
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
        //Cancel edocument request if any in progress
        Log.i(TAG, "Cancel eDocument request in progress");
        CancelEDocumentRequest cancelEDocumentRequest = new CancelEDocumentRequest();
        try {
            Boolean isOk = m_posnetServerAndroid.sendRequestWait(m_host, m_port, cancelEDocumentRequest);
            Log.i(TAG, "Cancel eDocument request if any: " + isOk);
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
                        //.setVatName("A", m_posnetServerAndroid.getVatCache())
                        .setVatIndex(0)
                        .setPrice(1235)
                        .setQuantity(1.0f)
                        .build()
                )
                .addLine(ParagonFakturaLine.Builder()
                        .setName("Towar 2")
                        //.setVatPercent("23,00", m_posnetServerAndroid.getVatCache())
                        //.setVatName("A", m_posnetServerAndroid.getVatCache())
                        .setVatIndex(0)
                        .setPrice(3456)
                        .setQuantity(1.0f)
                        .build()
                )
                .addLine(ParagonFakturaLine.Builder()
                        .setName("Towar stawka B")
                        .setVatName("B", m_posnetServerAndroid.getVatCache())
                        .setPrice(600)
                        .setQuantity(1.0f)
                        .build()
                )
                .addLine(ParagonFakturaLine.Builder()
                        .setName("Towar stawka C")
                        .setVatName("C", m_posnetServerAndroid.getVatCache())
                        .setPrice(400)
                        .setQuantity(2.0f)
                        .build()
                )
                .addLine(ParagonFakturaLine.Builder()
                        .setName("Towar stawka D")
                        .setVatName("D", m_posnetServerAndroid.getVatCache())
                        .setPrice(400)
                        .setQuantity(3.0f)
                        .build()
                )
                .addLine(ParagonFakturaLine.Builder()
                        .setName("Towar stawka E")
                        .setVatName("E", m_posnetServerAndroid.getVatCache())
                        .setPrice(250)
                        .setQuantity(3.0f)
                        .build()
                )
                .setTaxIdInfo(ParagonTaxIdInfo.Builder()
                        .setTaxId("5558889944")
                        .setHighlighted(false)
                        .setDescription("NIP NABYWCY")
                        .build()
                )
                .enableEParagonMode(EParagonMode.Builder()
                        .setIdz("11")   //alternatively IDZ can be modelled as: "11|jan.kowalski-1@email.com|500500500"
                        .setClientEmail("jan.kowalski-1@email.com")
                        .setClientPhone("500500500")
                        .setService("https://eparagon.cloud:4051")
                        .build()
                )
                .setTotal(8041)
                .setPaymentFormsTotal(9000)
                .setRest(959)
                .addPayment(PaymentObject.Builder()
                        .setType(2)
                        .setValue(9000)
                        .setName("Konto klienta")
                        .setRest(false)
                        .build()
                )
                .addPayment(PaymentObject.Builder()
                        .setType(8)
                        .setValue(959)
                        .setName("Konto klienta")
                        .setRest(true)
                        .build()
                )
                .build();

        m_posnetServerAndroid.sendRequest(m_host, m_port, paragon);
```

#### eInvoice (eFaktura) example
```
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
        //Cancel edocument request if any in progress
        Log.i(TAG, "Cancel eDocument request in progress");
        CancelEDocumentRequest cancelEDocumentRequest = new CancelEDocumentRequest();
        try {
            Boolean isOk = m_posnetServerAndroid.sendRequestWait(m_host, m_port, cancelEDocumentRequest);
            Log.i(TAG, "Cancel eDocument request if any: " + isOk);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //---------------------------------------------------------------------------
        //Process Invoice as an eInvoice (pol. eFaktura)
        InvoiceOnlineRequest invoice = InvoiceOnlineRequest.Builder()
                .addLine(ParagonFakturaLine.Builder()
                        .setName("Coca-Cola")
                        .setVatIndex(0)
                        .setPrice(550)
                        .setQuantity(2.0f)
                        .setDescription("Some description")
                        .setUnit("l")
                        .setCode("DEF12345")
                        .setPKWiU("50.41.34.1")
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
                .enableEInvoiceMode(EInvoiceMode.Builder()
                        .setIdz("11")   //alternatively IDZ can be modelled: "11|jan.kowalski-1@email.com|500500500"
                        .setClientEmail("jan.kowalski-1@email.com")
                        .setClientPhone("500500500")
                        .setService("https://eparagon.cloud:4051")
                        .build()
                )
                .setTotal(1400)
                .build();

        m_posnetServerAndroid.sendRequest(m_host, m_port, invoice);
```

### Discounts and Surcharges (pol. rabaty i narzuty)
#### VAT discount
```
     ParagonRequest paragon = ParagonRequest.Builder()
             .addLine(ParagonFakturaLine.Builder()
                     .setName("Towar 1")
                     //.setVatPercent("23,00", m_posnetServerAndroid.getVatCache())
                     .setVatName("A", m_posnetServerAndroid.getVatCache())
                     //.setVatIndex(0)
                     .setPrice(1350)
                     .setQuantity(1.0f)
                     .addDiscount(DiscountObject.Builder()
                             .prepareVatDiscount()
                             //.setVatPercent("23,00", m_posnetServerAndroid.getVatCache())      //identifier of the VAT rate that discount/surcharge is granted and in which the goods were sold
                             .setVatName("A", m_posnetServerAndroid.getVatCache())         //identifier of the VAT rate that discount/surcharge is granted and in which the goods were sold
                             //.setVatIndex(0)                                                   //identifier of the VAT rate that discount/surcharge is granted and in which the goods were sold
                             .setDiscount(true)                  //true - discount, false - surcharge
                             .setValueAmount(100)                //value of discount/surcharge
                             //.setValuePercent(1500)            //percent value of discount/surcharge
                             .setName("Summer Promo 1")          //name of discount/surcharge
                             .setBaseAmount(1350)                //the amount of the sale from which the discount/surcharge is granted
                             .build()
                     )
                     .addDiscount(DiscountObject.Builder()
                             .prepareVatDiscount()
                             //.setVatPercent("23,00", m_posnetServerAndroid.getVatCache())
                             .setVatName("A", m_posnetServerAndroid.getVatCache())
                             //.setVatIndex(0)
                             .setDiscount(true)
                             .setValueAmount(200)
                             //.setValuePercent(1500)
                             .setName("Summer Promo 2")
                             .setBaseAmount(1350)
                             .build()
                     )
                     .build()
             )
             .addLine(ParagonFakturaLine.Builder()
                     .setName("Towar 2")
                     //.setVatPercent("23,00", m_posnetServerAndroid.getVatCache())
                     .setVatName("A", m_posnetServerAndroid.getVatCache())
                     //.setVatIndex(0)
                     .setPrice(2350)
                     .setQuantity(1.0f)
                     .build()
             )
             .setTotal(3400)
             .build();

     m_posnetServerAndroid.sendRequest(m_host, m_port, paragon);
```
![discount-vat](https://github.com/bigdotsoftware/posnetserver-android/raw/master/img/discount-vat.jpg)

#### Line discount
```
     ParagonRequest paragon = ParagonRequest.Builder()
             .addLine(ParagonFakturaLine.Builder()
                     .setName("Towar 1")
                     //.setVatPercent("23,00", m_posnetServerAndroid.getVatCache())
                     .setVatName("A", m_posnetServerAndroid.getVatCache())
                     //.setVatIndex(0)
                     .setPrice(1350)
                     .setQuantity(1.0f)
                     .addDiscount(DiscountObject.Builder()
                             .prepareLineDiscount()
                             //.setVatPercent("23,00", m_posnetServerAndroid.getVatCache())      //identifier of the VAT rate that discount/surcharge is granted and in which the goods were sold
                             .setVatName("A", m_posnetServerAndroid.getVatCache())         //identifier of the VAT rate that discount/surcharge is granted and in which the goods were sold
                             //.setVatIndex(0)                                                   //identifier of the VAT rate that discount/surcharge is granted and in which the goods were sold
                             .setRelatedName("Towar X")          //name of the product to which the discount/surcharge applies
                             .setBaseAmount(1350)                //the amount of the sale from which the discount/surcharge is granted
                             .setCancelDiscount(false)           //cancellation of a discount or surcharge
                             .setDiscount(true)                  //true - discount, false - surcharge
                             .setValueAmount(100)                //value of discount/surcharge
                             //.setValuePercent(1500)            //percent value of discount/surcharge
                             .setName("Summer Line Promo 1")     //name of discount/surcharge
                             .build()
                     )
                     .addDiscount(DiscountObject.Builder()
                             .prepareLineDiscount()
                             //.setVatPercent("23,00", m_posnetServerAndroid.getVatCache())
                             .setVatName("A", m_posnetServerAndroid.getVatCache())
                             //.setVatIndex(0)
                             .setValueAmount(200)
                             .setCancelDiscount(false)
                             .setRelatedName("Towar Y")
                             .setBaseAmount(1350)
                             .setCancelDiscount(false)
                             .setDiscount(true)
                             .setValueAmount(200)
                             //.setValuePercent(1500)
                             .setName("Summer Line Promo 2")
                             .build()
                     )
                     .build()
             )
             .addLine(ParagonFakturaLine.Builder()
                     .setName("Towar 2")
                     //.setVatPercent("23,00", m_posnetServerAndroid.getVatCache())
                     .setVatName("A", m_posnetServerAndroid.getVatCache())
                     //.setVatIndex(0)
                     .setPrice(2350)
                     .setQuantity(1.0f)
                     .build()
             )
             .setTotal(3400)
             .build();

     m_posnetServerAndroid.sendRequest(m_host, m_port, paragon);
```
![discount-line](https://github.com/bigdotsoftware/posnetserver-android/raw/master/img/discount-line.jpg)
#### Promotion discount
```
     ParagonRequest paragon = ParagonRequest.Builder()
             .addLine(ParagonFakturaLine.Builder()
                     .setName("Towar 1")
                     //.setVatPercent("23,00", m_posnetServerAndroid.getVatCache())
                     .setVatName("A", m_posnetServerAndroid.getVatCache())
                     //.setVatIndex(0)
                     .setPrice(1350)
                     .setQuantity(1.0f)
                     .addDiscount(DiscountObject.Builder()
                             .preparePromoDiscount()
                             //.setVatPercent("23,00", m_posnetServerAndroid.getVatCache())
                             .setVatName("A", m_posnetServerAndroid.getVatCache())
                             //.setVatIndex(0)
                             .setValueAmount(100)
                             .setCancelDiscount(false)
                             .setName("Summer Promo 1")
                             .build()
                     )
                     .addDiscount(DiscountObject.Builder()
                             .preparePromoDiscount()
                             //.setVatPercent("23,00", m_posnetServerAndroid.getVatCache())
                             .setVatName("A", m_posnetServerAndroid.getVatCache())
                             //.setVatIndex(0)
                             .setValueAmount(200)
                             .setCancelDiscount(false)
                             .setName("Summer Promo 2")
                             .build()
                     )
                     .build()
             )
             .addLine(ParagonFakturaLine.Builder()
                     .setName("Towar 2")
                     //.setVatPercent("23,00", m_posnetServerAndroid.getVatCache())
                     .setVatName("A", m_posnetServerAndroid.getVatCache())
                     //.setVatIndex(0)
                     .setPrice(2350)
                     .setQuantity(1.0f)
                     .build()
             )
             .setTotal(3400)
             .build();

     m_posnetServerAndroid.sendRequest(m_host, m_port, paragon);
```
![discount-promo](https://github.com/bigdotsoftware/posnetserver-android/raw/master/img/discount-promo.jpg)
#### SubTotal discount
```
     ParagonRequest paragon = ParagonRequest.Builder()
             .addLine(ParagonFakturaLine.Builder()
                     .setName("Towar 1")
                     //.setVatPercent("23,00", m_posnetServerAndroid.getVatCache())
                     .setVatName("A", m_posnetServerAndroid.getVatCache())
                     //.setVatIndex(0)
                     .setPrice(1350)
                     .setQuantity(1.0f)
                     .addDiscount(DiscountObject.Builder()
                             .prepareSubTotalDiscount()
                             .setName("Summer Promo 1")
                             .setDiscount(true)
                             //.setValuePercent(20)
                             .setValueAmount(100)
                             .build()
                     )
                     .addDiscount(DiscountObject.Builder()
                             .prepareSubTotalDiscount()
                             .setName("Summer Promo 2")
                             .setDiscount(true)
                             //.setValuePercent(20)
                             .setValueAmount(200)
                             .build()
                     )
                     .build()
             )
             .addLine(ParagonFakturaLine.Builder()
                     .setName("Towar 2")
                     //.setVatPercent("23,00", m_posnetServerAndroid.getVatCache())
                     .setVatName("A", m_posnetServerAndroid.getVatCache())
                     //.setVatIndex(0)
                     .setPrice(2350)
                     .setQuantity(1.0f)
                     .build()
             )
             .setTotal(3400)
             .build();

     m_posnetServerAndroid.sendRequest(m_host, m_port, paragon);
```
![discount-subtotal](https://github.com/bigdotsoftware/posnetserver-android/raw/master/img/discount-subtotal.jpg)
#### Bill discount
```
    ParagonRequest paragon = ParagonRequest.Builder()
            .addLine(ParagonFakturaLine.Builder()
                    .setName("Towar 1")
                    //.setVatPercent("23,00", m_posnetServerAndroid.getVatCache())
                    .setVatName("A", m_posnetServerAndroid.getVatCache())
                    //.setVatIndex(0)
                    .setPrice(1350)
                    .setQuantity(1.0f)
                    .build())
            .addLine(ParagonFakturaLine.Builder()
                    .setName("Towar 2")
                    //.setVatPercent("23,00", m_posnetServerAndroid.getVatCache())
                    .setVatName("A", m_posnetServerAndroid.getVatCache())
                    //.setVatIndex(0)
                    .setPrice(2350)
                    .setQuantity(1.0f)
                    .build()
            )
            .addDiscountTotal(DiscountObject.Builder()
                    .prepareBillDiscount()
                    .setDiscount(true)
                    .setName("Summer Promo")
                    .setValueAmount(100)
                    .build()
            )
            .setTotal(3600)
            .build();

    m_posnetServerAndroid.sendRequest(m_host, m_port, paragon);
```
![discount-bill](https://github.com/bigdotsoftware/posnetserver-android/raw/master/img/discount-bill.jpg)

### Barcodes
You can print barcode as a separate printout.

```
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
```

### Sessions
#### Cashier login
```
LoginRequest loginRequest = new LoginRequest("Kowalski", "Cash #2", true);
m_posnetServerAndroid.sendRequest(m_host, m_port, loginRequest);
```
#### Cashier logout
```
LogoutRequest logoutRequest = new LogoutRequest("Kowalski", "Cash #2");
m_posnetServerAndroid.sendRequest(m_host, m_port, logoutRequest);
```

### Reports
#### Daily report (pol. Raport dobowy)
```
Date currentTime = Calendar.getInstance().getTime();
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
ReportEndOfDayRequest reportEndOfDayRequest = new ReportEndOfDayRequest(sdf.format(currentTime));
m_posnetServerAndroid.sendRequest(m_host, m_port, reportEndOfDayRequest);
```
#### Monthly report (pol. Raport miesięczny)
```
Date currentTime = Calendar.getInstance().getTime();
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
ReportEndOfMonthRequest reportEndOfMonthRequest = new ReportEndOfMonthRequest(sdf.format(currentTime), false);
m_posnetServerAndroid.sendRequest(m_host, m_port, reportEndOfMonthRequest);
```

#### Periodic report (pol. Raport okresowy)
```
ReportPeriodicRequest reportPeriodicRequest = new ReportPeriodicRequest("2022-01-01", "2022-01-31", false);
m_posnetServerAndroid.sendRequest(m_host, m_port, reportPeriodicRequest);
```

#### Shift report (pol. Raport zmianowy)
```
ReportShiftRequest reportShiftRequest = new ReportShiftRequest("Shift #3", true);
m_posnetServerAndroid.sendRequest(m_host, m_port, reportShiftRequest);
```

#### Cash deposit (pol. Wpłata gotówki) (from version 1.1)
```
CashDepositRequest cashDepositRequest = new CashDepositRequest(30850);
m_posnetServerAndroid.sendRequest(m_host, m_port, cashDepositRequest);
```

#### Cash Withdrawal (pol. Wypłata gotówki) (from version 1.1)
```
CashWithdrawalRequest cashWithdrawalRequest = new CashWithdrawalRequest(30850);
m_posnetServerAndroid.sendRequest(m_host, m_port, cashWithdrawalRequest);
```

#### Custom report
```
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
```

![posnetserver-android](https://github.com/bigdotsoftware/posnetserver-android/raw/master/img/print31.png)

### Device management
#### VAT rates
```
VatRatesGetRequest vatRatesGetRequest = new VatRatesGetRequest();
m_posnetServerAndroid.sendRequest(host, port, vatRatesGetRequest);
```

```
VatRatesSetRequest vatRatesSetRequest = new VatRatesSetRequest();
vatRatesSetRequest.setRate(0,"A", "23");
vatRatesSetRequest.setRate(1,"B", "8");
vatRatesSetRequest.setRate(2,"C", "0");
vatRatesSetRequest.setRate(3,"D", "3");
vatRatesSetRequest.setRate(4,"G", "100");
m_posnetServerAndroid.sendRequest(host, port, vatRatesSetRequest);
```

#### Header
```
HeaderSetRequest headerSetRequest = new HeaderSetRequest("My Company", true);
m_posnetServerAndroid.sendRequest(host, port, headerSetRequest);
```

#### Maintenenace date
```
MaintenanceRequest maintenanceRequest = new MaintenanceRequest("2022-11-01");
m_posnetServerAndroid.sendRequest(host, port, maintenanceRequest);
```


