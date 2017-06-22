package com.example.administrator.xflytest.tranlate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

//只需传入需要翻译的原文，译文的语言，以及展示译文的textview
public class HttpGet {
		private  static final String UTF8 = "utf-8";
	    //APP ID
	    private static final  String APP_ID = "20170608000055594";
	    //Key
	    private  static final String SECRET_KEY = "ukqWZefsxqfHM2AI5u1v";
	    //翻译HTTP地址：
	    private  static final String baseURL = "http://api.fanyi.baidu.com/api/trans/vip/translate";
	    //随机数
	    private  static final Random random = new Random();
	    private  static final String from = "auto";
	    private static void translate( String needToTransString, String to, final TransApi callBack) throws Exception {
	        //生成签名sign
	        int salt = random.nextInt(10000);
	        //appid+needToTransString+salt+密钥
	        String md5String = APP_ID + new String(needToTransString.getBytes())+ salt + SECRET_KEY;
	        String sign = MD5.encode(md5String.toString());
	        //匹配的Url地址
	        final URL urlFinal = new URL(baseURL + "?q=" + URLEncoder.encode(needToTransString, UTF8) +
	                "&from=" + from + "&to=" + to + "&appid=" + APP_ID + "&salt=" + salt + "&sign=" + sign);
	       URLEncoder.encode(needToTransString, UTF8);//%E4%BD%A0%E5%A5%BD

	        //异步任务访问网络
	        new AsyncTask<Void, Integer, String>() {
	            @Override
	            protected String doInBackground(Void... params) {
	                String text = null ;
	                HttpURLConnection conn = null;
	                try {
	                    conn = (HttpURLConnection) urlFinal.openConnection();
	                    conn.setRequestMethod("GET");
	                    //连接超时
	                    conn.setConnectTimeout(5000);
	                    InputStream is = conn.getInputStream();
	                    InputStreamReader isr = new InputStreamReader(is);
	                    BufferedReader br = new BufferedReader(isr);

	                    String line;
	                    StringBuilder builder = new StringBuilder();
	                    while ((line = br.readLine()) != null) {
	                        builder.append(line).append("\n");
	                    }
	                    //关闭输入流
	                    br.close();
	                    isr.close();
	                    is.close();
	                    JSONObject resultJson = new JSONObject(builder.toString());

	                    /**
	                     * 当翻译结果无法正常返回时，可通过下面的控制台输出找到问题
	                     * 如果不用try/catch包裹，下面通过json解析不到text的值
	                     */
	                    try {
	                        String error_code = resultJson.getString("error_code");
	                        if (error_code != null) {
	                            Log.i("Notice", "doInBackground: "+error_code);
	                            Log.i("Notice", "doInBackground: "+resultJson.getString("error_msg"));
	                            callBack.onFailure("出错信息:" + resultJson.getString("error_msg"));
	                        }
	                    } catch (Exception e) {
	                        e.printStackTrace();
	                    }
	                    //获取翻译成功的结果
	                    JSONArray jsonArray = (JSONArray) resultJson.get("trans_result");//返回结果类型为MIXED LIST
	                    JSONObject dstJson = (JSONObject) jsonArray.get(0);
	                    text = dstJson.getString("dst");//获取返回结果类型是TEXT
	                    text = URLDecoder.decode(text, UTF8);//utf-8译码

	                } catch (IOException e) {
	                    e.printStackTrace();
	                } catch (JSONException e) {
	                    e.printStackTrace();
	                } finally {//若url连接异常，则断开连接
	                    if (conn != null) {
	                        conn.disconnect();
	                    }
	                }
	                return text;
	            }

	            @Override
	            protected void onPostExecute(String s) {
	                super.onPostExecute(s);
	                //翻译成功进行成功的回调
	                callBack.onSuccess(s);
	                System.out.println("onPostExecute  ---->  " + s);
	            }
	        }.execute();
	    }
	  	public static  void onTranslate(String needToTranslate,String to ,final TextView view){
			try {
				translate(needToTranslate, to, new TransApi() {
                    @Override
                    public void onSuccess(String result) {
                        view.setText(result);
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

