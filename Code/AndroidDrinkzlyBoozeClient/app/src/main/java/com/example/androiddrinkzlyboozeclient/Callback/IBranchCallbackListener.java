package com.example.androiddrinkzlyboozeclient.Callback;

import com.example.androiddrinkzlyboozeclient.Model.BranchModel;

import java.util.List;

public interface IBranchCallbackListener {
    void onBranchLoadSuccess(List<BranchModel> branchModelList);
    void onBranchLoadFailed(String message);
}
