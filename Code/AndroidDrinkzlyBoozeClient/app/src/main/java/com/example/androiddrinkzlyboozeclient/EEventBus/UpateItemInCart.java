package com.example.androiddrinkzlyboozeclient.EEventBus;

import com.example.androiddrinkzlyboozeclient.Database.CartItem;

public class UpateItemInCart {
    private CartItem cartItem;

    public UpateItemInCart(CartItem cartItem) {
        this.cartItem = cartItem;
    }

    public CartItem getCartItem() {
        return cartItem;
    }

    public void setCartItem(CartItem cartItem) {
        this.cartItem = cartItem;
    }
}
