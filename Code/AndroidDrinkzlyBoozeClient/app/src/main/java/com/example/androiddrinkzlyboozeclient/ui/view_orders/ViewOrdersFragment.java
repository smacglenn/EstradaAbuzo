package com.example.androiddrinkzlyboozeclient.ui.view_orders;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androiddrinkzlyboozeclient.Adapter.MyOrdersAdapter;
import com.example.androiddrinkzlyboozeclient.Callback.ILoadOrderCallbackListener;
import com.example.androiddrinkzlyboozeclient.Common.Common;
import com.example.androiddrinkzlyboozeclient.Common.MySwipeHelper;
import com.example.androiddrinkzlyboozeclient.Database.CartDataSource;
import com.example.androiddrinkzlyboozeclient.Database.CartDatabase;
import com.example.androiddrinkzlyboozeclient.Database.CartItem;
import com.example.androiddrinkzlyboozeclient.Database.LocalCartDataSource;
import com.example.androiddrinkzlyboozeclient.EEventBus.CounterCartEvent;
import com.example.androiddrinkzlyboozeclient.EEventBus.MenuItemBack;
import com.example.androiddrinkzlyboozeclient.Model.OrderModel;
import com.example.androiddrinkzlyboozeclient.Model.ShippingOrderModel;
import com.example.androiddrinkzlyboozeclient.R;
import com.example.androiddrinkzlyboozeclient.TrackingOrderActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ViewOrdersFragment extends Fragment implements ILoadOrderCallbackListener {

    CartDataSource cartDataSource;
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    @BindView(R.id.recycler_orders)
    RecyclerView recycler_orders;

    AlertDialog dialog;

    private Unbinder unbinder;

    private ViewOrdersViewModel viewOrdersViewModel;

    private ILoadOrderCallbackListener listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewOrdersViewModel = new ViewModelProvider(this).get(ViewOrdersViewModel.class);
        View root = inflater.inflate(R.layout.fragment_view_orders, container, false);
        unbinder = ButterKnife.bind(this, root);

        initViews(root);
        loadOrdersFromFirebase();

        viewOrdersViewModel.getMutableLiveDataOrderList().observe(getViewLifecycleOwner(), orderList -> {
            MyOrdersAdapter adapter = new MyOrdersAdapter(getContext(),orderList);
            recycler_orders.setAdapter(adapter);
        });

        return root;
    }

    private void loadOrdersFromFirebase() {
        List<OrderModel> orderList = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference(Common.BRANCH_REF)
                .child(Common.currentBranch.getUid())
                .child(Common.ORDER_REF)
                .orderByChild("userId")
                .equalTo(Common.currentUser.getUid())
                .limitToLast(100)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot orderSnapShot:snapshot.getChildren())
                        {
                            OrderModel order = orderSnapShot.getValue(OrderModel.class);
                            order.setOrderNumber(orderSnapShot.getKey());
                            orderList.add(order);
                        }
                        listener.onLoadOrderSuccess(orderList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onLoadOrderFailed(error.getMessage());
                    }
                });
    }

    private void initViews(View root) {
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDao());

        listener = this;
        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(getContext()).build();

        recycler_orders.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_orders.setLayoutManager(layoutManager);
        recycler_orders.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));

        MySwipeHelper mySwipeHelper = new MySwipeHelper(getContext(), recycler_orders, 250) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> buf) {
                buf.add(new UnderlayButton(getContext(), "CANCEL ORDER", 30, 0, Color.parseColor("#FF3C30"),
                        pos -> {
                           OrderModel orderModel = ((MyOrdersAdapter)recycler_orders.getAdapter()).getItemAtPosition(pos);
                           if(orderModel.getOrderStatus()==0)
                           {
                               androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                               builder.setTitle("Cancel Order")
                                       .setMessage("Do you wish to cancel your order?")
                                       .setNegativeButton("NO", (dialog, which) -> dialog.dismiss())
                                       .setPositiveButton("YES", (dialog, which) -> {

                                           Map<String,Object> update_data = new HashMap<>();
                                           update_data.put("orderStatus", -1);
                                           FirebaseDatabase.getInstance()
                                                   .getReference(Common.BRANCH_REF)
                                                   .child(Common.currentBranch.getUid())
                                                   .child(Common.ORDER_REF)
                                                   .child(orderModel.getOrderNumber())
                                                   .updateChildren(update_data)
                                                   .addOnFailureListener(e -> Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show())
                                                   .addOnSuccessListener(aVoid -> {
                                                       orderModel.setOrderStatus(-1);
                                                       ((MyOrdersAdapter)recycler_orders.getAdapter()).setItemAtPosition(pos,orderModel);
                                                       recycler_orders.getAdapter().notifyItemChanged(pos);
                                                       Toast.makeText(getContext(), "Order canceled successfully!", Toast.LENGTH_SHORT).show();
                                                   });
                                       });
                               androidx.appcompat.app.AlertDialog dialog = builder.create();
                               dialog.show();
                           }
                           else
                           {
                               Toast.makeText(getContext(), new StringBuilder("Your order was already ")
                                       .append(Common.convertStatusToText(orderModel.getOrderStatus()))
                                       .append(", unable to cancel!"), Toast.LENGTH_SHORT).show();
                           }
                        }));

                buf.add(new UnderlayButton(getContext(), "TRACK ORDER", 30, 0, Color.parseColor("#001970"),
                        pos -> {
                            OrderModel orderModel = ((MyOrdersAdapter)recycler_orders.getAdapter()).getItemAtPosition(pos);

                            FirebaseDatabase.getInstance()
                                    .getReference(Common.BRANCH_REF)
                                    .child(Common.currentBranch.getUid())
                                    .child(Common.SHIPPING_ORDER_REF)
                                    .child(orderModel.getOrderNumber())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists())
                                            {
                                                Common.currentShippingOrder = snapshot.getValue(ShippingOrderModel.class);
                                                Common.currentShippingOrder.setKey(snapshot.getKey());
                                                if(Common.currentShippingOrder.getCurrentLat() != -1 &&
                                                Common.currentShippingOrder.getCurrentLng() != -1)
                                                {
                                                    startActivity(new Intent(getContext(), TrackingOrderActivity.class));
                                                }
                                                else
                                                {
                                                    Toast.makeText(getContext(), "Shipper not yet started to ship your order, Please wait", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                            else {
                                                Toast.makeText(getContext(), "You order is processing, Please wait for you order to ship", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(getContext(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }));

                buf.add(new UnderlayButton(getContext(), "REPEAT ORDER", 30, 0, Color.parseColor("#5d4037"),
                        pos -> {
                            OrderModel orderModel = ((MyOrdersAdapter)recycler_orders.getAdapter()).getItemAtPosition(pos);

                            dialog.show();
                            cartDataSource.cleanCart(Common.currentUser.getUid(),Common.currentBranch.getUid())
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Integer>() {
                                        @Override
                                        public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(@io.reactivex.annotations.NonNull Integer integer) {
                                            CartItem[] cartItems = orderModel
                                                    .getCartItemList().toArray(new CartItem[orderModel.getCartItemList().size()]);

                                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItems)
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(() -> {
                                                        dialog.dismiss();
                                                        Toast.makeText(getContext(), "Order repeated!", Toast.LENGTH_SHORT).show();
                                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                                    }, throwable -> {
                                                        dialog.dismiss();
                                                        Toast.makeText(getContext(), ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();

                                                    }));


                                        }



                                        @Override
                                        public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                            dialog.dismiss();
                                            Toast.makeText(getContext(), "[ERROR] "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }));
            }
        };
    }

    @Override
    public void onLoadOrderSuccess(List<OrderModel> orderList) {
        dialog.dismiss();
        viewOrdersViewModel.setMutableLiveDataOrderList(orderList);
    }

    @Override
    public void onLoadOrderFailed(String message) {
        dialog.dismiss();
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}
