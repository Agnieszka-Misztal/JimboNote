package com.romciasoft.jimbonote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    //guziki do logowania
    Button button;
    Button fingerButton;

    //pole do wpisania hasła
    EditText passwordText;

    //klasy potrzebne do obsługi logowania za pomocą odcisku palca
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //sprawdzamy czy mamy już klucz w KeyStore - jeżeli nie to generujemy go automatycznie
        SecretKeyUtils.CheckAndGeneratePassword();

        //sprawdzamy czy jest ustawione haslo do logowania, jak nie odsylamy do ustawienia nowego hasla
        //sprawdzam czy mam zapisane hasło w shared preferences, na początku inicjuję obiekt do pobierania danych
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("jimboNote", Context.MODE_PRIVATE);

        //prubuje pobrać string z shared preferences z hasłem do logowania
        String pass = preferences.getString("pass", "");

        if(pass==""){

            //nie ma zapisanego hasła więc przechodzę do activity gdzie mogę wygenerwować nowe hasło
            Intent passIntent = new Intent(MainActivity.this, PassActivity.class);
            startActivity(passIntent);
        }

        setContentView(R.layout.activity_main);

        //pobieranie kontrolek z widoku
        button = findViewById(R.id.button);
        passwordText = findViewById(R.id.editTextTextPassword);

        //guzik do logowania przez odcisk - domyślnie go wyłączę bo nie wiadomo czy telefon obsługuję odciski
        fingerButton=findViewById(R.id.loginfingerbutton);
        fingerButton.setEnabled(false);

        //przypisanie akcji do guzika logowania na hasło
        button.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {

                 //pobranie obecnie wpisanego hasła
                 String password = passwordText.getText().toString();

                  //użycie na nim funkcji hashującej - dodaję do niego dodatkowo sal w postaci tekstu: romeczka
                  String hashPass = EncryptionHelper.getSecureHash(password, "romeczka");

                  //pobieram z shared preferences zapisane oraz zahashowane hasło
                  SharedPreferences preferences = getApplicationContext().getSharedPreferences("jimboNote", Context.MODE_PRIVATE);
                  String pass = preferences.getString("pass", "");

                  //porównuję czy wpisane hasło zgadza się z zapisanym
                  if(hashPass.equals(pass)){

                      //przelaczenie na inny widok/activity
                      Intent memoIntent = new Intent(MainActivity.this, MemoActivity.class);
                      startActivity(memoIntent);
                      finish();
                }else {
                      //pokazanie informacji że hasło jest błędne
                      Toast toast = Toast.makeText(getApplicationContext(), "Incorrect password", Toast.LENGTH_LONG);
                      toast.show();
                  }
              }
          });


        //sprawdzenie czy telefon obsługuje biomertrykę
        //pobieramy informację o biometryce
        BiometricManager biometricManager = BiometricManager.from(this);

        //sprawdzam czy jest dostępna
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                //jeżeli jest dostępna to aktywuję przycisk logowania przez odcisk
                fingerButton.setEnabled(true);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.e("MY_APP_TAG", "No biometric features available on this device.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Log.e("MY_APP_TAG", "The user hasn't associated any biometric credentials with their account.");
                break;
        }

        //przygotowanie obsługi logowania przez odcisk palca
        executor = ContextCompat.getMainExecutor(this);

        //przygotowanie callbacka w którym obsłużymy pomyślne logowanie przez odcisk jak i błędy lub niepowodzenie
        biometricPrompt = new BiometricPrompt(MainActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {

            @Override
            public void onAuthenticationError(int errorCode,@NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),"Authentication error: " + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                //udało nam się przejść autoryzację pomyślnie
                //przechodzę do widoku notatki
                Intent intent = new Intent(MainActivity.this, MemoActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed",Toast.LENGTH_SHORT) .show();
            }
        });

        //przygotowanie widoku dla okna informacji o odcisku palca
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Use password")
                .build();

        //ustawienie akcji wywołującej okno logowania przez odcisk po naciśnięciu guzika
        fingerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                biometricPrompt.authenticate(promptInfo);
            }
        });
    }

    //chowanie klawiatury jak sie kliknie poza obszar edytowania tekstu
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }
}