package superapp.users;

import java.util.List;
import java.util.Optional;

public interface UserService {
	public Optional<UserBoundary> getSpecificUser(String supperApp, String email);
	
	@Deprecated
	public List<UserBoundary> getAllUsers();

	public UserBoundary createUser(NewUserBoundary newUser);

	public void deleteAllUsers();

	public void updateUser(String supperApp, String email, UserBoundary update);
	
	
}
