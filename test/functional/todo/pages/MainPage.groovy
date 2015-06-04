package todo.pages

import geb.Page

class MainPage extends Page {
	static url = 'toDo/index'

	static at = {
		$( '.page-header > h1:nth-child(1)' ).text() == 'Main Page'
	}
}
