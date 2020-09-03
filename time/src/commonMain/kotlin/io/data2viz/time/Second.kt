/*
 * Copyright (c) 2018-2019. data2viz sàrl.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package io.data2viz.time

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlin.time.ExperimentalTime

@ExperimentalTime
class Second : Interval(
    fun(date: LocalDateTime): LocalDateTime =
        LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, date.hour, date.minute, date.second, 0),
    fun(date: LocalDateTime, step: Int): LocalDateTime {
        return when {
            step > 0 -> date + (DateTimeUnit.SECOND * step).duration
            step < 0 -> date - (DateTimeUnit.SECOND * -step).duration
            else -> date
        }
    },
    fun(start: LocalDateTime, end: LocalDateTime): Int = (end - start).inSeconds.toInt(),
    fun(date: LocalDateTime): Int = date.second
)

@ExperimentalTime
val timeSecond = Second()