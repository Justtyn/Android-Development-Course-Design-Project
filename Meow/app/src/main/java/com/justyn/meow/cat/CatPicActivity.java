package com.justyn.meow.cat;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.justyn.meow.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 猫图墙页面：用网格展示本地猫图集合。
 * <p>
 * 数据来源为本地 drawable 资源，使用 RecyclerView 网格布局展示。
 * </p>
 */
public class CatPicActivity extends AppCompatActivity {

    /**
     * 初始化网格列表与数据源。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cat_pic);

        RecyclerView rvCatPics = findViewById(R.id.rvCatPics);

        // 2 列网格布局
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvCatPics.setLayoutManager(layoutManager);

        // 构造本地猫图数据
        List<CatPic> picList = buildLocalPicList();

        CatPicAdapter adapter = new CatPicAdapter(picList);
        rvCatPics.setAdapter(adapter);
    }

    /**
     * 构造本地猫图清单（图片资源 id + 标题）。
     */
    private List<CatPic> buildLocalPicList() {
        List<CatPic> list = new ArrayList<>();
        list.add(new CatPic(R.drawable.siyue_1, "四月-仰望星空版"));
        list.add(new CatPic(R.drawable.siyue_3, "四月-不知道手去哪儿版"));
        list.add(new CatPic(R.drawable.siyue_4, "四月-写代码版"));
        list.add(new CatPic(R.drawable.siyue_5, "四月-拥过去了版"));
        list.add(new CatPic(R.drawable.siyue_6, "四月"));
        list.add(new CatPic(R.drawable.siyue_7, "四月-喝水呢版"));
        list.add(new CatPic(R.drawable.siyue_8, "四月"));
        list.add(new CatPic(R.drawable.siyue_9, "四月-公主坐版"));
        list.add(new CatPic(R.drawable.siyue_10, "四月-晒太阳版"));
        list.add(new CatPic(R.drawable.siyue_11, "四月-眼睛好大尾巴好长版"));
        list.add(new CatPic(R.drawable.siyue_12, "四月-超绝帅气版"));
        list.add(new CatPic(R.drawable.siyue_13, "四月-差点被一屁股坐死版"));
        list.add(new CatPic(R.drawable.siyue_14, "四月-随地大小睡版"));
        list.add(new CatPic(R.drawable.siyue_15, "四月"));
        list.add(new CatPic(R.drawable.siyue_16, "四月"));
        list.add(new CatPic(R.drawable.siyue_17, "四月-绝美阳光版"));
        list.add(new CatPic(R.drawable.siyue_18, "四月"));
        list.add(new CatPic(R.drawable.siyue_19, "四月-想吃螺狮粉版"));
        list.add(new CatPic(R.drawable.siyue_20, "四月-超级猥琐版"));
        list.add(new CatPic(R.drawable.siyue_21, "四月-想吃樱桃版"));
        list.add(new CatPic(R.drawable.siyue_22, "四月-不想理人版"));
        list.add(new CatPic(R.drawable.siyue_23, "四月-古老的埃及神兽版"));
        list.add(new CatPic(R.drawable.siyue_24, "四月-看蜡笔小新版"));
        list.add(new CatPic(R.drawable.siyue_25, "四月-炒鸡可爱卖萌版"));
        list.add(new CatPic(R.drawable.siyue_26, "四月"));
        list.add(new CatPic(R.drawable.siyue_28, "四月-晒大腚版"));
        list.add(new CatPic(R.drawable.siyue_30, "四月-AI手办版"));
        list.add(new CatPic(R.drawable.siyue_31, "四月-哇去好凶狠版"));
        list.add(new CatPic(R.drawable.siyue_32, "四月-哇去好正常版"));
        list.add(new CatPic(R.drawable.siyue_33, "四月-整生气了版"));
        list.add(new CatPic(R.drawable.siyue_34, "四月-一只眼偷看版"));
        list.add(new CatPic(R.drawable.siyue_35, "四月"));
        list.add(new CatPic(R.drawable.siyue_36, "四月-不知道看什么版"));
        list.add(new CatPic(R.drawable.siyue_37, "四月-一身肥肉版"));
        list.add(new CatPic(R.drawable.siyue_38, "四月"));
        list.add(new CatPic(R.drawable.siyue_39, "四月-被机箱吸住了版"));
        list.add(new CatPic(R.drawable.siyue_40, "四月-瞅我干啥版"));
        return list;
    }
}
