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

public class recycler_ChangeAdapter extends RecyclerView.Adapter<recycler_ChangeAdapter.ViewHolder> {
    private int[] addnum=new int[111];
    private int[][] time;
    ArrayList<Integer> start;
    ArrayList<Integer> end;
    MyClass one;
    Boolean delete;
    int[] mood=new int[120];
    int[] pos=new int[120];

    public recycler_ChangeAdapter(final MyClass in){
        time=new int[111][25];
        for(int i=0;i<111;i++)
            for(int ii=0;ii<25;ii++)
                time[i][ii]=-1;
        start=new ArrayList<>();
        end=new ArrayList<>();
        one=in;
        delete=false;
            for (int i = 0; i < 25; i++) {
                ArrayList<Integer> temp = in.onclass(i);
                for (int ii = 0; ii < temp.size(); ii += 2) {
                    time[temp.get(ii)][i] = temp.get(ii + 1);
                    int same = 0;
                    for (int iii = 0; iii < start.size(); iii++) {
                        if (start.get(iii) == temp.get(ii) && end.get(iii) == time[temp.get(ii)][i])
                            same++;
                    }
                    if (same == 0) {
                        start.add(temp.get(ii));
                        end.add(time[temp.get(ii)][i]);
                    }
            }
            }
    }
    ViewGroup parent;
    @NonNull
    @Override
    public recycler_ChangeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerOneItemBinding one=RecyclerOneItemBinding.inflate(LayoutInflater.from(parent.getContext()));
        //View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_one_item,parent,false);
        ViewHolder my=new ViewHolder(one);
        this.parent=parent;
        return my;

    }

    @Override
    public void onBindViewHolder(@NonNull recycler_ChangeAdapter.ViewHolder holder, int position) {

        if(position==start.size()+1){
            holder.one.name.setVisibility(View.GONE);
            holder.one.room.setVisibility(View.GONE);
            holder.one.teacher.setVisibility(View.GONE);
            holder.one.weeknum.setVisibility(View.GONE);
            holder.one.deleteClass.setVisibility(View.GONE);
            holder.one.time.setVisibility(View.GONE);
            holder.one.start.setVisibility(View.GONE);
            holder.one.end.setVisibility(View.GONE);
            holder.one.weekin.setVisibility(View.GONE);
            holder.one.addClass.setVisibility(View.VISIBLE);
            holder.one.addClass.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    start.add(1);
                    end.add(1);
                    notifyItemInserted(start.size());
                }
            });
            return;
        } else if(position==0) {
            holder.one.name.setVisibility(View.VISIBLE);
            holder.one.room.setVisibility(View.VISIBLE);
            holder.one.teacher.setVisibility(View.VISIBLE);
            holder.one.deleteClass.setVisibility(View.VISIBLE);
            holder.one.weeknum.setVisibility(View.GONE);
            holder.one.addClass.setVisibility(View.GONE);
            holder.one.time.setVisibility(View.GONE);
            holder.one.start.setVisibility(View.GONE);
            holder.one.end.setVisibility(View.GONE);
            holder.one.weekin.setVisibility(View.GONE);
            holder.one.name.setText(one.getname());
            holder.one.teacher.setText(one.getteacher());
            holder.one.room.setText(one.getroom());
            holder.one.deleteClass.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!delete) {
                        Toast.makeText(parent.getContext(), "再次点击将删除该课程", Toast.LENGTH_LONG).show();
                        delete = true;
                    }else{
                        time=new int[111][25];
                        int te=start.size();
                        for(int i=0;i<111;i++)
                            for(int ii=0;ii<25;ii++)
                                time[i][ii]=-1;

                        start=new ArrayList<>();
                        end=new ArrayList<>();
                        for(;te>0;te--) {
                            notifyItemRemoved(te);
                        }

                        delete = false;
                    }
                }
            });
        }else {

            final int npo=position-1;
            holder.one.name.setVisibility(View.GONE);
            holder.one.room.setVisibility(View.GONE);
            holder.one.teacher.setVisibility(View.GONE);
            holder.one.weeknum.setVisibility(View.VISIBLE);
            holder.one.deleteClass.setVisibility(View.VISIBLE);
            holder.one.time.setVisibility(View.VISIBLE);
            holder.one.start.setVisibility(View.VISIBLE);
            holder.one.end.setVisibility(View.VISIBLE);
            holder.one.weekin.setVisibility(View.VISIBLE);
            holder.one.addClass.setVisibility(View.GONE);
            holder.one.weeknum.setText(String.valueOf(start.get(npo) % 7 + 1));

            holder.one.weekin.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    switch (group.getCheckedRadioButtonId()) {
                        case R.id.lianxu:
                            for (int i = 0; i < 25; i++)
                                time[start.get(npo)][i] = end.get(npo);
                            mood[npo]=0;
                            break;
                        case R.id.danshu:
                            mood[npo]=1;
                            break;
                        case R.id.shuangshu:
                            mood[npo]=2;
                            break;
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                notifyItemChanged(npo+1);
                            }catch (IllegalStateException e) {
                                new Handler().postDelayed(this,200);
                            }
                        }
                    },200);

                }
            });
            switch (mood[npo]) {
                case 0:
                    holder.one.lianxu.setChecked(true);
                    break;
                case 1:
                    holder.one.danshu.setChecked(true);
                    break;
                case 2:
                    holder.one.shuangshu.setChecked(true);
                    break;
            }
            holder.one.deleteClass.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    start.remove(npo);
                    end.remove(npo);
                    notifyItemRangeRemoved(position,start.size()+2);
                    notifyItemRangeInserted(position,start.size()+1);
                    for(int i=npo;i<119;i++)
                        mood[i]=mood[i+1];
                }
            });

            String week = "";

            for (int i = 0; i < 25; i++) {
                if (time[start.get(npo)][i] == end.get(npo)) {
                    if(mood[npo]==2&&i%2==0){
                        time[start.get(npo)][i]=-1;
                        continue;
                    }

                    else if(mood[npo]==1&&i%2!=0) {
                        time[start.get(npo)][i]=-1;
                        continue;
                    }
                    else
                        week += i + 1 + ",";
                }
            }


            if (week.length() < 1)
                for (int i = 0; i < 25; i++)
                    if(mood[npo]==2&&i%2==0)
                        continue;
                    else if(mood[npo]==1&&i%2!=0)
                        continue;
                    else{
                        week += ((i + 1) + ",");
                        time[start.get(npo)][i] = end.get(npo);
                    }

            holder.one.time.setText(week);
            holder.one.start.setText(String.valueOf(start.get(npo) / 7 + 1));
            holder.one.end.setText(String.valueOf(end.get(npo) / 7 + 1));
        }
    }

    @Override
    public int getItemCount() {
        return start.size()+2;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        RecyclerOneItemBinding one;
        public ViewHolder(@NonNull RecyclerOneItemBinding item) {
            super(item.getRoot());
            one=item;
        }
    }
}
