package com.example.scancardnumber.utils

object Utils {
    fun formatCardNumber(t: String, s: String, num: Int = 4): String {
        if (s.length <= num) {
            //String to small, do nothing
            return s
        }
        val retVal: StringBuilder = StringBuilder(s)
        var i = retVal.length
        while (i > 0) {
            retVal.insert(i, t)
            i -= num
        }
        return retVal.toString()
    }
}