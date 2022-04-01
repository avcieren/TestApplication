package com.erenavci.testapplication.view

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.erenavci.testapplication.R
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import java.net.URL


class DetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        val platform = intent.getStringExtra("platform")
        val firmware = intent.getStringExtra("firmware")
        val macAddress = intent.getStringExtra("macAddress")
        val imageUrl = intent.getStringExtra("image")
        val pkDevice = intent.getIntExtra("pkDevice", 0)
        val isEditable = intent.getBooleanExtra("isEditable", false)
        println("eren :: ${imageUrl}")
        text_details_firmware.setText(firmware)
        text_details_mac_address.setText(macAddress)
        text_details_model.setText(platform)
        text_details_pk_device.setText(pkDevice.toString())
        text_details_platform.setText(platform)
        text_details_editable_platform.setText(platform)

        if (isEditable) {
            text_details_editable_platform.visibility = View.VISIBLE
            text_details_platform.visibility = View.INVISIBLE
        } else {
            text_details_editable_platform.visibility = View.INVISIBLE
            text_details_platform.visibility = View.VISIBLE
        }
        val adaptor = MainActivity.getRecyclerViewAdapter()
        text_details_editable_platform.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                adaptor.editDeviceByDeviceId(pkDevice, s.toString())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
            }
        })

        GlobalScope.launch {
            val url = URL(imageUrl)
            val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())
            withContext(Dispatchers.Main) {
                image_details_device.setImageBitmap(bmp)
            }
        }
    }
}