package com.azwinckteam.androidmultichoicequiz;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.azwinckteam.androidmultichoicequiz.Adapter.AnswerSheetAdapter;
import com.azwinckteam.androidmultichoicequiz.Adapter.AnswerSheetHelperAdapter;
import com.azwinckteam.androidmultichoicequiz.Adapter.QuestionFragmentAdapter;
import com.azwinckteam.androidmultichoicequiz.Common.Common;
import com.azwinckteam.androidmultichoicequiz.DBHelper.DBHelper;
import com.azwinckteam.androidmultichoicequiz.Model.CurrentQuestion;
import com.azwinckteam.androidmultichoicequiz.Model.Question;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.gson.Gson;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class QuestionActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int CODE_GET_RESULT = 9999;
    int time_play = Common.TOTAL_TIME;
    boolean isAnswerModeView = false;

    TextView txt_right_answer,txt_timer,txt_wrong_answer;
    RecyclerView answer_sheet_view;
    RecyclerView answer_sheet_helper;
    AnswerSheetAdapter answerSheetAdapter;
    AnswerSheetHelperAdapter answerSheetHelperAdapter;

    ViewPager viewPager;
    TabLayout tabLayout;

    //ctrl+O


    @Override
    protected void onDestroy() {
        if(Common.countDownTimer != null)
            Common.countDownTimer.cancel();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(Common.selectedCategory.getName());
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        //NavigationView navigationView = findViewById(R.id.nav_view);
        /*ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();*/
        //navigationView.setNavigationItemSelectedListener(this);

        //First, we need take question from DB
        takeQuestion();

        if(Common.questionList.size() > 0) {
           //Show TextView right answer and text view timer
            txt_right_answer = (TextView)findViewById(R.id.txt_question_right);
            txt_timer = (TextView)findViewById(R.id.txt_timer);

            txt_timer.setVisibility(View.VISIBLE);
            //txt_right_answer.setVisibility(View.VISIBLE);

            txt_right_answer.setText(new StringBuilder(String.format("%d/%d",Common.right_answer_count,Common.questionList.size())));

            countTimer();


            //View
            answer_sheet_view = (RecyclerView) findViewById(R.id.grid_answer);
            answer_sheet_view.setHasFixedSize(true);
            if (Common.questionList.size() > 4) //if question list have size > 5, we will seperate 2 rows
                answer_sheet_view.setLayoutManager(new GridLayoutManager(this, Common.questionList.size()/5));
            answerSheetAdapter = new AnswerSheetAdapter(this, Common.answerSheetList);
            answer_sheet_view.setAdapter(answerSheetAdapter);

            //answerSheetHelperAdapter = new AnswerSheetHelperAdapter(this,Common.answerSheetList);
            //answer_sheet_helper.setAdapter(answerSheetAdapter);

            viewPager = (ViewPager)findViewById(R.id.view_pager);
            tabLayout = (TabLayout)findViewById(R.id.sliding_tabs);

            genFragmentList();

            QuestionFragmentAdapter questionFragmentAdapter = new QuestionFragmentAdapter(getSupportFragmentManager(),
                    this,
                    Common.fragmentsList);
            viewPager.setAdapter(questionFragmentAdapter);
            viewPager.setOffscreenPageLimit(Common.questionList.size()); //fixed viewpager size
            tabLayout.setupWithViewPager(viewPager);

            //Event
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                int SCROLLING_RIGHT = 0;
                int SCROLLING_LEFT = 1;
                int SCROLLING_UNDERTERMINED = 2;

                int currentScrollDirection = 2;

                private void setScrollingDirection(float positionOffset)
                {
                    if((1-positionOffset) >= 0.5)
                        this.currentScrollDirection=SCROLLING_RIGHT;
                    else if((1-positionOffset) <= 0.5 )
                        this.currentScrollDirection=SCROLLING_LEFT;
                }

                private boolean isScrollDirectionUndetermined(){
                    return currentScrollDirection == SCROLLING_UNDERTERMINED;
                }

                public boolean isScrollingRight(){
                    return currentScrollDirection == SCROLLING_RIGHT;
                }

                private boolean isScrollingLeft(){
                    return currentScrollDirection == SCROLLING_LEFT;
                }

                @Override
                public void onPageScrolled(int i, float v, int il) {
                    if(isScrollDirectionUndetermined())
                        setScrollingDirection(v);
                }

                @Override
                public void onPageSelected(int i) {
                    QuestionFragment questionFragment;
                    int position = 0;
                    if(i>0)
                    {
                        if (isScrollingRight()) {
                            //if user scroll to right , get previous fragment to calculate result
                            questionFragment = Common.fragmentsList.get(i-1);
                            position = i - 1;
                        }
                        else if (isScrollingLeft())
                        {
                            //if user scroll to left , get next fragment to calculate result
                            questionFragment = Common.fragmentsList.get(i+1);
                            position = i + 1;
                        }
                        else {
                            questionFragment = Common.fragmentsList.get(position);
                        }
                    }
                    else{
                        questionFragment = Common.fragmentsList.get(0);
                        position = 0;
                    }

                    //menghentikan audio ketika user scroll right or left
                    if(i>0)
                    {
                        if (isScrollingRight() || isScrollingLeft()) {
                            questionFragment.stopMediaPlayer();
                        }
                    }

                    //optimize, only question have no answer  just active this code
                   if(Common.answerSheetList.get(position).getType()== Common.ANSWER_TYPE.NO_ANSWER)
                    {
                        //if you want to show correct answer, just call function here
                        CurrentQuestion question_state = questionFragment.getSelectedAnswer();
                        Common.answerSheetList.set(position,question_state); //set question answer for answersheet
                        answerSheetAdapter.notifyDataSetChanged();  // change color in answer sheet

                        CountCorrectAnswer();
                        txt_right_answer.setText(new StringBuilder(String.format("%d",Common.right_answer_count))
                                .append("/")
                                .append(String.format("%d",Common.questionList.size())).toString());
                        txt_wrong_answer.setText(String.valueOf(Common.wrong_answer_count));

                        //jika tidak ada napilih
                        if(question_state.getType() != Common.ANSWER_TYPE.NO_ANSWER)
                        {
                            questionFragment.showCorrectAnswer();
                            questionFragment.disableAnswer();
                        }
                    }

                }

                @Override
                public void onPageScrollStateChanged(int i) {
                    if(i==ViewPager.SCROLL_STATE_IDLE)
                        this.currentScrollDirection = SCROLLING_UNDERTERMINED;
                }
            });
        }

    }

    private void finishGame() {
        timerStop();
        int position = viewPager.getCurrentItem();

        QuestionFragment questionFragment = Common.fragmentsList.get(position);
        questionFragment.stopMediaPlayer();

        CurrentQuestion question_state = questionFragment.getSelectedAnswer();
        Common.answerSheetList.set(position,question_state); //set question answer for answersheet
        answerSheetAdapter.notifyDataSetChanged();  // change color in answer sheet
        //answerSheetHelperAdapter.notifyDataSetChanged();

        CountCorrectAnswer();
        txt_right_answer.setText(new StringBuilder(String.format("%d",Common.right_answer_count))
                .append("/")
                .append(String.format("%d",Common.questionList.size())).toString());
        txt_wrong_answer.setText(String.valueOf(Common.wrong_answer_count));

        if(question_state.getType() != Common.ANSWER_TYPE.NO_ANSWER)
        {
            questionFragment.showCorrectAnswer();
            questionFragment.disableAnswer();
        }

        //we will navigate no new result activity here
        Intent intent = new Intent(QuestionActivity.this,ResultActivity.class);
        Common.timer = Common.TOTAL_TIME-time_play;
        Common.no_answer_count = Common.questionList.size()-(Common.wrong_answer_count+Common.right_answer_count);
        Common.data_question = new StringBuilder(new Gson().toJson(Common.answerSheetList));

        startActivityForResult(intent, CODE_GET_RESULT);
    }

    private void finishG() {
        int position = viewPager.getCurrentItem();

        QuestionFragment questionFragment = Common.fragmentsList.get(position);
        CurrentQuestion question_state = questionFragment.getSelectedAnswer();

        //we will navigate no new result activity here
        Intent intent = new Intent(QuestionActivity.this,ResultActivity.class);
        Common.timer = Common.TOTAL_TIME-time_play;
        Common.no_answer_count = Common.questionList.size()-(Common.wrong_answer_count+Common.right_answer_count);
        Common.data_question = new StringBuilder(new Gson().toJson(Common.answerSheetList));

        startActivityForResult(intent, CODE_GET_RESULT);
    }

    private void CountCorrectAnswer() {
        //Reset variabel
        Common.right_answer_count = Common.wrong_answer_count = 0;
        for(CurrentQuestion item:Common.answerSheetList)
            if(item.getType() == Common.ANSWER_TYPE.RIGHT_ANSWER)
            Common.right_answer_count++;
            else if(item.getType() == Common.ANSWER_TYPE.WRONG_ANSWER)
                Common.wrong_answer_count++;
    }

    private void genFragmentList() {
            Common.fragmentsList.clear();
            for(int i=0;i<Common.questionList.size();i++)
            {
                Bundle bundle = new Bundle();
                bundle.putInt("index",i);
                QuestionFragment fragment = new QuestionFragment();
                fragment.setArguments(bundle);
                Common.fragmentsList.add(fragment);
            }
    }

    private void countTimer()   {

        if(Common.countDownTimer == null) {

            if(Common.selectedCategory.getId() == 0) {

                Common.countDownTimer = new CountDownTimer((Common.TOTAL_TIME)*75,1000) {
                    @Override
                    public void onTick(long l) {
                        txt_timer.setText(String.format("%02d:%02d",
                                TimeUnit.MILLISECONDS.toMinutes(l),
                                TimeUnit.MILLISECONDS.toSeconds(l) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(l))));
                        time_play-=1000;
                    }

                    @Override
                    public void onFinish() {
                        finishGame();
                    }
                }.start();
            }

            if(Common.selectedCategory.getId() == 1) {

                Common.countDownTimer = new CountDownTimer((Common.TOTAL_TIME)*45,1000) {
                    @Override
                    public void onTick(long l) {
                        txt_timer.setText(String.format("%02d:%02d",
                                TimeUnit.MILLISECONDS.toMinutes(l),
                                TimeUnit.MILLISECONDS.toSeconds(l) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(l))));
                        time_play-=1000;
                    }

                    @Override
                    public void onFinish() {
                        finishGame();
                    }
                }.start();
            }

        }
        else
        {
            Common.countDownTimer.cancel();

            if(Common.selectedCategory.getId() == 0) {

                Common.countDownTimer = new CountDownTimer((Common.TOTAL_TIME)*75,1000) {
                    @Override
                    public void onTick(long l) {
                        txt_timer.setText(String.format("%02d:%02d",
                                TimeUnit.MILLISECONDS.toMinutes(l),
                                TimeUnit.MILLISECONDS.toSeconds(l) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(l))));
                        time_play-=1000;
                    }

                    @Override
                    public void onFinish() {
                        finishGame();
                    }
                }.start();
            }

            if(Common.selectedCategory.getId() == 1) {

                Common.countDownTimer = new CountDownTimer((Common.TOTAL_TIME)*45,1000) {
                    @Override
                    public void onTick(long l) {
                        txt_timer.setText(String.format("%02d:%02d",
                                TimeUnit.MILLISECONDS.toMinutes(l),
                                TimeUnit.MILLISECONDS.toSeconds(l) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(l))));
                        time_play-=1000;
                    }

                    @Override
                    public void onFinish() {
                        finishGame();
                    }
                }.start();
            }

        }
    }

    private void takeQuestion() {
        Common.questionList = DBHelper.getInstance(this).getQuestionByCategory(Common.selectedCategory.getId());
        if(Common.questionList.size() == 0)
        {
            //if no quetion
            new MaterialStyledDialog.Builder(this)
                    .setTitle("Opps !")
                    .setIcon(R.drawable.ic_sentiment_very_dissatisfied_black_24dp)
                    .setDescription("we don't have any question in this"+ Common.selectedCategory.getName()+" category")
                    .setPositiveText("OK")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                            finish();
                        }
                    }).show();
        }
        else
        {
            if(Common.answerSheetList.size() > 0)
                Common.answerSheetList.clear();
                //gen answerSheet item from question
                //30 question = 30 answer sheet item
                //1 quetion = 1 answer sheet item
                for(int i=0;i<Common.questionList.size();i++)
                {
                    //because we need take index of questions in list, so we will use for i
                    Common.answerSheetList.add(new CurrentQuestion(i,Common.ANSWER_TYPE.NO_ANSWER)); //default all answer is no answer
                }
        }
    }


    @Override
    public void onBackPressed() {

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.menu_wrong_answer);
        ConstraintLayout constraintLayout = (ConstraintLayout)item.getActionView();
        txt_wrong_answer = (TextView)constraintLayout.findViewById(R.id.txt_wrong_answer);
        txt_wrong_answer.setText(String.valueOf(0));

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.question, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection simplifiableIfStatement
        if(id == R.id.menu_finish_game){
            if(!isAnswerModeView) {
                new MaterialStyledDialog.Builder(this)
                        .setTitle("Finish ?")
                        .setIcon(R.drawable.ic_mood_black_24dp)
                        .setDescription("do you realy want to finish ?")
                        .setNegativeText("No")
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveText("yes")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                                finishGame();
                            }
                        }).show();
                Toast.makeText(getApplicationContext(), "finsh"+isAnswerModeView, Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(getApplicationContext(), "yey", Toast.LENGTH_LONG).show();
                finishG();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_tools) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int position = viewPager.getCurrentItem();
        QuestionFragment questionFragment = Common.fragmentsList.get(position);
        CurrentQuestion question_state = questionFragment.getSelectedAnswer();


        if(requestCode == CODE_GET_RESULT) {
            if(resultCode == Activity.RESULT_OK)
                {
                    String action = data.getStringExtra("action");

                    if(action == null || TextUtils.isEmpty(action))
                    {
                        int questionNum = data.getIntExtra(Common.KEY_BACK_FROM_RESULT,-1);
                        viewPager.setCurrentItem(questionNum);

                        isAnswerModeView=true;
                        Common.countDownTimer.cancel();

                        txt_wrong_answer.setVisibility(View.GONE);
                        txt_right_answer.setVisibility(View.GONE);
                        txt_timer.setVisibility(View.GONE);

                        questionFragment.disableAnswer();



                    }else
                        {
                            if(action.equals("viewquizanswer"))
                            {
                                if(question_state.getType() == Common.ANSWER_TYPE.NO_ANSWER)
                                {
                                    questionFragment.showCorrectAnswer();
                                    questionFragment.disableAnswer();
                                }

                                Toast.makeText(getApplicationContext(), "VIEW QUIZ ANSWER", Toast.LENGTH_LONG).show();
                                viewPager.setCurrentItem(0);

                                isAnswerModeView=true;
                                Common.countDownTimer.cancel();

                                txt_wrong_answer.setVisibility(View.GONE);
                                txt_right_answer.setVisibility(View.VISIBLE);
                                txt_timer.setVisibility(View.GONE);


                                for(int i=0;i<Common.fragmentsList.size();i++)
                                {
                                    Common.fragmentsList.get(i).showCorrectAnswer();
                                    Common.fragmentsList.get(i).disableAnswer();
                                }
                            }
                            /*else if(action.equals("doitgain")) {

                                Toast.makeText(getApplicationContext(), "doitgain", Toast.LENGTH_LONG).show();
                                viewPager.setCurrentItem(0);
                                isAnswerModeView=false;
                                countTimer();

                                txt_wrong_answer.setVisibility(View.VISIBLE);
                                txt_right_answer.setVisibility(View.VISIBLE);
                                txt_timer.setVisibility(View.VISIBLE);


                                for(CurrentQuestion item:Common.answerSheetList)
                                    item.setType(Common.ANSWER_TYPE.NO_ANSWER); //reset all question
                                answerSheetAdapter.notifyDataSetChanged();
                                //answerSheetHelperAdapter.notifyDataSetChanged();

                                for(int i=1;i<Common.fragmentsList.size();i++)
                                {
                                    Common.fragmentsList.get(i).resetQuestion();
                                }

                            }*/
                        }

                }
            }
    }


    private void timerStop(){
        Common.countDownTimer.cancel();
    }




}
