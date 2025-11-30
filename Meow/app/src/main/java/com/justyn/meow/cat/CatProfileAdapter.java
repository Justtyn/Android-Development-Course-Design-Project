package com.justyn.meow.cat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.justyn.meow.R;

import java.util.List;

/**
 * 猫咪档案列表适配器
 * 负责把 CatProfile 数据绑定到 item_cat_profile.xml 的控件上
 */
public class CatProfileAdapter extends RecyclerView.Adapter<CatProfileAdapter.CatViewHolder> {

    private final List<CatProfile> data;

    /**
     * 可选：列表 item 点击回调接口
     * 后面如果你要点一只猫进入“猫咪详情页”，可以用到
     */
    public interface OnCatClickListener {
        void onCatClicked(CatProfile profile, int position);
    }

    private final OnCatClickListener listener;

    public CatProfileAdapter(List<CatProfile> data, OnCatClickListener listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cat_profile_track, parent, false);
        return new CatViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CatViewHolder holder, int position) {
        CatProfile profile = data.get(position);
        holder.bind(profile);

        // 整个卡片点击
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                int adapterPos = holder.getAdapterPosition();
                if (adapterPos != RecyclerView.NO_POSITION) {
                    listener.onCatClicked(profile, adapterPos);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    static class CatViewHolder extends RecyclerView.ViewHolder {

        ImageView imgCatAvatar;
        TextView tvCatName;
        TextView tvCatBreed;
        TextView tvCatAge;
        TextView tvCatIntro;

        CatViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCatAvatar = itemView.findViewById(R.id.imgCatAvatar);
            tvCatName = itemView.findViewById(R.id.tvCatName);
            tvCatBreed = itemView.findViewById(R.id.tvCatBreed);
            tvCatAge = itemView.findViewById(R.id.tvCatAge);
            tvCatIntro = itemView.findViewById(R.id.tvCatIntro);
        }

        void bind(CatProfile profile) {
            imgCatAvatar.setImageResource(profile.getAvatarResId());
            tvCatName.setText(profile.getName());
            tvCatBreed.setText(profile.getBreed());
            tvCatAge.setText(profile.getAge());
            tvCatIntro.setText(profile.getIntro());
        }
    }
}
