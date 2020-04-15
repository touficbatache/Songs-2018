package com.batache.songs.player;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.SparseArray;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.TransferListener;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

public class ResolvingDataSource implements DataSource {

  private static final String RESOLVABLE_SCHEME = "app";

  public static final class Factory implements DataSource.Factory {

    private final Context context;
    private final DefaultHttpDataSourceFactory wrappedFactory;

    public Factory(Context context, DefaultHttpDataSourceFactory wrappedFactory) {
      this.context = context;
      this.wrappedFactory = wrappedFactory;
    }

    @Override
    public DataSource createDataSource() {
      return new ResolvingDataSource(context, wrappedFactory.createDataSource());
    }
  }

  private Context context;
  private DefaultHttpDataSource wrappedDataSource;

  public ResolvingDataSource(Context context, HttpDataSource wrappedDataSource) {
    this.context = context;
    this.wrappedDataSource = (DefaultHttpDataSource) wrappedDataSource;
  }

  @Override
  public void addTransferListener(TransferListener transferListener) {
    wrappedDataSource.addTransferListener(transferListener);
  }

  @SuppressLint("StaticFieldLeak")
  @Override
  public long open(DataSpec dataSpec) {
    if (RESOLVABLE_SCHEME.equals(dataSpec.uri.getScheme())) {
//      DataSpec newDataSpec = new DataSpec(
//          Uri.parse("https://r2---sn-vbxgv-cxte.googlevideo.com/videoplayback?expire=1570053125&ei=pceUXf0exOHGAqi9r6AC&ip=178.135.95.227&id=o-AK8sa-L0yQXWe4bw7O5zT_g6pFGvQU0ELM1CxLOeQLAL&itag=140&source=youtube&requiressl=yes&mm=31%2C29&mn=sn-vbxgv-cxte%2Csn-hgn7rn7k&ms=au%2Crdu&mv=m&mvi=1&pl=22&initcwndbps=140000&mime=audio%2Fmp4&gir=yes&clen=14799115&dur=914.378&lmt=1569789649473501&mt=1570031402&fvip=1&keepalive=yes&fexp=23842630&c=WEB&txp=5531432&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cmime%2Cgir%2Cclen%2Cdur%2Clmt&sig=ALgxI2wwRQIgTTEnJkeF01FQVydnXAEsYFefQt_24smHTjUJbtFnaiwCIQDjyO-rzGT277zctnkrfT34qRRTYIj2A1RUotCxGGfQhA%3D%3D&lsparams=mm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Cinitcwndbps&lsig=AHylml4wRgIhAMwiomNe_RHE-xjrZZ2ZCHGFYXDvkFQi0Jy-8UqVZ8mmAiEAipEsOAr5PlLTeSWlAReQin5Ap7pBg2JQKzPzWb6d29g%3D"),
//          dataSpec.httpMethod,
//          dataSpec.httpBody,
//          dataSpec.absoluteStreamPosition,
//          dataSpec.position,
//          dataSpec.length,
//          dataSpec.key,
//          dataSpec.flags);
//      try {
//        wrappedDataSource.open(newDataSpec);
//      } catch (IOException e) {
//        e.printStackTrace();
//      }

      new YouTubeExtractor(context) {
        @Override
        protected void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta videoMeta) {
          if (ytFiles != null) {
            for (int i = 0, itag; i < ytFiles.size(); i++) {
              itag = ytFiles.keyAt(i);
              final YtFile ytFile = ytFiles.get(itag);

              // We only want audio: height == -1 :)
              if (ytFile.getFormat().getHeight() == -1) {
                DataSpec newDataSpec = new DataSpec(
                    Uri.parse(ytFile.getUrl()),
                    dataSpec.httpMethod,
                    dataSpec.httpBody,
                    dataSpec.absoluteStreamPosition,
                    dataSpec.position,
                    dataSpec.length,
                    dataSpec.key,
                    dataSpec.flags);
                AsyncTask.execute(() -> {
                  try {
                    wrappedDataSource.open(newDataSpec);
                  } catch (HttpDataSource.HttpDataSourceException e) {
                    e.printStackTrace();
                  }
                });
              }
            }
          }
        }
      }.extract(dataSpec.uri.toString(), true, false);
    }
    return 14799115;
  }

  @Override
  public int read(byte[] buffer, int offset, int readLength) throws IOException {
    return wrappedDataSource.read(buffer, offset, readLength);
  }

  @Nullable
  @Override
  public Uri getUri() {
    if (wrappedDataSource.getUri() == null) {
      return Uri.parse("");
    }
    return wrappedDataSource.getUri();
  }

  @Override
  public Map<String, List<String>> getResponseHeaders() {
    return wrappedDataSource.getResponseHeaders();
  }

  @Override
  public void close() throws IOException {
    wrappedDataSource.close();
  }
}