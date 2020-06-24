package com.romciasoft.jimbonote;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MemoActivity extends AppCompatActivity {

    Button buttonSave;
    EditText memoText;
    Button changePasswordButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo);

        // pobieranie kontrolek z widoku
        buttonSave = findViewById(R.id.button2);
        memoText = findViewById(R.id.editTextTextMultiLine);
        changePasswordButton = findViewById(R.id.button3);

        // pobranie z shared references naszej notatki jeżeli była zapisana wcześniej
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("jimboNote", Context.MODE_PRIVATE);

        // pobranie memo wg klucza tj. messege
        String memo = preferences.getString("message", "");

        if(memo != ""){

            //jeżeli mamy jakąś wiadomość to staramy się ją zdeszyfrować
            String decryptedMemo = SecretKeyUtils.decryptMessage(memo,getApplicationContext());
            //ustawienie widoku zapisanej wiadomosci
            memoText.setText(decryptedMemo);
        }

        //ustawienie akcji na naduszenie guzika zmiany hasła
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //przekierowane na podstrone pass
                Intent passIntent = new Intent(MemoActivity.this, PassActivity.class);
                //przełączenie na widok zmiany hasła
                startActivity(passIntent);
            }
        });

        //ustawienie akcji na naduszenie przycisku zapisu notakti
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //pobranie tekstu z kontrolki
                String memo = memoText.getText().toString();

                //pobranie obiektu shared preferences
                SharedPreferences preferences = getApplicationContext().getSharedPreferences("jimboNote", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();

                //zaszyfrowanie naszej wiadomości
                String encryptedMemo =SecretKeyUtils.encryptMessage(memo,getApplicationContext());

                //zapis memo do pamieci
                editor.putString("message", encryptedMemo);
                editor.commit();

                //pokaż wiadomość o zapisaniu notatki
                Toast toast = Toast.makeText(getApplicationContext(),"Note saved", Toast.LENGTH_SHORT);
                toast.show();
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