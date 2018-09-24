package com.teamsoftware.materialmusic;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.Fragment;
import rm.com.audiowave.AudioWaveView;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SongRecyclerAdapter.ClickInterface {

    private BottomNavigationView navbar;
    private AppBarLayout appBar;
    private FrameLayout container;
    private MetadataCacher cache;
    private View viewLayout;
    private boolean isPermissionChecked;
    private SongManager songManager;
    private ProgressDialog progressDialog;
    private MediaWrapper mediaWrapper;
    private ArrayList<File> allSongs;
    private Fragment songFrag, albumFrag, artistFrag, currentFrag;
    private Intent playIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isPermissionChecked = (PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED);

        if (isPermissionChecked) {
            setReference();
        }
        checkPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setReference();
        }
    }

    private void checkPermissions() {
        if ((PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED)) {
            //good to go
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        }
    }

    private void setReference() {
        viewLayout = LayoutInflater.from(this).inflate(R.layout.activity_main, container);

        SlidingUpPanelLayout lay = (SlidingUpPanelLayout) findViewById(R.id.slide);

        //navbar = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        container = (FrameLayout) findViewById(R.id.container);
        appBar = (AppBarLayout) findViewById(R.id.my_app_bar);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        StateListAnimator stateListAnimator = new StateListAnimator();
        stateListAnimator.addState(new int[0], ObjectAnimator.ofFloat(appBar, "elevation", 5));
        appBar.setStateListAnimator(stateListAnimator);

        preloadMusic();
        songFrag = new MusicFragment(cache, this);
        mediaWrapper = new MediaWrapper(cache, cache.getSongList());
        albumFrag = new AlbumFragment();
        artistFrag = new ArtistFragment();
        changeFragment(songFrag);

        initPlayerView();
    }

    private void changeFragment(Fragment frag) {
        currentFrag = frag;
        getSupportFragmentManager().beginTransaction().replace(container.getId(), currentFrag).commit();
    }

    private void preloadMusic() {

        songManager = new SongManager();

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(true);
        progressDialog.setMessage("Loading songs...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        if (cache == null) {
            while (!songManager.getFetchStatus()) {
                progressDialog.show();
                allSongs = songManager.findSongList(new File(Environment.getExternalStorageDirectory().getAbsolutePath()));
            }
            if (allSongs != null) {
                cache = new MetadataCacher(allSongs);
                progressDialog.dismiss();
            }
        }


    }

    @Override
    public void onSongClick(int position) {
        Log.d("song", "id " +position);
        this.position = position;
        mediaWrapper.playSong(position);
        setSongData();
    }

    /*-----------Player----------*/


    private int position;
    private ImageSwitcher cover;
    private TextView song, artistAlbum;
    private AudioWaveView wave;
    private ImageView prev, play, next;
    private boolean isPlaying;

    private void setSongData() {
        String songSt = "", artistAlbumSt = "";

        Mp3File file = cache.getSongCache().get(position);

            songSt = cache.getMetadataAll(file).get("Title");
            artistAlbumSt = cache.getMetadataAll(file).get("Artist") + " | " + cache.getMetadataAll(cache.getSongCache().get(position)).get("Album");
            //cover.setImageDrawable(new BitmapDrawable(cache.getAlbumArt(file)));


        song.setText(songSt);
        artistAlbum.setText(artistAlbumSt);
    }

    private void initPlayerView() {
        isPlaying = mediaWrapper.isPlaying();

        Animation in = AnimationUtils.loadAnimation(this, R.anim.left_to_right_in);
        Animation out = AnimationUtils.loadAnimation(this, R.anim.left_to_right_out);

        cover = (ImageSwitcher) findViewById(R.id.imageswitcher);
        cover.setInAnimation(in);
        cover.setOutAnimation(out);
        //cover.setImageDrawable(getResources().getDrawable(R.drawable.preload));

        song = (TextView) findViewById(R.id.song);
        artistAlbum = (TextView) findViewById(R.id.artist_album);
        wave = (AudioWaveView) findViewById(R.id.wave);

        prev = (ImageView) findViewById(R.id.prev);
        play = (ImageView) findViewById(R.id.play);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_anim));
                Log.d("Play Button", "Button Pressed");
                pauseResumeSong();
            }
        });

        next = (ImageView) findViewById(R.id.next);

        if (isPlaying) {
            setSongData();
           play.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
        }
    }

    private void pauseResumeSong()
    {
        mediaWrapper.toggleCurrentSong();
        isPlaying = mediaWrapper.isPlaying();
    }

}