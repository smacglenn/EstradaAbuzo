package com.example.androiddrinkzlyboozeclient.ui.branch;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.androiddrinkzlyboozeclient.Callback.IBranchCallbackListener;
import com.example.androiddrinkzlyboozeclient.Common.Common;
import com.example.androiddrinkzlyboozeclient.Model.BranchModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BranchViewModel extends ViewModel implements IBranchCallbackListener {
    private MutableLiveData<List<BranchModel>> branchListMutable;
    private MutableLiveData<String> messageError = new MutableLiveData<>();
    private IBranchCallbackListener listener;

    public BranchViewModel() {
        listener = this;
    }

    public MutableLiveData<List<BranchModel>> getBranchListMutable() {
        if(branchListMutable==null)
        {
            branchListMutable = new MutableLiveData<>();
            loadBranchFromFireBase();
        }
        return branchListMutable;
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    private void loadBranchFromFireBase() {
        List<BranchModel> branchModels = new ArrayList<>();
        DatabaseReference branchRef = FirebaseDatabase.getInstance()
                .getReference(Common.BRANCH_REF);
        branchRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    for(DataSnapshot branchSnapShot:snapshot.getChildren())
                    {
                        BranchModel branchModel = branchSnapShot.getValue(BranchModel.class);
                        branchModel.setUid(branchSnapShot.getKey());
                        branchModels.add(branchModel);
                    }
                    if(branchModels.size()>0)
                        listener.onBranchLoadSuccess(branchModels);
                    else
                        listener.onBranchLoadFailed("Branch List empty");
                }
                else
                    listener.onBranchLoadFailed("Branch List doesn't exists");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBranchLoadSuccess(List<BranchModel> branchModelList) {
        branchListMutable.setValue(branchModelList);
    }

    @Override
    public void onBranchLoadFailed(String message) {
        messageError.setValue(message);
    }
}