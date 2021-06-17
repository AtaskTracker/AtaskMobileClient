package org.hse.ataskmobileclient.services


import java.util.*

class DateTimeComparer {

    companion object {
        fun compareDateOnly(first: Date, second: Date) : Int {
            val firstCal = Calendar.getInstance()
            firstCal.time = first

            val secondCal = Calendar.getInstance()
            secondCal.time = second

            firstCal.set(Calendar.MILLISECOND, 0)
            firstCal.set(Calendar.SECOND, 0)
            firstCal.set(Calendar.MINUTE, 0)
            firstCal.set(Calendar.HOUR_OF_DAY, 0)
            secondCal.set(Calendar.MILLISECOND, 0)
            secondCal.set(Calendar.SECOND, 0)
            secondCal.set(Calendar.MINUTE, 0)
            secondCal.set(Calendar.HOUR_OF_DAY, 0)

            return firstCal.compareTo(secondCal)
        }
    }
}