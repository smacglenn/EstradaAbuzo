package com.example.drinkzlyboozeserver.ui.best_deals;

import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.drinkzlyboozeserver.R;
import com.example.drinkzlyboozeserver.ui.category.CategoryViewModel;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import EEventBus.ToastEvent;
import adapter.MyBestDealAdapter;
import adapter.MyCategoriesAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import common.Common;
import common.MySwipeHelper;
import dmax.dialog.SpotsDialog;
import model.BestDealsModel;
import model.CategoryModel;

public class BestDealsFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1235;
    private BestDealsViewModel mViewModel;

    Unbinder unbinder;
    @BindView(R.id.recycler_best_deal)
    RecyclerView recycler_best_deal;
    AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;
    MyBestDealAdapter adapter;

    List<BestDealsModel> bestDealsModels;
    ImageView img_best_deals;
    private Uri imageUri=null;

    FirebaseStorage storage;
    StorageReference storageReference;


 

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mViewModel =
                new ViewModelProvider(this).get(BestDealsViewModel.class);
        View root = inflater.inflate(R.layout.best_deals_fragment, container, false);

        unbinder = ButterKnife.bind(this,root);
        initViews();
        mViewModel.getMessageError().observe(getViewLifecycleOwner(), s -> {
            Toast.makeText(getContext(), "" +s, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        mViewModel.getBestDealsListMutable().observe(getViewLifecycleOwner(), list -> {
            dialog.dismiss();
            bestDealsModels = list;
            adapter = new MyBestDealAdapter(getContext(),bestDealsModels);
            recycler_best_deal.setAdapter(adapter);
            recycler_best_deal.setLayoutAnimation(layoutAnimationController);

        });
        return root;
    }

    private void initViews() {

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        //  dialog.show();
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        recycler_best_deal.setLayoutManager(layoutManager);
        recycler_best_deal.addItemDecoration(new DividerItemDecoration(getContext(),layoutManager.getOrientation()));

        MySwipeHelper mySwipeHelper = new MySwipeHelper(getContext(), recycler_best_deal, 200) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> buf) {

                buf.add(new UnderlayButton(getContext(), "DELETE", 30, 0, Color.parseColor("#333639"),
                        pos -> {

                            Common.bestDealsSelected = bestDealsModels.get(pos);

                            showDeleteDialog();

                        }));

                buf.add(new UnderlayButton(getContext(), "UPDATE", 30, 0, Color.parseColor("#560027"),
                        pos -> {

                            Common.bestDealsSelected = bestDealsModels.get(pos);

                            showUpdateDialog();

                        }));
            }
        };
    }

    private void showDeleteDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Delete");
        builder.setMessage("Do you wish to delete this item?");
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    deleteBestDeals();
            }
        });
        
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteBestDeals() {
        FirebaseDatabase.getInstance()
                .getReference(Common.BRANCH_REF)
                .child(Common.currentServerUser.getBranch())
                .child(Common.BEST_DEALS)
                .child(Common.bestDealsSelected.getKey())
                .removeValue()
                .addOnFailureListener(e -> Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {
                    mViewModel.loadBestDeals();
                    EventBus.getDefault().postSticky(new ToastEvent(Common.ACTION.DELETE, true));
                });
    }

    private void showUpdateDialog() {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
            builder.setTitle("Update");
            builder.setMessage("Please fill information");

            View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_category, null);
            EditText edt_category_name = (EditText)itemView.findViewById(R.id.edt_category_name);
            img_best_deals = (ImageView)itemView.findViewById(R.id.img_category);

            edt_category_name.setText(new StringBuilder("").append(Common.bestDealsSelected.getName()));
            Glide.with(getContext()).load(Common.bestDealsSelected.getImage()).into(img_best_deals);

            img_best_deals.setOnClickListener(v -> {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);

            });

            builder.setNegativeButton("CANCEL", (dialogInterface, which) -> dialog.dismiss());
            builder.setPositiveButton("UPDATE", (dialogInterface, which) -> {

                Map<String,Object> updateData = new HashMap<>();
                updateData.put("name", edt_category_name.getText().toString());

                if(imageUri != null)
                {
                    dialog.setMessage("Uploading ...");
                    dialog.show();
                    String unique_name = UUID.randomUUID().toString();
                    StorageReference imageFolder = storageReference.child("images/"+unique_name);

                    imageFolder.putFile(imageUri)
                            .addOnFailureListener(e -> {
                                dialog.dismiss();
                                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }).addOnCompleteListener(task -> {
                        dialog.dismiss();
                        imageFolder.getDownloadUrl().addOnSuccessListener(uri -> {
                            updateData.put("image", uri.toString());
                            updateBestDeals(updateData);
                        });
                    }).addOnProgressListener(taskSnapshot -> {
                        double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                        dialog.setMessage(new StringBuilder("Uploading: ").append(progress).append("%"));
                    });
                }
                else
                {
                    updateBestDeals(updateData);
                }

            });
            builder.setView(itemView);
            androidx.appcompat.app.AlertDialog dialog = builder.create();
            dialog.show();
        }

    private void updateBestDeals(Map<String, Object> updateData) {
        FirebaseDatabase.getInstance()
                .getReference(Common.BRANCH_REF)
                .child(Common.currentServerUser.getBranch())
                .child(Common.BEST_DEALS)
                .child(Common.bestDealsSelected.getKey())
                .updateChildren(updateData)
                .addOnFailureListener(e -> Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {
                    mViewModel.loadBestDeals();
                    EventBus.getDefault().postSticky(new ToastEvent(Common.ACTION.UPDATE, true));
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && requestCode == Activity.RESULT_OK)
        {
            if(data != null && data.getData() != null)
            {
                imageUri = data.getData();
                img_best_deals.setImageURI(imageUri);
            }
        }
    }



}