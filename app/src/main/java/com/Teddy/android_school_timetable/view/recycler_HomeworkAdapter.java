package com.Teddy.android_school_timetable.view;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;



import java.util.ArrayList;
import java.util.HashMap;

public class recycler_HomeworkAdapter extends RecyclerView.Adapter<recycler_HomeworkAdapter.ViewHolder> {
    HashMap<String,String> Min;
    String name="";
    public recycler_HomeworkAdapter(final HashMap<String,String> in){
        Min=in;
    }
    ViewGroup parent;
    @NonNull
    @Override
    public recycler_HomeworkAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerHomeworkBinding one=RecyclerHomeworkBinding.inflate(LayoutInflater.from(parent.getContext()));
        //View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_one_item,parent,false);
        ViewHolder my=new ViewHolder(one);
        this.parent=parent;
        return my;

    }

    @Override
    public void onBindViewHolder(@NonNull recycler_HomeworkAdapter.ViewHolder holder, int position) {
        int i=0;
        name="";
        System.out.println(position);
        for (String key : Min.keySet()) {

            if (i == position) {
                name=key;
                break;
            }
            i++;
        }
        holder.one.nameClass.setText(name);
        holder.one.hText.setText(Min.get(name));
        System.out.println(name+Min.get(name));

            holder.one.deleteH.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println(position);

                    Min.remove(name);
                    notifyItemRangeRemoved(position,1);
                   // notifyItemRangeInserted(position,Min.size());

                }
            });


    }

    @Override
    public int getItemCount() {
        return Min.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        RecyclerHomeworkBinding one;
        public ViewHolder(@NonNull RecyclerHomeworkBinding item) {
            super(item.getRoot());
            one=item;
        }
    }
}
