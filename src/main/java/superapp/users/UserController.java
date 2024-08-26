package superapp.users;

import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


//import demo.DemoBoundary;

@RestController
@RequestMapping(path = { "/superapp/users" })
public class UserController {
	private UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary storeUser(@RequestBody NewUserBoundary newUser) {
		return this.userService.createUser(newUser);
	}

	@GetMapping(path = { "/login/{superapp}/{email}" }, produces = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary getSpecificUser(@PathVariable("superapp") String superApp,
			@PathVariable("email") String email) {
		Optional<UserBoundary> userOp = this.userService.getSpecificUser(superApp, email);

		return userOp.get();

	}

	@PutMapping(path = { "/{superapp}/{userEmail}" }, consumes = { MediaType.APPLICATION_JSON_VALUE })
	public void update(@PathVariable("superapp") String superApp, @PathVariable("userEmail") String userEmail,
			@RequestBody UserBoundary update) {
		this.userService.updateUser(superApp, userEmail, update);
	}
}
