package com.example.androiddrinkzlyboozeclient.Database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;



@Entity(tableName = "Cart", primaryKeys = {"uid","categoryId","foodId","branchId"})
public class CartItem {

    @NonNull
    @ColumnInfo(name = "branchId")
    private String branchId;

    @NonNull
    @ColumnInfo(name = "categoryId")
    private String cateoryId;

     @NonNull
    @ColumnInfo(name = "foodId")
    private String foodId;

    @ColumnInfo(name = "foodName")
    private String foodName;

    @ColumnInfo(name = "foodImage")
    private String foodImage;

    @ColumnInfo(name = "foodPrice")
    private Double foodPrice;

    @ColumnInfo(name = "foodQuantity")
    private int foodQuantity;

    @ColumnInfo(name = "userPhone")
    private String userPhone;

    @NonNull
    @ColumnInfo(name = "uid")
    private String uid;

    @NonNull
    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(@NonNull String branchId) {
        this.branchId = branchId;
    }

    @NonNull
    public String getCateoryId() {
        return cateoryId;
    }

    public void setCateoryId(@NonNull String cateoryId) {
        this.cateoryId = cateoryId;
    }

    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(@NonNull String foodId) {
        this.foodId = foodId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getFoodImage() {
        return foodImage;
    }

    public void setFoodImage(String foodImage) {
        this.foodImage = foodImage;
    }

    public Double getFoodPrice() {
        return foodPrice;
    }

    public void setFoodPrice(Double foodPrice) {
        this.foodPrice = foodPrice;
    }

    public int getFoodQuantity() {
        return foodQuantity;
    }

    public void setFoodQuantity(int foodQuantity) {
        this.foodQuantity = foodQuantity;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj == this)
            return true;
        if(!(obj instanceof CartItem))
            return false;
        CartItem cartItem = (CartItem) obj;
        return cartItem.getFoodId().equals(this.foodId);
    }
}
