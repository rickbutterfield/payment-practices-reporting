package reports

import cats.instances.either._
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.{HtmlForm, HtmlParagraph}
import controllers.{ReportController, ReportingPeriodController, ShortFormController}
import forms.DateFields
import org.apache.xalan.xsltc.compiler.Choose
import org.openqa.selenium.WebDriver
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import org.scalatestplus.play.{HtmlUnitFactory, OneBrowserPerTest, PlaySpec}
import play.api.i18n.MessagesApi
import webspec.WebSpec

import scala.language.postfixOps

class ShortReportSpec extends PlaySpec with WebSpec with GuiceOneServerPerSuite with OneBrowserPerTest with HtmlUnitFactory with ReportingSteps {

  override def createWebDriver(): WebDriver = HtmlUnitFactory.createWebDriver(false)

  val messages: MessagesApi = app.injector.instanceOf[MessagesApi]

  private def NavigateToShortForm(companyName: String, startDate: DateFields = DateFields(1, 5, 2017), endDate: DateFields = DateFields(1, 6, 2017)): PageStep[WebClient] = {
    NavigateToReportingPeriodForm(testCompanyName) andThen
      SetDateFields("reportDates.startDate", startDate) andThen
      SetDateFields("reportDates.endDate", endDate) andThen
      ChooseAndContinue("hasQualifyingContracts-no")
  }

  "the search page" should {
    "let me navigate to publishing start page" in webSpec {
      StartPublishingForCompany(testCompanyName) should
        ShowPage(PublishFor(testCompanyName))
          .containingElement[HtmlParagraph](ReportController.companyNumberParagraphId)(_.getTextContent.contains(testCompany.companiesHouseId.id))
    }
  }

  "selecting a company and navigating through the sign-in" should {
    "show reporting period form" in webSpec {
      NavigateToReportingPeriodForm(testCompanyName) should
        ShowPage(PublishFor(testCompanyName)) withElementById[HtmlForm] ReportingPeriodController.reportingPeriodFormId
    }
  }

  "entering valid dates and selecting No Qualifying Contracts" should {
    "show the short form" in webSpec {
      NavigateToShortForm(testCompanyName) should
        ShowPage(ShortFormPage(testCompany)) withElementById[HtmlForm] ShortFormController.shortFormId
    }
  }

  "selecting no payment codes and submitting" should {
    "show the review page" in webSpec {
      NavigateToShortForm(testCompanyName) andThen
        ChooseAndContinue("paymentCodes.yesNo-no") should
        ShowPage(ShortReviewPage) where {
        Table("review-table") should {
          ContainRow("Start date of reporting period") having Value("1 May 2017") and
            ContainRow("End date of reporting period") having Value("1 June 2017") and
            ContainRow("Are you a member of a code of conduct or standards on payment practices?") having Value("No")
        }
      }
    }
  }

  "entering a value for payment code and submitting" should {
    "show the review page" in webSpec {
      NavigateToShortForm(testCompanyName) andThen
        ChooseRadioButton("paymentCodes.yesNo-yes") andThen
        SetTextField("paymentCodes.text", "payment codes") andThen
        SubmitForm("Continue") should
        ShowPage(ShortReviewPage) where {
        Table("review-table") should {
          ContainRow("Start date of reporting period") having Value("1 May 2017") and
            ContainRow("End date of reporting period") having Value("1 June 2017") and
            ContainRow("Are you a member of a code of conduct or standards on payment practices?") having Value("Yes – payment codes")
        }
      }
    }
  }
}