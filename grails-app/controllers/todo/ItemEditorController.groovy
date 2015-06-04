package todo

import grails.validation.ValidationException

class ItemEditorController {
	def itemManagerService

    def index() {
		redirect controlled: 'toDo'
	}

	/**
	 * process for creating a new item.
	 *
	 * @param id the id of the target project for the new item
	 */
	def newItem( Long id ) {
		log.debug "newItem( $id ) - START"
		render( view: 'editItem',
				model: [ targetProjectId: id, targetItemId: 0, itemDetails: flash.itemDetails,
				priorities: itemManagerService.listPriorities() ] )
	}

	/**
	 * edit an existing item
	 *
	 * @param id the id of an existing item
	 */
	def editItem( Long id ) {
		def item = itemManagerService.listItems( [ itemId: id ] )[0].item

		render( view: 'editItem',
				model: [ targetProjectId: item.projectId, targetItemId: id, itemDetails: item,
				priorities: itemManagerService.listPriorities() ] )
	}

	/**
	 * Saves with changes or creates a new item.
	 *
	 * @param id if 0 assumes creating new item
	 */
	def saveItem( Long id, ItemCommand itemDetails ) {
		log.debug "saveItem( $id ) - START"
		log.debug "params: $params"
		log.debug "itemDetails: $itemDetails"

		boolean tryAgain = false

		// Closure for the processing task
		def processRequest = { serviceCall ->
			if( !itemDetails.validate() ) {
				log.debug "itemDetails failed validation"
				tryAgain = true
			}
			else {
				try {
					// All is suitable, let's save it
					serviceCall.call()
				}
				catch( ValidationException ve ) {
					// TODO: This is far from proper handling of the situation, improve if time permits
					flash.error = ve.message
					tryAgain = true
				}
			}
		}

		// perform actual processing
		if( id == 0 ) {
			log.debug "processing as new item"
			processRequest {
				itemManagerService.addItem( itemDetails )
			}

			if( tryAgain ) {
				flash.itemDetails = itemDetails
				return redirect( action: 'newItem', id: itemDetails.projectId )
			}
		}
		else {
			log.debug "updating existing item"
			processRequest {
				itemManagerService.updateItem( id, itemDetails )
			}

			if( tryAgain ) {
				flash.itemDetails = itemDetails
				return redirect( action: 'editItem', id: id )
			}
		}

		// If all was well, back to the index
		redirect controller: 'toDo', action: 'index'
	}
}
