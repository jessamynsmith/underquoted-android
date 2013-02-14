package jessamynsmith.underquoted;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	class RetrieveQuotationTask extends AsyncTask<String, Void, String> {

	    protected String doInBackground(String... urls) {
			StringBuilder builder = new StringBuilder();
			HttpClient client = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(
					"https://underquoted.herokuapp.com/api/v1/quotations/?format=json&random=true&limit=1");
			try {
				HttpResponse response = client.execute(httpGet);
				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();
				if (statusCode == 200) {
					HttpEntity entity = response.getEntity();
					InputStream content = entity.getContent();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(content));
					String line;
					while ((line = reader.readLine()) != null) {
						builder.append(line);
					}
				} else {
					Log.e(RetrieveQuotationTask.class.toString(),
							"Failed to download data");
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return builder.toString();
	    }

	    protected void onPostExecute(String result) {
			try {
				JSONObject quotationJson = new JSONObject(result);
				JSONObject meta = quotationJson.getJSONObject("meta");
				Integer totalObjects = meta.getInt("total_count");
				
				if (totalObjects > 0) {
					JSONArray objects = quotationJson.getJSONArray("objects");
					JSONObject quotation = objects.getJSONObject(0);
					JSONObject author = quotation.getJSONObject("author");
					
					String quotationText = String.format("%s\n\n\t- %s", quotation.getString("text"), author.getString("name"));

					Log.i(RetrieveQuotationTask.class.getName(), quotationText);
					
					TextView editText = (TextView) findViewById(R.id.quotation);
					editText.setText(quotationText);
				} else {
					Log.i(RetrieveQuotationTask.class.getName(), "No objects received");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
	    }
	 };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Attempt at getting custom colors in title bar
        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_main);
        //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.mytitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public void getQuotation(View view) {
    	new RetrieveQuotationTask().execute("");
    }
}
