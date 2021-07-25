package com.thic.callendarexample

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.model.InDateStyle
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.next
import com.kizitonwose.calendarview.utils.yearMonth
import com.thic.callendarexample.databinding.ActivityMainBinding
import com.thic.callendarexample.databinding.Example1CalendarDayBinding
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*


class MainActivity : AppCompatActivity()  {

    private lateinit var binding:ActivityMainBinding
    private var weeklyModeOn:Boolean = false

    private val selectedDates = mutableSetOf<LocalDate>()
    private val today = LocalDate.now()
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM")

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.hide()

        val daysOfWeek = daysOfWeekFromLocale()

        // Haftanın günlerini bindiriyoruz
        binding.legendLayout.root.children.forEachIndexed { index, view ->
            (view as TextView).apply {
                text = daysOfWeek[index].getDisplayName(TextStyle.SHORT, Locale.getDefault()).toUpperCase(Locale.getDefault())
                setTextColorRes(R.color.white)
            }
        }
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(10)
        val endMonth = currentMonth.plusMonths(10)
        binding.exOneCalendar.setup(startMonth, endMonth, daysOfWeek.first())
        binding.exOneCalendar.scrollToMonth(currentMonth)

        // ----
        class DayViewContainer(view: View) : ViewContainer(view) {
            // Will be set when this container is bound. See the dayBinder.
            lateinit var day: CalendarDay
            val textView = Example1CalendarDayBinding.bind(view).exOneDayText

            init {
                view.setOnClickListener {
                    if (weeklyModeOn){
                        Toast.makeText(applicationContext,"Number of jobs found on this date: : ${jobReturner(day.date.toString())}",Toast.LENGTH_SHORT).show()
                        if (day.owner == DayOwner.THIS_MONTH) {
                        if (selectedDates.contains(day.date)) {
                            selectedDates.remove(day.date)
                        } else {
                            selectedDates.add(day.date)
                        }
                        binding.exOneCalendar.notifyDayChanged(day)
                        }
                    }else{
                        Toast.makeText(applicationContext,"you can not showing jobs in monthly mode",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        // ----

        binding.exOneCalendar.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.textView
                textView.text = day.date.dayOfMonth.toString()

                if(selectedDates.contains(day.date)) {
                    textView.setTextColorRes(R.color.white)
                    textView.setBackgroundResource(R.drawable.example_1_selected_bg)
                }
            }
        }

        binding.exOneCalendar.monthScrollListener = {
            if (binding.exOneCalendar.maxRowCount == 6) {
                binding.exOneYearText.text = it.yearMonth.year.toString()
                binding.exOneMonthText.text = monthTitleFormatter.format(it.yearMonth)
            } else {
                // In week mode, we show the header a bit differently.
                // We show indices with dates from different months since
                // dates overflow and cells in one index can belong to different
                // months/years.
                val firstDate = it.weekDays.first().first().date
                val lastDate = it.weekDays.last().last().date
                if (firstDate.yearMonth == lastDate.yearMonth) {
                    binding.exOneYearText.text = firstDate.yearMonth.year.toString()
                    binding.exOneMonthText.text = monthTitleFormatter.format(firstDate)
                } else {
                    binding.exOneMonthText.text =
                            "${monthTitleFormatter.format(firstDate)} - ${monthTitleFormatter.format(lastDate)}"
                    if (firstDate.year == lastDate.year) {
                        binding.exOneYearText.text = firstDate.yearMonth.year.toString()
                    } else {
                        binding.exOneYearText.text = "${firstDate.yearMonth.year} - ${lastDate.yearMonth.year}"
                    }
                }
            }
        }
        // ----

        binding.weekModeCheckBox.setOnCheckedChangeListener { _, monthToWeek ->

            weeklyModeOn = !weeklyModeOn

            val firstDate = binding.exOneCalendar.findFirstVisibleDay()?.date
                    ?: return@setOnCheckedChangeListener
            val lastDate = binding.exOneCalendar.findLastVisibleDay()?.date
                    ?: return@setOnCheckedChangeListener

            val oneWeekHeight = binding.exOneCalendar.daySize.height
            val oneMonthHeight = oneWeekHeight * 6

            val oldHeight = if (monthToWeek) oneMonthHeight else oneWeekHeight
            val newHeight = if (monthToWeek) oneWeekHeight else oneMonthHeight

            // Animate calendar height changes.
            val animator = ValueAnimator.ofInt(oldHeight, newHeight)
            animator.addUpdateListener { animator ->
                binding.exOneCalendar.updateLayoutParams {
                    height = animator.animatedValue as Int
                }
            }

            animator.doOnStart {
                if (!monthToWeek) {
                    binding.exOneCalendar.updateMonthConfiguration(
                            inDateStyle = InDateStyle.ALL_MONTHS,
                            maxRowCount = 6,
                            hasBoundaries = true
                    )
                }
            }
            animator.doOnEnd {
                if (monthToWeek) {
                    binding.exOneCalendar.updateMonthConfiguration(
                            inDateStyle = InDateStyle.FIRST_MONTH,
                            maxRowCount = 1,
                            hasBoundaries = false
                    )
                }

                if (monthToWeek) {
                    // We want the first visible day to remain
                    // visible when we change to week mode.
                    binding.exOneCalendar.scrollToDate(firstDate)
                } else {
                    // When changing to month mode, we choose current
                    // month if it is the only one in the current frame.
                    // if we have multiple months in one frame, we prefer
                    // the second one unless it's an outDate in the last index.
                    if (firstDate.yearMonth == lastDate.yearMonth) {
                        binding.exOneCalendar.scrollToMonth(firstDate.yearMonth)
                    } else {
                        // We compare the next with the last month on the calendar so we don't go over.
                        binding.exOneCalendar.scrollToMonth(minOf(firstDate.yearMonth.next, endMonth))
                    }
                }
            }
            animator.duration = 250
            animator.start()

        }
    }

    override fun onStart() {
        super.onStart()
        this.window.statusBarColor = this.getColorCompat(R.color.white)
    }

    override fun onStop() {
        super.onStop()
        this.window.statusBarColor = this.getColorCompat(R.color.colorPrimaryDark)
    }

    fun jobReturner(date:String):Int{
        val myJobs = getJobList()
        var count = 0
        for (index in myJobs){
            if (index.startingDate == date){
                count++
            }
        }
        return count
    }
}


