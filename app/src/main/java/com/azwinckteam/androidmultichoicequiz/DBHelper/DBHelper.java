package com.azwinckteam.androidmultichoicequiz.DBHelper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.azwinckteam.androidmultichoicequiz.Model.Category;
import com.azwinckteam.androidmultichoicequiz.Model.Question;
import com.azwinckteam.androidmultichoicequiz.QuestionActivity;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteAssetHelper {

    private static final String DB_Name = "EDMTQuiz2019.db";
    private static final int DB_Ver = 1;

    private static DBHelper instance;

    public static synchronized DBHelper getInstance(Context context)
    {
        if(instance == null)
            instance = new DBHelper(context);
        return instance;
    }

    public DBHelper(Context context) {
        super(context, DB_Name, null, DB_Ver);
    }

    /* Get All Categories from DB*/

    public List<Category> getAllCategories()
    {
        SQLiteDatabase db = instance.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM Category;",null);
        List<Category> categories = new ArrayList<>();
        if(cursor.moveToFirst())
        {
            while (!cursor.isAfterLast())
            {
                Category category = new Category(cursor.getInt(cursor.getColumnIndex("ID")),
                        cursor.getString(cursor.getColumnIndex("Name")),
                        cursor.getString(cursor.getColumnIndex("Image")));
                categories.add(category);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();

        return  categories;
    }

    /*
    GET 30 QUESTION FROM DB BY CATEGORY
     */
    public List<Question> getQuestionByCategory(int category)
    {
        SQLiteDatabase db = instance.getWritableDatabase();

        Cursor cursor = db.rawQuery(String.format("SELECT * FROM Question WHERE CategoryID = %d ",category),null);
        List<Question> questions = new ArrayList<>();
        if(cursor.moveToFirst())
        {
            while (!cursor.isAfterLast())
            {
                Question question = new Question(cursor.getInt(cursor.getColumnIndex("ID")),
                        cursor.getString(cursor.getColumnIndex("QuestionText")),
                        cursor.getString(cursor.getColumnIndex("QuestionImage")),
                        cursor.getString(cursor.getColumnIndex("QuestionAudio")),
                        cursor.getString(cursor.getColumnIndex("AnswerA")),
                        cursor.getString(cursor.getColumnIndex("AnswerB")),
                        cursor.getString(cursor.getColumnIndex("AnswerC")),
                        cursor.getString(cursor.getColumnIndex("AnswerD")),
                        cursor.getString(cursor.getColumnIndex("CorrectAnswer")),
                        cursor.getInt(cursor.getColumnIndex("IsImageQuestion"))==0?Boolean.FALSE:Boolean.TRUE,
                        cursor.getInt(cursor.getColumnIndex("IsAudioQuestion"))==0?Boolean.FALSE:Boolean.TRUE,
                        cursor.getInt(cursor.getColumnIndex("CategoryID")));
                questions.add(question);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();

        return  questions;
    }
}
