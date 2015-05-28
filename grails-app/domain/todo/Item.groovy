package todo

class Item {
	Priority priority
	String description
	Date dueDate

	Date dateCreated
	Date lastUpdated

    static constraints = {
		priority nullable: true
		description maxSize: 200, blank: false
		dueDate nullable: true
    }

	static belongsTo = [ project: Project ]
	static hasMany = [ labels: Label ]

	enum Priority {
		HIGH( 1 ), MEDIUM( 2 ), LOW( 3 )
		final Integer id

		public Priority( Integer idVal ) {
			this.id = idVal
		}
	}
}
