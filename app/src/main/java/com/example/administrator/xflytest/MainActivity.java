package com.example.administrator.xflytest;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {
    private final int CAMERA_REQUEST_CODE = 1;
    private RecognizerDialog iatDialog;
    private Button say;
    private EditText tv;
    private String translateText;
    public static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        say = ((Button) findViewById(R.id.btn));
        tv = ((EditText) findViewById(R.id.textView));
//        say.setOnClickListener(this);
        say.setOnLongClickListener(this);
    }

    @Override
    public void onClick(View v) {
//        requestPermission();
//        iatDialog = new RecognizerDialog(MainActivity.this, mInitListener);
//
//        iatDialog.setListener(new RecognizerDialogListener() {
//            String resultJson = "[";
//            @Override
//            public void onResult(RecognizerResult recognizerResult, boolean b) {
//
//                Log.e("JJJ", "onResult111111111111: " );
//                System.out.println("-----------------   onResult   -----------------");
//                if (!b) {
//                    resultJson += recognizerResult.getResultString() + ",";
//                } else {
//                    resultJson += recognizerResult.getResultString() + "]";
//                }
//                if (b) {
//                    //解析语音识别后返回的json格式的结果
//                    Gson gson = new Gson();
//                    List<DictationResult> resultList = gson.fromJson(resultJson,
//                            new TypeToken<List<DictationResult>>() {
//                            }.getType());
//                    String result = "";
//                    for (int i = 0; i < resultList.size() - 1; i++) {
//                        result += resultList.get(i).toString();
//                    }
//                    tv.setText(result);
//                    //获取焦点
//                    tv.requestFocus();
//                    //将光标定位到文字最后，以便修改
//                    tv.setSelection(result.length());
//                }
//            }
//
//            @Override
//            public void onError(SpeechError speechError) {
//                speechError.getPlainDescription(true);
//            }
//        });
//        iatDialog.show();
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

    @Override
    public boolean onLongClick(View v) {
        requestPermission();
        //1.创建SpeechRecognizer对象，第二个参数：本地听写时传InitListener
        SpeechRecognizer mIat= SpeechRecognizer.createRecognizer(this, null);
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
                    translate(result);
                }
            }

            @Override
            public void onError(SpeechError speechError) {

            }
        });
        dialog.show();
        //2.设置听写参数，详见《科大讯飞MSC API手册(Android)》SpeechConstant类
//        mIat.setParameter(SpeechConstant.DOMAIN, "iat");
//        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
//        mIat.setParameter(SpeechConstant.ACCENT, "mandarin ");
//        //3.开始听写
//        mIat.startListening(mRecoListener);
        //听写监听器



        return false;
    }
    private static final String to = "en";
    private RecognizerListener mRecoListener = new RecognizerListener() {
        //听写结果回调接口(返回Json格式结果，用户可参见附录12.1)；
        //一般情况下会通过onResults接口多次返回结果，完整的识别内容是多次结果的累加；
        //关于解析Json的代码可参见MscDemo中JsonParser类；
        //isLast等于true时会话结束。
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
//            Log.d("Result=======",resultJson);
            if (!b){
                String result = JsonParser.parseIatResult(resultJson);
//                Log.d("text=======",result);
                tv.setText(result);
                //获取焦点
                tv.requestFocus();
                //将光标定位到文字最后，以便修改
                tv.setSelection(result.length());
            }
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
    private void translate(String str){
        HttpGet httpGet = new HttpGet();
        try {
            httpGet.translate(str, to, new TransApi() {
                @Override
                public void onSuccess(String result) {
                    tv.setText(result);
                    //获取焦点
                    tv.requestFocus();
                    //将光标定位到文字最后，以便修改
                    tv.setSelection(result.length());
                }

                @Override
                public void onFailure(String exception) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}