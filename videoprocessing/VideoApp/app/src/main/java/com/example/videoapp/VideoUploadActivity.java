package com.example.videoapp;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

//import com.example.util;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.util.HttpUtil;
import com.example.util.ProgressListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class VideoUploadActivity extends AppCompatActivity {
    public static final String TAG = VideoUploadActivity.class.getName();
    public  final static int VEDIO_KU = 101;
    private String path = "";//文件路径
    private ProgressBar post_progress;
    private TextView post_text;
    Button to_playvideo_Btn;
    Button download_vid_Btn;

    //下载目录定义
    public static String basePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/okhttp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new HttpUtil();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_upload);
        getSupportActionBar().setTitle("视频上传");
        //注册各个部件
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final EditText video_name = (EditText)findViewById(R.id.upload_video_name);
        post_progress = (ProgressBar) findViewById(R.id.post_progress);
        post_text = (TextView) findViewById(R.id.post_text);
        to_playvideo_Btn = findViewById(R.id.to_playvideo_page);
        download_vid_Btn = findViewById(R.id.video_download);


        //设置选择视频的onclick
        findViewById(R.id.video_select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectVideo();
                video_name.setText(path);
            }
        });
        findViewById(R.id.video_upload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(VideoUploadActivity.this, "路径："+basePath, Toast.LENGTH_LONG).show();
                //(Permission denied) 没有文件的读写权限
                //这样吧 我在  注册了了读写权限  等会安装app后 你进入app的权限管理开启那两个权限  然后再上传试一下 收到 弄了和我说一下 好 手机打开的这个软件的权限不 开了 设置里面开了 点 点 开软件读写权限 我截图发咸鱼你看一下 可以了，试一下不用断点
                //ok了老哥 android:requestLegacyExternalStorage="true" android10(compileSdkVersion 29)强制文件要走沙盒路径了
                //如果没有android:requestLegacyExternalStorage="true"这个  需要把文件拷贝到沙盒里面 这个你要看一下android文件适配

                //下面这两个权限你可以百度一下  改一下逻辑  在想要触发的什么向用户申请权限

               if (ContextCompat.checkSelfPermission(VideoUploadActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                   Log.i(TAG, "has READ_EXTERNAL_STORAGE Permission");
               }

               if (ContextCompat.checkSelfPermission(VideoUploadActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                   Log.i(TAG, "has WRITE_EXTERNAL_STORAGE Permission");
               }

                if(path.equals(""))
                    Toast.makeText(VideoUploadActivity.this, "请选择视频后，再点击上传！", Toast.LENGTH_LONG).show();
                else {
                    File file = new File( path);
                    String postUrl = "http://10.242.229.207:5000/api/upload";

                    HttpUtil.postFile(postUrl, new ProgressListener() {
                        @Override
                        public void onProgress(long currentBytes, long contentLength, boolean done) {
                            Log.i(TAG, "currentBytes==" + currentBytes + "==contentLength==" + contentLength + "==done==" + done);
                            int progress = (int) (currentBytes * 100 / contentLength);
                            post_progress.setProgress(progress);
                            post_text.setText(progress + "%");
                        }
                    }, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.i(TAG, "onFailure==" + e.getMessage());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            Log.i(TAG, "onResponse");
                            if (response != null) {
                                String result = response.body().string();
                                Log.i(TAG, "result===" + result);
                            }
                        }
                    }, file);

                }

            }
        });

        to_playvideo_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                     Intent intent=new Intent(VideoUploadActivity.this,MainActivity.class);
                     startActivity(intent);
                } catch (Exception e) {
                    // TOAST to show the message to the user
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        download_vid_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "http://10.242.229.207:5000/videos/processed.mp4";
//                final String fileName = url.split("/")[url.split("/").length - 1];
                final String fileName ="processed.mp4";
                Log.i(TAG, "fileName==" + fileName);

                 if (ContextCompat.checkSelfPermission(VideoUploadActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                   Log.i(TAG, "has READ_EXTERNAL_STORAGE Permission");
               }

               if (ContextCompat.checkSelfPermission(VideoUploadActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                   Log.i(TAG, "has WRITE_EXTERNAL_STORAGE Permission");
               }


                HttpUtil.downloadFile(url, new ProgressListener() {
                    @Override
                    public void onProgress(long currentBytes, long contentLength, boolean done) {
                        Log.i(TAG, "currentBytes==" + currentBytes + "==contentLength==" + contentLength + "==done==" + done);
                        int progress = (int) (currentBytes * 100 / contentLength);
                        post_progress.setProgress(progress);
                        post_text.setText(progress + "%");
                    }
                }, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }


                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response != null) {
                            InputStream is = response.body().byteStream();
                            FileOutputStream fos = new FileOutputStream(new File(basePath + "/" + fileName));
                            int len = 0;
                            byte[] buffer = new byte[2048];
                            while (-1 != (len = is.read(buffer))) {
                                fos.write(buffer, 0, len);
                            }
                            fos.flush();
                            fos.close();
                            is.close();
                        }
                    }
                });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void selectVideo() {
        // TODO 启动相册
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent,VideoUploadActivity.VEDIO_KU);
    }

    /**
     * 选择回调
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // TODO 视频
            case VideoUploadActivity.VEDIO_KU:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        Uri uri = data.getData();
                        uri = geturi(this, data);
                        File file = null;
                        if (uri.toString().indexOf("file") == 0) {
                            file = new File(new URI(uri.toString()));
                            path = file.getPath();
                        } else {
                            path = getPath(uri);
                            file = new File(path);
                        }
                        if (!file.exists()) {
                            break;
                        }
                        if (file.length() > 100 * 1024 * 1024) {
//                            "文件大于100M";
                            break;
                        }
                        //视频播放
//                        mVideoView.setVideoURI(uri);
//                        mVideoView.start();
                        //开始上传视频，
//                        submitVedio();
                    } catch (Exception e) {
                        String  a=e+"";
                    } catch (OutOfMemoryError e) {
                        String  a=e+"";
                    }
                }
                break;
        }

    }

    public static Uri geturi(Context context, android.content.Intent intent) {
        Uri uri = intent.getData();
        String type = intent.getType();
        if (uri.getScheme().equals("file") && (type.contains("image/"))) {
            String path = uri.getEncodedPath();
            if (path != null) {
                path = Uri.decode(path);
                ContentResolver cr = context.getContentResolver();
                StringBuffer buff = new StringBuffer();
                buff.append("(").append(MediaStore.Images.ImageColumns.DATA).append("=")
                        .append("'" + path + "'").append(")");
                Cursor cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        new String[] { MediaStore.Images.ImageColumns._ID },
                        buff.toString(), null, null);
                int index = 0;
                for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                    index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID);
                    // set _id value
                    index = cur.getInt(index);
                }
                if (index == 0) {
                    // do nothing
                } else {
                    Uri uri_temp = Uri
                            .parse("content://media/external/images/media/"
                                    + index);
                    if (uri_temp != null) {
                        uri = uri_temp;
                        Log.i("urishi", uri.toString());
                    }
                }
            }
        }
        return uri;
    }
//处理得到的path
    private String getPath(Uri uri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
}