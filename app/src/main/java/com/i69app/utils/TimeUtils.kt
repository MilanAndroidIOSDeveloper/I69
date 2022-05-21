package com.i69app.utils

import android.content.Context
import android.text.format.DateUtils
import android.text.format.DateUtils.*
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

val displayDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

fun timeForLastMessage(timestamp: Long): String {
    val cal = Calendar.getInstance()
    val currentCal = Calendar.getInstance()
    cal.timeInMillis = timestamp
    val year = currentCal.get(Calendar.YEAR) - cal.get(Calendar.YEAR)
    val months = currentCal.get(Calendar.MONTH) - cal.get(Calendar.MONTH) + year * 12
    val days = currentCal.get(Calendar.DAY_OF_MONTH) - cal.get(Calendar.DAY_OF_MONTH) + months * 30
    val hours = currentCal.get(Calendar.HOUR_OF_DAY) - cal.get(Calendar.HOUR_OF_DAY)
    val minutes = currentCal.get(Calendar.MINUTE) - cal.get(Calendar.MINUTE)
    val seconds = currentCal.get(Calendar.SECOND) - cal.get(Calendar.SECOND)

    if (year > 0) return if (year == 1) "$year year ago" else "$year years ago"
    if (months > 0) return if (months == 1) "$months month ago" else "$months months ago"
    if (days > 0) return if (days == 1) "$days day ago" else "$days days ago"
    if (hours > 0) return "${hours}h ago"
    if (minutes > 0) return "${minutes}m ago"
    if (seconds > 0) return "Few seconds ago"
    return "Just now"
}

fun monthsBetweenDates(startDate: Date, endDate: Date): Int {
    val start = Calendar.getInstance()
    start.time = startDate

    val end = Calendar.getInstance()
    end.time = endDate

    var monthsBetween = 0
    var dateDiff = end.get(Calendar.DAY_OF_MONTH) - start.get(Calendar.DAY_OF_MONTH)

    if (dateDiff < 0) {
        val borrow = end.getActualMaximum(Calendar.DAY_OF_MONTH)
        dateDiff = end.get(Calendar.DAY_OF_MONTH) + borrow - start.get(Calendar.DAY_OF_MONTH)
        monthsBetween--

        if (dateDiff > 0) {
            monthsBetween++
        }
    } else {
        monthsBetween++
    }
    monthsBetween += end.get(Calendar.MONTH) - start.get(Calendar.MONTH)
    monthsBetween += (end.get(Calendar.YEAR) - start.get(Calendar.YEAR)) * 12
    return monthsBetween
}

fun currentTimeUTCMillis(): Long {
    return try {
        val date = Date()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ROOT)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        date.time
    } catch (e: ParseException) {
        0
    }
}

fun getDateWithTimeZone(): String {
    val calendar = Calendar.getInstance()
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ROOT)
    val time = simpleDateFormat.format(calendar.time)
    return time.substring(0, 22) + ":" + time.substring(22)
}

fun formatDayDate(ctx: Context, millis: Long): CharSequence? {
    val curr = System.currentTimeMillis()
    val isWithinWeek = curr - millis <= (WEEK_IN_MILLIS - DAY_IN_MILLIS)
    //val isWithinYear = curr - millis <= YEAR_IN_MILLIS;
    if (isWithinWeek) {
        if (curr - millis <= (DAY_IN_MILLIS)) {
            return getRelativeTimeSpanString(millis, Date().time, DAY_IN_MILLIS)
        }
        else {
            return formatDateTime(ctx, millis, FORMAT_SHOW_WEEKDAY)
        }
    } else {
        //return DateUtils.formatDateTime(ctx, millis, DateUtils.FORMAT_SHOW_DATE)
        return displayDate.format(Date(millis))
    }
}