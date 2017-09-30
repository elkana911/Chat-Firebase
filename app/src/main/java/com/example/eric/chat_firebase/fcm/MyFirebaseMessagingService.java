package com.example.eric.chat_firebase.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.eric.chat_firebase.MainActivity;
import com.example.eric.chat_firebase.R;
import com.example.eric.chat_firebase.util.FirebaseConst;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Eric on 13-Dec-16.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    /*
    public static void broadcastMessage(Context ctx, String collFrom, @Nullable RemoteMessage.Notification notification
                                     , Map<String, String> data
    ) {
        //Displaying data in log
        //It is optional
        Log.e(TAG, "From: " + collFrom); //719224898380

        try {
            Intent pushNotification = new Intent(ConstChat.PUSH_NOTIFICATION);
            pushNotification.putExtra("from", collFrom);

            if (notification != null) {
                Log.e(TAG, "Notification Message Title: " + notification.getTitle());
                Log.e(TAG, "Notification Message Body: " + notification.getBody());  //Hello Test notification

                pushNotification.putExtra("title", notification.getTitle());
                pushNotification.putExtra("body", notification.getBody());
            }

            Log.e(TAG, "Data Size: " + data.size());
            if (data.size() > 0) {

                if (NewsUtil.isTitleIsNews(notification.getTitle())) {
                    String key_uid = data.get(NewsUtil.KEY_UID);
                    String key_from = data.get(NewsUtil.KEY_FROM);
                    String key_to = data.get(NewsUtil.KEY_TO);
                    String key_msg = data.get(NewsUtil.KEY_ARTICLE);
                    String key_msgType = data.get(NewsUtil.KEY_MSG_TYPE);
                    String key_timestamp = data.get(NewsUtil.KEY_TIMESTAMP);

                    pushNotification.putExtra(NewsUtil.KEY_UID, key_uid);
                    pushNotification.putExtra(NewsUtil.KEY_FROM, key_from);
                    pushNotification.putExtra(NewsUtil.KEY_TO, key_to);
                    pushNotification.putExtra(NewsUtil.KEY_ARTICLE, key_msg);
                    pushNotification.putExtra(NewsUtil.KEY_MSG_TYPE, key_msgType);
                    pushNotification.putExtra(NewsUtil.KEY_TIMESTAMP, key_timestamp);

                } else {
                    String key_from = data.get(ConstChat.KEY_FROM);
                    String key_uid = data.get(ConstChat.KEY_UID);
                    String key_msg = data.get(ConstChat.KEY_MESSAGE);
                    String key_status = data.get(ConstChat.KEY_STATUS);
//                String key_seqno = remoteMessage.getData().get(ConstChat.KEY_SEQNO);
                    String key_timestamp = data.get(ConstChat.KEY_TIMESTAMP);

                    pushNotification.putExtra(ConstChat.KEY_UID, key_uid);
                    pushNotification.putExtra(ConstChat.KEY_FROM, key_from);
                    // replace previous chat_msg with real data
                    pushNotification.putExtra(ConstChat.KEY_MESSAGE, key_msg);
                    pushNotification.putExtra(ConstChat.KEY_STATUS, key_status);
//                pushNotification.putExtra(ConstChat.KEY_SEQNO, key_seqno);
                    pushNotification.putExtra(ConstChat.KEY_TIMESTAMP, key_timestamp);

                }

            }

            LocalBroadcastManager.getInstance(ctx).sendBroadcast(pushNotification);

            // play notification sound
//        NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
            NotificationUtils.playNotificationSound(ctx);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
*/

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            String title = remoteMessage.getData().get("title");
            String message = remoteMessage.getData().get("text");
            String username = remoteMessage.getData().get("username");
            String uid = remoteMessage.getData().get("uid");
            String fcmToken = remoteMessage.getData().get("fcm_token");

            // Don't show notification if chat activity is open.
            if (!FirebaseChatMainApp.isChatActivityOpen()) {
                sendNotification(title,
                        message,
                        username,
                        uid,
                        fcmToken);
            } else {
                EventBus.getDefault().post(new PushNotificationEvent(title,
                        message,
                        username,
                        uid,
                        fcmToken));
            }
        }

    }

    /**
     * Create and show a simple notification containing the received FCM message.
     */
    private void sendNotification(String title,
                                  String message,
                                  String receiver,
                                  String receiverUid,
                                  String firebaseToken) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(FirebaseConst.ARG_RECEIVER, receiver);
        intent.putExtra(FirebaseConst.ARG_RECEIVER_UID, receiverUid);
        intent.putExtra(FirebaseConst.ARG_FIREBASE_TOKEN, firebaseToken);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_messaging)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }
}
