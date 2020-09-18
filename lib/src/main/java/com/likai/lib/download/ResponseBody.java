package com.likai.lib.download;


import android.util.Log;

import java.io.IOException;

import okhttp3.MediaType;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * Description: 带进度 下载请求体
 * Created by likai on 2017/11/30.
 */
public class ResponseBody extends okhttp3.ResponseBody {

    private okhttp3.ResponseBody responseBody;

    private DownloadListener downloadListener;

    // BufferedSource 是okio库中的输入流，这里就当作inputStream来使用。
    private BufferedSource bufferedSource;

    public ResponseBody(okhttp3.ResponseBody responseBody, DownloadListener downloadListener) {
        this.responseBody = responseBody;
        this.downloadListener = downloadListener;
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            long totalBytesRead = 0L;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                // read() returns the number of bytes read, or -1 if this source is exhausted.
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                Log.e("download", "read: "+ (int) (totalBytesRead * 100 / responseBody.contentLength()));
                if (null != downloadListener) {
                    if (bytesRead != -1) {
                        downloadListener.onProgress((int) (totalBytesRead * 100 / responseBody.contentLength()));
                    }

                }
                return bytesRead;
            }
        };

    }
}
