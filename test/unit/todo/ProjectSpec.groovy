package todo

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Project)
class ProjectSpec extends Specification {
    void "test basic constraints"() {
		given: 'a newly setup Project'
			Project testPrj = new Project( name: 'Test Project' )

		expect: 'it passes validation'
			testPrj.validate()
    }

	void "test maxSize constraint"() {
		given: 'a newly setup Project with an excessively long name'
			Project testPrj = new Project( name: "Test Project" * 100 )

		expect: 'it fails validation'
			testPrj.validate() == false
	}

	@spock.lang.Unroll
	void 'test all invalid constraint values with simple parameterisation - #testName'() {
		when: 'a new project is setup with the tested value'
			Project testProj = new Project( name: testName )

		then: 'validate should fail'
			testProj.validate() == false

		where:
			testName << [ null, '', 'long name' * 100 ]
	}

	void 'test all positive and negative ranges with full where clause - expectedResult: #expectedResult, testValue: #testValue'() {
		when: 'a new project is setup with test input'
			Project testProj = new Project( name: testValue )

		then: 'validate should result in the expected outcome'
			testProj.validate() == expectedResult

		where:
			expectedResult | testValue
			true           | 'Good Name'
			false          | null
			false          | '' // blank: false
			false          | 'Long name' * 100
	}
}
