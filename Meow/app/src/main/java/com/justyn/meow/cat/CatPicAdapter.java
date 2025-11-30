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
public class CatPicAdapter extends RecyclerView.Adapter<CatPicAdapter.CatPicViewHolder> {

    private final List<CatPic> data;

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
        CatPic pic = data.get(position);
        holder.bind(pic);
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    static class CatPicViewHolder extends RecyclerView.ViewHolder {

        ImageView imgCat;
        TextView tvCatName;

        CatPicViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCat = itemView.findViewById(R.id.imgCat);
            tvCatName = itemView.findViewById(R.id.tvCatName);
        }

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
