 package com.example.androiddrinkzlyboozeclient.ui.cart;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androiddrinkzlyboozeclient.Adapter.MyCartAdapter;
import com.example.androiddrinkzlyboozeclient.Callback.ILoadTimeFromFirebaseListener;
import com.example.androiddrinkzlyboozeclient.Common.Common;
import com.example.androiddrinkzlyboozeclient.Common.MySwipeHelper;
import com.example.androiddrinkzlyboozeclient.Database.CartDataSource;
import com.example.androiddrinkzlyboozeclient.Database.CartDatabase;
import com.example.androiddrinkzlyboozeclient.Database.CartItem;
import com.example.androiddrinkzlyboozeclient.Database.LocalCartDataSource;
import com.example.androiddrinkzlyboozeclient.EEventBus.CounterCartEvent;
import com.example.androiddrinkzlyboozeclient.EEventBus.HideFABCart;
import com.example.androiddrinkzlyboozeclient.EEventBus.MenuItemBack;
import com.example.androiddrinkzlyboozeclient.EEventBus.UpateItemInCart;
import com.example.androiddrinkzlyboozeclient.Model.BranchLocationModel;
import com.example.androiddrinkzlyboozeclient.Model.DiscountModel;
import com.example.androiddrinkzlyboozeclient.Model.FCMSendData;
import com.example.androiddrinkzlyboozeclient.Model.OrderModel;
import com.example.androiddrinkzlyboozeclient.R;
import com.example.androiddrinkzlyboozeclient.Remote.IFCMService;
import com.example.androiddrinkzlyboozeclient.Remote.RetrofitFCMClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

//import android.support.v4.app.Fragment;

public class CartFragment extends Fragment implements ILoadTimeFromFirebaseListener {

    private static final int SCAN_QR_PERMISSION = 7171;
    private Place placeSelected;
    private AutocompleteSupportFragment places_fragment;
    private PlacesClient placesClient;
    private List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG);

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Parcelable recyclerViewState;
    private CartDataSource cartDataSource;
    private String comment,address;

    LocationRequest locationRequest;
    LocationCallback locationCallback;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;

    IFCMService ifcmService;
    ILoadTimeFromFirebaseListener listener;

    @BindView(R.id.recycler_cart)
    RecyclerView recycler_cart;
    @BindView(R.id.txt_total_price1)
    TextView txt_total_price;
    @BindView(R.id.txt_empty_cart)
    TextView txt_empty_cart;
    @BindView(R.id.group_place_holder)
    CardView group_place_holder;
    @BindView(R.id.edt_discount_code)
    EditText edt_discount_code;



    @OnClick(R.id.img_check)
    void onApplyDiscount(){
        if(!TextUtils.isEmpty(edt_discount_code.getText().toString()))
        {
            AlertDialog dialog = new AlertDialog.Builder(requireContext())
                    .setCancelable(false)
                    .setMessage("Please wait...")
                    .create();
            dialog.show();
            final DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
            final DatabaseReference discountRef = FirebaseDatabase.getInstance().getReference(Common.BRANCH_REF)
                    .child(Common.currentBranch.getUid())
                    .child(Common.DISCOUNT);
            offsetRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long offset = snapshot.getValue(Long.class);
                    long estimatedServerTimeMs = System.currentTimeMillis()+offset;

                    discountRef.child(edt_discount_code.getText().toString().toLowerCase())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    DiscountModel discountModel = snapshot.getValue(DiscountModel.class);
                                    discountModel.setKey(snapshot.getKey());
                                    if(discountModel.getUntilDate()<estimatedServerTimeMs)
                                    {
                                        dialog.dismiss();
                                        listener.onLoadTimeFailed("Discount code has been expire");
                                    }
                                    else
                                    {
                                        dialog.dismiss();
                                        Common.discountApply = discountModel;
                                        sumAllItemInCart();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    dialog.dismiss();
                                    listener.onLoadTimeFailed(error.getMessage());
                                }
                            });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    dialog.dismiss();
                    listener.onLoadTimeFailed(error.getMessage());
                }
            });
        }
    }



    @OnClick(R.id.btn_place_order)
    void onPlaceOrderClick() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("One more step!");

        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_place_order, null);


        EditText edt_comment = (EditText) view.findViewById(R.id.edt_comment1);
        TextView txt_address = (TextView) view.findViewById(R.id.txt_address_detail);
        RadioButton rdi_home = (RadioButton) view.findViewById(R.id.rdi_home_address);
        RadioButton rdi_other = (RadioButton) view.findViewById(R.id.rdi_other_address);
        RadioButton rdi_ship_to = (RadioButton) view.findViewById(R.id.rdi_ship_this_address);
        RadioButton rdi_cod = (RadioButton) view.findViewById(R.id.rdi_cod);

        places_fragment = (AutocompleteSupportFragment)getActivity().getSupportFragmentManager()
                .findFragmentById(R.id.places_autocomplete_fragment);
        places_fragment.setPlaceFields(placeFields);
        places_fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                placeSelected = place;
                    txt_address.setText(place.getAddress());

            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(getContext(), ""+status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        txt_address.setText(Common.currentUser.getAddress());

        rdi_home.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                txt_address.setText(Common.currentUser.getAddress());
                txt_address.setVisibility(View.VISIBLE);
                places_fragment.setHint(Common.currentUser.getAddress());
            }
        });
        rdi_other.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                txt_address.setVisibility(View.VISIBLE);
            }
        });
        rdi_ship_to.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                fusedLocationProviderClient.getLastLocation()
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            txt_address.setVisibility(View.GONE);
                        }).addOnCompleteListener(task -> {
                            String coordinates = new StringBuilder()
                                    .append(task.getResult().getLatitude())
                                    .append("/")
                                    .append(task.getResult().getLongitude()).toString();

                                    Single<String> singleAddress = Single.just(getAddressFromLatLng(task.getResult().getLatitude(),
                                            task.getResult().getLongitude()));
                                    Disposable disposable = singleAddress.subscribeWith(new DisposableSingleObserver<String>() {
                                        @Override
                                        public void onSuccess(@io.reactivex.annotations.NonNull String s) {
                                            txt_address.setText(s);
                                            txt_address.setVisibility(View.VISIBLE);
                                            places_fragment.setHint(s);

                                        }

                                        @Override
                                        public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                            txt_address.setText(e.getMessage());
                                            txt_address.setVisibility(View.VISIBLE);
                                        }
                                    });


                        });
            }
        });


        builder.setView(view);
        builder.setNegativeButton("CANCEL", (dialog, which) -> {
            dialog.dismiss();
        }).setPositiveButton("YES", (dialog, which) -> {
            dialog.dismiss();
            if(rdi_cod.isChecked()) {
                if(txt_address.getText().toString().contains("Cagayan de Oro, Misamis Oriental, Philippines") || txt_address.getText().toString().contains("Cagayan de Oro, 9000 Misamis Oriental, Philippines")){
                    address = txt_address.getText().toString();
                    comment = edt_comment.getText().toString();
                    paymentCOD(address, comment);
                   // paymentCOD(txt_address.getText().toString(), edt_comment.getText().toString());
                }else {
                    Toast.makeText(getContext(), "[ERROR!] Place Order Failed! Outside Cagayan de Oro!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
                  
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(dialogInterface -> {
                if(places_fragment!=null)
                    getActivity().getSupportFragmentManager()
                    .beginTransaction().remove(places_fragment)
                    .commit();
        });
        dialog.show();

    }

    private void paymentCOD(String address, String comment) {

        FirebaseDatabase.getInstance()
                .getReference(Common.BRANCH_REF)
                .child(Common.currentBranch.getUid())
                .child(Common.LOCATION_REF)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            BranchLocationModel location = snapshot.getValue(BranchLocationModel.class);
                            applyShippingFeeByLocation(location);
                        }
                        else
                            applyShippingFeeByLocation(null);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });



    }

    private void applyShippingFeeByLocation(BranchLocationModel location) {
        compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getUid(),
                Common.currentBranch.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cartItems ->
                        cartDataSource.sumPriceInCart(Common.currentUser.getUid(),
                                Common.currentBranch.getUid())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new SingleObserver<Double>() {
                                    @Override
                                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                                    }

                                    @Override
                                    public void onSuccess(@io.reactivex.annotations.NonNull Double totalPrice) {
                                        double finalPrice = totalPrice;
                                        OrderModel order = new OrderModel();
                                        order.setUserId(Common.currentUser.getUid());
                                        order.setUserName(Common.currentUser.getName());
                                        order.setUserPhone(Common.currentUser.getPhone());
                                        order.setShippingAddress(address);
                                        order.setComment(comment);
                                        if (currentLocation != null) {
                                            order.setLat(currentLocation.getLatitude());
                                            order.setLng(currentLocation.getLongitude());

                                            if(location!=null)
                                            {
                                                Location orderLocation = new Location("");
                                                orderLocation.setLatitude(currentLocation.getLatitude());
                                                orderLocation.setLongitude(currentLocation.getLongitude());

                                                Location branchLocation = new Location("");
                                                branchLocation.setLatitude(location.getLat());
                                                branchLocation.setLongitude(location.getLng());

                                                float distance = orderLocation.distanceTo(branchLocation)/1000;

                                                if((distance*Common.SHIPPING_COST_PER_KM)> Common.MAX_SHIPPING_COST)
                                                    order.setShippingFee(Common.MAX_SHIPPING_COST);
                                               else if(distance*Common.SHIPPING_COST_PER_KM<30)
                                                   order.setShippingFee(30);
                                                else
                                                    order.setShippingFee(distance*Common.SHIPPING_COST_PER_KM);

                                                Toast.makeText(getContext(), String.valueOf(distance) + " || " +String.valueOf(branchLocation.getLatitude())+","+String.valueOf(branchLocation.getLongitude()), Toast.LENGTH_LONG).show();
                                            }else
                                                order.setShippingFee(0);



                                        } else {

                                            order.setLat(-0.1f);
                                            order.setLng(-0.1f);

                                            order.setShippingFee(Common.MAX_SHIPPING_COST);
                                        }
                                        order.setCartItemList(cartItems);
                                        if(Common.discountApply!=null)
                                            order.setDiscount(Common.discountApply.getPercent());
                                        else
                                            order.setDiscount(0);

                                        order.setFinalPayment(finalPrice);
                                        order.setTotalPayment(finalPrice+order.getShippingFee());
                                        order.setCod(true);
                                        order.setTransactionId("Cash On Delivery");

                                        syncLocalTimeWithGlobalTime(order);

                                    }

                                    @Override
                                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                        if (!e.getMessage().contains("Query returned empty result set"))
                                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }), throwable -> Toast.makeText(CartFragment.this.getContext(), "" + throwable.getMessage(), Toast.LENGTH_SHORT).show()));
    }

    private void syncLocalTimeWithGlobalTime(OrderModel order) {
       AlertDialog dialog = new AlertDialog.Builder(requireContext())
               .setTitle("Shipping Fee")
               .setMessage(new StringBuilder("We will take ₱")
               .append(Math.round(order.getShippingFee()))
               .append(" for shipping your order\nYour order total payment is: ₱")
               .append(Math.round(order.getFinalPayment()+order.getShippingFee())).toString())
               .setNegativeButton("NO", (dialog1, which) -> dialog1.dismiss())
               .setPositiveButton("YES", (dialog12, which) -> {
                   dialog12.dismiss();
                   final DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
                   offsetRef.addListenerForSingleValueEvent(new ValueEventListener() {
                       @Override
                       public void onDataChange(@NonNull DataSnapshot snapshot) {
                           long offset = snapshot.getValue(Long.class);
                           long estimatedServerTimeMs = System.currentTimeMillis()+offset;
                           SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm");
                           Date resultDate = new Date(estimatedServerTimeMs);
                           Log.d("TEST_DATE", ""+sdf.format(resultDate));

                           listener.onLoadTimeSuccess(order, estimatedServerTimeMs);
                       }

                       @Override
                       public void onCancelled(@NonNull DatabaseError error) {
                           listener.onLoadTimeFailed(error.getMessage());
                       }
                   });
               }).create();
       dialog.show();
    }

    private void writeOrderToFirebase(OrderModel order) {

        FirebaseDatabase.getInstance()
                .getReference(Common.BRANCH_REF)
                .child(Common.currentBranch.getUid())
                .child(Common.ORDER_REF)
                .child(Common.createOrderNumber())
                .setValue(order)
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnCompleteListener(task -> {
                    cartDataSource.cleanCart(Common.currentUser.getUid(),Common.currentBranch.getUid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SingleObserver<Integer>() {
                                @Override
                                public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                                }

                                @Override
                                public void onSuccess(@io.reactivex.annotations.NonNull Integer integer) {
                                    Map<String,String> notiData = new HashMap<>();
                                    notiData.put(Common.NOTI_TITLE, "New Order");
                                    notiData.put(Common.NOTI_CONTENT,"You have new order from "+Common.currentUser.getPhone());

                                    FCMSendData sendData = new FCMSendData(Common.createTopicOrder(),notiData);

                                    compositeDisposable.add(ifcmService.sendNotification(sendData)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(fcmResponse -> {
                                        Toast.makeText(getContext(), "Order placed successfully", Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                    }, throwable -> {
                                        Toast.makeText(getContext(), "Order sent but failed to send notification", Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                    }));



                                }

                                @Override
                                public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                    Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                });
    }

    private String getAddressFromLatLng(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        String result="";
        try {
            List<Address> addressList = geocoder.getFromLocation(latitude,longitude,1);
            if(addressList != null && addressList.size() > 0)
            {
                Address address = addressList.get(0);
                StringBuilder sb = new StringBuilder(address.getAddressLine(0));
                result = sb.toString();
            }else
                result = "Address not found";

        }catch (IOException e){
            e.printStackTrace();
            result = e.getMessage();
        }
        return result;
    }

    private MyCartAdapter adapter;

    private Unbinder unbinder;

    private CartViewModel cartViewModel;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        View root = inflater.inflate(R.layout.fragment_cart, container, false);

        ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);

        listener = this;
        cartViewModel.initCartDataSource(getContext());
        cartViewModel.getMutableLiveDataCartItems().observe(getViewLifecycleOwner(), new Observer<List<CartItem>>() {
            @Override
            public void onChanged(List<CartItem> cartItems) {
                if (cartItems == null || cartItems.isEmpty()) {
                    recycler_cart.setVisibility(View.GONE);
                    group_place_holder.setVisibility(View.GONE);
                    txt_empty_cart.setVisibility(View.VISIBLE);
                } else {
                    recycler_cart.setVisibility(View.VISIBLE);
                    group_place_holder.setVisibility(View.VISIBLE);
                    txt_empty_cart.setVisibility(View.GONE);

                    adapter = new MyCartAdapter(getContext(), cartItems);
                    recycler_cart.setAdapter(adapter);
                }
            }
        });

        unbinder = ButterKnife.bind(this, root);
        initViews();
        initLocation();
        calculateTotalPrice();
        return root;

    }

    private void initLocation() {
        buildLocationRequest();
        buildLocationCallback();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
             //4/24 1:00am

            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currentLocation = locationResult.getLastLocation();
            }
        };
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);
    }

    private void initViews() {

        initPlacesClient();
        setHasOptionsMenu(true);

        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDao());

        EventBus.getDefault().postSticky(new HideFABCart(true));

        recycler_cart.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_cart.setLayoutManager(layoutManager);
        recycler_cart.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));

       MySwipeHelper mySwipeHelper = new MySwipeHelper(getContext(), recycler_cart, 200) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> buf) {
                buf.add(new UnderlayButton(getContext(), "DELETE", 30, 0, Color.parseColor("#FF3C30"),
                        pos -> {
                            CartItem cartItem = adapter.getItemAtPosition(pos);
                                cartDataSource.deleteCartItems(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new SingleObserver<Integer>() {
                                        @Override
                                        public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(@io.reactivex.annotations.NonNull Integer integer) {
                                            adapter.notifyItemRemoved(pos);
                                            sumAllItemInCart(); //Update Total Price
                                            EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                            Toast.makeText(getContext(), "Cart item Deleted", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }));
            }
        };



        sumAllItemInCart();

    }

    private void initPlacesClient() {
        Places.initialize(getContext(),getString(R.string.google_maps_key));
        placesClient = Places.createClient(getContext());
    }

    private void sumAllItemInCart() {
        cartDataSource.sumPriceInCart(Common.currentUser.getUid(),Common.currentBranch.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@io.reactivex.annotations.NonNull Double aDouble) {
                        if(Common.discountApply!=null)
                        {
                            aDouble = aDouble - (aDouble*Common.discountApply.getPercent()/100);
                            txt_total_price.setText(new StringBuilder("Total: ₱").append(aDouble)
                            .append(" (-")
                            .append(Common.discountApply.getPercent())
                            .append("%)"));
                        }
                        else
                        {
                            //txt_total_price.setText(new StringBuilder("Total: ₱"));
                            calculateTotalPrice();
                        }

                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        if (!e.getMessage().contains("Query returned empty"))
                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.action_settings).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // 4/22 10.42 YT pt.17 30:26
        inflater.inflate(R.menu.cart_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_clear_cart) {
            cartDataSource.cleanCart(Common.currentUser.getUid(),Common.currentBranch.getUid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                        }

                        @Override
                        public void onSuccess(@io.reactivex.annotations.NonNull Integer integer) {
                            Toast.makeText(getContext(), "Clear cart Success", Toast.LENGTH_SHORT).show();
                            EventBus.getDefault().postSticky(new CounterCartEvent(true));
                        }

                        @Override
                        public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().removeAllStickyEvents();
        EventBus.getDefault().postSticky(new HideFABCart(false));
        EventBus.getDefault().postSticky(new CounterCartEvent(false));
        cartViewModel.onStop();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            compositeDisposable.clear();
                super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (fusedLocationProviderClient != null) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        }

    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onUpdateItemInCartEvent(UpateItemInCart event)
    {
        if(event.getCartItem() != null)
        {
            recyclerViewState = recycler_cart.getLayoutManager().onSaveInstanceState();
            cartDataSource.updateCartItems(event.getCartItem())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                                 
                        }

                        @Override
                        public void onSuccess(@io.reactivex.annotations.NonNull Integer integer) {
                            calculateTotalPrice();
                            recycler_cart.getLayoutManager().onRestoreInstanceState(recyclerViewState);
                        }

                        @Override
                        public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                            Toast.makeText(getContext(), "[UPDATE CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void calculateTotalPrice() {
        cartDataSource.sumPriceInCart(Common.currentUser.getUid(),Common.currentBranch.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@io.reactivex.annotations.NonNull Double price) {
                        txt_total_price.setText(new StringBuilder("Total : ₱").append(price));

                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        if(!e.getMessage().contains("Query returned empty result set"))
                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onLoadTimeSuccess(OrderModel order, long estimateTimeInMs) {
            order.setCreateDate(estimateTimeInMs);
            order.setOrderStatus(0);
            writeOrderToFirebase(order);
    }

    @Override
    public void onLoadOnlyTimeSuccess(long estimateTimeInMs) {
        //do nothing
    }

    @Override
    public void onLoadTimeFailed(String message) {
        Toast.makeText(getContext(), ""+message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==SCAN_QR_PERMISSION)
        {
            if(resultCode== Activity.RESULT_OK)
            {
                edt_discount_code.setText(data.getStringExtra(Common.QR_CODE_TAG).toLowerCase());
            }
        }
    }
}
