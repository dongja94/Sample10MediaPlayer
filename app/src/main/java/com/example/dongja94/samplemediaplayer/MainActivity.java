package com.example.dongja94.samplemediaplayer;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    MediaPlayer mPlayer;
    enum PlayerState {
        STATE_IDLE,
        STATE_INITIALIZED,
        STATE_PREPARED,
        STATE_STARTED,
        STATE_PAUSED,
        STATE_STOPPED,
        STATE_END,
        STATE_ERROR,
    }

    PlayerState mState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mPlayer = MediaPlayer.create(this, R.raw.winter_blues);
        mState = PlayerState.STATE_PREPARED;

//        mPlayer = new MediaPlayer();
//        mState = PlayerState.STATE_IDLE;
//
//        AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.winter_blues);
//        try {
//            if (mState == PlayerState.STATE_IDLE) {
//                mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
//                mState = PlayerState.STATE_INITIALIZED;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        if (mState == PlayerState.STATE_INITIALIZED) {
//            try {
//                mPlayer.prepare();
//                mState = PlayerState.STATE_PREPARED;
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

        mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mState = PlayerState.STATE_ERROR;
                return false;
            }
        });

        Button btn = (Button)findViewById(R.id.btn_start);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mState == PlayerState.STATE_INITIALIZED || mState == PlayerState.STATE_STOPPED) {
                    try {
                        mPlayer.prepare();
                        mState = PlayerState.STATE_PREPARED;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (mState == PlayerState.STATE_PREPARED || mState == PlayerState.STATE_PAUSED) {
                    mPlayer.start();
                    mState = PlayerState.STATE_STARTED;
                }
            }
        });

        btn = (Button)findViewById(R.id.btn_pause);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mState == PlayerState.STATE_STARTED) {
                    mPlayer.pause();
                    mState = PlayerState.STATE_PAUSED;
                }
            }
        });

        btn = (Button)findViewById(R.id.btn_stop);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mState == PlayerState.STATE_PREPARED ||
                        mState == PlayerState.STATE_STARTED ||
                        mState == PlayerState.STATE_PAUSED) {
                    mPlayer.stop();
                    mState = PlayerState.STATE_STOPPED;
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayer.release();
        mState = PlayerState.STATE_END;
        mPlayer = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
