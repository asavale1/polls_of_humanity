package pollsofhumanity.hardikar.com.pollsofhumanity;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Calendar;

import pollsofhumanity.hardikar.com.pollsofhumanity.receiver.ResultsAlarmReceiver;
import pollsofhumanity.hardikar.com.pollsofhumanity.receiver.UpdateAlarmReceiver;
import pollsofhumanity.hardikar.com.pollsofhumanity.server.GetQuestion;
import pollsofhumanity.hardikar.com.pollsofhumanity.server.listener.GetQuestionListener;
import pollsofhumanity.hardikar.com.pollsofhumanity.server.GetResults;
import pollsofhumanity.hardikar.com.pollsofhumanity.server.listener.GetResultsListener;
import pollsofhumanity.hardikar.com.pollsofhumanity.server.PostAnswer;
import pollsofhumanity.hardikar.com.pollsofhumanity.server.listener.PostAnswerListener;
import pollsofhumanity.hardikar.com.pollsofhumanity.server.holder.QuestionHolder;
import pollsofhumanity.hardikar.com.pollsofhumanity.server.holder.ResultsHolder;

public class BaseActivity extends AppCompatActivity {
    private TextView questionText;
    private Button yesButton, noButton;
    private ManageSharedPref manageSharedPref;
    public Dialog resultsDialog;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        manageSharedPref = new ManageSharedPref(getApplicationContext());
        context = this;

        resultsDialog = new Dialog(this);
        resultsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        yesButton = (Button)findViewById(R.id.butt_Yes);
        yesButton.setOnClickListener(yesListener);

        noButton= (Button)findViewById(R.id.butt_No);
        noButton.setOnClickListener(noListener);

        questionText=(TextView) findViewById(R.id.question_Text);

        disableSubmit();

        System.out.println("Check for update: " + manageSharedPref.getUpdated());
        System.out.println("ID: " + manageSharedPref.getId());
        if(manageSharedPref.getId() == -1){
            new GetQuestion(this, gQListener).execute();
        }else{
            questionText.setText(manageSharedPref.getQuestion());
            if(!manageSharedPref.getIsQuestionAnswered()){
                enableSubmit();
            }
        }

        setupUpdateCheck();
        setUpdateAlarm();
        setResultsAlarm();

        int showResults = this.getIntent().getIntExtra("results_ready", -1);
        System.out.println("Show results: "+showResults);
        if(showResults != -1){
            System.out.println("Got results");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        System.out.println("IN NEW INTENT");

    }

    @Override
    protected void onResume(){
        super.onResume();
        System.out.println("IN ON RESUME");


        resultsDialog.setContentView(R.layout.dialog_results);

        resultsDialog.show();

        int questionId = this.getIntent().getIntExtra("results_ready", -1);
        if(questionId != -1){

            new GetResults(questionId, gRListener).execute();
        }


        System.out.println("Show result in resume: " + questionId);
    }

    private void setupUpdateCheck(){
        final Handler updateHandler = new Handler();
        Runnable updateRunnable = new Runnable() {

            @Override
            public void run() {

                updateHandler.postDelayed(this, 30000);
                System.out.println("Checking for updates");
                if(manageSharedPref.getUpdated()){
                    questionText.setText(manageSharedPref.getQuestion());
                    enableSubmit();
                    manageSharedPref.setUpdated(false);
                }
            }
        };

        updateHandler.postDelayed(updateRunnable, 0);
    }

    private void enableSubmit(){

        yesButton.setVisibility(View.VISIBLE);
        noButton.setVisibility(View.VISIBLE);
    }

    private void disableSubmit(){
        yesButton.setVisibility(View.INVISIBLE);
        noButton.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_base, menu);
        return false;
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

    GetQuestionListener gQListener = new GetQuestionListener() {
        @Override
        public void onGetQuestionComplete(QuestionHolder question) {
            questionText.setText(question.getQuestion());

            manageSharedPref.setIsQuestionAnswered(false);
            manageSharedPref.setUpdated(false);
            manageSharedPref.setQuestion(question.getQuestion());
            manageSharedPref.setId(question.getId());
            enableSubmit();

        }
    };

    PostAnswerListener pAListener = new PostAnswerListener() {
        @Override
        public void onPostAnswerComplete() {

            manageSharedPref.setIsQuestionAnswered(true);
            manageSharedPref.setUpdated(false);
            System.out.println("Answered: " + manageSharedPref.getIsQuestionAnswered());
            disableSubmit();
        }
    };

    GetResultsListener gRListener = new GetResultsListener() {
        @Override
        public void onGetResultsComplete(ResultsHolder results) {


            //((TextView) resultsDialog.findViewById(R.id.yes_count)).setText(Integer.toString(results.getYesCount()));
            //((TextView) resultsDialog.findViewById(R.id.no_count)).setText(Integer.toString(results.getNoCount()));
            //((TextView) resultsDialog.findViewById(R.id.total_count)).setText(Integer.toString(results.getTotal()));
        }
    };

    View.OnClickListener yesListener= new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new PostAnswer(manageSharedPref.getId(), "yes", pAListener).execute();
        }
    };
    View.OnClickListener noListener= new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             new PostAnswer(manageSharedPref.getId(), "no", pAListener).execute();
        }
    };


    private void setUpdateAlarm(){
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(BaseActivity.this, UpdateAlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(BaseActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        System.out.println("ALARM SET IN HERE");
        // Set the alarm to start at approximately 2:00 p.m.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 12);

        // With setInexactRepeating(), you have to use one of the AlarmManager interval
        // constants--in this case, AlarmManager.INTERVAL_DAY.
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pi);

    }

    private void setResultsAlarm(){
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(BaseActivity.this, ResultsAlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(BaseActivity.this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        System.out.println("IN RESULTS ALARM");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 57);

        //am.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
    }

    @Override
    public void onBackPressed(){
        finish();
    }

}
