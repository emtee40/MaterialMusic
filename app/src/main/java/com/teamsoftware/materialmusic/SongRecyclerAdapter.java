package com.teamsoftware.materialmusic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.mpatric.mp3agic.Mp3File;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import cn.gavinliu.android.lib.shapedimageview.ShapedImageView;

public class SongRecyclerAdapter extends RecyclerView.Adapter<SongRecyclerAdapter.SongViewHolder> {
    private Context mContext;
    private List<File> songsList;
    Fragment frag;
    MetadataCacher cache;
    private int lastPosition = -1;
    public ClickInterface clickInterface;

    public SongRecyclerAdapter(Context context, ArrayList<File> list, Fragment frag, MetadataCacher cache, ClickInterface clickInterface) {
        mContext = context;
        songsList = list;
        this.frag = frag;
        this.cache = cache;
        this.clickInterface = clickInterface;
    }
    public interface ClickInterface {
        void onSongClick(int position);
    }

    @Override
    public SongRecyclerAdapter.SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout, parent, false);
        SongViewHolder viewHolder = new SongViewHolder(view, frag);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(SongRecyclerAdapter.SongViewHolder holder, int position) {
        String title = "";
        Bitmap art = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_audiotrack_black_24dp);
        if (cache.getSongCache().size() > position) {
            title = cache.getMetadataAll(cache.getSongCache().get(position)).get("Title");
        }
        if (cache.getAlbumCache().size() > position) {
            art = cache.getAlbumCache().get(position);
        }
        setAnimation(holder.itemView, position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickInterface.onSongClick(position);
            }
        });

        holder.bindSong(title, art);
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return songsList.size();
    }

    public class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ShapedImageView image;
        private TextView name;
        private Context mContext;
        private Fragment frag;

        public SongViewHolder(View itemView, Fragment frag) {
            super(itemView);
            mContext = itemView.getContext();
            image = (ShapedImageView) itemView.findViewById(R.id.image_view);
            name = (TextView) itemView.findViewById(R.id.text_view);
            this.frag = frag;
        }

        public void bindSong(String title, Bitmap art) {
            image.setImageBitmap(art);
            name.setText(title);
        }
        @Override
        public void onClick(View v) {
                clickInterface.onSongClick(getAdapterPosition());
        }
    }
}
