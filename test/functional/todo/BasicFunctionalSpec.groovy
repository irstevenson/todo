package todo

import geb.spock.GebReportingSpec
import spock.lang.*

import todo.pages.*

@Stepwise
class BasicFunctionalSpec extends GebReportingSpec {
	static final testProjectName = 'The Test Project'

	void 'test the system is running'() {
		when: 'user navigates to the site and clicks the logo'
			to TitlePage
			logo.click()

		then: "they're at the main page and there's no projects"
			at MainPage
			!hasProjects()
	}

	void 'test adding a project'() {
		when: 'a user adds the first project'
			addProject( testProjectName )

		then: "they're returned to the main page and there are now projects"
			at MainPage
			hasProjects() && hasProject( testProjectName )
	}

	void 'test removing a project'() {
		when: 'a user removes a project'
			removeProject( testProjectName )

		then: "they return to the main page, and it's gone"
			at MainPage
			!hasProjects()
	}

	void 'add multiple projects, choose one and then start adding an item'() {
		when: 'a user adds multiple items and chooses one'
			for( i in 1..10 )
				addProject( "$testProjectName $i" )
			selectProject( "$testProjectName 5" )
			addItemButton.click()			

		then: 'the user arrives at the edit item page'
			at EditItemPage
	}

	void 'user provides details for new items and returns to the main page'() {
		when: 'the user completes the details for the new item and saves'
			final itemName = 'Test Item 1'
			saveItem( itemName, EditItemPage.Priority.LOW, '', false )

		then: 'they arrive back at the main page, and the item is there'
			at MainPage
			hasItems()
			hasItem( itemName )
	}
}
