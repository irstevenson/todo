package todo.pages

import geb.Page
import geb.Module

class MainPage extends Page {
	static url = 'toDo/index'

	static at = {
		$( '.page-header > h1:nth-child(1)' ).text() == 'Main Page'
	}

	static content = {
		projectNameInput { $( '#projectName' ) }
		addProjectButton { $( '#addProject' ) }
		addItemButton { $( '#addItem' ) }
		projectRows( required: false ) {
			$( 'table#projectsTable tbody tr.projectRow' ).collect {
				module ProjectRow, it
			}
		}
		itemRows( required: false ) {
			$( 'table#itemsTable tbody tr.itemRow' ).collect {
				module ItemRow, it
			}
		}
	}

	void addProject( String projectName ) {
		projectNameInput.value( projectName )
		addProjectButton.click()
	}

	void removeProject( String projectName ) {
		findProject( projectName ).removeProject.click()
	}

	ProjectRow findProject( String projectName ) {
		projectRows.find { row ->
			row.name == projectName
		}
	}

	ItemRow findItem( String itemDescription ) {
		itemRows.find { row ->
			row.description == itemDescription
		}
	}

	boolean hasProjects() {
		$( '#projectsTable > tbody > tr > td[colspan="2"]' )?.text() != 'add a project' && projectRows.size() > 0
	}

	boolean hasItems() {
		$( '#itemsTable > tbody > tr > td[colspan="5"]' )?.text() != 'add a item' && projectRows.size() > 0
	}

	boolean hasProject( String projectName ) {
		findProject( projectName )
	}

	boolean hasItem( String itemDescription ) {
		findItem( itemDescription )
	}

	void selectProject( String projectName ) {
		findProject( projectName ).viewItems.click()
	}
}

// Some nice doco around using lists, tables etc. can be found here:
// http://adhockery.blogspot.com.au/2010/11/modelling-repeating-structures-with-geb.html
class ProjectRow extends Module {
	static content = {
		cell { i -> $("td", i) }
		// cell(0).text() will be the project name plus the item count in a 'badge', so an example
		// would look like: This is a test project 0
		name { (cell(0).text() =~ /(.*) \d+/)[0][1] } // Use regex to chop off item count
		itemCount { (cell(0).text() =~ /.* (\d+)/)[0][1] } // Use regex to get item count
		actions { cell(1) }
		viewItems { actions.find( 'span.viewItems' ) }
		removeProject { actions.find( 'span.removeProject' ) }
	}
}

class ItemRow extends Module {
	static content = {
		cell { i -> $("td", i) }
		priority { cell(0).text() }
		dueDate { cell(1).text() }
		description { cell(2).text() }
		done { cell(3).text() }
		actions { cell(4).text() }
	}
}
