package com.example.drinkzlyboozeserver.ui.discount;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import callback.IDiscountCallbackListener;
import common.Common;
import model.DiscountModel;

public class DiscountViewModel extends ViewModel implements IDiscountCallbackListener {

    private MutableLiveData<String> messageError = new MutableLiveData<>();
    private MutableLiveData<List<DiscountModel>> discountMutableLiveData;
    private IDiscountCallbackListener discountCallbackListener;

    public DiscountViewModel() {
        discountCallbackListener = this;
    }

    public MutableLiveData<List<DiscountModel>> getDiscountMutableLiveData() {
        if(discountMutableLiveData ==null) discountMutableLiveData=new MutableLiveData<>();
        loadDiscount();
        return discountMutableLiveData;
    }

    public void loadDiscount() {
        List<DiscountModel> temp = new ArrayList<>();
        DatabaseReference discountRef = FirebaseDatabase.getInstance()
                .getReference(Common.BRANCH_REF)
                .child(Common.currentServerUser.getBranch())
                .child(Common.DISCOUNT);
        discountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getChildren().iterator().hasNext())
                {
                    for(DataSnapshot discountSnapshot:snapshot.getChildren())
                    {
                        DiscountModel discountModel = discountSnapshot.getValue(DiscountModel.class);
                        discountModel.setKey(discountSnapshot.getKey());
                        temp.add(discountModel);
                    }
                    discountCallbackListener.onListDiscountLoadSuccess(temp);
                }
                else
                    discountCallbackListener.onListDiscountLoadFailed("Empty Data");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                discountCallbackListener.onListDiscountLoadFailed(error.getMessage());
            }
        });
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onListDiscountLoadSuccess(List<DiscountModel> discountModelList) {
        discountMutableLiveData.setValue(discountModelList);
    }

    @Override
    public void onListDiscountLoadFailed(String message) {
        if(message.equals("Empty Data"))
            discountMutableLiveData.setValue(null);
        messageError.setValue(message);
    }
}