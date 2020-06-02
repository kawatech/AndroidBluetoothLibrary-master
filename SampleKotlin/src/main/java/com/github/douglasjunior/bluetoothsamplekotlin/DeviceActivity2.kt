/*
 * MIT License
 *
 * Copyright (c) 2015 Douglas Nassif Roma Junior
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.douglasjunior.bluetoothsamplekotlin

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService.OnBluetoothEventCallback
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothWriter
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.text.SimpleDateFormat
import java.util.*
import kotlin.experimental.and

/**
 * Created by douglas on 10/04/2017.
 */
class DeviceActivity2 : AppCompatActivity(), OnBluetoothEventCallback, View.OnClickListener {
    //    private var mFab: FloatingActionButton? = null
//    private var mEdRead: EditText? = null
    private var mEdRead: TextView? = null                  // kawa TextView に変更
    private var mEdWrite: EditText? = null                  // kawa 削除した
    private var mService: BluetoothService? = null
    private var mWriter: BluetoothWriter? = null
    // kawa
    private val hexArray = "0123456789ABCDEF".toCharArray()
    private var mChart: LineChart? = null           // kawa2
    private var mChart2: LineChart? = null           // kawa3

    private var offset = 9999.toInt()
    private var restBuffer: ByteArray = byteArrayOf(0x02.toByte(), 0x02.toByte(), 0x02.toByte(), 0x02.toByte(), 0x02.toByte())
    private var prevVer =0.toInt()

    private lateinit var mToolbar: Toolbar              // kawa Drawer
    private var mGenre = 0

    private var xLenght = 1000f
    private var loopCount = 1f

    // 不連続時に1回前の値を代用するために、保存しておくための変数
    private var prevf1 = 0f
    private var prevf2 = 0f

    // X軸のラベルの間隔
    private var xIntervalRange = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device2)

        // kawa Drawer ここの修正が必要
        //       val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        //    setSupportActionBar(toolbar)


        // kawa Drawer これがあると停止する
        mToolbar = findViewById(R.id.toolbar)
        //      setSupportActionBar(mToolbar)

/* ----------------------------------------------------------------------------------------------
         Mainから秒数の設定データをもらう。キーワードがXSECでvaluew1に値が入る
         この値によって30秒ごとにX軸の幅を設定する。メニューバーに秒数表示。
 ------------------------------------------------------------------------------------------------- */
        val value1 = intent.getIntExtra("XSEC",1)

        val dm = DisplayMetrics()
        getWindowManager().getDefaultDisplay().getMetrics(dm)
        var winW = (dm.widthPixels )
        var winH = dm.heightPixels
        var wLong = 1
        if(winW > winH) {
            wLong = winW
        }
        else {
            wLong = winH
        }
        wLong = (wLong - 600) / 22
        var stmp = ""
        for (i in 0..wLong) {
            stmp = stmp + " "
        }

        if (value1 == 1 ) {
            mToolbar.title = "Losteaka Oscilloscope 30sec " + stmp + "ロステーカ株式会社"
            xLenght = 1875f
            xIntervalRange = 100
            //    xLenght = 187f                 // 試しに幅を 1/10　にするとき
        }
        else if (value1 == 2)
        else if (value1 == 3) {
            mToolbar.title = "Losteaka Oscilloscope 90sec " + stmp + "ロステーカ株式会社"
            xLenght = 5625f
            xIntervalRange = 300
        }
        else if (value1 == 4) {
            mToolbar.title = "Losteaka Oscilloscope 120sec" + stmp + "ロステーカ株式会社"
            xLenght = 7500f
            xIntervalRange = 400
        }
        else if (value1 == 5) {
            mToolbar.title = "Losteaka Oscilloscope 150sec" + stmp + "ロステーカ株式会社"
            xLenght = 9375f
            xIntervalRange = 500
        }
        else if (value1 == 6) {
            mToolbar.title = "Losteaka Oscilloscope 180sec" + stmp + "ロステーカ株式会社"
            xLenght = 11250f
            xIntervalRange = 600
        }
        else if (value1 == 7) {
            mToolbar.title = "Losteaka Oscilloscope 210sec" + stmp + "ロステーカ株式会社"
            xLenght = 13125f
            xIntervalRange = 700
        }
        else if (value1 == 8) {
            mToolbar.title = "Losteaka Oscilloscope 15sec " + stmp + "ロステーカ株式会社"
            xLenght = 938f
            xIntervalRange = 50
        }
        setSupportActionBar(mToolbar)
        /* --------------- kawa2 ---------------------------
            グラフ（チャート）の初期設定、2段で2つ分
        ---------------------------------------------------- */
        mChart = findViewById(R.id.chart) as LineChart
        initChart()

        mChart2 = findViewById(R.id.chart2) as LineChart            // kawa3
        initChart2()


/* -------------------------------------------------------------------------------------
               // kawa Drawer ナビゲーションドロワーの設定
               // メニューが全画面になってしまう
               val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
               val toggle = ActionBarDrawerToggle(this, drawer, mToolbar, R.string.app_name, R.string.app_name)
               drawer.addDrawerListener(toggle)
               toggle.syncState()

               val navigationView = findViewById<NavigationView>(R.id.nav_view)
               navigationView.setNavigationItemSelectedListener(this)
     -------------------------------------------------------------------------------------  */



        //       mFab = findViewById<View>(R.id.fab) as FloatingActionButton
        //       mFab!!.setOnClickListener(this)

        //      mEdRead = findViewById<View>(R.id.ed_read) as EditText
        mEdRead = findViewById<View>(R.id.ed_read) as TextView                  // kawa EditからTextView に変更

        //       mEdWrite = findViewById<View>(R.id.ed_write) as EditText          // 送信することはないのでマスクする
        mService = BluetoothService.getDefaultInstance()
        mWriter = BluetoothWriter(mService)

        //★★★ ソフトキーボードを隠す。
        //   val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        //   inputMethodManager.hideSoftInputFromWindow(mEdWrite?.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)

        /* --------------------------------------------------
           ボタンをタッチすると、MainActivityの画面に戻る
           MainActivityでX軸の幅を設定するようにした
        ---------------------------------------------------- */
        val btn1: Button = findViewById(R.id.backr_btn)
        btn1.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("Los", 2)
            startActivity(intent)
        }


        /* --------------------------------------------------
            NORMALボタンをタッチすると、DeviceActivityの画面に移る

        val btn2: Button = findViewById(R.id.normal_btn)
        btn2.setOnClickListener {
            val intent = Intent(this, DeviceActivity::class.java)
            intent.putExtra("Los", 2)
            startActivity(intent)
        }
---------------------------------------------------- */


    }       // onCreat()ここまで

    /* -------------------------------------------------------------
    この画面で、X軸の幅を設定するときは、ここを使う
    いまは、ドロワーを出そうとすると停止するのでマスク
    ----------------------------------------------------------------- */
/*
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.nav_30s) {
            mToolbar.title = "30sec"
            mGenre = 1
        } else if (id == R.id.nav_60s) {
            mToolbar.title = "60sec"
            mGenre = 2
        } else if (id == R.id.nav_90s) {
            mToolbar.title = "90sec"
            mGenre = 3
        } else if (id == R.id.nav_120s) {
            mToolbar.title = "120sec"
            mGenre = 4
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }
*/


    override fun onResume() {
        super.onResume()
        mService!!.setOnEventCallback(this)

    }


    /*  ------------------------------------ kawa2 -----------------
    チャートの初期設定、MPAndroidChartSampleから流用
    ----------------------------------------------------------------- */

    private fun initChart() {
        // no description text
        mChart?.setDescription("")
        mChart?.setNoDataTextDescription("You need to provide data for the chart.")

        // enable touch gestures
        mChart?.setTouchEnabled(true)

        // enable scaling and dragging
        mChart?.setDragEnabled(true)
        mChart?.setScaleEnabled(true)
        mChart?.setDrawGridBackground(false)


        // if disabled, scaling can be done on x- and y-axis separately
        mChart?.setPinchZoom(true)


        // set an alternative background color
        //   mChart?.setBackgroundColor(Color.LTGRAY)
        mChart?.setBackgroundColor(Color.BLACK)
        val data = LineData()
        //   data.setValueTextColor(Color.BLACK)
        data.setValueTextColor(Color.LTGRAY)

        // add empty data
        mChart?.setData(data)

        //  ラインの凡例の設定
        val l = mChart?.getLegend()
        l?.form = Legend.LegendForm.LINE        // 凡例を線で表す
        //    l?.textColor = Color.BLACK
        l?.textColor = Color.LTGRAY

        val xl = mChart?.getXAxis()
        //   xl?.textColor = Color.BLACK
        xl?.textColor = Color.LTGRAY
        xl?.setLabelsToSkip(xIntervalRange)

        xl?.isEnabled = true                   // falseのとき、上のラベルが表示されない

        val leftAxis = mChart?.getAxisLeft()
        //   leftAxis?.textColor = Color.BLACK
        leftAxis?.textColor = Color.LTGRAY
        leftAxis?.setAxisMaxValue( 5000.0f)             // Y軸の最大、最小
        leftAxis?.setAxisMinValue(-3.0f)

        // リミットラインを入れる、やめる
        //   leftAxis?.setDrawLimitLinesBehindData(true)           // グラフの線の後ろにするとき
        /* -----------------------------------------
        val ll = LimitLine(750f,"lower")
        ll.lineColor = Color.parseColor("#008577")        // 濃い緑
        ll.lineWidth = 1f
        ll.textColor = Color.BLACK
        ll.textSize = 10f
        leftAxis?.addLimitLine(ll)

        val uu = LimitLine(3500f,"upper")
        uu.lineColor = Color.CYAN                   // 空色
        uu.lineWidth = 1f
        uu.textColor = Color.BLACK
        uu.textSize = 10f
        leftAxis?.addLimitLine(uu)
    ------------------------------------------------------------- */

        //     leftAxis?.setStartAtZero(false)
        leftAxis?.setDrawGridLines(true)
        val rightAxis = mChart?.getAxisRight()
        rightAxis?.isEnabled = false
    }

    // ---------------------------------------------------------
    private fun initChart2() {
        // no description text
        mChart2?.setDescription("")
        mChart2?.setNoDataTextDescription("You need to provide data for the chart.")

        // enable touch gestures
        mChart2?.setTouchEnabled(true)

        // enable scaling and dragging
        mChart2?.setDragEnabled(true)
        mChart2?.setScaleEnabled(true)
        mChart2?.setDrawGridBackground(false)

        // if disabled, scaling can be done on x- and y-axis separately
        mChart2?.setPinchZoom(true)

        // set an alternative background color
        //   mChart2?.setBackgroundColor(Color.LTGRAY)
        mChart2?.setBackgroundColor(Color.BLACK)
        val data2 = LineData()                      // kawa2
        //   data2.setValueTextColor(Color.BLACK)
        data2.setValueTextColor(Color.LTGRAY)

        // add empty data
        mChart2?.setData(data2)

        //  ラインの凡例の設定
        val l = mChart2?.getLegend()
        l?.form = Legend.LegendForm.LINE
        //   l?.textColor = Color.BLACK
        l?.textColor = Color.LTGRAY
        val xl = mChart2?.getXAxis()
        //   xl?.textColor = Color.BLACK
        xl?.textColor = Color.LTGRAY
        xl?.setLabelsToSkip(xIntervalRange)

        xl?.isEnabled = false                   // falseのとき、上のラベルが表示されない

        val leftAxis = mChart2?.getAxisLeft()
        //    leftAxis?.textColor = Color.BLACK
        leftAxis?.textColor = Color.LTGRAY

        //   leftAxis?.setAxisMaxValue( 100.0f)
        //   leftAxis?.setAxisMinValue(-10.0f)

        // Y軸の幅を明示的に1.0にする
        leftAxis?.setLabelCount(6,true)         // Y軸のラベルを6個
        leftAxis?.setGranularity(1.0f)                           // 間隔は1.0

        leftAxis?.setAxisMaxValue( 5.0f)
        leftAxis?.setAxisMinValue(0.0f)
        leftAxis?.setShowOnlyMinMax(true)

        //     leftAxis?.setStartAtZero(false)
        leftAxis?.setDrawGridLines(true)
        val rightAxis = mChart2?.getAxisRight()
        rightAxis?.isEnabled = false
    }
// -------------------------------------------------------------------------





    /*  ----------------------------------------------------------------------------
        // ここが受信データが入ったときに実行される関数、この中で処理する
     ------------------------------------------------------------------------------- */
    override fun onDataRead(buffer: ByteArray, length: Int) {
        //  Log.d(TAG, "onDataRead: " + String(buffer, 0, length))      // これはAndroid Studio に出す分
        //   Log.d(TAG, "onDataRead: " + buffer.contentToString())       // これはLogで10進数が出る

        //  mEdRead!!.append("""    < ${String(buffer, 0, length)}    """.trimIndent()) // org 文字化けする

        //  val buffer1 = byteArrayOfInts(0xA1, 0x2E, 0x38, 0xD4, 0x89, 0xC3)
        //    mEdRead!!.setText(buffer.contentToString(), TextView.BufferType.NORMAL)     // kawa 数字10進数を連続で出す 下位2bit OKだが10進
//    mEdRead!!.setText(buffer.contentToString())                  // kawa TextView に変更

// kawa このループの中で何かする


        // ここでグラフにデータ nn を送る
        //  追加描画するデータを追加
        val data = mChart?.getData() ?: return
        val data2 = mChart2?.getData() ?: return
        //   var set = data.getDataSetByIndex(0)

        //   if(data != null){
//リアルタイムでデータを更新する場合はILineDataSetを使う
        //データをセットする際にインデックスを指定

        //リアルタイムでデータを更新する場合はILineDataSetを使う
        //データをセットする際にインデックスを指定
        // data, data2でそれぞれ0から始める
        var set1 = data.getDataSetByIndex(0)
        //2本目のグラフ（インデックスを1に）
        var set2 = data.getDataSetByIndex(1)       /////

        // 下のグラフに描く、dsta2でインデックスは0から
        var set21 = data2.getDataSetByIndex(0)
        var set22 = data2.getDataSetByIndex(1)
        var set23 = data2.getDataSetByIndex(2)
        var set24 = data2.getDataSetByIndex(3)
        var set25 = data2.getDataSetByIndex(4)
        var set26 = data2.getDataSetByIndex(5)

        //   }


        if (set1 == null) {
            //      set1 = createSet()
            //      set2 = createSet()
            set1 = LineDataSet(null, "データ1")
            //   set1.color = Color.BLUE
            set1.color = Color.YELLOW
            set1.setDrawValues(false)
            set1.setDrawCircles(false)          // データの頂点の丸を描画しない
            data.addDataSet(set1)
        }

        if (set2 == null) {
            set2 = LineDataSet(null, "データ2")
            //    set2.color = Color.RED
            //    set2.color = Color.GREEN
            //    set2.color = Color.parseColor("#00FF5F")            // 鮮やかな緑
            set2.color = Color.parseColor("#00FF00")            // 鮮やかな緑
            set2.setDrawValues(false)
            set2.setDrawCircles(false)
            data.addDataSet(set2)  /////
        }

        if (set21 == null) {
            set21 = LineDataSet(null, "IO0")        // kawa3
            //   set3.color = Color.GREEN                      // 緑
            //    set3.color = Color.BLACK
            //      set3.color = Color.MAGENTA                  // 桃色
            //      set3.color = Color.CYAN                     // 空色 //
            //      set3.color = Color.parseColor("#008577")        // 濃い緑
            //   set3.color = Color.parseColor("#d2691e")    //チョコレート色
            set21.color = Color.YELLOW
            set21.setDrawValues(false)
            set21.setDrawCircles(false)
            data2.addDataSet(set21)
        }

        if (set22 == null) {
            set22 = LineDataSet(null, "IO1")        // kawa3
            //   set22.color = Color.GREEN                      // 緑
            //    set22.color = Color.BLACK
            //      set22.color = Color.MAGENTA                  // 桃色
            //      set22.color = Color.CYAN                     // 空色 //
            //      set22.color = Color.parseColor("#008577")        // 濃い緑
           //    set22.color = Color.parseColor("#d2691e")    //チョコレート色
            set22.color = Color.parseColor("#ffa500")    //オレンジ
         //   set22.color = Color.YELLOW
            set22.setDrawValues(false)
            set22.setDrawCircles(false)
            data2.addDataSet(set22)
        }

        if (set23 == null) {
            set23 = LineDataSet(null, "IO2")        // kawa3
            //   set22.color = Color.GREEN                      // 緑
            //    set22.color = Color.BLACK
                  set23.color = Color.MAGENTA                  // 桃色
            //      set22.color = Color.CYAN                     // 空色 //
            //      set22.color = Color.parseColor("#008577")        // 濃い緑
            //   set22.color = Color.parseColor("#d2691e")    //チョコレート色
         //   set22.color = Color.YELLOW
            set23.setDrawValues(false)
            set23.setDrawCircles(false)
            data2.addDataSet(set23)
        }


        if (set24 == null) {
            set24 = LineDataSet(null, "IO3")        // kawa3
            //   set22.color = Color.GREEN                      // 緑
            //    set22.color = Color.BLACK
         //   set24.color = Color.MAGENTA                  // 桃色
                  set24.color = Color.CYAN                     // 空色 //
            //      set22.color = Color.parseColor("#008577")        // 濃い緑
            //   set22.color = Color.parseColor("#d2691e")    //チョコレート色
            //   set22.color = Color.YELLOW
         //   set23.setDrawValues(false)
            set24.setDrawCircles(false)
            data2.addDataSet(set24)
        }


        if (set25 == null) {
            set25 = LineDataSet(null, "IO4")        // kawa3
            //   set22.color = Color.GREEN                      // 緑
            //    set22.color = Color.BLACK
            //   set24.color = Color.MAGENTA                  // 桃色
         //   set25.color = Color.CYAN                     // 空色 //
            set25.color = Color.RED                     // 赤
            //      set22.color = Color.parseColor("#008577")        // 濃い緑
            //   set22.color = Color.parseColor("#d2691e")    //チョコレート色
            //   set22.color = Color.YELLOW
            //   set23.setDrawValues(false)
            set25.setDrawCircles(false)
            data2.addDataSet(set25)
        }


        if (set26 == null) {
            set26 = LineDataSet(null, "IO5")        // kawa3
               set26.color = Color.GREEN                      // 緑
            //    set22.color = Color.BLACK
            //   set24.color = Color.MAGENTA                  // 桃色
            //   set25.color = Color.CYAN                     // 空色 //
         //   set25.color = Color.RED                     // 赤
            //      set22.color = Color.parseColor("#008577")        // 濃い緑
            //   set22.color = Color.parseColor("#d2691e")    //チョコレート色
            //   set22.color = Color.YELLOW
            //   set23.setDrawValues(false)
            set26.setDrawCircles(false)
            data2.addDataSet(set26)
        }












        val date = Date()
        val format = SimpleDateFormat("HH:mm:ss")
        data.addXValue(format.format(date))
        data2.addXValue(format.format(date))

        // 初期値を設定しておく
        var fvalue1 = 5000f
        var fvalue2 = 5000f

        var fvalue21 = 0f
        var fvalue22 = 0.5f
        var fvalue23 = 1.5f
        var fvalue24 = 2.5f
        var fvalue25 = 3.5f
        var fvalue26 = 4.5f

        var workBuffer = ByteArray(80)

        var tmpOffset = 0
        val ll = buffer.size

/* -----------------------------------------------------------
   下2ビットが00までのオフセットを求める
   tmpOffsetにいれて、画面上面に表示する
   ----------------------------------------------------------- */

        for (i in 0..10) {
            // 連続した2バイトの下2ビットが00

            if (((buffer[i] and (0x03)) == 0x00.toByte()) and ((buffer[i + 1] and (0x03)) == 0x00.toByte())) {
                //    offset = 5- (i % 5)
                //    tmpOffset = i

                //   if((tmpOffset == 5) or (tmpOffset == 0)) {
                //       offset = 0
                //   }
                break
            }
            tmpOffset++
        }

        Log.d(TAG, "onDataRead: " + tmpOffset)
        mEdRead!!.setText(tmpOffset.toString())             // オフセットを画面に出す



        /* --------------------------------------------------------------
           if(tmpOffset > 0) {
               for (k in 0..(tmpOffset - 1)) {
                   buffer[k] = 0x02.toByte()
               }
           }

         // workBufferに入れる
        if(offset > 0) {
            for (k in 0..(offset - 1)) {
                workBuffer[k] = restBuffer[k]
            }
        }

        // ここからNGが出る 直った()にする
          for(k in 0..(ll - offset-1)) {
              workBuffer[k + offset] = buffer[k]
          }

          // restBuffer[ ]に入れる
        // ここからNGが出る
        if(offset > 0) {
            for (k in 0..(offset - 1)) {
              //  restBuffer[k] = buffer[ll - offset + k + 1]
                restBuffer[k] = buffer[ll - offset + k]
            }
        }
    ----------------------------------------------------------------------------- */

/* -------------------------------------------------------------------------------
 連続して、00 00 01 01 の並びの時に値を抽出する。
     12bitでマスクする。
-------------------------------------------------------------------------------- */


        for (i in 0..10) {                        // データが10個なら0と1
            // 連続した2バイトの下2ビットが00
            if (((buffer[i] and (0x03)) == 0x00.toByte()) and ((buffer[i+1] and (0x03)) == 0x00.toByte())
                    and ((buffer[i+2] and (0x03)) == 0x01.toByte()) and ((buffer[i+3] and (0x03)) == 0x01.toByte())) {

                var v = buffer[i + 1].toInt() and (0xFC)
                var u = buffer[i].toInt() and (0xFC)
                var nn = u.shl(4) + v.ushr(2)
                //    nn = nn and (0xFFF)                               // ここのマスクはあってもなくてもい
                fvalue1 = nn.toFloat()               // kawa Floatに変換して使う


                v = buffer[i+3].toInt() and (0xFC)
                u = buffer[i+2].toInt() and (0xFC)
                nn = u.shl(4) + v.ushr(2)
                //     nn = nn and (0xFFF)
                fvalue2 = nn.toFloat()               // kawa Floatに変換して使う

                break
            }
        }




// 下2ビットが一致しなかったとき、データを出力する
        /*
    if (fvalue1 > 4500f) {
        mEdRead!!.setText(Integer.toHexString(ByteBuffer.wrap(buffer).getInt()), TextView.BufferType.NORMAL)
    }
    */

/*
         for (i in 0..10) {                        // データが10個なら0と1
             // 連続した2バイトの下2ビットが00
             if (((buffer[i] and (0x03)) == 0x00.toByte()) and ((buffer[i + 1] and (0x03)) == 0x00.toByte())) {
                 var v = buffer[i + 1].toInt() and (0xFC)
                 var u = buffer[i].toInt() and (0xFC)
                 var nn = u.shl(4) + v.ushr(2)

                 fvalue1 = nn.toFloat()               // kawa Floatに変換して使う
                 break
             }
         }

         for (i in 0..10) {
             // 連続した2バイトの下2ビットが01
             if (((buffer[i] and (0x03)) == 0x01.toByte()) and ((buffer[i + 1] and (0x03)) == 0x01.toByte())) {
                 var v = buffer[i + 1].toInt() and (0xFC)
                 var u = buffer[i].toInt() and (0xFC)
                 var nn = u.shl(4) + v.ushr(2)
                 fvalue2 = nn.toFloat()               // kawa Floatに変換して使う
                 break
             }
         }
*/



/*
         for (i in 0..10) {
             // 1バイトの下2ビットが11
             if ((buffer[i] and (0x03)) == 0x03.toByte()) {
                 var u = buffer[i].toInt() and (0xFC)
                 var nn = u.ushr(2)
                 fvalue3 = nn.toFloat()               // kawa Floatに変換して使う
                 break
             }
         }
*/
        fvalue21 = tmpOffset.toFloat()          // 00 00 のオフセットを出力する


        // 赤のラインで12ビットの範囲の値でなければ、表示しない。これで見かけ上不連続なし
        // ただし、サンプル1回分抜けるので、よく見ると段差が見える

        // 不連続、初期値の時は1回前の値を使い描画は毎サンプル行う
        if (fvalue2 > 4095) {
            fvalue1 = prevf1
            fvalue2 = prevf2

            //   for (i in 0..10) {


            //   mEdRead!!.setText(buffer[i].toString())
            //        mEdRead!!.append(buffer[i].toString())
            //   mEdRead!!.append(buffer.toString())
            //    }

        }



        data.addEntry(Entry(fvalue1, set1.getEntryCount()), 0)
        data.addEntry(Entry(fvalue2, set2.getEntryCount()), 1)
        data2.addEntry(Entry(fvalue21, set21.getEntryCount()), 0)
        data2.addEntry(Entry(fvalue22, set22.getEntryCount()), 1)
        data2.addEntry(Entry(fvalue23, set23.getEntryCount()), 2)
        data2.addEntry(Entry(fvalue24, set24.getEntryCount()), 3)
        data2.addEntry(Entry(fvalue25, set24.getEntryCount()), 4)
        data2.addEntry(Entry(fvalue26, set24.getEntryCount()), 5)



        //  データを追加したら必ずよばないといけない
        //   data.notifyDataChanged()
        mChart?.notifyDataSetChanged()
        mChart?.setVisibleXRangeMaximum(xLenght)
        mChart?.setVisibleXRangeMinimum(xLenght)           // 最小値を最大値と同じにすると軸が固定
        //      mChart?.moveViewToX(data.xValCount - (xLenght + 1f)) //  移動する

        mChart2?.notifyDataSetChanged()
        mChart2?.setVisibleXRangeMaximum(xLenght)
        mChart2?.setVisibleXRangeMinimum(xLenght)

        // X軸を固定したときの画像の移動方法
        // 軸いっぱいに達するまでは単に更新する。それ以降の時は移動させる
        if (loopCount < xLenght) {
            mChart?.invalidate()               // 更新する
            mChart2?.invalidate()
        }else {
            mChart?.moveViewToX(data.xValCount - (xLenght + 1f)) //  移動する
            mChart2?.moveViewToX(data2.xValCount - (xLenght + 1f)) //  移動する
            loopCount = xLenght
        }
        loopCount++                // 描き始めに軸に達するまでをカウントする

// 今回のデータを次回のために保存する
        prevf1 = fvalue1
        prevf2 = fvalue2


/* ----------------------------------------------------------------------------
        ここから下は、試行錯誤した残り
        何をやったか残すため、コメントあるとしたままにしておく
-------------------------------------------------------------------------------- */

/*
        for( i in 0..ll-2 ){


         //   Log.d(TAG, "onDataRead: " + (buffer[i] and 0x03))       // これで、0, 0, 1, 1, 3の繰り返しOK
        //    mEdRead!!.setText((buffer[i] and 0x03).toString())              // 0, 1, 3の繰り返し、連続表示はない

            // 連続した2バイトの下2ビットが00
            if (((workBuffer[i]?. and (0x03)) == 0x00.toByte()) and ((workBuffer[i+1]?. and (0x03)) == 0x00.toByte())) {
                var v = workBuffer[i + 1]!!.toInt() and 0xFC
                var u = workBuffer[i]!!.toInt() and 0xFC
                var nn = u.shl(4) + v.ushr(2)

           //     Log.d(TAG, "onDataRead: " + nn)           //  ここで書くようにLogが多くなると描画が遅くなる
          //      Log.d(TAG, "onDataRead: " + i)
                mEdRead!!.setText(nn.toString())

                fvalue1 = nn.toFloat()               // kawa Floatに変換して使う
            }

            // 連続した2バイトの下2ビットが01
            if (((workBuffer[i]?. and (0x03)) == 0x01.toByte()) and ((workBuffer[i+1]?. and (0x03)) == 0x01.toByte())) {
                var v = workBuffer[i + 1]!!.toInt() and 0xFC
                var u = workBuffer[i]!!.toInt() and 0xFC
                var nn = u.shl(4) + v.ushr(2)

                fvalue2 = nn.toFloat()               // kawa Floatに変換して使う
            }

            // 1バイトの下2ビットが11
            if ((workBuffer[i]?. and (0x03)) == 0x03.toByte())  {
                var u = workBuffer[i]!!.toInt() and 0xFC
                var nn = u.ushr(2)

 //   if(nn != 0) {
 //   Log.d(TAG, "onDataRead: " + nn)
 //   mEdRead!!.setText(nn.toString())
//}
               fvalue3 = nn.toFloat()               // kawa Floatに変換して使う
            }

       //     if ((j % 16 == 0 ) || (fvalue3 != 0f)) {
            if (j % 40 == 0 ) {
            //    data.addEntry(Entry(fvalue, set.entryCount), 0)
                if (fvalue1 != 9999f) {
                    data.addEntry(Entry(fvalue1, set1.getEntryCount()), 0)
                }
                if (fvalue2 != 9999f) {
                    data.addEntry(Entry(fvalue2, set2.getEntryCount()), 1)
                }
                if (fvalue3 != 9999f) {
                    data2.addEntry(Entry(fvalue3, set3.getEntryCount()), 0)
                }


                     //   data.addEntry(Entry(fvalue/2, set2.entryCount), 0)

                        //  データを追加したら必ずよばないといけない
                mChart?.notifyDataSetChanged()
                mChart?.setVisibleXRangeMaximum(60f)
                mChart?.moveViewToX(data.xValCount - 61.toFloat()) //  移動する

                // kawa3
                mChart2?.notifyDataSetChanged()
                mChart2?.setVisibleXRangeMaximum(60f)
                mChart2?.moveViewToX(data2.xValCount - 61.toFloat()) //  移動する


            }
            j++

            //    Log.d(TAG, "onDataRead: " + i.toString())               // kawa これは動く
        }       // end of for
*/







        //    mEdRead!!.setText(Integer.toHexString(ByteBuffer.wrap(buffer).getInt()), TextView.BufferType.NORMAL)        // 下位2bir OK

        // -------------------------------------------------------------------------------------------
        //   val len = buffer.size
        //   var stmp =""
        // var stmp = buffer[1]
        //  Log.d(TAG, "onDataRead: " + stmp)
        //   mEdRead!!.setText(bytesToHex(buffer), TextView.BufferType.NORMAL)       // 停止する

        //   mEdRead!!.setText(bytesToHex(buffer), TextView.BufferType.NORMAL)       // 停止する

        //   mEdRead!!.append(bytesToHex(buffer))    // 停止する
        //   mEdRead!!.setText(bytesToHex(buffer))    // 停止する

        //    mEdRead!!.setText(Integer.toHexString(ByteBuffer.wrap(buffer).getInt()), TextView.BufferType.NORMAL)        // 下位2bir OK
        //   mEdRead!!.append(Integer.toHexString(ByteBuffer.wrap(buffer).getInt()))
        //    mEdRead!!.append(Integer.toHexString(ByteBuffer.wrap(buffer).getInt()))         // kawa 連続、下位2bit　NG
        //   mEdRead!!.append(Integer.toHexString(ByteBuffer.wrap(buffer).getInt()), 0, 4)          // kawa 停止する


        //   mEdRead!!.setText(Integer.toHexString(buffer.contentToString()), TextView.BufferType.NORMAL)

        //    mEdRead!!.setText(buffer.toString(), TextView.BufferType.NORMAL)      // kawa 単独行　「B@で始まる
        //   mEdRead!!.append(buffer.toString())             // kawa 連続だし　「B@で始まる



    }   // end of onDataRead()





// -----------------------------------------------------------------
// kawa これは作った関数

    fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v = (bytes[j] and 0xFF.toByte()).toInt()

            hexChars[j * 2] = hexArray[v ushr 4]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }
// --------------------------------------------------------------------







    override fun onDestroy() {
        super.onDestroy()
        mService!!.disconnect()
    }

    override fun onStatusChange(status: BluetoothStatus) {
        Log.d(TAG, "onStatusChange: $status")
    }

    override fun onDeviceName(deviceName: String) {
        Log.d(TAG, "onDeviceName: $deviceName")
    }

    override fun onToast(message: String) {
        Log.d(TAG, "onToast")
    }

    override fun onDataWrite(buffer: ByteArray) {
        Log.d(TAG, "onDataWrite")
        mEdRead!!.append("> " + String(buffer))
    }

    override fun onClick(v: View) {
        //       mWriter!!.writeln(mEdWrite!!.text.toString())
        //       mEdWrite!!.setText("")
    }

    companion object {
        private const val TAG = "DeviceActivity"
    }
}