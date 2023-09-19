package com.example.finalproject;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

public class positionScreen extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener{

    private TextView textViewPositionName, titleScope, titleShift;
    private RadioGroup radioGroup;
    private RadioButton fullJobRadio, partialJobRadio;
    private CheckBox morningBox, noonBox, eveningBox, nightBox;
    private EditText trail, ageFrom, salary;
    private Button submit;

    private String scopeJob;
    private ArrayList<String> shiftTypesList;

    private ArrayList<String> usernameList;
    private ArrayList<String> passwordList;
    private ArrayList<String> positionNameList;


    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private int numOfBusinesses;
    private int numOfPositions;

    private String username;
    private String password;
    private String stringPositionName;
    private int businessNumber;
    private int positionNumber;

    private Position position;
    private ArrayList<Employed> employedListFromData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_position_screen);
        openScreen();
    }

    /**
     * this function just opens position activity screen, for convenience
     */
    public void openScreen(){
        titleScope = (TextView) findViewById(R.id.textViewScope);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        fullJobRadio = (RadioButton) findViewById(R.id.fullJobRadio);
        partialJobRadio = (RadioButton) findViewById(R.id.partJobRadio);
        radioGroup.setOnCheckedChangeListener(this);
        titleShift = (TextView) findViewById(R.id.textViewShiftType);
        morningBox = (CheckBox) findViewById(R.id.morningBox);
        noonBox = (CheckBox) findViewById(R.id.noonBox);
        eveningBox = (CheckBox) findViewById(R.id.eveningBox);
        nightBox = (CheckBox) findViewById(R.id.nightBox);
        trail = (EditText) findViewById(R.id.trail);
        ageFrom = (EditText) findViewById(R.id.ageFrom);
        salary = (EditText) findViewById(R.id.salary);
        submit = (Button) findViewById(R.id.submit);


        database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child("Businesses");

        usernameList = new ArrayList<>();
        passwordList = new ArrayList<>();
        shiftTypesList = new ArrayList<>();
        positionNameList = new ArrayList<>();

        employedListFromData = new ArrayList<>();

        getPositionName();
        ReadFromDatabase();
    }

    /**
     * get the position name from the last activity
     */
    public void getPositionName(){
        textViewPositionName = (TextView) findViewById(R.id.positionName);
        stringPositionName = getIntent().getStringExtra("positionName");
        textViewPositionName.setText(stringPositionName);
    }

    /**
     * check if the position required full job or partial.
     * @param group
     * @param checkedId
     */
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

        switch (checkedId){

            case R.id.fullJobRadio:
                scopeJob = "full job";
                break;

            case R.id.partJobRadio:
                scopeJob = "partial job";
                break;
        }

    }


    /**
     * read businesses from data base and get the specific business that
     * use in the app (businessNumber)
     */
    public void ReadFromDatabase(){
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                numOfBusinesses = (int) dataSnapshot.getChildrenCount();
                for (int i = 1; i <= dataSnapshot.getChildrenCount(); i++){
                    String businessUsernameFromData = ((String) dataSnapshot.child("business " + i).
                            child("userName").getValue());
                    usernameList.add(businessUsernameFromData); // user names list
                    String businessPasswordFromData = ((String) dataSnapshot.child("business " + i).
                            child("password").getValue());
                    passwordList.add(businessPasswordFromData); // password list

                    businessNumber = getIntent().getIntExtra("businessNumber", 0);
                    numOfPositions = (int) dataSnapshot.child("business " +businessNumber).
                            child("positions").getChildrenCount();
                    for (int j =0 ; j < numOfPositions; j++){
                        // read position features
                        String positionNameFromData = ((String)dataSnapshot.
                                child("business " + businessNumber).child("positions").
                                child(String.valueOf(j)).child("positionName").getValue());
                        positionNameList.add(positionNameFromData);
                    }
                }
                setPositionValues();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // when there is an error (no internet, no signal)
                AlertDialog.Builder connectError = new AlertDialog.Builder(positionScreen.this)
                        .setMessage(databaseError.getMessage()).setPositiveButton("ok", null);
                connectError.show();
            }
        });
    }

    /**
     * set the position values from data base, to be update
     */
    public void setPositionValues() {
        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");
        for (int i = 0; i < numOfBusinesses; i++) {
            String userFromData = usernameList.get(i);
            String passwordFromData = passwordList.get(i);
            if (username.equals(userFromData) && password.equals(passwordFromData)) {
                for (int j = 0; j< numOfPositions; j++){
                    String positionName = positionNameList.get(j);
                    if(stringPositionName.equals(positionName)){
                        positionNumber = j;
                        myRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                long ageFromData = (long) dataSnapshot.
                                        child("business " + businessNumber).child("positions").
                                        child(String.valueOf(positionNumber)).child("age").getValue();
                                if (ageFromData != 0) {
                                    ageFrom.setText(String.valueOf(ageFromData));
                                }
                                //==================================================================
                                long salaryFromData = (long) dataSnapshot.
                                        child("business " + businessNumber).child("positions").
                                        child(String.valueOf(positionNumber)).child("hourSalary").getValue();
                                if (salaryFromData !=0) {
                                    salary.setText(String.valueOf(salaryFromData));
                                }
                                //==================================================================
                                String trailFromData = (String) dataSnapshot.
                                        child("business " + businessNumber).child("positions").
                                        child(String.valueOf(positionNumber)).child("trail").getValue();
                                if (!trailFromData.equals("trail")) {
                                    trail.setText(trailFromData);
                                }
                                //==================================================================
                                String scopeFromData = (String) dataSnapshot.
                                        child("business " + businessNumber).child("positions").
                                        child(String.valueOf(positionNumber)).child("scopeJob").getValue();
                                if (scopeFromData.equals("full job")) {
                                    fullJobRadio.setChecked(true);
                                }
                                if (scopeFromData.equals("partial job")){
                                    partialJobRadio.setChecked(true);
                                }
                                //==================================================================
                                ArrayList<String> shiftTypesFromData = (ArrayList<String>) dataSnapshot.
                                        child("business " + businessNumber).child("positions").
                                        child(String.valueOf(positionNumber)).child("shiftTypes").getValue();
                                for (String shift: shiftTypesFromData){
                                    if(shift.equals("morning shift")){
                                        morningBox.setChecked(true);
                                    }
                                    if(shift.equals("noon shift")){
                                        noonBox.setChecked(true);
                                    }
                                    if (shift.equals("evening shift")){
                                        eveningBox.setChecked(true);
                                    }
                                    if (shift.equals("night shift")){
                                        nightBox.setChecked(true);
                                    }
                                }
                                ReadFromDataEmployedListOfPosition(businessNumber, positionNumber);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // when there is an error (no internet, no signal)
                                AlertDialog.Builder connectError = new AlertDialog.
                                        Builder(positionScreen.this).
                                        setMessage(databaseError.getMessage()).
                                        setPositiveButton("ok", null);
                                connectError.show();
                            }
                        });
                    }
                }
            }
        }
        checkValidation();
    }

    /**
     * read the employed list, to set value of position without touch the employed list, only
     * change deatails values
     * @param businessNumber
     * @param positionNumber
     */
    public void ReadFromDataEmployedListOfPosition(int businessNumber, int positionNumber){
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("business " + businessNumber).child("positions").child(String.valueOf(positionNumber)).child("employedList").getValue() != null){
                    int numOfCandidate = (int) dataSnapshot.child("business " + businessNumber).child("positions").child(String.valueOf(positionNumber)).child("employedList").getChildrenCount();
                    for (int i = 0;i< numOfCandidate;i++) {

                        String addressEmployedFromData = (String) dataSnapshot.
                                child("business " +businessNumber).child("positions").
                                child(String.valueOf(positionNumber)).child("employedList").
                                child(String.valueOf(i)).child("address").getValue();

                        String ageEmployedFromData = String.valueOf ((long) dataSnapshot.
                                child("business " +businessNumber).child("positions").
                                child(String.valueOf(positionNumber)).child("employedList").
                                child(String.valueOf(i)).child("age").getValue());

                        String emailEmployedFromData = (String) dataSnapshot.
                                child("business " +businessNumber).child("positions").
                                child(String.valueOf(positionNumber)).child("employedList").
                                child(String.valueOf(i)).child("email").getValue();

                        String genderEmployedFromData = (String) dataSnapshot.
                                child("business " +businessNumber).child("positions").
                                child(String.valueOf(positionNumber)).child("employedList").
                                child(String.valueOf(i)).child("gender").getValue();

                        String nameEmployedFromData = (String) dataSnapshot.
                                child("business " +businessNumber).child("positions").
                                child(String.valueOf(positionNumber)).child("employedList").
                                child(String.valueOf(i)).child("name").getValue();

                        String passwordEmployedFromData = (String) dataSnapshot.
                                child("business " +businessNumber).child("positions").
                                child(String.valueOf(positionNumber)).child("employedList").
                                child(String.valueOf(i)).child("password").getValue();

                        String phoneEmployedFromData = (String) dataSnapshot.
                                child("business " +businessNumber).child("positions").
                                child(String.valueOf(positionNumber)).child("employedList").
                                child(String.valueOf(i)).child("phoneNumber").getValue();

                        String usernameEmployedFromData = (String) dataSnapshot.
                                child("business " +businessNumber).child("positions").
                                child(String.valueOf(positionNumber)).child("employedList").
                                child(String.valueOf(i)).child("userName").getValue();

                        String statusEmployedFromData = (String) dataSnapshot.
                                child("business " +businessNumber).child("positions").
                                child(String.valueOf(positionNumber)).child("employedList").
                                child(String.valueOf(i)).child("status").getValue();

                        double ratEmployedFromData = (Double.parseDouble(String.valueOf(dataSnapshot.
                                child("business " +businessNumber).child("positions").
                                child(String.valueOf(positionNumber)).child("employedList").
                                child(String.valueOf(i)).child("rate").getValue())));

                        employedListFromData.add(new Employed(usernameEmployedFromData,
                                passwordEmployedFromData, nameEmployedFromData, emailEmployedFromData,
                                Double.parseDouble(ageEmployedFromData), genderEmployedFromData,
                                phoneEmployedFromData, addressEmployedFromData, statusEmployedFromData, ratEmployedFromData));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // when there is an error (no internet, no signal)
                AlertDialog.Builder connectError = new AlertDialog.Builder(positionScreen.this).
                        setMessage(databaseError.getMessage()).setPositiveButton("ok", null);
                connectError.show();
            }
        });
    }

    /**
     * check validation of user input
     */
    public void checkValidation(){
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (morningBox.isChecked()){
                    shiftTypesList.add("morning shift");
                }
                if (noonBox.isChecked()){
                    shiftTypesList.add("noon shift");
                }
                if (eveningBox.isChecked()){
                    shiftTypesList.add("evening shift");
                }
                if (nightBox.isChecked()){
                    shiftTypesList.add("night shift");
                }

                if (scopeJob == null || shiftTypesList.size() == 0 ||
                        String.valueOf(trail.getText()).equals("") ||
                        String.valueOf(ageFrom.getText()).equals("") ||
                        String.valueOf(salary.getText()).equals("")){
                    AlertDialog.Builder emptyData = new AlertDialog.Builder(positionScreen.this).
                            setMessage("Can not connect, your details is empty").
                            setPositiveButton("ok", null);
                    emptyData.show();
                }
                else{
                    position = new Position(stringPositionName, scopeJob, shiftTypesList,
                            String.valueOf(trail.getText()),
                            Double.parseDouble(String.valueOf(ageFrom.getText())),
                            Double.parseDouble(String.valueOf(salary.getText())), employedListFromData);
                    addPositionDeatailsToDatabase(position);
                }
                submit.setEnabled(false);
            }
        });
    }


    /**
     * change the default values to the user input values / update the values of position
     * @param position
     */
    public void addPositionDeatailsToDatabase(Position position){
        if (!position.getScopeJob().equals("") && position.getShiftTypes().size() != 0 &&
                !position.getTrail().equals("") && position.getAge() != 0 && position.getHourSalary() != 0){
            myRef.child("business " + businessNumber).child("positions").
                    child(String.valueOf(positionNameList.indexOf(stringPositionName))).setValue(position);
        }
    }


}
