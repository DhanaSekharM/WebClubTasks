package com.example.chatbot;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.TokenResponse;

public class AuthenticationActivity extends AppCompatActivity {

    private static final String USED_INTENT = "USED_INTENT";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AuthorizationServiceConfiguration serviceConfiguration = new AuthorizationServiceConfiguration(
                Uri.parse("https://accounts.google.com/o/oauth2/auth") /* auth endpoint */,
                Uri.parse("https://oauth2.googleapis.com/token") /* token endpoint */
        );

        String clientId = "103687242489070706361";
        Uri redirectUri = Uri.parse("com.example.chatbot:/oauth2callback");   //
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(
                serviceConfiguration,
                clientId,
                ResponseTypeValues.CODE,  //
                redirectUri
        );
        builder.setScopes("https://www.googleapis.com/auth/dialogflow");
        AuthorizationRequest request = builder.build();

        Log.d("AUTH", "Not");

//        AuthorizationService authorizationService = new AuthorizationService(this);
//        String action = "com.example.chatbot.HANDLE_AUTHORIZATION_RESPONSE";
//        Intent postAuthorizationIntent = new Intent(action);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, request.hashCode(), postAuthorizationIntent, 0);
//        authorizationService.performAuthorizationRequest(request, pendingIntent);

        AuthorizationService authService = new AuthorizationService(this);
        Intent authIntent = authService.getAuthorizationRequestIntent(request);
        startActivityForResult(authIntent, 100);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("AUTH", "Intent");
        checkIntent(intent);
    }

    private void checkIntent(@Nullable Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            switch (action) {
                case "com.example.chatbot.HANDLE_AUTHORIZATION_RESPONSE":
                    if (!intent.hasExtra(USED_INTENT)) {
                        handleAuthorizationResponse(intent);
                        intent.putExtra(USED_INTENT, true);
                    }
                    break;
                default:
                    // do nothing
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            handleAuthorizationResponse(data);
            // ... process the response or exception ...

        } else {
            // ...
        }
    }

    private void handleAuthorizationResponse(Intent intent) {
        AuthorizationResponse response = AuthorizationResponse.fromIntent(intent);
        AuthorizationException error = AuthorizationException.fromIntent(intent);
        final AuthState authState = new AuthState(response, error);



        if (response != null) {
            Log.i("AUTH", String.format("Handled Authorization Response %s ", authState.toString()));
            AuthorizationService service = new AuthorizationService(this);
            service.performTokenRequest(response.createTokenExchangeRequest(), new AuthorizationService.TokenResponseCallback() {
                @Override
                public void onTokenRequestCompleted(@Nullable TokenResponse tokenResponse, @Nullable AuthorizationException exception) {
                    if (exception != null) {
                        Log.w("AUTH", "Token Exchange failed", exception);
                    } else {
                        if (tokenResponse != null) {
                            authState.update(tokenResponse, exception);
//                            persistAuthState(authState);
                            Log.i("AUTH", String.format("Token Response [ Access Token: %s, ID Token: %s ]", tokenResponse.accessToken, tokenResponse.idToken));
                        }
                    }
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkIntent(getIntent());
    }
}
