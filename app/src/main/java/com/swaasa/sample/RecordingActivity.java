package com.swaasa.sample;

import android.Manifest;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.swaasa.swaasaplatform.Assessment;
import com.swaasa.swaasaplatform.CoughRecorder;
import com.swaasa.swaasaplatform.RestCallback;
import com.swaasa.swaasaplatform.model.BaseResponse;
import com.swaasa.swaasaplatform.model.Gender;
import com.swaasa.swaasaplatform.model.PredictionResponse;
import com.swaasa.swaasaplatform.model.Symptoms;
import com.swaasa.swaasaplatform.model.VerifyCoughResponse;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class RecordingActivity extends AppCompatActivity implements View.OnClickListener,EasyPermissions.PermissionCallbacks, RestCallback<BaseResponse>{

    Button start, stop, verifyCough, submit;
    private File audioFilePath;
    String filename;

    TextView textView;


    Chronometer chronometer;

    CoughRecorder coughRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);
        Timber.plant(new Timber.DebugTree());

        audioFilePath = file("audio1.WAV");
        coughRecorder = new CoughRecorder(audioFilePath);

        textView = findViewById(R.id.textView);
        start = findViewById(R.id.start_rec);
        stop = findViewById(R.id.stop_rec);

        verifyCough = findViewById(R.id.verify_cough);
        submit = findViewById(R.id.submit_assessment);

        start.setOnClickListener(this);
        stop.setOnClickListener(this);
        verifyCough.setOnClickListener(this);
        submit.setOnClickListener(this);
        chronometer = findViewById(R.id.timer);


    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.start_rec){
            Timber.i("start rec called");

            checkPermissionAndRecord2();
        }
        if (view.getId() == R.id.stop_rec){
            Timber.i("stop rec called");

            stopRecording(false);
        }

        if(view.getId() == R.id.verify_cough){
            Timber.i("verify cough called");
            submitVerifyCough();
        }

        if(view.getId() == R.id.submit_assessment){
            submitAssessment();
        }
    }

    private void submitAssessment() {
        textView.setText("");
        Assessment assessment = new Assessment("<api-key>", this);

        Symptoms symptoms = new Symptoms();
        symptoms.frequentCough = 1;
        symptoms.coughAtNight = 1;
        symptoms.sputum = 1;
        symptoms.painInChest = 1;
        symptoms.wheezing = 1;
        symptoms.shortnessOfBreath = 1;
        assessment.submitAssessment(audioFilePath, symptoms, 20, Gender.FEMALE);
    }


    private File file(String filename) {
        File folder = new File(this.getCacheDir(), filename);
        try {
            folder.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return folder;
    }

    public void checkPermissionAndRecord2() {

        if(EasyPermissions.hasPermissions(this,
                Manifest.permission.RECORD_AUDIO)){
            startRecording();
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.stop();
            chronometer.start();

        } else {
            EasyPermissions.requestPermissions(
                    this,
                    "Audio Record access is needed to record",
                    100,
                    Manifest.permission.RECORD_AUDIO);

            Timber.i("requesting permission");
        }
    }




    public void startRecording() {
        Timber.i("start recording called");
        coughRecorder.startRecording();
    }

    public void stopRecording(boolean delete) {
        coughRecorder.stopRecording();
        chronometer.stop();
        chronometer.setActivated(false);

    }


    void submitVerifyCough(){
        textView.setText("");
        Assessment assessment = new Assessment("<api-key>", this);
        assessment.verifyCough(audioFilePath);
    }




    @Override
    public <T extends BaseResponse> void onSuccess(T response) {
        if(response instanceof VerifyCoughResponse){
            VerifyCoughResponse verifyCoughResponse = (VerifyCoughResponse) response;
//            textView.setText(String.format("%s %s-%s", response.status, response.data.isValidCough, response.data.message));
            textView.setText(new Gson().toJson(response));
        }

        if(response instanceof PredictionResponse){
            PredictionResponse predictionResponse = (PredictionResponse) response;
            textView.setText(new Gson().toJson(response));        }
    }

    @Override
    public void onFailure(BaseResponse baseResponse) {
        Timber.i(baseResponse.toString());
        textView.setText(new Gson().toJson(baseResponse));
    }



    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if(requestCode == 100){
            startRecording();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }
}