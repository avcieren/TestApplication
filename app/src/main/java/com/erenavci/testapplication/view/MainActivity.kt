package com.erenavci.testapplication.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.erenavci.testapplication.R
import com.erenavci.testapplication.service.DataSource
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    companion object {
        private lateinit var recyclerAdaptor: DeviceRecyclerAdaptor
        fun getRecyclerViewAdapter(): DeviceRecyclerAdaptor {
            return recyclerAdaptor
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initRecyclerView()
        loadDevicesToRecyclerView()
    }

    private fun initRecyclerView() {
        val densityDpi = resources.displayMetrics.densityDpi
        recycler_view.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            recyclerAdaptor = DeviceRecyclerAdaptor()
            recyclerAdaptor.setContext(this@MainActivity)
            recyclerAdaptor.setDeviceDensity(densityDpi)
            adapter = recyclerAdaptor
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun loadDevicesToRecyclerView() {
        GlobalScope.launch {
            val devices = DataSource.retrieveDeviceList()
            println("eren :: ${devices.size}")
            recyclerAdaptor.submitList(devices)
            withContext(Dispatchers.Main) {
                recyclerAdaptor.notifyDataSetChanged()
            }
        }
    }
}