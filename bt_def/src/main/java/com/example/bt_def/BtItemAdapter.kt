package com.example.bt_def

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bt_def.databinding.ListItemBinding


class BtItemAdapter(private val listener: Listener, val adapterType: Boolean) :
    ListAdapter<BtDevice, BtItemAdapter.BtItemViewHolder>(Comparator()) {
    private var oldCheckBox: CheckBox? = null

    class BtItemViewHolder(
        private val binding: ListItemBinding,
        private val adapter: BtItemAdapter,
        private val listener: Listener,
        val adapterType: Boolean
    ) : RecyclerView.ViewHolder(binding.root) {
        //private val binding = ListItemBinding.bind(view)
        private var btDevice: BtDevice? = null

        init {
            binding.checkBox.setOnClickListener {
                btDevice?.let { it1 -> listener.onClick(it1) }
                adapter.selectCheckBox(it as CheckBox)
            }

            itemView.setOnClickListener {
                //если в арг в объекта BtItemAdapter предать true, то делаем возможным сопряжение false
                if(adapterType){
                    try {
                        //создает + запрашивает сопряжение
                        btDevice?.device?.createBond()
                    } catch (e: SecurityException){}
                } else {
                    btDevice?.let { it1 -> listener.onClick(it1) }
                    adapter.selectCheckBox(binding.checkBox)
                }
            }
        }

        fun bind(item: BtDevice) = with(binding) {
            // если в арг в объекта BtItemAdapter предать true => прячем кнопку чекбокс тк для нижнего RC он не нужен
            checkBox.visibility = if (adapterType) View.GONE else View.VISIBLE
            btDevice = item
            try {
                name.text = item.device.name
                mac.text = item.device.address
            } catch (e: SecurityException){}
            //достав из sharedPref и передав Лист в адаптер мы проверяем есть ли отмеченный и тогда вызываем метод иммитируя нажатия что бы появл галка
            if(item.isChecked) adapter.selectCheckBox(checkBox)
        }
    }

    fun selectCheckBox(newCheckBox: CheckBox) {
        oldCheckBox?.isChecked = false
        oldCheckBox = newCheckBox
        oldCheckBox?.isChecked = true
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BtItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListItemBinding.inflate(layoutInflater, parent, false)
        /*val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)*/
        return BtItemViewHolder(binding, this, listener, adapterType)
    }

    override fun onBindViewHolder(holder: BtItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class Comparator : DiffUtil.ItemCallback<BtDevice>() {
        override fun areItemsTheSame(oldItem: BtDevice, newItem: BtDevice) = (oldItem == newItem)

        override fun areContentsTheSame(oldItem: BtDevice, newItem: BtDevice) = (oldItem == newItem)
    }

    interface Listener {
        fun onClick(device: BtDevice)
    }
}