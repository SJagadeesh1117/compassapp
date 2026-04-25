package com.compass.app

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.compass.app.databinding.FragmentCompassBinding
import kotlin.math.roundToInt

class CompassFragment : Fragment() {

    private var _binding: FragmentCompassBinding? = null
    private val binding get() = _binding!!
    private val sensorViewModel: SensorViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCompassBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sensorViewModel.azimuth.observe(viewLifecycleOwner) { azimuth ->
            updateDisplay(azimuth)
        }
    }

    private fun updateDisplay(azimuth: Float) {
        binding.compassView.azimuth = azimuth

        val names = VastuData.getNames(azimuth)
        binding.tvEnglish.text = names.english
        binding.tvTelugu.text  = names.telugu
        binding.tvTamil.text   = names.tamil

        val code   = VastuData.getCode(azimuth)
        val result = VastuData.mainEntranceVastu[code]!!
        val color  = Color.parseColor(result.rating.colorHex)

        binding.tvVastuStars.text      = result.rating.stars
        binding.tvVastuStars.setTextColor(color)
        binding.tvVastuLabel.text      = "  ${result.rating.label}"
        binding.tvVastuLabel.setTextColor(color)
        binding.tvVastuAdvice.text     = result.advice
        binding.vastuIndicator.setBackgroundColor(color)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
