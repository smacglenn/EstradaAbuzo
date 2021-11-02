package com.example.androiddrinkzlyboozeclient.Callback;

import java.util.List;

import com.example.androiddrinkzlyboozeclient.Model.PopularCategoryModel;

public interface IPopularCallbackListener {
    void onPopularLoadSuccess(List<PopularCategoryModel> popularCategoryModels);
    void onPopularLoadFailed(String message);
}
