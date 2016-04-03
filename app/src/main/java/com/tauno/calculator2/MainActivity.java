package com.tauno.calculator2;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    private static final String STATE_NUMBERS = "Numbers in memory";
    private static final String STATE_FLAG = "Boolean";
    private static final String STATE_SAVEDVALUE = "LastAnswer";
    private static final String STATE_OPERATOR = "Operator";
    private static final String STATE_CURRENTNUMBER = "CurrentNr";

    private static final String TAG = "MainActivity";
    private static String strDouble = "0";
    private TextView textViewShow;
    private int orient;

    private String[] numbers = {"", ""};
    private char operator = '\u0000';
    private double savedValue = 0;
    private boolean flag = false;// true if there is first number
    private static final int maxLength = 35;


//TODO: mitu nulli saab järjest kirjutada, avades ja C vajutades kuvab 0, UI veidi ümber teha.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        orient = getScreenOrientation();
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCreateCalled");
        }
        setContentView(R.layout.activity_main);
        textViewShow = (TextView) findViewById(R.id.textViewShowNr);
        textViewShow.setText(strDouble);


        if (savedInstanceState != null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Restoring state");
            }

            numbers = savedInstanceState.getStringArray(STATE_NUMBERS);
            operator = savedInstanceState.getChar(STATE_OPERATOR);
            flag = savedInstanceState.getBoolean(STATE_FLAG);
            savedValue = Double.parseDouble(savedInstanceState.getString(STATE_SAVEDVALUE));
            strDouble = savedInstanceState.getString(STATE_CURRENTNUMBER);
            showContent();
        }

    }

    public void btnClicked(View view) {
        Button btn = (Button) view;
        String idAsString = btn.getResources().getResourceName(btn.getId());

        //DEBUG
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Button pressed :" + idAsString);
        }

        if (btn.getText().equals("C")) {
            resetAnswer();
        } else if (btn.getText().equals("=")) {
            calculateAnswer();
        } else if (!operatorCheck(btn)) {
            saveNumber(btn);
        } else if (operatorCheck(btn)) {
            setOperator(btn);
        }


        //DEBUG
        String orientation = "";
        if (orient == 1) {
            orientation = "portrait";
        } else if (orient == 2) {
            orientation = "landscape";
        } else {
            orientation = "ERROR";
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.valueOf(operator));
            Log.d(TAG, numbers[0]);
            Log.d(TAG, numbers[1]);
            Log.d(TAG, orientation);
        }

    }

    public void showContent() {
        setTextSize();
        textViewShow.setText(strDouble);
    }


    public void setOperator(Button button) {
        if(numbers[0] == ""){
            numbers[0] = strDouble;
        }
        //strDouble = "";
        if (checkIfTwoNumbers()) {
            calculateAnswer();
        }
        setOperator(button.getText().toString());
    }


    public void calculateAnswer() {
        if (checkIfTwoNumbers()) {

            // SIIA BROADCAST
            Intent intent = new Intent();
            intent.setAction("com.tauno.MYREQUEST");
            intent.addFlags(intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            intent.putExtra("number1", Double.parseDouble(numbers[0]));
            intent.putExtra("number2", Double.parseDouble(numbers[1]));
            intent.putExtra("operator", operator);
            sendOrderedBroadcast(intent, null, new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    strDouble = getResultData();
                    showContent();
                }
            }, null, Activity.RESULT_OK, null, null);
            numbers[0] = "";
            numbers[1] = "";
        }

    }

    public void resetAnswer() {
        strDouble = "0";
        reset();
        showContent();
    }

    public void saveNumber(Button btn) {

        String nr = btn.getText().toString();
        checkIfInfinity();
        if(operatorSet()){
            numbers[0] = strDouble;
        }

        if(btn.getText().toString().equals("0") && strDouble == "0"){
            return;
        }
        if(btn.getText().toString().equals(".") && strDouble == "0"){
            strDouble = strDouble + btn.getText().toString();
            addNumber(nr);
            showContent();
            return;
        }
        //check if number contains coma
        if (btn.getText().toString().contains(".") && strDouble.contains(".")) {
            return;
            //first number not set flag = false
        } else if (!flag) {
            if(strDouble == "0"){
                strDouble = "";
                strDouble = btn.getText().toString();
                addNumber(nr);
                showContent();

            }else {
                strDouble = strDouble + btn.getText();
                addNumber(nr);
                showContent();
            }
        } else {
            addNumber(nr);
            strDouble = numbers[1];
            showContent();
        }
    }


    public void setTextSize() {
        if (strDouble.length() > 10 && orient == 1) {
            textViewShow.setTextSize(40);
        } else if (strDouble.length() > 10 && orient == 2) {
            textViewShow.setTextSize(60);
        }
    }

    public void checkIfInfinity(){
        if(textViewShow.getText().toString().equals("Math Error")) {
            resetAnswer();
        }else {

        }
    }

    public void addNumber(String number) {
        if (!flag && numbers[0].length() <= maxLength) {
            numbers[0] += number;
        } else if (flag && numbers[1].length() <= maxLength) {
            numbers[1] += number;
        }
    }

    public void setOperator(String op) {
        if (numbers[0] != "") {
            operator = op.charAt(0);
            flag = true;
        }
    }
    public boolean operatorSet(){
        if(operator == '\u0000'){
            return false;
        }
        return true;
    }


    public boolean operatorCheck(Button btn) {
        if (btn.getText().equals("/") || btn.getText().equals("*") || btn.getText().equals("+") ||
                btn.getText().equals("-")) {
            return true;
        } else {
            return false;
        }
    }


    public void reset() {
        savedValue = 0;
        operator = '\u0000';
        numbers = new String[]{"", ""};
        flag = false;
    }

    public boolean checkIfTwoNumbers() {
        if ((!numbers[0].equals("") && !numbers[1].equals("")) && (!numbers[0].equals(".") && !numbers[1].equals("."))) {
            return true;
        }
        return false;
    }

    public String returnSavedValue() {
        if(savedValue == Double.POSITIVE_INFINITY ){
            reset();
            return "Math Error";
        }else {
            return String.valueOf(savedValue);
        }

    }


    // SOURCE http://stackoverflow.com/questions/14955728/getting-orientation-of-android-device
    public int getScreenOrientation() {

        // Query what the orientation currently really is.
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            return 1; // Portrait Mode

        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return 2;   // Landscape mode
        }
        return 0;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        savedInstanceState.putString(STATE_SAVEDVALUE, returnSavedValue());
        savedInstanceState.putChar(STATE_OPERATOR, operator);
        savedInstanceState.putBoolean(STATE_FLAG, flag);
        savedInstanceState.putStringArray(STATE_NUMBERS, numbers);

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Saving strDouble to STATE, value is: " + numbers[0]);
        }
        savedInstanceState.putString(STATE_CURRENTNUMBER, numbers[0]);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onStartCalled");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onResumeCalled");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onPauseCalled");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onStopCalled");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onDestroyCalled");
        }
    }


}
