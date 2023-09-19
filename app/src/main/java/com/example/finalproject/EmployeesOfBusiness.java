package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EmployeesOfBusiness extends AppCompatActivity {

    private TextView titleEmployees;

    private ArrayList<String> employedList;
    private ListView listViewEmployees;
    private ArrayAdapter<String> employeesAdapter;

    private String username;
    private String password;
    private int businessNumber;

    private ArrayList<String> usernameList;
    private ArrayList<String> passwordList;


    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private int numOfBusinesses;
    private int numOfCandidates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employees_of_business);
        openScreen();
    }

    /**
     * this function just open employees activity screen, for convenience
     */
    public void openScreen(){
        titleEmployees = (TextView) findViewById(R.id.titleCandidate);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child("Businesses");

        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");

        usernameList = new ArrayList<>();
        passwordList = new ArrayList<>();
        employedList = new ArrayList<>();

        ReadFromDatabase();
    }

    /**
     * read businesses from realtime database
     */
    public void ReadFromDatabase(){
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                numOfBusinesses = (int) dataSnapshot.getChildrenCount();
                for (int i = 1; i <= dataSnapshot.getChildrenCount(); i++){
                    String businessUsernameFromData = ((String) dataSnapshot.child("business " + i).child("userName").getValue());
                    usernameList.add(businessUsernameFromData); // user names list
                    String businessPasswordFromData = ((String) dataSnapshot.child("business " + i).child("password").getValue());
                    passwordList.add(businessPasswordFromData); // password list
                }
                getEmployeesDeatails();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // when there is an error (no internet, no signal)
                AlertDialog.Builder connectError = new AlertDialog.Builder(EmployeesOfBusiness.this).setMessage(databaseError.getMessage()).setPositiveButton("ok", null);
                connectError.show();
            }
        });
    }

    /**
     * get the employees list of the business, all of the employees who has confirmed status.
     */
    public void getEmployeesDeatails(){
        for (int i =0; i<numOfBusinesses;i++){
            String userFromData = usernameList.get(i);
            String passwordFromData = passwordList.get(i);
            if (username.equals(userFromData) && password.equals(passwordFromData)){
                businessNumber = i+1;
                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int numOfPositions = (int) dataSnapshot.child("business " +businessNumber).child("positions").getChildrenCount();
                        for (int j =0 ; j < numOfPositions; j++){
                            String positionName = (String) dataSnapshot.child("business " + businessNumber).
                                    child("positions").child(String.valueOf(j)).child("positionName").getValue();

                            numOfCandidates = (int)dataSnapshot.child("business " + businessNumber).
                                    child("positions").child(String.valueOf(j)).
                                    child("employedList").getChildrenCount();
                            for (int i =0; i<numOfCandidates;i++){
                                // add employed to candidateList and set to list view
                                String ageEmployedFromData = String.valueOf ((long) dataSnapshot.
                                        child("business " +businessNumber).child("positions").
                                        child(String.valueOf(j)).child("employedList").

                                        child(String.valueOf(i)).child("age").getValue());
                                String emailEmployedFromData = (String) dataSnapshot.
                                        child("business " +businessNumber).child("positions").
                                        child(String.valueOf(j)).child("employedList").
                                        child(String.valueOf(i)).child("email").getValue();

                                String genderEmployedFromData = (String) dataSnapshot.
                                        child("business " +businessNumber).child("positions").
                                        child(String.valueOf(j)).child("employedList").
                                        child(String.valueOf(i)).child("gender").getValue();

                                String nameEmployedFromData = (String) dataSnapshot.
                                        child("business " +businessNumber).child("positions").
                                        child(String.valueOf(j)).child("employedList").
                                        child(String.valueOf(i)).child("name").getValue();

                                String phoneEmployedFromData = (String) dataSnapshot.
                                        child("business " +businessNumber).child("positions").
                                        child(String.valueOf(j)).child("employedList").
                                        child(String.valueOf(i)).child("phoneNumber").getValue();

                                String statusEmployedFromData = (String) dataSnapshot.
                                        child("business " +businessNumber).child("positions").
                                        child(String.valueOf(j)).child("employedList").
                                        child(String.valueOf(i)).child("status").getValue();

                                String item = positionName + "\n\n" + "name: " + nameEmployedFromData
                                        + "\n" + "gender: " + genderEmployedFromData + "\n"
                                        + "age: " + ageEmployedFromData + "\n" + "email: "
                                        + emailEmployedFromData + "\n" + "phone number: " + phoneEmployedFromData;

                                if (!employedList.contains(item)){
                                    if (statusEmployedFromData.equals("confirm")) {
                                        employedList.add(item);
                                    }
                                }
                            }
                        }
                        showCandidateList();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // when there is an error (no internet, no signal)
                        AlertDialog.Builder connectError = new AlertDialog.Builder(EmployeesOfBusiness.this).
                                setMessage(databaseError.getMessage()).setPositiveButton("ok", null);
                        connectError.show();
                    }
                });
            }
        }
    }

    /**
     * show the candidates list view
     */
    public void showCandidateList(){
        listViewEmployees = (ListView) findViewById(R.id.listEmployees);

        employeesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, employedList);
        listViewEmployees.setAdapter(employeesAdapter);
        listViewEmployees.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String finalString = String.valueOf(parent.getAdapter().getItem(position));
                String positionName = finalString.split("\n\n")[0];

                String passEmployedName = finalString.split("\n")[2];
                String employedName = passEmployedName.split("name: ")[1];
                String employedEmailSection = finalString.split("email: ")[1]; // whats after the email section
                String employedmail = employedEmailSection.split("\nphone")[0]; // get the email address

                System.out.println(positionName);
                System.out.println(employedName);


                AlertDialog.Builder statusDialog = new AlertDialog.Builder(EmployeesOfBusiness.this)
                        .setMessage("employed status")
                        .setPositiveButton("remove employ", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                clickOnDelete(positionName, employedName, finalString);
                            }
                        }).setNegativeButton("send Email to employee", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + employedmail)));
                            }
                        })
                        ;
                statusDialog.show();

            }
        });
    }

    /**
     * remove employee from list view when clicked on delete by change his status
     * @param finalString
     */
    public void removeEmployed(String finalString){
        employedList.remove(finalString);
        employeesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, employedList);
        listViewEmployees.setAdapter(employeesAdapter);
    }

    public void clickOnDelete(String positionName, String employedName, String finalString){
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int numOfPositions = (int) dataSnapshot.child("business " +businessNumber).
                        child("positions").getChildrenCount();

                for (int i = 0; i<numOfPositions;i++){
                    String positionNameFromData = (String) dataSnapshot.child("business " + businessNumber)
                            .child("positions").child(String.valueOf(i)).child("positionName").getValue();
                    if (positionName.equals(positionNameFromData)){
                        int positionNumber = i;

                        int numOfCandidatesOfPosition = (int)dataSnapshot.
                                child("business " + businessNumber).child("positions").
                                child(String.valueOf(positionNumber)).child("employedList").getChildrenCount();

                        for (int j = 0; j<numOfCandidatesOfPosition;j++){

                            String nameEmployedFromData = (String) dataSnapshot.
                                    child("business " +businessNumber).child("positions").
                                    child(String.valueOf(positionNumber)).child("employedList").
                                    child(String.valueOf(j)).child("name").getValue();

                            if (employedName.equals(nameEmployedFromData)){
                                int employedNumber = j;
                                changeEmployedStatus(businessNumber, positionNumber, employedNumber, finalString);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // when there is an error (no internet, no signal)
                AlertDialog.Builder connectError = new AlertDialog.Builder(EmployeesOfBusiness.this).setMessage(databaseError.getMessage()).setPositiveButton("ok", null);
                connectError.show();
            }
        });
    }

    /**
     * set candidate status in database - decline
     * @param businessNumber
     * @param positionNumber
     * @param employedNumber
     * @param finalString
     */
    public void changeEmployedStatus(int businessNumber, int positionNumber, int employedNumber, String finalString){
        myRef.child("business " + businessNumber).child("positions").child(String.valueOf(positionNumber)).child("employedList").child(String.valueOf(employedNumber)).child("status").setValue("decline");
        removeEmployed(finalString);
    }
}
