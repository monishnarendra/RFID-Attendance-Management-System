package com.example.asus.ams_rfid_mnmg;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    EditText et_Email, et_Password;
    ProgressBar progressBar;
    private DatabaseReference mDatabase;
    private String Email_Id,RFID,Name,PhoneNo,USN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mAuth = FirebaseAuth.getInstance();

        et_Email = (EditText)findViewById(R.id.editText3);
        et_Password = (EditText)findViewById(R.id.editText2);
        progressBar = (ProgressBar) findViewById(R.id.progressBar3);

        Typeface myFont1 = Typeface.createFromAsset(getAssets(),"fonts/Android_Insomnia_Regular.ttf");  // For Headings
        //Typeface myFont2 = Typeface.createFromAsset(getAssets(),"fonts/Pacifico.ttf");                     // Signature
        //Typeface myFont3 = Typeface.createFromAsset(getAssets(),"fonts/GrandHotel-Regular.otf");

        et_Email.setTypeface(myFont1);
        et_Password.setTypeface(myFont1);
        //test1.setTypeface(myFont1);
        //textSignUp.setTypeface(myFont1);
        //SignInButton.setTypeface(myFont1);

    }
    public void SignUpText(View view){
        finish();
        startActivity(new Intent(MainActivity.this,SignUp.class));
    }

    public void Login(){

        final String User_Email = et_Email.getText().toString().trim();
        String Password = et_Password.getText().toString().trim();
        Email_Id = User_Email;

        if(User_Email.isEmpty()){
            et_Email.setError("Email ID is Required");
            et_Email.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(User_Email).matches()){
            et_Email.setError("Please Enter a Valid Email ID");
            et_Email.requestFocus();
            return;
        }

        if(Password.isEmpty()){
            et_Password.setError("Password is Required");
            et_Password.requestFocus();
            return;
        }

        if(Password.length() < 8){
            et_Password.setError("Password Must have Minimum 8 Charecters");
            et_Password.requestFocus();
            return;
        }

        if (!Password.matches("^(?=.*[@$%&#_()=+?»«<>£§€{}\\[\\]-])(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).*")) {
            et_Password.setError("Password Must have at least 1 upper case letter \n " +
                    "Password Must have at least 1 lower case letter \n " +
                    "Password Must have at least 1 digit \n ");
            et_Password.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(User_Email,Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);
                if(task.isSuccessful()){
                    finish();
                    Toast.makeText(getApplicationContext(),"Successfull Login",Toast.LENGTH_LONG).show();

                    mDatabase = FirebaseDatabase.getInstance().getReference();

                    mDatabase.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Email_Id = EncodeEmail(Email_Id);
                            RFID = dataSnapshot.child("Users").child(Email_Id).child("RFID").getValue().toString();
                            Name = dataSnapshot.child("Users").child(Email_Id).child("Fname").getValue().toString();
                            Name = Name + " " + dataSnapshot.child("Users").child(Email_Id).child("LName").getValue().toString();
                            PhoneNo = dataSnapshot.child("Users").child(Email_Id).child("PhoneNo").getValue().toString();
                            USN = dataSnapshot.child("Users").child(Email_Id).child("USN").getValue().toString();
                            Email_Id = DecodeEmail(Email_Id);
                            /*
                            Toast.makeText(getApplicationContext(),"Email: " + User_Email,Toast.LENGTH_LONG).show();
                            Toast.makeText(MainActivity.this, "RFID : " + RFID, Toast.LENGTH_SHORT).show();
                            Toast.makeText(MainActivity.this, "Name : " + Name, Toast.LENGTH_SHORT).show();
                            Toast.makeText(MainActivity.this, "PhoneNo : " + PhoneNo, Toast.LENGTH_SHORT).show();
                            Toast.makeText(MainActivity.this, "USN : " + USN, Toast.LENGTH_SHORT).show();
                            */
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Do something after 5s = 5000ms
                            Intent i = new Intent(MainActivity.this,MainProfile.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            i.putExtra("Email_Id",User_Email);
                            i.putExtra("RFID",RFID);
                            i.putExtra("Name",Name);
                            i.putExtra("PhoneNo",PhoneNo);
                            i.putExtra("USN",USN);
                            startActivity(i);
                        }
                    }, 7000);

                }else {
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public String EncodeEmail(String emailAddress){
        emailAddress = emailAddress.replace(".", ",");
        return emailAddress;
    }

    public String DecodeEmail(String emailAddress) {
        emailAddress = emailAddress.replace(",", ".");
        return emailAddress;
    }

    public void SignInButton(View view){
        Login();
    }
}