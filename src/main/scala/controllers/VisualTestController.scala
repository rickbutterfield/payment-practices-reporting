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

package controllers

import javax.inject.Inject

import play.api.mvc.{Action, Controller}
import play.twirl.api.Html
import questionnaire.Questions

class VisualTestController @Inject()(questions: Questions) extends Controller with PageHelper {
import questions._
  def show = Action {
    val index = views.html.index()
    val download = views.html.download.accessData()
    val qStart = views.html.questionnaire.start()

    val content = (Seq(index, download, qStart) ++ questionPages).flatMap(x => Seq(x, Html("<hr/>")))
    Ok(page(content: _*))
  }

  val questionPages = Seq(
    isCompanyOrLLPQuestion,
    financialYearQuestion,
    hasSubsidiariesQuestion,
    companyTurnoverQuestionY2,
    companyBalanceSheetQuestionY2,
    companyEmployeesQuestionY2,
    companyTurnoverQuestionY3,
    companyBalanceSheetQuestionY3,
    companyEmployeesQuestionY3,
    subsidiaryTurnoverQuestionY2,
    subsidiaryBalanceSheetQuestionY2,
    subsidiaryEmployeesQuestionY2,
    subsidiaryTurnoverQuestionY3,
    subsidiaryBalanceSheetQuestionY3,
    subsidiaryEmployeesQuestionY3
  ).map(views.html.questionnaire.question("", _))

}
