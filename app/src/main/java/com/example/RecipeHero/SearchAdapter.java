package com.example.RecipeHero;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    final private MyClickListener mOnClickListener;
    private LayoutInflater mInflater;
    ArrayList<String> ratings = new ArrayList<String>();
    ArrayList<String> ratingVotes = new ArrayList<String>();
    ArrayList<String> titles = new ArrayList<String>();
    ArrayList<String> descriptions = new ArrayList<String>();
    ArrayList<String> links = new ArrayList<String>();
    ArrayList<String> vidlinks = new ArrayList<String>();
    List<Bitmap> imgs = new ArrayList<>();
    List<Boolean> isAdded = new ArrayList<>();
    List<Boolean> isViewed = new ArrayList<>();
    private Boolean isDeadSearch = false;
    int pagenum = 1;
    // data is passed into the constructor
    public SearchAdapter(Context context, ArrayList<ArrayList<String>> dataPack, List<Bitmap> imgs, MyClickListener listener, List<Boolean> isAdded,List<Boolean> isViewed, int pagenum) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(dataPack.size() > 0) {
            titles = dataPack.get(0);
            descriptions = dataPack.get(1);
            ratings = dataPack.get(2);
            ratingVotes = dataPack.get(3);
            links = dataPack.get(4);

            if(links.size()==0 || titles.size()==0) isDeadSearch = true;
            vidlinks = dataPack.get(5);
            this.imgs = imgs;
            this.pagenum = pagenum;
        }
        this.isAdded = isAdded;
        this.isViewed = isViewed;
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
        holder.scrapedDescription.setText(descriptions.get(position));
        if(isDeadSearch) return;
        if(position == getItemCount()-1) {
            holder.nextPageControls.setVisibility(View.VISIBLE);
            holder.currentPage.setText(String.valueOf(pagenum));
        } else holder.nextPageControls.setVisibility(View.GONE);
        holder.scrapedImg.setImageBitmap(imgs.get(position));
        holder.scrapedNumRatings.setText(ratingVotes.get(position));
        holder.scrapedRating.setRating(Float.parseFloat(ratings.get(position)));
        if(!isViewed.get(position)) {
            holder.playVideo.setImageResource(R.drawable.ic_iconmonstr_video_14);
        } else holder.playVideo.setImageResource(R.drawable.ic_iconmonstr_video_14watched);
        if(!isAdded.get(position)) {
            holder.add.setImageResource(R.drawable.ic_iconmonstr_add_recipe);
            holder.add.setEnabled(true);
        } else {
            holder.add.setEnabled(false);
            holder.add.setImageResource(R.drawable.ic_iconmonstr_recipe_added);
        }

        if(vidlinks.size() == 0 || vidlinks.get(position).equals("")){
            holder.playVideo.setVisibility(View.GONE);
        } else {
            holder.playVideo.setVisibility(View.VISIBLE);
        }

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return titles.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView scrapedImg;
        private TextView scrapedTitle;
        private TextView scrapedNumRatings;
        private TextView scrapedDescription;
        private RatingBar scrapedRating;
        private ImageView viewRec;
        private ImageView add;
        private ImageView playVideo;
        private ConstraintLayout nextPageControls;
        private Button currentPage;
        private Button previousPage;
        MyClickListener listener;
        ViewHolder(View itemView, MyClickListener myClickListener) {
            super(itemView);
            currentPage = itemView.findViewById(R.id.button_currentPage);
            currentPage.setOnClickListener(this);
            previousPage = itemView.findViewById(R.id.button_previousPage);
            previousPage.setOnClickListener(this);
            nextPageControls = itemView.findViewById(R.id.nextPageControls);
            scrapedImg = itemView.findViewById(R.id.image_scraped_RecipeImage);
            scrapedTitle = itemView.findViewById(R.id.text_scraped_recipeTitle);
            scrapedNumRatings = itemView.findViewById(R.id.text_scraped_numberOfRatings);
            scrapedDescription = itemView.findViewById(R.id.text_scraped_recipeDescription);
            scrapedRating = itemView.findViewById(R.id.ratingBar_scraped_rating);
            viewRec = itemView.findViewById(R.id.button_scraping_view);
            viewRec.setOnClickListener(this);
            add = itemView.findViewById(R.id.button_scraping_add);
            add.setOnClickListener(this);
            playVideo = itemView.findViewById(R.id.button_scraping_playVid);
            playVideo.setOnClickListener(this);
            itemView.findViewById(R.id.button_nextPage).setOnClickListener(this);
            this.listener = myClickListener;
            if(isDeadSearch) {
                playVideo.setVisibility(View.GONE);
                viewRec.setVisibility(View.GONE);
                add.setVisibility(View.GONE);
            } else {
                playVideo.setVisibility(View.VISIBLE);
                viewRec.setVisibility(View.VISIBLE);
                add.setVisibility(View.VISIBLE);
            }
            //add more listeners here
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.button_scraping_add:
                    isAdded.set(getAdapterPosition(), true);
                    listener.quickAdd(getAdapterPosition());
                    view.setEnabled(false);
                    ((ImageView) view).setImageResource(R.drawable.ic_iconmonstr_recipe_added);
                    isAdded.set(getAdapterPosition(), true);
                    break;
                case R.id.button_scraping_playVid:
                    ((ImageView) view).setImageResource(R.drawable.ic_iconmonstr_video_14watched);;
                    isViewed.set(getAdapterPosition(), true);
                    listener.playVideo(getAdapterPosition());
                    //load video
                    break;
                case R.id.button_currentPage:
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(view.getContext());
                    builder1.setTitle("Page Jump");
                    builder1.setMessage("what page would you like to jump to");
                    builder1.setCancelable(true);
                    // Set up the input
                    final EditText input = new EditText(view.getContext());
                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
                    input.setGravity(Gravity.CENTER);
                    input.setBackground(ContextCompat.getDrawable(view.getContext() ,R.color.recipeListItem));
                    builder1.setView(input);
                    builder1.setPositiveButton(
                            "Confirm",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    try {
                                        int pageTo = Integer.parseInt(input.getText().toString());
                                        listener.nextPage(pageTo - pagenum);
                                        dialog.cancel();
                                    } catch (Exception e){
                                        dialog.cancel();
                                    }
                                }
                            });
                    builder1.setNegativeButton(
                            "Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                    break;
                case R.id.button_scraping_view:
                    listener.onView(getAdapterPosition());
                    break;
                case R.id.button_nextPage:
                    listener.nextPage(1);
                    break;
                case R.id.button_previousPage:
                    listener.nextPage(-1);
                    break;
                default:
                    break;
            }
        }
    }


    // parent activity will implement this method to respond to click events
    public interface MyClickListener {
        void onView(int layoutPosition);
        void quickAdd(int layoutPosition);
        void playVideo(int layoutPosition);
        void nextPage(int incrementPageBy);
    }
}