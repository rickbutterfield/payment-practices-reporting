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

package forms.report

import javax.inject.Inject

import models.CompaniesHouseId
import org.joda.time.LocalDate
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid}
import play.api.data.{FormError, Mapping}
import utils.YesNo.Yes
import utils.{AdjustErrors, TimeSource}

class Validations @Inject()(timeSource: TimeSource) {

  import forms.Validations._

  def isBlank(s: String): Boolean = s.trim() == ""

  val companiesHouseId: Mapping[CompaniesHouseId] = nonEmptyText.transform(s => CompaniesHouseId(s), (c: CompaniesHouseId) => c.id)

  val percentage = number(min = 0, max = 100)

  private val textRequiredIfYes = Constraint { ct: ConditionalText =>
    ct match {
      case ConditionalText(Yes, None) => Invalid("error.required")
      case _ => Valid
    }
  }

  private val condText: Mapping[ConditionalText] = mapping(
    "yesNo" -> yesNo,
    "text" -> optional(text)
  )(ConditionalText.apply)(ConditionalText.unapply)
    .transform(_.normalize, (ct: ConditionalText) => ct)
    .verifying(textRequiredIfYes)


  /*
  * Move any messages attached to the base key to the `text` subkey. The
  * only message we're expecting is the `error.required` generated by the
  * `textRequiredIfYes` constraint.
   */
  val conditionalText = AdjustErrors(condText) { (key, errs) =>
    errs.map {
      case FormError(k, messages, args) if k == key => FormError(s"$k.text", messages, args)
      case e => e
    }
  }

  val percentageSplit: Mapping[PercentageSplit] = mapping(
    "percentWithin30Days" -> percentage,
    "percentWithin60Days" -> percentage,
    "percentBeyond60Days" -> percentage
  )(PercentageSplit.apply)(PercentageSplit.unapply)
    .verifying("error.sumto100", sumTo100(_))

  private def sumTo100(ps: PercentageSplit): Boolean = (100 - ps.total).abs <= 2

  val paymentHistory: Mapping[PaymentHistory] = mapping(
    "averageDaysToPay" -> number(min = 0),
    "percentPaidBeyondAgreedTerms" -> percentage,
    "percentageSplit" -> percentageSplit
  )(PaymentHistory.apply)(PaymentHistory.unapply)

  val paymentTerms: Mapping[PaymentTerms] = mapping(
    "terms" -> nonEmptyText,
    "paymentPeriod" -> number(min = 0),
    "maximumContractPeriod" -> number(min = 0),
    "maximumContractPeriodComment" -> optional(nonEmptyText),
    "paymentTermsChanged" -> conditionalText,
    "paymentTermsNotified" -> conditionalText,
    "paymentTermsComment" -> optional(nonEmptyText)
  )(PaymentTerms.apply)(PaymentTerms.unapply)

  private def now() = new LocalDate(timeSource.currentTimeMillis())

  val reportFormModel = mapping(
    "filingDate" -> jodaLocalDate,
    "reportDates" -> dateRange.verifying("error.beforenow", dr => dr.startDate.isBefore(now())),
    "paymentHistory" -> paymentHistory,
    "paymentTerms" -> paymentTerms,
    "disputeResolution" -> nonEmptyText,
    "paymentCodes" -> conditionalText,
    "offerEInvoicing" -> yesNo,
    "offerSupplyChainFinancing" -> yesNo,
    "retentionChargesInPolicy" -> yesNo,
    "retentionChargesInPast" -> yesNo
  )(ReportFormModel.apply)(ReportFormModel.unapply)

  val reportReviewModel = mapping(
    "confirmed" -> boolean,
    "confirmedBy" -> nonEmptyText
  )(ReportReviewModel.apply)(ReportReviewModel.unapply)
}
