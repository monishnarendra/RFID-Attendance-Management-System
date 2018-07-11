package com.example.asus.ams_rfid_mnmg;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

public class MainProfile extends AppCompatActivity {

    private String ii,oo,dd,s = " ";
    private int HoursWorked = 0;
    String date = new SimpleDateFormat("d-M-yyyy", Locale.getDefault()).format(new Date());
    ArrayList<String> Date = new ArrayList<String>();
    ArrayList<Integer> InTime = new ArrayList<Integer>();
    ArrayList<Integer> OutTime = new ArrayList<Integer>();

    TextView tv_UserName,tv_Email,tv_Phone,tv_USN,tv_RFID,tv_HW,tv_TD;
    String Email,url,Name,PhoneNo,RFID,USN;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    RelativeLayout relativeLayout,relativeLayout_1;
    Button button;

    int color[] = {
            Color.rgb(255,128,255),
            Color.rgb(0,153,255),
            Color.rgb(102,255,153),
            Color.rgb(255,102,102),
            Color.rgb(255,255,77),
            Color.rgb(163,102,255)
    };

    Random random = new Random();

        /* Pink         255 128 255
           Blue         0 153 255
           Green        102 255 153
           red          255 102 102
           Yellow       255 255 77
           Purple       163 102 255 */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_profile);

        tv_Email = (TextView) findViewById(R.id.textView8);
        tv_UserName = (TextView) findViewById(R.id.textView9);
        tv_Phone = (TextView) findViewById(R.id.textView17);
        tv_USN = (TextView) findViewById(R.id.textView16);
        tv_RFID = (TextView) findViewById(R.id.textView15);
        tv_HW = (TextView) findViewById(R.id.HW);
        tv_TD = (TextView) findViewById(R.id.TD);

        //imageView = (ImageView) findViewById(R.id.imageView3) ;
        //progressBar = (ProgressBar) findViewById(R.id.progressBar5);

        //button = (Button) findViewById(R.id.button4);*/

        Typeface myFont1 = Typeface.createFromAsset(getAssets(),"fonts/Android_Insomnia_Regular.ttf");  // For Headings
        //Typeface myFont2 = Typeface.createFromAsset(getAssets(),"fonts/Pacifico.ttf");                     // Signature
        //Typeface myFont3 = Typeface.createFromAsset(getAssets(),"fonts/GrandHotel-Regular.otf");

        tv_Email.setTypeface(myFont1);
        tv_UserName.setTypeface(myFont1);
        tv_Phone.setTypeface(myFont1);
        tv_USN.setTypeface(myFont1);
        tv_RFID.setTypeface(myFont1);

        Email = getIntent().getStringExtra("Email_Id");
        RFID = getIntent().getStringExtra("RFID");
        Name = getIntent().getStringExtra("Name");
        PhoneNo = getIntent().getStringExtra("PhoneNo");
        USN = getIntent().getStringExtra("USN");
/*
        Toast.makeText(getApplicationContext(),"Email: " + Email,Toast.LENGTH_LONG).show();
        Toast.makeText(MainProfile.this, "RFID : " + RFID, Toast.LENGTH_SHORT).show();
        Toast.makeText(MainProfile.this, "Name : " + Name, Toast.LENGTH_SHORT).show();
        Toast.makeText(MainProfile.this, "PhoneNo : " + PhoneNo, Toast.LENGTH_SHORT).show();
        Toast.makeText(MainProfile.this, "USN : " + USN, Toast.LENGTH_SHORT).show();
*/
        tv_Email.setText(Email);
        tv_RFID.setText(RFID);
        tv_UserName.setText(Name);
        tv_Phone.setText(PhoneNo);
        tv_USN.setText(USN);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 5s = 5000ms
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference(RFID);
                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Gson gson = new Gson();
                        final String s1 = gson.toJson(dataSnapshot.getValue());

                        String dt = "1-4-2018";  // Start date
                        SimpleDateFormat sdf = new SimpleDateFormat("d-M-yyyy");
                        Calendar c = Calendar.getInstance();
                        int i = 0,TotalDays = 0;

                        while (!Objects.equals(dt, date)) {
                            try {
                                c.setTime(sdf.parse(dt));

                                c.add(Calendar.DATE, 1);  // number of days to add
                                dt = sdf.format(c.getTime());
                                TotalDays++;
                                JSONObject jsonObject = new JSONObject(s1);

                                JSONObject object = jsonObject.getJSONObject(dt);
                                dd = object.getString("Date");
                                ii = object.getString("Arrival Time");
                                oo = object.getString("Departed Time");
                                if(dd != null || ii != null || oo != null) {
                                    Date.add(dd);
                                    InTime.add(Integer.parseInt(ii));
                                    OutTime.add(Integer.parseInt(oo));
                                    i++;
                                }

                            } catch(JSONException | ParseException e){
                                e.printStackTrace();
                            }
                        }

                        for(i = 0;i < Date.size(); i++){
                            HoursWorked = HoursWorked + (OutTime.get(i) - InTime.get(i));
                        }
                        Toast.makeText(MainProfile.this, "HoursWorked: " + HoursWorked, Toast.LENGTH_SHORT).show();
                        Toast.makeText(MainProfile.this, "TotalDays: " + TotalDays, Toast.LENGTH_SHORT).show();
                        s = "" +  HoursWorked;
                        tv_HW.setText(s);
                        s = " ";
                        s = s + TotalDays;
                        tv_TD.setText(s);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(MainProfile.this, "Failed To Retrieve Data", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, 7000);
    }

    public static String EncodeString(String string) {
        return string.replace(".", ",");
    }

    public static String DecodeString(String string) {
        return string.replace(",", ".");
    }
}
