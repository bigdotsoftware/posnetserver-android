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
