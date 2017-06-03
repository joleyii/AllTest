/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shine.alltest.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.shine.alltest.R;
import com.shine.alltest.manager.Recorder;
import com.shine.alltest.manager.RemainingTimeCalculator;
import com.shine.alltest.manager.SuClient;
import com.shine.alltest.view.VUMeter;
import com.shine.utilitylib.A64Utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class SoundRecorderActivity extends BaseAvtivity
        implements Button.OnClickListener, Recorder.OnStateChangedListener {
    static final String TAG = "SoundRecorder";
    static final String STATE_FILE_NAME = "soundrecorder.state";
    static final String RECORDER_STATE_KEY = "recorder_state";
    static final String SAMPLE_INTERRUPTED_KEY = "sample_interrupted";
    static final String MAX_FILE_SIZE_KEY = "max_file_size";
    static final String AUDIO_3GPP = "audio/3gpp";
    static final String AUDIO_AMR = "audio/amr";
    static final String AUDIO_ANY = "audio/*";
    static final String ANY_ANY = "*/*";
    static final int BITRATE_AMR = 5900; // bits/sec
    static final int BITRATE_3GPP = 5900;
    private static final int SOUND_RECORDER_PERMISSION_REQUEST = 1;
    WakeLock mWakeLock;
    String mRequestedType = AUDIO_ANY;
    Recorder mRecorder;
    boolean mSampleInterrupted = false;
    String mErrorUiMessage = null; // Some error messages are displayed in the UI, 
    // not a dialog. This happens when a recording
    // is interrupted for some reason.
    long mMaxFileSize = -1;        // can be specified in the intent
    RemainingTimeCalculator mRemainingTimeCalculator;
    String mTimerFormat;
    final Handler mHandler = new Handler();
    Runnable mUpdateTimer = new Runnable() {
        public void run() {
            updateTimerView();
        }
    };
    ImageButton mRecordButton;
    ImageButton mPlayButton;
    ImageButton mStopButton;
    ImageView mStateLED;
    TextView mStateMessage1;
    TextView mStateMessage2;
    TextView mTimerView;
    TextView tv_tingtongbanzai;
    TextView tv_currentmode;
    TextView tv_speakernow;
    TextView tv_earpiecenow;
    VUMeter mVUMeter;
    private BroadcastReceiver mSDCardMountEventReceiver = null;
    private Intent mIntent;
    private Bundle mBundle;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle icycle) {
        super.onCreate(icycle);
        mA64Utility = new A64Utility();
        mIntent = getIntent();
        mBundle = icycle;
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(
                    getApplicationInfo().packageName, PackageManager.GET_PERMISSIONS);

            if (packageInfo.requestedPermissions != null) {
                for (String permission : packageInfo.requestedPermissions) {
                    Log.v(TAG, "Checking permissions for: " + permission);
                    if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(packageInfo.requestedPermissions,
                                SOUND_RECORDER_PERMISSION_REQUEST);
                        return;
                    }
                }
            }
            createContinue();
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Unable to load package's permissions", e);
            Toast.makeText(this, "runtime_permissions_error", Toast.LENGTH_SHORT).show();
        }

        currentHardWare = getIntent().getIntExtra("currentHardWare", 0);
        set710();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        setContentView(R.layout.activity_sound_recorder);
        initResourceRefs();
        updateUi();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if ((mRecorder == null) || (mRecorder.sampleLength() == 0))
            return;

        Bundle recorderState = new Bundle();

        mRecorder.saveState(recorderState);
        recorderState.putBoolean(SAMPLE_INTERRUPTED_KEY, mSampleInterrupted);
        recorderState.putLong(MAX_FILE_SIZE_KEY, mMaxFileSize);

        outState.putBundle(RECORDER_STATE_KEY, recorderState);
    }

    private Button down = null;
    private Button up = null;
    private ProgressBar pb = null;

    private int maxVolume = 50; // 最大音量值
    private int curVolume = 20; // 当前音量值
    private int stepVolume = 0; // 每次调整的音量幅度
    private AudioManager audioMgr = null; // Audio管理器，用了控制音量

    private void initPlayWork() {
        audioMgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // 获取最大音乐音量
        maxVolume = audioMgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        Log.d("Volume", "maxVolume" + maxVolume);
        // 初始化音量大概为最大音量的1/2
        curVolume = maxVolume / 2;
        // 每次调整的音量大概为最大音量的1/6
        stepVolume = maxVolume / 6;

    }

    /*
     * Whenever the UI is re-created (due f.ex. to orientation change) we have
     * to reinitialize references to the views.
     */
    private void initResourceRefs() {
        mRecordButton = (ImageButton) findViewById(R.id.recordButton);
        mPlayButton = (ImageButton) findViewById(R.id.playButton);
        mStopButton = (ImageButton) findViewById(R.id.stopButton);

        mStateLED = (ImageView) findViewById(R.id.stateLED);
        mStateMessage1 = (TextView) findViewById(R.id.stateMessage1);
        mStateMessage2 = (TextView) findViewById(R.id.stateMessage2);
        mTimerView = (TextView) findViewById(R.id.timerView);
        tv_tingtongbanzai = (TextView) findViewById(R.id.tv_tingtongbanzai);
        tv_currentmode = (TextView) findViewById(R.id.tv_currentmode);
        tv_speakernow = (TextView) findViewById(R.id.tv_speakernow);
        tv_earpiecenow = (TextView) findViewById(R.id.tv_earpiecenow);

        mVUMeter = (VUMeter) findViewById(R.id.uvMeter);

        mRecordButton.setOnClickListener(this);
        mPlayButton.setOnClickListener(this);
        mStopButton.setOnClickListener(this);

        down = (Button) findViewById(R.id.down);
        up = (Button) findViewById(R.id.up);
        mTimerFormat = "timer_format";
        initPlayWork();
        pb = (ProgressBar) findViewById(R.id.progress);
        pb.setMax(maxVolume);
        pb.setProgress(curVolume);

        mVUMeter.setRecorder(mRecorder);
        getCurrentSpeaker(14);
        getCurrentSpeaker(13);
    }

    /*
     * Make sure we're not recording music playing in the background, ask
     * the MediaPlaybackService to pause playback.
     */
    private void stopAudioPlayback() {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    /*
     * Handle the buttons.
     */
    public void onClick(View button) {
        if (!button.isEnabled())
            return;

        switch (button.getId()) {
            case R.id.recordButton:
                mRemainingTimeCalculator.reset();
                if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    mSampleInterrupted = true;
                    mErrorUiMessage = "insert_sd_card";
                    updateUi();
                } else if (!mRemainingTimeCalculator.diskSpaceAvailable()) {
                    mSampleInterrupted = true;
                    mErrorUiMessage = "storage_is_full";
                    updateUi();
                } else {
                    stopAudioPlayback();

                    if (AUDIO_AMR.equals(mRequestedType)) {
                        mRemainingTimeCalculator.setBitRate(BITRATE_AMR);
                        mRecorder.startRecording(MediaRecorder.OutputFormat.AMR_NB, ".amr", this);
                    } else if (AUDIO_3GPP.equals(mRequestedType)) {
                        mRemainingTimeCalculator.setBitRate(BITRATE_3GPP);
                        mRecorder.startRecording(MediaRecorder.OutputFormat.THREE_GPP, ".3gpp",
                                this);
                    } else {
                        throw new IllegalArgumentException("Invalid output file type requested");
                    }

                    if (mMaxFileSize != -1) {
                        mRemainingTimeCalculator.setFileSizeLimit(
                                mRecorder.sampleFile(), mMaxFileSize);
                    }
                }
                break;
            case R.id.playButton:
                mRecorder.startPlayback();
                break;
            case R.id.stopButton:
                mRecorder.stop();
                break;
        }
    }

    /*
     * Handle the "back" hardware key. 
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            switch (mRecorder.state()) {
                case Recorder.IDLE_STATE:
                    if (mRecorder.sampleLength() > 0)
                        saveSample();
                    finish();
                    break;
                case Recorder.PLAYING_STATE:
                    mRecorder.stop();
                    saveSample();
                    break;
                case Recorder.RECORDING_STATE:
                    mRecorder.clear();
                    break;
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onStop() {
        if (mRecorder != null) {
            mRecorder.stop();
        }
        super.onStop();
    }

    @Override
    protected void onPause() {
        if (mRecorder != null) {
            mSampleInterrupted = mRecorder.state() == Recorder.RECORDING_STATE;
            mRecorder.stop();
        }
        super.onPause();
    }

    /*
     * If we have just recorded a smaple, this adds it to the media data base
     * and sets the result to the sample's URI.
     */
    private void saveSample() {
        if (mRecorder.sampleLength() == 0)
            return;
        Uri uri = null;
        try {
            uri = this.addToMediaDB(mRecorder.sampleFile());
        } catch (UnsupportedOperationException ex) {  // Database manipulation failure
            return;
        }
        if (uri == null) {
            return;
        }
        setResult(RESULT_OK, new Intent().setData(uri)
                .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));
    }

    /*
     * Called on destroy to unregister the SD card mount event receiver.
     */
    @Override
    public void onDestroy() {
        if (mSDCardMountEventReceiver != null) {
            unregisterReceiver(mSDCardMountEventReceiver);
            mSDCardMountEventReceiver = null;
        }
        super.onDestroy();
    }

    /*
     * Registers an intent to listen for ACTION_MEDIA_EJECT/ACTION_MEDIA_MOUNTED
     * notifications.
     */
    private void registerExternalStorageListener() {
        if (mSDCardMountEventReceiver == null) {
            mSDCardMountEventReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
                        mRecorder.delete();
                    } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                        mSampleInterrupted = false;
                        updateUi();
                    }
                }
            };
            IntentFilter iFilter = new IntentFilter();
            iFilter.addAction(Intent.ACTION_MEDIA_EJECT);
            iFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            iFilter.addDataScheme("file");
            registerReceiver(mSDCardMountEventReceiver, iFilter);
        }
    }

    /*
     * A simple utility to do a query into the databases.
     */
    private Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        try {
            ContentResolver resolver = getContentResolver();
            if (resolver == null) {
                return null;
            }
            return resolver.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (UnsupportedOperationException ex) {
            return null;
        }
    }

    /*
     * Add the given audioId to the playlist with the given playlistId; and maintain the
     * play_order in the playlist.
     */
    private void addToPlaylist(ContentResolver resolver, int audioId, long playlistId) {
        String[] cols = new String[]{
                "count(*)"
        };
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        Cursor cur = resolver.query(uri, cols, null, null, null);
        cur.moveToFirst();
        final int base = cur.getInt(0);
        cur.close();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, Integer.valueOf(base + audioId));
        values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, audioId);
        resolver.insert(uri, values);
    }

    /*
     * Obtain the id for the default play list from the audio_playlists table.
     */
    private int getPlaylistId(Resources res) {
        Uri uri = MediaStore.Audio.Playlists.getContentUri("external");
        final String[] ids = new String[]{MediaStore.Audio.Playlists._ID};
        final String where = MediaStore.Audio.Playlists.NAME + "=?";
        final String[] args = new String[]{"audio_db_playlist_name"};
        Cursor cursor = query(uri, ids, where, args, null);
        if (cursor == null) {
            Log.v(TAG, "query returns null");
        }
        int id = -1;
        if (cursor != null) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                id = cursor.getInt(0);
            }
        }
        cursor.close();
        return id;
    }

    /*
     * Create a playlist with the given default playlist name, if no such playlist exists.
     */
    private Uri createPlaylist(Resources res, ContentResolver resolver) {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Audio.Playlists.NAME, "audio_db_playlist_name");
        Uri uri = resolver.insert(MediaStore.Audio.Playlists.getContentUri("external"), cv);
        if (uri == null) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.app_name)
                    .setMessage("error_mediadb_new_record")
                    .setPositiveButton("button_ok", null)
                    .setCancelable(false)
                    .show();
        }
        return uri;
    }

    /*
     * Adds file and returns content uri.
     */
    private Uri addToMediaDB(File file) {
        Resources res = getResources();
        ContentValues cv = new ContentValues();
        long current = System.currentTimeMillis();
        long modDate = file.lastModified();
        Date date = new Date(current);
        SimpleDateFormat formatter = new SimpleDateFormat(
                "audio_db_title_format");
        String title = formatter.format(date);
        long sampleLengthMillis = mRecorder.sampleLength() * 1000L;

        // Lets label the recorded audio file as NON-MUSIC so that the file
        // won't be displayed automatically, except for in the playlist.
        cv.put(MediaStore.Audio.Media.IS_MUSIC, "0");

        cv.put(MediaStore.Audio.Media.TITLE, title);
        cv.put(MediaStore.Audio.Media.DATA, file.getAbsolutePath());
        cv.put(MediaStore.Audio.Media.DATE_ADDED, (int) (current / 1000));
        cv.put(MediaStore.Audio.Media.DATE_MODIFIED, (int) (modDate / 1000));
        cv.put(MediaStore.Audio.Media.DURATION, sampleLengthMillis);
        cv.put(MediaStore.Audio.Media.MIME_TYPE, mRequestedType);
        cv.put(MediaStore.Audio.Media.ARTIST,
                "audio_db_artist_name");
        cv.put(MediaStore.Audio.Media.ALBUM,
                "audio_db_album_name");
        Log.d(TAG, "Inserting audio record: " + cv.toString());
        ContentResolver resolver = getContentResolver();
        Uri base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Log.d(TAG, "ContentURI: " + base);
        Uri result = resolver.insert(base, cv);
        if (result == null) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.app_name)
                    .setMessage("error_mediadb_new_record")
                    .setPositiveButton("button_ok", null)
                    .setCancelable(false)
                    .show();
            return null;
        }
        if (getPlaylistId(res) == -1) {
            createPlaylist(res, resolver);
        }
        int audioId = Integer.valueOf(result.getLastPathSegment());
        addToPlaylist(resolver, audioId, getPlaylistId(res));

        // Notify those applications such as Music listening to the 
        // scanner events that a recorded audio file just created. 
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, result));
        return result;
    }

    /**
     * Update the big MM:SS timer. If we are in playback, also update the
     * progress bar.
     */
    private void updateTimerView() {
        Resources res = getResources();
        int state = mRecorder.state();

        boolean ongoing = state == Recorder.RECORDING_STATE || state == Recorder.PLAYING_STATE;

        long time = ongoing ? mRecorder.progress() : mRecorder.sampleLength();
        String timeStr = String.format(mTimerFormat, time / 60, time % 60);
        mTimerView.setText(timeStr);

        if (state == Recorder.PLAYING_STATE) {
        } else if (state == Recorder.RECORDING_STATE) {
            updateTimeRemaining();
        }

        if (ongoing)
            mHandler.postDelayed(mUpdateTimer, 1000);
    }

    /*
     * Called when we're in recording state. Find out how much longer we can 
     * go on recording. If it's under 5 minutes, we display a count-down in 
     * the UI. If we've run out of time, stop the recording. 
     */
    private void updateTimeRemaining() {
        long t = mRemainingTimeCalculator.timeRemaining();

        if (t <= 0) {
            mSampleInterrupted = true;

            int limit = mRemainingTimeCalculator.currentLowerLimit();
            switch (limit) {
                case RemainingTimeCalculator.DISK_SPACE_LIMIT:
                    mErrorUiMessage
                            = "storage_is_full";
                    break;
                case RemainingTimeCalculator.FILE_SIZE_LIMIT:
                    mErrorUiMessage
                            = "max_length_reached";
                    break;
                default:
                    mErrorUiMessage = null;
                    break;
            }

            mRecorder.stop();
            return;
        }

        Resources res = getResources();
        String timeStr = "";

        if (t < 60)
            timeStr = String.format("sec_available", t);
        else if (t < 540)
            timeStr = String.format("min_available", t / 60 + 1);

        mStateMessage1.setText(timeStr);
    }

    /**
     * Shows/hides the appropriate child views for the new state.
     */
    private void updateUi() {
        Resources res = getResources();

        switch (mRecorder.state()) {
            case Recorder.IDLE_STATE:
                if (mRecorder.sampleLength() == 0) {
                    mRecordButton.setEnabled(true);
                    mRecordButton.setFocusable(true);
                    mPlayButton.setEnabled(false);
                    mPlayButton.setFocusable(false);
                    mStopButton.setEnabled(false);
                    mStopButton.setFocusable(false);
                    mRecordButton.requestFocus();

                    mStateMessage1.setVisibility(View.INVISIBLE);
                    mStateLED.setVisibility(View.INVISIBLE);
                    mStateMessage2.setVisibility(View.INVISIBLE);

                    mVUMeter.setVisibility(View.VISIBLE);
                    setTitle("record_your_message");
                } else {
                    mRecordButton.setEnabled(true);
                    mRecordButton.setFocusable(true);
                    mPlayButton.setEnabled(true);
                    mPlayButton.setFocusable(true);
                    mStopButton.setEnabled(false);
                    mStopButton.setFocusable(false);

                    mStateMessage1.setVisibility(View.INVISIBLE);
                    mStateLED.setVisibility(View.INVISIBLE);
                    mStateMessage2.setVisibility(View.INVISIBLE);

                    mVUMeter.setVisibility(View.INVISIBLE);
                    setTitle("message_recorded");
                }

                if (mSampleInterrupted) {
                    mStateMessage2.setVisibility(View.VISIBLE);
                    mStateMessage2.setText("recording_stopped");
                    mStateLED.setVisibility(View.INVISIBLE);
                }

                if (mErrorUiMessage != null) {
                    mStateMessage1.setText(mErrorUiMessage);
                    mStateMessage1.setVisibility(View.VISIBLE);
                }

                break;
            case Recorder.RECORDING_STATE:
                mRecordButton.setEnabled(false);
                mRecordButton.setFocusable(false);
                mPlayButton.setEnabled(false);
                mPlayButton.setFocusable(false);
                mStopButton.setEnabled(true);
                mStopButton.setFocusable(true);

                mStateMessage1.setVisibility(View.VISIBLE);
                mStateLED.setVisibility(View.VISIBLE);
                mStateLED.setImageResource(R.drawable.recording_led);
                mStateMessage2.setVisibility(View.VISIBLE);
                mStateMessage2.setText("recording");

                mVUMeter.setVisibility(View.VISIBLE);
                setTitle("record_your_message");

                break;

            case Recorder.PLAYING_STATE:
                mRecordButton.setEnabled(true);
                mRecordButton.setFocusable(true);
                mPlayButton.setEnabled(false);
                mPlayButton.setFocusable(false);
                mStopButton.setEnabled(true);
                mStopButton.setFocusable(true);

                mStateMessage1.setVisibility(View.INVISIBLE);
                mStateLED.setVisibility(View.INVISIBLE);
                mStateMessage2.setVisibility(View.INVISIBLE);
                mVUMeter.setVisibility(View.INVISIBLE);
                setTitle("review_message");
                break;
        }

        updateTimerView();
        mVUMeter.invalidate();
    }

    /*
     * Called when Recorder changed it's state.
     */
    public void onStateChanged(int state) {
        if (state == Recorder.PLAYING_STATE || state == Recorder.RECORDING_STATE) {
            mSampleInterrupted = false;
            mErrorUiMessage = null;
            mWakeLock.acquire(); // we don't want to go to sleep while recording or playing
        } else {
            if (mWakeLock.isHeld())
                mWakeLock.release();
        }

        updateUi();
    }

    /*
     * Called when MediaPlayer encounters an error.
     */
    public void onError(int error) {
        Resources res = getResources();

        String message = null;
        switch (error) {
            case Recorder.SDCARD_ACCESS_ERROR:
                message = "error_sdcard_access";
                break;
            case Recorder.IN_CALL_RECORD_ERROR:
                // TODO: update error message to reflect that the recording could not be
                //       performed during a call.
            case Recorder.INTERNAL_ERROR:
                message = "error_app_internal";
                break;
        }
        if (message != null) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.app_name)
                    .setMessage(message)
                    .setPositiveButton("button_ok", null)
                    .setCancelable(false)
                    .show();
        }
    }

    private void createContinue() {
        if (mIntent != null) {
            String s = mIntent.getType();
            if (AUDIO_AMR.equals(s) || AUDIO_3GPP.equals(s) || AUDIO_ANY.equals(s)
                    || ANY_ANY.equals(s)) {
                mRequestedType = s;
            } else if (s != null) {
                // we only support amr and 3gpp formats right now
                setResult(RESULT_CANCELED);
                finish();
                return;
            }

            final String EXTRA_MAX_BYTES
                    = MediaStore.Audio.Media.EXTRA_MAX_BYTES;
            mMaxFileSize = mIntent.getLongExtra(EXTRA_MAX_BYTES, -1);
        }

        if (AUDIO_ANY.equals(mRequestedType) || ANY_ANY.equals(mRequestedType)) {
            mRequestedType = AUDIO_3GPP;
        }

        setContentView(R.layout.activity_sound_recorder);

        mRecorder = new Recorder();
        mRecorder.setOnStateChangedListener(this);
        mRemainingTimeCalculator = new RemainingTimeCalculator();

        PowerManager pm
                = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
                "SoundRecorder");

        initResourceRefs();

        setResult(RESULT_CANCELED);
        registerExternalStorageListener();
        if (mBundle != null) {
            Bundle recorderState = mBundle.getBundle(RECORDER_STATE_KEY);
            if (recorderState != null) {
                mRecorder.restoreState(recorderState);
                mSampleInterrupted = recorderState.getBoolean(SAMPLE_INTERRUPTED_KEY, false);
                mMaxFileSize = recorderState.getLong(MAX_FILE_SIZE_KEY, -1);
            }
        }

        updateUi();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == SOUND_RECORDER_PERMISSION_REQUEST) {
            boolean bAllGranted = true;
            int permissionLength = permissions.length;
            int resultLength = grantResults.length;
            if ((permissionLength == resultLength) && (permissionLength > 0)) {
                for (int i = 0; i < permissionLength; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        bAllGranted = false;
                    }
                }
            } else {
                bAllGranted = false;
            }

            if (bAllGranted == true) {
                createContinue();
            } else {
                Log.v(TAG, "Permission not granted.");
                Toast.makeText(this, "runtime_permissions_error", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private A64Utility mA64Utility;
    private SuClient mSuClient;

    public void banzaiClick(View view) {
        if (currentHardWare == 7) {
            mA64Utility.SelectMicDev(0);
            tv_currentmode.setText("当前选择板载");
        } else if (currentHardWare == 10) {
            set10Mic(0);
        }
    }

    public void tingtongClick(View view) {
        if (currentHardWare == 7) {
            mA64Utility.SelectMicDev(1);
            tv_currentmode.setText("当前选择手持mic");
        } else if (currentHardWare == 10) {
            set10Mic(1);
        }
    }

    public void volumeClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.up://按下增大音量按钮
                curVolume += stepVolume;
                if (curVolume >= maxVolume) {
                    curVolume = maxVolume;
                }
                pb.setProgress(curVolume);
                adjustVolume();
                break;
            case R.id.down://按下减小音量按钮
                curVolume -= stepVolume;
                if (curVolume <= 0) {
                    curVolume = 0;
                }
                pb.setProgress(curVolume);
                adjustVolume();
                break;
            case R.id.speakerup://按下减小音量按钮
                setTinymixUp(14, true);
                break;
            case R.id.speakerdown://按下减小音量按钮
                setTinymixUp(14, false);
                break;
            case R.id.earpieceup://按下减小音量按钮
                setTinymixUp(13, true);
                break;
            case R.id.earpiecedown://按下减小音量按钮
                setTinymixUp(13, false);
                break;
        }
    }

    private void adjustVolume() {
        Log.d("Volume", "curVolume" + curVolume);
        audioMgr.setStreamVolume(AudioManager.STREAM_MUSIC, curVolume,
                AudioManager.FLAG_PLAY_SOUND);
    }

    private void set10Mic(int type) {
        switch (type) {
            case 1://听筒
                mA64Utility.selectMic(1);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (mSuClient == null) {
                            mSuClient = new SuClient();
                            mSuClient.init(null);
                        }
                        mSuClient.execCMD("tinymix 14 0");
                        mSuClient.execCMD("tinymix 112 1");

                    }
                }).start();
                tv_currentmode.setText("当前选择听筒");
                break;
            case 0://板载
                mA64Utility.selectMic(0);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (mSuClient == null) {
                            mSuClient = new SuClient();
                            mSuClient.init(null);
                        }
                        mSuClient.execCMD("tinymix 14 30");
                        mSuClient.execCMD("tinymix 112 0");
                    }
                }).start();
                tv_currentmode.setText("当前选择板载");
                break;
        }
    }

    int currentHardWare;

    public void set710() {
        switch (currentHardWare) {
            case 7:
                tv_tingtongbanzai.setText("手持mic");
                tv_currentmode.setText("请设置手持mic或者板载");
                break;
            case 10:
                tv_tingtongbanzai.setText("听筒");
                tv_currentmode.setText("请设置听筒或者板载");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (mSuClient == null) {
                            mSuClient = new SuClient();
                            mSuClient.init(null);
                        }
                        mSuClient.execCMD("echo 199 >  /sys/class/gpio/export");
                        mSuClient.execCMD("echo \"in\" > /sys/class/gpio/gpio199/direction");
                        mSuClient.execCMD("chmod 777 /sys/class/gpio/gpio199/value");
                    }
                }).start();
                break;
        }
    }

    int currentSpeaker;
    int maxSpeaker;
    int currentearpiece;
    int maxearpiece;

    //获得Speaker大小
    public void getCurrentSpeaker(final int type) {
        final String sFile = "/sdcard/tinymixnow" + type;
        final File file = new File(sFile);
        Flowable
                .create(new FlowableOnSubscribe<String>() {
                    @Override
                    public void subscribe(FlowableEmitter<String> emitter) throws Exception {
                        if (mSuClient == null) {
                            mSuClient = new SuClient();
                            mSuClient.init(null);
                        }
                        mSuClient.execCMD("tinymix " + type + " > " + sFile);
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        try {
                            FileInputStream fileInputStream = new FileInputStream(file);
                            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                            String line = "";
                            String now = "";
                            String max = "";
                            while (((line = bufferedReader.readLine()) != null)) {
                                now = line.substring(line.indexOf(":") + 2,
                                        line.indexOf("(") - 1);
                                max = line.substring(line.indexOf(">") + 1,
                                        line.indexOf(")"));
                            }
                            if (!TextUtils.isEmpty(now) && !TextUtils.isEmpty(max)) {
                                switch (type) {
                                    case 14:
                                        currentSpeaker = Integer.parseInt(now);
                                        maxSpeaker = Integer.parseInt(max);
                                        break;
                                    case 13:
                                        currentearpiece = Integer.parseInt(now);
                                        maxearpiece = Integer.parseInt(max);
                                        break;
                                }
                                emitter.onNext(now);
                            }
                            fileInputStream.close();
                            inputStreamReader.close();
                            bufferedReader.close();
                            emitter.onComplete();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, BackpressureStrategy.BUFFER)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        //UI改变当前音量
                        switch (type) {
                            case 14:
                                tv_speakernow.setText("0" + " " + s + " " + maxSpeaker);
                                break;
                            case 13:
                                tv_earpiecenow.setText("0" + " " + s + " " + maxearpiece);
                                break;
                        }
                    }
                });
    }

    public void setTinymixUp(final int type, final boolean upB) {
        Flowable
                .create(new FlowableOnSubscribe<String>() {
                    @Override
                    public void subscribe(FlowableEmitter<String> emitter) throws Exception {
                        if (mSuClient == null) {
                            mSuClient = new SuClient();
                            mSuClient.init(null);
                        }
                        switch (type) {
                            case 14:
                                if (upB) {
                                    int up = currentSpeaker + 3;
                                    if (up > maxSpeaker) {
                                        up = maxSpeaker;
                                    }
                                    mSuClient.execCMD("tinymix " + type + " " + up);
                                } else {
                                    int up = currentSpeaker - 3;
                                    if (up < 0) {
                                        up = 0;
                                    }
                                    mSuClient.execCMD("tinymix " + type + " " + up);
                                }
                                break;
                            case 13:
                                if (upB) {
                                    int up1 = currentearpiece + 3;
                                    if (up1 > maxearpiece) {
                                        up1 = maxearpiece;
                                    }
                                    mSuClient.execCMD("tinymix " + type + " " + up1);
                                } else {
                                    int up1 = currentearpiece - 3;
                                    if (up1 < 0) {
                                        up1 = 0;
                                    }
                                    mSuClient.execCMD("tinymix " + type + " " + up1);
                                }
                                break;
                        }
                        emitter.onNext("");
                        emitter.onComplete();
                    }
                }, BackpressureStrategy.BUFFER)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        //UI改变当前音量
                        getCurrentSpeaker(type);
                    }
                });
    }
}
