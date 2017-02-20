package com.rmtd.melo.welcomedemo;

import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by melo on 16/6/21.
 */

public class OkhttpHelper {
    public static OkHttpClient okHttpClient;

    public interface MyCallback {
        void onFail();

        void onSuccess(String result);
    }

    static {
        okHttpClient = new OkHttpClient();
    }

    public static void doGet(final String url, final MyCallback myCallback) {
        useOkhttp(url, myCallback);
//        useUrlConnection(urls, myCallback);
    }

    private static void useUrlConnection(final String urls, final MyCallback myCallback) {
        new Thread(new Runnable() {
            @Override
            public void run() {


                Handler handler = new Handler(Looper.getMainLooper());
                StringBuffer buffer = null;
                try {
                    URL url = new URL(urls);
                    URLConnection urlConnection = url.openConnection();
                    HttpURLConnection httpurlconn = (HttpURLConnection) urlConnection;
                    httpurlconn.setRequestMethod("GET");
                    //设置编码格式
                    //设置接受的数据类型
                    httpurlconn.setRequestProperty("Accept-Charset", "utf-8");
                    //设置可以序列化的java对象
                    httpurlconn.setRequestProperty("Context-Type", "application/x-www-form-urlencoded");

                    int code = httpurlconn.getResponseCode();
//            Log.d(TAG, "获得的状态码是：" + code);
                    if (code == HttpsURLConnection.HTTP_OK) {
                        InputStream is = httpurlconn.getInputStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(is));
                        String line = br.readLine();
                        buffer = new StringBuffer();
                        while (line != null) {
                            buffer.append(line);
                            line = br.readLine();
                        }
                        System.out.println(buffer.toString());
                        final String result = buffer.toString();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                myCallback.onSuccess(result);
                            }
                        });
                    } else {
//                Log.d(TAG, "网络连接错误");
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                myCallback.onFail();
                            }
                        });
                    }

                } catch (MalformedURLException e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            myCallback.onFail();
                        }
                    });
                    e.printStackTrace();
                } catch (IOException e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            myCallback.onFail();
                        }
                    });
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static void useOkhttp(String url, final MyCallback myCallback) {
        //创建网络请求
        Request request = new Request.Builder().url(url).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //非UI线程
                //创建一个UI线程中的handler
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        myCallback.onFail();
                    }
                });

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                Handler handler = new Handler(Looper.getMainLooper());

                if (response.code() == 200) {
                    final String result = response.body().string();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            myCallback.onSuccess(result);
                        }
                    });

                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            myCallback.onFail();
                        }
                    });
                }
            }
        });
    }
}
