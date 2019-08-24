package com.batache.songs.network;

import android.content.Context;
import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.lang.ref.WeakReference;

import com.batache.songs.util.NetworkUtils;

public class HTMLDataFetcher extends AsyncTask<Document, Document, Document> {

  private WeakReference<Context> context;

  private String url;
  private HTMLDataFetcherCallback htmlDataFetcherCallback;

  public HTMLDataFetcher(Context context, String url, HTMLDataFetcherCallback htmlDataFetcherCallback) {
    this.context = new WeakReference<>(context);

    this.url = url;
    this.htmlDataFetcherCallback = htmlDataFetcherCallback;
  }

  @Override
  protected Document doInBackground(Document... args) {

    Document result = null;
    if(NetworkUtils.isOnline(context.get())) {
      try {
        result = Jsoup.connect(url).get();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return result;
  }

  @Override
  protected void onPostExecute(Document result) {
    super.onPostExecute(result);

    if(result == null) {
      this.htmlDataFetcherCallback.onFailure();
      return;
    }
    this.htmlDataFetcherCallback.onSuccess(result);
  }

  public interface HTMLDataFetcherCallback {
    // method called when server's data get fetched
    void onSuccess(Document result);

    void onFailure();
  }

}