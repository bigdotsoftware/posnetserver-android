## PosnetServer for Android

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


### Simple fiscal printout
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
Note that SDK doesn't automatically sum total value and setTotal() must be called explicitly. This is an intentional double-check to prevent accidental input of wrong VAT rate, quantity or price in line positions.

### With different/mixed payment methods
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

### Cancel current operation
```
CancelRequest cancelRequest = new CancelRequest();
m_posnetServerAndroid.sendRequest(m_host, m_port, cancelRequest);
```
