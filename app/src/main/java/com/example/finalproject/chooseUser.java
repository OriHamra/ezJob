package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class chooseUser extends AppCompatActivity {
    private Button business;
    private Button employed;
    private TextView choose;

    private Intent intentBusiness;
    private Intent intentEmployed;
    private Intent intentBusinessHomePage;
    private Intent intentEmployedHomePage;

    private FirebaseDatabase database;
    private DatabaseReference myRefEmployed; // read from Employdes database
    private DatabaseReference myRefBusiness; // read from Business database

    private String username;
    private String password;

    private ArrayList<String> businessUsernameList;
    private ArrayList<String> businessPasswordList;
    private int numOfBusinesses = 0;

    private ArrayList<String> employedUsernameList;
    private ArrayList<String> employedPasswordList;

    private int numOfEmployeds = 0;

    private Boolean openBusinessHomePage = false;
    private Boolean openEmployedHomePage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_user);
        openScreen();
    }

    /**
     * this function just opens choose activity screen, for convenience
     */
    public void openScreen(){
        business = (Button) findViewById(R.id.business);
        employed = (Button) findViewById(R.id.employed);
        choose = (TextView) findViewById(R.id.choose);

        database = FirebaseDatabase.getInstance();
        myRefBusiness = database.getReference().child("Businesses");
        myRefEmployed = database.getReference().child("Employeds");
        businessUsernameList = new ArrayList<>();
        businessPasswordList = new ArrayList<>();
        employedUsernameList = new ArrayList<>();
        employedPasswordList = new ArrayList<>();


        intentBusiness = new Intent(this, BusinessScreen.class);
        intentEmployed = new Intent(this, EmployedScreen.class);
        intentBusinessHomePage = new Intent(this,businessHomePage.class);
        intentEmployedHomePage = new Intent(this, EmployedHomePage.class);


        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");

        readFromDataBaseBusiness();
        ReadFromDatabaseEmployed();
        clickOnBusiness();
        clickOnEmployed();

    }


    /**
     * function that opens the next screen - businessScreen / employedScreen.
     * @param intent
     */
    public void openNextScreen(Intent intent){
        String name = getIntent().getStringExtra("name");
        String email = getIntent().getStringExtra("email");
        intent.putExtra("name", name); // use name in the next screen
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        intent.putExtra("email", email);
        startActivity(intent);
    }

    public void clickOnBusiness(){
        business.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginToBusinessHome();
                if (!openBusinessHomePage) {
                    openNextScreen(intentBusiness);
                }
            }
        });
    }


    public void clickOnEmployed(){
        employed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginToEmployedHome();
                if (!openEmployedHomePage) {
                    openNextScreen(intentEmployed);
                }
            }
        });
    }

    /**
     * read the number of businesses in real time database
     */
    public void readFromDataBaseBusiness(){
        myRefBusiness.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                numOfBusinesses = (int) dataSnapshot.getChildrenCount();
                for (int i = 1; i <= dataSnapshot.getChildrenCount(); i++){
                    String businessUsernameFromData = ((String) dataSnapshot.
                            child("business " + i).child("userName").getValue());
                    businessUsernameList.add(businessUsernameFromData); // user names list
                    String businessPasswordFromData = ((String) dataSnapshot.
                            child("business " + i).child("password").getValue());
                    businessPasswordList.add(businessPasswordFromData); // password list
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // when there is an error (no internet, no signal)
                AlertDialog.Builder connectError = new AlertDialog.Builder(chooseUser.this).
                        setMessage(databaseError.getMessage()).setPositiveButton("ok", null);
                connectError.show();
            }
        });

    }

    /**
     * read the number of employees in real time database
     */
    public void ReadFromDatabaseEmployed(){
        myRefEmployed.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                numOfEmployeds = (int) dataSnapshot.getChildrenCount();
                for (int i = 1; i <= dataSnapshot.getChildrenCount(); i++){

                    String businessUsernameFromData = ((String) dataSnapshot.
                            child("employed " + i).child("userName").getValue());
                    employedUsernameList.add(businessUsernameFromData); // user names list

                    String businessPasswordFromData = ((String) dataSnapshot.
                            child("employed " + i).child("password").getValue());
                    employedPasswordList.add(businessPasswordFromData); // password list
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // when there is an error (no internet, no signal)
                AlertDialog.Builder connectError = new AlertDialog.Builder(chooseUser.this).setMessage(databaseError.getMessage()).setPositiveButton("ok", null);
                connectError.show();
            }
        });
    }

    /**
     * check if the user exist as business so go to businessHomePage and
     * jump the register activity
     */
    public void loginToBusinessHome(){
        for (int i =0; i<numOfBusinesses;i++){
            String userFromData = businessUsernameList.get(i);
            String passwordFromData = businessPasswordList.get(i);
            if (username.equals(userFromData) && password.equals(passwordFromData)){
                // exist business, so jump to home page business screen
                openBusinessHomePage = true;
                openNextScreen(intentBusinessHomePage);
            }
        }
    }

    /**
     * check if the user exist as employed so go to EmployedHomePage and
     * jump the register activity
     */
    public void loginToEmployedHome(){
        for (int i =0; i<numOfEmployeds;i++){
            String userFromData = employedUsernameList.get(i);
            String passwordFromData = employedPasswordList.get(i);
            if (username.equals(userFromData) && password.equals(passwordFromData)){
                // exist business, so jump to home page business screen
                openEmployedHomePage = true;
                openNextScreen(intentEmployedHomePage);
            }
        }
    }
}

