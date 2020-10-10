package com.azwinckteam.androidmultichoicequiz;



import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.azwinckteam.androidmultichoicequiz.Common.Common;
import com.azwinckteam.androidmultichoicequiz.Interface.IQuestion;
import com.azwinckteam.androidmultichoicequiz.Model.CurrentQuestion;
import com.azwinckteam.androidmultichoicequiz.Model.Question;

import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 */
public class QuestionFragment extends Fragment implements IQuestion {

    TextView txt_question_text;
    CheckBox ckbA, ckbB, ckbC, ckbD;
    FrameLayout layout_image;
    LinearLayout layout_audio;
    ProgressBar progressBar;
    String result1;
    MediaPlayer mediaPlayer;
    Activity context;

    Question question;
    int questionIndex=-1;

    //audio atribut
    Button playBtn,stopBtn;
    SeekBar positionBar;
    SeekBar volumeBar;
    TextView elapsedTimeLabel;
    TextView remainingTimeLabel;
    MediaPlayer mp;
    int totalTime;

    public QuestionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View itemView = inflater.inflate(R.layout.fragment_question, container, false);

        //get question
        questionIndex = getArguments().getInt("index",-1);
        question = Common.questionList.get(questionIndex);

        if(question != null) {
            layout_image = (FrameLayout) itemView.findViewById(R.id.layout_image);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
            layout_audio = (LinearLayout) itemView.findViewById(R.id.layout_audio);

            if(question.isImageQuestion())
            {
                ImageView img_question = (ImageView) itemView.findViewById(R.id.image_question);
                Uri uri = Uri.parse("android.resource://com.azwinckteam.androidmultichoicequiz/mipmap/"+question.getQuestionImage());
                img_question.setImageURI(uri);

                //Bitmap bmImg = BitmapFactory.decodeFile("res//"+"/raw/"+question.getQuestionImage());
                //img_question.setImageBitmap(bmImg);
                progressBar.setVisibility(View.GONE);


                /*if(question.isAudioQuestion()){
                    VideoView videoView = (VideoView) itemView.findViewById((R.id.audio_question));
                    Uri uri1 = Uri.parse("android.resource://com.azwinckteam.androidmultichoicequiz/mipmap/"+question.getQuestionAduio());
                    videoView.setVideoURI(uri1);*/


               /* Picasso.get().load(question.getQuestionImage()).into(img_question, new Callback() {

                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(getContext(), ""+question.getQuestionImage(), Toast.LENGTH_SHORT).show();
                        //Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });*/
            }
            else
                layout_image.setVisibility(View.GONE);

            if(question.isAudioQuestion())
            {
                //Toast.makeText(getContext(), "j"+mp, Toast.LENGTH_SHORT).show();
                //audio atribut
                playBtn = (Button) itemView.findViewById(R.id.playBtn);

                elapsedTimeLabel = (TextView) itemView.findViewById(R.id.elapsedTimeLabel);
                remainingTimeLabel = (TextView) itemView.findViewById(R.id.remainingTimeLabel);

                // Media Player

                String filename = "android.resource://com.azwinckteam.androidmultichoicequiz/raw/"+question.getQuestionAudio();

                mp = new MediaPlayer();

                try { mp.setDataSource(getContext(),Uri.parse(filename)); } catch (Exception e) {}
                try { mp.prepare(); } catch (Exception e) {}

                mp.setLooping(false);
                mp.seekTo(0);
                mp.setVolume(1, 1);  //artinya volume akan disesuaikan dengan pengaturan volume gadget. jika pembaca memberi nilai (0, 0) sama artinya dengan menghilangkan volume suara.
                totalTime = mp.getDuration();

                // Position Bar
                positionBar = (SeekBar) itemView.findViewById(R.id.positionBar);
                positionBar.setMax(totalTime);
                positionBar.setOnSeekBarChangeListener(
                        new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (fromUser) {
                                    mp.seekTo(progress);
                                    positionBar.setProgress(progress);
                                }
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {

                            }
                        }
                );

               // Thread (Update positionBar & timeLabel)
               new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (mp != null) {
                            try {
                                Message msg = new Message();
                                msg.what = mp.getCurrentPosition();
                                handler.sendMessage(msg);
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {}
                        }
                    }
                }).start();

                playBtn.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if(mp!=null)
                        {
                            if (!mp.isPlaying()) {
                                // Stopping
                                mp.start();
                                playBtn.setBackgroundResource(R.drawable.stop);
                            } else {
                                // Playing
                                mp.pause();
                                playBtn.setBackgroundResource(R.drawable.play);
                            }
                        }
                    }
                });
                //return itemView ;

            }
            else
                layout_audio.setVisibility(View.GONE);



            //view
            txt_question_text = (TextView) itemView.findViewById(R.id.txt_question_text);
            txt_question_text.setText(question.getQuestionText());



            ckbA = (CheckBox) itemView.findViewById(R.id.ckbA);
            ckbA.setText(question.getAnswerA());
            ckbA.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b)
                        Common.selected_values.add(ckbA.getText().toString());
                    else
                        Common.selected_values.remove(ckbA.getText().toString());
                }
            });

            ckbB = (CheckBox) itemView.findViewById(R.id.ckbB);
            ckbB.setText(question.getAnswerB());
            ckbB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b)
                        Common.selected_values.add(ckbB.getText().toString());
                    else
                        Common.selected_values.remove(ckbB.getText().toString());
                }
            });

            ckbC = (CheckBox) itemView.findViewById(R.id.ckbC);
            ckbC.setText(question.getAnswerC());
            ckbC.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b)
                        Common.selected_values.add(ckbC.getText().toString());
                    else
                        Common.selected_values.remove(ckbC.getText().toString());
                }
            });

            ckbD = (CheckBox) itemView.findViewById(R.id.ckbD);
            ckbD.setText(question.getAnswerD());
            ckbD.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b)
                        Common.selected_values.add(ckbD.getText().toString());
                    else
                        Common.selected_values.remove(ckbD.getText().toString());
                }
            });

        }

        return itemView;
    }

    @Override
    public CurrentQuestion getSelectedAnswer() {

        //this function will return state of answer
        //rigt, wrong or normal
        CurrentQuestion currentQuestion = new CurrentQuestion(questionIndex,Common.ANSWER_TYPE.NO_ANSWER); //defaut no answer
        StringBuilder result = new StringBuilder();
        if(Common.selected_values.size() >= 2)
        {
            resetQuestion();
            Toast.makeText(getContext(), "only one", Toast.LENGTH_SHORT).show();
            //if multichoice
            //split answer to array
            //Ex: arr[0] = a. ney york
            //Ex: arr[1] = b. paris
            /*Object[] arrayAnswer = Common.selected_values.toArray();
            for (int i=0;i<arrayAnswer.length;i++)
                if(i<arrayAnswer.length-1)
                    result.append(new StringBuilder(((String)arrayAnswer[i]).substring(0,1)).append(",")); //take first letter of answer: Ex : A. New york, we will take letter 'A'
                else
                    result.append(new StringBuilder((String)arrayAnswer[i]).substring(0,1)); //too*/
        }
        else if(Common.selected_values.size() == 1)
        {
            //if only one choice
            Object[] arrayAnswer = Common.selected_values.toArray();
            result1=result.append((String)arrayAnswer[0]).substring(0,1);
        }
        else if(Common.selected_values.size() == 0)
        {
            //if only one choice
            //resetQuestion();
            //Toast.makeText(getContext(), "kembali lagi jika telah mengetahui jawabannya", Toast.LENGTH_SHORT).show();
        }

        if(question != null)
        {
            //compare correct answer with user answer
            if(!TextUtils.isEmpty(result)) {
                if (result1.equals(question.getCorrectAnswer()))
                    currentQuestion.setType(Common.ANSWER_TYPE.RIGHT_ANSWER);
                else
                    currentQuestion.setType(Common.ANSWER_TYPE.WRONG_ANSWER);
            }
            else
                currentQuestion.setType(Common.ANSWER_TYPE.NO_ANSWER);
        }
        else
        {
            Toast.makeText(getContext(), "Cannot get question", Toast.LENGTH_SHORT).show();
            currentQuestion.setType(Common.ANSWER_TYPE.NO_ANSWER);
        }
        Common.selected_values.clear(); //always clear selected_value when compare done
        return currentQuestion;
    }

    @Override
    public void showCorrectAnswer() {

        //bold correct answer
        //pattern A,B
        String[] correctAnswer = question.getCorrectAnswer().split(",");
        for(String answer:correctAnswer) {
            if (answer.equals("A"))
            {
                ckbA.setTypeface(null,Typeface.BOLD);
                ckbA.setTextColor(Color.RED);
            }
            else if (answer.equals("B"))
            {
                ckbB.setTypeface(null,Typeface.BOLD);
                ckbB.setTextColor(Color.RED);
            }
            else if (answer.equals("C"))
            {
                ckbC.setTypeface(null,Typeface.BOLD);
                ckbC.setTextColor(Color.RED);
            }
            else if (answer.equals("D"))
            {
                ckbD.setTypeface(null,Typeface.BOLD);
                ckbD.setTextColor(Color.RED);
            }
        }

    }

    @Override
    public void disableAnswer() {

        ckbA.setEnabled(false);
        ckbB.setEnabled(false);
        ckbC.setEnabled(false);
        ckbD.setEnabled(false);

    }

    @Override
    public void resetQuestion() {

        //enable check box
        ckbA.setEnabled(true);
        ckbB.setEnabled(true);
        ckbC.setEnabled(true);
        ckbD.setEnabled(true);

        //remove all selected
        ckbA.setChecked(false);
        ckbB.setChecked(false);
        ckbC.setChecked(false);
        ckbD.setChecked(false);


        //remove all bold on text
        ckbA.setTypeface(null, Typeface.NORMAL);
        ckbA.setTextColor(Color.BLACK);
        ckbB.setTypeface(null, Typeface.NORMAL);
        ckbB.setTextColor(Color.BLACK);
        ckbC.setTypeface(null, Typeface.NORMAL);
        ckbC.setTextColor(Color.BLACK);
        ckbD.setTypeface(null, Typeface.NORMAL);
        ckbD.setTextColor(Color.BLACK);

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int currentPosition = msg.what;
            // Update positionBar.
            positionBar.setProgress(currentPosition);

            // Update Labels.
            String elapsedTime = createTimeLabel(currentPosition);
            elapsedTimeLabel.setText(elapsedTime);

            String remainingTime = createTimeLabel(totalTime-currentPosition);
            remainingTimeLabel.setText("- " + remainingTime);
        }
    };

    public String createTimeLabel(int time) {
        String timeLabel = "";
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;

        timeLabel = min + ":";
        if (sec < 10) timeLabel += "0";
        timeLabel += sec;

        return timeLabel;
    }

    public void stopMediaPlayer  () {
        getFragmentManager();
        if(mp!=null)
        {
            if (!mp.isPlaying()) {
                // Stopping
                //mp.start();
                //playBtn.setBackgroundResource(R.drawable.stop);
            } else {
                // Playing
                //mp.stop();
                mp.pause();
                playBtn.setBackgroundResource(R.drawable.play);
            }
        }
    }




}
