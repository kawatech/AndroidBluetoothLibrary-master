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
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)

        //--------------- kawa2 ---------------------------
            mChart = findViewById(R.id.chart) as LineChart
           initChart()

            mChart2 = findViewById(R.id.chart2) as LineChart            // kawa3
            initChart2()

        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
 //       mFab = findViewById<View>(R.id.fab) as FloatingActionButton
 //       mFab!!.setOnClickListener(this)
  //      mEdRead = findViewById<View>(R.id.ed_read) as EditText
        mEdRead = findViewById<View>(R.id.ed_read) as TextView                  // kawa TextView に変更

 //       mEdWrite = findViewById<View>(R.id.ed_write) as EditText
        mService = BluetoothService.getDefaultInstance()
        mWriter = BluetoothWriter(mService)

        //★★★ ソフトキーボードを隠す。
     //   val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
     //   inputMethodManager.hideSoftInputFromWindow(mEdWrite?.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)


    }

    override fun onResume() {
        super.onResume()
        mService!!.setOnEventCallback(this)

    }




    // ------------------------------------ kawa2 -----------------

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
        l?.form = Legend.LegendForm.LINE
        l?.textColor = Color.BLACK
        val xl = mChart?.getXAxis()
        xl?.textColor = Color.BLACK
        xl?.setLabelsToSkip(9)
        val leftAxis = mChart?.getAxisLeft()
        leftAxis?.textColor = Color.BLACK
    //    leftAxis?.axisMaxValue = 5000.0f                // kawa2
    //    leftAxis?.axisMinValue = -3.0f
        leftAxis?.setAxisMaxValue( 5000.0f)
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
        val leftAxis = mChart2?.getAxisLeft()
        leftAxis?.textColor = Color.BLACK
        //    leftAxis?.axisMaxValue = 5000.0f                // kawa2
        //    leftAxis?.axisMinValue = -3.0f
        leftAxis?.setAxisMaxValue( 100.0f)
        leftAxis?.setAxisMinValue(-3.0f)

        //     leftAxis?.setStartAtZero(false)
        leftAxis?.setDrawGridLines(true)
        val rightAxis = mChart2?.getAxisRight()
        rightAxis?.isEnabled = false
    }
// -------------------------------------------------------------------------





// ----------------------------------------------------------------------------
    // ここが受信データが入ったときに実行される関数、この中で処理する
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
        var set1 = data.getDataSetByIndex(0)
        //2本目のグラフ（インデックスを1に）
        var set2 = data.getDataSetByIndex(1)       /////
        var set3 = data2.getDataSetByIndex(0)

 //   }


        if (set1 == null) {
    //    set = LineDataSet(null, "サンプルデータ")
    //    set.color = Color.BLUE
    //    set.setDrawValues(false)
    //    data.addDataSet(set)


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
            set3.color = Color.GREEN
            set3.setDrawValues(false)
            set3.setDrawCircles(false)
            data2.addDataSet(set3)
        }

        val date = Date()
        val format = SimpleDateFormat("HH:mm:ss")
        data.addXValue(format.format(date))
        data2.addXValue(format.format(date))

        var fvalue1 = 9999f
        var fvalue2 = 9999f
        var fvalue3 = 9999f

        var j = 0
        val ll = buffer.size
        for( i in 0..ll-2 ){
         //   Log.d(TAG, "onDataRead: " + (buffer[i] and 0x03))       // これで、0, 0, 1, 1, 3の繰り返しOK
        //    mEdRead!!.setText((buffer[i] and 0x03).toString())              // 0, 1, 3の繰り返し、連続表示はない

            // 連続した2バイトの下2ビットが00
            if (((buffer[i] and 0x03) == 0x00.toByte()) and ((buffer[i+1] and 0x03) == 0x00.toByte())) {
                var v = buffer[i + 1].toInt() and 0xFC
                var u = buffer[i].toInt() and 0xFC
                var nn = u.shl(4) + v.ushr(2)

           //     Log.d(TAG, "onDataRead: " + nn)           //  ここで書くようにLogが多くなると描画が遅くなる
          //      Log.d(TAG, "onDataRead: " + i)
                mEdRead!!.setText(nn.toString())

                fvalue1 = nn.toFloat()               // kawa Floatに変換して使う
            }

            // 連続した2バイトの下2ビットが01
            if (((buffer[i] and 0x03) == 0x01.toByte()) and ((buffer[i+1] and 0x03) == 0x01.toByte())) {
                var v = buffer[i + 1].toInt() and 0xFC
                var u = buffer[i].toInt() and 0xFC
                var nn = u.shl(4) + v.ushr(2)

                fvalue2 = nn.toFloat()               // kawa Floatに変換して使う
            }

            // 1バイトの下2ビットが11
            if ((buffer[i] and 0x03) == 0x03.toByte())  {
                var u = buffer[i].toInt() and 0xFC
                var nn = u.ushr(2)

 //   if(nn != 0) {
 //   Log.d(TAG, "onDataRead: " + nn)
 //   mEdRead!!.setText(nn.toString())
//}
               fvalue3 = nn.toFloat()               // kawa Floatに変換して使う
            }

       //     if ((j % 16 == 0 ) || (fvalue3 != 0f)) {
            if (j % 16 == 0 ) {
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