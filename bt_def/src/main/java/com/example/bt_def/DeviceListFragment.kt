package com.example.bt_def

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.Manifest
import android.content.*
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.bt_def.databinding.FragmentBtListBinding
import com.google.android.material.snackbar.Snackbar

const val APP_PREFERENCES = "APP_PREFERENCES"
const val PREF_MAC_VALUE = "PREF_MAC_VALUE"

class DeviceListFragment : Fragment(), BtItemAdapter.Listener {
    private var _binding: FragmentBtListBinding? = null
    private val binding get() = _binding!!

    private lateinit var btItemAdapter: BtItemAdapter
    private lateinit var discoveryAdapter: BtItemAdapter
    private var btAdapter: BluetoothAdapter? = null
    private var sharedPreferences: SharedPreferences? = null
    private lateinit var btLauncher: ActivityResultLauncher<Intent>
    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentBtListBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = activity?.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)

        binding.imBluetoothOn.setOnClickListener {
            btLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
        binding.imBluetoothSearch.setOnClickListener {
            try{
                //проверяем включен ли блутуз чтобы избежать постоянную работу ProgressBar
                if(btAdapter?.isEnabled == true){
                    btAdapter?.startDiscovery()
                    it.visibility = View.GONE
                    binding.pbSearch.visibility = View.VISIBLE
                }
            } catch (e: SecurityException){
            }
        }

        intentFilters()
        checkPermissions()
        initRcView()
        registerBtLauncher()
        initBtAdapter()
        bluetoothState()
    }

    private fun initRcView() {
        btItemAdapter = BtItemAdapter(this@DeviceListFragment, false)
        binding.rcViewPaired.adapter = btItemAdapter

        //Recycler для нижнего списка найденных блтз лежащих устройств
        discoveryAdapter = BtItemAdapter(this@DeviceListFragment, true)
        binding.rcViewSearch.adapter = discoveryAdapter
    }

    private fun initBtAdapter() {
        val bManager = activity?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btAdapter = bManager.adapter
    }

    //получаем разрешение на вкл блютуз. Если пользователь согласился, то
    private fun registerBtLauncher() {
        btLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                changeButtonIcon(binding.imBluetoothOn,R.drawable.ic_bt_disable_24)
                getPairedDevices()
                Snackbar.make(binding.root, "Блютуз включен!", Snackbar.LENGTH_LONG).show()
            } else {
                // TODO(при выключении блютуз прятать споряженные устройисва и отображать надпись "Пусто")
                Snackbar.make(binding.root, "Блютуз выключен!", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    // Получаем сопряженные устройств, вытягивая из них имя и мак адресс, после чего записываем данные в BtDevice и закидываем в ArrayList
    private fun getPairedDevices() {
        try {
            val list = ArrayList<BtDevice>()
            val deviceList = btAdapter?.bondedDevices as Set<BluetoothDevice>
            deviceList.forEach {
                list.add(
                    BtDevice(
                        it,
                        //проверяет есть из получаемых сопряж устройств, то у которого раннее был сохранен MAC-адресс, если ДА то True у чек бокс будет
                        sharedPreferences?.getString(PREF_MAC_VALUE, "") == it.address
                    )
                )
            }
            //прячем надпись "Пусто", если в в ArrayList не пуст
            binding.tvEmptyPaired.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            //передаем адаптеру обновленнй список
            btItemAdapter.submitList(list)
        } catch (e: SecurityException) {
            e.stackTrace
        }
    }

    /*меняет сосотяние кнопки блютуз в зависимости от того включен блютуз или нет.
     Блютуз включен -> ImageView зачерктнутое (чтобы при нажатии снова он отлючил блютуз)*/
    private fun bluetoothState() {
        if (btAdapter?.isEnabled == true) {
            changeButtonIcon(binding.imBluetoothOn,R.drawable.ic_bt_disable_24)
            getPairedDevices()
        } else {
            // TODO(при выключении блютуз прятать споряженные устройисва и отображать надпись "Пусто")
            //binding.imBluetoothOn.setImageResource(R.drawable.ic_bt_enable_24)
            changeButtonIcon(binding.imBluetoothOn, R.drawable.ic_bt_enable_24)
        }
    }

    private fun saveMac(mac: String) {
        sharedPreferences?.edit()
            ?.putString(PREF_MAC_VALUE, mac)
            ?.apply()

    }

    private fun checkPermissions(){
        if(!checkBtPermissions()){
            registerPermissionListener()
            launchBtPermissions()
        }
    }

    private fun launchBtPermissions(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            pLauncher.launch(arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            ))
        } else {
            pLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            ))
        }
    }

    private fun registerPermissionListener(){
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ){

        }
    }

    override fun onClick(item: BtDevice) {
        saveMac(item.device.address)
    }

    private val btReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, intent: Intent?) {
            if(intent?.action == BluetoothDevice.ACTION_FOUND){
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val list = mutableSetOf<BtDevice>()
                list.addAll(discoveryAdapter.currentList)
                if(device != null) list.add(BtDevice(device, false))
                discoveryAdapter.submitList(list.toList())
                binding.tvEmptySearch.visibility = if(list.isEmpty()) View.VISIBLE else View.GONE
                try{
                    Log.d("MyLog", "Device: ${device?.name}")
                } catch (e: SecurityException){

                }
            } else if(intent?.action == BluetoothDevice.ACTION_BOND_STATE_CHANGED){ //если проийзойдёт доб нового девайса в список соряжения то здесь обновим сего
                getPairedDevices()

            } else if(intent?.action == BluetoothAdapter.ACTION_DISCOVERY_FINISHED){
                binding.pbSearch.visibility = View.GONE
                binding.imBluetoothSearch.visibility = View.VISIBLE
            }
        }
    }

    private fun intentFilters(){
        //ждет найденные устройства
        val f1 = IntentFilter(BluetoothDevice.ACTION_FOUND)
        //изм сост сопряжения
        val f2 = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        //окончание поиска близ леж устройст (использ для progressBar)
        val f3 = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        activity?.registerReceiver(btReceiver, f1)
        activity?.registerReceiver(btReceiver, f2)
        activity?.registerReceiver(btReceiver, f3)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}