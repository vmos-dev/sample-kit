package com.samplekit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.samplekit.bean.FileItem;
import com.samplekit.utils.FileSizeFormat;
import com.samplekit.R;
import com.samplekit.databinding.SampleItemFileBinding;

import java.util.List;

public class FilesAdapter extends BaseBindingAdapter<FileItem, SampleItemFileBinding> {

    public FilesAdapter(List<FileItem> data) {
        super(data);
    }

    @NonNull
    @Override
    public BindingHolder<SampleItemFileBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BindingHolder<>(SampleItemFileBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BindingHolder<SampleItemFileBinding> holder, FileItem item, int position) {
        holder.binding.tvFileName.setText(item.getName());
        if (item.isDirectory()) {
            holder.binding.ivTypeIcon.setImageResource(R.drawable.sample_ic_folder);
            holder.binding.ivTypeClickable.setVisibility(View.VISIBLE);
            holder.binding.tvFileSize.setVisibility(View.GONE);
        } else {
            holder.binding.ivTypeIcon.setImageResource(R.drawable.sample_ic_file);
            holder.binding.ivTypeClickable.setVisibility(View.GONE);
            holder.binding.tvFileSize.setVisibility(View.VISIBLE);
            holder.binding.tvFileSize.setText(FileSizeFormat.formatSize(item.getSize()));
        }
        holder.binding.getRoot().setAlpha(item.isDisabled() ? 0.5f : 1f);
    }

}
