package adapter;

import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.drinkzlyboozeserver.R;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import common.Common;
import model.OrderTransactionModel;

public class MyTransactionsAdater extends RecyclerView.Adapter<MyTransactionsAdater.MyViewHolder> {
    Context context;
    List<OrderTransactionModel> orderTransactionModelList;
    SimpleDateFormat simpleDateFormat;

    public MyTransactionsAdater(Context context, List<OrderTransactionModel> orderTransactionModelList) {
        this.context = context;
        this.orderTransactionModelList = orderTransactionModelList;
        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context)
        .inflate(R.layout.layout_trans_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context)
                .load(orderTransactionModelList.get(position).getCartItemList().get(0).getFoodImage())
                .into(holder.img_food_trans);
        Common.setSpanStringColor("Order date: ",simpleDateFormat.format(orderTransactionModelList.get(position).getCreateDate()),
                holder.txt_date_trans, Color.parseColor("#333639"));
        Common.setSpanStringColor("Transaction ID: ",orderTransactionModelList.get(position).getKey(),
                holder.txt_order_number_trans, Color.parseColor("#333639"));
        Common.setSpanStringColor("Mode of Payment: ",orderTransactionModelList.get(position).getTransactionId(),
                holder.txt_order_trans, Color.parseColor("#333639"));
        Common.setSpanStringColor("Total: ",String.valueOf(orderTransactionModelList.get(position).getFinalPayment()),
                holder.txt_total_trans, Color.parseColor("#333639"));
        Common.setSpanStringColor("Status: ",Common.convertStatusToString(orderTransactionModelList.get(position).getOrderStatus()),
                holder.txt_status_trans, Color.parseColor("#333639"));

        holder.btn_remit.setOnClickListener(v -> {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
            builder.setTitle("Remit");
            builder.setMessage("Mark as Complete?");

            builder.setNegativeButton("CANCEL",((dialogInterface, which) -> dialogInterface.dismiss()))
                    .setPositiveButton("COMPLETE",((dialogInterface, which) -> {
                        holder.btn_remit.setText("COMPLETED");
                        holder.btn_remit.setEnabled(false);
                    }));

            androidx.appcompat.app.AlertDialog dialog = builder.create();
            dialog.show();
        });

    }

    @Override
    public int getItemCount() {
        return orderTransactionModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
            @BindView(R.id.img_food_trans)
            ImageView img_food_trans;
            @BindView(R.id.txt_date_trans)
            TextView txt_date_trans;
            @BindView(R.id.txt_order_number_trans)
            TextView txt_order_number_trans;
            @BindView(R.id.txt_order_transId)
            TextView txt_order_trans;
            @BindView(R.id.txt_total_trans)
            TextView txt_total_trans;
            @BindView(R.id.txt_status_trans)
            TextView txt_status_trans;
            @BindView(R.id.btn_remit)
            MaterialButton btn_remit;

            private Unbinder unbinder;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
        }
    }
}
