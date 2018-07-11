package com.example.asus.ams_rfid_mnmg;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUp extends AppCompatActivity {

    EditText et_FName, et_LName, et_Email, et_Password, et_USN, et_PhoneNo, et_RFID;
    TextView tv_Heading;
    ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    String User_Email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Typeface myFont1 = Typeface.createFromAsset(getAssets(),"fonts/Android_Insomnia_Regular.ttf");  // For Headings
        Typeface myFont2 = Typeface.createFromAsset(getAssets(),"fonts/Pacifico.ttf");                  // Signature
        Typeface myFont3 = Typeface.createFromAsset(getAssets(),"fonts/GrandHotel-Regular.otf");

        et_PhoneNo = (EditText) findViewById(R.id.editText9);
        et_RFID = (EditText) findViewById(R.id.editText10);
        et_FName = (EditText) findViewById(R.id.editText4);
        et_LName = (EditText) findViewById(R.id.editText5);
        et_Email = (EditText) findViewById(R.id.editText6);
        et_Password = (EditText) findViewById(R.id.editText7);
        et_USN = (EditText) findViewById(R.id.editText);
        tv_Heading = (TextView) findViewById(R.id.textView2);

        tv_Heading.setTypeface(myFont1);
        et_FName.setTypeface(myFont1);
        et_LName.setTypeface(myFont1);
        et_Email.setTypeface(myFont1);
        et_Password.setTypeface(myFont1);
        et_USN.setTypeface(myFont1);
        et_PhoneNo.setTypeface(myFont1);
        et_RFID.setTypeface(myFont1);

        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
    }

    public void RegisterUser(){
        String Fname = et_FName.getText().toString().trim();
        String LName = et_LName.getText().toString().trim();
        User_Email = et_Email.getText().toString().trim();
        String Password = et_Password.getText().toString().trim();
        String USN = et_USN.getText().toString().trim();
        String PhoneNo = et_PhoneNo.getText().toString().trim();
        String RFID = et_RFID.getText().toString().trim();

        if(Fname.isEmpty()){
            et_FName.setError("First Name is Required");
            et_FName.requestFocus();
            return;
        }

        if(LName.isEmpty()){
            et_LName.setError("Last Name is Required");
            et_LName.requestFocus();
            return;
        }

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

        if(USN.isEmpty()){
            et_USN.setError("USN is Required");
            et_USN.requestFocus();
            return;
        }

        if (!USN.matches("^\\d[a-zA-Z]\\w{1}\\d{2}[a-zA-Z]\\w{1}\\d{3}$")) {
            et_USN.setError("Please Enter a Valid USN");
            et_USN.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        UploadToDataBase(Fname,LName,Password,USN,PhoneNo,RFID);

        mAuth.createUserWithEmailAndPassword(User_Email,Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);
                if(task.isSuccessful()){
                    finish();
                    Toast.makeText(getApplicationContext(),"User Registered Successfull",Toast.LENGTH_LONG).show();
                    Intent i = new Intent(SignUp.this,MainActivity.class);
                    i.putExtra("Email_Id",User_Email);
                    startActivity(i);
                }else {
                    if(task.getException() instanceof FirebaseAuthUserCollisionException){
                        Toast.makeText(getApplicationContext(),"User Already Registered",Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    public void UploadToDataBase(String Fname,String LName,String Password,String USN,String PhoneNo,String RFID){
        User_Email = EncodeString(User_Email);
        Toast.makeText(getApplicationContext(),User_Email,Toast.LENGTH_LONG).show();
        mDatabase.child("Users").child(User_Email).child("Fname").setValue(Fname);
        mDatabase.child("Users").child(User_Email).child("LName").setValue(LName);
        mDatabase.child("Users").child(User_Email).child("Password").setValue(Password);
        mDatabase.child("Users").child(User_Email).child("USN").setValue(USN);
        mDatabase.child("Users").child(User_Email).child("PhoneNo").setValue(PhoneNo);
        mDatabase.child("Users").child(User_Email).child("RFID").setValue(RFID);
        User_Email = DecodeString(User_Email);
    }

    public static String EncodeString(String string) {
        return string.replace(".", ",");
    }

    public static String DecodeString(String string) {
        return string.replace(",", ".");
    }

    public void SignUpButton(View view){
        RegisterUser();
    }

    public void SignInText(View view){
        finish();
        startActivity(new Intent(SignUp.this,MainActivity.class));
    }
}
