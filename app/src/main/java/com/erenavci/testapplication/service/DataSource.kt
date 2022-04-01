package com.erenavci.testapplication.service

import com.erenavci.testapplication.model.Device
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class DataSource {
    companion object {
        private val DEVICE_LIST_URL: String = "https://veramobile.mios.com/test_android/items.test"

        fun retrieveDeviceList(): ArrayList<Device> {
            val inputStream: InputStream
            val url = URL(DEVICE_LIST_URL)

            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.connect()
            inputStream = connection.inputStream
            if (inputStream == null) {
                println("Http Error")
                return ArrayList()
            }

            val result: String = convertInputStreamToString(inputStream)
            return formatResponse(result)
        }

        private fun formatResponse(response: String): ArrayList<Device> {
            val jsonObject = JSONObject(response)
            val devicesObject: JSONArray = jsonObject.getJSONArray("Devices")
            val devices = ArrayList<Device>()
            for (i in 0..devicesObject.length() - 1) {
                val item: JSONObject = devicesObject.getJSONObject(i)
                val pkDevice = item.optInt("PK_Device")
                val macAddress = item.optString("MacAddress")
                val pkDeviceType = item.optInt("PK_DeviceType")
                val pkDeviceSubtType = item.optInt("PK_DeviceSubType")
                val firmware = item.optString("Firmware")
                val serverDevice = item.optString("Server_Device")
                val serverEvent = item.optString("Server_Event")
                val serverAccount = item.optString("Server_Account")
                val internalIP = item.optString("InternalIP")
                val lastAliveReported = item.optString("LastAliveReported")
                val platform = item.optString("Platform")
                val pkAccount = item.optInt("PK_Account")
                val device = Device(firmware, internalIP, lastAliveReported, macAddress,
                    pkAccount, pkDevice, pkDeviceSubtType, pkDeviceType, platform, serverAccount, serverDevice, serverEvent)
                devices.add(device)
            }
            devices.sortBy { it.PK_Device }

            return devices
        }

        private fun convertInputStreamToString(inputStream: InputStream): String {
            val bufferedReader: BufferedReader? = BufferedReader(InputStreamReader(inputStream))
            var line: String? = bufferedReader?.readLine()
            var result = ""
            while (line != null) {
                result += line
                line = bufferedReader?.readLine()
            }

            inputStream.close()
            return result
        }
    }
}