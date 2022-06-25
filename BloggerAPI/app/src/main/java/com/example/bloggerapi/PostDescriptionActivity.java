package com.example.bloggerapi;

import static javax.xml.transform.OutputKeys.ENCODING;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PostDescriptionActivity extends AppCompatActivity {

    private TextView titleTV, publishedInfo;
    private WebView webView;
    private RecyclerView commentsRV;

    private String postId; // intent from AdapterPost
    private static final String TAG = "POST_DETAILS_TAG";
    private static final String TAG_COMMENTS = "POST_COMMENTS_TAG";


    private ArrayList<ModelComment> commentArrayList;
    private AdapterComment adapterComment;

    //actionbar
    private ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_description);

        //init actionbar
        actionBar = getSupportActionBar();
        actionBar.setTitle("Post Description");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // init views
        titleTV = findViewById(R.id.titleTV);
        publishedInfo = findViewById(R.id.publishedInfo);
        webView = findViewById(R.id.webView);
        commentsRV = findViewById(R.id.commentsRV);

        // GET postId from AdapterPost intent
        postId = getIntent().getStringExtra("postId");
        Log.d(TAG, "onCreate: "+ postId);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        loadPostDescription();
    }

    private void loadPostDescription() {
        String url = "https://www.googleapis.com/blogger/v3/blogs/"+Constants.BLOG_ID
                +"/posts/"+postId
                +"?key="+Constants.API_KEY;
        Log.d(TAG, "loadPostDescription: URL:"+ url);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "onResponse: "+response);
                //response 200, returns JSON object

                try{
                    JSONObject jsonObject = new JSONObject(response);

                    //get data
                    String title = jsonObject.getString("title");
                    String published = jsonObject.getString("published");
                    String content = jsonObject.getString("content");
                    String url = jsonObject.getString("url");
                    String displayName = jsonObject.getJSONObject("author").getString("displayName");

                    // date conversion
                    String gmtDate = published;
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); //   2022-06-06T06:53:00-07:00
                    SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy K:mm a"); // 06/06/2022 15:53
                    String formattedDate = "";
                    try{
                        Date date = dateFormat.parse(gmtDate);
                        formattedDate = dateFormat2.format(date);


                    }catch(Exception e){
                        formattedDate = published;
                        e.printStackTrace();
                    }

                    // set data
                    actionBar.setSubtitle(title);
                    titleTV.setText(title);
                    publishedInfo.setText("By " + displayName + " "+ formattedDate);

                    //load content to webview
                    webView.loadDataWithBaseURL(null, content, "text/html", ENCODING, null);

                    loadComments();


                }catch (Exception e){
                    Log.d(TAG, "onResponse: "+e.getMessage());
                    Toast.makeText(PostDescriptionActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // failed response, show error
                Toast.makeText(PostDescriptionActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        //queue request

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void loadComments(){
        String url = "https://www.googleapis.com/blogger/v3/blogs/"+Constants.BLOG_ID+"/posts/"+postId+"/comments?key="+Constants.API_KEY;
        Log.d(TAG_COMMENTS, "loadComments: URL: "+ url);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //response succeeded
                Log.d(TAG_COMMENTS, "onResponse: "+response);

                commentArrayList = new ArrayList<>();
                commentArrayList.clear();

                try{
                    //returns JSONObject
                    JSONObject jsonObject = new JSONObject(response);

                    JSONArray jsonArrayItems = jsonObject.getJSONArray("items");
                    for(int i=0; i<jsonArrayItems.length();i++){
                        //get specific comment
                        JSONObject jsonObjectComment = jsonArrayItems.getJSONObject(i);
                        // get data from JSONObject
                        String id = jsonObjectComment.getString("id");
                        String published = jsonObjectComment.getString("published");
                        String content = jsonObjectComment.getString("content");
                        String displayName = jsonObjectComment.getJSONObject("author").getString("displayName");
                        String profileImage = "http:" + jsonObjectComment.getJSONObject("author").getJSONObject("image").getString("url");
                        Log.d("TAG_IMAGE_URL", "onResponse: "+profileImage );

                        //adding data to model

                        ModelComment modelComment =new ModelComment(
                                ""+id,
                                ""+displayName,
                                ""+profileImage,
                                ""+published,
                                ""+content
                        );
                        //adding model to array
                        commentArrayList.add(modelComment);

                    }
                    //adapter
                    adapterComment = new AdapterComment(PostDescriptionActivity.this, commentArrayList);
                    //assign adapter to RV
                    commentsRV.setAdapter(adapterComment);



                }catch (Exception e){
                    Log.d(TAG_COMMENTS, "onResponse: "+e.getMessage());
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //response failed
                Log.d(TAG, "onErrorResponse: "+error.getMessage());

            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //go back to previous activity
        return super.onSupportNavigateUp();
    }
}