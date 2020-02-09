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
import android.widget.TextView
import kotlinx.android.synthetic.main.control_layout.*
import java.io.IOException



class ControlActivity: AppCompatActivity() {

    var countDownTimer: CountDownTimer? = null;
    var remainingTime: Long = 0
    var INTERVAL: Long = 1
    var didStartCountDown = false

    companion object {

        var licznik  = 0;
        var differenc = 0;
        var transmision = 0;
        var Left_Counter = 0;
        var Right_Counter = 0;
        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket: BluetoothSocket? = null
        lateinit var m_progress: ProgressDialog
        lateinit var m_bluetoothAdapter: BluetoothAdapter
        var m_isConnected: Boolean = false
        lateinit var m_address: String
        var trafienie = 0;

    }



    override fun onCreate(savedInstanceState: Bundle?) {"'"


        super.onCreate(savedInstanceState)
        setContentView(R.layout.control_layout)
        m_address = intent.getStringExtra(SelectDeviceActivity.EXTRA_ADDRESS)

        ConnectToDevice(this).execute()

        setButtonCountDownListener()
        setButtonStopCountDownListener()
        disconectButton.setOnClickListener { disconnect() }





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
        sendCommend(13)
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
            sendCommend(60)
            remainingTime=0;
            remainingTime=60000;
            Timer_Countdown.text="01:00"
        }
        timer_3min.setOnClickListener{
            sendCommend(180)
            remainingTime=0;
            remainingTime=180000;
            Timer_Countdown.text="03:00"
        }

        Start.setOnClickListener {

            //  if(editText.text.toString().isEmpty()){
            //     return@setOnClickListener
            //}
            sendCommend(11)//11 oznacza START
            Stop.isEnabled = true

            //   remainingTime = Integer.parseInt(editText.text.toString()).toLong()
            stopCountDownTimer()
            startCountDownTimer(remainingTime, INTERVAL)

        }

    }
    private fun setButtonStopCountDownListener(){
        Stop.setOnClickListener{
            sendCommend(12)//oznacza zatrzymanie stopera
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

                if(differenc==5) {
                    transmision = m_bluetoothSocket!!.inputStream.available()
                    if (transmision > 0) {

                        trafienie = m_bluetoothSocket!!.inputStream.read()

                     if(trafienie==50){
                         sendCommend(100)
                         right_plus()
                     }
                     if(trafienie==51){
                         sendCommend(100)
                         left_plus()
                     }
                     if(trafienie==52){
                         sendCommend(100)
                         right_plus()
                         left_plus()
                     }

                    }
                }
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



    private fun disconnect() {
        if (m_bluetoothSocket != null) {
            try {
                m_bluetoothSocket!!.close()
                m_bluetoothSocket = null
                m_isConnected = false
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        finish()
    }
    private fun sendCommend(input:Int){
        if(m_bluetoothSocket != null){
            try{
                m_bluetoothSocket!!.outputStream.write(input)
            }  catch(e: IOException){
                e.printStackTrace()}
        }

    }

    private fun readCommend(){
        if(m_bluetoothSocket != null){
            try{
                trafienie= m_bluetoothSocket!!.inputStream.read()
            }  catch(e: IOException){
                e.printStackTrace()}
        }

    }


    private class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>() {
        private var connectSuccess: Boolean = true
        private val context: Context

        init {
            this.context = c
        }

        override fun onPreExecute() {
            super.onPreExecute()
            m_progress = ProgressDialog.show(context, "Connecting...", "please wait")
        }

        override fun doInBackground(vararg p0: Void?): String? {
            try {
                if (m_bluetoothSocket == null || !m_isConnected) {
                    m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = m_bluetoothAdapter.getRemoteDevice(m_address)
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    m_bluetoothSocket!!.connect()
                }
            } catch (e: IOException) {
                connectSuccess = false
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (!connectSuccess) {
                Log.i("data", "couldn't connect")
            } else {
                m_isConnected = true
            }
            m_progress.dismiss()
        }
    }

}