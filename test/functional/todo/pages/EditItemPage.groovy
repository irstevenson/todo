package todo.pages

import geb.Page

class EditItemPage extends Page {
	static at = {
		$( '.page-header > h1:nth-child(1)' ).text() == 'Edit Item'
	}

	static content = {
		description { $( '#description' ) }
		priority { $( '#priority' ) }
		dueDate { $( '#dueDate' ) }
		done { $( '#done' ) }
		saveButton { $( '#save' ) }
	}

	void saveItem( String newDescription, Priority newPriority, String newDueDate, boolean newDone ) {
		description.value( newDescription )
		priority.value( newPriority )
		dueDate.value( newDueDate )
		done.value( newDone )
		
		saveButton.click()
	}

	enum Priority { HIGH, MEDIUM, LOW }
}
