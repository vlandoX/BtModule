package com.example.bt_def.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import java.io.IOException
import java.util.*

class ConnectThread(device: BluetoothDevice): Thread() {

    private val uuid = ""
    private var mSocket: BluetoothSocket? = null
    init {
        try {
            //открываем канал соединения
            mSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(uuid))
        } catch (e: IOException){

        } catch (se: SecurityException){

        }
    }

    override fun run() {
        try {
            mSocket?.connect()
        } catch (e: IOException){
            //если произощел разрыв соединение или не удалось
        } catch (se: SecurityException){

        }
    }

    fun closeConnection(){
        try {
            mSocket?.close()
        } catch (e: IOException){

        }
    }
}