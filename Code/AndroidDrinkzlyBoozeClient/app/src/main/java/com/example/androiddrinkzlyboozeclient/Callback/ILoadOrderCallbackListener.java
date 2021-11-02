package com.example.androiddrinkzlyboozeclient.Callback;

import com.example.androiddrinkzlyboozeclient.Model.OrderModel;

import java.util.List;

public interface ILoadOrderCallbackListener {
    void onLoadOrderSuccess(List<OrderModel> orderList);
    void onLoadOrderFailed(String message);
}
