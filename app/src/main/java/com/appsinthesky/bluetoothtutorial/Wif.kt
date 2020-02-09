package com.appsinthesky.bluetoothtutorial

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import java.util.*
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.graphics.Color
import android.graphics.ColorSpace
import android.os.CountDownTimer
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.webkit.RenderProcessGoneDetail
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_wif.*
import kotlinx.android.synthetic.main.control_layout.*
import kotlinx.android.synthetic.main.control_layout.Red_Card_Button
import kotlinx.android.synthetic.main.control_layout.Reset
import kotlinx.android.synthetic.main.control_layout.Start
import kotlinx.android.synthetic.main.control_layout.Stop
import kotlinx.android.synthetic.main.control_layout.Timer_Countdown
import kotlinx.android.synthetic.main.control_layout.Yellow_Card_Button
import kotlinx.android.synthetic.main.control_layout.card_button
import kotlinx.android.synthetic.main.control_layout.minus_left
import kotlinx.android.synthetic.main.control_layout.minus_right
import kotlinx.android.synthetic.main.control_layout.plus_left
import kotlinx.android.synthetic.main.control_layout.plus_right
import kotlinx.android.synthetic.main.control_layout.timer_1min
import kotlinx.android.synthetic.main.control_layout.timer_3min
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import kotlinx.android.synthetic.main.activity_wif.etMessage as etMessage1


class Wif: AppCompatActivity() {


    internal var Thread1: Thread? = null
    var countDownTimer: CountDownTimer? = null;
    var remainingTime: Long = 0
    var INTERVAL: Long = 1
    var didStartCountDown = false

    companion object {
        internal lateinit var serverSocket: ServerSocket
        private var input: BufferedReader? = null
        private var output: PrintWriter? = null
        var SERVER_IP = "192.168.43.1"
        val SERVER_PORT = 54321
        var licznik  = 0;
        var differenc = 0;
        var transmision = 0;
        var Left_Counter = 0;
        var Right_Counter = 0;
        var message = 0;

    }



    override fun onCreate(savedInstanceState: Bundle?) {"'"


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wif)
        ConnectToWifi(this).execute()

        setButtonCountDownListener()
        setButtonStopCountDownListener()
        val etMessage = findViewById(R.id.etMessage) as TextView
        etMessage.text="$message"




        if(savedInstanceState !=null) {

            remainingTime = savedInstanceState.getLong("remainingTime")
            didStartCountDown = savedInstanceState.getBoolean("didStartCountDown")
            Stop.setText(savedInstanceState.getString("buttonStopCountDownText"))
            Timer_Countdown.setText(savedInstanceState.getString("textViewCountDown"))
            if (didStartCountDown) {
                startCountDownTimer(remainingTime, INTERVAL)
                Stop.isEnabled = true
            }
        }


        Yellow_Card_Button.setOnClickListener {
            card_button.setBackgroundColor(Color.YELLOW)
            card_button.visibility= View.VISIBLE;
            setButtonStopCountDownListener()

        }
        Red_Card_Button.setOnClickListener {
            card_button.setBackgroundColor(Color.RED)
            card_button.visibility= View.VISIBLE;
            setButtonStopCountDownListener()
            // var ShowRedCard = Intent(applicationContext, Red_Card::class.java)
            // startActivity(ShowRedCard)


        }
        Reset.setOnClickListener{
            reset_all()
        }
        plus_left.setOnClickListener {
            left_plus()
        }
        minus_left.setOnClickListener {
            left_minus()
        }
        plus_right.setOnClickListener {
            right_plus()

        }
        minus_right.setOnClickListener {
            right_minus()

        }

    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putLong("remainingTime", remainingTime)
        outState?.putBoolean("didStartCountDown", didStartCountDown)
        outState?.putString("buttonStopCountDownText", Stop.text.toString())
        outState?.putString("textViewCountDown", Timer_Countdown.text.toString())
    }
    private fun right_plus(){
        Right_Counter++
        card_button.setBackgroundColor(Color.GREEN)
        card_button.visibility= View.VISIBLE;
        val Right_Score = findViewById(R.id.right_score) as TextView
        Right_Score.text="$Right_Counter"
        StopTimer()


    }


    private fun right_minus(){
        Right_Counter--
        val Right_Score = findViewById(R.id.right_score) as TextView
        Right_Score.text="$Right_Counter"
        StopTimer()
    }
    private fun left_plus(){
        Left_Counter++
        card_button.setBackgroundColor(Color.WHITE)
        card_button.visibility= View.VISIBLE;
        val Left_Score = findViewById(R.id.left_score) as TextView
        Left_Score.text="$Left_Counter"
        StopTimer()


    }


    private fun left_minus(){
        Left_Counter--
        val Left_Score = findViewById(R.id.left_score) as TextView
        Left_Score.text="$Left_Counter"
        StopTimer()
    }


    private fun reset_all(){

        Timer_Countdown.text="00:00"
        remainingTime=0;
        Right_Counter = 0;
        val Right_Score = findViewById(R.id.right_score) as TextView
        Right_Score.text="$Right_Counter"
        Left_Counter = 0;
        val Left_Score = findViewById(R.id.left_score) as TextView
        Left_Score.text="$Left_Counter"
    }

    private fun setButtonCountDownListener(){

        timer_1min.setOnClickListener{
            remainingTime=0;
            remainingTime=60000;
            Timer_Countdown.text="01:00"
        }
        timer_3min.setOnClickListener{
            remainingTime=0;
            remainingTime=180000;
            Timer_Countdown.text="03:00"
        }

        Start.setOnClickListener {

            //  if(editText.text.toString().isEmpty()){
            //     return@setOnClickListener
            //}

            Stop.isEnabled = true

            //   remainingTime = Integer.parseInt(editText.text.toString()).toLong()
            stopCountDownTimer()
            startCountDownTimer(remainingTime, INTERVAL)

        }

    }
    private fun setButtonStopCountDownListener(){
        Stop.setOnClickListener{

            if (Stop.text.toString().equals(getString(R.string.stopCountDown), ignoreCase = true)) {
                stopCountDownTimer()
                Stop.text = getString(R.string.resumeCountDown)
            } else {
                startCountDownTimer(remainingTime, INTERVAL)
                Stop.text = getString(R.string.stopCountDown)
            }
        }
    }



    private fun startCountDownTimer(duration: Long, interval: Long){
        countDownTimer = object : CountDownTimer(duration, interval){
            override fun onTick(millisUntilFinished: Long) {
                licznik = remainingTime.toInt()/1000*10
                differenc = remainingTime.toInt()/100-licznik;


                remainingTime = millisUntilFinished
                var seconds = millisUntilFinished / 1000
                var minutes = seconds / 60
                val hours = minutes / 60

                if (minutes > 0)
                    seconds = seconds % 60
                if (hours > 0)
                    minutes = minutes % 60
                val time = formatNumber(hours) + ":" + formatNumber(minutes) + ":" +
                        formatNumber(seconds)
                Timer_Countdown.setText(time)
            }

            override fun onFinish() {
                Timer_Countdown.setText("00:00")
                // flashAnimate(Timer_Countdown, 500, 0, Animation.REVERSE, Animation.INFINITE)
            }
        }
        Stop.text = getString(R.string.stopCountDown)
        countDownTimer!!.start()
        didStartCountDown = true
    }
    private fun formatNumber(value: Long): String{
        if(value < 10)
            return "0$value"
        return "$value"
    }

    private fun stopCountDownTimer(){
        countDownTimer?.cancel()
        didStartCountDown = false
    }
    fun StopTimer(){

        stopCountDownTimer()
        Stop.text = getString(R.string.resumeCountDown)


    }


     class ConnectToWifi(c: Context) : AsyncTask<Void, Void, String>() {

         private val context: Context
         init {
             this.context = c
         }

        override fun doInBackground(vararg p0: Void?): String? {
            val socket: Socket
            try {
                serverSocket = ServerSocket(SERVER_PORT)

                try {
                    socket = serverSocket.accept()
                    output = PrintWriter(socket.getOutputStream())
                    input = BufferedReader(InputStreamReader(socket.getInputStream()))
                    message = input!!.readLine().toInt()

                } catch (e: IOException) {
                    e.printStackTrace()
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }
            return message.toString()

        }


    }



}