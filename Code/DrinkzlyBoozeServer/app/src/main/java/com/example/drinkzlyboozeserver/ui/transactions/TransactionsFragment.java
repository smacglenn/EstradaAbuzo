package com.example.drinkzlyboozeserver.ui.transactions;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import com.example.drinkzlyboozeserver.R;

import adapter.MyTransactionsAdater;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class TransactionsFragment extends Fragment {
    @BindView(R.id.recycler_transactions)
    RecyclerView recycler_transactions;

    Unbinder unbinder;
    LayoutAnimationController layoutAnimationController;
    MyTransactionsAdater adater;

    private TransactionsViewModel transactionsViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
       transactionsViewModel = new ViewModelProvider(this).get(TransactionsViewModel.class);

        View root = inflater.inflate(R.layout.transactions_fragment, container, false);
        unbinder = ButterKnife.bind(this,root);
        initViews();
        transactionsViewModel.getMessageError().observe(getViewLifecycleOwner(),s -> {
            Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
        });
        transactionsViewModel.getOrderTransactionMutableLiveData().observe(getViewLifecycleOwner(),transactionModels ->{
            if(transactionModels!=null)
            {
                adater = new MyTransactionsAdater(getContext(),transactionModels);
                recycler_transactions.setAdapter(adater);
                recycler_transactions.setLayoutAnimation(layoutAnimationController);
            }
        });
        return root;
    }

    private void initViews() {
        recycler_transactions.setHasFixedSize(true);
        recycler_transactions.setLayoutManager(new LinearLayoutManager(getContext()));
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);

    }

}