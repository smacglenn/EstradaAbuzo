package com.example.androiddrinkzlyboozeclient.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.androiddrinkzlyboozeclient.Callback.IRecyclerClickListener;
import com.example.androiddrinkzlyboozeclient.Common.Common;
import com.example.androiddrinkzlyboozeclient.EEventBus.MenuItemEvent;
import com.example.androiddrinkzlyboozeclient.Model.BranchModel;
import com.example.androiddrinkzlyboozeclient.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyBranchAdapter extends RecyclerView.Adapter<MyBranchAdapter.MyViewHolder> {

        Context context;
    List<BranchModel> branchModelList;

    public MyBranchAdapter(Context context, List<BranchModel> branchModelList) {
        this.context = context;
        this.branchModelList = branchModelList;
    }



    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_branch,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context)
                .load(branchModelList.get(position).getImageUrl())
                .into(holder.img_branch);
        holder.txt_branch_name.setText(new StringBuilder(branchModelList.get(position).getName()));
        holder.txt_branch_address.setText(new StringBuilder(branchModelList.get(position).getAddress()));

        holder.setListener((view, pos) -> {
            Common.currentBranch = branchModelList.get(pos);
            EventBus.getDefault().postSticky(new MenuItemEvent(true,branchModelList.get(pos)));
        });
    }

    @Override
    public int getItemCount() {
        return branchModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.txt_branch_name)
        TextView txt_branch_name;
        @BindView(R.id.txt_branch_address)
        TextView txt_branch_address;
        @BindView(R.id.img_branch)
        ImageView img_branch;

        Unbinder unbinder;

        IRecyclerClickListener listener;


        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onItemClickListener(v,getAdapterPosition());
        }
    }


}
