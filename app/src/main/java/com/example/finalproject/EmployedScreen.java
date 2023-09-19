package com.example.finalproject;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class EmployedScreen extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener{
    private TextView hiClient;
    private EditText age;
    private EditText phone;
    private RadioGroup radioGroup;
    private String gender = "";
    private Button confirmation;

    private SearchView searchAddress;
    Geocoder geocoder;
    List<Address> addresses;
    String customerAddress = "";

    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private int numOfEmployeds;
    private Employed employed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employed_screen);
        openScreen();
    }

    /**
     * this function just open login activity screen, for convenience
     */
    public void openScreen(){
        String name = getIntent().getStringExtra("name");
        String username = getIntent().getStringExtra("username");
        String password = getIntent().getStringExtra("password");
        String email = getIntent().getStringExtra("email");

        hiClient = (TextView) findViewById(R.id.hiClient);
        age = (EditText) findViewById(R.id.age);
        phone = (EditText) findViewById(R.id.phone);
        confirmation = (Button) findViewById(R.id.confirmation);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(this);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child("Employeds");

        getCustomerAddress();
        readFromDataBase();
        ClickOnConfirmation(name, username, password, email);

    }


    /**
     * this function checks if the user is male or female.
     * @param group
     * @param checkedId
     */
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

        switch (checkedId){

            case R.id.male:
                gender = "male";
                break;

            case R.id.female:
                gender = "female";
                break;
        }

    }

    /**
     * this function open the employed home page with the employed from this activity
     * @param employed
     * @param username
     * @param password
     */
    public void openEmployedHomePage(Employed employed, String username, String password) {
        addEmployedToDatabase(employed);
        Intent EHomePage = new Intent(this, EmployedHomePage.class);
        EHomePage.putExtra("username", username);
        EHomePage.putExtra("password", password);
        startActivity(EHomePage);
    }


    /**
     * this function checks the input, if confirmed create new employed with input deatails values.
     * @param name
     * @param username
     * @param password
     * @param email
     */
    public void ClickOnConfirmation(final String name, final String username, final String password, final String email){
        confirmation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (String.valueOf(age.getText()).equals("") || customerAddress.equals("")
                        || String.valueOf(phone.getText()).equals("") || gender.equals("")) {
                    AlertDialog.Builder emptyData = new AlertDialog.Builder(EmployedScreen.this)
                            .setMessage("Can not connect, your details is empty").setPositiveButton("ok", null);
                    emptyData.show();
                }
                else if((Double.parseDouble(String.valueOf(age.getText())))< 15){
                    AlertDialog.Builder emptyData = new AlertDialog.Builder(EmployedScreen.this)
                            .setMessage("Can not connect, your age is too young").setPositiveButton("ok", null);
                    emptyData.show();
                }
                else if (String.valueOf(phone.getText()).length() != 10 || !String.valueOf(phone.getText()).startsWith("05")){
                    AlertDialog.Builder emptyData = new AlertDialog.Builder(EmployedScreen.this)
                            .setMessage("Can not connect, not valid phone").setPositiveButton("ok", null);
                    emptyData.show();
                }
                else{
                    String employedStatus = null; // employed status is for employedList in business confirm/decline
                    employed = new Employed(username, password, name, email,
                            Double.parseDouble(String.valueOf(age.getText())), gender,
                            String.valueOf(phone.getText()), customerAddress, employedStatus); // create new employed with rate attribute
                    openEmployedHomePage(employed, username, password);
                }
            }
        });
    }

    /**
     * his function reads the number of employees that registered to my app, with real time database.
     */
    public void readFromDataBase(){
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                numOfEmployeds = (int) dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // when there is an error (no internet, no signal)
                AlertDialog.Builder connectError = new AlertDialog.Builder(EmployedScreen.this)
                        .setMessage(databaseError.getMessage()).setPositiveButton("ok", null);
                connectError.show();
            }
        });

    }

    /**
     * this function add new Employed to real time data base in the wanted reference.
     * @param employed
     */
    public void addEmployedToDatabase(Employed employed){
        //write a message to database
        if (employed.getAge() != 0 && !employed.getGender().equals("") && !employed.getPhoneNumber().equals("") && !employed.getAddress().equals("")){
            myRef.child("employed " + ++numOfEmployeds).setValue(employed); // writing employed to reference
        }
    }


    /**
     * this function gets the real address, after get the location of the user input.
     */
    public void getCustomerAddress() {
        searchAddress = (SearchView) findViewById(R.id.searchAddress);
        searchAddress.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                customerAddress = query;
                String textSearchAddress =searchPlace(getLocationFromAddress(customerAddress)[0], getLocationFromAddress(customerAddress)[1]);

                if (textSearchAddress != null && !textSearchAddress.equals("")) {
                    searchAddress.setQuery(textSearchAddress, false);
                    customerAddress = searchAddress.getQuery().toString();
                }
                else if (textSearchAddress.equals("")){
                    AlertDialog.Builder addressError = new AlertDialog.Builder(EmployedScreen.this)
                            .setMessage("Location not found - try to be more specific").setPositiveButton("ok", null);
                    addressError.show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                customerAddress = newText;
                return false;
            }
        });
    }


    /**
     * this function gets address and do geo coding - address to LatLng
     * @param strAddress
     * @return
     */
    public double[] getLocationFromAddress(String strAddress){
        Geocoder coder = new Geocoder(this);
        List<Address> address;
        double latitude = 0.0;
        double longitude = 0.0;

        try {
            address = coder.getFromLocationName(strAddress,5);
            if (address==null) {
                return null;
            }
            Address location=address.get(0);
            latitude = location.getLatitude();
            longitude = location.getLongitude();

        } catch (Exception e){
            e.printStackTrace();
        }

        double[] latlangArr = {latitude, longitude};
        return latlangArr;
    }


    /**
     * this function gets the location. The function get from geocoder the real address
     * @param latitude
     * @param longitude
     * @return
     */
    public String searchPlace(double latitude, double longitude){
        geocoder = new Geocoder(this, Locale.getDefault());
        String address = "";
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 5);
            // Here 5 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            address = addresses.get(0).getAddressLine(0);
            // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        } catch (Exception e){
            e.printStackTrace();
        }
        return address;
    }
}
