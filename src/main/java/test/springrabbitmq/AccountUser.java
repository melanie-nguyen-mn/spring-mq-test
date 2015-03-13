package test.springrabbitmq;

public class AccountUser {
	private String id;
	private String firstName;
	private String lastName;
	
	public AccountUser() {}
	
	
	public AccountUser(String idStr, String firstName, String lastName) {
		this.id = idStr;
		this.firstName = firstName;
		this.lastName = lastName;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public String toString() {
		return "{id: " + this.id + 
				",  firstName: " + this.firstName 
				+ ", lastName:" + this.lastName + "}" ;
	}
}
