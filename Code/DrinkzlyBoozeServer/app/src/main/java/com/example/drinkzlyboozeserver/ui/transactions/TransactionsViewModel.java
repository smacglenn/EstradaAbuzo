package com.example.drinkzlyboozeserver.ui.transactions;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import callback.ITransactionsCallbackListener;
import common.Common;
import model.OrderTransactionModel;

public class TransactionsViewModel extends ViewModel implements ITransactionsCallbackListener {

     private MutableLiveData<List<OrderTransactionModel>> orderTransactionMutableLiveData;
    private MutableLiveData<String> messageError;

    private ITransactionsCallbackListener listener;

    public TransactionsViewModel() {
        orderTransactionMutableLiveData = new MutableLiveData<>();
        messageError = new MutableLiveData<>();
        listener = this;
    }

    public MutableLiveData<List<OrderTransactionModel>> getOrderTransactionMutableLiveData() {
        loadOrderByStatus(2);
        return orderTransactionMutableLiveData;
    }

    private void loadOrderByStatus(int status) {
        List<OrderTransactionModel> tempList = new ArrayList<>();
        Query orderRef = FirebaseDatabase.getInstance().getReference(Common.BRANCH_REF)
                .child(Common.currentServerUser.getBranch())
                .child(Common.ORDER_REF)
                .orderByChild("orderStatus")
                .equalTo(status);
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot itemSnapShot:snapshot.getChildren()) {
                    OrderTransactionModel orderTransactionModel = itemSnapShot.getValue(OrderTransactionModel.class);
                    orderTransactionModel.setKey(itemSnapShot.getKey());
                    tempList.add(orderTransactionModel);
                }
                listener.onTransactionsLoadSuccess(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onTransactionsLoadFailed(error.getMessage());
            }
        });
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onTransactionsLoadSuccess(List<OrderTransactionModel> orderTransactionModelList) {
        if(orderTransactionModelList.size()>0)
        {
            Collections.sort(orderTransactionModelList, (orderTransactionModel,t1)->{
                if(orderTransactionModel.getCreateDate()<t1.getCreateDate())
                    return -1;
                return orderTransactionModel.getCreateDate() == t1.getCreateDate() ? 0:1;
            });
        }
        orderTransactionMutableLiveData.setValue(orderTransactionModelList);
    }

    @Override
    public void onTransactionsLoadFailed(String message) {
            messageError.setValue(message);
    }
}