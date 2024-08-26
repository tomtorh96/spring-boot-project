package superapp.users;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import exception.DeprecationException;
import exception.InputException;
import exception.NotFoundException;
import exception.UnauthorizedException;

@Service
public class RDBUserService implements EnhancedUsersService {
	private UserDao userDao;
	private UserConverter userConverter;
	private String superApp;

	public RDBUserService(UserDao userDao, UserConverter userConverter) {
		this.userDao = userDao;
		this.userConverter = userConverter;
	}

	@Value("${spring.application.name:JILL}")
	public void setDefaultSuperAppName(String name) {
		this.superApp = name;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<UserBoundary> getSpecificUser(String superApp, String email){
		String id = this.userConverter.convertToEntityId(superApp, email);
		Optional<UserEntity> entityOp = this.userDao.findById(id);
		Optional<UserBoundary> boundaryOp = entityOp.map(this.userConverter::toBoundary);

		if (boundaryOp.isEmpty())
			throw new UnauthorizedException("The request lacks valid authentication credentials No User found with the following  email: " + email);

		return boundaryOp;
	}

	@Override
	@Deprecated
	public List<UserBoundary> getAllUsers() {
		throw new DeprecationException("You should not invoke this method");
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserBoundary> getAllUsers(int size, int page) {
		return this.userDao.findAll(PageRequest.of(page, size, Direction.DESC, "userId")).stream()
				.map(this.userConverter::toBoundary).toList();
	}

	@Override
	@Transactional(readOnly = false)
	public UserBoundary createUser(NewUserBoundary newUserBoundary) {
	
		if (newUserBoundary.getEmail() == null)
			throw new InputException(" Unable to create new user ,no email was filled ");
		if (EmailValidation.isValidEmail(newUserBoundary.getEmail()) == false) 
			throw new InputException("Incorrect email syntax");	
		if (newUserBoundary.getUsername().isEmpty() || newUserBoundary.getUsername() == null)
			throw new InputException(" Unable to create new user ,no userName was filled ");
		if (newUserBoundary.getRole() == null)
			throw new InputException(" No role was filled ");
		if (newUserBoundary.getAvatar().isEmpty() || newUserBoundary.getAvatar() == null)
			throw new InputException(" No avatar was filled ");

		String id = this.userConverter.convertToEntityId(superApp, newUserBoundary.getEmail());
		if (this.userDao.findById(id).isPresent())
			throw new RuntimeException(" This email is already beeing used ");
		UserBoundary userBoundary = new UserBoundary();

		userBoundary.setAvatar(newUserBoundary.getAvatar());
		userBoundary.setRole(newUserBoundary.getRole());
		userBoundary.setUsername(newUserBoundary.getUsername());
		userBoundary.setUserId(new UserIdBoundary(superApp, newUserBoundary.getEmail()));
		UserEntity entity = this.userConverter.toEntity(userBoundary);
		entity = this.userDao.save(entity);
		return this.userConverter.toBoundary(entity);
	}

	@Override
	@Transactional(readOnly = false)
	public void deleteAllUsers() {
		this.userDao.deleteAll();
	}

	@Override
	@Transactional(readOnly = false)
	public void updateUser(String superApp, String email, UserBoundary update) {
		String id = this.userConverter.convertToEntityId(superApp, email);
		UserEntity entity = this.userDao.findById(id)
				.orElseThrow(() -> new NotFoundException("*Could not find user for update by id: " + id));

		// ignore id
		// ignore superApp

		if (update.getAvatar() != null && !update.getAvatar().isEmpty()) {
			entity.setAvatar(update.getAvatar());
		}
		if (update.getRole() != null) {
			entity.setRole(userConverter.toEntity(update.getRole()));
		}
		if (update.getUsername() != null && !update.getUsername().isEmpty()) {
			entity.setUsername(update.getUsername());
		}
		entity = this.userDao.save(entity);
	}

}
