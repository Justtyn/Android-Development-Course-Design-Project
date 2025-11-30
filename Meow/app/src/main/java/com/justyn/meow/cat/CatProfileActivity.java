package com.justyn.meow.cat;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.justyn.meow.R;

import java.util.ArrayList;
import java.util.List;

public class CatProfileActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cat_profile);

        RecyclerView rvCatList = findViewById(R.id.rvTracks);
        rvCatList.setLayoutManager(new LinearLayoutManager(this));

        // 用本地构造的猫咪列表数据
        List<CatProfile> profiles = buildLocalCatProfiles();

        // 适配器 + 简单点击事件（先 Toast，后面你想扩展详情页再说）
        CatProfileAdapter adapter = new CatProfileAdapter(profiles, (profile, position) -> {
            Toast.makeText(
                    CatProfileActivity.this,
                    "喵～你点了：「" + profile.getName() + "」",
                    Toast.LENGTH_SHORT
            ).show();
        });

        rvCatList.setAdapter(adapter);
    }

    private List<CatProfile> buildLocalCatProfiles() {
        List<CatProfile> list = new ArrayList<>();

        list.add(new CatProfile("雪团", "白猫", "2 岁", "安静黏人，最爱晒太阳的小白团子。",
                R.drawable.bai_mao));
        list.add(new CatProfile("丝绒", "波斯猫", "4 岁", "高贵慵懒，喜欢待在安静的角落躺平。",
                R.drawable.bo_si_mao));
        list.add(new CatProfile("小博士", "博学猫", "3 岁", "机灵好奇，总在翻书架和观察人类。",
                R.drawable.bo_xue_mao));
        list.add(new CatProfile("奶茶", "布偶猫", "2 岁", "软绵绵地任人抱，黏人又温柔。",
                R.drawable.bu_ou_mao));
        list.add(new CatProfile("夜影", "黑猫", "3 岁", "动作敏捷，夜里巡逻像个小守护。",
                R.drawable.hei_mao));
        list.add(new CatProfile("金宝", "黄猫", "4 岁", "性格阳光，爱蹭人讨摸摸。",
                R.drawable.huang_mao));
        list.add(new CatProfile("柚子", "橘猫", "5 岁", "饭点准时，撒娇卖萌第一名。",
                R.drawable.ju_mao));
        list.add(new CatProfile("雾蓝", "蓝猫", "2 岁", "沉稳安静，偶尔发呆看窗外。",
                R.drawable.lan_mao));
        list.add(new CatProfile("狸狸", "狸花猫", "3 岁", "机警灵活，捕虫和玩逗猫棒都很带劲。",
                R.drawable.li_hua_mao));
        list.add(new CatProfile("小喵", "猫", "1 岁", "活力满满，对一切都充满好奇。",
                R.drawable.mao));
        list.add(new CatProfile("灰爵", "美短猫", "2 岁", "外向亲人，最爱追逐小球。",
                R.drawable.mei_duan_mao));
        list.add(new CatProfile("笑眯", "眯眯眼猫", "4 岁", "整天眯着眼笑，性格佛系好脾气。",
                R.drawable.mi_mi_yan_mao));
        list.add(new CatProfile("雪松", "缅因猫", "5 岁", "体型大却温柔，像个大哥哥照顾大家。",
                R.drawable.mian_yin_mao));
        list.add(new CatProfile("奶糖", "牛奶猫", "1 岁", "甜甜软软，喜欢蜷在怀里呼噜。",
                R.drawable.niu_nai_mao));
        list.add(new CatProfile("花羽", "三花猫", "3 岁", "独立又机灵，偶尔会撒娇求陪伴。",
                R.drawable.san_hua_mao));
        list.add(new CatProfile("憨豆", "傻猫", "2 岁", "有点迷糊，总闹出可爱的笑话。",
                R.drawable.sha_mao));
        list.add(new CatProfile("阿土", "田园猫", "4 岁", "性格随和，喜欢庭院里晒太阳。",
                R.drawable.tian_yuan_mao));
        list.add(new CatProfile("赛博", "无毛猫", "3 岁", "好奇心爆棚，喜欢贴着人类取暖。",
                R.drawable.wu_mao_mao));
        list.add(new CatProfile("摩卡", "暹罗猫", "2 岁", "爱聊天，声线软糯黏人又聪明。",
                R.drawable.xian_luo_mao));
        list.add(new CatProfile("骑士", "英短猫", "3 岁", "稳重绅士范儿，温顺地陪在身边。",
                R.drawable.ying_duan_mao));

        return list;
    }
}
