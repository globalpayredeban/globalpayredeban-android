# Redeban Payment Android SDK
===================

Redeban Payment Android SDK is a library that allows developers to easily connect to the Redeban CREDITCARDS API

![Example](https://github.com/globalpayredeban/globalpayredeban-android/blob/master/img/global_pay1.jpg)


## Installation

### Android Studio (or Gradle)

Add this line to your app's `build.gradle` inside the `dependencies` section:

    implementation 'com.redeban:payment-android:1.2.8'

### ProGuard

If you're planning on optimizing your app with ProGuard, make sure that you exclude the Redeban bindings. You can do this by adding the following to your app's `proguard.cfg` file:

    -keep class com.redeban.payment.** { *; }

## Usage

### Using the CardMultilineWidget

You can add a widget to your apps that easily handles the UI states for collecting card data.

First, add the CardMultilineWidget to your layout.

```xml
<com.redeban.payment.view.CardMultilineWidget
        android:id="@+id/card_multiline_widget"
        android:layout_alignParentTop="true"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"/>
```

You can customize the view with this tags:

```xml
app:shouldShowPostalCode="true"
app:shouldShowRedebanLogo="true"
app:shouldShowCardHolderName="true"
app:shouldShowScanCard="true"
```

In order to use any of this tags, you'll need to enable the app XML namespace somewhere in the layout.

```xml
xmlns:app="http://schemas.android.com/apk/res-auto"
```

To get a `Card` object from the `CardMultilineWidget`, you ask the widget for its card.

```java
Card cardToSave = cardWidget.getCard();
if (cardToSave == null) {
    Alert.show(mContext,
        "Error",
        "Invalid Card Data");
    return;
}
```

If the returned `Card` is null, error states will show on the fields that need to be fixed. 

Once you have a non-null `Card` object from the widget, you can call [addCard](#addCard).

### Init library
You should initialize the library on your Application or in your first Activity. 

```java
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.redeban.payment.Payment;
import com.redeban.payment.example.utils.Constants;

public class MainActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);      
      setContentView(R.layout.activity_main);
      
      /**
       * Init library
       *
       * @param test_mode false to use production environment
       * @param redeban_client_app_code provided by Redeban.
       * @param redeban_client_app_key provided by Redeban.
       */
      Payment.setEnvironment(Constants.REDEBAN_IS_TEST_MODE, Constants.REDEBAN_CLIENT_APP_CODE, Constants.REDEBAN_CLIENT_APP_KEY);
      
      
       // In case you have your own Fraud Risk Merchant Id
       //Payment.setRiskMerchantId(1000);
       // Note: for most of the devs, that's not necessary.
    }
}
```

### addCard

addCard converts sensitive card data to a single-use token which you can safely pass to your server to charge the user. 

```java
Payment.addCard(mContext, uid, email, cardToSave, new TokenCallback() {

    public void onSuccess(Card card) {
        
        if(card != null){
            if(card.getStatus().equals("valid")){
                Alert.show(mContext,
                        "Card Successfully Added",
                        "status: " + card.getStatus() + "\n" +
                                "Card Token: " + card.getToken() + "\n" +
                                "transaction_reference: " + card.getTransactionReference());

            } else if (card.getStatus().equals("review")) {
                Alert.show(mContext,
                        "Card Under Review",
                        "status: " + card.getStatus() + "\n" +
                                "Card Token: " + card.getToken() + "\n" +
                                "transaction_reference: " + card.getTransactionReference());

            } else {
                Alert.show(mContext,
                        "Error",
                        "status: " + card.getStatus() + "\n" +
                                "message: " + card.getMessage());
            }


        }

        //TODO: Create charge or Save Token to your backend
    }

    public void onError(RedebanError error) {        
        Alert.show(mContext,
                "Error",
                "Type: " + error.getType() + "\n" +
                        "Help: " + error.getHelp() + "\n" +
                        "Description: " + error.getDescription());

        //TODO: Handle error
    }

});
```

The first argument to addCard is mContext (Context).
+ mContext. Context of the Current Activity

The second argument to addCard is uid (String).
+ uid Customer identifier. This is the identifier you use inside your application; you will receive it in notifications.

The third argument to addCard is email (String).
+ email Email of the customer

The fourth argument to addCard is a Card object. A Card contains the following fields:

+ number: card number as a string without any separators, e.g. '4242424242424242'.
+ holderName: cardholder name.
+ expMonth: integer representing the card's expiration month, e.g. 12.
+ expYear: integer representing the card's expiration year, e.g. 2013.
+ cvc: card security code as a string, e.g. '123'.
+ type: 

The fifth argument tokenCallback is a callback you provide to handle responses from Redeban.
It should send the token to your server for processing onSuccess, and notify the user onError.

Here's a sample implementation of the token callback:
```java
Payment.addCard(
    mContext, uid, email, cardToSave,
    new TokenCallback() {
        public void onSuccess(Card card) {
            // Send token to your own web service
            MyServer.chargeToken(card.getToken());
        }
        public void onError(RedebanError error) {
            Toast.makeText(getContext(),
                error.getDescription(),
                Toast.LENGTH_LONG).show();
        }
    }
);
```

`addCard` is an asynchronous call – it returns immediately and invokes the callback on the UI thread when it receives a response from Redeban's servers.

### getSessionId

The Session ID is a parameter Redeban use for fraud purposes. 
Call this method if you want to Collect your user's Device Information.

```java
String session_id = Payment.getSessionId(mContext);
```

Once you have the Session ID, you can pass it to your server to charge the user.

### Client-side validation helpers

The Card object allows you to validate user input before you send the information to Redeban.

#### validateNumber

Checks that the number is formatted correctly and passes the [Luhn check](http://en.wikipedia.org/wiki/Luhn_algorithm).

#### validateExpiryDate

Checks whether or not the expiration date represents an actual month in the future.

#### validateCVC

Checks whether or not the supplied number could be a valid verification code.

#### validateCard

Convenience method to validate card number, expiry date and CVC.

### Getting started with the Android example app

Note: the app require an [Android SDK](https://developer.android.com/studio/index.html) and [Gradle](https://gradle.org/) to build and run.


### Building and Running the RedebanStore

Before you can run the RedebanStore application, you need to provide it with your Redeban Credentials and a Sample Backend.

1. If you don't have any Credentials yet, please ask your contact on Redeban Team for it.
2. Head to https://github.com/redeban/example-java-backend and click "Deploy to Heroku" (you may have to sign up for a Heroku account as part of this process). Provide your Redeban Server Credentials `REDEBAN_SERVER_APP_CODE` and  `REDEBAN_SERVER_APP_KEY` fields under 'Env'. Click "Deploy for Free".
3. Open the project on Android Studio.
4. Replace the `REDEBAN_CLIENT_APP_CODE` and `REDEBAN_CLIENT_APP_KEY` constants in Constants.java with your own Redeban Client Credentials.
5. Replace the `BACKEND_URL` variable in the Constants.java file with the app URL Heroku provides you with (e.g. "https://my-example-app.herokuapp.com")
6. Run the Project.

Important Note: if you only have one APP_CODE, please asume that it's your `REDEBAN_SERVER_APP_CODE`. So you need to ask your contact on Redeban Team for your `REDEBAN_CLIENT_APP_CODE`.
