package com.ywl5320.wlivefm.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.ywl5320.wlivefm.R;
import com.ywl5320.wlivefm.http.beans.LiveChannelTypeBean;

import java.util.List;

/**
 * Created by ywl on 2017-7-25.
 */

public class TypeAdapter extends RecyclerView.Adapter<TypeAdapter.TypeHolder>{

    private Context context;
    private List<LiveChannelTypeBean> datas;

    public TypeAdapter(Context context, List<LiveChannelTypeBean> datas) {
        this.context = context;
        this.datas = datas;
    }

    @Override
    public TypeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_type_layout, null);
        TypeHolder holder = new TypeHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(TypeHolder holder, int position) {
        holder.btn_type.setText(datas.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public class TypeHolder extends RecyclerView.ViewHolder
    {
        private TextView btn_type;
        public TypeHolder(View itemView) {
            super(itemView);
            btn_type = (TextView) itemView.findViewById(R.id.btn_type);
        }
    }
}
