package com.justyn.meow.cat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.justyn.meow.R;

import java.util.List;

/**
 * 猫图墙列表适配器：
 * 将 CatPic 数据绑定到网格 item 布局中。
 */
public class CatPicAdapter extends RecyclerView.Adapter<CatPicAdapter.CatPicViewHolder> {

    // 图片列表数据源
    private final List<CatPic> data;

    /**
     * 构造适配器。
     *
     * @param data 猫图数据列表
     */
    public CatPicAdapter(List<CatPic> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public CatPicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 加载 item 布局
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cat_pic, parent, false);
        return new CatPicViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CatPicViewHolder holder, int position) {
        // 根据位置取出数据并绑定到 ViewHolder
        CatPic pic = data.get(position);
        holder.bind(pic);
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    /**
     * 猫图 ViewHolder：负责缓存控件引用并绑定数据。
     */
    static class CatPicViewHolder extends RecyclerView.ViewHolder {

        // 图片控件
        ImageView imgCat;
        // 标题文本
        TextView tvCatName;

        CatPicViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCat = itemView.findViewById(R.id.imgCat);
            tvCatName = itemView.findViewById(R.id.tvCatName);
        }

        /**
         * 绑定图片与名称，同时注册点击提示。
         */
        void bind(CatPic pic) {
            imgCat.setImageResource(pic.getImageResId());
            tvCatName.setText(pic.getName());

            // 点一下弹 Toast，说“正在云吸 xxx”
            itemView.setOnClickListener(v -> {
                String text = "正在云吸 " + pic.getName() + " …";
                Toast.makeText(v.getContext(), text, Toast.LENGTH_SHORT).show();
            });
        }
    }
}
