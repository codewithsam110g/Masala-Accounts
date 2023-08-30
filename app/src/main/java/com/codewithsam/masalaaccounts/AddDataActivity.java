package com.codewithsam.masalaaccounts;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.codewithsam.masalaaccounts.objects.Data;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AddDataActivity extends AppCompatActivity {

    EditText name, amount;
    Button add, cancel;
    FirebaseDatabase mDatabase;
    DatabaseReference mDatabaseRef;
    GoogleSignInAccount account;
    ProgressBar pb_loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_data);

        name = findViewById(R.id.edt_name);
        amount = findViewById(R.id.edt_amount);
        add = findViewById(R.id.btn_add_account);
        cancel = findViewById(R.id.btn_cancel);
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseRef = mDatabase.getReference();

        pb_loading = findViewById(R.id.pb_loading);

        account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null) {
            startActivity(new Intent(AddDataActivity.this, SplashActivity.class));
        }
        cancel.setOnClickListener(v -> startActivity(new Intent(AddDataActivity.this, MainActivity.class)));
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                pb_loading.setVisibility(View.VISIBLE);
                add.setEnabled(false);

                String s_name, s_amount;
                s_name = name.getText().toString();
                s_amount = amount.getText().toString();
                if (!TextUtils.isEmpty(s_name) || !TextUtils.isEmpty(s_amount)) {
                    Expression exp = new ExpressionBuilder(s_amount).build();
                    Date d = new Date();
                    String s = DateFormat.getDateInstance().format(d);
                    Data data = new Data(s_name, Float.parseFloat(Double.toString(exp.evaluate())),s);
                    mDatabaseRef = mDatabaseRef.child(account.getId()).child("Accounts");
                    mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            mDatabaseRef.child(data.name).setValue(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    pb_loading.setVisibility(View.GONE);
                                    add.setEnabled(true);
                                    startActivity(new Intent(AddDataActivity.this, MainActivity.class));
                                    finish();
                                }
                            });

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });
    }
}