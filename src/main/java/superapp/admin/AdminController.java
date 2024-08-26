package superapp.admin;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import exception.NoPremissionException;
import superapp.command.EnhancedCommandService;
import superapp.command.MiniAppCommandBoundary;
import superapp.superappobjects.ObjectService;
import superapp.users.EnhancedUsersService;
import superapp.users.UserBoundary;
import superapp.users.UserRoleEnum;

@RestController
@RequestMapping(path = { "/superapp/admin" })
public class AdminController {
	private EnhancedUsersService userService;
	private ObjectService objectService;
	private EnhancedCommandService commandService;

	public AdminController(EnhancedUsersService userService, ObjectService objectService,
			EnhancedCommandService commandService) {
		this.userService = userService;
		this.objectService = objectService;
		this.commandService = commandService;
	}

	@DeleteMapping(path = { "/users" })
	public void deleteAllUsers(@RequestParam(name = "userSuperapp", required = true) String userSuperapp,
			@RequestParam(name = "userEmail", required = true) String email) {
		if (checkPremission(userSuperapp, email) != UserRoleEnum.ADMIN) {
			throw new NoPremissionException("No premissions, only admins have access to this command");
		}
		this.userService.deleteAllUsers();
	}

	@GetMapping(path = { "/users" }, produces = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary[] getAllUsers(@RequestParam(name = "userSuperapp", required = true) String userSuperapp,
			@RequestParam(name = "userEmail", required = true) String email,
			@RequestParam(name = "size", required = false, defaultValue = "10") int size,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page) {
		if (checkPremission(userSuperapp, email) != UserRoleEnum.ADMIN) {
			throw new NoPremissionException("No premissions, only admins have access to this command");
		}
		return this.userService.getAllUsers(size, page).toArray(new UserBoundary[0]);
	}

	@DeleteMapping(path = { "/objects" })
	public void deleteAllObjects(@RequestParam(name = "userSuperapp", required = true) String userSuperapp,
			@RequestParam(name = "userEmail", required = true) String email) {
		if (checkPremission(userSuperapp, email) != UserRoleEnum.ADMIN) {
			throw new NoPremissionException("No premissions, only admins have access to this command");
		}
		this.objectService.deleteAllObjects();
	}

	@DeleteMapping(path = { "/miniapp" })
	public void deleteAllCommands(@RequestParam(name = "userSuperapp", required = true) String userSuperapp,
			@RequestParam(name = "userEmail", required = true) String email) {
		if (checkPremission(userSuperapp, email) != UserRoleEnum.ADMIN) {
			throw new NoPremissionException("No premissions, only admins have access to this command");
		}
		this.commandService.deleteAllMiniAppCommands();
	}

	@GetMapping(path = { "/miniapp" }, produces = MediaType.APPLICATION_JSON_VALUE)
	public MiniAppCommandBoundary[] getAllMiniAppCommands(
			@RequestParam(name = "userSuperapp", required = true) String userSuperapp,
			@RequestParam(name = "userEmail", required = true) String email,
			@RequestParam(name = "size", required = false, defaultValue = "10") int size,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page) {
		if (checkPremission(userSuperapp, email) != UserRoleEnum.ADMIN) {
			throw new NoPremissionException("No premissions, only admins have access to this command");
		}
		return this.commandService.getAllMiniAppCommands(size, page).toArray(new MiniAppCommandBoundary[0]);
	}

	@GetMapping(path = { "/miniapp/{miniAppName}" }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public MiniAppCommandBoundary[] searchCommandsByMiniApp(@PathVariable("miniAppName") String miniAppName,
			@RequestParam(name = "userSuperapp", required = true) String userSuperapp,
			@RequestParam(name = "userEmail", required = true) String email,
			@RequestParam(name = "size", required = false, defaultValue = "10") int size,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page) {
		if (checkPremission(userSuperapp, email) != UserRoleEnum.ADMIN) {
			throw new NoPremissionException("No premissions, only admins have access to this command");
		}
		return this.commandService.getAllMiniAppCommandsByMiniAppName(miniAppName, size, page)
				.toArray(new MiniAppCommandBoundary[0]);
	}

	private UserRoleEnum checkPremission(String userSuperApp, String email) {
		return userService.getSpecificUser(userSuperApp, email).get().getRole();
	}
}
