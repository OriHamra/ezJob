package com.example.finalproject;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
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
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BusinessScreen extends AppCompatActivity{
    private TextView helloName;
    private EditText myBusinessName;
    private Button confirmation;

    private ArrayList<Position> positionTypeList;
    private ArrayList<String> positionsList; // the positions list to choose.
    private ArrayAdapter<String> positionsAdapter;
    private SearchView searchPosition;
    private ListView listViewPositions;
    private ArrayList<String> choosePositions; // final positions list
    private LatLng location;

    private SearchView searchAddress;
    Geocoder geocoder;
    List<Address> addresses;
    String customerAddress;

    private FirebaseDatabase database;
    private DatabaseReference businessRef;
    private int numOfBusinesses = 0;
    private Business business;

    private ArrayList<String> businessNameListFromData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_screen);
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

        helloName = (TextView) findViewById(R.id.hiName);
        myBusinessName = (EditText) findViewById(R.id.businessName);
        confirmation = (Button) findViewById(R.id.confirmation);

        database = FirebaseDatabase.getInstance();
        businessRef = database.getReference().child("Businesses");

        businessNameListFromData = new ArrayList<>();

        getNameOfUser(name);
        SearchPosition();
        getCustomerAddress();
        readFromDataBase();
        ClickOnConfirmation(name, username, password, email);

    }

    /**
     * this function open the business home page with the business from this activity
     * @param business
     */
    public void openBusinessHomePage(Business business) {
        addBusinessToDatabase(business);
        Intent BHomePage = new Intent(this, businessHomePage.class);
        BHomePage.putExtra("username", business.getUserName());
        BHomePage.putExtra("password", business.getPassword());
        startActivity(BHomePage);
    }

    /**
     * this function checks the input of the user, if the input confirmed i will
     * create new Business with the input deatails.
     * @param name
     * @param username
     * @param password
     * @param email
     */
    public void ClickOnConfirmation(final String name, final String username,
                                    final String password, final String email){
        confirmation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if the deatails is empty
                if (String.valueOf(myBusinessName.getText()).equals("") || customerAddress.equals("")
                        || choosePositions.size() == 0) {
                    AlertDialog.Builder emptyData = new AlertDialog.Builder(BusinessScreen.this)
                            .setMessage("Can not connect, your details is empty").
                                    setPositiveButton("ok", null);
                    emptyData.show();
                }
                // check if the business name is already exist in my database
                else if (businessNameListFromData.contains(String.valueOf(myBusinessName.getText()))){
                    AlertDialog.Builder existDialog = new AlertDialog.Builder(BusinessScreen.this)
                            .setMessage("Can not connect, the business name is already exist").
                                    setPositiveButton("ok", null);
                    existDialog.show();
                }
                else{
                    positionTypeList = new ArrayList<>();
                    ArrayList<String> emptyList= new ArrayList<String>();
                    emptyList.add("empty shifts");
                    for (String position:choosePositions){
                        ArrayList<Employed> emptyArrayList = new ArrayList<>();
                        positionTypeList.add(new Position(position, "scopeJob", emptyList,
                                "trail", 0.0,0.0, emptyArrayList)); // turn to list
                        // of position type with default values
                    }
                    String businessStatus = "open";
                    business = new Business(username, password, name, email, String.valueOf(myBusinessName.getText()),
                            customerAddress, positionTypeList, businessStatus, location); // create new business
                    openBusinessHomePage(business);
                }
            }
        });
    }

    /**
     * this function reads the number of businesses that registered to my app, with real time database.
     */
    public void readFromDataBase(){
        businessRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                numOfBusinesses = (int) dataSnapshot.getChildrenCount();
                for (int i=1; i<= numOfBusinesses;i++){
                    String businessNameFromData = (String) dataSnapshot.child("business " + i).child("businessName").getValue();
                    businessNameListFromData.add(businessNameFromData);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // when there is an error (no internet, no signal)
                AlertDialog.Builder connectError = new AlertDialog.Builder(BusinessScreen.this)
                        .setMessage(databaseError.getMessage()).setPositiveButton("ok", null);
                connectError.show();
            }
        });

    }

    /**
     * this function adds new Business to real time data base in the wanted reference.
     * @param business
     */
    public void addBusinessToDatabase(Business business){
        //write a message to database
        if (!business.getBusinessName().equals("") && !business.getBusinessPlace().equals("")
                && business.getPositions().size() != 0){
            businessRef.child("business " + ++numOfBusinesses).setValue(business); // writing business to reference
        }
    }



    /**
     * get the name of user
     * @param name
     */
    public void getNameOfUser(String name){

        helloName.setText("Hello " + name);
    }

    /**
     * this function responsible of the position List and set the string to the chosen position
     */
    public void SearchPosition() {
        searchPosition = (SearchView) findViewById(R.id.searchPositions);
        listViewPositions = (ListView) findViewById(R.id.listPositions);
        choosePositions = new ArrayList<String>();
        positionsList = new ArrayList<String>();
        Positions();
        positionsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, positionsList);
        listViewPositions.setAdapter(positionsAdapter);
        listViewPositions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String finalPo = String.valueOf(parent.getAdapter().getItem(position));
                if (!choosePositions.contains(finalPo)) { // if the user enter 2 times on the same position, add only 1 time
                    choosePositions.add(finalPo); // add to final chooses positions
                }
                searchPosition.setQuery(finalPo, false); // set query of the position searchView
                Toast loadingImageToast = Toast.makeText
                        (BusinessScreen.this, "add position if you want - search and press again",Toast.LENGTH_LONG);
                loadingImageToast.show();
            }
        });
        searchPosition.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                positionsAdapter.getFilter().filter(s);
                return false;
            }
        });
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
                String textSearchAddress = searchPlace
                        (getLocationFromAddress(customerAddress)[0], getLocationFromAddress(customerAddress)[1]);

                location = new LatLng(getLocationFromAddress(customerAddress)[0],
                        getLocationFromAddress(customerAddress)[1]);
                if (textSearchAddress != null && !textSearchAddress.equals("")) {
                    searchAddress.setQuery(textSearchAddress, false);
                    customerAddress = searchAddress.getQuery().toString();
                }
                else if (textSearchAddress.equals("")){
                    AlertDialog.Builder addressError = new AlertDialog.Builder(BusinessScreen.this)
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
     * this function gets the location and get from geocoder the real address
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
            // If any additional address line present than only, check with max
            // available address lines by getMaxAddressLineIndex()
        } catch (Exception e){
            e.printStackTrace();
        }
        return address;
    }


    /**
     * this function adds the positions to position list
     */
    public void Positions(){
        positionsList.add("אופה");
        positionsList.add("אח / אחות");
        positionsList.add("אחמש" );
        positionsList.add("אינסטלטור");
        positionsList.add("איש אחזקה");
        positionsList.add("איש מכירות");
        positionsList.add("איש בידור");
        positionsList.add("איש צוות שירות");
        positionsList.add("בייביסטר");
        positionsList.add("בל בוי");
        positionsList.add("בקר תנועה");
        positionsList.add("בריסטה");
        positionsList.add("ברמן");
        positionsList.add("גנן/גננת ילדים");
        positionsList.add("גנן/ גננת צמחייה");
        positionsList.add("גריל-מן");
        positionsList.add("דייל אירועים");
        positionsList.add("דייל מכירות");
        positionsList.add("חדרן");
        positionsList.add("חופף");
        positionsList.add("טבח");
        positionsList.add("טכנאי");
        positionsList.add("יועץ יופי");
        positionsList.add("ירקן");
        positionsList.add("מאבטח");
        positionsList.add("מאפר מקצועי");
        positionsList.add("מארח");
        positionsList.add("מדריך");
        positionsList.add("מדריך טיולים");
        positionsList.add("מדריך כושר");
        positionsList.add("מוכר");
        positionsList.add("מוכר אופטיקה");
        positionsList.add("מוקדן");
        positionsList.add("מורה פרטי");
        positionsList.add("מזכיר");
        positionsList.add("מחנה רכבים");
        positionsList.add("מחסנאי");
        positionsList.add("מטפל יופי");
        positionsList.add("מכונאי");
        positionsList.add("מלגזן");
        positionsList.add("מלצר");
        positionsList.add("מלקט סחורה");
        positionsList.add("מנהל");
        positionsList.add("מנהל אירועים");
        positionsList.add("מנהל בר");
        positionsList.add("מנהל הפצה");
        positionsList.add("מנהל חשבונות");
        positionsList.add("מנהל כספים");
        positionsList.add("מנהל מזון ומשקאות");
        positionsList.add("מנהל מחסן");
        positionsList.add("מנהל מטבח");
        positionsList.add("מנהל משק בית");
        positionsList.add("מנהל סניף");
        positionsList.add("מנהל קבלה");
        positionsList.add("מפיק אירועים");
        positionsList.add("מפעיל חדר בריחה");
        positionsList.add("מציל");
        positionsList.add("מרכזן");
        positionsList.add("מתווך");
        positionsList.add("מתדלק");
        positionsList.add("נהג");
        positionsList.add("נציג מכירות טלפוני");
        positionsList.add("נציג שירות ומכירה");
        positionsList.add("נציג שירות לקוחות");
        positionsList.add("נציג תמיכה טכנית");
        positionsList.add("סבל");
        positionsList.add("סגן מנהל");
        positionsList.add("סדרן הסעות");
        positionsList.add("סדרן סחורה");
        positionsList.add("סו-שף");
        positionsList.add("סוכן נדלן");
        positionsList.add("סייע");
        positionsList.add("ספר");
        positionsList.add("עובד דלפק");
        positionsList.add("עובד חקלאות");
        positionsList.add("עובד ייצור");
        positionsList.add("עובד מטבח");
        positionsList.add("עובד מיון ואריזה");
        positionsList.add("עובד ניקיון");
        positionsList.add("עובד פנים");
        positionsList.add("עורך דין");
        positionsList.add("פועל בניין");
        positionsList.add("פיקולו");
        positionsList.add("פקיד");
        positionsList.add("צקר");
        positionsList.add("צוות בר");
        positionsList.add("קונדיטור");
        positionsList.add("קופאי");
        positionsList.add("קצב");
        positionsList.add("קצין ביטחון");
        positionsList.add("רואה חשבון");
        positionsList.add("שוטף כלים");
        positionsList.add("שומר");
        positionsList.add("שליח");
        positionsList.add("שף");
        //positionsList.add("אחר");

    }

}
