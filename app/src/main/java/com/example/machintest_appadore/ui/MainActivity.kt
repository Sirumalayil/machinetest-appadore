package com.example.machintest_appadore.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.machintest_appadore.R
import com.example.machintest_appadore.databinding.ActivityMainBinding
import com.example.machintest_appadore.databinding.RecyclerListItemLayoutBinding
import com.example.machintest_appadore.model.Country
import com.example.machintest_appadore.model.Questions
import com.example.machintest_appadore.model.flagChallenge
import com.example.machintest_appadore.utils.AppUtils.getCountryFlagByCountryCode
import com.example.machintest_appadore.utils.PreferenceUtil
import com.example.machintest_appadore.viewmodel.FlagChallengeViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.Calendar


class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    private var savedTime: Long = 0L
    private var questionNo: Int = 0
    private var selectedButton: MaterialButton? = null
    private var questionCountDown: CountDownTimer?= null
    private var timerRunning: Boolean = false
    private var timeLeftInMillis: Long = 0L
    private val buttonList = LinkedHashMap<Int,MaterialButton>()

    private val viewModel: FlagChallengeViewModel by lazy {
        ViewModelProvider(this)[FlagChallengeViewModel::class.java]
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setCurrentTime()
        binding?.btnSchedule?.setOnClickListener {
            showTimePicker()
        }
        binding?.btnSave?.setOnClickListener {
            Toast.makeText(this, "Time Saved!", Toast.LENGTH_SHORT).show()
            val currentTimeInMillis = System.currentTimeMillis()
            val startTimeInMillis = savedTime
            val timeDifferenceInMillis = startTimeInMillis - currentTimeInMillis
            startCountdownTimer(timeDifferenceInMillis)
        }
    }


    private fun startCountdownTimer(timeDifferenceInMillis: Long) {
        if (timeDifferenceInMillis > 0) {
            object : CountDownTimer(10000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val secondsRemaining = millisUntilFinished / 1000
                    if (secondsRemaining <= 10L) {
                        val countDownTime = String.format("00:%02d", secondsRemaining)
                        binding?.challengeStartsCountDown?.text = countDownTime
                        binding?.timeScheduleView?.isVisible = false
                        binding?.scheduleTimerView?.isVisible = true
                    }
                }

                override fun onFinish() {
                    binding?.challengeStartsCountDown?.isVisible = false
                    binding?.appCompatTextView?.isVisible = false
                    binding?.timeScheduleView?.isVisible = false

                    val questionObj = flagChallenge.questions?.get(0)
                    val countryList = questionObj?.countries

                    observeAnswer()
                    startQuestionTimer()
                    showQuestionLayout(questionObj,countryList)
                }
            }.start()
        }
    }

    private fun showQuestionLayout(questionObj: Questions?, countryList: List<Country>?) {
        binding?.timeScheduleView?.isVisible = false
        binding?.questionCount?.text = questionNo.toString()
        binding?.countryFlag?.setImageResource(getCountryFlagByCountryCode(questionObj?.countryCode))

        viewModel.answerStatusId.postValue(questionObj?.answerId)
        val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding?.container?.removeAllViews()
        viewModel.listAnswerTextIds.clear()
        buttonList.clear()

        var rowLayout: LinearLayout? = null
        countryList?.forEachIndexed { index, country ->
            if (index % 2 == 0) {
                rowLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    binding?.container?.addView(this)
                }
            }

            val itemParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )

            val layoutBinding = RecyclerListItemLayoutBinding.inflate(layoutInflater)
            val buttonView = layoutBinding.buttonView
            val answerStatus = layoutBinding.answerStatus
            buttonView.text = country.countryName

            viewModel.listAnswerTextIds[country.id] = answerStatus
            buttonList[country.id] = buttonView

            buttonView.setOnClickListener {
                selectedButton?.apply {
                    setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.grey)) // Reset background to default
                    setTextColor(ContextCompat.getColor(this@MainActivity, R.color.black)) // Reset text color to default
                    isChecked = false
                }
                selectedButton = buttonView
                selectedButton?.apply {
                    setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
                    setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.primary_blue))
                    isChecked = true
                }
                viewModel.selectedCountry = country
                Log.e("TAG","selected country: $country")
            }

            layoutBinding.root.layoutParams = itemParams

            rowLayout?.addView(layoutBinding.root)
        }

    }

    @SuppressLint("SetTextI18n")
    private fun startQuestionTimer(millisInFuture: Long = 5000) {
        binding?.scheduleTimerView?.isVisible = false
        binding?.timeScheduleView?.isVisible = false
        binding?.challengeView?.isVisible = true
        val questionCount = if (questionNo == 0) questionNo++ else questionNo
        binding?.questionCount?.text = "$questionCount"
        Log.e("TAG","question count start: $questionNo")
        viewModel.userState = PreferenceUtil.UserState.ON_TIME_RUNNING.value

        questionCountDown?.cancel()
        questionCountDown = object: CountDownTimer(millisInFuture, 1000){
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateUI(millisUntilFinished)
            }

            override fun onFinish() {
                questionNo++
                timerRunning = false
                validateAnswer()
                observeAnswer()
                if (questionNo < ((flagChallenge.questions?.size) ?: 0)) {
                    val newQuestionNo = questionNo
                    val questionObj = flagChallenge.questions?.get(newQuestionNo)
                    val countryList = questionObj?.countries

                    Handler(Looper.getMainLooper()).postDelayed({
                        showQuestionLayout(questionObj, countryList)
                        startQuestionTimer()
                    }, 1000)
                } else {
                    showScoreView()
                }
            }
        }
        questionCountDown?.start()
        timerRunning = true
    }

    private fun showScoreView() {
        viewModel.userState = PreferenceUtil.UserState.ON_GAME_OVER.value
        binding?.challengeView?.isVisible = false
        binding?.gameOverView?.isVisible = true
        binding?.textGameOver?.isVisible = true
        Handler(Looper.getMainLooper()).postDelayed({
            binding?.textGameOver?.isVisible = false
            binding?.scoreView?.isVisible = true
            binding?.textScore?.text = getTotalScore()
            viewModel.userState = PreferenceUtil.UserState.ON_GAME_SCORE_VIEW.value
        },5000)
    }

    private fun getTotalScore(): String {
        val totalQuestions = flagChallenge.questions?.size
        val score = viewModel.userAnswerDataMap?.values?.count { it == "correct" }
        return "$score/$totalQuestions"
    }

    private fun validateAnswer() {
        viewModel.userState = PreferenceUtil.UserState.ON_ANSWER_VALIDATING.value
        buttonList.forEach { (_, button) ->
            button.isClickable = false
            button.isEnabled = false
        }
    }

    private fun updateUI(millisUntilFinished: Long) {
        val secondsRemaining = millisUntilFinished / 1000
        val countDownTime = String.format("00:%02d", secondsRemaining)
        binding?.btnTimePicker?.text = countDownTime
    }

    private fun observeAnswer() {
        viewModel.answerStatusId.observe(this) { answerId ->

            if (viewModel.selectedCountry?.id == answerId) {
                val answerText = viewModel.listAnswerTextIds[viewModel.selectedCountry?.id]
                answerText?.isVisible = true
                answerText?.text = getString(R.string.correct)
                answerText?.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.green))
                updateScoreData(questionNo,"correct")
                selectedButton?.apply {
                    strokeColor = ColorStateList.valueOf(ContextCompat.getColor(
                        this@MainActivity, R.color.green))
                    setBackgroundColor(ContextCompat.getColor(this@MainActivity,R.color.green))
                    setTextColor(ContextCompat.getColor(this@MainActivity,R.color.white))
                }
            } else {
                val answerText = viewModel.listAnswerTextIds[viewModel.selectedCountry?.id]
                if (answerText != null) {
                    answerText.isVisible = true
                    answerText.text = getString(R.string.wrong)
                    answerText.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.colorPrimary))
                    selectedButton?.setBackgroundColor(ContextCompat.getColor(this@MainActivity,R.color.colorPrimary))
                    selectedButton?.setTextColor(ContextCompat.getColor(this@MainActivity,R.color.white))
                    val buttonView = buttonList[answerId]
                    val newAnswerText = viewModel.listAnswerTextIds[answerId]
                    newAnswerText?.text = getString(R.string.correct)
                    buttonView?.apply {
                        strokeColor = ColorStateList.valueOf(ContextCompat.getColor(
                            this@MainActivity, R.color.green))
                        setBackgroundColor(ContextCompat.getColor(this@MainActivity,R.color.green))
                        setTextColor(ContextCompat.getColor(this@MainActivity,R.color.white))
                    }
                    updateScoreData(questionNo,"wrong")
                }
            }
        }
    }

    private fun updateScoreData(key: Int, value: String) {
        if (viewModel.userAnswerDataMap?.containsKey(key) == true) {
            viewModel.userAnswerDataMap?.remove(key)
        }
        viewModel.userAnswerDataMap?.set(key, value)

    }

    private fun setCurrentTime() {
        val calender = Calendar.getInstance()
        val hour = calender.get(Calendar.HOUR)
        val minute = calender.get(Calendar.MINUTE)
        val AM_PM = calender.get(Calendar.AM_PM)

        val timeFormat = if (AM_PM == Calendar.AM) "AM" else "PM"
        val formattedHour = if (hour == 0) 12 else hour
        val timeNow = String.format("%02d:%02d %s",formattedHour,minute,timeFormat)

        binding?.btnTimePicker?.text = timeNow
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()

        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(calendar.get(Calendar.HOUR_OF_DAY))
            .setMinute(calendar.get(Calendar.MINUTE))
            .setTitleText("Select Challenge Start Time")
            .build()

        timePicker.show(supportFragmentManager, "timePicker")

        timePicker.addOnPositiveButtonClickListener {
            val calender = Calendar.getInstance()
            val selectedHour = timePicker.hour
            val selectedMinute =  timePicker.minute
            val seconds = calendar.get(Calendar.SECOND)
            val AM_PM = calender.get(Calendar.AM_PM)
            val timeFormat = if (AM_PM == Calendar.AM) 0 else 1
            val formattedHour = if (selectedHour % 12 == 0) 12 else selectedHour % 12

            binding?.hourDigit1?.text = formattedHour.toString().first().toString()
            binding?.hourDigit2?.text = formattedHour.toString().last().toString()
            binding?.minuteDigit1?.text = selectedMinute.toString().first().toString()
            binding?.minuteDigit2?.text = selectedMinute.toString().last().toString()
            binding?.secondsDigit1?.text = seconds.toString().first().toString()
            binding?.secondsDigit2?.text = seconds.toString().last().toString()

            binding?.scheduleTimerView?.isEnabled = true

            val isAM: Boolean = timeFormat == Calendar.AM
            val hourOfDay = if (isAM && selectedHour == 12) {
                0
            } else if (!isAM && selectedHour != 12) {
                selectedHour + 12 // Convert PM to 24-hour format
            } else {
                selectedHour
            }

            savedTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hourOfDay)
                set(Calendar.MINUTE, selectedMinute)
                set(Calendar.AM_PM, timeFormat)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }

        timePicker.addOnNegativeButtonClickListener {}
    }

    override fun onPause() {
        super.onPause()
        PreferenceUtil(this).feedUserData(
            questionNo = questionNo,
            endTime = timeLeftInMillis,
            userStatus = viewModel.userState
        )
        PreferenceUtil(this).saveHashMapToPreferences("data",viewModel.userAnswerDataMap)
        questionCountDown?.cancel()
    }

    override fun onResume() {
        super.onResume()
        updateUserState()
    }

    override fun onStart() {
        super.onStart()
        updateUserState()
    }

    private fun updateUserState() {
        val timeLeftInMillis = PreferenceUtil(this).fetchEndTime()
        val newQuestNo = PreferenceUtil(this).fetchQuestionNo()
        val userState = PreferenceUtil(this).fetchUserState()
        val userAnswerData = PreferenceUtil(this).getHashMapFromPreferences("data")

        viewModel.userState = userState
        viewModel.userAnswerDataMap.clear()
        viewModel.userAnswerDataMap = userAnswerData

        if (newQuestNo == -1 || newQuestNo >= ((flagChallenge.questions?.size) ?: 0)) return
        val questionObj = flagChallenge.questions?.get(newQuestNo)
        val countryList = questionObj?.countries

        Log.e("TAG", "Userstate: $userState")

        when(userState) {
            PreferenceUtil.UserState.ON_TIME_RUNNING.value -> {
                questionNo = newQuestNo
                if (timeLeftInMillis != 0L) {
                    startQuestionTimer(timeLeftInMillis)
                    showQuestionLayout(
                        questionObj = questionObj,
                        countryList = countryList
                    )
                    PreferenceUtil(this).clearPreferences()
                }
            }
            PreferenceUtil.UserState.ON_ANSWER_VALIDATING.value -> {
                questionNo = newQuestNo
                Handler(Looper.getMainLooper()).postDelayed({
                    showQuestionLayout(
                        questionObj = questionObj,
                        countryList = countryList
                    )
                    startQuestionTimer()
                },1000)
                binding?.challengeView?.isVisible = true
            }
            PreferenceUtil.UserState.ON_GAME_OVER.value -> {
                showScoreView()
            }
            PreferenceUtil.UserState.ON_GAME_SCORE_VIEW.value -> {
                binding?.textGameOver?.isVisible = false
                binding?.scoreView?.isVisible = true
                binding?.textScore?.text = getTotalScore()
                viewModel.userState = PreferenceUtil.UserState.ON_GAME_SCORE_VIEW.value
            }
        }
    }
}