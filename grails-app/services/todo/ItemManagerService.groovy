package todo

import grails.transaction.Transactional
import grails.validation.Validateable

@Transactional
class ItemManagerService {
	/**
	 * Returns a list of projects and item count for each.
	 */
	List listProjects() {
	}
	
	/**
	 * Create a new project.
	 *
	 * @param description The description for the new project.
	 * @return the id of the new project
	 */
	def addProject( String description ) {
	}

	/**
	 * Delete an existing project - and all it's items (cascade delete)
	 *
	 * @param projectId the id of the project to delete
	 * @return true on successful deletion
	 */
	boolean removeProject( def projectId ) {
	}

	/**
	 * Gets a list of all items for a project - or all items if projectId is null.
	 *
	 * @param filter a map of filter values, acceptable keys are: projectId, done, dueDate and
	 *               labels[]
	 * @return 
	 */
	List listItems( Map filter ) {
	}

	/**
	 * Add a new item to a project as specified in itemDetails.projectId.
	 *
	 * @param itemDetails the details of the new item
	 * @return the id of the new item
	 */
	def addItem( ItemCommand itemDetails ) {
	}

	/**
	 * Removes a specific item.
	 *
	 * @param itemId The id of the item to remove
	 * @return true if successful
	 */
	boolean removeItem( def itemId ) {
	}

	/**
	 * Adds the specified label to an item - creating the label in the db if it doesn't exist
	 *
	 * @param itemId The id of the item which to add the label to
	 * @param label The string of the label - new label will be created if it doesn't exist
	 * @return true if successful
	 */
	boolean addLabel( def itemId, String label ) {
	}

	boolean removeLabel( def itemId, String label ) {
	}

	/**
	 * List all labels in the system, or just for the specified project.
	 *
	 * @param itemId the id of an item if you wish only the labels for that item - defaults to null
	 *               to retrieve all labels in the system
	 * @return a list of strings representing the labels
	 */
	List listLabels( def itemId = null ) {
	}
}

@Validateable
class ItemCommand {
	def projectId
	Item.Priority priority
	String description
	Date dueDate

	static constraints = {
		importFrom Item
	}
}
