package com.compass.app

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.chip.Chip
import com.compass.app.databinding.FragmentRoomCheckBinding

class RoomCheckFragment : Fragment() {

    private var _binding: FragmentRoomCheckBinding? = null
    private val binding get() = _binding!!
    private val sensorViewModel: SensorViewModel by activityViewModels()

    private var selectedRoomIndex = 0
    private var lastAzimuth = 0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRoomCheckBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buildRoomChips()
        sensorViewModel.azimuth.observe(viewLifecycleOwner) { azimuth ->
            lastAzimuth = azimuth
            updateDisplay(azimuth)
        }
    }

    private fun buildRoomChips() {
        VastuData.roomRules.forEachIndexed { index, room ->
            val chip = Chip(requireContext()).apply {
                text = room.roomName
                isCheckable = false
                applyChipStyle(this, index == 0)
            }
            chip.setOnClickListener {
                selectedRoomIndex = index
                refreshChipStyles()
                updateDisplay(lastAzimuth)
            }
            binding.chipGroupRooms.addView(chip)
        }
    }

    private fun applyChipStyle(chip: Chip, selected: Boolean) {
        val fg = if (selected) Color.parseColor("#E94560") else Color.parseColor("#9399B2")
        val bg = if (selected) Color.parseColor("#2A0E18") else Color.parseColor("#1E1E2E")
        chip.chipBackgroundColor = ColorStateList.valueOf(bg)
        chip.setTextColor(fg)
        chip.chipStrokeWidth = 0f
    }

    private fun refreshChipStyles() {
        for (i in 0 until binding.chipGroupRooms.childCount) {
            val chip = binding.chipGroupRooms.getChildAt(i) as? Chip ?: continue
            applyChipStyle(chip, i == selectedRoomIndex)
        }
    }

    private fun updateDisplay(azimuth: Float) {
        binding.compassViewRoom.azimuth = azimuth

        val names = VastuData.getNames(azimuth)
        binding.tvRoomEnglish.text = names.english
        binding.tvRoomTelugu.text  = names.telugu
        binding.tvRoomTamil.text   = names.tamil

        val room   = VastuData.roomRules[selectedRoomIndex]
        val code   = VastuData.getCode(azimuth)
        val result = VastuData.getRoomVastu(room, code)
        val color  = Color.parseColor(result.rating.colorHex)

        binding.tvRoomVastuHeader.text = "${room.roomName.uppercase()} VASTU"
        binding.tvRoomVastuStars.text  = result.rating.stars
        binding.tvRoomVastuStars.setTextColor(color)
        binding.tvRoomVastuLabel.text  = "  ${result.rating.label}"
        binding.tvRoomVastuLabel.setTextColor(color)
        binding.tvRoomVastuAdvice.text = result.advice

        val bgColor = when (result.rating) {
            VastuRating.EXCELLENT -> Color.parseColor("#0D2A1A")
            VastuRating.GOOD      -> Color.parseColor("#0D2530")
            VastuRating.NEUTRAL   -> Color.parseColor("#2A2310")
            VastuRating.AVOID     -> Color.parseColor("#2A1A0A")
            VastuRating.BAD       -> Color.parseColor("#2A0E18")
        }
        binding.cardRoomVastu.setBackgroundColor(bgColor)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
