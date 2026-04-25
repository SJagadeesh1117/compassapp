package com.compass.app

import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.compass.app.databinding.FragmentVastuGuideBinding

class VastuGuideFragment : Fragment() {

    private var _binding: FragmentVastuGuideBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVastuGuideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buildDirectionSection()
        buildRoomSection()
        buildChecklist()
        buildTips()
    }

    // ── Direction guide ──────────────────────────────────────────────────────

    private fun buildDirectionSection() {
        VastuData.directionOrder.forEachIndexed { index, code ->
            val names  = VastuData.directionNames[code]!!
            val result = VastuData.mainEntranceVastu[code]!!
            val color  = Color.parseColor(result.rating.colorHex)

            if (index > 0) binding.containerDirections.addView(makeDivider())

            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity     = android.view.Gravity.CENTER_VERTICAL
                setPadding(0, dp(10), 0, dp(10))
            }

            val dot = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(dp(10), dp(10)).also {
                    it.marginEnd = dp(12)
                }
                background = circleDrawable(color)
            }

            val nameCol = LinearLayout(requireContext()).apply {
                orientation  = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            nameCol.addView(makeText(names.english, 15f, "#CDD6F4", bold = true))
            nameCol.addView(makeText("${names.telugu}  •  ${names.tamil}", 11f, "#6C7086"))

            val ratingCol = LinearLayout(requireContext()).apply {
                orientation  = LinearLayout.VERTICAL
                gravity      = android.view.Gravity.END
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }
            ratingCol.addView(makeText(result.rating.stars, 13f, result.rating.colorHex))
            ratingCol.addView(makeText(result.rating.label, 10f, result.rating.colorHex).also {
                it.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
            })

            row.addView(dot)
            row.addView(nameCol)
            row.addView(ratingCol)
            binding.containerDirections.addView(row)
        }
    }

    // ── Room placement ───────────────────────────────────────────────────────

    private fun buildRoomSection() {
        VastuData.roomRules.forEachIndexed { index, room ->
            if (index > 0) binding.containerRooms.addView(makeDivider())

            val card = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, dp(10), 0, dp(10))
            }

            val nameRow = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity     = android.view.Gravity.CENTER_VERTICAL
            }
            val dot = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(dp(8), dp(8)).also { it.marginEnd = dp(8) }
                background = circleDrawable(Color.parseColor("#E94560"))
            }
            nameRow.addView(dot)
            nameRow.addView(makeText(room.roomName, 14f, "#CDD6F4", bold = true))
            card.addView(nameRow)

            card.addView(makeText(
                "   ${room.roomTelugu}  •  ${room.roomTamil}", 11f, "#6C7086"
            ).also { it.setPadding(0, dp(2), 0, dp(4)) })

            val dirRow = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity     = android.view.Gravity.CENTER_VERTICAL
            }
            dirRow.addView(dirBadge("Best", room.bestDirs, "#A6E3A1"))
            dirRow.addView(dirBadge("Avoid", room.avoidDirs, "#F38BA8").also {
                (it.layoutParams as LinearLayout.LayoutParams).marginStart = dp(8)
            })
            card.addView(dirRow)

            binding.containerRooms.addView(card)
        }
    }

    private fun dirBadge(label: String, dirs: List<String>, colorHex: String): LinearLayout {
        val color   = Color.parseColor(colorHex)
        val bgColor = Color.argb(0x22, Color.red(color), Color.green(color), Color.blue(color))
        return LinearLayout(requireContext()).apply {
            orientation  = LinearLayout.HORIZONTAL
            gravity      = android.view.Gravity.CENTER_VERTICAL
            background   = roundRect(bgColor, dp(6))
            setPadding(dp(8), dp(4), dp(8), dp(4))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            addView(makeText("$label: ", 10f, colorHex, bold = true))
            addView(makeText(dirs.joinToString(" "), 10f, colorHex))
        }
    }

    // ── Checklist ────────────────────────────────────────────────────────────

    private fun buildChecklist() {
        VastuData.propertyChecklist.forEachIndexed { index, (main, sub) ->
            if (index > 0) binding.containerChecklist.addView(makeDivider())

            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity     = android.view.Gravity.TOP
                setPadding(0, dp(8), 0, dp(8))
            }

            val checkBox = CheckBox(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.marginEnd = dp(10) }
                buttonTintList = android.content.res.ColorStateList.valueOf(
                    Color.parseColor("#E94560")
                )
            }

            val textCol = LinearLayout(requireContext()).apply {
                orientation  = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            textCol.addView(makeText(main, 13f, "#CDD6F4"))
            textCol.addView(makeText(sub, 11f, "#6C7086").also {
                it.setPadding(0, dp(2), 0, 0)
            })

            row.addView(checkBox)
            row.addView(textCol)
            binding.containerChecklist.addView(row)
        }
    }

    // ── Quick tips ───────────────────────────────────────────────────────────

    private fun buildTips() {
        VastuData.quickTips.forEach { tip ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity     = android.view.Gravity.TOP
                setPadding(0, dp(5), 0, dp(5))
            }
            row.addView(makeText("•", 14f, "#E94560").also {
                it.layoutParams = LinearLayout.LayoutParams(dp(18), LinearLayout.LayoutParams.WRAP_CONTENT)
            })
            row.addView(makeText(tip, 12f, "#9399B2").also {
                it.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                it.setLineSpacing(0f, 1.3f)
            })
            binding.containerTips.addView(row)
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun makeText(text: String, sizeSp: Float, colorHex: String, bold: Boolean = false): TextView =
        TextView(requireContext()).apply {
            setText(text)
            textSize = sizeSp
            setTextColor(Color.parseColor(colorHex))
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }

    private fun makeDivider(): View = View(requireContext()).apply {
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
        setBackgroundColor(Color.parseColor("#20CDD6F4"))
    }

    private fun circleDrawable(color: Int): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }

    private fun roundRect(color: Int, radius: Int): GradientDrawable =
        GradientDrawable().apply {
            shape        = GradientDrawable.RECTANGLE
            cornerRadius = radius.toFloat()
            setColor(color)
        }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
