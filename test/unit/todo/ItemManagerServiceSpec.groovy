package todo

import grails.test.mixin.TestFor
import spock.lang.Specification
import todo.ItemManagerService.EntityNotFound
import todo.ItemManagerService.BusinessRuleViolation

@TestFor(ItemManagerService)
@Mock([ Project, Item, Label ])
class ItemManagerServiceSpec extends Specification {
    void "test that adding a new project results in it showing up in the list of projects"() {
		given: 'a newly added project'
			final projectName = 'Testing add project'
			service.addProject( projectName )

		when: 'a list of projects is requested'
			def projects = service.listProjects()

		then: 'the correct list is returned'
			projects.size() == 1 // There is only one project - as I only added one
			projects[0].name == projectName // project has the specified name
			projects[0].itemCount == 0 // the project has no items yet
    }

	void 'test that the list of projects shows the correct itemCounts'() {
		given: 'a collection of projects and items'
			def projectsDefinition = [ 'Project One': 10, 'Project Two': 5, 'Project Three': 12 ]
			projectsDefinition.each { name, itemCount ->
				def projId = service.addProject( name )
				for( i in 1..itemCount )
					service.addItem( new ItemCommand( projectId: projId, description: "$name - $i of $itemCount" ) )
			}

		when: 'a list of projects is requested'
			def projects = service.listProjects()

		then: 'the correct list is returned'
			projects.size() == projectsDefinition.size()
			for( project in projects ) {
				assert projectsDefinition.containsKey( project.name )
				assert projectsDefinition[ project.name ] == project.itemCount
			}
	}

	void 'test that you can successfully remove a project'() {
		given: 'Several newly created projects - plus a special one'
			for( i in 1..10 )
				service.addProject( "New Project - $i" )
			final targetProjectName = 'Delete Me'
			def targetProjectId = service.addProject( targetProjectName )

		when: 'the project is deleted'
			service.removeProject( targetProjectId )

		then: 'the resultant state is correct'
			// Check the size is as it was
			service.listProjects().size() + 1 == old( service.listProjects().size() )
			// Ensure there is no project with the deleted name - i.e. it deleted the right one
			service.listProjects()*.name.find { it == targetProjectName } == null
	}

	void 'test that attempts to remove a bogus project fails correctly'() {
		when: 'a call to remove a bogus project'
			service.removeProject( 1234567 )

		then: 'the correct exception is thrown'
			thrown EntityNotFound
	}

	void 'test that deleting a project, also deletes all its items'() {
		given: 'a new project'
			def projId = service.addProject( 'Test cascade delete' )

		when: 'a collection of items are added to the project, and the project is then deleted'
			for( i in 1..10 ) {
				service.addItem( new ItemCommand( projectId: projId, description: "delete me - $i" ) )
			}
			service.removeProject( projId )

		then: 'the item count is back to what it was at the start'
			Project.count() == old( Project.count() ) - 1
			Item.count() == old( Item.count() )
	}

	void 'test that you can add a new item to a project'() {
		given: 'a project for testing'
			def projId = service.addProject( 'Test adding items' )

		when: 'a new item is added'
			def itemId = service.addItem( new ItemCommand( projectId: projId, description: 'added item', dueDate: new Date( '18-JAN-2080' ) ) )

		then: 'the item count is reflected on the project, and the item is in the list'
			service.listProjects().find{ it.id == projId }.itemCount == 1
			service.listItems( [ projectId: projId ] ).find { item -> item.id == itemId }
	}

	void "test that you can't add a new item with a due date in the past"() {
		when: 'an attempt to add a item that was due in the past'
			def projId = service.addProject( 'Test adding past items' )
			service.addItem( new ItemCommand( projectId: projId, description: 'past item', dueDate: new Date( '1-JAN-1901' ) ) )

		then: 'a suitable exception'
			thrown BusinessRuleViolation
	}

	void 'test that addItem fails sensibly when provided with a bogus projectId'() {
		when: 'An attempt to create a project with a bogus projectId'
			service.addItem( new ItemCommand( projectId: 1234567, description: 'bogus project test') )

		then: 'a suitable exception'
			thrown EntityNotFound
	}

	void 'test that addItem fails sensibly when provided with an item that fails constraints'() {
		when: 'An attempt to create a project with blank description'
			service.addItem( new ItemCommand( projectId: 1234567, description: '') )

		then: 'a suitable exception'
			thrown IllegalArgumentException
	}

	void 'test the filter for listItems'() {
		given: 'a collection of items'
			def projOneId = service.addProject( 'Project One' )
			for( i in 1..10 ) 
				service.addItem( new ItemCommand( projectId: projOneId, description: "Project One - $i" ) )
			service.addItem( new ItemCommand( projectId: projOneId, description: "Project One - done item", done: true ) )

			def projTwoId = service.addProject( 'Project Two' )
			for( i in 1..10 ) 
				service.addItem( new ItemCommand( projectId: projTwoId, description: "Project Two - $i" ) )
			service.addItem( new ItemCommand( projectId: projTwoId, description: "Project Two - done item", done: true ) )

		expect: 'the filter to return the correct amount'
			// Ideally the below would have been done with a parameterized test (where clause), however due to the
			// need to pass in the returned projectIds it got a bit involved - so may revisit later
			[ [ null, 22 ],
			  [ [done: true], 2 ],
			  [ [projectId: projOneId], 11 ],
			  [ [projectId: projTwoId], 11 ],
			  [ [projectId: projOneId, done: true], 1 ],
			  [ [projectId: projTwoId, done: true], 1 ] ].each { testSet ->
			  	assert service.listItems( testSet[0] ).size() == testSet[1]
			  }
	}

	void 'test filtering items by label'() {
		given: 'a collection of items with labels'
			final labelOne = 'label-one', labelTwo = 'label-two'
			def projId = service.addProject( 'Items with labels for filtering' )
			for( i in 1..5 ) {
				def itemId = service.addItem( new ItemCommand( projectId: projId, description: "First Batch - $i" ) )
				service.addLabel( itemId, labelOne )
			}
			for( i in 6..10 ) {
				def itemId = service.addItem( new ItemCommand( projectId: projId, description: "Second Batch - $i" ) )
				service.addLabel( itemId, labelTwo )
			}
			for( i in 11..15 ) {
				def itemId = service.addItem( new ItemCommand( projectId: projId, description: "Third Batch - $i" ) )
				service.addLabel( itemId, labelOne )
				service.addLabel( itemId, labelTwo )
			}

		when: 'the items are filtered to label'
			def labelOneItems = service.listItems( [ labels: [ labelOne ] ] )
			def labelTwoItems = service.listItems( [ labels: [ labelTwo ] ] )
			def bothLabelsItems = service.listItems( [ labels: [ labelOne, labelTwo ] ] )

		then: 'the correct items are captured'
			// check counts
			labelOneItems.size() == 10
			labelTwoItems.size() == 10
			bothLabelsItems.size() == 15
	}

	void 'test that we can use listItems to get a single item'() {
		given: 'a new project with an item'
			def projId = service.addProject( 'Single Item Project' )
			final itemDescription = 'List me!'
			def itemId = service.addItem( new ItemCommand( projectId: projId, description: itemDescription ) )

		when: 'I retrieve the item'
			def result = service.listItems( [ itemId: itemId ] )

		then: 'that we can retrieve that item'
			result.size() == 1
			result[0].id == itemId
			result[0].item.description == itemDescription
	}

	void 'test that we can remove an item'() {
		given: 'a project with some items'
			def targetItemName = 'Delete me'
			def projId = service.addProject( 'Test item removal' )
			service.addItem( new ItemCommand( projectId: projId, description: 'Keep me - 1' ) )
			def targetItemId = service.addItem( new ItemCommand( projectId: projId, description: targetItemName ) )
			service.addItem( new ItemCommand( projectId: projId, description: 'Keep me - 2' ) )

		when: 'the item is removed'
			service.removeItem( targetItemId )

		then: 'the item is correctly removed'
			service.listItems( [ projectId: projId ] ).size() == old( service.listItems( [ projectId: projId ] ).size() ) - 1
			service.listItems( [ projectId: projId ] )*.item.description.find { it == targetItemName } == null
	}

	void 'test adding a label to an item'() {
		given: 'a new item with a label'
			def projId = service.addProject( 'Label Tests' )
			def itemId = service.addItem( new ItemCommand( projectId: projId, description: 'Item to label' ) )

		when: 'a label is added'
			final firstLabel = 'first-label'
			service.addLabel( itemId, firstLabel )

		then: 'the label should be present on the item'
			service.listItems( [ itemId: itemId ] )[0].labels == [ firstLabel ]
			service.listLabels( itemId ) == [ firstLabel ]

		when: 'an additional label is added'
			final secondLabel = 'second-label'
			service.addLabel( itemId, secondLabel )

		then: 'the label should be present, as should the previous'
			service.listItems( [ itemId: itemId ] )[0].labels as Set == [ firstLabel, secondLabel ] as Set
			service.listLabels( itemId ) as Set == [ firstLabel, secondLabel ] as Set
	}

	void 'test listing labels across all items'() {
		given: 'a couple of items with a label'
			def projId = service.addProject( 'Label Tests' )
			def itemOneId = service.addItem( new ItemCommand( projectId: projId, description: 'Item One to label' ) )
			def itemTwoId = service.addItem( new ItemCommand( projectId: projId, description: 'Item Two to label' ) )

		when: 'labels are added to both items'
			service.addLabel( itemOneId, 'labelOne' )
			service.addLabel( itemTwoId, 'labelTwo' )

		then: 'That the label count covers both items'
			service.listLabels().size() == old( service.listLabels().size() ) + 2
	}

	void 'test removing a label'() {
		given: 'an item with a few labels'
			def projId = service.addProject( 'Delete Labels Test' )
			def itemId = service.addItem( new ItemCommand( projectId: projId, description: 'So many labels' ) )
			def labelNames = [ 'label-one', 'label-two', 'label-three' ]
			labelNames.each { labelName ->
				service.addLabel( itemId, labelName )
			}

		when: 'a label is remove'
			service.removeLabel( itemId, labelNames[1] )
			labelNames.remove( 1 )

		then: 'the list should be correct'
			service.listLabels( itemId ) as Set == labelNames as Set

		when: 'the rest of the labels are removed'
			labelNames.each { name ->
				service.removeLabel( itemId, name )
			}

		then: 'all still works - and there are no labels on the item'
			service.listLabels( itemId ).size() == 0
	}

	void 'test removing a non-existent label causes no pain'() {
		given: 'an item with no labels'
			def projId = service.addProject( 'No labelled item project' )
			def itemId = service.addItem( new ItemCommand( projectId: projId, description: 'No labels here' ) )

		when: 'a non-existent label is removed'
			service.removeLabel( itemId, 'no-such-label' )

		then: 'an appropriate exception is thrown'
			thrown EntityNotFound
	}

	void 'test that an item can have values updated'() {
		given: 'a fully specified item'
			def projId = service.addProject( 'Test updates Project' )
			def itemId = service.addItem( new ItemCommand (
					projectId: projId,
					description: 'Update Me',
					dueDate: new Date( '1-JAN-2020' ),
					priority: Item.Priority.MEDIUM
				) )

		when: 'the item is updated and then retrieved'
			ItemCommand updatedItem = service.listItems( [ itemId: itemId ] )[0].item
			updatedItem.with {
				description = 'Update Me - now updated'
				dueDate = new Date() + 1
				priority = Item.Priority.LOW
				done = true
			}
			def updateResult = service.updateItem( itemId, updatedItem )
			// Retrieve the new and improved version
			ItemCommand newlyUpdatedItem = service.listItems( [ itemId: itemId ] )[0].item	

		then: 'the new values are properly stored'
			updateResult == true
			newlyUpdatedItem.description == updatedItem.description
			newlyUpdatedItem.dueDate == updatedItem.dueDate
			newlyUpdatedItem.priority == updatedItem.priority
			newlyUpdatedItem.done == updatedItem.done
	}
}
