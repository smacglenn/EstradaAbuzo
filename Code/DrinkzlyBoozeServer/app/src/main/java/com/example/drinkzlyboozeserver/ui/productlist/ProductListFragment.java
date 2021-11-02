package com.example.drinkzlyboozeserver.ui.productlist;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.drinkzlyboozeserver.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import EEventBus.ChangeMenuClick;
import EEventBus.ToastEvent;
import adapter.MyProductListAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import common.Common;
import common.MySwipeHelper;
import dmax.dialog.SpotsDialog;
import model.FoodModel;

public class ProductListFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1234;
    private ImageView img_food;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private android.app.AlertDialog dialog;

    private ProductListViewModel productListViewModel;
    private List<FoodModel> foodModelList;

    Unbinder unbinder;
    @BindView(R.id.recycler_food_list)
    RecyclerView recycler_food_list;

    LayoutAnimationController layoutAnimationController;
    MyProductListAdapter adapter;
    private Uri imageUri=null;


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.food_list_menu,menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager)getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView)menuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                startSearchFood(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        ImageView closeButton = (ImageView)searchView.findViewById(R.id.search_close_btn);
        closeButton.setOnClickListener(v -> {
        EditText ed = (EditText)searchView.findViewById(R.id.search_src_text);
        ed.setText("");
        searchView.setQuery("",false);
        searchView.onActionViewCollapsed();
        menuItem.collapseActionView();
        productListViewModel.getMutableLiveDataFoodList().setValue(Common.categorySelected.getFoods());

        });
    }

    private void startSearchFood(String query) {
        List<FoodModel> resultFood = new ArrayList<>();
        for(int i=0; i<Common.categorySelected.getFoods().size();i++) {
            FoodModel foodModel = Common.categorySelected.getFoods().get(i);
            if (foodModel.getName().toLowerCase().contains(query.toLowerCase()))
            {
                foodModel.setPositionInList(i);
                resultFood.add(foodModel);
            }
        }
            productListViewModel.getMutableLiveDataFoodList().setValue(resultFood);
     }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        productListViewModel =
                new ViewModelProvider(this).get(ProductListViewModel.class);
        View root = inflater.inflate(R.layout.fragment_foodlist, container, false);
        unbinder = ButterKnife.bind(this, root);
        initViews();
        productListViewModel.getMutableLiveDataFoodList().observe(getViewLifecycleOwner(), foodModels -> {
            if(foodModels!=null) {
                foodModelList = foodModels;
                adapter = new MyProductListAdapter(getContext(), foodModelList);
                recycler_food_list.setAdapter(adapter);
                recycler_food_list.setLayoutAnimation(layoutAnimationController);
            }
        });
        return root;
    }

    private void initViews() {

        setHasOptionsMenu(true);

        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        ((AppCompatActivity)getActivity())
                .getSupportActionBar()
                .setTitle(Common.categorySelected.getName());

        recycler_food_list.setHasFixedSize(true);
        recycler_food_list.setLayoutManager(new LinearLayoutManager(getContext()));

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_from_left);

        MySwipeHelper mySwipeHelper = new MySwipeHelper(getContext(), recycler_food_list, 300) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> buf) {
                buf.add(new UnderlayButton(getContext(), "DELETE", 30, 0, Color.parseColor("#9b0000"),
                        pos -> {
                                if(foodModelList!=null)
                                Common.selectedFood = foodModelList.get(pos);
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("DELETE")
                                    .setMessage("Do you wish to delete this product?")
                                    .setNegativeButton("CANCEL",((dialogInterface, which) -> dialogInterface.dismiss()))
                                    .setPositiveButton("DELETE", ((dialogInterface, which) -> {
                                        FoodModel foodModel = adapter.getItemAtPosition(pos);
                                        if(foodModel.getPositionInList() == -1)
                                            Common.categorySelected.getFoods().remove(pos);
                                        else
                                            Common.categorySelected.getFoods().remove(foodModel.getPositionInList());
                                        updateFood(Common.categorySelected.getFoods(), Common.ACTION.DELETE);
                                    }));
                            AlertDialog deleteDialog = builder.create();
                            deleteDialog.show();

                        }));

                buf.add(new UnderlayButton(getContext(), "UPDATE", 30, 0, Color.parseColor("#560027"),
                        pos -> {

                    FoodModel foodModel = adapter.getItemAtPosition(pos);
                        if(foodModel.getPositionInList() == -1)
                            showUpdateDialog(pos, foodModel);
                        else
                            showUpdateDialog(foodModel.getPositionInList(), foodModel);

                        }));
            }
        };
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.action_create1)
            showAddDialog();
        return super.onOptionsItemSelected(item);
    }

    private void showAddDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("CREATE PRODUCT");
        builder.setMessage("Please fill information");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_food, null);
        EditText edt_food_name = (EditText)itemView.findViewById(R.id.edt_food_name);
        EditText edt_food_price = (EditText)itemView.findViewById(R.id.edt_food_price);
        EditText edt_food_description = (EditText)itemView.findViewById(R.id.edt_food_description);
        img_food = (ImageView)itemView.findViewById(R.id.img_food_image);


        Glide.with(getContext()).load(R.drawable.ic_baseline_image_24).into(img_food);

        img_food.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
        });

        builder.setNegativeButton("CANCEL",((dialogInterface, which) -> dialogInterface.dismiss()))
                .setPositiveButton("CREATE",((dialogInterface, which) -> {

                    FoodModel updateFood = new FoodModel();
                    updateFood.setName(edt_food_name.getText().toString());
                    updateFood.setId(UUID.randomUUID().toString());
                    updateFood.setDescription(edt_food_description.getText().toString());
                    updateFood.setPrice(TextUtils.isEmpty(edt_food_price.getText()) ? 0 :
                            Long.parseLong(edt_food_price.getText().toString()));

                    if(imageUri!=null)
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
                                updateFood.setImage(uri.toString());
                                if(Common.categorySelected.getFoods()==null)
                                    Common.categorySelected.setFoods(new ArrayList<>());
                                Common.categorySelected.getFoods().add(updateFood);
                                updateFood(Common.categorySelected.getFoods(), Common.ACTION.CREATE);
                            });
                        }).addOnProgressListener(taskSnapshot -> {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            dialog.setMessage(new StringBuilder("Uploading: ").append(progress).append("%"));
                        });
                    }
                    else
                    {
                        if(Common.categorySelected.getFoods()==null)
                            Common.categorySelected.setFoods(new ArrayList<>());
                        Common.categorySelected.getFoods().add(updateFood);
                        updateFood(Common.categorySelected.getFoods(), Common.ACTION.CREATE);
                    }

                }));

        builder.setView(itemView);
        AlertDialog updateDialog = builder.create();
        updateDialog.show();

    }

    private void showUpdateDialog(int pos, FoodModel foodModel) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Update");
        builder.setMessage("Please fill information");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_food, null);
        EditText edt_food_name = (EditText)itemView.findViewById(R.id.edt_food_name);
        EditText edt_food_price = (EditText)itemView.findViewById(R.id.edt_food_price);
        EditText edt_food_desctiption = (EditText)itemView.findViewById(R.id.edt_food_description);
        img_food = (ImageView)itemView.findViewById(R.id.img_food_image);

        edt_food_name.setText(new StringBuilder("")
        .append(foodModel.getName()));
        edt_food_price.setText(new StringBuilder("")
                .append(foodModel.getPrice()));
        edt_food_desctiption.setText(new StringBuilder("")
                .append(foodModel.getDescription()));
        Glide.with(getContext()).load(foodModel.getImage()).into(img_food);

        img_food.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
        });

        builder.setNegativeButton("CANCEL",((dialogInterface, which) -> dialogInterface.dismiss()))
                .setPositiveButton("UPDATE",((dialogInterface, which) -> {

                    FoodModel updateFood = foodModel;
                    updateFood.setName(edt_food_name.getText().toString());
                    updateFood.setDescription(edt_food_desctiption.getText().toString());
                    updateFood.setPrice(TextUtils.isEmpty(edt_food_price.getText()) ? 0 :
                            Long.parseLong(edt_food_price.getText().toString()));

                    if(imageUri!=null)
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
                                updateFood.setImage(uri.toString());
                                Common.categorySelected.getFoods().set(pos,updateFood);
                                updateFood(Common.categorySelected.getFoods(), Common.ACTION.UPDATE);
                            });
                        }).addOnProgressListener(taskSnapshot -> {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            dialog.setMessage(new StringBuilder("Uploading: ").append(progress).append("%"));
                        });
                    }
                    else
                    {
                        Common.categorySelected.getFoods().set(pos,updateFood);
                        updateFood(Common.categorySelected.getFoods(), Common.ACTION.UPDATE);
                    }

                }));

        builder.setView(itemView);
        AlertDialog updateDialog = builder.create();
        updateDialog.show();

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK)
        {
            if(data != null && data.getData() != null)
            {
                imageUri = data.getData();
                img_food.setImageURI(imageUri);
            }
        }
    }

    private void updateFood(List<FoodModel> foods, Common.ACTION action) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("foods", foods);
        FirebaseDatabase.getInstance()
                .getReference(Common.BRANCH_REF)
                .child(Common.currentServerUser.getBranch())
                .child(Common.CATEGORY_REF)
                .child(Common.categorySelected.getMenu_id())
                .updateChildren(updateData)
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful())
                    {
                        productListViewModel.getMutableLiveDataFoodList();
                        EventBus.getDefault().postSticky(new ToastEvent(action,true));

                    }
                });
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new ChangeMenuClick(true));
        super.onDestroy();
    }
}