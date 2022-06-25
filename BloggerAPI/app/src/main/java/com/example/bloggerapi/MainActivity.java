package com.example.bloggerapi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView postsRV;
    private AppCompatButton loadmoreBtn;
    private EditText searchEdt;
    private ImageButton searchBtn;

    private String url = ""; // url for getting posts
    private String nextToken = ""; // next page token for more posts
    private boolean isSearch = false;

    private ArrayList<ModelPost> postArrayList;
    private AdapterPost adapterPost;

    private ProgressDialog progressDialog;

    private static final String TAG = "MAIN_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init Views
        postsRV = findViewById(R.id.postsRV);
        loadmoreBtn = findViewById(R.id.loadmoreBtn);
        searchBtn = findViewById(R.id.searchBtn);
        searchEdt = findViewById(R.id.searchEdt);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");

        postArrayList = new ArrayList<>();
        postArrayList.clear();

        loadPosts();

        // on click for new page button
        loadmoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // text from EditText to string
                String query = searchEdt.getText().toString().trim();
                if (TextUtils.isEmpty(query)) {
                    loadPosts();
                } else {
                    searchPosts(query);
                }
            }
        });

        // on click for search button
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextToken = "";
                url = "";

                postArrayList = new ArrayList<>();
                postArrayList.clear();
                adapterPost.notifyDataSetChanged();


                // text from EditText to string
                String query = searchEdt.getText().toString().trim();
                if (TextUtils.isEmpty(query)) {
                    loadPosts();
                } else {
                    searchPosts(query);
                }
            }
        });

    }

    private void searchPosts(String query) {
        isSearch = true;
        Log.d(TAG, "searchPosts: isSearch: " + isSearch);

        progressDialog.show();
        if (nextToken.equals("")) {
            Log.d(TAG, "searchPosts: Next Page token is empty, no more pages");
            url = "https://www.googleapis.com/blogger/v3/blogs/" + Constants.BLOG_ID
                    + "/posts/search?q=" + query
                    + "&key=" + Constants.API_KEY;
        } else if (nextToken.equals("end")) {
            Log.d(TAG, "searchPosts: Next Page token is empty- end");
            Toast.makeText(this, "No more posts available", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            return;
        } else {
            Log.d(TAG, "searchPosts: Next token: " + nextToken);
            url = "https://www.googleapis.com/blogger/v3/blogs/" + Constants.BLOG_ID
                    + "/posts/search?q=" + query
                    + "&pageToken=" + nextToken
                    + "&key=" + Constants.API_KEY;
        }
        Log.d(TAG, "searchPosts: URL: " + url);

        // data request with GET

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //
                progressDialog.dismiss();
                Log.d(TAG, "onResponse: " + response);

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    try {
                        nextToken = jsonObject.getString("nextPageToken");
                        Log.d(TAG, "onResponse: NextPageToken: " + nextToken);
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "End of page", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onResponse: Reached the end of a page" + e.getMessage());
                        nextToken = "end";
                    }
                    // getting data from Json
                    JSONArray jsonArray = jsonObject.getJSONArray("items");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            // data GET
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            String id = jsonObject1.getString("id");
                            String title = jsonObject1.getString("title");
                            String content = jsonObject1.getString("content");
                            String published = jsonObject1.getString("published");
                            String updated = jsonObject1.getString("updated");
                            String url = jsonObject1.getString("url");
                            String selfLink = jsonObject1.getString("selfLink");
                            String authorName = jsonObject1.getJSONObject("author").getString("displayName");

                            // data SET
                            ModelPost modelPost = new ModelPost("" + authorName,
                                    "" + content,
                                    "" + id,
                                    "" + published,
                                    "" + selfLink,
                                    "" + title,
                                    "" + updated,
                                    "" + url);

                            // add to list
                            postArrayList.add(modelPost);
                        } catch (Exception e) {
                            Log.d(TAG, "onResponse: 1: " + e.getMessage());
                            Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    // adapter
                    adapterPost = new AdapterPost(MainActivity.this, postArrayList);
                    postsRV.setAdapter(adapterPost);
                    progressDialog.dismiss();
                } catch (Exception e) {
                    Log.d(TAG, "onResponse: 2: " + e.getMessage());
                    Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: " + error.getMessage());
                Toast.makeText(MainActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
        // queue request
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void loadPosts() {
        isSearch = false;
        Log.d(TAG, "loadPosts: isSearch: " + isSearch);

        progressDialog.show();
        if (nextToken.equals("")) {
            Log.d(TAG, "loadPosts: Next Page token is empty, no more pages");
            url = "https://www.googleapis.com/blogger/v3/blogs/" + Constants.BLOG_ID
                    + "/posts?maxResults=" + Constants.MAX_POST_RESULT
                    + "&key=" + Constants.API_KEY;
        } else if (nextToken.equals("end")) {
            Log.d(TAG, "loadPosts: Next Page token is empty- end");
            Toast.makeText(this, "No more posts available", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            return;
        } else {
            Log.d(TAG, "loadPosts: Next token: " + nextToken);
            url = "https://www.googleapis.com/blogger/v3/blogs/" + Constants.BLOG_ID
                    + "/posts?maxResults=" + Constants.MAX_POST_RESULT
                    + "&pageToken=" + nextToken
                    + "&key=" + Constants.API_KEY;
        }
        Log.d(TAG, "loadPost: URL: " + url);

        // data request with GET
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //
                progressDialog.dismiss();
                Log.d(TAG, "onResponse: " + response);

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    try {
                        nextToken = jsonObject.getString("nextPageToken");
                        Log.d(TAG, "onResponse: NextPageToken: " + nextToken);
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "End of page", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onResponse: Reached the end of a page" + e.getMessage());
                        nextToken = "end";
                    }
                    // getting data from Json
                    JSONArray jsonArray = jsonObject.getJSONArray("items");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            // data GET
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            String id = jsonObject1.getString("id");
                            String title = jsonObject1.getString("title");
                            String content = jsonObject1.getString("content");
                            String published = jsonObject1.getString("published");
                            String updated = jsonObject1.getString("updated");
                            String url = jsonObject1.getString("url");
                            String selfLink = jsonObject1.getString("selfLink");
                            String authorName = jsonObject1.getJSONObject("author").getString("displayName");

                            // data SET
                            ModelPost modelPost = new ModelPost("" + authorName,
                                    "" + content,
                                    "" + id,
                                    "" + published,
                                    "" + selfLink,
                                    "" + title,
                                    "" + updated,
                                    "" + url);

                            // add to list
                            postArrayList.add(modelPost);
                        } catch (Exception e) {
                            Log.d(TAG, "onResponse: 1: " + e.getMessage());
                            Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }

                    // adapter
                    adapterPost = new AdapterPost(MainActivity.this, postArrayList);
                    postsRV.setAdapter(adapterPost);
                    progressDialog.dismiss();
                } catch (Exception e) {
                    Log.d(TAG, "onResponse: 2: " + e.getMessage());
                    Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: " + error.getMessage());
                Toast.makeText(MainActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
        // queue request
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}