package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class confirmedBusinessesOfEmployed extends AppCompatActivity {

    private TextView titleBusinesses;

    private FirebaseDatabase database;
    private DatabaseReference businessRef;

    private String username;
    private String password;

    private ArrayList<String> businessStringList;
    private ListView listViewBusinesses;
    private ArrayAdapter<String> businessesAdapter;

    private int numOfBusinesses;
    private int numOfPositions;
    private int numOfEmployees;

    private int businessNumber;
    private int positionNumber;
    private int employedNumberInEmployedsList;

    private ArrayList<String> confirmedBusinessNameList;
    private ArrayList<String> confirmedPositionNameList;

    private ArrayList<String> businessNameListFromData;
    private ArrayList<String> positionNameListFromData;
    private ArrayList<String> usernameEmployedsList;

    private DatabaseReference rateRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmed_businesses_of_employed);

        titleBusinesses = (TextView) findViewById(R.id.titleBusinesses);

        database = FirebaseDatabase.getInstance();
        businessRef = database.getReference().child("Businesses");

        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");

        businessStringList = new ArrayList<>(); // list view item

        confirmedBusinessNameList = new ArrayList<>();
        confirmedPositionNameList = new ArrayList<>();

        businessNameListFromData = new ArrayList<>();
        positionNameListFromData = new ArrayList<>();
        usernameEmployedsList = new ArrayList<>();

        seeConfirmedBusiness();
    }

    /**
     * when the user click on my businesses, read from data base the businesses that
     * confirmed the candidate, by check status = "confirm"
     */
    public void seeConfirmedBusiness(){
        businessRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int numOfBusinessesInMenu = (int) dataSnapshot.getChildrenCount();
                for (int i=1; i<= numOfBusinessesInMenu; i++){

                    String businessNameInMenu = (String) dataSnapshot.
                            child("business " + i).child("businessName").getValue();
                    String businessPlaceInMenu = (String) dataSnapshot.
                            child("business " + i).child("businessPlace").getValue();

                    int numOfPositionsInMenu = (int) dataSnapshot.child("business " + i).
                            child("positions").getChildrenCount();

                    for (int j=0; j<numOfPositionsInMenu; j++){

                        String positionNameInMenu = (String) dataSnapshot.child("business " + i).
                                child("positions").child(String.valueOf(j)).child("positionName").getValue();

                        if (dataSnapshot.child("business " + i).child("positions").
                                child(String.valueOf(j)).child("employedList").getValue() != null){

                            int numOfEmployedListInMenu = (int) dataSnapshot.child("business " + i).
                                    child("positions").child(String.valueOf(j)).child("employedList")
                                    .getChildrenCount();

                            for (int k=0; k<numOfEmployedListInMenu; k++){

                                String employedUserNameInList = (String) dataSnapshot.
                                        child("business " + i).child("positions").
                                        child(String.valueOf(j)).child("employedList").
                                        child(String.valueOf(k)).child("userName").getValue();

                                String employedPasswordInList = (String) dataSnapshot.
                                        child("business " + i).child("positions").
                                        child(String.valueOf(j)).child("employedList").
                                        child(String.valueOf(k)).child("password").getValue();

                                String employedStatusInList = (String) dataSnapshot.
                                        child("business " + i).child("positions").
                                        child(String.valueOf(j)).child("employedList").
                                        child(String.valueOf(k)).child("status").getValue();

                                if (username.equals(employedUserNameInList) && password.equals(employedPasswordInList)){
                                    if (employedStatusInList != null){
                                        if (employedStatusInList.equals("confirm")){
                                            String item = businessNameInMenu + "\n" + businessPlaceInMenu
                                                    + "\n" + "מקצוע: " + positionNameInMenu;
                                            if (!businessStringList.contains(item)) {
                                                businessStringList.add(item);
                                            }

                                            confirmedBusinessNameList.add(businessNameInMenu);
                                            confirmedPositionNameList.add(positionNameInMenu);

                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                showBusinessesList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // when there is an error (no internet, no signal)
                AlertDialog.Builder connectError = new AlertDialog.Builder
                        (confirmedBusinessesOfEmployed.this).
                        setMessage(databaseError.getMessage()).
                        setPositiveButton("ok", null);
                connectError.show();
            }
        });
    }

    /**
     * show the business list view
     */
    public void showBusinessesList(){
        listViewBusinesses = (ListView) findViewById(R.id.listBusinesses);

        businessesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                businessStringList);
        listViewBusinesses.setAdapter(businessesAdapter);
        listViewBusinesses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String finalItem = String.valueOf(parent.getAdapter().getItem(position));

                String businessName = finalItem.split("\n")[0];
                String positionName = finalItem.split("מקצוע: ")[1];

                System.out.println(businessName);
                System.out.println(positionName);

                checkBusiness(businessName, positionName);
                buildLayoutAlertRate();
            }
        });
    }

    /**
     * check which business is clicked, and get his index in database
     * @param businessName
     * @param positionName
     */
    public void checkBusiness(String businessName, String positionName){
        businessRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                numOfBusinesses = (int) dataSnapshot.getChildrenCount();
                for (int i = 1; i <= dataSnapshot.getChildrenCount(); i++){
                    // read business name from database
                    String businessNameFromData = ((String) dataSnapshot.
                            child("business " + i).child("businessName").getValue());
                    businessNameListFromData.add(businessNameFromData); // business name list
                }
                confirmBusiness(businessName, positionName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // when there is an error (no internet, no signal)
                AlertDialog.Builder connectError = new AlertDialog.Builder
                        (confirmedBusinessesOfEmployed.this).
                        setMessage(databaseError.getMessage()).
                        setPositiveButton("ok", null);
                connectError.show();
            }
        });
    }

    /**
     * confirm the business that clicked with database and
     * check which position is clicked, get his index in database
     * @param businessName
     * @param positionName
     */
    public void confirmBusiness(String businessName, String positionName){
        for (int i =0; i< numOfBusinesses; i++){
            String businessNameFromData = businessNameListFromData.get(i);
            if (businessName.equals(businessNameFromData)){
                businessNumber = i+1; // get business number in database

                businessRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        numOfPositions = (int) dataSnapshot.child("business " +businessNumber).
                                child("positions").getChildrenCount();
                        for (int j =0 ; j < numOfPositions; j++){
                            // read position name from database
                            String positionNameFromData = (String)dataSnapshot.
                                    child("business " + businessNumber).child("positions").
                                    child(String.valueOf(j)).child("positionName").getValue();
                            positionNameListFromData.add(positionNameFromData);
                        }
                        confirmedPosition(positionName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // when there is an error (no internet, no signal)
                        AlertDialog.Builder connectError = new AlertDialog.Builder
                                (confirmedBusinessesOfEmployed.this).
                                setMessage(databaseError.getMessage()).
                                setPositiveButton("ok", null);
                        connectError.show();
                    }
                });
            }
        }
    }

    /**
     * confirm the position that clicked with database and
     * read the employedList of position in business
     * @param positionName
     */
    public void confirmedPosition(String positionName){
        for (int i =0; i< numOfPositions; i++){
            String positionNameFromData = positionNameListFromData.get(i);
            if (positionName.equals(positionNameFromData)){
                positionNumber = i; // get position number in database

                businessRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        numOfEmployees = (int)dataSnapshot.child("business " + businessNumber).
                                child("positions").child(String.valueOf(positionNumber)).
                                child("employedList").getChildrenCount();
                        for (int i =0; i<numOfEmployees ;i++){
                            String usernameEmployedFromData = (String) dataSnapshot.
                                    child("business " +businessNumber).child("positions").
                                    child(String.valueOf(positionNumber)).child("employedList").
                                    child(String.valueOf(i)).child("userName").getValue();

                            usernameEmployedsList.add(usernameEmployedFromData);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // when there is an error (no internet, no signal)
                        AlertDialog.Builder connectError = new AlertDialog.Builder
                                (confirmedBusinessesOfEmployed.this).
                                setMessage(databaseError.getMessage()).
                                setPositiveButton("ok", null);
                        connectError.show();
                    }
                });

            }
        }
    }

    /**
     * build the rate alert dialog
     */
    public void buildLayoutAlertRate(){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder
                (confirmedBusinessesOfEmployed.this);
        View view = getLayoutInflater().inflate(R.layout.rate_business_layout, null);
        TextView rateText = (TextView) view.findViewById(R.id.rateTextView);
        RatingBar ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);

        alertBuilder.setView(view);
        alertBuilder.setPositiveButton("submit", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ratingBar.setMin(1);
                double rate = ratingBar.getRating();
                businessStringList = new ArrayList<>(); // reset the list of businesses to not add again
                changeRateForBusiness(rate);
            }
        }).
                setNegativeButton("cancel", null);
        AlertDialog dialog = alertBuilder.create();
        dialog.show();

    }


    /**
     * set value of the rate in employed attribute in database
     * @param rate
     */
    public void changeRateForBusiness(double rate){
        // define reference of the rate
        rateRef = database.getReference().child("Businesses").
                child("business " + businessNumber).child("positions").
                child(String.valueOf(positionNumber)).
                child("employedList");

        for (int i=0; i<numOfEmployees; i++){
            String employedUsernameFromData = usernameEmployedsList.get(i);
            if (username.equals(employedUsernameFromData)){
                employedNumberInEmployedsList = i;

                rateRef.child(String.valueOf(employedNumberInEmployedsList)).child("rate").setValue(rate);
            }
        }
    }
}
