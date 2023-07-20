package com.samplekit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.samplekit.bean.InstalledInfo;
import com.samplekit.databinding.SampleItemInstalledBinding;

import java.util.List;

public class InstalledAdapter extends BaseBindingAdapter<InstalledInfo, SampleItemInstalledBinding> {
    public InstalledAdapter(@Nullable List<InstalledInfo> data) {
        super(data);
    }

    @NonNull
    @Override
    public BindingHolder<SampleItemInstalledBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BindingHolder<>(SampleItemInstalledBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BindingHolder<SampleItemInstalledBinding> holder, InstalledInfo item, int position) {
        holder.binding.tvAppname.setText(item.getAppName());
        holder.binding.tvCreateTime.setVisibility(View.VISIBLE);
        holder.binding.tvCreateTime.setText(item.getPackageName());
        holder.binding.ivLogo.setImageDrawable(item.getIcon());
        holder.binding.tvNumber.setText(String.valueOf(position + 1));
    }
}
