package  ru.ifmo.ctddev.vanyan.imageseeker;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ru.ifmo.ctddev.vanyan.imageseeker.utilities.NetworkUtils;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private EditText sb;
    private TextView sr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sb = findViewById(R.id.search_box);
        sr = findViewById(R.id.search_results);
    }


    private void makeSearchQuery() {
        String query = sb.getText().toString();
        URL searchUrl = NetworkUtils.buildUrl(query);
        new QueryTask().execute(searchUrl);
    }

    public class QueryTask extends AsyncTask<URL, Void, String> {
        @Override
        protected String doInBackground(URL... urls) {
            URL searchURL = urls[0];
            String searchResults = null;
            try {
                searchResults = NetworkUtils.getResponseFromHttpUrl(searchURL);
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            if (searchResults == null) return null;
            return searchResults.substring(15, searchResults.length() - 1);
        }

        @Override
        protected void onPostExecute(String JSONString) {
           try {
               JSONObject allJSON = new JSONObject(JSONString);
               JSONArray items = allJSON.getJSONArray("items");
               StringBuilder answer = new StringBuilder();
               for (int i = 0; i < items.length(); i++ ) {
                   JSONObject cur = items.getJSONObject(i);

                   answer.append(cur.getString("title"));
                   answer.append('\n');
                   answer.append(cur.getString("link"));
                   answer.append("\n\n");
               }
               sr.setText(answer.toString());
            } catch (JSONException e) {
                e.printStackTrace();
           }
        }
    }


    public void go(View view) {
        makeSearchQuery();
    }
}
