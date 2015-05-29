package todo

import grails.test.spock.IntegrationSpec

class ItemIntegrationSpec extends IntegrationSpec {
	final testItemName = 'Simple ToDo'
	final testLabelName = 'Test Label'
	def testProject

    def setup() {
		testProject = new Project( name: 'ItemIntegrationSpec Project' )
		testProject.save( failOnError: true )
    }

    void "test creating a basic item"() {
		given: 'a newly created todo item'
			testProject.addToItems( new Item( description: testItemName ) )
				.save( failOnError: true )

		expect: 'the project to have one todo item'
			testProject.items.size() == 1
			Item.countByProject( testProject ) == 1
    }

	void 'test creating a fully defined item and adding labels'() {
		given: 'a newly created todo item'
			testProject.addToItems( new Item( description: testItemName ) )
				.save( failOnError: true )

		when: 'the item is labelled'
			def item = Item.findByDescription( testItemName )
			item.addToLabels( new Label( name: testLabelName ) )
				.save( failOnError: true )

		then: 'the item can be found by the label name'
			def label = Label.findByName( testLabelName )
			def foundItem = Item.withCriteria {
				labels {
					eq 'id', label.id
				}
			}
			foundItem.size() == 1
			foundItem[0].id == item.id
	}

	void 'test finding all items with a specific label'() {
		given: 'a collection of items with various labels'
			def labelOne = new Label( name: 'one' )
			def labelTwo = new Label( name: 'two' )

			for( i in 1..10 ) {
				def newItem = new Item( description: "$i" )
				if( i % 2 == 0 )
					newItem.addToLabels( labelTwo )
				else
					newItem.addToLabels( labelOne )
				testProject.addToItems( newItem ).save( failOnError: true )
			}

		when: 'the items are retrieved by label'
			def itemsWithLabelOne = Label.findByName( 'one' ).items
			def itemsWithLabelTwo = Label.findByName( 'two' ).items

		then: 'that we can find the correct groupings'
			itemsWithLabelOne.size() == 5
			itemsWithLabelOne.each { item ->
				assert new Integer( item.description ) % 2 != 0
			}

			itemsWithLabelTwo.size() == 5
			itemsWithLabelTwo.each { item ->
				assert new Integer( item.description ) % 2 == 0
			}
	}
}
