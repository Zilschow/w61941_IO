package com.example.bloggerapi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AdapterComment extends RecyclerView.Adapter<AdapterComment.HolderComment>{

    private Context context;
    private ArrayList<ModelComment> commentArrayList;

    public AdapterComment(Context context, ArrayList<ModelComment> commentArrayList) {
        this.context = context;
        this.commentArrayList = commentArrayList;
    }

    @NonNull
    @Override
    public HolderComment onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate row_comment layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_comment, parent, false);

        return new HolderComment(view);
    }



    @Override
    public void onBindViewHolder(@NonNull HolderComment holder, int position) {

        //Get data

        ModelComment modelComment = commentArrayList.get(position);
        String id = modelComment.getId();
        String name = modelComment.getName();
        String published = modelComment.getPublished();
        String comment = modelComment.getComment();
        String image = modelComment.getProfileImage();

        //date conversion

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

        //Set data
        holder.nameTV.setText(name);
        holder.dateTV.setText(formattedDate);
        holder.commentTV.setText(comment);
        try{
            Picasso.get().load(image).placeholder(R.drawable.ic_baseline_person_24).into(holder.profileIV);
        }catch(Exception e){
            holder.profileIV.setImageResource(R.drawable.ic_baseline_person_24);
        }

    }

    @Override
    public int getItemCount() {
        return commentArrayList.size(); // returns number of comments
    }

    //view holder for row_comment
    class HolderComment extends RecyclerView.ViewHolder{

        // view declaration

        ImageView profileIV;
        TextView nameTV, dateTV, commentTV;

        public HolderComment(@NonNull View itemView) {
            super(itemView);

            //init views
            profileIV = itemView.findViewById(R.id.profileIV);
            nameTV = itemView.findViewById(R.id.nameTV);
            dateTV = itemView.findViewById(R.id.dateTV);
            commentTV = itemView.findViewById(R.id.commentTV);
        }
    }
}
