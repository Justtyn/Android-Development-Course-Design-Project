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

    // 列表数据
    private List<CatProfile> data;

    /**
     * 列表交互回调
     */
    public interface Listener {
        void onAddClicked();

        void onItemClicked(CatProfile profile);

        void onItemLongPressed(CatProfile profile);
    }

    // 交互回调
    private final Listener listener;

    /**
     * 构造适配器。
     *
     * @param data     初始数据
     * @param listener 交互回调
     */
    public CatProfileAdapter(List<CatProfile> data, Listener listener) {
        this.data = data;
        this.listener = listener;
    }

    /**
     * 替换列表数据并刷新。
     */
    public void submitList(List<CatProfile> newData) {
        this.data = newData;
        notifyDataSetChanged();
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
        if (profile.isAddEntry()) {
            holder.bindAsAddEntry();
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddClicked();
                }
            });
            holder.itemView.setOnLongClickListener(v -> true);
            return;
        }

        holder.bind(profile);

        // 单击：保留原来的点击反馈（Toast 由 Activity 处理）
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClicked(profile);
            }
        });

        // 长按：弹出操作（编辑/删除）
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onItemLongPressed(profile);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    /**
     * 单条 item 的 ViewHolder，负责视图缓存与数据绑定。
     */
    static class CatViewHolder extends RecyclerView.ViewHolder {

        // 头像
        ImageView imgCatAvatar;
        // 名称
        TextView tvCatName;
        // 品种/性格
        TextView tvCatBreed;
        // 年龄
        TextView tvCatAge;
        // 简介
        TextView tvCatIntro;

        CatViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCatAvatar = itemView.findViewById(R.id.imgCatAvatar);
            tvCatName = itemView.findViewById(R.id.tvCatName);
            tvCatBreed = itemView.findViewById(R.id.tvCatBreed);
            tvCatAge = itemView.findViewById(R.id.tvCatAge);
            tvCatIntro = itemView.findViewById(R.id.tvCatIntro);
        }

        /**
         * 绑定普通猫咪档案数据。
         */
        void bind(CatProfile profile) {
            imgCatAvatar.setImageURI(null);
            if (profile.getAvatarUri() != null) {
                imgCatAvatar.setImageURI(android.net.Uri.parse(profile.getAvatarUri()));
            } else {
                imgCatAvatar.setImageResource(profile.getAvatarResId());
            }
            tvCatName.setText(profile.getName());
            tvCatBreed.setText(profile.getBreed());
            tvCatAge.setText(profile.getAge());
            tvCatIntro.setText(profile.getIntro());
        }

        /**
         * 绑定“添加猫咪”入口样式。
         */
        void bindAsAddEntry() {
            imgCatAvatar.setImageResource(R.drawable.ic_meow_add);
            tvCatName.setText("添加猫咪");
            tvCatBreed.setText("点击新增档案");
            tvCatAge.setText("");
            tvCatIntro.setText("支持长按编辑、删除哦～");
        }
    }
}
