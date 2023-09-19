package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class loginToApp extends AppCompatActivity {

    private EditText loginPassword;
    private EditText loginUsername;
    private Button loginButton;
    private TextView loginText;
    private Switch switchButton;

    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private User user;

    private ArrayList<String> usernameList;
    private ArrayList<String> emailList;
    private ArrayList<String> passwordList;
    private ArrayList<String> namesList;

    private int numOfUsers = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_to_app);
        openScreen();
    }

    /**
     * this function just open login activity screen, for convenience
     */
    public void openScreen(){
        switchButton = (Switch) findViewById(R.id.switchButton);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child("Users");
        usernameList = new ArrayList<>();
        emailList = new ArrayList<>();
        passwordList = new ArrayList<>();
        namesList = new ArrayList<>();

        readFromDatabase();
        IHaveAnAccount();

    }

    /**
     * this function checks if the user click on (I have a User)
     */
    public void IHaveAnAccount(){
        switchButton.setChecked(true);
        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!switchButton.isChecked()){
                    openSignInScreen();
                }
            }
        });
    }

    /**
     * this function opens sign in screen
     */
    public void openSignInScreen(){
        Intent intentMain = new Intent(this, MainActivity.class);
        startActivity(intentMain);
    }

    /**
     * this function opens the next activity - choose screen with the user deatails.
     */
    public void openChooseScreen() {
        Intent intentChoose = new Intent(this, chooseUser.class);
        intentChoose.putExtra("name", user.getName()); // use the name in the next screen
        intentChoose.putExtra("username", user.getUserName());
        intentChoose.putExtra("password", user.getPassword());
        intentChoose.putExtra("email", user.getEmail());
        startService();
        startActivity(intentChoose);
    }

    /**
     * start service function - open service
     */
    public void startService(){
        Intent serviceIntent = new Intent(this, notificationService.class);
        serviceIntent.putExtra("username", user.getUserName());
        serviceIntent.putExtra("password", user.getPassword());
        startService(serviceIntent);
    }

    /**
     * this function reads users from real time data base in order to check if
     * the user exist in database.
     */
    public void readFromDatabase() {
        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //boolean exist = false;
                numOfUsers = (int) dataSnapshot.getChildrenCount();
                for (int i = 1; i <= dataSnapshot.getChildrenCount(); i++) {
                    String userFromData = ((String) dataSnapshot.child("user " + i).child("userName").getValue());
                    usernameList.add(userFromData); // user names list
                    String emailFromData = ((String) dataSnapshot.child("user " + i).child("email").getValue());
                    emailList.add(emailFromData); // emails list
                    String passwordFromData = ((String) dataSnapshot.child("user " + i).child("password").getValue());
                    passwordList.add(passwordFromData); // password list
                    String nameFromData = ((String) dataSnapshot.child("user " + i).child("name").getValue());
                    namesList.add(nameFromData); // names list
                }
                loginFunction();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // when there is an error (no internet, no signal)
                AlertDialog.Builder connectError = new AlertDialog.Builder(loginToApp.this)
                        .setMessage(databaseError.getMessage()).setPositiveButton("ok", null);
                connectError.show();
            }
        });
    }

    /**
     * this function checks the input of the user, if the input confirmed I will move to next
     * screen with my user with deatails from the input + database.
     */
    public void loginFunction(){
        loginText = (TextView) findViewById(R.id.loginText);
        loginUsername = (EditText) findViewById(R.id.loginUsername);
        loginPassword = (EditText) findViewById(R.id.loginPassword);
        loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean empty = false;
                boolean pass = false;
                if (String.valueOf(loginUsername.getText()).equals("") || String.valueOf(loginPassword.getText()).equals("")) {
                    empty = true;
                    AlertDialog.Builder emptyData = new AlertDialog.Builder(loginToApp.this)
                            .setMessage("Can not login, your details is empty").setPositiveButton("ok", null);
                    emptyData.show();
                }
                for(int i = 0; i<numOfUsers; i++){
                    String userFromData = usernameList.get(i);
                    String passwordFromData = passwordList.get(i);
                    if ((String.valueOf(loginUsername.getText()).equals(userFromData)) &&
                            (String.valueOf(loginPassword.getText()).equals(passwordFromData))) {
                        user = new User(userFromData, passwordFromData, namesList.get(i), emailList.get(i));
                        openChooseScreen();
                        pass = true;
                    }
                }
                if (!pass && !empty) {
                    AlertDialog.Builder notExist = new AlertDialog.Builder(loginToApp.this)
                            .setMessage("the user is not exist, sign in").setPositiveButton("ok", null);
                    notExist.show();
                }
            }
        });
    }

}
