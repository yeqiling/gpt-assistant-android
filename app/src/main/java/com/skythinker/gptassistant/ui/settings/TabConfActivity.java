package com.skythinker.gptassistant.ui.settings;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.skythinker.gptassistant.utils.GlobalDataHolder;
import com.skythinker.gptassistant.ui.main.PromptTabData;
import com.skythinker.gptassistant.R;
import com.skythinker.gptassistant.ui.apiprovider.ApiProviderListActivity;

import java.util.List;

public class TabConfActivity extends Activity {

    private RecyclerView rvTabList;
    private TabConfListAdapter adapter;
    private BroadcastReceiver localReceiver;
    private Handler handler = new Handler();

    private interface CustomTextWatcher extends TextWatcher { // 去掉TextWatcher不需要的方法
        @Override
        default void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        default void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_conf);
        overridePendingTransition(R.anim.translate_left_in, R.anim.translate_right_out); // 进入动画

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS); // 沉浸式状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(Color.parseColor("#F5F6F7"));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        List<PromptTabData> tabDataList = GlobalDataHolder.getTabDataList();
        rvTabList = findViewById(R.id.rv_tab_conf_list);
        rvTabList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TabConfListAdapter(this);
        rvTabList.setAdapter(adapter);

        // 模板列表拖拽处理
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder dragged, RecyclerView.ViewHolder target) { // 拖拽排序
                int position_dragged = dragged.getAdapterPosition();
                int position_target = target.getAdapterPosition();
                PromptTabData tab = tabDataList.get(position_dragged);
                tabDataList.set(position_dragged, tabDataList.get(position_target));
                tabDataList.set(position_target, tab);
                adapter.notifyItemMoved(position_dragged, position_target);
                GlobalDataHolder.saveTabDataList();
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) { // 左滑删除
                tabDataList.remove(viewHolder.getAdapterPosition());
                adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                GlobalDataHolder.saveTabDataList();
            }
        }).attachToRecyclerView(rvTabList);

        // 接收模板编辑请求广播（来自TabConfListAdapter）
        localReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                startEditTab(intent.getStringExtra("title"), intent.getStringExtra("prompt"), intent.getIntExtra("position", 0));
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(localReceiver, new IntentFilter("com.skythinker.gptassistant.TAB_EDIT"));

        (findViewById(R.id.bt_add_tab)).setOnClickListener(view -> {
            startEditTab("", "", tabDataList.size());
        });

        findViewById(R.id.ll_go_setting_apis).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TabConfActivity.this, ApiProviderListActivity.class));
            }
        });

        ((Switch) findViewById(R.id.sw_def_enable_multi_chat_conf)).setChecked(GlobalDataHolder.getDefaultEnableMultiChat());
        ((Switch) findViewById(R.id.sw_def_enable_multi_chat_conf)).setOnCheckedChangeListener((compoundButton, checked) -> {
            GlobalDataHolder.saveMultiChatSetting(checked);
        });

        ((Switch) findViewById(R.id.sw_remember_tab_conf)).setChecked(GlobalDataHolder.getSelectedTab() != -1);
        ((Switch) findViewById(R.id.sw_remember_tab_conf)).setOnCheckedChangeListener((compoundButton, checked) -> {
            if (checked && GlobalDataHolder.getSelectedTab() == -1) {
                GlobalDataHolder.saveSelectedTab(0);
            } else if (!checked && GlobalDataHolder.getSelectedTab() != -1) {
                GlobalDataHolder.saveSelectedTab(-1);
            }
        });

        ((Switch) findViewById(R.id.sw_auto_save_history_conf)).setChecked(GlobalDataHolder.getAutoSaveHistory());
        ((Switch) findViewById(R.id.sw_auto_save_history_conf)).setOnCheckedChangeListener((compoundButton, checked) -> {
            GlobalDataHolder.saveHistorySetting(checked);
        });

        ((Switch) findViewById(R.id.sw_limit_vision_size_conf)).setChecked(GlobalDataHolder.getLimitVisionSize());
        ((Switch) findViewById(R.id.sw_limit_vision_size_conf)).setOnCheckedChangeListener((compoundButton, checked) -> {
            GlobalDataHolder.saveVisionSetting(checked);
        });

        ((Switch) findViewById(R.id.sw_enable_internet_conf)).setChecked(GlobalDataHolder.getEnableInternetAccess());
        setInternetItemHidden(!GlobalDataHolder.getEnableInternetAccess());
        ((Switch) findViewById(R.id.sw_enable_internet_conf)).setOnCheckedChangeListener((compoundButton, checked) -> {
            if (checked)
                Toast.makeText(this, R.string.toast_enable_network, Toast.LENGTH_LONG).show();
            GlobalDataHolder.saveFunctionSetting(checked, GlobalDataHolder.getWebMaxCharCount(), GlobalDataHolder.getOnlyLatestWebResult());
            setInternetItemHidden(!checked);
        });

        ((EditText) findViewById(R.id.et_web_max_char_conf)).setText(String.valueOf(GlobalDataHolder.getWebMaxCharCount()));
        ((EditText) findViewById(R.id.et_web_max_char_conf)).addTextChangedListener(new CustomTextWatcher() {
            public void afterTextChanged(Editable editable) {
                try {
                    int maxChars = 2000;
                    if (!editable.toString().isEmpty())
                        maxChars = Integer.parseInt(editable.toString());
                    GlobalDataHolder.saveFunctionSetting(GlobalDataHolder.getEnableInternetAccess(), maxChars, GlobalDataHolder.getOnlyLatestWebResult());
                } catch (NumberFormatException e) {
                    ((EditText) findViewById(R.id.et_web_max_char_conf)).setText(String.valueOf(GlobalDataHolder.getWebMaxCharCount()));
                }
            }
        });

        ((Switch) findViewById(R.id.sw_only_latest_web_conf)).setChecked(GlobalDataHolder.getOnlyLatestWebResult());
        ((Switch) findViewById(R.id.sw_only_latest_web_conf)).setOnCheckedChangeListener((compoundButton, checked) -> {
            GlobalDataHolder.saveFunctionSetting(GlobalDataHolder.getEnableInternetAccess(), GlobalDataHolder.getWebMaxCharCount(), checked);
        });

        ((Switch) findViewById(R.id.sw_send_hide_input_conf)).setChecked(GlobalDataHolder.getAutoHideInput());
        ((Switch) findViewById(R.id.sw_send_hide_input_conf)).setOnCheckedChangeListener((compoundButton, checked) -> {
            GlobalDataHolder.saveAutoHideInputSetting(checked);
        });

        (findViewById(R.id.bt_back_conf)).setOnClickListener(view -> {
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String selectHost = TextUtils.isEmpty(GlobalDataHolder.getGptApiHost()) ? getString(R.string.no_gpt_host) : GlobalDataHolder.getGptApiHost();
        String apiProvider = String.format(getString(R.string.now_select_api_desc), selectHost, GlobalDataHolder.getGptModel());
        ((TextView) findViewById(R.id.select_api)).setText(apiProvider);
    }

    // 设置联网子配置项是否隐藏
    private void setInternetItemHidden(boolean hidden) {
        ((LinearLayout) findViewById(R.id.et_web_max_char_conf).getParent()).setVisibility(hidden ? View.GONE : View.VISIBLE);
        ((LinearLayout) findViewById(R.id.sw_only_latest_web_conf).getParent()).setVisibility(hidden ? View.GONE : View.VISIBLE);
    }

    // 进入模板编辑页面
    public void startEditTab(String title, String prompt, int position) {
        Intent intent = new Intent(this, TabDetailConfActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("prompt", prompt);
        startActivityForResult(intent, position);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { // 处理模板编辑页面返回的数据
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data.hasExtra("ok")) {
            if (data.getBooleanExtra("ok", false)) {
                String title = data.getStringExtra("title");
                String prompt = data.getStringExtra("prompt");
                boolean fromOnline = data.getBooleanExtra("fromOnline", false);
                if (requestCode == GlobalDataHolder.getTabDataList().size() || fromOnline) {
                    GlobalDataHolder.getTabDataList().add(new PromptTabData(title, prompt));
                    adapter.notifyItemInserted(GlobalDataHolder.getTabDataList().size() - 1);
                } else {
                    GlobalDataHolder.getTabDataList().get(requestCode).setTitle(title);
                    GlobalDataHolder.getTabDataList().get(requestCode).setPrompt(prompt);
                    adapter.notifyItemChanged(requestCode);
                }
                GlobalDataHolder.saveTabDataList();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localReceiver);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.translate_left_in, R.anim.translate_right_out);
    }
}