package com.example.finalproject;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Stack;

public class notificationService extends Service {
    private String username;
    private String password;
    private int businessNumber;

    private int numOfPositions;
    private int numOfBusinesses;

    private ArrayList<Integer> listOfNumOfEmployedList;
    private ArrayList<String> businessUserNameListFromData;
    private ArrayList<String> positionsNameListFromData;

    private FirebaseDatabase database;
    private DatabaseReference businessRef;
    private DatabaseReference employedListRef;

    public static final String SHARED_PREFS = "sharedPrefs"; //name of shared prefs
    public static final String KEY_NUMBER = "number"; // shared prefs value to save
    public static final String KEY_USERNAME = "usernameMe"; // shared prefs value to load
    public static final String KEY_PASSWORD = "passwordMe"; // shared prefs value to load
    private int numToSave;// num from load data


    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        username = sharedPreferences.getString(KEY_USERNAME, ""); // get username from phone
        password = sharedPreferences.getString(KEY_PASSWORD, ""); // get password from phone

        businessUserNameListFromData = new ArrayList<>();
        positionsNameListFromData = new ArrayList<>();

        database = FirebaseDatabase.getInstance();
        businessRef = database.getReference().child("Businesses");

        ReadBusinessesFromDataBase();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public notificationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }



    /**
     * read businesses username from database
     */
    public void ReadBusinessesFromDataBase(){
        businessRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                numOfBusinesses = (int) dataSnapshot.getChildrenCount();
                for (int i=1; i<=numOfBusinesses;i++){
                    String businessUserNameFromData = (String) dataSnapshot.child("business " + i)
                            .child("userName").getValue();
                    businessUserNameListFromData.add(businessUserNameFromData);
                }
                filterUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // when there is an error (no internet, no signal)
                AlertDialog.Builder connectError = new AlertDialog.Builder(notificationService.this).
                        setMessage(databaseError.getMessage()).setPositiveButton("ok", null);
                connectError.show();
            }
        });
    }

    /**
     * filter users that are not has an business user
     */
    public void filterUsers(){
        for (int i =0; i<numOfBusinesses; i++){
            if (username.equals(businessUserNameListFromData.get(i))){ // to check if the user is exist business in data base
                businessNumber = i+1;
                employedListRef = database.getReference().child("Businesses").
                        child("business " + businessNumber).child("positions");
                ReadEmployedListCount();
            }
        }
    }

    /**
     * read the employedList count to specific business
     */
    public void ReadEmployedListCount(){
        employedListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listOfNumOfEmployedList = new ArrayList<>();
                numOfPositions = (int) dataSnapshot.getChildrenCount();
                for (int i =0; i<numOfPositions;i++){
                    if (dataSnapshot.child(String.valueOf(i)).child("employedList").getValue() != null){
                        int numOfEmployed = (int) dataSnapshot.child(String.valueOf(i)).
                                child("employedList").getChildrenCount();
                        listOfNumOfEmployedList.add(numOfEmployed);
                    }
                }
                callToSend();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // when there is an error (no internet, no signal)
                AlertDialog.Builder connectError = new AlertDialog.Builder(notificationService.this).
                        setMessage(databaseError.getMessage()).setPositiveButton("ok", null);
                connectError.show();
            }
        });
    }

    /**
     * sum the number of employees in employees_list
     * @param numOfEmployedList
     * @return
     */
    public int sumEmployeds(ArrayList<Integer> numOfEmployedList){
        int sum = 0;
        for (int i =0; i< numOfEmployedList.size(); i++){
            sum += numOfEmployedList.get(i);
        }
        System.out.println("sum " + sum);
        return sum;
    }

    /**
     * save number of employees in shared preferences
     */
    public void saveData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(KEY_NUMBER,sumEmployeds(listOfNumOfEmployedList));
        System.out.println("saveData");
        editor.apply();
    }

    /**
     * load number from shared preferences in to numToSave variable
     */
    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        numToSave = sharedPreferences.getInt(KEY_NUMBER, 0);
        System.out.println("num " + numToSave);
    }

    /**
     * check if the number in data base in more then number in preferences call to send message
     * with - "new candidates"
     */
    public void callToSend(){
        loadData();
        if (sumEmployeds(listOfNumOfEmployedList) > numToSave){
            sendNotification("EZ-job", "מועסק/ים חדשים- גע כדי לראות מי המועמד");
        }
        saveData(); // update the NUMBER of the phone
    }

    /**
     * send notification to the phone
     * @param title
     * @param text
     */
    public void sendNotification(String title, String text){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                notificationService.this
        )
                .setSmallIcon(R.drawable.ic_message)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true);

        builder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        Intent intent = new Intent(notificationService.this, CandidateForWorkScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("username", username);
        intent.putExtra("password", password);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                notificationService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager)getSystemService(
                Context.NOTIFICATION_SERVICE);

        // === Removed some obsoletes - to match all version of android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String channelId = "Your_channel_id";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }

        notificationManager.notify(0, builder.build());
    }

}
