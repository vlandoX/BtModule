package com.example.bluetoothmodule

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.bluetoothmodule.databinding.ActivityStartBinding
import com.example.bluetoothmodule.databinding.FragmentMainBinding

class StartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStartBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater).also { setContentView(it.root) }
    }
}