package com.example.androiddrinkzlyboozeclient;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.androiddrinkzlyboozeclient.Common.Common;
import com.example.androiddrinkzlyboozeclient.Model.TokenModel;
import com.example.androiddrinkzlyboozeclient.Model.UserModel;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;
import io.reactivex.disposables.CompositeDisposable;

public class MainActivity extends AppCompatActivity {

    private static int APP_REQUEST_CODE = 7171;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private DatabaseReference userRef;
    private List<AuthUI.IdpConfig> providers;
    private AuthUI authUI;
   // private ICloudFunctions cloudFunctions;
    private android.app.AlertDialog dialog;

    private Place placeSelected;
    private AutocompleteSupportFragment places_fragment;
    private PlacesClient placesClient;
    private List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG);





    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        if (listener != null)
            firebaseAuth.removeAuthStateListener(listener);
        compositeDisposable.clear();
        super.onStop();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        init();

    }


    private void init(){

        Places.initialize(this,getString(R.string.google_maps_key));
        placesClient = Places.createClient(this);
         providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build(), new AuthUI.IdpConfig.EmailBuilder().build());
        Locale.getDefault().getCountry();


        userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCER);
        firebaseAuth = FirebaseAuth.getInstance();
       dialog = new SpotsDialog.Builder().setCancelable(false).setContext(this).build();
       // cloudFunctions = RetrofitCloudClient.getInstance().create(ICloudFunctions.class);
        listener = firebaseAuth -> {

            Dexter.withActivity(this)
                    .withPermissions(
                                    Arrays.asList(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                            Manifest.permission.CAMERA)
                            )
                            .withListener(new MultiplePermissionsListener() {
                                @Override
                                public void onPermissionsChecked(MultiplePermissionsReport report) {
                                    if(report.areAllPermissionsGranted())
                                    {

                                        FirebaseUser user = firebaseAuth.getCurrentUser();
                                        if (user != null) {

                                            checkUserFromFireBase(user);

                                        } else {

                                            phoneLogin();

                                        }
                                    }
                                    else
                                        Toast.makeText(MainActivity.this, "You must accept all permission to use this application", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                                }
                            }).check();


            };
        }

    private void checkUserFromFireBase(FirebaseUser user) {
                 dialog.show();
                userRef.child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            UserModel userModel = dataSnapshot.getValue(UserModel.class);
                            TokenModel tokenModel = new TokenModel();
                            goToHomeActivity(userModel, tokenModel.getToken());

                        } else {
                            showRegisterDialog(user);
                        }

                        dialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        dialog.dismiss();
                    }
                });

    }

    private void showRegisterDialog(FirebaseUser user){
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Register");
        builder.setMessage("Please fill information");

        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register, null);
        TextInputLayout phone_input_layout = (TextInputLayout)itemView.findViewById(R.id.phone_input_layout);
        EditText edt_name = (EditText) itemView.findViewById(R.id.edt_name);
        TextView txt_address_detail = (TextView) itemView.findViewById(R.id.txt_address_detail1);
        EditText edt_phone = (EditText) itemView.findViewById(R.id.edt_phone);

        places_fragment = (AutocompleteSupportFragment)getSupportFragmentManager()
                .findFragmentById(R.id.places_autocomplete_fragment);
        places_fragment.setPlaceFields(placeFields);
        places_fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                placeSelected = place;
                txt_address_detail.setText(place.getAddress());
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(MainActivity.this, ""+status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        if(user.getPhoneNumber() == null || TextUtils.isEmpty(user.getPhoneNumber()))
        {
            phone_input_layout.setHint("Email");
            edt_phone.setText(user.getEmail());
            edt_name.setText(user.getDisplayName());
        }
        else
       edt_phone.setText(user.getPhoneNumber());

        builder.setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton("REGISTER", (DialogInterface dialog, int which) -> {

            if(placeSelected!=null) {
                if (TextUtils.isEmpty(edt_name.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Please enter your name", Toast.LENGTH_SHORT).show();
                    return;
                }


                UserModel userModel = new UserModel();
                TokenModel tokenModel = new TokenModel();
                userModel.setUid(user.getUid());
                userModel.setName(edt_name.getText().toString());
                userModel.setAddress(txt_address_detail.getText().toString());
                userModel.setPhone(edt_phone.getText().toString());
                userModel.setLat(placeSelected.getLatLng().latitude);
                userModel.setLng(placeSelected.getLatLng().longitude);


                userRef.child(user.getUid()).setValue(userModel)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                dialog.dismiss();
                                Toast.makeText(MainActivity.this, "Congratulations! Registration success!", Toast.LENGTH_SHORT).show();
                                goToHomeActivity(userModel, tokenModel.getToken());
                            }
                        });
            }
            else
            {
                Toast.makeText(this, "Please select address", Toast.LENGTH_SHORT).show();
                return;
            }

        });

        builder.setView(itemView);

        android.app.AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(dialog1 -> {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.remove(places_fragment);
            fragmentTransaction.commit();
        });
        dialog.show();

    }

    private void goToHomeActivity(UserModel userModel, String token) {
        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnFailureListener(e -> {
                    Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    Common.currentUser = userModel;
                    Common.currentToken = token;
                    startActivity(new Intent(getApplicationContext(),HomeActivity.class));
                    finish();
                }).addOnCompleteListener(task -> {

            Common.currentUser = userModel;
            Common.currentToken = token;
            Common.updateToken(MainActivity.this,task.getResult().getToken());
            startActivity(new Intent(getApplicationContext(),HomeActivity.class));
            finish();
                });

    }


    private void phoneLogin(){

        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.drink)
                .setTheme(R.style.LoginTheme)
                .build(), APP_REQUEST_CODE);
      //  startActivity(new Intent(getApplicationContext(),HomeActivity.class));
        }

     @Override
    public void onActivityResult(int requestCode,int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if (requestCode == APP_REQUEST_CODE) {
             IdpResponse response = IdpResponse.fromResultIntent(data);
             if (resultCode == RESULT_OK) {
                 FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
             } else {
                 Toast.makeText(this, "Failded to sign in!", Toast.LENGTH_SHORT).show();
             }
         }


     }




}