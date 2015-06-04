package todo.pages

import geb.Page

class TitlePage extends Page {
	static url = ''

	static at = {
		title ==~ /ToDo - Welcome/
	}

	static content = {
		logo { $( '#logo' ) }
	}
}
