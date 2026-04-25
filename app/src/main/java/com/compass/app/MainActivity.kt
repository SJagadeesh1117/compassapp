package com.compass.app

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.compass.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val sensorViewModel: SensorViewModel by viewModels()

    private val compassFragment    = CompassFragment()
    private val roomCheckFragment  = RoomCheckFragment()
    private val vastuGuideFragment = VastuGuideFragment()
    private var activeFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showFragment(compassFragment)

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_compass -> { showFragment(compassFragment);    true }
                R.id.nav_room    -> { showFragment(roomCheckFragment);  true }
                R.id.nav_guide   -> { showFragment(vastuGuideFragment); true }
                else             -> false
            }
        }
    }

    private fun showFragment(fragment: Fragment) {
        val tx = supportFragmentManager.beginTransaction()
        if (!fragment.isAdded) tx.add(R.id.fragmentContainer, fragment)
        activeFragment?.let { tx.hide(it) }
        tx.show(fragment)
        activeFragment = fragment
        tx.commit()
    }

    override fun onResume()  { super.onResume();  sensorViewModel.registerSensors() }
    override fun onPause()   { super.onPause();   sensorViewModel.unregisterSensors() }
}
