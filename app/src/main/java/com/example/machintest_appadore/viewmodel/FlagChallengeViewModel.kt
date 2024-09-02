package com.example.machintest_appadore.viewmodel

import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.machintest_appadore.model.Country

/**
 * Created by Siru malayil on 01-09-2024.
 */
class FlagChallengeViewModel: ViewModel() {

    var answerStatusId = MutableLiveData<Int?>()

    var selectedCountry: Country? = null

    var listAnswerTextIds = LinkedHashMap<Int,AppCompatTextView>()

    var userState: Int = -1

    var userAnswerDataMap = hashMapOf<Int,String>()
}