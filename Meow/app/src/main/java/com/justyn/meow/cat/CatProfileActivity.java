package com.justyn.meow.cat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.justyn.meow.R;
import com.justyn.meow.data.MeowDbHelper;
import com.justyn.meow.util.MeowPreferences;

import java.util.ArrayList;
import java.util.List;

/**
 * 猫咪档案页面：展示列表、支持搜索、增删改，并允许选择头像。
 * <p>
 * 主要功能：
 * - 读取/写入本地数据库
 * - 支持标题关键字搜索
 * - 通过系统图片选择器选择头像
 * </p>
 */
public class CatProfileActivity extends AppCompatActivity {

    // 数据库操作类
    private MeowDbHelper dbHelper;
    // 列表适配器
    private CatProfileAdapter adapter;
    // 搜索输入框
    private TextInputEditText etSearch;

    // 临时回调：用于接收系统图片选择器的 Uri
    private interface UriReceiver {
        void onPicked(Uri uri);
    }

    // 图片选择器
    private ActivityResultLauncher<String[]> imagePickerLauncher;
    // 暂存回调（用于弹窗内选择头像后回填）
    private UriReceiver pendingImageReceiver;

    /**
     * 初始化列表、搜索与图片选择器。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cat_profile);

        dbHelper = new MeowDbHelper(this);

        etSearch = findViewById(R.id.etSearch);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri == null) {
                        return;
                    }
                    persistReadPermission(uri);
                    if (pendingImageReceiver != null) {
                        pendingImageReceiver.onPicked(uri);
                    }
                }
        );

        RecyclerView rvCatList = findViewById(R.id.rvTracks);
        rvCatList.setLayoutManager(new LinearLayoutManager(this));

        seedDefaultProfilesIfNeeded();

        adapter = new CatProfileAdapter(new ArrayList<>(), new CatProfileAdapter.Listener() {
            @Override
            public void onAddClicked() {
                showAddDialog();
            }

            @Override
            public void onItemClicked(CatProfile profile) {
                Toast.makeText(
                        CatProfileActivity.this,
                        "喵～你点了：「" + profile.getName() + "」",
                        Toast.LENGTH_SHORT
                ).show();
            }

            @Override
            public void onItemLongPressed(CatProfile profile) {
                showActionsDialog(profile);
            }
        });

        rvCatList.setAdapter(adapter);

        reloadList(null);

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
    }

    /**
     * 初始化默认猫咪档案（仅第一次进入时写入）。
     */
    private void seedDefaultProfilesIfNeeded() {
        if (MeowPreferences.isCatProfileSeeded(this)) {
            return;
        }
        if (dbHelper.hasAnyCatProfiles()) {
            MeowPreferences.markCatProfileSeeded(this);
            return;
        }
        List<CatProfile> defaults = buildLocalCatProfiles();
        for (CatProfile profile : defaults) {
            dbHelper.insertCatProfile(
                    profile.getName(),
                    profile.getAge(),
                    profile.getBreed(),
                    profile.getIntro(),
                    profile.getAvatarResId(),
                    null
            );
        }
        MeowPreferences.markCatProfileSeeded(this);
    }

    /**
     * 读取数据库并刷新列表。
     *
     * @param titleQuery 标题关键字，空表示不过滤
     */
    private void reloadList(String titleQuery) {
        List<CatProfile> profiles = dbHelper.queryCatProfiles(titleQuery);
        profiles.add(CatProfile.addEntry());
        adapter.submitList(profiles);
    }

    /**
     * 弹出新增猫咪档案对话框。
     */
    private void showAddDialog() {
        android.view.View view = android.view.LayoutInflater.from(this).inflate(R.layout.dialog_cat_profile, null);
        android.widget.ImageView imgAvatar = view.findViewById(R.id.imgAvatar);
        com.google.android.material.button.MaterialButton btnPickAvatar = view.findViewById(R.id.btnPickAvatar);
        TextInputEditText etTitle = view.findViewById(R.id.etTitle);
        TextInputEditText etAge = view.findViewById(R.id.etAge);
        TextInputEditText etPersonality = view.findViewById(R.id.etPersonality);
        TextInputEditText etDescription = view.findViewById(R.id.etDescription);

        final Uri[] selectedAvatarUri = new Uri[]{null};

        btnPickAvatar.setOnClickListener(v -> {
            pendingImageReceiver = uri -> {
                selectedAvatarUri[0] = uri;
                imgAvatar.setImageURI(uri);
            };
            imagePickerLauncher.launch(new String[]{"image/*"});
        });

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle("新增猫咪档案")
                .setView(view)
                .setNegativeButton("取消", (d, which) -> {
                })
                .setPositiveButton("保存", null);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(d -> pendingImageReceiver = null);
        dialog.show();

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String title = safeText(etTitle);
            if (title.isEmpty()) {
                Toast.makeText(this, "名称不能为空喵～", Toast.LENGTH_SHORT).show();
                return;
            }
            String age = safeText(etAge);
            String personality = safeText(etPersonality);
            String description = safeText(etDescription);

            Integer avatarResId = null;
            String avatarUri = null;
            if (selectedAvatarUri[0] != null) {
                avatarUri = selectedAvatarUri[0].toString();
            } else {
                avatarResId = R.drawable.logo;
            }

            dbHelper.insertCatProfile(title, age, personality, description, avatarResId, avatarUri);
            reloadList(etSearch.getText() == null ? null : etSearch.getText().toString());
            dialog.dismiss();
        });
    }

    /**
     * 弹出编辑猫咪档案对话框。
     */
    private void showEditDialog(CatProfile profile) {
        android.view.View view = android.view.LayoutInflater.from(this).inflate(R.layout.dialog_cat_profile, null);
        android.widget.ImageView imgAvatar = view.findViewById(R.id.imgAvatar);
        com.google.android.material.button.MaterialButton btnPickAvatar = view.findViewById(R.id.btnPickAvatar);
        TextInputEditText etTitle = view.findViewById(R.id.etTitle);
        TextInputEditText etAge = view.findViewById(R.id.etAge);
        TextInputEditText etPersonality = view.findViewById(R.id.etPersonality);
        TextInputEditText etDescription = view.findViewById(R.id.etDescription);

        etTitle.setText(profile.getName());
        etAge.setText(profile.getAge());
        etPersonality.setText(profile.getBreed());
        etDescription.setText(profile.getIntro());

        if (profile.getAvatarUri() != null) {
            imgAvatar.setImageURI(Uri.parse(profile.getAvatarUri()));
        } else if (profile.getAvatarResId() != 0) {
            imgAvatar.setImageResource(profile.getAvatarResId());
        }

        final Uri[] selectedAvatarUri = new Uri[]{null};

        btnPickAvatar.setOnClickListener(v -> {
            pendingImageReceiver = uri -> {
                selectedAvatarUri[0] = uri;
                imgAvatar.setImageURI(uri);
            };
            imagePickerLauncher.launch(new String[]{"image/*"});
        });

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle("编辑猫咪档案")
                .setView(view)
                .setNegativeButton("取消", (d, which) -> {
                })
                .setPositiveButton("保存", null);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(d -> pendingImageReceiver = null);
        dialog.show();

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String title = safeText(etTitle);
            if (title.isEmpty()) {
                Toast.makeText(this, "名称不能为空喵～", Toast.LENGTH_SHORT).show();
                return;
            }
            String age = safeText(etAge);
            String personality = safeText(etPersonality);
            String description = safeText(etDescription);

            Integer avatarResId = null;
            String avatarUri = null;
            if (selectedAvatarUri[0] != null) {
                avatarUri = selectedAvatarUri[0].toString();
            } else if (profile.getAvatarUri() != null) {
                avatarUri = profile.getAvatarUri();
            } else if (profile.getAvatarResId() != 0) {
                avatarResId = profile.getAvatarResId();
            }

            dbHelper.updateCatProfile(profile.getId(), title, age, personality, description, avatarResId, avatarUri);
            reloadList(etSearch.getText() == null ? null : etSearch.getText().toString());
            dialog.dismiss();
        });
    }

    /**
     * 弹出删除确认对话框并执行删除。
     */
    private void showDeleteDialog(CatProfile profile) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("删除")
                .setMessage("确定删除「" + profile.getName() + "」吗？")
                .setNegativeButton("取消", (d, which) -> {
                })
                .setPositiveButton("删除", (d, which) -> {
                    dbHelper.deleteCatProfile(profile.getId());
                    reloadList(etSearch.getText() == null ? null : etSearch.getText().toString());
                    Toast.makeText(this, "已删除喵～", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    /**
     * 长按条目后的操作入口：编辑 / 删除。
     */
    private void showActionsDialog(CatProfile profile) {
        String[] items = new String[]{"编辑", "删除"};
        new MaterialAlertDialogBuilder(this)
                .setTitle(profile.getName())
                .setItems(items, (dialog, which) -> {
                    if (which == 0) {
                        showEditDialog(profile);
                    } else if (which == 1) {
                        showDeleteDialog(profile);
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
     * 持久化读取权限，确保下次启动仍可访问图片 Uri。
     */
    private void persistReadPermission(Uri uri) {
        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException ignored) {
        }
    }

    /**
     * 构造本地默认猫咪档案清单。
     */
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
