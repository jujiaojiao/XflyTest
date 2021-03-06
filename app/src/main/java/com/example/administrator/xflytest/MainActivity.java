package com.example.administrator.xflytest;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.administrator.xflytest.tranlate.HttpGet;
import com.example.administrator.xflytest.xfly.JsonParser;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import java.io.InputStream;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements View.OnLongClickListener {
    private final int CAMERA_REQUEST_CODE = 1;
    private Button say;
    private EditText tv;
    private BluetoothAdapter mBluetoothAdapter;
    private AcceptThread acceptThread;
    // 和客户端相同的UUID
    private final UUID MY_UUID = UUID.fromString("abcd1234-ab12-ab12-ab12-abcdef123456");
    private final String NAME = "Bluetooth_Socket";
    private BluetoothServerSocket serverSocket;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            recognize();
            super.handleMessage(msg);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }
    //控件初始化操作
    public void initView(){
        say = ((Button) findViewById(R.id.btn));
        tv = ((EditText) findViewById(R.id.textView));
        say.setOnLongClickListener(this);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        acceptThread = new AcceptThread();
        acceptThread.start();
    }
    //动态申请权限
    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            // 第一次请求权限时，用户如果拒绝，下一次请求shouldShowRequestPermissionRationale()返回true
            // 向用户解释为什么需要这个权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                new AlertDialog.Builder(this)
                        .setMessage("申请录音权限")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //申请相机权限
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.RECORD_AUDIO}, CAMERA_REQUEST_CODE);
                            }
                        })
                        .show();
            } else {
                //申请相机权限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO}, CAMERA_REQUEST_CODE);
            }
        } else {
            Toast.makeText(this, "录音权限已申请", Toast.LENGTH_SHORT).show();
        }
    }
    //权限申请结果
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "录音权限已申请", Toast.LENGTH_SHORT).show();

            } else {
                //用户勾选了不再询问
                //提示用户手动打开权限
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    Toast.makeText(this, "录音权限已被禁止", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    //长按进行操作
    @Override
    public boolean onLongClick(View v) {
        requestPermission();
        //==================================显示对话框=========================================
        recognize();
        //===================不显示对话框==================================
     /*   //1.创建SpeechRecognizer对象，第二个参数：本地听写时传InitListener
        SpeechRecognizer mIat= SpeechRecognizer.createRecognizer(this, null);
        //2.设置听写参数，详见《科大讯飞MSC API手册(Android)》SpeechConstant类
        mIat.setParameter(SpeechConstant.DOMAIN, "iat");
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin ");
        //3.开始听写
        mIat.startListening(mRecoListener);*/


        return false;
    }
    private void recognize(){
        RecognizerDialog dialog = new RecognizerDialog(this, new InitListener() {
            @Override
            public void onInit(int i) {

            }
        });
        dialog.setListener(new RecognizerDialogListener() {
            @Override
            public void onResult(RecognizerResult recognizerResult, boolean b) {
                String resultString = recognizerResult.getResultString();
                if (!b){
                    String result = JsonParser.parseIatResult(resultString);
                    HttpGet.onTranslate(result,tv);
                }
            }

            @Override
            public void onError(SpeechError speechError) {

            }
        });
        dialog.show();
    }
    //不显示对话框的听写监听器
    private RecognizerListener mRecoListener = new RecognizerListener() {
        @Override
        public void onVolumeChanged(int i, byte[] bytes) {

        }

        @Override
        public void onBeginOfSpeech() {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            String resultJson = recognizerResult.getResultString();
            //b=true 表示会话完成
            String result = null;
            if (!b){
                result = JsonParser.parseIatResult(resultJson);
            }
            HttpGet.onTranslate(result,tv);
        }

        @Override
        public void onError(SpeechError speechError) {
            speechError.getPlainDescription(true) ;//获取错误码描述
        }

        //扩展用接口
        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {


        }
    };

    /**
     * 接收线程
     */
    private class AcceptThread extends Thread {
        public void run() {
            try {
                serverSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (Exception e) {
                e.printStackTrace();
            }
//            Log.d(tag, "等待客户连接...");
            while (true) {
                try {
                    BluetoothSocket socket = serverSocket.accept();
                    BluetoothDevice device = socket.getRemoteDevice();
//                    Log.d(tag, "接受客户连接 , 远端设备名字:" + device.getName() + " , 远端设备地址:" + device.getAddress());
                    byte[] buffer = new byte[64];
                    if (socket.isConnected()) {
//                        Log.d(tag, "已建立与客户连接.");
                        InputStream is = socket.getInputStream();
                        int cnt = is.read(buffer);
                        is.close();
                        Message msg = new Message();
//                        msg.what =cnt;
                        msg.obj = new String(buffer, 0, cnt, "utf-8");
                        Log.e("JJJ========", "run: "+msg.obj);
                        handler.sendMessage(msg);
//                        Log.d(tag, "收到服务端发来数据:" + s);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}