package com.example.healthdiary.ui;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthdiary.R;

import java.util.List;

/**
 * This class is responsible for filling the recyclerView list with list items according to the items_for_list.xml layout
 */
public class ListViewAdapter extends RecyclerView.Adapter<ListViewAdapter.ViewHolder> {

    private final List<? extends Parcelable> readings;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.textView_listAll);
        }

        public TextView getTextView() {
            return textView;
        }
    }

    public ListViewAdapter(List<? extends Parcelable> list) {
        readings = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.items_for_list, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        try{
            viewHolder.getTextView().setText(readings.get(position).toString());
        } catch (IndexOutOfBoundsException empty){
            viewHolder.getTextView().setText(R.string.empty_list_item);
        }
    }

    @Override
    public int getItemCount() {
        if(readings.size()==0) return 1;  // to enable showing empty list with text, since otherwise onBindViewHolder would not be called on empty input
        return readings.size();
    }
}


