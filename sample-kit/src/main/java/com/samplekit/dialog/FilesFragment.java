package com.samplekit.dialog;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.samplekit.utils.FilenameUtils;
import com.samplekit.adapters.FilesAdapter;
import com.samplekit.bean.FileItem;
import com.samplekit.databinding.SampleFragmentFilesBinding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class FilesFragment extends Fragment {
    private SampleFragmentFilesBinding binding;

    private final Uri externalUri = MediaStore.Files.getContentUri("external");
    private String sdcardRealPath;

    private int currentDirectoryId = -1;
    private String currentDirectoryPath;

    private boolean queryProvider;
    private List<String> disabledMimeTypes;
    private List<String> disabledFileSuffixes;
    private boolean supportUseDirectory;

    private OnFileItemListener fileItemListener;

    private final int ID_PARENT = -1;
    private final int ID_SELECT_CURRENT = -2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SampleFragmentFilesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Bundle arguments = getArguments();
        onArguments(arguments);
        sdcardRealPath = getSdcardRealPath();
        final String rootDir = arguments.getString("root_dir");
        onInitFiles(rootDir);
    }

    protected void onArguments(Bundle arguments) {
        String title = null;
        if (arguments != null) {
            final String[] mineTypeArray = arguments.getStringArray("disabled_mime_types");
            if (mineTypeArray != null) {
                disabledMimeTypes = Arrays.asList(mineTypeArray);
            }
            final String[] disabledFileSuffixArray = arguments.getStringArray("disabled_file_suffixes");
            if (disabledFileSuffixArray != null) {
                disabledFileSuffixes = Arrays.asList(disabledFileSuffixArray);
            }
            title = arguments.getString("title");
            queryProvider = arguments.getBoolean("query_provider", false);
            supportUseDirectory = arguments.getBoolean("support_use_directory", false);
        }
        if (TextUtils.isEmpty(title)) {
            binding.tvFragmentTitle.setVisibility(View.GONE);
        } else {
            binding.tvFragmentTitle.setVisibility(View.VISIBLE);
            binding.tvFragmentTitle.setText(title);
        }
    }

    protected void onInitFiles(String rootDir) {
        requestExternalFiles(TextUtils.isEmpty(rootDir) ? sdcardRealPath : rootDir, -1);
    }

    public void setQueryProvider(boolean queryProvider) {
        this.queryProvider = queryProvider;
        reloadExternalFiles();
    }

    public void reloadExternalFiles() {
        requestExternalFiles(currentDirectoryPath, currentDirectoryId);
    }

    protected abstract void requestExternalFiles(String directoryPath, int queryDirectoryId);

    protected abstract String getSdcardRealPath();

    protected abstract JSONArray queryContentProvider(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder);

    protected abstract List<FileItem> traverseDirectoryFiles(String parent);

    /**
     * 根据路径查询目录id
     *
     * @param path 在虚拟机内的路径
     * @return
     */
    private int queryDirectoryId(String path) {
        try {
            final JSONArray jsonArray = queryContentProvider(externalUri, new String[]{MediaStore.Files.FileColumns._ID},
                    MediaStore.Files.FileColumns.DATA + " = ?", new String[]{path}, null);
            return jsonArray.optJSONObject(0).optInt(MediaStore.Files.FileColumns._ID);
        } catch (Exception ignored) {
        }
        return 0;
    }

    protected abstract File getAbsoluteFile(String path);

    /**
     * 根据路径或路径id查询
     *
     * @param directoryPath    路径
     * @param queryDirectoryId 路径id
     */
    @SuppressLint("StaticFieldLeak")
    protected void asyncLoadExternalFiles(String directoryPath, int queryDirectoryId) {
        binding.tvErrorMessage.setVisibility(View.GONE);
        new AsyncTask<Void, Void, List<FileItem>>() {

            @Override
            protected void onPreExecute() {
                binding.progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected List<FileItem> doInBackground(Void... voids) {
                final List<FileItem> items = new ArrayList<>();
                if (!directoryPath.equals(sdcardRealPath)) {
                    // 上一级
                    FileItem parent = new FileItem();
                    parent.setId(ID_PARENT);
                    parent.setName("上一级...");
                    parent.setDirectory(true);
                    items.add(parent);
                    // 选择文件夹
                    if (supportUseDirectory) {
                        FileItem selectCurrent = new FileItem();
                        selectCurrent.setId(ID_SELECT_CURRENT);
                        selectCurrent.setName("选择此文件夹...");
                        selectCurrent.setDirectory(true);
                        items.add(selectCurrent);
                    }
                }

                final List<FileItem> fileItems;
                if (queryProvider) {
                    // 使用查询Provider的方式
                    // 给了路径id 用路径id  没有路径就根据路径查询
                    int directoryId = queryDirectoryId < 0 ? queryDirectoryId(directoryPath) : queryDirectoryId;
                    currentDirectoryId = directoryId;
                    fileItems = resolveProviderJSON(directoryId);
                } else {
                    // 使用遍历文件的方式
                    fileItems = traverseDirectoryFiles(directoryPath);
                }
                if (fileItems != null) items.addAll(fileItems);
                currentDirectoryPath = directoryPath;

                Collections.sort(items, (o1, o2) -> {
                    if (o1.getId() == ID_PARENT || o1.getId() == ID_SELECT_CURRENT) {
                        return o1.getId() - ID_SELECT_CURRENT;
                    } else if (o2.getId() == ID_PARENT || o2.getId() == ID_SELECT_CURRENT) {
                        return o2.getId() - ID_SELECT_CURRENT;
                    } else {
                        return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
                    }
                });
                return items;
            }

            @Override
            protected void onPostExecute(List<FileItem> result) {
                if (fileItemListener != null) {
                    fileItemListener.onEnterDirectory(FilesFragment.this, directoryPath);
                }
                binding.rv.setLayoutManager(new LinearLayoutManager(getContext()));
                FilesAdapter adapter = new FilesAdapter(result);
                adapter.setOnItemClickListener((v, position) -> {
                    final FileItem item = adapter.getData().get(position);
                    if (item.isDisabled()) {
                        return;
                    }
                    if (item.getId() == ID_SELECT_CURRENT) {
                        if (fileItemListener != null) {
                            final FileItem newItem = new FileItem();
                            newItem.setId(ID_SELECT_CURRENT);
                            newItem.setDirectory(true);
                            newItem.setAbsolutePath(directoryPath);
                            fileItemListener.onClickFile(FilesFragment.this, newItem);
                        }
                    } else {
                        // 如果是文件夹 继续进入
                        if (item.isDirectory()) {
                            if (item.getId() == ID_PARENT) {
                                // 如果是上一级
                                asyncLoadExternalFiles(new File(directoryPath).getParent(), ID_PARENT);
                            } else {
                                asyncLoadExternalFiles(item.getPath(), item.getId());
                            }
                        } else {
                            if (fileItemListener != null) {
                                fileItemListener.onClickFile(FilesFragment.this, item);
                            }
                        }
                    }
                });
                adapter.setOnItemLongClickListener((view, position) -> {
                    final String absolutePath = adapter.getData().get(position).getAbsolutePath();
                    if (!TextUtils.isEmpty(absolutePath)) {
                        Toast.makeText(getContext(),absolutePath , Toast.LENGTH_SHORT).show();
                    }
                });
                binding.rv.setAdapter(adapter);
                binding.tvFilesTitle.setText(directoryPath);
                binding.progressBar.setVisibility(View.GONE);
                binding.tvErrorMessage.setVisibility(View.GONE);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private List<FileItem> resolveProviderJSON(int directoryId) {
        try {
            final List<FileItem> items = new ArrayList<>();
            // 查找directoryId目录下的文件夹或文件
            final JSONArray jsonArray = queryContentProvider(externalUri,
                    new String[]{MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.MIME_TYPE, MediaStore.Files.FileColumns.SIZE},
                    MediaStore.Files.FileColumns.PARENT + " = ?", new String[]{String.valueOf(directoryId)}, MediaStore.Files.FileColumns.TITLE);
            // 解析
            for (int i = 0; i < jsonArray.length(); i++) {
                final JSONObject jsonObject = jsonArray.optJSONObject(i);
                if (jsonObject != null) {
                    final FileItem item = createFileItem(jsonObject.optInt(MediaStore.Files.FileColumns._ID), jsonObject.optString(MediaStore.Files.FileColumns.MIME_TYPE),
                            jsonObject.optString(MediaStore.Files.FileColumns.DATA));
                    if (item != null) {
                        items.add(item);
                    }
                }
            }
            return items;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public FileItem createFileItem(int id, String mimeType, String path) {
        final FileItem item = new FileItem();
        item.setId(id);
        item.setMimeType(mimeType);
        // 在虚拟机内的路径
        item.setPath(path);
        // 在真机的路径
        final File absoluteFile = getAbsoluteFile(item.getPath());
        // 不显示隐藏文件
        if (absoluteFile.exists() && !absoluteFile.isHidden()) {
            item.setAbsolutePath(absoluteFile.getAbsolutePath());
            item.setName(absoluteFile.getName());
            item.setSize(absoluteFile.length());
            item.setDirectory(absoluteFile.isDirectory());

            boolean disabled = false;
            if (!item.isDirectory()) {
                // 如果设置了过滤mimeType 不在mimeType的标记禁用
                if (disabledMimeTypes != null && !disabledMimeTypes.contains(item.getMimeType())) {
                    disabled = true;
                } else if (disabledFileSuffixes != null) {
                    // 如果设置了过滤后缀名 不在后缀名的标记禁用
                    String extension = "." + FilenameUtils.getExtension(item.getName());
                    if (!disabledFileSuffixes.contains(extension)) {
                        disabled = true;
                    }
                }
            }
            item.setDisabled(disabled);
            return item;
        }
        return null;
    }

    public void setFileItemListener(OnFileItemListener listener) {
        this.fileItemListener = listener;
    }

    public void setErrorMessage(CharSequence text) {
        binding.tvErrorMessage.setVisibility(View.VISIBLE);
        binding.tvErrorMessage.setText(text);
    }

    public interface OnFileItemListener {
        /**
         * 进入到目录
         *
         * @param fragment
         * @param path
         */
        void onEnterDirectory(Fragment fragment, String path);

        /**
         * 点击了文件
         *
         * @param fragment
         * @param fileItem
         */
        void onClickFile(Fragment fragment, FileItem fileItem);
    }

}
