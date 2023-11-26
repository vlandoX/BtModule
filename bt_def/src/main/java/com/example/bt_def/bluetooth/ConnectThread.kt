package com.example.bt_def.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.example.bt_def.bluetooth.BluetoothController.Companion.BLUETOOTH_CONNECTED
import com.example.bt_def.bluetooth.BluetoothController.Companion.BLUETOOTH_NO_CONNECTED
import java.io.IOException
import java.util.*

class ConnectThread(device: BluetoothDevice, val listener: BluetoothController.Listener) :
    Thread() {

    private val uuid = "00001101-0000-1000-8000-00805F9B34FB"
    private var mSocket: BluetoothSocket? = null

    init {
        try {
            //открываем канал соединения
            mSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(uuid))
        } catch (e: IOException) {

        } catch (se: SecurityException) {

        }
    }

    override fun run() {
        try {
            mSocket?.connect()
            listener.onReceive(BLUETOOTH_CONNECTED)
            readMessage()
        } catch (e: IOException) {
            //если произощел разрыв соединение или не удалось
            listener.onReceive(BLUETOOTH_NO_CONNECTED)
        } catch (se: SecurityException) {

        }
    }

    private fun readMessage() {
        val buffer = ByteArray(256)
        while (true) {
            try {
                //считываем данный в buffer и получаем его длинну на сколько заполенен массив
                val length = mSocket?.inputStream?.read(buffer)
                val message = String(buffer, 0, length ?: 0)
                listener.onReceive(message)
            } catch (e: IOException) {
                listener.onReceive(BLUETOOTH_NO_CONNECTED)
                //выходим в случае разрыва соединения
                break
            }
        }
    }

    fun sendMessage(message: String){
        mSocket?.outputStream?.write(message.toByteArray())
    }

    fun closeConnection() {
        try {
            mSocket?.close()
        } catch (e: IOException) {

        }
    }
}