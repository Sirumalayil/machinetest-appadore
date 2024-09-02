package com.example.machintest_appadore.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Created by Siru malayil on 01-09-2024.
 */

class PreferenceUtil(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)



    companion object {
        private const val PREFS_NAME = "app_prefs"
        private const val KEY_END_TIME = "key_question_end_time"
        private const val KEY_QUESTION_NO = "key_some_int"
        private const val KEY_USER_STATE = "key_user_state"
    }

    fun feedUserData(questionNo: Int,endTime: Long, userStatus:Int) {
        sharedPreferences.edit().apply {
            putInt(KEY_QUESTION_NO,questionNo)
            putInt(KEY_USER_STATE,userStatus)
            putLong(KEY_END_TIME,endTime).apply()
        }.apply()
    }

    fun fetchEndTime(): Long = sharedPreferences.getLong(KEY_END_TIME,0L)

    fun fetchQuestionNo(): Int = sharedPreferences.getInt(KEY_QUESTION_NO,-1)

    fun fetchUserState(): Int = sharedPreferences.getInt(KEY_USER_STATE,-1)

    fun clearEndTime() = sharedPreferences.edit().remove(KEY_END_TIME).apply()

    fun clearPreferences() = sharedPreferences.edit().clear().apply()

    fun saveHashMapToPreferences(key: String, map: HashMap<Int, String>?) {
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val jsonString = gson.toJson(map)
        editor.putString(key, jsonString)
        editor.apply()
    }

    fun getHashMapFromPreferences(key: String): HashMap<Int, String> {
        val jsonString = sharedPreferences.getString(key, null) ?: return hashMapOf()
        val gson = Gson()
        val type = object : TypeToken<HashMap<Int, String>>() {}.type
        return gson.fromJson(jsonString, type)
    }

    enum class UserState (val value: Int) {
        ON_TIME_RUNNING(1),
        ON_ANSWER_VALIDATING(2),
        ON_GAME_OVER(3),
        ON_GAME_SCORE_VIEW(4)
    }
}