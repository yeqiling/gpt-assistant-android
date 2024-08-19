package com.skythinker.gptassistant;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ApiProviderListActivity extends Activity {

    private String model = "";
    private RecyclerView rvTabList;
    private ApiProviderListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api_provider_list);

        overridePendingTransition(R.anim.translate_left_in, R.anim.translate_right_out); // 进入动画

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS); // 沉浸式状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(Color.parseColor("#F5F6F7"));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);


        findViewById(R.id.bt_back_model).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        EditText hostView = ((EditText) findViewById(R.id.et_openai_host_conf));
        EditText keyView = ((EditText) findViewById(R.id.et_openai_key_conf));

        findViewById(R.id.cv_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String host = hostView.getText().toString().trim();
                if (!host.isEmpty()) { // 自动补全URL
                    if (!host.startsWith("http://") && !host.startsWith("https://")) {
                        host = "https://" + host;
                    }
                    if (!host.endsWith("/")) {
                        host += "/";
                    }
                }

                String key = keyView.getText().toString().trim();

                if (!TextUtils.isEmpty(host) && !TextUtils.isEmpty(key) && !TextUtils.isEmpty(model)) {
                    ApiProvider provider = new ApiProvider(host, key, model, false);
                    GlobalDataHolder.getApisList().add(provider);
                    GlobalDataHolder.saveApisList();
                    adapter.setNewDatas(GlobalDataHolder.getApisList());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(ApiProviderListActivity.this, getString(R.string.model_params_empty), Toast.LENGTH_SHORT).show();
                }

            }
        });

        List<String> models = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.models))); // 内置模型列表
        ArrayAdapter<String> modelsAdapter = new ArrayAdapter<String>(this, R.layout.model_spinner_item, models) { // 设置Spinner样式和列表数据
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) { // 设置选中/未选中的选项样式
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                if (((Spinner) findViewById(R.id.sp_model_conf)).getSelectedItemPosition() == position) { // 选中项
                    tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                } else { // 未选中项
                    tv.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
                }
                return tv;
            }
        };
        modelsAdapter.setDropDownViewResource(R.layout.model_spinner_dropdown_item); // 设置下拉选项样式
        ((Spinner) findViewById(R.id.sp_model_conf)).setAdapter(modelsAdapter);
        ((Spinner) findViewById(R.id.sp_model_conf)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) { // 有选项被选中
                model = adapterView.getItemAtPosition(i).toString();
                modelsAdapter.notifyDataSetChanged();
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        for (int i = 0; i < modelsAdapter.getCount(); i++) { // 根据当前模型名查找选中的选项
            if (modelsAdapter.getItem(i).equals(GlobalDataHolder.getGptModel())) {
                ((Spinner) findViewById(R.id.sp_model_conf)).setSelection(i);
                break;
            }
            if (i == modelsAdapter.getCount() - 1) { // 没有找到当前模型名，默认选中第一项
                ((Spinner) findViewById(R.id.sp_model_conf)).setSelection(0);
            }
        }

        rvTabList = findViewById(R.id.apis_rv);
        rvTabList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ApiProviderListAdapter(GlobalDataHolder.getApisList());
        rvTabList.setAdapter(adapter);
        adapter.setOnItemClickListener(new ApiProviderListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                List<ApiProvider> apiProviderList = GlobalDataHolder.getApisList();
                for (int i = 0; i < apiProviderList.size(); i++) {
                    ApiProvider provider = apiProviderList.get(i);
                    if (i == position) {
                        provider.setChecked(true);
                    } else {
                        provider.setChecked(false);
                    }
                }
                GlobalDataHolder.saveApisList();
                ApiProvider provider = GlobalDataHolder.getApisList().get(position);
                if (provider != null) {
                    GlobalDataHolder.setGptApiHost(provider.getHost());
                    GlobalDataHolder.setGptApiKey(provider.getKey());
                    GlobalDataHolder.setGptModel(provider.getModel());
                }
                adapter.notifyDataSetChanged();
            }
        });

        // 模板列表拖拽处理
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder dragged, RecyclerView.ViewHolder target) { // 拖拽排序
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                // 移除项目
                ApiProvider removedProvider = GlobalDataHolder.getApisList().remove(position);

                if (GlobalDataHolder.getApisList().isEmpty()) {
                    // 如果列表为空，清空全局设置
                    GlobalDataHolder.setGptApiHost("");
                    GlobalDataHolder.setGptApiKey("");
                    GlobalDataHolder.setGptModel("");
                } else if (removedProvider.getChecked()) {
                    // 如果删除的是选中项，随机选择一个新的
                    Random random = new Random();
                    int newCheckedPosition = random.nextInt(GlobalDataHolder.getApisList().size());
                    GlobalDataHolder.getApisList().get(newCheckedPosition).setChecked(true);
                }

                // 更新适配器
                adapter.setNewDatas(GlobalDataHolder.getApisList());
                adapter.notifyDataSetChanged();

                // 保存更新后的列表
                GlobalDataHolder.saveApisList();
            }
        }).attachToRecyclerView(rvTabList);
    }
}