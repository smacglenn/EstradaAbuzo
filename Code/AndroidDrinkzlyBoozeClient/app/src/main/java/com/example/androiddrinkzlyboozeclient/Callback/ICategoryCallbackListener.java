package com.example.androiddrinkzlyboozeclient.Callback;

import com.example.androiddrinkzlyboozeclient.Model.BestDealModel;
import com.example.androiddrinkzlyboozeclient.Model.CategoryModel;

import java.util.List;

    public interface ICategoryCallbackListener {
        void onCategoryLoadSuccess(List<CategoryModel> categoryModelList);
         void onCategoryLoadFailed(String message);

}
