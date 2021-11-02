package com.example.drinkzlyboozeserver.ui.category;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import callback.ICategoryCallbackListener;
import common.Common;
import model.CategoryModel;

public class CategoryViewModel extends ViewModel implements ICategoryCallbackListener {

    private MutableLiveData<List<CategoryModel>> categoryListMutable;
    private MutableLiveData<String> messageError = new MutableLiveData<>();
    private ICategoryCallbackListener categoryCallbackListener;


    public CategoryViewModel() {
        categoryCallbackListener = this;

    }

    public MutableLiveData<List<CategoryModel>> getCategoryListMutable(){
        if(categoryListMutable == null){
            categoryListMutable = new MutableLiveData<>();
            loadCategories();
        }
        return categoryListMutable;
    }

    public void loadCategories() {
        List<CategoryModel> tempList = new ArrayList<>();
        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference(Common.BRANCH_REF)
                .child(Common.currentServerUser.getBranch())
                .child(Common.CATEGORY_REF);
        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                        CategoryModel categoryModel = itemSnapshot.getValue(CategoryModel.class);
                        categoryModel.setMenu_id(itemSnapshot.getKey());
                        tempList.add(categoryModel);
                    }
                    if(tempList.size()>0)
                    categoryCallbackListener.onCategoryLoadSuccess(tempList);
                    else
                        categoryCallbackListener.onCategoryLoadFailed("Category Empty!");
                }
                else
                    categoryCallbackListener.onCategoryLoadFailed("Category not exists!");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                categoryCallbackListener.onCategoryLoadFailed(databaseError.getMessage());
            }
        });
    }

    public MutableLiveData<String> getMessageError(){
        return messageError;
    }


    @Override
    public void onCategoryLoadSuccess(List<CategoryModel> categoryModelList) {
        categoryListMutable.setValue(categoryModelList);
    }

    @Override
    public void onCategoryLoadFailed(String message) {
            messageError.setValue(message);
    }
}