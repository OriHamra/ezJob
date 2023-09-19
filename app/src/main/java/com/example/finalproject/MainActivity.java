package com.example.finalproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private EditText passWord;
    private int numOfUsers = 0;
    private TextView signInText;
    private EditText username;
    private EditText name;
    private EditText email;
    private Button signInButton;
    private Switch switchButton;

    private CheckBox rememberBox;


    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private User user;
    private ArrayList<String> usernameList;
    private ArrayList<String> emailList;
    private ArrayList<String> passwordList;
    private ArrayList<String> namesList;

    public static final String SHARED_PREFS = "sharedPrefs"; //name of shared prefs
    public static final String KEY_USERNAME = "usernameMe"; // shared prefs value to save - remember me
    public static final String KEY_PASSWORD = "passwordMe"; // shared prefs value to save - remember me
    public static final String KEY_EMAIL = "emailMe"; // shared prefs value to save - remember me
    public static final String KEY_NAME = "nameMe"; // shared prefs value to save - remember me
    public static final String KEY_REMEMBER = "rememberMe"; // shared prefs value to save - remember me

    public static final String EMAIL_REGEX = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:" +
            "[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b" +
            "\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]" +
            "*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5" +
            "[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b" +
            "\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        openScreen();
    }

    /**
     * this function just open main activity screen, for convenience
     */
    public void openScreen() {
        // todo - write documentation here
        signInText = (TextView) findViewById(R.id.signInText);
        username = (EditText) findViewById(R.id.username);
        passWord = (EditText) findViewById(R.id.password);
        name = (EditText) findViewById(R.id.name);
        email = (EditText) findViewById(R.id.email);
        signInButton = (Button) findViewById(R.id.signInButton);
        rememberBox = (CheckBox) findViewById(R.id.rememberMeBox);
        switchButton = (Switch) findViewById(R.id.switchButton);


        database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child("Users");
        usernameList = new ArrayList<>();
        emailList = new ArrayList<>();
        passwordList = new ArrayList<>();
        namesList = new ArrayList<>();

        loadRemember(); // load data values of the user

        readFromDatabase();
        IHaveAnAccount();
    }

    /**
     * this function checks if the user click on (I have a User)
     */
    public void IHaveAnAccount(){
        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchButton.isChecked()){
                    openLoginScreen();
                }
            }
        });
    }

    /**
     * this function opens login screen
     */
    public void openLoginScreen(){
        Intent intentLogin = new Intent(this, loginToApp.class);
        startActivity(intentLogin);
    }

    /**
     * this function checks the input of the user, if the input confirmed I will call to checkValidation
     * function with new user with deatails from the input.
     */
    public void SignInFunction() {
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (String.valueOf(username.getText()).equals("") || String.valueOf(passWord.getText()).equals("") ||
                        String.valueOf(name.getText()).equals("") || String.valueOf(email.getText()).equals("")) {
                    AlertDialog.Builder emptyData = new AlertDialog.Builder(MainActivity.this)
                            .setMessage("Can not connect, your details is empty").setPositiveButton("ok", null);
                    emptyData.show();
                }

                 else if (!(String.valueOf(email.getText()).matches(EMAIL_REGEX))) {
                    AlertDialog.Builder emptyData = new AlertDialog.Builder(MainActivity.this)
                            .setMessage("Can not connect, your email address is not valid, please try again").setPositiveButton("ok", null);
                    emptyData.show();
                }
                 else if (String.valueOf(username.getText()).length() < 5){
                    AlertDialog.Builder invalidUsername = new AlertDialog.Builder(MainActivity.this)
                            .setMessage("Can not connect, your username need to be more then 5 chars, please try again").
                                    setPositiveButton("ok", null);
                    invalidUsername.show();
                }
                 else if(String.valueOf(passWord.getText()).length() < 5){
                    AlertDialog.Builder invalidPassword = new AlertDialog.Builder(MainActivity.this)
                            .setMessage("your password need to be more then 5 chars, please try again").
                                    setPositiveButton("ok", null);
                    invalidPassword.show();
                }
                 else {
                    user = new User(String.valueOf(username.getText()), String.valueOf(passWord.getText()),
                            String.valueOf(name.getText()), String.valueOf(email.getText()));
                    checkValidation();

                }
            }
        });
    }

    /**
     * load user deatails from shared preferences- to login with
     * remember me
     */
    public void loadRemember(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String phoneUsername = sharedPreferences.getString(KEY_USERNAME, "");
        String phonePassword = sharedPreferences.getString(KEY_PASSWORD, "");
        String phoneEmail = sharedPreferences.getString(KEY_EMAIL, "");
        String phoneName = sharedPreferences.getString(KEY_NAME, "");
        // create new user with data from shared preferences
        user = new User(phoneUsername, phonePassword, phoneName, phoneEmail);

        boolean phoneCheckbox = sharedPreferences.getBoolean(KEY_REMEMBER, false);
        if (phoneCheckbox){
            openChooseScreen();
            finish();
        }
    }

    /**
     * put the user values in shared preferences - to do remember me
     */
    public void rememberMe(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_REMEMBER, true);
        editor.putString(KEY_USERNAME,user.getUserName());
        editor.putString(KEY_PASSWORD, user.getPassword());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_NAME, user.getName());
        editor.apply();
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
     * this function adds user to real time data base in the wanted reference.
     * @param user
     */
    public void addUserToDatabase(User user) {
        // Write a message to the database
        if (!user.getName().equals("") && !user.getUserName().equals("") &&
                !user.getPassword().equals("") && !user.getEmail().equals("")) {
            myRef.child("user " + ++numOfUsers).setValue(user); // writing user to reference
        }
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
                    String userFromData = ((String) dataSnapshot.
                            child("user " + i).child("userName").getValue());
                    usernameList.add(userFromData); // user names list
                    String emailFromData = ((String) dataSnapshot.
                            child("user " + i).child("email").getValue());
                    emailList.add(emailFromData); // emails list
                    String passwordFromData = ((String) dataSnapshot.
                            child("user " + i).child("password").getValue());
                    passwordList.add(passwordFromData); // password list
                    String nameFromData = ((String) dataSnapshot.
                            child("user " + i).child("name").getValue());
                    namesList.add(nameFromData); // names list
                }
                SignInFunction();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // when there is an error (no internet, no signal)
                AlertDialog.Builder connectError = new AlertDialog.Builder(MainActivity.this).setMessage(databaseError.getMessage()).setPositiveButton("ok", null);
                connectError.show();
            }
        });
    }

    /**
     * this function checks if the user exist in database.
     */
    public void checkValidation() {
        // Read from the database
        boolean exist = false;
        for (int i = 0; i < numOfUsers; i++) {
            String userFromData = usernameList.get(i);
            String emailFromData = emailList.get(i);
            if (user.getUserName().equals(userFromData) || user.getEmail().equals(emailFromData)) {
                AlertDialog.Builder alreadyExist = new AlertDialog.Builder(MainActivity.this).setMessage("Can not sign in, already exist").setPositiveButton("ok", null);
                alreadyExist.show();
                exist = true;
            }
        }
        // user not exist - user can sign in
        if (!exist) {
            addUserToDatabase(user);
            if (rememberBox.isChecked()){
                rememberMe();
            }
            openChooseScreen();
        }
    }
}


