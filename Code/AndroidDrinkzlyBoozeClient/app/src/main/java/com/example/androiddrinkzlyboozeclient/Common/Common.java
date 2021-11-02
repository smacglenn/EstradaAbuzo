package com.example.androiddrinkzlyboozeclient.Common;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.androiddrinkzlyboozeclient.Model.BranchModel;
import com.example.androiddrinkzlyboozeclient.Model.CategoryModel;
import com.example.androiddrinkzlyboozeclient.Model.DiscountModel;
import com.example.androiddrinkzlyboozeclient.Model.FoodModel;
import com.example.androiddrinkzlyboozeclient.Model.ShippingOrderModel;
import com.example.androiddrinkzlyboozeclient.Model.TokenModel;
import com.example.androiddrinkzlyboozeclient.Model.UserModel;
import com.example.androiddrinkzlyboozeclient.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Common {

    public static final String USER_REFERENCER = "Users";
    public static final String POPULAR_CATEGORY_REF = "MostPopular";
    public static final String BEST_DEALS_REF = "BestDeals";
    public static final int DEFAULT_COLUMN_COUNT = 0;
    public static final int FULL_WITH_COLUMN = 1;
    public static final String CATEGORY_REF = "Category";
    public static final String COMMENT_REF = "Comments";
    public static final String ORDER_REF = "Orders";
    public static final String NOTI_TITLE = "title";
    public static final String NOTI_CONTENT = "content";

    public static final String IS_SEND_IMAGE = "IS_SEND_IMAGE";
    public static final String IMAGE_URL = "IMAGE_URL";
    public static final String CHAT_REF = "Chat";
    public static final String BRANCH_REF = "Stores";
    public static final String CHAT_DETAIL_REF = "ChatDetail";
    public static final String QR_CODE_TAG = "QRCode";
    public static final String DISCOUNT = "Discount";
    public static final String LOCATION_REF = "Location";
    public static final float SHIPPING_COST_PER_KM = 10;
    public static final double MAX_SHIPPING_COST = 60;
    private static final String TOKEN_REF = "Tokens";
    public static final String SHIPPING_ORDER_REF = "ShippingOrder";
    public static UserModel currentUser;
    public static CategoryModel categorySelected;
    public static FoodModel selectedFood;
    public static String currentToken = "";
    public static ShippingOrderModel currentShippingOrder;
    public static BranchModel currentBranch;
    public static DiscountModel discountApply;

    public static void setSpanString(String welcome, String name, TextView txt_user) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(welcome);
        SpannableString spannableString = new SpannableString(name);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan, 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        txt_user.setText(builder, TextView.BufferType.SPANNABLE);

    }

    public static String createOrderNumber() {
        return new StringBuilder()
                .append(System.currentTimeMillis())
                .append(Math.abs(new Random().nextInt()))
                .toString();
    }

    public static String getDateOfWeek(int i) {
        switch (i)
        {
            case 1:
                return "Monday";
            case 2:
                return "Tuesday";
            case 3:
                return "Wednesday";
            case 4:
                return "Thursday";
            case 5:
                return "Friday";
            case 6:
                return "Saturday";
            case 7:
                return "Sunday";
            default:
                return "Unk";
        }
    }

    public static float getBearing(LatLng begin, LatLng end) {
        double lat = Math.abs(begin.latitude-end.latitude);
        double lng = Math.abs(begin.longitude-end.longitude);

        if(begin.latitude<end.latitude && begin.longitude<begin.longitude)
            return (float)(Math.toDegrees(Math.atan(lng/lat)));
        else if(begin.latitude>=end.latitude && begin.longitude<end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng/lat)))+90);
        else if(begin.latitude>=end.latitude && begin.longitude>=end.longitude)
            return (float) ( Math.toDegrees(Math.atan(lng/lat))+180);
        else if(begin.latitude<end.latitude && begin.longitude>=end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng/lat)))+270);
        return -1;

    }

    public static String convertStatusToText(int orderStatus) {
        switch (orderStatus)
        {
            case 0:
                return "Processing ...";
            case 1:
                return "On The Way";
            case 2:
                return "Shipped";
            case -1:
                return "Cancelled";
                default:
                return "Unk";
        }
    }

    public static void showNotification(Context context, int id, String title, String content, Intent intent) {
        PendingIntent pendingIntent = null;
        if(intent!=null)
            pendingIntent = PendingIntent.getActivity(context,id,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        String NOTIFICATION_CHANNEL_ID = "drinkzlybooze";
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "DrinkzlyBooze", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("DrinkzlyBooze");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_baseline_wine_bar_24));
        if(pendingIntent!=null)
            builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notificationManager.notify(id,notification);
    }

    public static void updateToken(Context context, String newToken) {
      if(Common.currentUser!=null)
      {
          FirebaseDatabase.getInstance()
                  .getReference(Common.TOKEN_REF)
                  .child(Common.currentUser.getUid())
                  .setValue(new TokenModel(Common.currentUser.getPhone(),newToken))
                  .addOnFailureListener(e -> {
                      Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                  });
      }
    }

    public static String createTopicOrder() {
        return new StringBuilder("/topics/")
                .append(Common.currentBranch.getUid())
                .append("_")
                .append("new_order")
                .toString();
    }

    public static List<LatLng> decodePoly(String encoded) {
        List poly = new ArrayList();
        int index=0,len=encoded.length();
        int lat=0,lng=0;
        while (index<len)
        {
            int b,shift=0,result=0;
            do {
                b=encoded.charAt(index++)-63;
                result |= (b&0x1f) << shift;
                shift+=5;
            }while (b>=0x20);
            int dlat = ((result & 1) != 0 ? ~(result>>1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do{
                b=encoded.charAt(index++)-63;
                result |= (b&0x1f) << shift;
                shift+=5;
            }while (b>=0x20);
            int dlng = ((result & 1) !=0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double)lat/1E5)),
                    (((double)lng/1E5)));
            poly.add(p);
        }
        return poly;
    }

    public static void showNotificationBigStyle(Context context, int id, String title, String content, Bitmap bitmap, Intent intent) {
        PendingIntent pendingIntent = null;
        if(intent!=null)
            pendingIntent = PendingIntent.getActivity(context,id,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        String NOTIFICATION_CHANNEL_ID = "drinkzlybooze";
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "DrinkzlyBooze", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("DrinkzlyBooze");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(bitmap)
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap));
        if(pendingIntent!=null)
            builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notificationManager.notify(id,notification);
    }

    public static String createTopicNews() {
        return new StringBuilder("/topics/")
                .append(Common.currentBranch.getUid())
                .append("_")
                .append("news")
                .toString();
    }

    public static String generateChatRoomId(String a, String b) {
        if(a.compareTo(b)>0)
            return new StringBuilder(a).append(b).toString();
        else if(a.compareTo(b)<0)
            return new StringBuilder(b).append(a).toString();
        else
            return new StringBuilder("ChatYourself_Error_")
                    .append(new Random().nextInt())
                    .toString();
    }

    public static String getFileName(ContentResolver contentResolver, Uri fileUri) {
        String result = null;
        if(fileUri.getScheme().equals("content"))
        {
            Cursor cursor = contentResolver.query(fileUri,null,null,null,null);
            try {
                if(cursor!=null && cursor.moveToFirst())
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }finally {
                cursor.close();
            }
        }
        if(result==null)
        {
            result = fileUri.getPath();
            int cut = result.lastIndexOf('/');
            if(cut!=-1)
                result = result.substring(cut+1);
        }
        return result;
    }
}
