package com.skythinker.gptassistant.ui.settings;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.skythinker.gptassistant.utils.GlobalDataHolder;
import com.skythinker.gptassistant.ui.main.PromptTabData;
import com.skythinker.gptassistant.R;

public class TabConfListAdapter extends RecyclerView.Adapter<TabConfListAdapter.ViewHolder> {

    private TabConfActivity tabConfActivity;

    public TabConfListAdapter(TabConfActivity tabConfActivity) {
        this.tabConfActivity = tabConfActivity;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle, tvPrompt;
        private LinearLayout llOuter;
        public ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_list_item_title);
            tvPrompt = itemView.findViewById(R.id.tv_list_item_prompt);
            llOuter = itemView.findViewById(R.id.ll_list_item_outer);

            llOuter.setOnClickListener(null);
            llOuter.setOnClickListener(view -> { // item被点击时广播编辑请求
                PromptTabData tab = GlobalDataHolder.getTabDataList().get(getAdapterPosition());
                Intent broadcastIntent = new Intent("com.skythinker.gptassistant.TAB_EDIT");
                broadcastIntent.putExtra("title", tab.getTitle());
                broadcastIntent.putExtra("prompt", tab.getPrompt());
                broadcastIntent.putExtra("position", getAdapterPosition());
                LocalBroadcastManager.getInstance(view.getContext()).sendBroadcast(broadcastIntent);
                Log.d("edit btn click", "title: " + tab.getTitle() + ", position: " + getAdapterPosition());
                Log.d("list[0]", "title: " + GlobalDataHolder.getTabDataList().get(0).getTitle());
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tab_conf_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PromptTabData tab = GlobalDataHolder.getTabDataList().get(position);
        holder.tvTitle.setText(tab.getTitle());
        holder.tvPrompt.setText(tab.getContentWithoutParams());
    }

    @Override
    public int getItemCount() {
        return GlobalDataHolder.getTabDataList().size();
    }
}
