package com.example.drinkzlyboozeshipper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import adapter.MyBranchAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import common.Common;
import io.paperdb.Paper;
import model.BranchModel;
import model.ShipperUserModel;
import model.eventbus.BranchSelectEvent;
import model.eventbus.IBranchCallbackListener;

public class BranchListActivity extends AppCompatActivity implements IBranchCallbackListener {

    @BindView(R.id.recycler_branch)
    RecyclerView recycler_branch;
    AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;
    MyBranchAdapter adapter;

    DatabaseReference serverRef;
    IBranchCallbackListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_branch_list);

        initViews();
        loadAllBranch();
    }

    private void loadAllBranch() {
        dialog.show();
        List<BranchModel> branchModels = new ArrayList<>();
        DatabaseReference branchRef = FirebaseDatabase.getInstance()
                .getReference(Common.BRANCH_REF);
        branchRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    for(DataSnapshot branchSnapshot:snapshot.getChildren())
                    {
                        BranchModel branchModel = branchSnapshot.getValue(BranchModel.class);
                        branchModel.setUid(branchSnapshot.getKey());
                        branchModels.add(branchModel);
                    }
                    if(branchModels.size()>0)
                        listener.onBranchLoadSuccess(branchModels);
                    else
                        listener.onBranchLoadFailed("Branch List Empty");
                }else
                    listener.onBranchLoadFailed("Branch List not found");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onBranchLoadFailed(error.getMessage());
            }
        });
    }

    private void initViews() {
        ButterKnife.bind(this);
        listener = this;

        dialog = new AlertDialog.Builder(this).setCancelable(false).setMessage("Please wait...")
                .create();
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(this,R.anim.layout_item_from_left);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recycler_branch.setLayoutManager(layoutManager);
        recycler_branch.addItemDecoration(new DividerItemDecoration(this,layoutManager.getOrientation()));
    }

    @Override
    public void onBranchLoadSuccess(List<BranchModel> branchModelList) {
        dialog.dismiss();
        adapter = new MyBranchAdapter(this,branchModelList);
        recycler_branch.setAdapter(adapter);
        recycler_branch.setLayoutAnimation(layoutAnimationController);
    }

    @Override
    public void onBranchLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onBranchSelectEvent(BranchSelectEvent event)
    {
        if(event!=null)
        {

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user!=null)
            {
                checkServerUserFromFirebase(user,event.getBranchModel());
            }
        }
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
                                Toast.makeText(BranchListActivity.this, "You must be allowed from Server app", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                        {
                            dialog.dismiss();
                            showRegisterDialog(user,branchModel.getUid());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void showRegisterDialog(FirebaseUser user, String uid) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Register");
        builder.setMessage("Please fill information \n Admin will accept your account late");

        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register, null);
        TextInputLayout phone_input_layout = (TextInputLayout)itemView.findViewById(R.id.phone_input_layout);
        EditText edt_name = (EditText) itemView.findViewById(R.id.edt_name);
        EditText edt_phone = (EditText) itemView.findViewById(R.id.edt_phone);

        //set data
        if(user.getPhoneNumber() == null || TextUtils.isEmpty(user.getPhoneNumber()))
        {
            phone_input_layout.setHint("Email");
            edt_phone.setText(user.getEmail());
            edt_name.setText(user.getDisplayName());
        }
        else
            edt_phone.setText(user.getPhoneNumber());


        builder.setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("REGISTER", (dialogInterface, which) -> {
                    if (TextUtils.isEmpty(edt_name.getText().toString())) {
                        Toast.makeText(BranchListActivity.this, "Please enter your name", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ShipperUserModel shipperUserModel = new ShipperUserModel();
                    shipperUserModel.setUid(user.getUid());
                    shipperUserModel.setName(edt_name.getText().toString());
                    shipperUserModel.setPhone(edt_phone.getText().toString());
                    shipperUserModel.setActive(false);

                    dialog.show();

                    serverRef = FirebaseDatabase.getInstance().getReference(Common.BRANCH_REF)
                            .child(uid)
                            .child(Common.SHIPPER_REF);
                    serverRef.child(shipperUserModel.getUid())
                            .setValue(shipperUserModel)
                            .addOnFailureListener(e -> {
                                dialog.dismiss();
                                Toast.makeText(BranchListActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }).addOnCompleteListener(task -> {
                        dialog.dismiss();
                        Toast.makeText(BranchListActivity.this, "Registration Success! Admin will check and activate you soon", Toast.LENGTH_SHORT).show();

                    });
                });

        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog registerDialog = builder.create();
        registerDialog.show();
    }

    private void gotoHomeActivity(ShipperUserModel userModel,BranchModel branchModel) {
        dialog.dismiss();

        String jsonEncode = new Gson().toJson(branchModel);
        Paper.init(this);
        Paper.book().write(Common.BRANCH_SAVE,jsonEncode);

        Common.currentShipperUser = userModel;
        startActivity(new Intent(this,HomeActivity.class));
        finish();
    }
}