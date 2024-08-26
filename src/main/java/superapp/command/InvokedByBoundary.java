package superapp.command;

import superapp.users.UserIdBoundary;

public class InvokedByBoundary {
	private UserIdBoundary userId;

	public InvokedByBoundary() {
		super();
	}

	public InvokedByBoundary(String superApp, String email) {
		this.userId = new UserIdBoundary(superApp, email);
	}

	public UserIdBoundary getUserId() {
		return userId;
	}

	public void setUserId(UserIdBoundary userId) {
		this.userId = userId;
	}

}
