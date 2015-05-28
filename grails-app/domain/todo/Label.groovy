package todo

class Label {
	String name

	Date dateCreated
	Date lastUpdated

    static constraints = {
		name maxSize: 100, blank: false
    }

	static hasMany = [ items: Item]
	static belongsTo = Item
}
