package com.example.androiddrinkzlyboozeclient.Database;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public interface CartDataSource {

     Flowable<List<CartItem>> getAllCart(String uid,String branchId);

     Single<Integer> countItemInCart(String uid,String branchId);

     Single<Double> sumPriceInCart(String uid,String branchId);

     Single<CartItem> getItemCart(String foodId, String uid,String branchId);

     Completable insertOrReplaceAll(CartItem... cartItems);

     Single<Integer> updateCartItems(CartItem cartItem);

     Single<Integer> deleteCartItems(CartItem cartItem);

     Single<Integer> cleanCart(String uid,String branchId);

     Single<CartItem> getItemWithAllOptionsInCart(String uid,String categoryId, String foodId,String branchId);

}


