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

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast

import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothDeviceDecorator
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus

import java.util.Arrays
//--------------------------------------------------------- kawa2
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet


// kawa NavigationView.を追加した

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
        BluetoothService.OnBluetoothScanCallback, BluetoothService.OnBluetoothEventCallback,
        DeviceItemAdapter.OnAdapterItemClickListener {
 //   private var mChart: LineChart? = null           // kawa2
    private var pgBar: ProgressBar? = null
    private var mMenu: Menu? = null
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: DeviceItemAdapter? = null

    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mService: BluetoothService? = null
    private var mScanning: Boolean = false

    private lateinit var mToolbar: Toolbar              // kawa Drawer
    private var mGenre = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // original
  //      val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
 //       setSupportActionBar(toolbar)




        // kawa Drawer
        mToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(mToolbar)

// kawa Drawer ナビゲーションドロワーの設定
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(this, drawer, mToolbar, R.string.app_name, R.string.app_name)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)




        //--------------- kawa2 ---------------------------
    //    mChart = findViewById(R.id.chart) as LineChart
     //   initChart()


// kawa Drawer 小駒マスクする、上の mToolbar を使う
   //     val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
  //      setSupportActionBar(toolbar)

        pgBar = findViewById<View>(R.id.pg_bar) as ProgressBar
        pgBar!!.visibility = View.GONE

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        mRecyclerView = findViewById<View>(R.id.rv) as RecyclerView
        val lm = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mRecyclerView!!.layoutManager = lm

        mAdapter = DeviceItemAdapter(this, mBluetoothAdapter!!.bondedDevices)
        mAdapter!!.setOnAdapterItemClickListener(this)
        mRecyclerView!!.adapter = mAdapter

        mService = BluetoothService.getDefaultInstance()

        mService!!.setOnScanCallback(this)
        mService!!.setOnEventCallback(this)

    }


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
        } else if (id == R.id.nav_150s) {
            mToolbar.title = "150sec"
            mGenre = 5
        } else if (id == R.id.nav_180s) {
            mToolbar.title = "90sec"
            mGenre = 6
        } else if (id == R.id.nav_210s) {
            mToolbar.title = "210sec"
            mGenre = 7
        }

     //   intent.putExtra("XSEC", mGenre)             // データを渡す

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }







    override fun onResume() {
        super.onResume()
        mService!!.setOnEventCallback(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        mMenu = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_scan) {
            startStopScan()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun startStopScan() {
        if (!mScanning) {
            mService!!.startScan()
        } else {
            mService!!.stopScan()
        }
    }

    override fun onDeviceDiscovered(device: BluetoothDevice, rssi: Int) {
        Log.d(TAG, "onDeviceDiscovered: " + device.name + " - " + device.address + " - " + Arrays.toString(device.uuids))
        val dv = BluetoothDeviceDecorator(device, rssi)
        val index = mAdapter!!.devices.indexOf(dv)
        if (index < 0) {
            mAdapter!!.devices.add(dv)
            mAdapter!!.notifyItemInserted(mAdapter!!.devices.size - 1)
        } else {
            mAdapter!!.devices[index].device = device
            mAdapter!!.devices[index].rssi = rssi
            mAdapter!!.notifyItemChanged(index)
        }
    }

    override fun onStartScan() {
        Log.d(TAG, "onStartScan")
        mScanning = true
        pgBar!!.visibility = View.VISIBLE
        mMenu!!.findItem(R.id.action_scan).setTitle(R.string.action_stop)
    }

    override fun onStopScan() {
        Log.d(TAG, "onStopScan")
        mScanning = false
        pgBar!!.visibility = View.GONE
        mMenu!!.findItem(R.id.action_scan).setTitle(R.string.action_scan)
    }

    override fun onDataRead(buffer: ByteArray, length: Int) {
  //      Log.d(TAG, "onDataRead")
    }

    override fun onStatusChange(status: BluetoothStatus) {
     //   Log.d(TAG, "onStatusChange: $status")                     // kawa4
        Toast.makeText(this, status.toString(), Toast.LENGTH_SHORT).show()

        if (status == BluetoothStatus.CONNECTED) {

            val builder = AlertDialog.Builder(this)
            // ここで画面を替える
            val intent = Intent(this@MainActivity, DeviceActivity::class.java)

            intent.putExtra("XSEC", mGenre)             // データを渡す, startAvtivity()の前にやる
            startActivity(intent)
            builder.setCancelable(false)
            builder.show()
        }
            // kawa4 画像の選択はマスクした
            /*
            val colors = arrayOf<CharSequence>("Try text", "Try picture")

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Select")
            builder.setItems(colors) { dialog, which ->
                if (which == 0) {
                    startActivity(Intent(this@MainActivity, DeviceActivity::class.java))
                } else {
                    startActivity(Intent(this@MainActivity, BitmapActivity::class.java))
                }
            }
            builder.setCancelable(false)
            builder.show()
        } */

    }

    override fun onDeviceName(deviceName: String) {
        Log.d(TAG, "onDeviceName: $deviceName")
    }

    override fun onToast(message: String) {
        Log.d(TAG, "onToast")
    }

    override fun onDataWrite(buffer: ByteArray) {
        Log.d(TAG, "onDataWrite")
    }

    override fun onItemClick(device: BluetoothDeviceDecorator, position: Int) {
        mService!!.connect(device.device)
    }

    companion object {

        val TAG = "BluetoothExampleKotlin"
    }
}
