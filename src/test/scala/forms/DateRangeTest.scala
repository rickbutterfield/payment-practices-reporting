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

package forms

import org.joda.time.LocalDate
import org.scalatest.{Matchers, WordSpecLike}

class DateRangeTest extends WordSpecLike with Matchers {

  "DateRange" can {
    "calculate months in range" should {
      "give 6 months" in {
        DateRange(new LocalDate(2017,4,6), new LocalDate(2017, 10, 5)).monthsInRange shouldBe 6
      }
      "give 7 months" in {
        DateRange(new LocalDate(2017,4,6), new LocalDate(2017, 10, 6)).monthsInRange shouldBe 7
      }
    }
  }

}