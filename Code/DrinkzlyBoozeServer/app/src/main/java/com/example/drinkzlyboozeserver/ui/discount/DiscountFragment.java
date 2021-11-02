package com.example.drinkzlyboozeserver.ui.discount;

import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.drinkzlyboozeserver.R;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import EEventBus.ToastEvent;
import adapter.MyDiscountAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import common.Common;
import common.MySwipeHelper;
import dmax.dialog.SpotsDialog;
import model.DiscountModel;
import model.FoodModel;

public class DiscountFragment extends Fragment {

    private DiscountViewModel mViewModel;

    private Unbinder unbinder;

    @BindView(R.id.recycler_discount)
    RecyclerView recycler_discount;
    AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;
    MyDiscountAdapter adapter;
    List<DiscountModel> discountModelList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this).get(DiscountViewModel.class);
        View root = inflater.inflate(R.layout.discount_fragment, container, false);
        unbinder = ButterKnife.bind(this,root);
        initViews();
        mViewModel.getMessageError().observe(getViewLifecycleOwner(), s -> {
            Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        mViewModel.getDiscountMutableLiveData().observe(getViewLifecycleOwner(), list -> {
            dialog.dismiss();
            if(list==null)
                discountModelList = new ArrayList<>();
            else
                discountModelList = list;
            adapter = new MyDiscountAdapter(getContext(),discountModelList);
            recycler_discount.setAdapter(adapter);
            recycler_discount.setLayoutAnimation(layoutAnimationController);
        });

        return root;
    }

    private void initViews() {
        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        setHasOptionsMenu(true);

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_discount.setLayoutManager(layoutManager);
        recycler_discount.addItemDecoration(new DividerItemDecoration(getContext(),layoutManager.getOrientation()));

        MySwipeHelper swipeHelper = new MySwipeHelper(getContext(),recycler_discount,200) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                underlayButtons.add(new UnderlayButton(getContext(),"Delete", 30,0, Color.parseColor("#333639"),
                        pos -> {
                            Common.discountSelected = discountModelList.get(pos);
                            showDeleteDialog();
                        }));
                underlayButtons.add(new UnderlayButton(getContext(),"Update", 30,0, Color.parseColor("#414243"),
                        pos -> {
                            Common.discountSelected = discountModelList.get(pos);
                            showUpdateDialog();
                        }));
            }
        };
    }

    private void showAddDialog() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar selectedDate = Calendar.getInstance();
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Create");
        builder.setMessage("Please fill information");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_discount,null);
        EditText edt_code = (EditText)itemView.findViewById(R.id.edt_code);
        EditText edt_percent = (EditText)itemView.findViewById(R.id.edt_percent);
        EditText edt_valid = (EditText)itemView.findViewById(R.id.edt_valid);
        ImageView img_calendar = (ImageView)itemView.findViewById(R.id.pickDate);


        DatePickerDialog.OnDateSetListener listener = (view, year, month, dayOfMonth) -> {
            selectedDate.set(Calendar.YEAR,year);
            selectedDate.set(Calendar.MONTH,month);
            selectedDate.set(Calendar.DAY_OF_MONTH,dayOfMonth);
            edt_valid.setText(simpleDateFormat.format(selectedDate.getTime()));
        };
        img_calendar.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(getContext(),listener,calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH))
                    .show();
        });

        builder.setNegativeButton("CANCEL",((dialogInterface, which) -> dialogInterface.dismiss()))
                .setPositiveButton("CREATE",((dialogInterface, which) -> {

                    DiscountModel discountModel = new DiscountModel();
                    discountModel.setKey(edt_code.getText().toString().toLowerCase());
                    discountModel.setPercent(Integer.parseInt(edt_percent.getText().toString()));
                    discountModel.setUntilDate(selectedDate.getTimeInMillis());
                    
                    createDiscount(discountModel);

                }));
        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void createDiscount(DiscountModel discountModel) {
        FirebaseDatabase.getInstance()
                .getReference(Common.BRANCH_REF)
                .child(Common.currentServerUser.getBranch())
                .child(Common.DISCOUNT)
                .child(discountModel.getKey())
                .setValue(discountModel)
                .addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful())
                    {
                        mViewModel.loadDiscount();
                        adapter.notifyDataSetChanged();
                        EventBus.getDefault().post(new ToastEvent(Common.ACTION.CREATE,true));
                    }
                });
    }

    private void updateDiscount(Map<String, Object> updateData) {
        FirebaseDatabase.getInstance()
                .getReference(Common.BRANCH_REF)
                .child(Common.currentServerUser.getBranch())
                .child(Common.DISCOUNT)
                .child(Common.discountSelected.getKey())
                .updateChildren(updateData)
                .addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful())
                    {
                        mViewModel.loadDiscount();
                        adapter.notifyDataSetChanged();
                        EventBus.getDefault().post(new ToastEvent(Common.ACTION.UPDATE,true));
                    }
                });
    }

    private void deleteDiscount() {
        FirebaseDatabase.getInstance()
                .getReference(Common.BRANCH_REF)
                .child(Common.currentServerUser.getBranch())
                .child(Common.DISCOUNT)
                .child(Common.discountSelected.getKey())
                .removeValue()
                .addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful())
                    {
                        mViewModel.loadDiscount();
                        adapter.notifyDataSetChanged();
                        EventBus.getDefault().post(new ToastEvent(Common.ACTION.DELETE,true));
                    }
                });
    }

    private void showUpdateDialog() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar selectedDate = Calendar.getInstance();
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Update");
        builder.setMessage("Please fill information");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_discount,null);
        EditText edt_code = (EditText)itemView.findViewById(R.id.edt_code);
        EditText edt_percent = (EditText)itemView.findViewById(R.id.edt_percent);
        EditText edt_valid = (EditText)itemView.findViewById(R.id.edt_valid);
        ImageView img_calendar = (ImageView)itemView.findViewById(R.id.pickDate);

        edt_code.setText(Common.discountSelected.getKey());
        edt_code.setEnabled(false);

        edt_percent.setText(new StringBuilder().append(Common.discountSelected.getPercent()));
        edt_valid.setText(simpleDateFormat.format(Common.discountSelected.getUntilDate()));

        DatePickerDialog.OnDateSetListener listener = (view, year, month, dayOfMonth) -> {
            selectedDate.set(Calendar.YEAR,year);
            selectedDate.set(Calendar.MONTH,month);
            selectedDate.set(Calendar.DAY_OF_MONTH,dayOfMonth);
            edt_valid.setText(simpleDateFormat.format(selectedDate.getTime()));
        };
        img_calendar.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(getContext(),listener,calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH))
                    .show();
        });

        builder.setNegativeButton("CANCEL",((dialogInterface, which) -> dialogInterface.dismiss()))
                .setPositiveButton("UPDATE",((dialogInterface, which) -> {

                    Map<String,Object> updateData = new HashMap<>();
                    updateData.put("percent",Integer.parseInt(edt_percent.getText().toString()));
                    updateData.put("untilDate",selectedDate.getTimeInMillis());

                    updateDiscount(updateData);

                }));
        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDeleteDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Delete");
        builder.setMessage("Delete this item?");

        builder.setNegativeButton("CANCEL",((dialogInterface, which) -> dialogInterface.dismiss()))
                .setPositiveButton("DELETE",((dialogInterface, which) -> {

                    deleteDiscount();

                }));

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }



    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.discount_menu,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.action_create2)
            showAddDialog();
        return super.onOptionsItemSelected(item);
    }
}