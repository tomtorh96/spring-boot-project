package superapp.users;

public class UserIdBoundary {
	private String superapp;
	private String email;
	
	public UserIdBoundary() {
	}
	
	public UserIdBoundary(String superApp, String email) {
		this.superapp = superApp;
		this.email = email;
	}

	public String getSuperapp() {
		return superapp;
	}
	
	public void setSuperapp(String superApp) {
		this.superapp = superApp;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	@Override
	public String toString() {
		return "UserIdBoundary [superApp=" + superapp + ", email=" + email + "]";
	}
	
}
