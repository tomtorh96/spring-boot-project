package superapp;

import java.util.Collections;
import java.util.stream.IntStream;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import superapp.superappobjects.CreatedBy;
import superapp.superappobjects.EnhancedObjectService;
import superapp.superappobjects.Location;
import superapp.superappobjects.ObjectBoundary;
import superapp.superappobjects.ObjectId;
import superapp.users.NewUserBoundary;
import superapp.users.UserBoundary;
import superapp.users.UserRoleEnum;
import superapp.users.UserService;

@Component
@Profile("test")
public class Initialization implements CommandLineRunner {
	private EnhancedObjectService objectService;
	private UserService userService;

	public Initialization(EnhancedObjectService objectService, UserService userService) {
		this.objectService = objectService;
		this.userService = userService;
	}

	@Override
	public void run(String... args) throws Exception {
		AddObjects();
		// TODO add startup inputs
		// AddUsers();
		// AddCommaneds();

	}

	private void AddObjects() {
		IntStream.range(0, 10).mapToObj(i -> {
			ObjectBoundary boundary = new ObjectBoundary();
			boundary.setObjectId(new ObjectId("SuperApp #" + i, "1"));
			boundary.setAlias("object #" + i);
			boundary.setType("app");
			boundary.setLocation(new Location(i * 2.0, i * 2.5));
			boundary.setActive(i % 2 == 0);
			NewUserBoundary newuser = new NewUserBoundary();
			newuser.setEmail(i + "@" + "gmail.com");
			newuser.setUsername("tom#" + i);
			newuser.setRole(UserRoleEnum.SUPERAPP_USER);
			newuser.setAvatar("t" + i);
			UserBoundary user = this.userService.createUser(newuser);
			boundary.setCreatedBy(new CreatedBy(user.getUserId()));
			boundary.setObjectDetails(Collections.singletonMap("app", "#" + i));
			return this.objectService.createAnObject(boundary);
		});

	}

}
