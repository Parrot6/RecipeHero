package com.example.testapp2;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapterTasty extends RecyclerView.Adapter<SearchAdapterTasty.ViewHolder> {

    final private MyClickListener mOnClickListener;
    private LayoutInflater mInflater;
    //ArrayList<String> ratings = new ArrayList<String>();
    //ArrayList<String> ratingVotes = new ArrayList<String>();
    ArrayList<String> titles = new ArrayList<String>();
    //ArrayList<String> descriptions = new ArrayList<String>();
    ArrayList<String> links = new ArrayList<String>();
    //ArrayList<String> vidlinks = new ArrayList<String>();
    List<Bitmap> imgs = new ArrayList<>();
    List<Boolean> isAdded = new ArrayList<>();
    List<Boolean> isViewed = new ArrayList<>();
    private Boolean isDeadSearch = false;
    // data is passed into the constructor
    public SearchAdapterTasty(Context context, ArrayList<ArrayList<String>> dataPack, List<Bitmap> imgs,  MyClickListener listener) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(dataPack.size() > 0) {
            titles = dataPack.get(0);
            links = dataPack.get(1);
            if(links.size()==0) isDeadSearch = true;
            //vidlinks = dataPack.get(2);
            this.imgs = imgs;
        }
        for(int i = 0; i < getItemCount(); i++){
            isAdded.add(false);
            isViewed.add(false);
        }
        this.mOnClickListener = listener;
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.scraped_recipe_preview, parent, false);
        ViewHolder holder = new ViewHolder(view, mOnClickListener);

        return holder;
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.scrapedTitle.setText(titles.get(position));
        holder.scrapedImg.setImageBitmap(imgs.get(position));
        holder.scrapedDescription.setVisibility(View.GONE);
        if(isDeadSearch) return;
        holder.scrapedNumRatings.setVisibility(View.GONE);
        holder.scrapedRating.setVisibility(View.GONE);
        if(!isViewed.get(position)) {
            holder.playVideo.setImageResource(R.drawable.ic_iconmonstr_video_14);
        } else holder.playVideo.setImageResource(R.drawable.ic_iconmonstr_video_14watched);
        if(!isAdded.get(position)) {
            holder.add.setImageResource(R.drawable.ic_iconmonstr_add_recipe);
        } else holder.add.setImageResource(R.drawable.ic_iconmonstr_recipe_added);
        holder.viewRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.listener.onView(position, holder.add, isAdded);
            }
        });
        /*if(vidlinks.get(position).equals("")){*/
            holder.playVideo.setVisibility(View.GONE);
/*        } else {
            holder.playVideo.setVisibility(View.VISIBLE);
            holder.playVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.playVideo.setImageResource(R.drawable.ic_iconmonstr_video_14watched);;
                    isViewed.set(position, true);
                    Log.e("VIDEO","attemptPlay");
                    holder.listener.playVideo(position);
                    //load video
                }
            });
        }*/
        holder.add.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                isAdded.set(position, true);
                holder.listener.quickAdd(position, holder.add);
            }
        });
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return titles.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView scrapedImg;
        private TextView scrapedTitle;
        private TextView scrapedNumRatings;
        private TextView scrapedDescription;
        private RatingBar scrapedRating;
        private ImageView viewRec;
        private ImageView add;
        private ImageView playVideo;
        MyClickListener listener;
        ViewHolder(View itemView, MyClickListener myClickListener) {
            super(itemView);
            scrapedImg = itemView.findViewById(R.id.image_scraped_RecipeImage);
            scrapedTitle = itemView.findViewById(R.id.text_scraped_recipeTitle);
            scrapedNumRatings = itemView.findViewById(R.id.text_scraped_numberOfRatings);
            scrapedDescription = itemView.findViewById(R.id.text_scraped_recipeDescription);
            scrapedRating = itemView.findViewById(R.id.ratingBar_scraped_rating);
            viewRec = itemView.findViewById(R.id.button_scraping_view);
            add = itemView.findViewById(R.id.button_scraping_add);
            playVideo = itemView.findViewById(R.id.button_scraping_playVid);
            this.listener = myClickListener;
            //add more listeners here
        }

    }


    // parent activity will implement this method to respond to click events
    public interface MyClickListener {
        void onView(int layoutPosition, ImageView toggleMe, List<Boolean> isAdded);
        void quickAdd(int layoutPosition, ImageView toggleMe);
        void playVideo(int layoutPosition);
    }
}