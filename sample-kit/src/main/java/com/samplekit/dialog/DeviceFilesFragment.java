package com.samplekit.dialog;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.samplekit.bean.FileItem;
import com.samplekit.utils.DatabaseUtils;

import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

/**
 * 真机文件列表
 */
@RuntimePermissions
public class DeviceFilesFragment extends FilesFragment {
    private final int REQUEST_CODE_PERMISSION = 0;

    private String directoryPathApi30;
    private Integer queryDirectoryIdApi30;

    public static DeviceFilesFragment newInstance(Bundle parent) {
        Bundle args = new Bundle();
        args.putString("title", "本机");
        if (parent != null) {
            args.putAll(parent);
        }
        DeviceFilesFragment fragment = new DeviceFilesFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    protected String getSdcardRealPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    @Override
    protected void requestExternalFiles(String directoryPath, int queryDirectoryId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requestExternalFilesFromDeviceWithPermissionCheckApi30(directoryPath, queryDirectoryId);
        } else {
            DeviceFilesFragmentPermissionsDispatcher.requestExternalFilesFromDeviceWithPermissionCheck(this, directoryPath, queryDirectoryId);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void requestExternalFilesFromDeviceWithPermissionCheckApi30(String directoryPath, Integer queryDirectoryId) {
        this.directoryPathApi30 = directoryPath;
        this.queryDirectoryIdApi30 = queryDirectoryId;

        // 有的系统上没这个界面
        final Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
        final ResolveInfo resolveInfo = requireContext().getPackageManager().resolveActivity(intent, 0);
        if (resolveInfo == null) {
            // 不支持新版本申请的方式的 就走老版本流程
            DeviceFilesFragmentPermissionsDispatcher.requestExternalFilesFromDeviceWithPermissionCheck(this, directoryPath, queryDirectoryId);
        } else {
            if (Environment.isExternalStorageManager()) {
                requestExternalFilesFromDevice(directoryPath, queryDirectoryId);
            } else {
                startActivityForResult(intent, REQUEST_CODE_PERMISSION);
            }
        }
    }

    @Override
    protected JSONArray queryContentProvider(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // 真机
        final Cursor cursor = getContext().getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
        JSONArray jsonArray = DatabaseUtils.toJsonArray(cursor);
        cursor.close();
        return jsonArray == null ? new JSONArray() : jsonArray;
    }

    @Override
    protected List<FileItem> traverseDirectoryFiles(String parent) {
        try {
            final List<FileItem> items = new ArrayList<>();
            final File[] files = new File(parent).listFiles();
            if (files != null) {
                for (File file : files) {
                    final FileItem item = createFileItem(file.hashCode(), null, file.getAbsolutePath());
                    if (item != null) items.add(item);
                }
            }
            return items;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected File getAbsoluteFile(String path) {
        return new File(path);
    }

    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE})
    public void requestExternalFilesFromDevice(String directoryPath, Integer queryDirectoryId) {
        asyncLoadExternalFiles(directoryPath, queryDirectoryId);
    }

    @OnPermissionDenied({Manifest.permission.READ_EXTERNAL_STORAGE})
    public void onExternalPermissionDenied() {
        setErrorMessage("请授予存储权限");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        DeviceFilesFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE_PERMISSION == requestCode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                requestExternalFilesFromDevice(directoryPathApi30, queryDirectoryIdApi30);
            } else {
                onExternalPermissionDenied();
            }
        }
    }
}
