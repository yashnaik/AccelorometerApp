/*
*Created by Yash Naik
* Course Number ECE 573
* Homework2 - Accelerometer
*This file creates and App in android which senses Accelerometer values for user defined interval and creates a txt file or m-file
 * and store it on an android device.
* It also provides a way to send that file to an someone via e-mail .
*/
package com.example.yash.acclmtrapp;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yash.acclmtrapp.Constants.java;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import static com.example.yash.acclmtrapp.Constants.java.result_file;
import static com.example.yash.acclmtrapp.Constants.java.sensor_val;


public class MainActivity extends ActionBarActivity implements SensorEventListener,MenuItem.OnMenuItemClickListener, PopupMenu.OnMenuItemClickListener {
    Sensor accelerometer;
    SensorManager sm;
    TextView text;
    Button Save;
    Button mail;
    EditText input;
    boolean isSDavail=true;                            //Check if external SD card is available and writable
    boolean isSDWritable=false;
    final int max_time=30;                             //Max recordable time
    static boolean time_up=false;
    static int time;                                  //User defined time to record
    static int create_file=0;
    //static String  sensor_val="null";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, accelerometer, 50000);               //The sensor senses every 50ms ie at 20hz
        //text = (TextView) findViewById(R.id.acc);
        Save = (Button) findViewById(R.id.savefile);                   //Button to start sensing and save file
        mail=(Button) findViewById(R.id.button);                       //Button to open an email client
        input=(EditText)findViewById(R.id.EditText);                   //EditText to take number of seconds user wants the app to calculate sensor values


        checkSD();                                                      //function to check is SD card working

        mail.setOnClickListener(new View.OnClickListener() {            //Listener object for Email button ..On click of this button email client is opened
            @Override
            public void onClick(View v) {
                   send_mail();                                        //Function to open email client

            }
        });
        Save.setOnClickListener(new View.OnClickListener() {          //Listener to check for command to start listening

            @Override
            public void onClick(View v) {
                if(create_file!=0)                                    //Checks if user has selected appropriate file to be created
                {String str=input.getText().toString();
                try{
                time = Integer.parseInt(str);                        // get the input time for which user wants to sense
                    if(time<=max_time){
                write_file(create_file);}                            //Call function to save data and write to a file
                else{
                        Toast.makeText(getBaseContext(),"Max recordable time is 30 seconds",Toast.LENGTH_SHORT).show();
                }                                                    //User input should be less than 30 secs
                }
                catch (ActivityNotFoundException e) {
                    e.printStackTrace();

                }}
                else{
                    Toast.makeText(getBaseContext(),"Click Start to choose the type of file you wish to create",Toast.LENGTH_SHORT).show();
                }

            }
        });



    }
    /******************Function to send email*///////////////////////////////////////////
    protected void send_mail(){


        File att=new File(java.result_file); //File to be attached to the email

        String[] TO = {"yashnaeek@gmail.com"};  //Email address of the recipient

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");

        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);

        emailIntent.putExtra(Intent.EXTRA_SUBJECT,"Your subject");                    //Add subject ,email body and attachment to the email Draft
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Email message goes here");
        emailIntent.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(att));



        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));                     //Open an email client installed in the device
            Toast.makeText(getBaseContext(), "Attach Files from:"+java.Dir_path, Toast.LENGTH_SHORT).show();
            Toast.makeText(MainActivity.this,"Email client has opened",Toast.LENGTH_SHORT).show();
            finish();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this,
                    "There is no email client installed.", Toast.LENGTH_SHORT).show();           //Notify user if no email client is installed

        }


    }
   /***************Function to check availability of SdCard*******************/
    public void checkSD(){
        String state =Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state))                  //Check if SD card is available and writable
        {
            isSDavail=true;
            isSDWritable=true;
            Toast.makeText(getBaseContext(),"SD card detected and writable",Toast.LENGTH_SHORT).show();
        }
        else if  (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))       //Else  Check if SD card is readable
        {
            isSDavail=true;
            Toast.makeText(getBaseContext(),"SD card detected but not writable",Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getBaseContext(),"SD card not detected",Toast.LENGTH_SHORT).show();      //notify user about the absence of SD card
        }



    }
/***********Function to write data to a file*///////////////////////////////////////////////
     public void write_file(int file_type){                   //Accepts a parameter which is actually the time of file user wants to be created

           try {
               String ECE573 = "ECE573";
               //FileOutputStream file = openFileOutput("test.txt",MODE_APPEND);
               File sdcard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);// Get the path of SD card
               File Dir = new File(sdcard.getAbsolutePath(), ECE573);                                      //Create a new directory on that path
               if (!Dir.exists())
               {
                   Dir.mkdirs();


               }
               java.Dir_path=Dir.toString();
               Toast.makeText(getBaseContext(), "Files to be saved at:"+java.Dir_path, Toast.LENGTH_SHORT).show();
               String f_type=null;
                if(file_type==2)                                            //Create an output file as required by the user.
                {
                     f_type="txt";
                }
               else
                {
                  f_type="m";
                }
               int num=1;
               File file = new File(Dir, "yashnaik_hw02_0"+num+"."+f_type);

               for(num=1;num>0;num++)                         //Check is the file with the generated name exists
               {
               file = new File(Dir, "yashnaik_hw02_0"+num+"."+f_type);
               if(!file.exists())
                 {
                   result_file=file.toString();
                   break;
                 }
               }

               FileOutputStream fou = new FileOutputStream(file);               //Define bufferedWriter object to write to the file
               BufferedWriter out= null;
               try {
                   out = new BufferedWriter(new FileWriter(file,true));
               } catch (IOException e) {
                   e.printStackTrace();
               }
               //Start generating the output file from here

               String currentDateTimeString ="% Generated on " + DateFormat.getDateTimeInstance().format(new Date())+"\n % Generated by Yash Naik ECE573 \n";

               try {
                   out.write(currentDateTimeString);              //Create an internal timestamp


               } catch (IOException e) {
                   e.printStackTrace();
               }




           if(file_type==2){
/*****************************create text file*///////////////////////////////////////////////////////////

               for(int t=0;t<time*1000;t=t+50) {                // For loop is defined to time*1000 times which is equivalent to miliseconds value of the user defined running time

                   final Handler handler = new Handler();
                   final BufferedWriter finalOut = out;         //Create a Handler and ask it to fire every 50ms which is equal to 20hz
                   handler.postDelayed(new Runnable() {
                       @Override
                       public void run() {

                           try {
                               finalOut.write(sensor_val);  //Print out the sensor values every 50ms
                               finalOut.flush();

                           } catch (IOException e) {
                               e.printStackTrace();

                           }
                       }
                   }, (t + 1));

               }

                   Toast.makeText(getBaseContext(), "Collecting data", Toast.LENGTH_SHORT).show();                //Notify the user when data collection is in process

               final Handler save =new Handler();      //Fire a Handler to notify the user that the file is saved
               save.postDelayed(new Runnable() {
                   @Override
                   public void run() {
                       Toast.makeText(getBaseContext(), "Saved as :"+ result_file, Toast.LENGTH_SHORT).show();
                   }
               },time*1000);}
/*******************************Create m file*///////////////////////////////////////
               else
           {

               try {
                   out.write("%The script extracts each of the three numbers from every element in Array A \n % into 3 separate arrays and plot them agains time \n");
                   out.write("A = [");               //Start creating the m-file define an Array


               } catch (IOException e) {
                   e.printStackTrace();
               }

               for(int t=0;t<=time*1000;t=t+50){                //Use the same idea as the one used to create txt file and get input every 50ms
                   final Handler mhandler = new Handler();
                   final BufferedWriter finalO=out;
                   final int t_last=t;
                   mhandler.postDelayed(new Runnable() {
                       @Override
                       public void run() {
                           try {
                               if(t_last==time*1000-50)
                               {finalO.write(java.sensor_mval+"];\n");}    //For the last sensor values "]" indicates end of array in matlab
                               else if(t_last<time*1000-50)
                               {
                                   finalO.write(java.sensor_mval+";");      //Output every sensor values to the m-file
                               }
                               else
                               {
                                   finalO.write("\n");try {                //Once the array is formed extra three columns from it
                                   finalO.write("x=A(:,1);\n");            //indicating the x,y,z axis values of the sensor
                                   finalO.write("y=A(:,2);\n");
                                   finalO.write("z=A(:,3); \n");
                                   finalO.write("l=length(x); \n");         //Get their lengths to create an appropriate time-axis

                                   finalO.write("time=50:50:l*50; \n");      //Generate time array with step of 50ms indicating sensor values every 50ms

                                   finalO.write("plot(time,x,'r',time,y,'y',time,z,'g')\n"); //plot it in matlab
                                   finalO.write("xlabel('time in milliseconds');\n");
                                   finalO.write("ylabel('Acceleration');\n");
                               } catch (IOException e) {
                                   e.printStackTrace();
                               }
                               }
                           } catch (IOException e) {
                               e.printStackTrace();
                           }
                           try {
                               finalO.flush();
                           } catch (IOException e) {
                               e.printStackTrace();
                           }


                       }
                   },(t+1));
               }
               Toast.makeText(getBaseContext(), "Collecting data", Toast.LENGTH_SHORT).show();
               final Handler savem = new Handler();
              final BufferedWriter Fout=out;
               savem.postDelayed(new Runnable() {
                   @Override
                   public void run() {
                       Toast.makeText(getBaseContext(), "Saved as :"+ result_file, Toast.LENGTH_SHORT).show();  //notify the user when file has been created ans saved

                   }
               },time*1000);

           }
/*************************End of m file*****************************////////////////////////////
         } catch (FileNotFoundException e) {

             Toast.makeText(getBaseContext(),"Error",Toast.LENGTH_SHORT).show();
             e.printStackTrace();
         }



     }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        sensor_val= " "+ event.values[0] + "      " + event.values[1] + "      " + event.values[2]+"\n";//Sensor values as string for text file
        java.sensor_mval=" "+ event.values[0] + "  " + event.values[1] + "  " + event.values[2];    //Sensor values as string for m-file



    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void FilePopUp(View v)          //Creating a small pop-up mean when user click the start button ,indicating which files needs to be created
    {
        Toast.makeText(getBaseContext(),"Choose the file you wish to generate", Toast.LENGTH_SHORT).show();
        PopupMenu mypop = new PopupMenu(this,v);
        mypop.setOnMenuItemClickListener(MainActivity.this);
        MenuInflater menuinf = mypop.getMenuInflater();
        menuinf.inflate(R.menu.popup, mypop.getMenu());         //get pop-up object to run the appropriate menu
        mypop.show();
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) { // This function sets the value of create_file variable to 1 or 2 depending upon what
                                                    // type of file the user wants to be generate.
        switch (item.getItemId()) {
            case R.id.m_file:
                create_file=1;
                Toast.makeText(getBaseContext(), "M-file", Toast.LENGTH_SHORT).show();          //Notify the user of his selection
                return true;

            case R.id.txt_file:
                create_file=2;
                Toast.makeText(getBaseContext(), "txt-file", Toast.LENGTH_SHORT).show();         //Notify the user of his selection
                return true;
            default:
                break;

        }
        return false;
    }
}
