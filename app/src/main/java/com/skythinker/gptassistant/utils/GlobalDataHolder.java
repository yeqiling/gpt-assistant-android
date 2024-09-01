package com.skythinker.gptassistant.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.skythinker.gptassistant.ui.main.PromptTabData;
import com.skythinker.gptassistant.R;
import com.skythinker.gptassistant.ui.apiprovider.ApiProvider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class GlobalDataHolder {
    public static final String TAG = "GlobalDataHolder";
    private static List<PromptTabData> tabDataList = null;
    private static String gptApiHost;
    private static String gptApiKey;
    private static String gptModel;
    private static boolean defaultEnableMultiChat;
    private static int selectedTab;
    private static boolean enableInternetAccess;
    private static int webMaxCharCount;
    private static boolean onlyLatestWebResult;
    private static boolean limitVisionSize;
    private static boolean autoSaveHistory;

    private static boolean autoHideInput;
    private static SharedPreferences sp = null;
    private static List<ApiProvider> apiProviderList = null;

    public static void init(Context context) {
        sp = context.getSharedPreferences("gpt_assistant", Context.MODE_PRIVATE);
        loadTabDataList();
        if (tabDataList.size() == 0) {
            tabDataList.add(new PromptTabData(context.getString(R.string.text_default_tab_title), context.getString(R.string.text_default_tab_content)));
            saveTabDataList();
        }
        loadApisList();
        loadGptApiInfo();
        loadMultiChatSetting();
        loadSelectedTab();
        loadFunctionSetting();
        loadVisionSetting();
        loadHistorySetting();
        loadAutoHideInputSetting();
    }

    public static List<PromptTabData> getTabDataList() {
        return tabDataList;
    }

    public static List<ApiProvider> getApisList() {
        return apiProviderList;
    }

    public static void saveTabDataList() {
        SharedPreferences.Editor editor = sp.edit();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(tabDataList);
            String base64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
            editor.putString("tab_data_list", base64);
            editor.apply();
            Log.d("saveTabDataList", "saved");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadTabDataList() {
        String base64 = sp.getString("tab_data_list", "");
        if (base64.equals("")) {
            tabDataList = new ArrayList<>();
            return;
        }
        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            tabDataList = (List<PromptTabData>) (new ObjectInputStream(bais).readObject());
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            tabDataList = new ArrayList<>();
        }
    }

    public static void loadGptApiInfo() {
        ApiProvider provider = getFirstCheckedProvider(apiProviderList);
        if (provider == null) {
            gptApiHost = "";
            gptApiKey = "";
            gptModel = "";
        } else {
            gptApiHost = provider.getHost();
            gptApiKey = provider.getKey();
            gptModel = provider.getModel();
        }
    }

    public static ApiProvider getFirstCheckedProvider(List<ApiProvider> apiProviderList) {
        if (apiProviderList == null || apiProviderList.isEmpty()) {
            return null;
        }

        return apiProviderList.stream()
                .filter(ApiProvider::getChecked)
                .findFirst()
                .orElse(null);
    }

    public static void saveGptApiInfo(String host, String key, String model) {
        gptApiHost = host;
        gptApiKey = key;
        gptModel = model;
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("gpt_api_host", gptApiHost);
        editor.putString("gpt_api_key", gptApiKey);
        editor.putString("gpt_model", gptModel);
        editor.apply();
    }

    public static void saveApisList() {
        SharedPreferences.Editor editor = sp.edit();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(apiProviderList);
            String base64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
            editor.putString("apis", base64);
            editor.apply();
            Log.d("saveTabDataList", "saved");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadApisList() {
        String base64 = sp.getString("apis", "");
        if (base64.equals("")) {
            apiProviderList = new ArrayList<>();
            return;
        }
        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            apiProviderList = (List<ApiProvider>) (new ObjectInputStream(bais).readObject());
            Log.i(TAG, apiProviderList.toString());
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            apiProviderList = new ArrayList<>();
        }
    }

    public static void loadMultiChatSetting() {
        defaultEnableMultiChat = sp.getBoolean("default_enable_multi_chat", false);
    }

    public static void saveMultiChatSetting(boolean defaultEnable) {
        defaultEnableMultiChat = defaultEnable;
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("default_enable_multi_chat", defaultEnableMultiChat);
        editor.apply();
    }

    public static void loadSelectedTab() {
        selectedTab = sp.getInt("selected_tab", -1);
    }

    public static void saveSelectedTab(int tab) {
        selectedTab = tab;
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("selected_tab", selectedTab);
        editor.apply();
    }

    public static void loadFunctionSetting() {
        enableInternetAccess = sp.getBoolean("enable_internet", false);
        webMaxCharCount = sp.getInt("web_max_char_count", 2000);
        onlyLatestWebResult = sp.getBoolean("only_latest_web_result", false);
    }

    public static void saveFunctionSetting(boolean enableInternet, int maxCharCount, boolean onlyLatest) {
        enableInternetAccess = enableInternet;
        webMaxCharCount = maxCharCount;
        onlyLatestWebResult = onlyLatest;
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("enable_internet", enableInternetAccess);
        editor.putInt("web_max_char_count", webMaxCharCount);
        editor.putBoolean("only_latest_web_result", onlyLatestWebResult);
        editor.apply();
    }

    public static void loadVisionSetting() {
        limitVisionSize = sp.getBoolean("limit_vision_size", false);
    }

    public static void saveVisionSetting(boolean limitSize) {
        limitVisionSize = limitSize;
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("limit_vision_size", limitVisionSize);
        editor.apply();
    }

    public static void loadHistorySetting() {
        autoSaveHistory = sp.getBoolean("auto_save_history", true);
    }

    public static void loadAutoHideInputSetting() {
        autoHideInput = sp.getBoolean("auto_hide_input", true);
    }

    public static void saveAutoHideInputSetting(boolean hide) {
        autoHideInput = hide;
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("auto_hide_input", autoHideInput);
        editor.apply();
    }

    public static void saveHistorySetting(boolean autoSave) {
        autoSaveHistory = autoSave;
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("auto_save_history", autoSaveHistory);
        editor.apply();
    }

    public static String getGptApiHost() {
        return gptApiHost;
    }

    public static void setGptApiHost(String gptApiHost) {
        GlobalDataHolder.gptApiHost = gptApiHost;
    }

    public static void setGptApiKey(String gptApiKey) {
        GlobalDataHolder.gptApiKey = gptApiKey;
    }

    public static String getGptApiKey() {
        return gptApiKey;
    }

    public static String getGptModel() {
        return gptModel;
    }

    public static void setGptModel(String gptModel) {
        GlobalDataHolder.gptModel = gptModel;
    }

    public static boolean getDefaultEnableMultiChat() {
        return defaultEnableMultiChat;
    }

    public static int getSelectedTab() {
        return selectedTab;
    }

    public static boolean getEnableInternetAccess() {
        return enableInternetAccess;
    }

    public static int getWebMaxCharCount() {
        return webMaxCharCount;
    }

    public static boolean getOnlyLatestWebResult() {
        return onlyLatestWebResult;
    }

    public static boolean getLimitVisionSize() {
        return limitVisionSize;
    }

    public static boolean getAutoSaveHistory() {
        return autoSaveHistory;
    }

    public static boolean getAutoHideInput() {
        return autoHideInput;
    }
}
