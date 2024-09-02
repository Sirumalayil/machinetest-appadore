package com.example.machintest_appadore.utils

import com.example.machintest_appadore.R

/**
 * Created by Siru malayil on 02-09-2024.
 */
object AppUtils {


    fun getCountryFlagByCountryCode(countryCode: String?): Int {
        return when (countryCode) {
            "NZ" -> R.drawable.ic_newziland
            "AW" -> R.drawable.ic_aruba
            "TM" -> R.drawable.ic_turkmenistan
            "PY" -> R.drawable.ic_paraguay
            "EC" -> R.drawable.ic_ecuador
            "GA" -> R.drawable.ic_gabon
            "KG" -> R.drawable.ic_kyrgystan
            "JP" -> R.drawable.ic_japan
            "BZ" -> R.drawable.ic_belize
            "AE" -> R.drawable.ic_uae
            "MQ" -> R.drawable.ic_martinique
            "CZ" -> R.drawable.ic_czech_republic
            "PM" -> R.drawable.ic_saint_pierre
            "LS" -> R.drawable.ic_lesotho
            "JE" -> R.drawable.ic_jersey
            else -> R.drawable.ic_timer_text_bg
        }
    }
}