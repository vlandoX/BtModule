package com.example.bluetoothmodule

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.navigation.fragment.findNavController
import com.example.bluetoothmodule.databinding.FragmentMainBinding
import com.example.bt_def.APP_PREFERENCES
import com.example.bt_def.PREF_MAC_VALUE
import com.example.bt_def.bluetooth.BluetoothController
import com.example.bt_def.bluetooth.BluetoothController.Companion.BLUETOOTH_CONNECTED
import com.example.bt_def.bluetooth.BluetoothController.Companion.BLUETOOTH_NO_CONNECTED


class MainFragment : Fragment(), BluetoothController.Listener {

    private lateinit var binding: FragmentMainBinding
    private lateinit var bluetoothController: BluetoothController
    private lateinit var btAdapter: BluetoothAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBtAdapter()
        val pref = activity?.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        val macAddress = pref?.getString(PREF_MAC_VALUE, "")
        bluetoothController = BluetoothController(btAdapter)
        binding.bList.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_deviceListFragment)
        }
        binding.bConnect.setOnClickListener {
            bluetoothController.connect(macAddress ?: "", this)
        }
    }

    private fun initBtAdapter(){
        val bManager = activity?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btAdapter = bManager.adapter
    }

    override fun onReceive(message: String) {
        activity?.runOnUiThread {
            when(message){
                BLUETOOTH_CONNECTED -> {
                    binding.bConnect.backgroundTintList = AppCompatResources
                        .getColorStateList(requireContext(), R.color.red)
                    binding.bConnect.text = "Disconnect"

                }
                BLUETOOTH_NO_CONNECTED -> {
                    binding.bConnect.backgroundTintList = AppCompatResources
                        .getColorStateList(requireContext(), R.color.green)
                    binding.bConnect.text = "Connect"
                }
                else -> {
                    binding.tvStatus.text = message
                }
            }
        }
    }


}