package com.skythinker.gptassistant;

import android.content.Intent;
import android.service.quicksettings.TileService;
import android.util.Log;

import com.skythinker.gptassistant.ui.main.MainActivity;

public class QuickStartService extends TileService {
    @Override
    public void onClick() { // 状态栏快捷按钮点击事件
        super.onClick();
        Log.d("QuickStartService", "onClick");
        if(!MainActivity.isAlive() || !MainActivity.isRunning()) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityAndCollapse(intent); // 唤起应用
            Log.d("QuickStartService", "startActivity: MainActivity");
        }
    }
}
