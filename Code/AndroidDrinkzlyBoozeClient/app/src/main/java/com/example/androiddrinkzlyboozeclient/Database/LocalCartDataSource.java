package com.example.androiddrinkzlyboozeclient.Database;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public class LocalCartDataSource implements CartDataSource {

    private CartDAO cartDAO;

    public LocalCartDataSource(CartDAO cartDAO) {
        this.cartDAO = cartDAO;
    }

    @Override
    public Flowable<List<CartItem>> getAllCart(String uid,String branchId) {
        return cartDAO.getAllCart(uid,branchId);
    }

    @Override
    public Single<Integer> countItemInCart(String uid,String branchId) {
        return cartDAO.countItemInCart(uid,branchId);
    }


    @Override
    public Single<Double> sumPriceInCart(String uid,String branchId) {
        return cartDAO.sumPriceInCart(uid,branchId);
    }

    @Override
    public Single<CartItem> getItemCart(String foodId, String uid,String branchId) {
        return cartDAO.getItemCart(foodId, uid,branchId);
    }

    @Override
    public Completable insertOrReplaceAll(CartItem... cartItems) {
        return cartDAO.insertOrReplaceAll(cartItems);
    }

    @Override
    public Single<Integer> updateCartItems(CartItem cartItem) {
        return cartDAO.updateCartItems(cartItem);
    }

    @Override
    public Single<Integer> deleteCartItems(CartItem cartItem) {
        return cartDAO.deleteCartItems(cartItem);
    }

    @Override
    public Single<Integer> cleanCart(String uid,String branchId){
        return cartDAO.cleanCart(uid,branchId);
    }

    @Override
    public Single<CartItem> getItemWithAllOptionsInCart(String uid,String categoryId, String foodId,String branchId) {
        return cartDAO.getItemWithAllOptionsInCart(uid,categoryId,foodId,branchId);
    }
}
