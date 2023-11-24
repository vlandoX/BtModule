package com.example.bt_def

import android.bluetooth.BluetoothDevice

data class BtDevice(
    val device: BluetoothDevice,
    val isChecked: Boolean
)
