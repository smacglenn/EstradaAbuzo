package com.example.androiddrinkzlyboozeclient.Callback;

import java.util.List;

import com.example.androiddrinkzlyboozeclient.Model.BestDealModel;

public interface IBestDealCallbackListener {
    void onBestDealLoadSuccess(List<BestDealModel> bestDealModels);
    void onBestDealLoadFailed(String message);
}
