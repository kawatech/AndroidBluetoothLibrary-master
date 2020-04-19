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

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService.OnBluetoothEventCallback
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothWriter
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import java.lang.Math.abs
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import kotlin.experimental.and

/**
 * Created by douglas on 10/04/2017.
 */
 class DeviceActivity : AppCompatActivity(), OnBluetoothEventCallback, View.OnClickListener {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)

        // kawa Drawer ここの修正が必要
        //       val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
         //    setSupportActionBar(toolbar)


               // kawa Drawer これがあると停止する
               mToolbar = findViewById(R.id.toolbar)
        //      setSupportActionBar(mToolbar)

/* ----------------------------------------------------------------------------------------------
         Mainから秒数の設定データをもらう。キーワードがXSECでvaluew1に値が入る
         この値によって30秒ごとにX軸の幅を設定する。
 ------------------------------------------------------------------------------------------------- */
               val value1 = intent.getIntExtra("XSEC",1)

               if (value1 == 1 ) {
               mToolbar.title = "30sec"
                   xLenght = 1875f
               //    xLenght = 187f                 // 試しに幅を 1/10　にするとき
               }
               else if (value1 == 2) {
                   mToolbar.title = "60sec"
                   xLenght = 3750f
               }
               else if (value1 == 3) {
                   mToolbar.title = "90sec"
                   xLenght = 5625f
               }
               else if (value1 == 4) {
                   mToolbar.title = "120sec"
                   xLenght = 7500f
               }
               else if (value1 == 5) {
                   mToolbar.title = "150sec"
                   xLenght = 9375f
               }
               else if (value1 == 6) {
                   xLenght = 11250f
               }
               else if (value1 == 7) {
                   mToolbar.title = "210sec"
                   xLenght = 13125f
               }
               else if (value1 == 8) {
                       mToolbar.title = "15sec"
                       xLenght = 938f
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
        val btn1: Button = findViewById(R.id.monitor_btn)
            btn1.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
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
        mChart?.setBackgroundColor(Color.LTGRAY)
        val data = LineData()
        data.setValueTextColor(Color.BLACK)

        // add empty data
        mChart?.setData(data)

        //  ラインの凡例の設定
        val l = mChart?.getLegend()
        l?.form = Legend.LegendForm.LINE        // 凡例を線で表す
        l?.textColor = Color.BLACK

        val xl = mChart?.getXAxis()
        xl?.textColor = Color.BLACK
        xl?.setLabelsToSkip(9)

        xl?.isEnabled = false                   // falseのとき、上のラベルが表示されない

        val leftAxis = mChart?.getAxisLeft()
        leftAxis?.textColor = Color.BLACK
        leftAxis?.setAxisMaxValue( 5000.0f)             // Y軸の最大、最小
        leftAxis?.setAxisMinValue(-3.0f)

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
        mChart2?.setBackgroundColor(Color.LTGRAY)
        val data2 = LineData()                      // kawa2
        data2.setValueTextColor(Color.BLACK)

        // add empty data
        mChart2?.setData(data2)

        //  ラインの凡例の設定
        val l = mChart2?.getLegend()
        l?.form = Legend.LegendForm.LINE
        l?.textColor = Color.BLACK
        val xl = mChart2?.getXAxis()
        xl?.textColor = Color.BLACK
        xl?.setLabelsToSkip(9)

        xl?.isEnabled = false                   // falseのとき、上のラベルが表示されない

        val leftAxis = mChart2?.getAxisLeft()
        leftAxis?.textColor = Color.BLACK

     //   leftAxis?.setAxisMaxValue( 100.0f)
     //   leftAxis?.setAxisMinValue(-10.0f)

        leftAxis?.setAxisMaxValue( 20.0f)
        leftAxis?.setAxisMinValue(0.0f)


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
        var set3 = data2.getDataSetByIndex(0)

 //   }


        if (set1 == null) {
  //      set1 = createSet()
  //      set2 = createSet()
            set1 = LineDataSet(null, "データ1")
            set1.color = Color.BLUE
            set1.setDrawValues(false)
            set1.setDrawCircles(false)          // データの頂点の丸を描画しない
            data.addDataSet(set1)
        }

        if (set2 == null) {
            set2 = LineDataSet(null, "データ2")
            set2.color = Color.RED
            set2.setDrawValues(false)
            set2.setDrawCircles(false)
            data.addDataSet(set2)  /////
        }

        if (set3 == null) {
            set3 = LineDataSet(null, "データ3")        // kawa3
        //   set3.color = Color.GREEN                      // 緑
        //    set3.color = Color.BLACK
            //      set3.color = Color.MAGENTA                  // 桃色
      //      set3.color = Color.CYAN                     // 空色 //
            //      set3.color = Color.parseColor("#008577")        // 濃い緑
            set3.color = Color.parseColor("#d2691e")    //チョコレート色

            set3.setDrawValues(false)
            set3.setDrawCircles(false)
            data2.addDataSet(set3)
        }


        val date = Date()
        val format = SimpleDateFormat("HH:mm:ss")
        data.addXValue(format.format(date))
        data2.addXValue(format.format(date))

    // 初期値を設定しておく
        var fvalue1 = 5000f
        var fvalue2 = 5000f
        var fvalue3 = 4500f
        var workBuffer = ByteArray(80)

       var tmpOffset = 0
        val ll = buffer.size

/* -----------------------------------------------------------
   下2ビットが00までのオフセットを求める
   tmpOffsetにいれて、画面上面に表示する
   ----------------------------------------------------------- */

       for (i in 0..10) {
           // 連続した2バイトの下2ビットが00
           tmpOffset++
           if (((buffer[i] and (0x03)) == 0x00.toByte()) and ((buffer[i + 1] and (0x03)) == 0x00.toByte())) {
           //    offset = 5- (i % 5)
           //    tmpOffset = i

            //   if((tmpOffset == 5) or (tmpOffset == 0)) {
            //       offset = 0
            //   }
               break
           }
       }
    tmpOffset--
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
    fvalue3 = tmpOffset.toFloat()          // 00 00 のオフセットを出力する


        // 赤のラインで12ビットの範囲の値でなければ、表示しない。これで見かけ上不連続なし
        // ただし、サンプル1回分抜けるので、よく見ると段差が見える
  //       if (fvalue2 <= 4095) {
             data.addEntry(Entry(fvalue1, set1.getEntryCount()), 0)
             data.addEntry(Entry(fvalue2, set2.getEntryCount()), 1)
             data2.addEntry(Entry(fvalue3, set3.getEntryCount()), 0)

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
  //       }


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