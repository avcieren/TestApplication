package com.erenavci.testapplication.model

data class Device(
    val Firmware: String,
    val InternalIP: String,
    val LastAliveReported: String,
    val MacAddress: String,
    val PK_Account: Int,
    var PK_Device: Int,
    val PK_DeviceSubType: Int,
    val PK_DeviceType: Int,
    var Platform: String,
    val Server_Account: String,
    val Server_Device: String,
    val Server_Event: String
)