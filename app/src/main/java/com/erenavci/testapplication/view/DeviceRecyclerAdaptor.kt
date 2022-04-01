package com.erenavci.testapplication.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.erenavci.testapplication.R
import com.erenavci.testapplication.model.Device
import kotlinx.android.synthetic.main.list_row.view.*


class DeviceRecyclerAdaptor : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var devices: List<Device> = ArrayList()
    private var densityDpi: Int = 0
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return DeviceViewHolder(
            this,
            LayoutInflater.from(parent.context).inflate(R.layout.list_row, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is DeviceViewHolder -> {
                holder.bind(devices.get(position), context, densityDpi)
            }
        }
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    fun setDeviceDensity(density: Int) {
        densityDpi = density
    }

    fun submitList(deviceList: List<Device>){
        devices = deviceList
    }

    fun removeDevice(device: Device) {
        val copyDevices = ArrayList(devices)
        copyDevices.removeAt(copyDevices.indexOf(device))
        devices = copyDevices
    }

    fun editDeviceByDeviceId(deviceId: Int, platform: String) {
        val copyDevices = ArrayList(devices)
        for (i in copyDevices.indices) {
            if (copyDevices[i].PK_Device == deviceId) {
                copyDevices[i].Platform = platform
            }
        }
        devices = copyDevices
        this.notifyDataSetChanged()
    }

    fun setContext(appContext: Context) {
        context = appContext
    }

    class DeviceViewHolder(
        adapter: DeviceRecyclerAdaptor,
        itemView: View
    ): RecyclerView.ViewHolder(itemView) {
        private val IMAGE_URL_HOST: String = "https://veramobile.mios.com/test_android/"
        val imageDeviceView = itemView.image_device
        val homeNumberTextField = itemView.text_home_number
        val snNumberTextField = itemView.text_SN
        val recyclerAdaptor = adapter

        fun bind(device: Device, context: Context, density: Int) {

            val requestOptions = RequestOptions()
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)

            val imageUrl = selectImage(device.Platform, density)
            Glide.with(itemView.context)
                .applyDefaultRequestOptions(requestOptions)
                .load(imageUrl)
                .into(imageDeviceView)
            homeNumberTextField.setText(device.Platform)
            snNumberTextField.setText(device.PK_Device.toString())
            itemView.setOnClickListener {
                onClickItem(device, context, imageUrl, false)
            }
            itemView.setOnLongClickListener {
                onLongClickItem(device, context, recyclerAdaptor)
                true
            }
            itemView.image_arrow_right.setOnClickListener {
                onClickItem(device, context, imageUrl, true)
            }
        }

        fun onClickItem(device: Device, context: Context, imageUrl: String, editable: Boolean) {
            println("eren click")
            val activity = Intent(context, DetailsActivity::class.java)
            activity.putExtra("firmware", device.Firmware)
            activity.putExtra("macAddress", device.MacAddress)
            activity.putExtra("platform", device.Platform)
            activity.putExtra("pkDevice", device.PK_Device)
            activity.putExtra("image", imageUrl)
            activity.putExtra("isEditable", editable)
            startActivity(context, activity, Bundle())
        }

        fun onLongClickItem(device: Device, context: Context, recyclerAdaptor: DeviceRecyclerAdaptor) {
            val dialogBuilder = AlertDialog.Builder(context)


            dialogBuilder.setMessage("Do you want to delete this row?")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, id ->
                    recyclerAdaptor.removeDevice(device)
                    recyclerAdaptor.notifyDataSetChanged()
                }
                .setNegativeButton("Cancel") { dialog, id ->
                    dialog.cancel()
                }


            val alert = dialogBuilder.create()

            alert.setTitle("Warning")

            alert.show()
        }

        fun selectImage(platform: String, density: Int): String {
            var folder = when (density) {
                DisplayMetrics.DENSITY_XXXHIGH -> "drawable-xxxhdpi"
                DisplayMetrics.DENSITY_XXHIGH -> "drawable-xxhdpi"
                DisplayMetrics.DENSITY_XHIGH -> "drawable-xhdpi"
                else -> "drawable-hdpi"
            }
            var image = when (platform) {
                "Sercomm G450" -> "vera_plus_big.png"
                "Sercomm G550" -> "vera_secure_big.png"
                "MiCasaVerde VeraLite" -> "vera_edge_big.png"
                "Sercomm NA900" -> "vera_edge_big.png"
                "Sercomm NA301" -> "vera_edge_big.png"
                "Sercomm NA930" -> "vera_edge_big.png"
                else -> "vera_edge_big.png"
            }

            return "$IMAGE_URL_HOST$folder/$image"
        }

    }
}