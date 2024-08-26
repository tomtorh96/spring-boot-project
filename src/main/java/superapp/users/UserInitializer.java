package superapp.users;

import java.util.stream.IntStream;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("manualUsers")
public class UserInitializer implements CommandLineRunner {
	private EnhancedUsersService userService;

	public UserInitializer(EnhancedUsersService userService) {
		this.userService = userService;

	}

	@Override
	public void run(String... args) throws Exception {

		NewUserBoundary newAdminBoundary = new NewUserBoundary();
		newAdminBoundary.setAvatar("GOD");
		newAdminBoundary.setRole(UserRoleEnum.ADMIN);
		newAdminBoundary.setEmail("admin@s.afeka.ac.il");
		newAdminBoundary.setUsername("admin");
		userService.createUser(newAdminBoundary);

		IntStream.range(0, 10).mapToObj(i -> {
			NewUserBoundary boundary = new NewUserBoundary();
			boundary.setRole(UserRoleEnum.MINIAPP_USER);
			boundary.setEmail("miniAppUser" + String.valueOf(i) + "@s.afeka.ac.il");
			boundary.setUsername("miniAppUser" + String.valueOf(i));
			boundary.setAvatar(String.valueOf(i % 2 == 0));
			return this.userService.createUser(boundary);
		}).forEach(boundary -> System.err.println("* " + boundary));

		for (int j = 0; j < 10; j++) {
			NewUserBoundary newSuperAppUserboundary = new NewUserBoundary();
			newSuperAppUserboundary.setRole(UserRoleEnum.SUPERAPP_USER);
			newSuperAppUserboundary.setEmail("superAppUser" + String.valueOf(j) + "@s.afeka.ac.il");
			System.err.println(newSuperAppUserboundary.getEmail());
			newSuperAppUserboundary.setUsername("superAppUser" + String.valueOf(j));
			newSuperAppUserboundary.setAvatar(String.valueOf(j % 2 == 0));
			UserBoundary superAppUserboundary = this.userService.createUser(newSuperAppUserboundary);
			System.err.println("** " + superAppUserboundary);
		}

	}

}
