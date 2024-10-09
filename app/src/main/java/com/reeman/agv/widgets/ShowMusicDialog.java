package com.reeman.agv.widgets;

import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.reeman.agv.R;
import com.reeman.agv.adapter.BackgroundMusicAdapter;
import com.reeman.agv.utils.GetFiles;
import com.reeman.agv.utils.ToastUtils;
import com.reeman.agv.utils.VoiceHelper;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ShowMusicDialog extends BaseDialog {


    public ShowMusicDialog(@NonNull Context context, List<String> file, List<String> select, String ip, ShowMusicDialogListener listenter) {
        super(context);


        // 设置自定义布局
        View root = LayoutInflater.from(context).inflate(R.layout.layout_music_main, null);


        // 设置列表适配器

        RecyclerView listView = root.findViewById(R.id.rv_background_music_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        listView.setLayoutManager(layoutManager);
        BackgroundMusicAdapter adapter;
        if (select != null){
            adapter = new BackgroundMusicAdapter(context, file.toArray(new String[0]),select.toArray(new String[0]),ip);
        }else {
            adapter= new BackgroundMusicAdapter(context, file.toArray(new String[0]),null,ip);
        }
        listView.setAdapter(adapter);

        Button btnConfirm = root.findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (VoiceHelper.isPlaying()) {
                    VoiceHelper.pause();
                }
                // 处理确定按钮点击事件，获取选中的歌曲
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
                File testDir = new File(dir, "music");
                if (!testDir.exists()) {
                    testDir.mkdirs();
                }
                //选中歌曲
                List<String> selectSongs = adapter.getSelectSongs();
                //本地歌曲
                List<String> localSongs = Arrays.asList(GetFiles.getFilesWithExtensions(testDir.toString()));
                if (selectSongs.size() < 1 || selectSongs == null){
                    ToastUtils.showShortToast(context.getString(R.string.text_no_select));
                }else if (GetFiles.containsAllRemoteSongs(selectSongs,localSongs)){
                    //存入数据
                    listenter.onBackgroundMusicSelectedList(selectSongs);
                    ToastUtils.showShortToast(context.getString(R.string.text_save_success));
                    dismiss();
                }else {
                    ToastUtils.showShortToast(context.getString(R.string.text_please_download_this_music_first));
                }
            }
        });
        setContentView(root);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
    }

    public interface ShowMusicDialogListener {
        void onBackgroundMusicSelectedList(List<String> file);
    }
}