package com.example.drinkzlyboozeshipper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import common.Common;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import model.BranchModel;
import model.ShipperUserModel;

public class MainActivity extends AppCompatActivity {

    private static int APP_REQUEST_CODE = 1111;
    private FirebaseAuth firebaseAuth1;
    private FirebaseAuth.AuthStateListener listener;
    private AlertDialog dialog;
    private DatabaseReference serverRef;
    private List<AuthUI.IdpConfig> providers;

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth1.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        if (listener != null)
            firebaseAuth1.removeAuthStateListener(listener);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

    }/*
        Paper.init(this);
        Paper.book().delete(Common.TRIP_START);
        Paper.book().delete(Common.SHIPPING_ORDER_DATA);*/

    private void init() {
        providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build(), new AuthUI.IdpConfig.EmailBuilder().build());
        Locale.getDefault().getCountry();

      //  serverRef = FirebaseDatabase.getInstance().getReference(Common.SHIPPER_REF);
        firebaseAuth1 = FirebaseAuth.getInstance();
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        listener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth1.getCurrentUser();
            if (user != null) {
                    Paper.init(this);
                    String jsonEncode = Paper.book().read(Common.BRANCH_SAVE);
                BranchModel branchModel = new Gson().fromJson(jsonEncode,
                        new TypeToken<BranchModel>(){}.getType());
                if(branchModel!=null)
                    checkServerUserFromFirebase(user,branchModel);
                else
                {
                    startActivity(new Intent(MainActivity.this,BranchListActivity.class));
                    finish();
                }
            } else {
                phoneLogin();
            }
        };
    }

    private void checkServerUserFromFirebase(FirebaseUser user, BranchModel branchModel) {
        dialog.show();

        serverRef = FirebaseDatabase.getInstance().getReference(Common.BRANCH_REF)
                .child(branchModel.getUid())
                .child(Common.SHIPPER_REF);
        serverRef.child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            ShipperUserModel userModel = snapshot.getValue(ShipperUserModel.class);
                            if(userModel.isActive())
                            {
                                gotoHomeActivity(userModel,branchModel);
                            }
                            else
                            {
                                dialog.dismiss();
                                Toast.makeText(MainActivity.this, "You must be allowed from Server app", Toast.LENGTH_SHORT).show();
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void gotoHomeActivity(ShipperUserModel userModel,BranchModel branchModel) {
        dialog.dismiss();
        Common.currentBranch = branchModel;
        Common.currentShipperUser = userModel;
        startActivity(new Intent(this,HomeActivity.class));
        finish();
    }


    private void phoneLogin() {
        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.LoginTheme)
                .setLogo(R.drawable.drink)
                .build(), APP_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_REQUEST_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            } else {
                Toast.makeText(this, "Failed to Sign in", Toast.LENGTH_SHORT).show();
            }
        }
    }

}