package com.example.bloggerapi;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class AdapterPost extends RecyclerView.Adapter<AdapterPost.HolderPost> {

    private Context context;
    private ArrayList<ModelPost> postArrayList;

    public AdapterPost(Context context, ArrayList<ModelPost> postArrayList) {
        this.context = context;
        this.postArrayList = postArrayList;
    }

    @NonNull
    @Override
    public HolderPost onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_post, parent, false);

        return new HolderPost(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPost holder, int position) {
        ModelPost model = postArrayList.get(position); // get data at position

        String authorName = model.getAuthorName();
        String content = model.getContent();
        String id = model.getId();
        String published = model.getPublished();
        String selfLink = model.getSelfLink();
        String title = model.getTitle();
        String updated = model.getUpdated();
        String url = model.getUrl();

        Document document = Jsoup.parse(content);

        //date formatting
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

        //set data
        holder.postTitle.setText(title);
        holder.postDesc.setText(document.text());
        holder.regionName.setText("By: " + authorName + " " + formattedDate);

        // on click for arrow button
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start activity intent
                Intent intent = new Intent(context, PostDescriptionActivity.class);
                intent.putExtra("postId", id); // id used to get post description in PostDescriptionActivity
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return postArrayList.size();
    }

    class HolderPost extends RecyclerView.ViewHolder{

        ImageButton btn_more;
        TextView postTitle, regionName, postDesc;

        public HolderPost(@NonNull View itemView){
            super (itemView);

           btn_more = itemView.findViewById(R.id.btn_more);
           postTitle = itemView.findViewById(R.id.postTitle);
           regionName = itemView.findViewById(R.id.regionName);
           postDesc = itemView.findViewById(R.id.postDesc);

        }
    }
}
