package com.sample.foo.usingawarenessapi;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.BeaconFence;
import com.google.android.gms.awareness.fence.DetectedActivityFence;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.HeadphoneFence;
import com.google.android.gms.awareness.fence.LocationFence;
import com.google.android.gms.awareness.state.HeadphoneState;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;

public class FenceActivity extends AppCompatActivity {
    private static final String TAG = "FenceActivity";

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 10001;
    private static final String MY_FENCE_RECEIVER_ACTION = "MY_FENCE_ACTION";
    public static final String HEADPHONE_FENCE_KEY = "HeadphoneFenceKey";
    public static final String HEADPHONE_AND_WALKING_FENCE_KEY = "HeadphoneAndLocationFenceKey";
    public static final String HEADPHONE_OR_WALKING_FENCE_KEY = "HeadphoneOrLocationFenceKey";

    private GoogleApiClient mGoogleApiClient;

    private FenceBroadcastReceiver mFenceReceiver;

    private Button mAddAction1Button, mRemoveAction1Button;
    private Button mAddAction2Button, mRemoveAction2Button;
    private Button mAddAction3Button, mRemoveAction3Button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fence);

        mGoogleApiClient = new GoogleApiClient.Builder(FenceActivity.this)
                .addApi(Awareness.API)
                .build();
        mGoogleApiClient.connect();

        mFenceReceiver = new FenceBroadcastReceiver();

        mAddAction1Button = (Button) findViewById(R.id.add1Button);
        mAddAction1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addHeadphoneFence();
            }
        });
        mRemoveAction1Button = (Button) findViewById(R.id.remove1Button);
        mRemoveAction1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeFence(HEADPHONE_FENCE_KEY);
            }
        });

        mAddAction2Button = (Button) findViewById(R.id.add2Button);
        mAddAction2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addHeadphoneAndLocationFence();
            }
        });
        mRemoveAction2Button = (Button) findViewById(R.id.remove2Button);
        mRemoveAction2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeFence(HEADPHONE_AND_WALKING_FENCE_KEY);
            }
        });

        mAddAction3Button = (Button) findViewById(R.id.add3Button);
        mAddAction3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addHeadphoneOrLocationFence();
            }
        });
        mRemoveAction3Button = (Button) findViewById(R.id.remove3Button);
        mRemoveAction3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeFence(HEADPHONE_OR_WALKING_FENCE_KEY);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // We want to receive Broadcasts when activity is paused
        registerReceiver(mFenceReceiver, new IntentFilter(MY_FENCE_RECEIVER_ACTION));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mFenceReceiver);
    }

    private void addHeadphoneFence() {
        Intent intent = new Intent(MY_FENCE_RECEIVER_ACTION);
        PendingIntent mFencePendingIntent = PendingIntent.getBroadcast(FenceActivity.this,
                10001,
                intent,
                0);

        AwarenessFence headphoneFence = HeadphoneFence.during(HeadphoneState.PLUGGED_IN);
        Awareness.FenceApi.updateFences(
                mGoogleApiClient,
                new FenceUpdateRequest.Builder()
                        .addFence(HEADPHONE_FENCE_KEY, headphoneFence, mFencePendingIntent)
                        .build())
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Fence was successfully registered.");
                        } else {
                            Log.e(TAG, "Fence could not be registered: " + status);
                        }
                    }
                });
    }

    private void addHeadphoneAndLocationFence() {
            Intent intent = new Intent(MY_FENCE_RECEIVER_ACTION);
            PendingIntent mFencePendingIntent = PendingIntent.getBroadcast(FenceActivity.this,
                    10001,
                    intent,
                    0);

            AwarenessFence headphoneFence = HeadphoneFence.during(HeadphoneState.PLUGGED_IN);
            AwarenessFence activityFence = DetectedActivityFence.during(DetectedActivityFence.WALKING);
            AwarenessFence jointFence = AwarenessFence.and(headphoneFence, activityFence);
            Awareness.FenceApi.updateFences(
                    mGoogleApiClient,
                    new FenceUpdateRequest.Builder()
                            .addFence(HEADPHONE_AND_WALKING_FENCE_KEY,
                                    jointFence, mFencePendingIntent)
                            .build())
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                Log.i(TAG, "Headphones AND Walking Fence was successfully registered.");
                            } else {
                                Log.e(TAG, "Headphones AND Walking Fence could not be registered: " + status);
                            }
                        }
                    });
    }

    private void addHeadphoneOrLocationFence() {
        Intent intent = new Intent(MY_FENCE_RECEIVER_ACTION);
        PendingIntent mFencePendingIntent = PendingIntent.getBroadcast(FenceActivity.this,
                10001,
                intent,
                0);

        AwarenessFence headphoneFence = HeadphoneFence.during(HeadphoneState.PLUGGED_IN);
        AwarenessFence activityFence = DetectedActivityFence.during(DetectedActivityFence.WALKING);

        AwarenessFence orFence = AwarenessFence.or(headphoneFence, activityFence);
        Awareness.FenceApi.updateFences(
                mGoogleApiClient,
                new FenceUpdateRequest.Builder()
                        .addFence(HEADPHONE_OR_WALKING_FENCE_KEY,
                                orFence, mFencePendingIntent)
                        .build())
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Headphones OR Walking Fence was successfully registered.");
                        } else {
                            Log.e(TAG, "Headphones OR Walking Fence could not be registered: " + status);
                        }
                    }
                });
    }

    private void removeFence(final String fenceKey) {
        Awareness.FenceApi.updateFences(
                mGoogleApiClient,
                new FenceUpdateRequest.Builder()
                        .removeFence(fenceKey)
                        .build()).setResultCallback(new ResultCallbacks<Status>() {
            @Override
            public void onSuccess(@NonNull Status status) {
                String info = "Fence " + fenceKey + " successfully removed.";
                Log.i(TAG, info);
                Toast.makeText(FenceActivity.this, info, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(@NonNull Status status) {
                String info = "Fence " + fenceKey + " could NOT be removed.";
                Log.i(TAG, info);
                Toast.makeText(FenceActivity.this, info, Toast.LENGTH_LONG).show();
            }
        });
    }
}
