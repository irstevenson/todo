package todo

import geb.spock.GebReportingSpec
import spock.lang.*

import todo.pages.*

@Stepwise
class BasicFunctionalSpec extends GebReportingSpec {
	void 'test the system is running'() {
		when: 'user navigates to the site and clicks the logo'
			to TitlePage
			logo.click()

		then: "they're at the main page"
			at MainPage
	}
}
