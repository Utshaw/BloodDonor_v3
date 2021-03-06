package io.github.utshaw.blooddonor_v3;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Utshaw on 10/1/2017.
 */

public class CreateUserActivity extends AppCompatActivity {

    int PLACE_PICKER_REQUEST = 1;

    static final int DIALOG_ID = 0;

    private String authMobileNo;

    private EditText mNameEditText;
    private EditText mMobileNoEditText;
    private EditText mWeightEditText;
    private EditText mAddressEditText;
    private TextView mAuthMobileTextView;
    private Spinner mGenderSpinner;
    private Spinner mBloodGroupSpinner;
    private boolean mGender = true; // true for Male
    private KeyListener editTextKeyListener;
    private Button locationChooserButton;
    private EditText datePickerEditText;
    private long lastDoantionDate;

    CheckBox activateCheckbox;

    boolean activeDonor = true;


    private int year_x, month_x, day_x;


    //Firebase instances
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        authMobileNo = getIntent().getExtras().getString("phoneNumber");
        Log.e("KEY", authMobileNo);

        mNameEditText = (EditText) findViewById(R.id.edit_name);
        mMobileNoEditText = (EditText) findViewById(R.id.edit_mobile_no);
        mWeightEditText = (EditText) findViewById(R.id.edit_weight);
        mAddressEditText = (EditText) findViewById(R.id.edit_address);
        mAuthMobileTextView = (TextView) findViewById(R.id.edit_auth_mobile);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);
        mBloodGroupSpinner = (Spinner) findViewById(R.id.spinner_blood_group);

        activateCheckbox = (CheckBox) findViewById(R.id.activate_acc);
        activateCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.e("Utshaw", "Active Donor");
                    activeDonor = true;
                } else {
                    Log.e("Utshaw", "Inactive Donor");
                    activeDonor = false;
                    Snackbar.make(findViewById(android.R.id.content), "You can activate account later.", Snackbar.LENGTH_SHORT).show();
                }
            }
        });


        datePickerEditText = (EditText) findViewById(R.id.edit_date);
        datePickerEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_ID);
            }
        });


        final Calendar cal = Calendar.getInstance();
        year_x = cal.get(Calendar.YEAR);
        month_x = cal.get(Calendar.MONTH);
        day_x = cal.get(Calendar.DAY_OF_MONTH);


        locationChooserButton = (Button) findViewById(R.id.location_btn);

        locationChooserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAddressEditText.setKeyListener(null);
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(CreateUserActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });


        setupSpinner();


        editTextKeyListener = mAddressEditText.getKeyListener();


        final KeyListener editTextKeyListener = mAddressEditText.getKeyListener();


        mAuthMobileTextView.setText(authMobileNo);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_ID) {
            return new DatePickerDialog(this, dpickerListener, year_x, month_x, day_x);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener dpickerListener
            = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            year_x = year;
            month_x = month + 1;
            day_x = dayOfMonth;

            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");

            String strDate = day_x + "/" + month_x + "/" + year_x;

            Log.e("Utshaw", strDate);

            Date date = null;
            try {
                date = dateFormatter.parse(strDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            lastDoantionDate = date.getTime();

            dateFormatter.applyPattern("MMM dd, yyyy");
            String dateToDisplay = dateFormatter.format(date);

            datePickerEditText.setText(dateToDisplay);
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_PICKER_REQUEST) {

            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                locationChooserButton.setText("Change Location");
                mAddressEditText.setVisibility(View.VISIBLE);
                mAddressEditText.setText(place.getName() + ", " + place.getAddress());
                UserLocationInfo.setUserLatitude(place.getLatLng().latitude);
                UserLocationInfo.setUserLongitude(place.getLatLng().longitude);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (isNetworkAvailable(getApplicationContext())) {
                    String accName = mNameEditText.getText().toString().trim();
                    String accMobile = mMobileNoEditText.getText().toString().trim();
                    String accWeight = mWeightEditText.getText().toString().trim();
                    boolean gender = mGender;
                    String accBlood = mBloodGroupSpinner.getSelectedItem().toString();
                    String location = mAddressEditText.getText().toString();
                    if (accName.length() < 1 || accMobile.length() < 1 || accWeight.length() < 1 || accBlood.length() < 1 || location.length() < 1 || datePickerEditText.getText().toString().length() < 1) {
                        Toast.makeText(this, "Please fill up all the field!", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    User user = null;
                    user = new User(accName, accMobile, mGender, accBlood, accWeight, location, UserLocationInfo.userLatitude, UserLocationInfo.getUserLongitude(), lastDoantionDate, activeDonor);
                    mDatabaseReference.child("users").child(accBlood).child(authMobileNo).setValue(user);
                    mDatabaseReference.child("accounts").child(authMobileNo).setValue(accBlood);


                    if (activeDonor) {
                        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                        mDatabaseReference.child("user_tokens").child(accBlood).child(authMobileNo).setValue(refreshedToken);
                    }

                    Toast.makeText(this, "Account Created", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Check internet connection", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_sign_out:
                if (isNetworkAvailable(getApplicationContext())) {
                    FirebaseAuth.getInstance().signOut();
                    Toast.makeText(this, "Signing Out From Account", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Signing out failed.Check internet connection!", Toast.LENGTH_SHORT).show();
                }
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        ArrayAdapter bloodGroupSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_blood_groups, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        bloodGroupSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);
        mBloodGroupSpinner.setAdapter(bloodGroupSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = true; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = false; // Female
                    }
                }
            }


            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = true; // Male
            }


        });
    }


}
