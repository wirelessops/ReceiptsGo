package com.wops.receiptsgo.tooltip.model

import org.junit.Assert.*
import org.junit.Test

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TooltipTypeTest {

    @Test
    fun verifyNoTooltipsHaveTheSamePriority() {
        val prioritySet = mutableSetOf<Int>()
        TooltipType.values().forEach {
            assertFalse(prioritySet.contains(it.priority))
            prioritySet.add(it.priority)
        }
    }

    @Test
    fun verifyNoTooltipsHaveBothACloseIconAndCancelButton() {
        TooltipType.values().forEach {
            assertFalse(it.showCancelButton and it.showCloseIcon)
        }
    }
}