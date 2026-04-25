package com.compass.app

data class DirectionNames(
    val english: String,
    val telugu: String,
    val tamil: String
)

enum class VastuRating {
    EXCELLENT, GOOD, NEUTRAL, AVOID, BAD;

    val stars: String get() = when (this) {
        EXCELLENT -> "★★★★★"
        GOOD      -> "★★★★☆"
        NEUTRAL   -> "★★★☆☆"
        AVOID     -> "★★☆☆☆"
        BAD       -> "★☆☆☆☆"
    }

    val label: String get() = when (this) {
        EXCELLENT -> "EXCELLENT"
        GOOD      -> "VERY GOOD"
        NEUTRAL   -> "NEUTRAL"
        AVOID     -> "NOT IDEAL"
        BAD       -> "AVOID"
    }

    val colorHex: String get() = when (this) {
        EXCELLENT -> "#A6E3A1"
        GOOD      -> "#89DCEB"
        NEUTRAL   -> "#F9E2AF"
        AVOID     -> "#FAB387"
        BAD       -> "#F38BA8"
    }
}

data class VastuResult(
    val rating: VastuRating,
    val advice: String
)

data class RoomVastuRule(
    val roomName: String,
    val roomTelugu: String,
    val roomTamil: String,
    val bestDirs: List<String>,
    val goodDirs: List<String>,
    val avoidDirs: List<String>,
    val tip: String
)

object VastuData {

    val directionNames = mapOf(
        "N"  to DirectionNames("North",     "Uttaram",   "Vadakku"),
        "NE" to DirectionNames("Northeast", "Eshanyam",  "Vaada Kilakku"),
        "E"  to DirectionNames("East",      "Toorpu",    "Kilakku"),
        "SE" to DirectionNames("Southeast", "Aagneyam",  "Then Kilakku"),
        "S"  to DirectionNames("South",     "Dakshinam", "Therku"),
        "SW" to DirectionNames("Southwest", "Nairuthi",  "Then Merku"),
        "W"  to DirectionNames("West",      "Padamara",  "Merku"),
        "NW" to DirectionNames("Northwest", "Vaayuvyam", "Vaada Merku")
    )

    val directionOrder = listOf("N", "NE", "E", "NW", "W", "SE", "S", "SW")

    fun getCode(degrees: Float): String = when {
        degrees < 22.5f || degrees >= 337.5f -> "N"
        degrees < 67.5f  -> "NE"
        degrees < 112.5f -> "E"
        degrees < 157.5f -> "SE"
        degrees < 202.5f -> "S"
        degrees < 247.5f -> "SW"
        degrees < 292.5f -> "W"
        else             -> "NW"
    }

    fun getNames(degrees: Float): DirectionNames = directionNames[getCode(degrees)]!!

    val mainEntranceVastu = mapOf(
        "N"  to VastuResult(VastuRating.EXCELLENT, "North entrance brings wealth and prosperity. Highly recommended for main door."),
        "NE" to VastuResult(VastuRating.EXCELLENT, "Northeast (Eshanya) is the most auspicious entrance. Brings health, wealth and success."),
        "E"  to VastuResult(VastuRating.GOOD,      "East entrance brings positive energy and good health. Highly recommended."),
        "NW" to VastuResult(VastuRating.NEUTRAL,   "Northwest entrance is acceptable. Good for social connections and business."),
        "W"  to VastuResult(VastuRating.NEUTRAL,   "West entrance is neutral. Acceptable with proper Vastu measures."),
        "SE" to VastuResult(VastuRating.AVOID,     "Southeast entrance may cause conflicts and financial difficulties. Avoid if possible."),
        "S"  to VastuResult(VastuRating.BAD,       "South entrance is inauspicious. Associated with struggles and legal issues."),
        "SW" to VastuResult(VastuRating.BAD,       "Southwest entrance is very inauspicious. Strongly not recommended.")
    )

    val roomRules = listOf(
        RoomVastuRule(
            "Main Entrance", "Pradhana Dwaram", "Mukkiya Vaasal",
            bestDirs  = listOf("N", "NE", "E"),
            goodDirs  = listOf("NW"),
            avoidDirs = listOf("S", "SW", "SE"),
            tip = "Ideal: North, Northeast or East — brings health and wealth"
        ),
        RoomVastuRule(
            "Kitchen", "Vanta Gadi", "Samayal Arai",
            bestDirs  = listOf("SE"),
            goodDirs  = listOf("S"),
            avoidDirs = listOf("NE", "SW", "N", "NW"),
            tip = "Southeast (Agneya — fire corner) is the ideal zone for kitchen"
        ),
        RoomVastuRule(
            "Master Bedroom", "Pradhana Padakagadi", "Thalamai Padukkai Arai",
            bestDirs  = listOf("SW"),
            goodDirs  = listOf("S", "W"),
            avoidDirs = listOf("NE", "E", "N"),
            tip = "Southwest gives stability, authority and strong family bonds"
        ),
        RoomVastuRule(
            "Puja / Prayer Room", "Puja Gadi", "Pooja Arai",
            bestDirs  = listOf("NE"),
            goodDirs  = listOf("E", "N"),
            avoidDirs = listOf("S", "SW", "W", "SE"),
            tip = "Northeast (Eshanya) is sacred — best zone for prayers and meditation"
        ),
        RoomVastuRule(
            "Children's Bedroom", "Pillala Padakagadi", "Pillaikal Padukkai Arai",
            bestDirs  = listOf("W"),
            goodDirs  = listOf("N", "E"),
            avoidDirs = listOf("SW", "SE"),
            tip = "West or North helps children focus, study and grow well"
        ),
        RoomVastuRule(
            "Living Room / Hall", "Hall", "Vasippu Arai",
            bestDirs  = listOf("N", "NE", "E"),
            goodDirs  = listOf("W", "NW"),
            avoidDirs = listOf("S", "SW"),
            tip = "North or East facing hall attracts positive social energy"
        ),
        RoomVastuRule(
            "Study Room", "Chaduvula Gadi", "Padippu Arai",
            bestDirs  = listOf("NE", "N", "E"),
            goodDirs  = listOf("W"),
            avoidDirs = listOf("S", "SW", "SE"),
            tip = "Northeast or East promotes concentration, memory and knowledge"
        ),
        RoomVastuRule(
            "Bathroom / Toilet", "Bathroom", "Kulikkai Arai",
            bestDirs  = listOf("NW"),
            goodDirs  = listOf("W", "SE"),
            avoidDirs = listOf("NE", "SW", "N"),
            tip = "Northwest is ideal — never place bathroom in sacred Northeast corner"
        ),
        RoomVastuRule(
            "Staircase", "Meeda Meetu", "Padikatti",
            bestDirs  = listOf("SW", "S", "W"),
            goodDirs  = listOf("SE"),
            avoidDirs = listOf("NE", "N", "E"),
            tip = "South, West or Southwest staircase is ideal — never in Northeast"
        )
    )

    fun getRoomVastu(room: RoomVastuRule, dirCode: String): VastuResult = when {
        room.bestDirs.contains(dirCode)  -> VastuResult(VastuRating.EXCELLENT, "Perfect direction! ${room.tip}")
        room.goodDirs.contains(dirCode)  -> VastuResult(VastuRating.GOOD,      "Acceptable. ${room.tip}")
        room.avoidDirs.contains(dirCode) -> VastuResult(VastuRating.BAD,       "Not recommended! ${room.tip}")
        else                             -> VastuResult(VastuRating.NEUTRAL,    "Neutral. ${room.tip}")
    }

    val propertyChecklist = listOf(
        "Main entrance faces North, East or Northeast" to "Best directions for prosperity",
        "Kitchen is in Southeast corner of home" to "Fire element zone — ideal for cooking",
        "Master bedroom is in Southwest zone" to "Stability and authority zone",
        "Puja room is in Northeast corner" to "Most sacred corner of the house",
        "No bathroom or toilet in Northeast" to "Never place toilet in sacred NE corner",
        "Staircase in South, West or Southwest" to "Heavy structure belongs in SW/S/W",
        "Overhead water tank in West or Southwest" to "Heavy weight should be in SW/W",
        "Underground tank or borewell in Northeast" to "Water source in NE is auspicious",
        "Plot or flat shape is square or rectangular" to "Irregular shapes create Vastu defects",
        "No missing or cut corners in the plot" to "Especially avoid a missing NE corner",
        "Morning sunlight enters from East side" to "East-facing windows for natural light",
        "No large tree blocking main entrance" to "Entrance should be open and welcoming",
        "No road directly hitting front door (T-junction)" to "T-junction roads bring negative energy",
        "Surrounding area is clean and positive" to "Avoid vicinity of cemetery, slaughterhouse"
    )

    val quickTips = listOf(
        "Square or rectangular plots are most auspicious",
        "Northeast corner should always be open, light and clean",
        "Never keep heavy furniture (safe, stone idol) in Northeast",
        "Buy a plot where the previous owner prospered",
        "Check for waterlogging or drainage issues in the plot",
        "Avoid plots near cremation grounds, hospitals or courts",
        "T-junction plots (road hitting the front) create Vastu dosham",
        "Plot should ideally slope down from Southwest to Northeast",
        "Ensure cross-ventilation from East to West",
        "Open space (balcony/garden) should be on North or East side"
    )
}
