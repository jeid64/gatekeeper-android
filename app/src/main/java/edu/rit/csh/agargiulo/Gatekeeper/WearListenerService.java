package edu.rit.csh.agargiulo.Gatekeeper;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by jeid64 on 4/12/16.
 */

public class WearListenerService extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks {
        private static final String START_ACTIVITY = "/start_activity";
    private GoogleApiClient mApiClient;


    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.addListener( mApiClient, this );
    }

    @Override
    public void onPeerConnected(Node peer) {
        Log.d("GatekeeperService", "NODE CONNECTED!");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("GatekeeperService", "service created!");

        mApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d("GatekeeperService", "onConnected: " + connectionHint);
                        //  "onConnected: null" is normal.
                        //  There's nothing in our bundle.
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d("GatekeeperService", "onConnectionSuspended: " + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d("GatekeeperService", "onConnectionFailed: " + result);
                    }
                })
                .addApi(Wearable.API)
                .build();

        mApiClient.connect();

        Log.v("GatekeeperService", "might be waiting a long time");
        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mApiClient).await();
                Log.v("GatekeeperService", "Think we found some nodes!");

                for (Node node : nodes.getNodes()) {
                    Log.v("GatekeeperService", "Sending to node!");
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mApiClient, node.getId(), START_ACTIVITY, "YUUUUGE BUUUGE".getBytes()).await();
                }
            }
        }).start();

    }

    @Override
        public void onMessageReceived(MessageEvent messageEvent) {
            Log.v("GatekeeperService", "MESSAGE RECEIVED AND ITS HUGE");
            if( messageEvent.getPath().equalsIgnoreCase( START_ACTIVITY ) ) {
                Intent intent = new Intent( this, GatekeeperActivity.class );
                intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                String messageData = new String(messageEvent.getData());
                intent.putExtra("nfcDoorPop", messageData);
                startActivity( intent );
            } else {
                super.onMessageReceived(messageEvent);
            }
        }
    @Override
    public void onConnectionSuspended(int i) {

    }
    }