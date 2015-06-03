package todo

import grails.validation.ValidationException

import todo.ItemManagerService.EntityNotFound

class ToDoController {
	def itemManagerService

    def index() {
		if( !session.currentProjectName )
			session.currentProjectName = "please choose a project - or add a new one"

		def projects = itemManagerService.listProjects()
		log.trace "Projects: $projects"

		def items = session.currentProjectId ?
					itemManagerService.listItems( [ projectId: session.currentProjectId ] ) :
					[]
		log.trace "Items: $items"

		// The model
		[ projects: projects, items: items ]
	}

	def viewItems( Long id ) {
		def projects = itemManagerService.listProjects()
		if( id in projects*.id ) {
			// if a valid project id was provided (otherwise we'll ignore it)
			session.currentProjectId = id
			session.currentProjectName = projects.find { it.id == id }.name
		}
		
		redirect action: 'index'
	}

	def newProject( String projectName ) {
		log.debug "newProject( '$projectName' ) - START"
		log.debug "newProject() params: $params"

		if( !projectName ) {
			flash.error = "You must provide a project name to add one."
		}
		else {
			try {
				itemManagerService.addProject( projectName )
			}
			catch( ValidationException ve ) {
				// TODO: This is far from proper handling of the situation, improve if time permits
				flash.error = ve.message
			}
		}
		
		redirect action: 'index'
	}

	def removeProject( Long id ) {
		log.debug "removeProject( $id ) - START"
		
		try {
			itemManagerService.removeProject( id )

			if( id == session.currentProjectId ) {
				session.currentProjectId = null
				session.currentProjectName = null
			}
		}
		catch( EntityNotFound e ) {
			log.warn "Failed to remove entity: ${e.message}"
			log.debug e
			flash.error = e.message
		}

		redirect action: 'index'
	}

	def removeItem( Long id ) {
		log.debug "removeItem( $id ) - START"
		
		try {
			itemManagerService.removeItem( id )
		}
		catch( EntityNotFound e ) {
			log.warn "Failed to remove entity: ${e.message}"
			log.debug e
			flash.error = e.message
		}

		redirect action: 'index'
	}
}
