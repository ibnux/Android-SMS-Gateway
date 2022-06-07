package com.ibnux.smsgateway.data;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ibnux.smsgateway.ObjectBox;
import com.ibnux.smsgateway.R;
import com.ibnux.smsgateway.Utils.Fungsi;

import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.MyViewHolder> {
    private List<LogLine> datas;
    long offset = 0, limit = 50;
    String search = "";
    long smallTime = System.currentTimeMillis(), bigTime = 0;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txtDate, txtMsg;

        public MyViewHolder(View v) {
            super(v);
            txtMsg = v.findViewById(R.id.txtMsg);
            txtDate = v.findViewById(R.id.txtDate);
        }
    }

    public LogAdapter() {
        Fungsi.log("Data: " + ObjectBox.get().boxFor(LogLine.class).count());
    }

    public void reload() {
        smallTime = System.currentTimeMillis();
        bigTime = 0;
        if (search.length() > 0) {
            datas = ObjectBox.get().boxFor(LogLine.class).query().contains(LogLine_.message, search).orderDesc(LogLine_.time).build().find(offset, limit);
        } else {
            datas = ObjectBox.get().boxFor(LogLine.class).query().orderDesc(LogLine_.time).build().find(offset, limit);
        }
        for (int n = 0; n < getItemCount(); n++) {
            if (datas.get(n).time > bigTime) {
                bigTime = datas.get(n).time;
            }
            if (smallTime > datas.get(n).time) {
                smallTime = datas.get(n).time;
            }
        }
        Fungsi.log("reload " + datas.size() + " " + bigTime + " " + smallTime);
        notifyDataSetChanged();
    }

    public void search(String search) {
        this.search = search;
        reload();
    }

    public void getNewData() {
        List<LogLine> dts;
        if (search.length() > 0) {
            dts = ObjectBox.get().boxFor(LogLine.class).query().contains(LogLine_.message, search).greater(LogLine_.time, bigTime).order(LogLine_.time).build().find(offset, limit);
        } else {
            dts = ObjectBox.get().boxFor(LogLine.class).query().greater(LogLine_.time, bigTime).order(LogLine_.time).build().find(offset, limit);
        }
        for (int n = 0; n < dts.size(); n++) {
            datas.add(0, dts.get(n));
            if (dts.get(n).time > bigTime) {
                bigTime = dts.get(n).time;
            }
        }
        Fungsi.log("getNewData " + dts.size() + " " + bigTime);
        notifyDataSetChanged();
        if (datas.size() > 500) {
            reload();
        }
    }

    public void nextData() {
        List<LogLine> dts;
        if (search.length() > 0) {
            dts = ObjectBox.get().boxFor(LogLine.class).query().contains(LogLine_.message, search).less(LogLine_.time, smallTime).orderDesc(LogLine_.time).build().find(offset, limit);
        } else {
            dts = ObjectBox.get().boxFor(LogLine.class).query().less(LogLine_.time, smallTime).orderDesc(LogLine_.time).build().find(offset, limit);
        }
        for (int n = 0; n < dts.size(); n++) {
            datas.add(dts.get(n));
            if (smallTime > dts.get(n).time) {
                smallTime = dts.get(n).time;
            }
        }
        Fungsi.log("nextData " + dts.size() + " " + smallTime);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LogAdapter.MyViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.log_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        LogLine ll = datas.get(position);
        holder.txtDate.setText(ll.date);
        holder.txtMsg.setText(ll.message);
    }

    @Override
    public int getItemCount() {
        return (datas == null) ? 0 : datas.size();
    }
}
