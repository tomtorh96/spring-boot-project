package superapp.users;

import java.util.List;

public interface EnhancedUsersService extends UserService {
	public List<UserBoundary> getAllUsers(int size, int page);
}
