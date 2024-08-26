package superapp.miniapps;

import java.util.List;

import javax.naming.NoPermissionException;

import org.springframework.stereotype.Component;
import exception.InputException;
import exception.NoPremissionException;
import superapp.command.MiniAppCommandBoundary;
import superapp.miniapps.ProjectManagementApp.eAction;
import superapp.superappobjects.MiniAppSupportObjectService;
import superapp.superappobjects.ObjectBoundary;
import superapp.users.UserBoundary;
import superapp.users.UserRoleEnum;
import superapp.users.UserService;

@Component("Chat App")
public class ChatApp implements MiniAppAbstraction {
	private UserService userService;
	private MiniAppSupportObjectService objectService;
	private ProjectManagementApp managementService;
	static String type = "chat";
	static String alias = "chat message";

	public ChatApp(UserService userservice, MiniAppSupportObjectService objectservice,
			ProjectManagementApp managementService) {
		this.userService = userservice;
		this.objectService = objectservice;
		this.managementService = managementService;
	}

	/**
	 * Creates a new chat message object based on the provided command attributes.
	 * Validates input, sets necessary details, and creates the object in the
	 * system.
	 * 
	 * @param input The MiniAppCommandBoundary containing command and attributes for
	 *              creating the object. Must not be null; commandAttributes must
	 *              include "project name".
	 * @return The newly created ObjectBoundary representing the chat message
	 *         object.
	 * @throws InputException        If commandAttributes are null or missing
	 *                               "project name", or if other required attributes
	 *                               are not provided.
	 * @throws NotFoundException     If the project specified by "project name" does
	 *                               not exist.
	 * @throws NoPermissionException If the user does not have permission to perform
	 *                               the operation.                          
	 */
	public ObjectBoundary addMiniAppObject(MiniAppCommandBoundary input) throws RuntimeException {
		Object[] userAndNewProject = managementService.initialValidationAndProcessing(input, eAction.ADD, alias, type);
		UserBoundary user = (UserBoundary) userAndNewProject[0];
		ObjectBoundary newOb = (ObjectBoundary) userAndNewProject[1];

		newOb.getObjectDetails().put("sent by", user.getUsername());
		if (input.getCommandAttributes().get("message") != null)
			newOb.getObjectDetails().put("message", input.getCommandAttributes().get("message"));
		else
			newOb.getObjectDetails().put("message", "");

		user.setRole(UserRoleEnum.SUPERAPP_USER);
		userService.updateUser(user.getUserId().getSuperapp(), user.getUserId().getEmail(), user);

		newOb = this.objectService.createAnObject(newOb);

		user.setRole(UserRoleEnum.MINIAPP_USER);
		userService.updateUser(user.getUserId().getSuperapp(), user.getUserId().getEmail(), user);

		return newOb;
	}

	/**
	 * Retrieves a list of active chat message objects related to a
	 * specific project based on the provided command attributes, size, and page.
	 * 
	 * @param input The MiniAppCommandBoundary containing command and attributes for
	 *              retrieving objects. Must not be null; commandAttributes must
	 *              include "project name".
	 * @param size  The maximum number of objects to retrieve per page.
	 * @param page  The page number of the results to retrieve.
	 * @return A list of ObjectBoundary representing the active objects related to
	 *         the specified project.
	 * @throws InputException        If commandAttributes are null or missing
	 *                               "project name".
	 * @throws NotFoundException     If the project specified by "project name" does
	 *                               not exist.
	 * @throws NoPremissionException If the user does not have permission to access
	 *                               the project.                         
	 */
	public List<ObjectBoundary> getAllMiniAppObjects(MiniAppCommandBoundary input, int size, int page)
			throws RuntimeException {
		if (input.getCommandAttributes() == null)
			throw new InputException("No Command attributes provided");

		if (input.getCommandAttributes().get("project name") == null)
			throw new InputException("No project name provided");
		String projectName = input.getCommandAttributes().get("project name").toString();

		managementService.authenticateProjectMemberByEmail(input.getInvokedBy().getUserId().getSuperapp(), projectName,
				input.getInvokedBy().getUserId().getEmail());

		if (input.getCommandAttributes().get("size") != null)
			size = (int) input.getCommandAttributes().get("size");
		if (input.getCommandAttributes().get("page") != null)
			page = (int) input.getCommandAttributes().get("page");

		return this.objectService.findAllByTypeAndProjectNameAndActive(type, projectName, true, size, page);

	}

	
	/**
	 * Updates a chat message object based on the provided command boundary input.
	 *
	 * @param input The MiniAppCommandBoundary containing the details of the update command
	 * @return the updated ObjectBoundary
	 * @throws InputException if the target object is already active when trying to set it as active
	 * @throws NoPremissionException if the user attempting the update is not the original sender of the message
	 */
	@Override
	public ObjectBoundary updateMiniAppObject(MiniAppCommandBoundary input) {
		Object[] userAndTargetMessage = managementService.initialValidationAndProcessing(input, eAction.UPDATE, alias, type);
		UserBoundary user = (UserBoundary) userAndTargetMessage[0];
		ObjectBoundary targetObject = (ObjectBoundary) userAndTargetMessage[1];

		if (user.getUsername().compareTo(targetObject.getObjectDetails().get("sent by").toString()) == 0) {
			if (input.getCommandAttributes().get("active") != null) {
				if ((boolean) input.getCommandAttributes().get("active"))
					throw new InputException(
							"The target object is already active: " + targetObject.getObjectId().toString());
				targetObject.setActive((boolean) input.getCommandAttributes().get("active"));
			} else if (input.getCommandAttributes().get("message") != null) {
				targetObject.getObjectDetails().put("message", input.getCommandAttributes().get("message"));
			}
		} else
			throw new NoPremissionException("Only the user that sent the message can update it");

		user.setRole(UserRoleEnum.SUPERAPP_USER);
		userService.updateUser(user.getUserId().getSuperapp(), user.getUserId().getEmail(), user);

		targetObject = objectService.updateAnObject(targetObject.getObjectId().getSuperapp(),
				targetObject.getObjectId().getId(), input.getInvokedBy().getUserId().getSuperapp(),
				input.getInvokedBy().getUserId().getEmail(), targetObject);

		user.setRole(UserRoleEnum.MINIAPP_USER);
		userService.updateUser(user.getUserId().getSuperapp(), user.getUserId().getEmail(), user);

		return targetObject;
	}

}
