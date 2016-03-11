package com.example.dongja94.samplemediaplayer;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;

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
    SeekBar progressView;
    boolean isSeeking = false;
    AudioManager mAudioManager;
    SeekBar volumeView;
    CheckBox muteView;

    float currentVolume = 1.0f;
    Runnable volumeUp = new Runnable() {
        @Override
        public void run() {
            if (currentVolume < 1.0f) {
                mPlayer.setVolume(currentVolume, currentVolume);
                currentVolume += 0.2f;
                mHadler.postDelayed(this, 200);
            } else {
                currentVolume = 1.0f;
                mPlayer.setVolume(currentVolume, currentVolume);
            }
        }
    };

    Runnable volumeDown = new Runnable() {
        @Override
        public void run() {
            if (currentVolume > 0) {
                mPlayer.setVolume(currentVolume, currentVolume);
                currentVolume -= 0.2f;
                mHadler.postDelayed(this, 200);
            } else {
                currentVolume = 0;
                mPlayer.setVolume(currentVolume, currentVolume);
            }
        }
    };

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

        muteView = (CheckBox)findViewById(R.id.check_mute);
        muteView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mHadler.removeCallbacks(volumeUp);
                    mHadler.post(volumeDown);
//                    mPlayer.setVolume(0,0);
                } else {
                    mHadler.removeCallbacks(volumeDown);
                    mHadler.post(volumeUp);
//                    mPlayer.setVolume(1, 1);
                }
            }
        });

        progressView = (SeekBar)findViewById(R.id.seek_progress);
        progressView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress;
            private static final int PROGRESS_NOT_CHANGED = -1;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    this.progress = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                progress = PROGRESS_NOT_CHANGED;
                isSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (progress != PROGRESS_NOT_CHANGED) {
                    if (mState == PlayerState.STATE_STARTED) {
                        mPlayer.seekTo(progress);
                    }
                }
                isSeeking = false;
            }
        });
        volumeView = (SeekBar)findViewById(R.id.seek_volume);
        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        mPlayer = MediaPlayer.create(this, R.raw.winter_blues);
        mState = PlayerState.STATE_PREPARED;

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        volumeView.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volumeView.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

        volumeView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

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

        progressView.setMax(mPlayer.getDuration());

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
                    mPlayer.seekTo(progressView.getProgress());
                    mPlayer.start();
                    mState = PlayerState.STATE_STARTED;
                    mHadler.post(updateProgress);
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
                    progressView.setProgress(0);
                }
            }
        });

        btn = (Button)findViewById(R.id.btn_list);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, MusicListActivity.class), RC_LIST);
            }
        });

    }

    private static final int RC_LIST = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_LIST) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                String displayName = data.getStringExtra("displayName");
                String path = data.getStringExtra("file");
                setTitle(displayName);
                mPlayer.reset();
                mState = PlayerState.STATE_IDLE;
                try {
                    mPlayer.setDataSource(this, uri);
                    mState = PlayerState.STATE_INITIALIZED;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (mState == PlayerState.STATE_INITIALIZED) {
                    try {
                        mPlayer.prepare();
                        mState = PlayerState.STATE_PREPARED;
                        progressView.setMax(mPlayer.getDuration());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    Handler mHadler = new Handler(Looper.getMainLooper());
    private static final int INTERVAL = 50;

    Runnable updateProgress = new Runnable() {
        @Override
        public void run() {
            if (mState == PlayerState.STATE_STARTED) {
                if (!isSeeking) {
                    progressView.setProgress(mPlayer.getCurrentPosition());
                }
                mHadler.postDelayed(this, INTERVAL);
            }
        }
    };

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
