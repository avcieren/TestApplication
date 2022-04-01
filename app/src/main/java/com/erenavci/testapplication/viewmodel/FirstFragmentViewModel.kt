package com.erenavci.testapplication.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.erenavci.testapplication.model.Device
import com.erenavci.testapplication.model.Model

class FirstFragmentViewModel: ViewModel() {
    val devices = MutableLiveData <List<Model>>()

    fun refreshData() {
        val device = Device("firm","ip","lap","mcAd",10,
            1,2,3,"test",
            "str","devc","event")
        val devices1 =  arrayListOf<Device>(device)
        devices.value = listOf(Model(devices1))
    }
}