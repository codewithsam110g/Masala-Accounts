package com.codewithsam.masalaaccounts.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codewithsam.masalaaccounts.R;
import com.codewithsam.masalaaccounts.objects.Data;

import java.util.ArrayList;

public class DataViewAdapter extends RecyclerView.Adapter<DataViewAdapter.ViewHolder> {

    private ArrayList<Data> viewData;
    private Context context;
    private DataViewClickInterface clickInterface;
    private DataViewLongClickInterface longClickInterface;
    public void setData(ArrayList<Data> data) {
        viewData = new ArrayList<>(data);
        this.notifyDataSetChanged();
    }

    public DataViewAdapter(ArrayList<Data> viewData, Context context, DataViewClickInterface clickInterface, DataViewLongClickInterface longClickInterface) {
        this.viewData = viewData;
        this.context = context;
        this.clickInterface = clickInterface;
        this.longClickInterface = longClickInterface;
    }

    @NonNull
    @Override
    public DataViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.account_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DataViewAdapter.ViewHolder holder, int position) {
        Data data = viewData.get(position);
        holder.name.setText(data.name);
        holder.amount.setText((data.getAmount() < 0 )? ("" + data.amount) : ("+" + data.amount));
        holder.itemView.setOnClickListener(v -> {
            clickInterface.onDataClick(position);
        });
        holder.itemView.setOnLongClickListener(v -> longClickInterface.onDataLongClick(position));
    }

    @Override
    public int getItemCount() {
        return viewData.size();
    }

    public interface DataViewClickInterface {
        void onDataClick(int position);
    }

    public interface DataViewLongClickInterface{
        boolean onDataLongClick(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, amount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.txt_name);
            amount = itemView.findViewById(R.id.txt_amount);
        }
    }

}
