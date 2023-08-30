package com.codewithsam.masalaaccounts;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codewithsam.masalaaccounts.objects.Data;
import com.codewithsam.masalaaccounts.utils.DataViewAdapter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements DataViewAdapter.DataViewClickInterface, DataViewAdapter.DataViewLongClickInterface {
    private RecyclerView rv;
    private DataViewAdapter adapter;
    private GoogleSignInAccount account;
    private RelativeLayout bottomSheet;
    private ArrayList<Data> viewData;
    private FloatingActionButton fab;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseRef;
    private TextView txt_name, txt_amount, txt_noData;
    private float res;
    private ProgressBar pb_loading;

    private boolean update_val;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fab = findViewById(R.id.btn_fab);
        fab.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddDataActivity.class)));

        pb_loading = findViewById(R.id.pb_loading);
        txt_noData = findViewById(R.id.txt_no_data);
        txt_noData.setVisibility(View.GONE);
        FirebaseApp.initializeApp(/*context=*/ this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance());

        account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null) {
            startActivity(new Intent(MainActivity.this, SplashActivity.class));
        }

        txt_name = findViewById(R.id.txt_username);
        txt_amount = findViewById(R.id.txt_current_balance);

        txt_name.setText(account.getDisplayName());
        txt_amount.setText(String.valueOf(0.0f));
        rv = findViewById(R.id.rv_accounts);
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseRef = mDatabase.getReference();
        viewData = new ArrayList<>();
        bottomSheet = findViewById(R.id.rlb_sheet);
        adapter = new DataViewAdapter(viewData, this, this, this);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(llm);
        rv.setAdapter(adapter);
        getData();

        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(()-> {
                    if(viewData.size() == 0){
                        pb_loading.setVisibility(View.GONE);
                        txt_noData.setVisibility(View.VISIBLE);
                    }
                });
            }
        }, 10 * 1000);

    }

    private void getData() {
        pb_loading.setVisibility(View.VISIBLE);
        viewData.clear();
        mDatabaseRef.child(account.getId()).child("Accounts").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                viewData.add(snapshot.getValue(Data.class));
                adapter.notifyDataSetChanged();
                res += snapshot.getValue(Data.class).getAmount();
                txt_amount.setText(String.valueOf(res));
                pb_loading.setVisibility(View.GONE);
                txt_noData.setVisibility(View.GONE);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                viewData.remove(snapshot.getValue(Data.class));
                adapter.notifyDataSetChanged();
                startActivity(new Intent(MainActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onDataClick(int position) {
        displayBottomSheet(viewData.get(position));
    }

    private void displayBottomSheet(Data data) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View layout = LayoutInflater.from(this).inflate(R.layout.bsd_update_data, bottomSheet);
        bottomSheetDialog.setContentView(layout);
        bottomSheetDialog.show();

        TextView accountName, currentBalance;
        EditText newBalance;
        Button update, cancel;
        accountName = layout.findViewById(R.id.txt_account_name);
        currentBalance = layout.findViewById(R.id.edt_current_balance);
        newBalance = layout.findViewById(R.id.edt_new_balance);
        update = layout.findViewById(R.id.btn_update);
        cancel = layout.findViewById(R.id.btn_cancel);
        cancel.setOnClickListener(v -> bottomSheetDialog.dismiss());
        CheckBox chk_update = layout.findViewById(R.id.chk_update);
        update_val = false;
        accountName.setText("Account Name: " + data.getName());
        currentBalance.setText(String.valueOf(data.getAmount()));
        TextView up = layout.findViewById(R.id.txt_update);
        up.setText("Last Updated on: " + data.date);
        chk_update.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                update_val = b;
                TextView newBal = layout.findViewById(R.id.txt_new_balance);
                if(update_val){
                    newBal.setText("Update Balance: ");
                }else{
                    newBal.setText("New Balance: ");
                }
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String stm = newBalance.getText().toString();
                Expression exp = new ExpressionBuilder(stm).build();
                Data d = new Data();
                d.setName(data.name);
                d.setDate(DateFormat.getDateInstance().format(new Date()));
                if(update_val){
                    d.setAmount(data.getAmount() + Float.parseFloat(Double.toString(exp.evaluate())));
                }else{
                    d.setAmount(Float.parseFloat(Double.toString(exp.evaluate())));
                }
                mDatabaseRef = mDatabaseRef.child(account.getId()).child("Accounts");
                mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        mDatabaseRef.child(d.name).setValue(d);
                        startActivity(new Intent(MainActivity.this, MainActivity.class));
                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

    }

    @Override
    public boolean onDataLongClick(int position) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("Are you sure you want to Delete " + viewData.get(position).getName() + " 's Account?");
        dialog.setTitle("Delete Account");
        dialog.setPositiveButton("Yes", (dialogInterface, i) -> {
            mDatabaseRef = mDatabase.getReference(account.getId()).child("Accounts").child(viewData.get(position).getName());
            mDatabaseRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(MainActivity.this, "Successfully Removed Account", Toast.LENGTH_SHORT).show();
                }
            });
        });
        dialog.setNegativeButton("No", (dialogInterface, i) -> {
            dialogInterface.dismiss();
        });
        AlertDialog db = dialog.create();
        db.show();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inf = getMenuInflater();
        inf.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.logout: {
                Toast.makeText(this, "User Logged Out Successfully!", Toast.LENGTH_SHORT).show();
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();
                GoogleSignInClient client = GoogleSignIn.getClient(MainActivity.this, gso);
                client.signOut();
                Intent i = new Intent(MainActivity.this, SplashActivity.class);
                startActivity(i);
                this.finish();
                return true;
            }
            default:{
                return super.onOptionsItemSelected(item);
            }
        }
    }
}