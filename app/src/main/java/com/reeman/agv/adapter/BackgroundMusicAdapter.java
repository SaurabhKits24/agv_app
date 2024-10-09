package com.reeman.agv.adapter;

import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.elvishew.xlog.XLog;
import com.reeman.agv.R;
import com.reeman.agv.request.ServiceFactory;
import com.reeman.agv.request.service.RobotService;
import com.reeman.agv.utils.OpenMusic;
import com.reeman.agv.utils.ToastUtils;
import com.reeman.agv.utils.VoiceHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * 歌曲列表的适配
 */
public class BackgroundMusicAdapter extends RecyclerView.Adapter<BackgroundMusicAdapter.ViewHolder> {

    private final Context mContext;
    private final String[] mSongs;

    private final String[] select;

    private final Set<String> selectMusic;
    private final String ip;

    private int currentPlayingIndex = -1;

    private int concurrentDownloadCount = 0;

    private boolean musicTry = false;

    public BackgroundMusicAdapter(Context context, String[] songs, String[] selectSong, String ipAddress) {
        mContext = context;
        mSongs = songs;
        select = selectSong;
        ip = ipAddress;
        selectMusic = new LinkedHashSet<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewGroup root = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_music_button, parent, false);
        return new ViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        final String songName = mSongs[pos];
        holder.songNameTextView.setText(songName);
        //显示已经选择的歌曲
        if (select != null){
            if (containsAny(songName,select)){
                selectMusic.add(songName);
                holder.checkBox.setChecked(true);
            }
        }else {
        }

        //获取本地音乐路径
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        File testDir = new File(dir, "music");
        if (!testDir.exists()) {
            testDir.mkdirs();
        }

        //checkbox变化事件
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (holder.checkBox.isChecked()){
                    selectMusic.add(mSongs[pos]);
                }else {
                    selectMusic.remove(mSongs[pos]);
                }
            }
        });

        //item 点击事件
        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 切换checkbox的选中状态
                holder.checkBox.setChecked(!holder.checkBox.isChecked());
            }
        });

        if (OpenMusic.musicLocal(songName,testDir.toString())) {
            // 设置为已下载状态且不可点击
            holder.downloadButton.setText(mContext.getString(R.string.text_downloaded));
            holder.downloadButton.setClickable(false);
            holder.downloadButton.setEnabled(false);
        } else {
            // 设置为可下载状态
            holder.downloadButton.setText(mContext.getString(R.string.text_download));
            holder.downloadButton.setClickable(true);
            holder.downloadButton.setEnabled(true);
        }


        holder.downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 处理下载按钮点击事件
                concurrentDownloadCount ++;
                if (concurrentDownloadCount > 1){
                    ToastUtils.showShortToast(mContext.getString(R.string.text_overhead_max_concurrent_download_count));
                    return;
                }
                RobotService robotService = ServiceFactory.getRobotService();
                XLog.w("开始下载");
                holder.downloadButton.setText(mContext.getString(R.string.text_downloading));
                String netPath = "http://" + ip + "/file_down/delivery";
                Observable
                        .create(new ObservableOnSubscribe<Object>() {
                            @Override
                            public void subscribe(@NonNull ObservableEmitter<Object> emitter) throws Throwable {
                                Map<String, String> params = new HashMap<>();
                                params.put("name", songName);
                                Response<ResponseBody> response = ServiceFactory.getRobotService().downloadSync(netPath, params).execute();

                                InputStream inputStream = response.body().byteStream();
                                OutputStream outputStream = null;
                                OutputStream os = null;
                                long currentLength = 0;
                                try {
                                    outputStream = new FileOutputStream(testDir + "/" + songName);
                                    byte[] buffer = new byte[1024];
                                    int bytesRead;
                                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                                        outputStream.write(buffer, 0, bytesRead);
                                    }
                                    outputStream.flush(); // 确保缓冲区的数据被写入文件
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } finally {
                                    try {
                                        if (outputStream != null) {
                                            outputStream.close();
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                emitter.onComplete();
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<Object>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {

                            }

                            @Override
                            public void onNext(@NonNull Object o) {
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                ToastUtils.showShortToast(mContext.getString(R.string.text_download_failed) + e);
                                concurrentDownloadCount = 0;
                            }

                            @Override
                            public void onComplete() {
                                ToastUtils.showShortToast(mContext.getString(R.string.text_download_success));
                                holder.downloadButton.setText(mContext.getString(R.string.text_downloaded));
                                holder.downloadButton.setClickable(false);
                                holder.downloadButton.setEnabled(false);
                                concurrentDownloadCount = 0;
                            }
                        });
            }
        });

        holder.listenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicTry){
                    if (!(holder.listenButton.getText().equals(mContext.getString(R.string.text_pause)))){
                        Toast.makeText(mContext,mContext.getText(R.string.text_other_music_play),Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if (holder.downloadButton.getText().equals(mContext.getString(R.string.text_downloaded))) {
                    //音乐正在播放
                    if (VoiceHelper.isPlaying()) {
                        VoiceHelper.pause();
                        //说明当前是暂停
                        if (currentPlayingIndex == 1) {
                            musicTry = false;
                            holder.listenButton.setText(mContext.getString(R.string.text_try_listen));
                            currentPlayingIndex = -1;
                        } else {
                            musicTry = true;
                            holder.listenButton.setText(mContext.getString(R.string.text_pause));
                            currentPlayingIndex = 1;
                            VoiceHelper.playFile(testDir + "/" + songName, () -> {
                                holder.listenButton.setText(mContext.getString(R.string.text_try_listen));
                                musicTry = false;
                            });
                        }
                        return;
                    }
                    VoiceHelper.playFile(testDir + "/" + songName, () -> {
                        holder.listenButton.setText(mContext.getString(R.string.text_try_listen));
                        musicTry = false;
                    });
                    musicTry = true;
                    holder.listenButton.setText(mContext.getString(R.string.text_pause));
                    if (currentPlayingIndex == -1) {
                        currentPlayingIndex = 1;
                    } else if (currentPlayingIndex != 1) {
                        currentPlayingIndex = 1;
                    }
                }else {
                    Toast.makeText(mContext,mContext.getString(R.string.text_please_download_this_music_first),Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    public List<String> getSelectSongs(){
        List<String> select = new ArrayList<>(selectMusic);
        return select;
    }

    public static boolean containsAny(String a, String[] b) {
        for (String otherStr : b) {
            if (a.equals(otherStr)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return mSongs == null ? 0 : mSongs.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView songNameTextView;
        private final View root;
        private final Button downloadButton;

        private final Button listenButton;
        private final CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.root = itemView;
            songNameTextView = itemView.findViewById(R.id.song_name);
            downloadButton = itemView.findViewById(R.id.download_button);
            listenButton = itemView.findViewById(R.id.listen_button);
            checkBox = itemView.findViewById(R.id.checkbox);
        }
    }

}