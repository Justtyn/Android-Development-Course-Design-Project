package com.justyn.meow.cat;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.justyn.meow.R;

import java.util.ArrayList;
import java.util.List;

public class CatFmActivity extends AppCompatActivity {

    // 音频列表的适配器
    private CatFmAdapter adapter;

    // 真正负责播放音频的 MediaPlayer
    private MediaPlayer mediaPlayer;

    // 当前正在播放的列表下标（-1 / NO_POSITION 表示没有在播）
    private int currentPlayingPosition = RecyclerView.NO_POSITION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 开启 EdgeToEdge，让内容可以延伸到状态栏 / 导航栏区域
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cat_fm);

        // 1. 找到 RecyclerView，并设置垂直线性布局
        RecyclerView rvTracks = findViewById(R.id.rvTracks);
        rvTracks.setLayoutManager(new LinearLayoutManager(this));

        // 2. 构造 FM 音频列表（使用本地 raw 资源）
        List<FmTrack> trackList = buildTrackList();

        // 3. 创建适配器，并把「点击某条播放」的回调交给 handlePlayClick
        adapter = new CatFmAdapter(trackList, this::handlePlayClick);
        rvTracks.setAdapter(adapter);
    }

    /**
     * 构造本地音频列表
     */
    private List<FmTrack> buildTrackList() {
        List<FmTrack> list = new ArrayList<>();

        list.add(new FmTrack("if_you", "柔和节奏里的一句呢喃，像在耳边轻声问 候。", R.raw.fm_if_you));
        list.add(new FmTrack("no基米", "俏皮的拒绝里带点幽默，轻轻摇头也能上头。", R.raw.fm_no_ji_mi));
        list.add(new FmTrack("不得不哈", "欢快的旋律里藏着停不下来的笑意。", R.raw.fm_bu_de_bu_ha));
        list.add(new FmTrack("不再曼波", "跳脱出舞池的束缚，转身就是新的律动。", R.raw.fm_bu_zai_man_bo));
        list.add(new FmTrack("出哈", "一声清爽的招呼，像打开新篇章的开场白。", R.raw.fm_chu_ha));
        list.add(new FmTrack("打火基", "火花点亮节拍，热烈氛围瞬间燃起。", R.raw.fm_da_huo_ji));
        list.add(new FmTrack("粉红色的基米", "梦幻粉色滤镜下的轻快冒险，甜而不腻。", R.raw.fm_fen_hong_se_de_ji_mi));
        list.add(new FmTrack("关山哈", "穿过关山的风声，带来一阵爽朗的笑。", R.raw.fm_guan_shan_ha));
        list.add(new FmTrack("哈基米起床", "慵懒清晨的伸展，伴随轻快的叫醒曲。", R.raw.fm_ha_ji_mi_qi_chuang));
        list.add(new FmTrack("哈沫", "泡沫破裂的瞬间，留下一抹轻盈的愉悦。", R.raw.fm_ha_mo));
        list.add(new FmTrack("基米_to_the_moon", "逐月的步伐，伴着电子心跳一路上升。", R.raw.fm_ji_mi_to_the_moon));
        list.add(new FmTrack("基米说", "像讲故事般的旋律，每句都带点俏皮。", R.raw.fm_ji_mi_shuo));
        list.add(new FmTrack("蓝莲哈", "蓝色莲花的清凉与一声爽朗的笑交织。", R.raw.fm_lan_lian_ha));
        list.add(new FmTrack("两个哈基米", "双人合声的默契，笑点与节拍同步。", R.raw.fm_liang_ge_ha_ji_mi));
        list.add(new FmTrack("舌尖上的哈基米", "滋味与律动交织，像是在嘴里跳舞。", R.raw.fm_she_jian_shang_de_ha_ji_mi));
        list.add(new FmTrack("跳楼基", "急速下坠的心跳感，被节奏接住的瞬间。", R.raw.fm_tiao_lou_ji));
        list.add(new FmTrack("往事只能哈基", "回忆里的一声笑，将旧时光轻轻带回。", R.raw.fm_wang_shi_zhi_neng_ha_ji));
        list.add(new FmTrack("唯一哈基米", "独一份的旋律，像专属的密语。", R.raw.fm_wei_yi_ha_ji_mi));
        list.add(new FmTrack("悬疑哈基米", "神秘的节拍里藏着俏皮的伏笔。", R.raw.fm_xuan_yi_ha_ji_mi));
        list.add(new FmTrack("最后一哈", "收尾的一声轻笑，让整段旅程圆满。", R.raw.fm_zui_hou_yi_ha));
        return list;
    }

    /**
     * 处理每一条「播放 / 暂停」点击逻辑
     * <p>
     * 规则：
     * 1. 如果点击的是“当前正在播放”的同一条：
     * → 当作「停止播放」，停止 MediaPlayer，重置 UI
     * 2. 如果点击的是其他条目：
     * → 先停掉之前的音频，释放 MediaPlayer
     * → 为新条目创建 MediaPlayer，开始播放
     * → 通知 Adapter 更新“哪一条正在播放”
     *
     * @param track    被点击的音频条目
     * @param position 在列表中的位置
     */
    private void handlePlayClick(FmTrack track, int position) {
        // 情况一：点击的是当前正在播放的那一条 → 停止播放
        if (mediaPlayer != null && currentPlayingPosition == position) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            // 释放播放器资源
            releasePlayer();
            // 状态改为“没有任何一条在播放”
            currentPlayingPosition = RecyclerView.NO_POSITION;
            // 通知适配器刷新，所有按钮恢复为「▶ 播放」
            adapter.updatePlayingPosition(RecyclerView.NO_POSITION);
            return;
        }

        // 情况二：点击的是其他条目
        // 如果之前已经有一个 MediaPlayer 存在（说明之前有一条在播），先释放掉
        if (mediaPlayer != null) {
            releasePlayer();
        }

        // 为当前点击的音频创建新的 MediaPlayer
        mediaPlayer = MediaPlayer.create(this, track.getResId());
        if (mediaPlayer == null) {
            Toast.makeText(this, "喵～音频初始化失败了", Toast.LENGTH_SHORT).show();
            return;
        }

        // 更新当前正在播放的 position
        currentPlayingPosition = position;
        // 通知 Adapter：哪一条要高亮为“正在播放”（按钮显示「⏸ 暂停」）
        adapter.updatePlayingPosition(position);

        // 播放完成后的回调：
        // 自动把状态重置为“无播放”，并刷新 UI
        mediaPlayer.setOnCompletionListener(mp -> {
            currentPlayingPosition = RecyclerView.NO_POSITION;
            adapter.updatePlayingPosition(RecyclerView.NO_POSITION);
            releasePlayer();
        });

        // 开始播放音频
        mediaPlayer.start();
    }

    /**
     * 安全释放 MediaPlayer 资源，防止内存泄漏 / 占用系统资源
     */
    private void releasePlayer() {
        if (mediaPlayer != null) {
            try {
                // reset 不是必须，但有些机型上更稳妥
                mediaPlayer.reset();
            } catch (Exception ignored) {
                // 防御性写法：即使 reset 抛异常也不要崩
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 页面不可见（切后台 / 退出页面）时：
        // 1. 停止播放
        // 2. 重置当前播放标记
        // 3. 通知列表恢复按钮文案
        currentPlayingPosition = RecyclerView.NO_POSITION;
        if (adapter != null) {
            adapter.updatePlayingPosition(RecyclerView.NO_POSITION);
        }
        releasePlayer();
    }
}
