package com.azwinckteam.androidmultichoicequiz.Interface;

import com.azwinckteam.androidmultichoicequiz.Model.CurrentQuestion;

public interface IQuestion {
    CurrentQuestion getSelectedAnswer(); // Get selected Answer from user select
    void showCorrectAnswer();   //bold correct answer text
    void disableAnswer();  //Disable all check box
    void resetQuestion(); //reset all function on question
}
