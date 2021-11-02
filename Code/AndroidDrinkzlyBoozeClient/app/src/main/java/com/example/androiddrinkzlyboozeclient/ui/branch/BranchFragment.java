package com.example.androiddrinkzlyboozeclient.ui.branch;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androiddrinkzlyboozeclient.Adapter.MyBranchAdapter;
import com.example.androiddrinkzlyboozeclient.EEventBus.CounterCartEvent;
import com.example.androiddrinkzlyboozeclient.EEventBus.HideFABCart;
import com.example.androiddrinkzlyboozeclient.EEventBus.MenuInflateEvent;
import com.example.androiddrinkzlyboozeclient.R;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class BranchFragment extends Fragment {

    private BranchViewModel mViewModel;

    Unbinder unbinder;
    @BindView(R.id.recycler_branch)
    RecyclerView recycler_branch;
    AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;
    MyBranchAdapter adapter;

    public static BranchFragment newInstance() {
        return new BranchFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this).get(BranchViewModel.class);
        View root = inflater.inflate(R.layout.fragment_branch, container, false);
        unbinder = ButterKnife.bind(this,root);
        intViews();

        mViewModel.getMessageError().observe(getViewLifecycleOwner(),message->{
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        mViewModel.getBranchListMutable().observe(getViewLifecycleOwner(),branchModels ->{
            dialog.dismiss();
            adapter = new MyBranchAdapter(getContext(),branchModels);
            recycler_branch.setAdapter(adapter);
            recycler_branch.setLayoutAnimation(layoutAnimationController);
        });

        return root;
    }

    private void intViews() {
        EventBus.getDefault().postSticky(new HideFABCart(true));
        setHasOptionsMenu(true);
        dialog = new AlertDialog.Builder(getContext()).setCancelable(false)
                .setMessage("Please wait...").create();
        dialog.show();
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recycler_branch.setLayoutManager(linearLayoutManager);
        recycler_branch.addItemDecoration(new DividerItemDecoration(getContext(),linearLayoutManager.getOrientation()));
    }


    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().postSticky(new CounterCartEvent(true));
        EventBus.getDefault().postSticky(new MenuInflateEvent(false));
    }


}