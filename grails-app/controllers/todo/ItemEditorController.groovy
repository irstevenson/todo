package todo

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
				model: [ targetProjectId: id, targetItemId: 0, itemDetails: flash.itemDetails ] )
	}

	/**
	 * edit an existing item
	 *
	 * @param id the id of an existing item
	 */
	def editItem( Long id ) {
		def item = itemManagerService.listItems( [ itemId: id ] )[0].item

		render( view: 'editItem',
				model: [ targetProjectId: item.projectId, targetItemId: id, itemDetails: item ] )
	}

	/**
	 * Saves with changes or creates a new item.
	 *
	 * @param id if 0 assumes creating new item
	 */
	def saveItem( Long id, ItemCommand itemDetails ) {
		log.debug "saveItem( $id ) - START"
		log.debug "itemDetails: $itemDetails"
		if( id == 0 ) {
			log.debug "processing as new item"
			if( !itemDetails.validate() ) {
				flash.itemDetails = itemDetails
				return redirect( action: 'newItem', id: itemDetails.projectId )
			}

			// All is suitable, let's save it
			itemManagerService.addItem( itemDetails )
		}
		else {
			log.debug "updating existing item"
			itemManagerService.updateItem( id, itemDetails )
		}

		redirect controller: 'toDo', action: 'index'
	}
}
