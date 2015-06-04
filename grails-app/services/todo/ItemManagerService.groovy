package todo

import grails.transaction.Transactional
import grails.validation.Validateable

@Transactional
class ItemManagerService {
	/**
	 * Returns a list of projects and item count for each.
	 */
	List listProjects() {
		log.debug "listProjects() - START"
		def projects = []
		Project.list().each { proj ->
			projects.add( [ id: proj.id, name: proj.name, itemCount: Item.countByProject( proj ) ] )
		}

		log.debug "returning ${projects.size()} projects"
		projects
	}
	
	/**
	 * Create a new project.
	 *
	 * @param name The name for the new project.
	 * @return the id of the new project
	 */
	def addProject( String name ) {
		log.debug "addProject( $name ) - START"

		def newProject = new Project( name: name )
		newProject.save( failOnError: true )

		newProject.id
	}

	/**
	 * Delete an existing project - and all it's items (cascade delete)
	 *
	 * @param projectId the id of the project to delete
	 */
	void removeProject( def projectId ) {
		def proj = Project.get( projectId )
		if( !proj )
			throw new EntityNotFound( projectId, "Unable to remove Project, as Project not found - id: $projectId" )

		proj.items.each { item ->
			// delegate delete to appropriate method so that we can do
			// and specific business logic
			removeItem( item.id )
		}
		proj.delete()
		log.info "Deleted project: ${proj.name}"
	}

	/**
	 * Gets a list of items as per the filter, or all if filter is null. Also used to get a single
	 * item by providing an itemId in the filter - this is instead of having a seperate get method
	 * which would do the same.
	 *
	 * @param filter a map of filter values, acceptable keys are: itemId, projectId, done, dueDate and
	 *               labels[]
	 * @return 
	 */
	List listItems( Map filter = null ) {
		log.debug "listItems( $filter ) - START"

		def items = []
		Item.findAll {
			if( filter?.itemId ) {
				id == filter.itemId
			}
			if( filter?.projectId ) {
				project { id == filter.projectId }
			}
			if( filter?.done ) {
				done == filter.done
			}
			if( filter?.dueDate ) {
				dueDate == filter.dueDate
			}
			if( filter?.labels ) {
				labels { name in filter.labels }
			}
		}.each { item ->
			items << [ id: item.id, item: new ItemCommand( item ), labels: listLabels( item.id ) ]
		}

		items
	}

	/**
	 * Add a new item to a project as specified in itemDetails.projectId.
	 *
	 * @param itemDetails the details of the new item
	 * @return the id of the new item
	 */
	def addItem( ItemCommand itemDetails ) {
		log.debug "addItem() - START"

		// First, let's do some validation
		if( !itemDetails?.validate() )
			throw new IllegalArgumentException( 'itemDetails failed validation, please ensure you validate before calling.' )
		// Next some business rule validation
		validateItemBusinessRules( itemDetails )
		// Lastly let's get the target project
		def proj = Project.get( itemDetails.projectId )
		if( !proj )
			throw new EntityNotFound( itemDetails.projectId, 'Invalid project id, unable to find specified project' )

		// Now to update the db
		Item newItem = itemDetails.toItem()
		proj.addToItems( newItem ).save( failOnError: true )

		newItem.id
	}

	/**
	 * Removes a specific item.
	 *
	 * @param itemId The id of the item to remove
	 * @return true if successful
	 */
	void removeItem( def itemId ) {
		Item targetItem = Item.get( itemId )
		if( !targetItem )
			throw new EntityNotFound( itemId, "Unable to remove item, as item not found - id: $itemId" )

		targetItem.delete()
		log.info "Deleted item: ${targetItem.description}"
	}

	/**
	 * Method to update details of an item, such as updating description, dueDate and 'done' status.
	 *
	 * @param updateDetails
	 * @return true on success
	 */
	boolean updateItem( def itemId, ItemCommand updateDetails ) {
		Item targetItem = Item.get( itemId )
		if( !targetItem )
			throw new EntityNotFound( itemId, 'Unable to update item, as specified item was not found' )
		if( !updateDetails?.validate() )
			throw new IllegalArgumentException( 'itemDetails failed validation, please ensure you validate before calling.' )
		validateItemBusinessRules( updateDetails )

		// Okay, data looks good, let's apply it
		targetItem.with {
			description = updateDetails.description
			done = updateDetails.done
			dueDate = updateDetails.dueDate
			priority = updateDetails.priority

			it
		}.save( failOnError: true )
	}

	/**
	 * Adds the specified label to an item - creating the label in the db if it doesn't exist
	 *
	 * @param itemId The id of the item which to add the label to
	 * @param label The string of the label - new label will be created if it doesn't exist
	 * @return true if successful
	 */
	boolean addLabel( def itemId, String label ) {
		log.debug "addLabel( $itemId, '$label' ) - START"

		Item targetItem = Item.get( itemId )
		if( !targetItem )
			thrown new EntityNotFound( itemId, 'Unable to find specified item by id' )

		Label targetLabel = Label.findByName( label )
		if( !targetLabel ) {
			log.debug "Label '$label' does note exist, attempting to create"
			targetLabel = new Label( name: label ) 
			if( !targetLabel.validate() )
				throw new IllegalArgumentException( "Provided label of '$label' failed data constraints, please check" )
			targetLabel.save( failOnError: true )
			log.info "Created new label: '$label'"
		}

		targetItem.addToLabels( targetLabel ).save( failOnError: true )
	}

	/**
	 * Remove a label from an item.
	 */
	boolean removeLabel( def itemId, String label ) {
		Item sourceItem = Item.get( itemId )
		if( !sourceItem )
			throw new EntityNotFound( itemId, 'Unable to remove label, as specified Item not found' )

		Label targetLabel = Label.findByName( label )
		if( !targetLabel )
			throw new EntityNotFound( label, 'Specified label does not exists in system, unable to remove' )

		sourceItem.removeFromLabels( targetLabel ).save( failOnError: true )
	}

	/**
	 * List all labels in the system, or just for the specified project.
	 *
	 * @param itemId the id of an item if you wish only the labels for that item - defaults to null
	 *               to retrieve all labels in the system
	 * @return a list of strings representing the labels
	 */
	List listLabels( def itemId = null ) {
		if( !itemId ) {
			// return all labels
			return Label.list()*.name
		}

		// Else, get the labels for a specific item
		Item sourceItem = Item.get( itemId )
		if( !sourceItem )
			throw new EntityNotFound( itemId, 'Unable to list labels, as specified Item not found' )

		sourceItem.labels*.name
	}

	/**
	 * A simple method to encapsulate BR validation for an item.
	 */
	void validateItemBusinessRules( ItemCommand itemDetails ) {
		if( itemDetails.dueDate && itemDetails.dueDate < new Date() )
			throw new BusinessRuleViolation( 'the dueDate - if specified - must not be earlier than now' )
	}

	/**
	 * Exception to indicate a specified entity could not be found
	 * by it's provided id.
	 */
	class EntityNotFound extends Exception {
		/** The requested id */
		def entityId

		EntityNotFound( def id, String message ) {
			super( message )
			this.entityId = id
		}
	}

	/**
	 * Exception to indicate that business rule has been violated.
	 */
	class BusinessRuleViolation extends Exception {
		BusinessRuleViolation( String message ) {
			super( message )
		}
	}
}

@Validateable
class ItemCommand {
	Long projectId
	Item.Priority priority
	String description
	Date dueDate
	boolean done = false

	static constraints = {
		importFrom Item
	}

	ItemCommand(){}

	ItemCommand( Item domainItem ) {
		projectId = domainItem.project.id
		priority = domainItem.priority
		description = domainItem.description
		dueDate = domainItem.dueDate
		done = domainItem.done
	}

	/**
	 * Generate a domain class instance from the command object.
	 *
	 * @return a new domain class instance
	 */
	Item toItem() {
		new Item().with {
			priority = this.priority
			description = this.description
			dueDate = this.dueDate
			done = this.done

			it
		}
	}

	String toString() {
		"projectId: $projectId, priority: $priority, dueDate: $dueDate, done: $done, description: $description"
	}
}
