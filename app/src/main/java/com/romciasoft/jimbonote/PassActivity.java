package com.romciasoft.jimbonote;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PassActivity extends AppCompatActivity {

    EditText newPassword;
    EditText confirmPassword;
    Button savePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass);

        //pobranie kontrolek z widoku
        newPassword = findViewById(R.id.editTextTextPassword2);
        confirmPassword = findViewById(R.id.editTextTextPassword3);
        savePassword = findViewById(R.id.button4);

        //ustawienie akcji na naduszenie przycisku zapisu hasła
        savePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //pobranie obu haseł z kontrolek
                String pass = newPassword.getText().toString();
                String confirm = confirmPassword.getText().toString();

                //upewnienie się że się zgadzają oraz nie są puste
               if(pass.equals(confirm) && !pass.isEmpty() && !confirm.isEmpty()){

                   //pobranie obiektu shared preferences
                   SharedPreferences preferences = getApplicationContext().getSharedPreferences("jimboNote", Context.MODE_PRIVATE);
                   SharedPreferences.Editor editor = preferences.edit();

                   //hashowanie hasła
                   String hashPass = EncryptionHelper.getSecureHash(pass, "romeczka");

                   //zapis hasła do shared preferences
                   editor.putString("pass", hashPass);
                   editor.commit();

                   //zamknięcie okna
                   finish();
               }
               else {
                   Toast toast = Toast.makeText(getApplicationContext(),"Password doesn't match", Toast.LENGTH_LONG);
                   toast.show();
               }
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