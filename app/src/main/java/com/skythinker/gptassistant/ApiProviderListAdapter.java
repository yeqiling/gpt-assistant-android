package com.skythinker.gptassistant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ApiProviderListAdapter extends RecyclerView.Adapter<ApiProviderListAdapter.ViewHolder> {

    private List<ApiProvider> apiProviders;
    private OnItemClickListener listener;
    private int selectedPosition = -1;

    public ApiProviderListAdapter(List<ApiProvider> apiProviders) {
        this.apiProviders = apiProviders;
    }

    public void setNewDatas(List<ApiProvider> apiProviders){
        this.apiProviders = apiProviders;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle, tvPrompt;
        private LinearLayout llOuter;

        private ImageView ivChecked;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_list_item_title);
            tvPrompt = itemView.findViewById(R.id.tv_list_item_prompt);
            llOuter = itemView.findViewById(R.id.ll_list_item_outer);
            ivChecked = itemView.findViewById(R.id.imageView6);
        }

        public void bind(final ApiProvider apiProvider, final int position, final OnItemClickListener listener) {
            tvTitle.setText(apiProvider.getHost());
            tvPrompt.setText(apiProvider.getModel());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClick(position);
                    }
                }
            });

            ivChecked.setVisibility(apiProvider.getChecked() ? View.VISIBLE : View.GONE);

        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.api_provider_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ApiProvider apiProvider = apiProviders.get(position);
        holder.bind(apiProvider, position, listener);
    }

    @Override
    public int getItemCount() {
        return apiProviders.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

}
