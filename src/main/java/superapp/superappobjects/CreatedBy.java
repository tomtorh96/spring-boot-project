package superapp.superappobjects;

import superapp.users.UserIdBoundary;

public class CreatedBy {
	private UserIdBoundary userId;
	public CreatedBy() {};
	public CreatedBy(UserIdBoundary userId) {
		this.userId = userId;
	}

	public UserIdBoundary getUserId() {
		return userId;
	}

	public void setUserId(UserIdBoundary userid) {
		this.userId = userid;
	}

	@Override
	public String toString() {
		return "CreatedBy [userid=" + userId + "]";
	}
	
}
