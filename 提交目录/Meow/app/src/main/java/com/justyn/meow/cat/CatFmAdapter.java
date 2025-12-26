package com.justyn.meow.cat;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.justyn.meow.R;

import java.util.List;

/**
 * 喵音 FM 列表适配器：
 * 负责渲染音频条目与“添加喵音”入口，并把交互回调给 Activity。
 */
public class CatFmAdapter extends RecyclerView.Adapter<CatFmAdapter.FmViewHolder> {

    /**
     * 列表交互回调
     */
    public interface Listener {
        void onAddClicked();

        void onPlayClicked(FmTrack track, int position);

        void onItemLongPressed(FmTrack track);
    }

    // 音频列表条目数据
    private List<FmTrack> data;
    // 由 Activity 传进来的点击回调
    private final Listener listener;

    // 当前正在播放的 position（默认 NO_POSITION = -1，表示没有正在播放）
    private int playingPosition = RecyclerView.NO_POSITION;
    // 当前播放状态（决定按钮显示“暂停/继续”）
    private boolean isPlaying;

    /**
     * 构造适配器。
     *
     * @param data     列表数据
     * @param listener 交互回调
     */
    public CatFmAdapter(List<FmTrack> data, Listener listener) {
        this.data = data;
        this.listener = listener;
    }

    /**
     * 替换数据并刷新列表。
     */
    @SuppressLint("NotifyDataSetChanged")
    public void submitList(List<FmTrack> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }

    /**
     * 由 Activity 在“播放 / 停止某一条语音”之后调用，
     * 用来刷新列表里的「▶ 播放 / ⏸ 暂停」按钮状态。
     * 如果传 RecyclerView.NO_POSITION，表示没有任何一条在播放
     */
    @SuppressLint("NotifyDataSetChanged")
    public void updatePlayingPosition(int position) {
        // 默认认为正在播放，兼容旧调用
        updatePlayingState(position, position != RecyclerView.NO_POSITION);
    }

    /**
     * 同时更新当前播放的下标以及是否处于播放状态（用于展示「暂停 / 继续」文案）
     */
    @SuppressLint("NotifyDataSetChanged")
    public void updatePlayingState(int position, boolean isPlaying) {
        this.playingPosition = position;
        this.isPlaying = isPlaying;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 创建每一行的 View 使用 item_fm_track.xml
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fm_track, parent, false);
        return new FmViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FmViewHolder holder, int position) {
        // 绑定数据
        FmTrack track = data.get(position);
        if (track.isAddEntry()) {
            holder.bindAsAddEntry();

            holder.btnPlayPause.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddClicked();
                }
            });
            holder.cardTrack.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddClicked();
                }
            });
            holder.cardTrack.setOnLongClickListener(v -> true);
            return;
        }

        // 判断这一条是不是当前正在播放的那一条
        boolean isPlaying = (position == playingPosition);

        // 把数据和“是否正在播放”的状态一并交给 ViewHolder
        holder.bind(track, isPlaying, isPlaying && this.isPlaying);

        // 播放按钮点击事件
        holder.btnPlayPause.setOnClickListener(v -> {
            if (listener != null) {
                // 再拿一次 adapterPosition，避免下标过期
                int adapterPos = holder.getBindingAdapterPosition();
                if (adapterPos != RecyclerView.NO_POSITION) {
                    // 把当前条目的 track + 位置抛给 Activity
                    listener.onPlayClicked(track, adapterPos);
                }
            }
        });

        // 整个卡片点击：与之前一致（点击条目播放/暂停）
        holder.cardTrack.setOnClickListener(v -> {
            if (listener != null) {
                int adapterPos = holder.getBindingAdapterPosition();
                if (adapterPos != RecyclerView.NO_POSITION) {
                    listener.onPlayClicked(track, adapterPos);
                }
            }
        });

        // 长按：弹出操作（编辑/删除）
        holder.cardTrack.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onItemLongPressed(track);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    /**
     * 单条 item 的 ViewHolder，负责：
     * - 找到对应的控件
     * - 把数据填进去
     * - 根据 isPlaying 修改按钮文案
     */
    static class FmViewHolder extends RecyclerView.ViewHolder {

        MaterialCardView cardTrack;    // 整个卡片
        TextView tvTrackTitle;         // 标题：音频名称
        TextView tvTrackSubtitle;      // 描述
        MaterialButton btnPlayPause;   // 播放 / 暂停按钮

        FmViewHolder(@NonNull View itemView) {
            super(itemView);
            cardTrack = itemView.findViewById(R.id.cardTrack);
            tvTrackTitle = itemView.findViewById(R.id.tvTrackTitle);
            tvTrackSubtitle = itemView.findViewById(R.id.tvTrackSubtitle);
            btnPlayPause = itemView.findViewById(R.id.btnPlayPause);
        }

        /**
         * 绑定每一条数据，并根据 isPlaying 决定按钮文本
         */
        void bind(FmTrack track, boolean isCurrentPlaying, boolean isActuallyPlaying) {
            // 设置标题 / 副标题
            tvTrackTitle.setText(track.getTitle());
            tvTrackSubtitle.setText(track.getSubtitle());

            // 根据是否正在播放切换按钮文案
            // 这里只改文案，真正的播放 / 暂停逻辑在 Activity 里
            if (!isCurrentPlaying) {
                btnPlayPause.setText("▶ 播放");
            } else if (isActuallyPlaying) {
                btnPlayPause.setText("⏸ 暂停");
            } else {
                btnPlayPause.setText("▶ 继续");
            }
        }

        /**
         * 绑定“添加喵音”入口样式。
         */
        void bindAsAddEntry() {
            tvTrackTitle.setText("添加喵音");
            tvTrackSubtitle.setText("长按可选择删除、编辑选项～");
            btnPlayPause.setText("＋ 添加");
        }
    }
}
