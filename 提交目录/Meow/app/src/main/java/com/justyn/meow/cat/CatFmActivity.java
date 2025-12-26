package com.justyn.meow.cat;

import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.justyn.meow.R;
import com.justyn.meow.data.MeowDbHelper;
import com.justyn.meow.util.MeowPreferences;

import java.util.ArrayList;
import java.util.List;

/**
 * 喵音 FM 页面：管理音频列表与播放控制。
 * <p>
 * 主要职责：
 * - 列表增删改与搜索过滤
 * - MediaPlayer 播放/暂停、快进/快退
 * - 进度条与时间展示的同步刷新
 * </p>
 */
public class CatFmActivity extends AppCompatActivity {

    // 音频列表的适配器（负责渲染每一条音频卡片）
    private CatFmAdapter adapter;

    // 数据库操作类：用于读写音频列表
    private MeowDbHelper dbHelper;
    private String currentUsername;
    // 搜索输入框：实时过滤列表
    private TextInputEditText etSearch;

    // 临时回调：用于接收系统文件选择器返回的 Uri
    private interface UriReceiver {
        void onPicked(Uri uri);
    }

    // 系统文件选择器：选择音频文件
    private ActivityResultLauncher<String[]> audioPickerLauncher;
    // 保存当前选择动作的回调（避免多处共用时丢失）
    private UriReceiver pendingAudioReceiver;

    // 真正负责播放音频的 MediaPlayer（一次只播放一条）
    private MediaPlayer mediaPlayer;

    // 底部播放控制区控件
    private SeekBar seekBar;
    private TextView tvCurrentTime;
    private TextView tvTotalTime;
    private MaterialButton btnPlayPauseControl;
    private MaterialButton btnForward;
    private MaterialButton btnRewind;

    // 定时更新进度的 Handler（主线程）
    private final Handler progressHandler = new Handler(Looper.getMainLooper());

    // 用户是否正在拖动进度条（用于暂停自动刷新）
    private boolean isUserSeeking = false;

    // 快进/快退步长：10 秒
    private static final int SKIP_STEP_MS = 10_000;

    // 定时刷新进度条与当前时间
    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                if (!isUserSeeking) {
                    int positionMs = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(positionMs);
                    tvCurrentTime.setText(formatTime(positionMs));
                }
                progressHandler.postDelayed(this, 500);
            }
        }
    };

    // 当前正在播放的列表下标（-1 / NO_POSITION 表示没有在播）
    private int currentPlayingPosition = RecyclerView.NO_POSITION;
    // 当前正在播放的音频 ID（用于删除时判断）
    private long currentPlayingTrackId = -1;

    /**
     * 初始化列表、播放器控件与搜索。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 开启 EdgeToEdge，让内容可以延伸到状态栏 / 导航栏区域
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cat_fm);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // 初始化 DB 与搜索框
        dbHelper = new MeowDbHelper(this);
        currentUsername = MeowPreferences.getUsername(this);
        etSearch = findViewById(R.id.etSearch);

        // 注册音频文件选择器，获取 Uri 后持久化读权限
        audioPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri == null) {
                        return;
                    }
                    persistReadPermission(uri);
                    if (pendingAudioReceiver != null) {
                        pendingAudioReceiver.onPicked(uri);
                    }
                }
        );

        // 绑定播放器控件
        seekBar = findViewById(R.id.seekBar);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        btnPlayPauseControl = findViewById(R.id.btnPlayPauseControl);
        btnForward = findViewById(R.id.btnForward);
        btnRewind = findViewById(R.id.btnRewind);

        // 初始显示 00:00
        resetTimeUi();

        // 1. 找到 RecyclerView，并设置垂直线性布局
        RecyclerView rvTracks = findViewById(R.id.rvTracks);
        rvTracks.setLayoutManager(new LinearLayoutManager(this));

        // 首次进入时写入默认音频
        seedDefaultTracksIfNeeded();

        // 构造适配器，并绑定各项点击事件
        adapter = new CatFmAdapter(new ArrayList<>(), new CatFmAdapter.Listener() {
            @Override
            public void onAddClicked() {
                showAddDialog();
            }

            @Override
            public void onPlayClicked(FmTrack track, int position) {
                handlePlayClick(track, position);
            }

            @Override
            public void onItemLongPressed(FmTrack track) {
                showActionsDialog(track);
            }
        });
        rvTracks.setAdapter(adapter);

        // 首次加载列表
        reloadList(null);

        // 搜索框实时过滤列表
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                reloadList(s == null ? null : s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });

        // 初始化播放控制逻辑，默认禁用
        setupPlaybackControls();
        setPlaybackControlsEnabled(false);
    }

    /**
     * 若数据库为空则写入本地默认音频，避免每次启动重复写入。
     */
    private void seedDefaultTracksIfNeeded() {
        if (MeowPreferences.isFmSeeded(this, currentUsername)) {
            return;
        }
        if (dbHelper.hasAnyFmTracks(currentUsername)) {
            MeowPreferences.markFmSeeded(this, currentUsername);
            return;
        }
        List<FmTrack> defaults = buildTrackList();
        for (FmTrack track : defaults) {
            dbHelper.insertFmTrack(currentUsername, track.getTitle(), track.getSubtitle(), track.getResId(), null);
        }
        MeowPreferences.markFmSeeded(this, currentUsername);
    }

    /**
     * 根据关键词刷新列表，并重置当前播放状态。
     *
     * @param titleQuery 标题关键字，空表示不过滤
     */
    private void reloadList(String titleQuery) {
        releasePlayer();
        resetPlaybackUi();

        List<FmTrack> tracks = dbHelper.queryFmTracks(currentUsername, titleQuery);
        tracks.add(FmTrack.addEntry());
        adapter.submitList(tracks);
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
     * 初始化底部播放控制区域
     */
    private void setupPlaybackControls() {
        // 播放/暂停
        btnPlayPauseControl.setOnClickListener(v -> togglePlayPause());
        // 快进与快退
        btnForward.setOnClickListener(v -> seekBy(SKIP_STEP_MS));
        btnRewind.setOnClickListener(v -> seekBy(-SKIP_STEP_MS));

        // 进度条拖动监听：只在用户手动拖动时更新显示
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    tvCurrentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                } else {
                    seekBar.setProgress(0);
                }
                isUserSeeking = false;
            }
        });
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
        if (track == null || track.isAddEntry()) {
            return;
        }
        // 情况一：点击的是当前正在播放的 → 停止播放
        if (mediaPlayer != null && currentPlayingPosition == position) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            releasePlayer();
            resetPlaybackUi();
            return;
        }

        // 情况二：点击的是其他条目 如果之前已经有 MediaPlayer 存在说明在播，先释放
        if (mediaPlayer != null) {
            releasePlayer();
        }

        // 为当前点击的音频创建新的 MediaPlayer
        if (track.getAudioUri() != null) {
            mediaPlayer = MediaPlayer.create(this, Uri.parse(track.getAudioUri()));
        } else {
            mediaPlayer = MediaPlayer.create(this, track.getResId());
        }
        if (mediaPlayer == null) {
            Toast.makeText(this, "喵～音频初始化失败了", Toast.LENGTH_SHORT).show();
            resetPlaybackUi();
            return;
        }

        // 更新当前正在播放的 position
        currentPlayingPosition = position;
        currentPlayingTrackId = track.getId();
        // 通知 Adapter：哪一条要高亮为“正在播放”（按钮显示「⏸ 暂停」）
        adapter.updatePlayingState(position, true);
        prepareSeekBarForMediaPlayer();
        setPlaybackControlsEnabled(true);
        btnPlayPauseControl.setText("⏸ 暂停");

        // 播放完成后的回调：
        // 自动把状态重置为“无播放”，并刷新 UI
        mediaPlayer.setOnCompletionListener(mp -> {
            releasePlayer();
            resetPlaybackUi();
        });

        // 开始播放音频
        mediaPlayer.start();
        startProgressUpdates();
    }

    /**
     * 底部播放/暂停按钮
     */
    private void togglePlayPause() {
        // 未选择任何音频，直接提示
        if (mediaPlayer == null) {
            Toast.makeText(this, "先从列表里选一条喵音播放吧～", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mediaPlayer.isPlaying()) {
            // 暂停播放并更新按钮状态
            mediaPlayer.pause();
            adapter.updatePlayingState(currentPlayingPosition, false);
            btnPlayPauseControl.setText("▶ 继续");
            stopProgressUpdates();
        } else {
            // 继续播放并恢复进度刷新
            mediaPlayer.start();
            adapter.updatePlayingState(currentPlayingPosition, true);
            btnPlayPauseControl.setText("⏸ 暂停");
            startProgressUpdates();
        }
    }

    /**
     * 快进 / 快退
     */
    private void seekBy(int deltaMs) {
        if (mediaPlayer == null) {
            return;
        }
        // 计算目标时间，限制在 0~duration 范围内
        int target = mediaPlayer.getCurrentPosition() + deltaMs;
        target = Math.max(0, Math.min(target, mediaPlayer.getDuration()));
        mediaPlayer.seekTo(target);
        seekBar.setProgress(target);
        tvCurrentTime.setText(formatTime(target));
    }

    /**
     * 设置进度条最大值与初始值
     */
    private void prepareSeekBarForMediaPlayer() {
        if (mediaPlayer == null) {
            return;
        }
        // 以音频总时长作为最大进度
        seekBar.setMax(mediaPlayer.getDuration());
        seekBar.setProgress(0);
        tvTotalTime.setText(formatTime(mediaPlayer.getDuration()));
        tvCurrentTime.setText(formatTime(0));
    }

    /**
     * 开始定时刷新播放进度。
     */
    private void startProgressUpdates() {
        stopProgressUpdates();
        progressHandler.post(progressRunnable);
    }

    /**
     * 停止定时刷新，避免泄漏与重复回调。
     */
    private void stopProgressUpdates() {
        progressHandler.removeCallbacks(progressRunnable);
    }

    /**
     * 重置进度条。
     */
    private void resetSeekBar() {
        seekBar.setProgress(0);
        seekBar.setMax(0);
    }

    /**
     * 重置时间显示。
     */
    private void resetTimeUi() {
        tvCurrentTime.setText(formatTime(0));
        tvTotalTime.setText(formatTime(0));
    }

    /**
     * 统一控制底部播放控件可用状态。
     */
    private void setPlaybackControlsEnabled(boolean enabled) {
        seekBar.setEnabled(enabled);
        btnPlayPauseControl.setEnabled(enabled);
        btnForward.setEnabled(enabled);
        btnRewind.setEnabled(enabled);
    }

    /**
     * 重置播放状态与按钮 UI。
     */
    private void resetPlaybackUi() {
        currentPlayingPosition = RecyclerView.NO_POSITION;
        currentPlayingTrackId = -1;
        if (adapter != null) {
            adapter.updatePlayingState(RecyclerView.NO_POSITION, false);
        }
        resetSeekBar();
        resetTimeUi();
        setPlaybackControlsEnabled(false);
        btnPlayPauseControl.setText("▶ 播放");
    }

    /**
     * 安全释放 MediaPlayer 资源，防止内存泄漏 / 占用系统资源
     */
    private void releasePlayer() {
        // 先停掉进度刷新，避免释放后仍回调
        stopProgressUpdates();
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
        // 3. 列表恢复按钮文案
        releasePlayer();
        resetPlaybackUi();
    }

    /**
     * 弹出新增音频对话框，支持选择本地音频文件并写入数据库。
     */
    private void showAddDialog() {
        // 弹出“新增喵音”对话框
        android.view.View view = android.view.LayoutInflater.from(this).inflate(R.layout.dialog_fm_track, null);
        TextInputEditText etTitle = view.findViewById(R.id.etTitle);
        TextInputEditText etSubtitle = view.findViewById(R.id.etSubtitle);
        com.google.android.material.button.MaterialButton btnPickAudio = view.findViewById(R.id.btnPickAudio);
        android.widget.TextView tvAudioSelected = view.findViewById(R.id.tvAudioSelected);

        final Uri[] selectedAudioUri = new Uri[]{null};

        btnPickAudio.setOnClickListener(v -> {
            // 选择音频后更新提示文案
            pendingAudioReceiver = uri -> {
                selectedAudioUri[0] = uri;
                tvAudioSelected.setText("已选择：" + getDisplayName(uri));
            };
            audioPickerLauncher.launch(new String[]{"audio/*"});
        });

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle("新增喵音")
                .setView(view)
                .setNegativeButton("取消", (d, which) -> {
                })
                .setPositiveButton("保存", null);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(d -> pendingAudioReceiver = null);
        dialog.show();

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            // 校验标题与音频文件
            String title = safeText(etTitle);
            if (title.isEmpty()) {
                Toast.makeText(this, "标题不能为空喵～", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedAudioUri[0] == null) {
                Toast.makeText(this, "请先选择音频喵～", Toast.LENGTH_SHORT).show();
                return;
            }
            String subtitle = safeText(etSubtitle);
            // 写入数据库并刷新列表
            dbHelper.insertFmTrack(currentUsername, title, subtitle, null, selectedAudioUri[0].toString());
            reloadList(etSearch.getText() == null ? null : etSearch.getText().toString());
            dialog.dismiss();
        });
    }

    /**
     * 弹出编辑音频对话框（仅允许修改标题/副标题）。
     */
    private void showEditDialog(FmTrack track) {
        // 弹出“编辑喵音”对话框（仅允许修改标题/副标题）
        android.view.View view = android.view.LayoutInflater.from(this).inflate(R.layout.dialog_fm_track, null);
        TextInputEditText etTitle = view.findViewById(R.id.etTitle);
        TextInputEditText etSubtitle = view.findViewById(R.id.etSubtitle);
        com.google.android.material.button.MaterialButton btnPickAudio = view.findViewById(R.id.btnPickAudio);
        android.widget.TextView tvAudioSelected = view.findViewById(R.id.tvAudioSelected);

        etTitle.setText(track.getTitle());
        etSubtitle.setText(track.getSubtitle());

        btnPickAudio.setEnabled(false);
        btnPickAudio.setText("咪音不可更换");
        if (track.getAudioUri() != null) {
            tvAudioSelected.setText("音频已锁定：" + getDisplayName(Uri.parse(track.getAudioUri())));
        } else {
            tvAudioSelected.setText("该咪音不可更换哦～");
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle("编辑喵音")
                .setView(view)
                .setNegativeButton("取消", (d, which) -> {
                })
                .setPositiveButton("保存", null);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            // 校验标题并更新数据库
            String title = safeText(etTitle);
            if (title.isEmpty()) {
                Toast.makeText(this, "标题不能为空喵～", Toast.LENGTH_SHORT).show();
                return;
            }
            String subtitle = safeText(etSubtitle);
            dbHelper.updateFmTrack(track.getId(), title, subtitle);
            reloadList(etSearch.getText() == null ? null : etSearch.getText().toString());
            dialog.dismiss();
        });
    }

    /**
     * 弹出删除确认对话框并执行删除。
     */
    private void showDeleteDialog(FmTrack track) {
        // 删除确认弹窗
        new MaterialAlertDialogBuilder(this)
                .setTitle("删除")
                .setMessage("确定删除「" + track.getTitle() + "」吗？")
                .setNegativeButton("取消", (d, which) -> {
                })
                .setPositiveButton("删除", (d, which) -> {
                    // 删除前如果正在播放同一条，先停止
                    if (currentPlayingTrackId == track.getId()) {
                        releasePlayer();
                        resetPlaybackUi();
                    }
                    dbHelper.deleteFmTrack(track.getId());
                    reloadList(etSearch.getText() == null ? null : etSearch.getText().toString());
                    Toast.makeText(this, "已删除喵～", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    /**
     * 长按条目后的操作入口：编辑 / 删除。
     */
    private void showActionsDialog(FmTrack track) {
        // 长按后操作弹窗：编辑 / 删除
        String[] items = new String[]{"编辑", "删除"};
        new MaterialAlertDialogBuilder(this)
                .setTitle(track.getTitle())
                .setItems(items, (dialog, which) -> {
                    if (which == 0) {
                        showEditDialog(track);
                    } else if (which == 1) {
                        showDeleteDialog(track);
                    }
                })
                .setNegativeButton("取消", (d, w) -> {
                })
                .show();
    }

    /**
     * 安全读取输入框文本，避免空指针。
     */
    private static String safeText(TextInputEditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    /**
     * 持久化读取权限，避免下次启动无法访问。
     */
    private void persistReadPermission(Uri uri) {
        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException ignored) {
        }
    }

    /**
     * 获取音频文件展示名（失败则回退到 Uri 字符串）。
     */
    private String getDisplayName(Uri uri) {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    return cursor.getString(index);
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return uri.toString();
    }

    /**
     * 把毫秒格式化成 mm:ss。
     */
    private static String formatTime(int ms) {
        if (ms < 0) {
            ms = 0;
        }
        int totalSeconds = ms / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(java.util.Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }
}
