/*
 * Copyright (C) 2017  Department for Business, Energy and Industrial Strategy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package calculator

import forms.DateRange
import org.joda.time.LocalDate

case class FinancialYear(dates: DateRange) {

  def startsOnOrAfter(cutoff: LocalDate): Boolean = dates.startsOnOrAfter(cutoff)

  def nextYear: FinancialYear =
    FinancialYear(DateRange(dates.endDate.plusDays(1), dates.endDate.plusYears(1)))

  def firstYearOnOrAfter(cutoff: LocalDate): FinancialYear =
    if (startsOnOrAfter(cutoff)) this else nextYear.firstYearOnOrAfter(cutoff)

  /**
    * If the financial year is:
    * 9 months or less - single reporting period covering the whole financial year
    * 9 months and a day up to 15 months - split into 6-month period and a remainder period
    * 15 months and a day or more - split into 2 6-month periods and a remainder period
    */
  def reportingPeriods: Seq[ReportingPeriod] = dates.splitAt(6) match {
    case (first, None) => Seq(ReportingPeriod(first))
    // 9 months or less - single reporting period covering the whole financial year
    case (_, Some(remainder)) if remainder.monthsInRange <= 3 => Seq(ReportingPeriod(this.dates))
    // 9 to 15 months
    case (first, Some(remainder)) if remainder.monthsInRange <= 9 => Seq(first, remainder).map(ReportingPeriod)
    // more than 15 months - make two periods of six months and a remainder period
    case (first, Some(remainder)) => remainder.splitAt(6) match {
      case (second, rem2) => Seq(ReportingPeriod(first), ReportingPeriod(second)) ++ rem2.map(ReportingPeriod)
    }
  }
}