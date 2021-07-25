package com.thic.callendarexample

import android.content.Context
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import java.time.DayOfWeek
import java.time.temporal.WeekFields
import java.util.*
import kotlin.collections.ArrayList


internal fun Context.getColorCompat(@ColorRes color: Int) = ContextCompat.getColor(this, color)

internal fun TextView.setTextColorRes(@ColorRes color: Int) = setTextColor(context.getColorCompat(color))

fun daysOfWeekFromLocale(): Array<DayOfWeek> {
    val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
    var daysOfWeek = DayOfWeek.values()
    if (firstDayOfWeek != DayOfWeek.MONDAY) {
        val rhs = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last)
        val lhs = daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
        daysOfWeek = rhs + lhs
    }
    return daysOfWeek
}

fun getJobList():List<Event>{
    val myList = ArrayList<Event>()
    myList.add(Event("deneme 1","2021-07-19"))
    myList.add(Event("deneme 2","2021-07-19"))
    myList.add(Event("deneme 3","2021-07-19"))
    myList.add(Event("deneme 4","2021-07-20"))
    myList.add(Event("deneme 5","2021-07-20"))
    myList.add(Event("deneme 6","2021-07-20"))
    myList.add(Event("deneme 7","2021-07-20"))

    return myList
}


