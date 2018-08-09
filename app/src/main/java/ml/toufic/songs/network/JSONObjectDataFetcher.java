package ml.toufic.songs.network;

import android.os.AsyncTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class JSONObjectDataFetcher extends AsyncTask<JSONObject, JSONObject, JSONObject> {

  private String url;
  private JSONDataFetcherCallback jsonDataFetcherCallback;

  public JSONObjectDataFetcher(String url, JSONDataFetcherCallback jsonDataFetcherCallback) {
    this.url = url;
    this.jsonDataFetcherCallback = jsonDataFetcherCallback;
  }

  @Override
  protected JSONObject doInBackground(JSONObject... args) {

    JSONObject result = null;

    try {
      URL urlUrl = new URL(this.url);

      HttpURLConnection httpURLConnection = (HttpURLConnection) urlUrl.openConnection();
      InputStream inputStream = httpURLConnection.getInputStream();

      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

      String line;
      StringBuilder data = new StringBuilder();
      while ((line = bufferedReader.readLine()) != null) {
        data.append(line);
      }

      try {
        result = new JSONObject(data.toString());
      } catch (JSONException e) {
        e.printStackTrace();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return result;
  }

  @Override
  protected void onPostExecute(JSONObject result) {
    super.onPostExecute(result);

    if(result == null) {
      this.jsonDataFetcherCallback.onError();
      return;
    }
    this.jsonDataFetcherCallback.onSuccess(result);
  }

  public interface JSONDataFetcherCallback {
    // method called when server's data get fetched
    void onSuccess(JSONObject result);

    void onError();
  }
}

