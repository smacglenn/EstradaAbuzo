package com.example.androiddrinkzlyboozeclient.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

    @Dao
    public interface CartDAO {
    @Query("SELECT * FROM Cart WHERE uid=:uid AND branchId=:branchId")
    Flowable<List<CartItem>> getAllCart(String uid,String branchId);

    @Query("SELECT SUM(foodQuantity) from Cart WHERE uid=:uid AND branchId=:branchId")
    Single<Integer> countItemInCart(String uid,String branchId);

    @Query("SELECT SUM(foodPrice*foodQuantity) FROM Cart WHERE uid=:uid AND branchId=:branchId")
    Single<Double> sumPriceInCart(String uid,String branchId);

    @Query("SELECT * FROM Cart WHERE foodId=:foodId AND uid=:uid AND branchId=:branchId")
    Single<CartItem> getItemCart(String foodId, String uid,String branchId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertOrReplaceAll(CartItem... cartItems);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    Single<Integer> updateCartItems(CartItem cartItem);

    @Delete
    Single<Integer> deleteCartItems(CartItem cartItem);

    @Query("DELETE FROM Cart WHERE uid=:uid AND branchId=:branchId")
    Single<Integer> cleanCart(String uid,String branchId);

    @Query("SELECT * FROM Cart WHERE categoryId=:categoryId AND foodId=:foodId AND uid=:uid AND branchId=:branchId")
    Single<CartItem> getItemWithAllOptionsInCart(String uid,String categoryId, String foodId,String branchId);

}
