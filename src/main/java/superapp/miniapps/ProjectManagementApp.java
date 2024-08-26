package superapp.miniapps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import exception.InputException;
import exception.NoPremissionException;
import exception.NotFoundException;
import exception.UnauthorizedException;
import superapp.command.CommandService;
import superapp.command.MiniAppCommandBoundary;
import superapp.superappobjects.CreatedBy;
import superapp.superappobjects.Location;
import superapp.superappobjects.MiniAppSupportObjectService;
import superapp.superappobjects.ObjectBoundary;
import superapp.users.EnhancedUsersService;
import superapp.users.UserBoundary;
import superapp.users.UserRoleEnum;

@Component("Admin App")
public class ProjectManagementApp implements MiniAppAbstraction {
	private EnhancedUsersService userService;
	private MiniAppSupportObjectService objectService;
	private CommandService commandService;
	static String type = "project";

	public enum eAction {
		ADD, ADD_PROJECT, GET_ALL, GET_ALL_PROJECT, UPDATE;
	}

	public ProjectManagementApp(EnhancedUsersService userService, MiniAppSupportObjectService objectService,
			CommandService commandService) {
		super();
		this.userService = userService;
		this.objectService = objectService;
		this.commandService = commandService;
	}

	/**
	 * Adds a new project object based on the provided command attributes. Validates
	 * and processes attributes such as project name, team leader, and member list
	 * before creation.
	 * 
	 * @param input The MiniAppCommandBoundary containing command and attributes for
	 *              adding the project object. Must not be null; commandAttributes
	 *              must include "project name". Optional key includes "member
	 *              list".
	 * @return The newly created ObjectBoundary representing the project.
	 * @throws InputException    If commandAttributes are null, missing required
	 *                           keys (e.g., "project name"), or if a project with
	 *                           the same name already exists.
	 * @throws NotFoundException If the user invoking the command does not exist or
	 *                           cannot be validated.
	 * @throws RuntimeException  If there are runtime issues during object creation
	 *                           or user role updates.
	 * 
	 */
	@Override
	public ObjectBoundary addMiniAppObject(MiniAppCommandBoundary input) {
		Object[] userAndNewProject = initialValidationAndProcessing(input, eAction.ADD_PROJECT, type, type);
		UserBoundary user = (UserBoundary) userAndNewProject[0];
		ObjectBoundary newOb = (ObjectBoundary) userAndNewProject[1];
		String projectName = input.getCommandAttributes().get("project name").toString();

		if (projectName.compareTo("") == 0)
			throw new InputException("project name cannot be an empty string");

		if (doesProjectExist(projectName))
			throw new InputException("There's already a project with the name provided -----> " + projectName);

		newOb.getObjectDetails().put("team leader", user.getUserId().getEmail());

		if (input.getCommandAttributes().get("member list") != null) {
			List<Object> memberList = (List) input.getCommandAttributes().get("member list");
			addMembersToObject(newOb, memberList, projectName, false);
			memberList.add(newOb.getObjectDetails().get("team leader"));
		} else {
			List<Object> newMemList = new ArrayList<>();
			newMemList.add(newOb.getObjectDetails().get("team leader"));
			newOb.getObjectDetails().put("member list", newMemList.toArray());
		}

		user.setRole(UserRoleEnum.SUPERAPP_USER);
		userService.updateUser(input.getInvokedBy().getUserId().getSuperapp(),
				input.getInvokedBy().getUserId().getEmail(), user);

		newOb = this.objectService.createAnObject(newOb);

		user.setRole(UserRoleEnum.MINIAPP_USER);
		userService.updateUser(input.getInvokedBy().getUserId().getSuperapp(),
				input.getInvokedBy().getUserId().getEmail(), user);
		return newOb;
	}

	/**
	 * Retrieves a list of active project objects associated with the logged-in
	 * user.
	 * 
	 * @param input The MiniAppCommandBoundary containing the invoking user's
	 *              information.
	 * @param size  The number of objects to retrieve per page (not used in the
	 *              current implementation).
	 * @param page  The page number of results to retrieve (not used in the current
	 *              implementation).
	 * @return A list of ObjectBoundary representing active objects of the specified
	 *         type that the logged-in user is associated with.
	 * @throws NotFoundException If the user invoking the command does not exist or
	 *                           cannot be validated.
	 */
	@Override
	public List<ObjectBoundary> getAllMiniAppObjects(MiniAppCommandBoundary input, int size, int page) {

		initialValidationAndProcessing(input, eAction.GET_ALL_PROJECT, type, type);
//		validateMiniAppUser(input.getInvokedBy().getUserId().getEmail(),
//				input.getInvokedBy().getUserId().getSuperapp());

		List<ObjectBoundary> ob = null;
		String member = "%" + input.getInvokedBy().getUserId().getEmail().trim() + "%";
		ob = objectService.findAllByTypeAndActiveAndByMemberListContaining(type, true, member);

		return ob;

	}

	/**
	 * Updates attributes of a specific project object based on the provided command
	 * attributes. Allows changes to "active" status, "team leader", and "member
	 * list" under specific conditions. Updates related Kanban tasks and Calendar
	 * events if the "member list" is modified.
	 * 
	 * @param input The MiniAppCommandBoundary containing command and attributes for
	 *              updating the object. Must not be null; commandAttributes must
	 *              include "active", "team leader", or "member list".
	 * @return The updated ObjectBoundary representing the modified mini app object.
	 * @throws InputException        If commandAttributes are null or missing
	 *                               required keys
	 * @throws NoPremissionException If anyone but the team leader is trying to
	 *                               update the "team leader" or "member list".
	 * @throws NoPremissionException If the user does not have permission to perform
	 *                               the requested operation (e.g., updating objects
	 *                               of another mini app or inactive objects ).
	 * @throws NotFoundException     If the object to update does not exist.
	 * 
	 */
	@Override
	public ObjectBoundary updateMiniAppObject(MiniAppCommandBoundary input) {
		Object[] userAndTargetProject = initialValidationAndProcessing(input, eAction.UPDATE, type, type);
		UserBoundary user = (UserBoundary) userAndTargetProject[0];
		ObjectBoundary targetObject = (ObjectBoundary) userAndTargetProject[1];

		if (targetObject.getObjectDetails().get("team leader").toString().compareTo(user.getUserId().getEmail()) != 0)
			throw new NoPremissionException("Only the team leader can update the project");

		if (input.getCommandAttributes().get("active") != null) {
			if ((boolean) input.getCommandAttributes().get("active"))
				throw new InputException(
						"The target object is already active: " + targetObject.getObjectId().toString());
			else {
				targetObject.setActive((boolean) input.getCommandAttributes().get("active"));
				deactivateAllRelatedObject(targetObject.getObjectDetails().get("project name").toString());

			}

		} else {

			if (input.getCommandAttributes().get("team leader") != null)
				targetObject.getObjectDetails().put("team leader", input.getCommandAttributes().get("team leader"));

			if (input.getCommandAttributes().get("member list") != null) {

				List<Object> newMemberList = (List) input.getCommandAttributes().get("member list");
				if (newMemberList.contains(targetObject.getObjectDetails().get("team leader")) == false)
					newMemberList.add(targetObject.getObjectDetails().get("team leader"));

				String[] array = (String[]) targetObject.getObjectDetails().get("member list");
				List<String> oldMemberList = Arrays.asList(array);

				addMembersToObject(targetObject, newMemberList,
						targetObject.getObjectDetails().get("project name").toString(), true);

				updateMembersForRelatedObjects(newMemberList, oldMemberList,
						targetObject.getObjectDetails().get("project name").toString());
			}
		}

		user.setRole(UserRoleEnum.SUPERAPP_USER);
		userService.updateUser(user.getUserId().getSuperapp(), user.getUserId().getEmail(), user);

		targetObject = objectService.updateAnObject(targetObject.getObjectId().getSuperapp(),
				targetObject.getObjectId().getId(), input.getInvokedBy().getUserId().getSuperapp(),
				input.getInvokedBy().getUserId().getEmail(), targetObject);

		user.setRole(UserRoleEnum.MINIAPP_USER);
		userService.updateUser(user.getUserId().getSuperapp(), user.getUserId().getEmail(), user);

		return targetObject;
	}

	/**
	 * Performs initial validation and processing for a given MiniApp command based
	 * on the specified action. This method validates the input command attributes,
	 * retrieves or creates user and object boundaries as needed for the action, and
	 * handles different types of actions such as ADD, UPDATE, and GET_ALL.
	 *
	 * @param input  The MiniApp command boundary containing command attributes and
	 *               invoked by user information.
	 * @param action The action to be performed, specified by the eAction enum.
	 * @param alias  The alias for the new object, used when creating a new object.
	 * @param type   The type of the object, used for validation and creation of new
	 *               objects.
	 * @return An array of objects where the first element is a UserBoundary and the
	 *         second element is an ObjectBoundary.
	 * @throws InputException        If the command attributes are null, the project
	 *                               name is not provided for certain actions, or if
	 *                               the target object is not provided for update
	 *                               action.
	 * @throws NoPremissionException If an attempt is made to update an object of
	 *                               another mini app.
	 * @author Code-Noam Laub
	 * @author Documentation-Chat GPT
	 */
	public Object[] initialValidationAndProcessing(MiniAppCommandBoundary input, eAction action, String alias,
			String type) {
		
		if (input.getCommandAttributes() == null)
			throw new InputException("No Command attributes provided");

		Object[] userAndObjectBoundary = new Object[2];
		UserBoundary user;

		if (action != eAction.UPDATE) {
			if (action != eAction.GET_ALL_PROJECT)
				if (input.getCommandAttributes().get("project name") == null)
					throw new InputException("No project name provided in command attributes");

			if (action != eAction.ADD_PROJECT && action != eAction.GET_ALL_PROJECT)
				user = authenticateProjectMemberByEmail(input.getInvokedBy().getUserId().getSuperapp(),
						input.getCommandAttributes().get("project name").toString(),
						input.getInvokedBy().getUserId().getEmail());
			else
				user = validateMiniAppUser(input.getInvokedBy().getUserId().getEmail(),
						input.getInvokedBy().getUserId().getSuperapp());

			userAndObjectBoundary[0] = user;
		}

		if (eAction.GET_ALL == action || eAction.GET_ALL_PROJECT == action)
			return userAndObjectBoundary;

		if (action == eAction.ADD || action == eAction.ADD_PROJECT) {
			ObjectBoundary newOb = new ObjectBoundary();
			newOb.setObjectDetails(new HashMap<>());
			newOb.getObjectDetails().put("project name", input.getCommandAttributes().get("project name"));
			newOb.setAlias(alias);
			newOb.setCreatedBy(new CreatedBy(input.getInvokedBy().getUserId()));
			newOb.setLocation(new Location(0, 0));
			newOb.setType(type);
			newOb.setCreationTimestamp(new Date());
			newOb.setActive(true);
			userAndObjectBoundary[1] = newOb;
			return userAndObjectBoundary;
		}
		if (action == eAction.UPDATE) {
			if (input.getTargetObject() == null || input.getTargetObject().getObjectId() == null
					|| input.getTargetObject().getObjectId().getId().compareTo("") == 0)
				throw new InputException("No object provided in Target Object");

			ObjectBoundary targetObject = objectService.retrieveObjectForUpdateForMiniApps(
					input.getTargetObject().getObjectId().getSuperapp(), input.getTargetObject().getObjectId().getId(),
					input.getInvokedBy().getUserId().getSuperapp(), input.getInvokedBy().getUserId().getEmail());
			if (!targetObject.getActive())
				throw new InputException("cannot update a inactive object");
			if (targetObject.getType().compareTo(type) != 0)
				throw new NoPremissionException("Cannot update objects of another mini app ");

			userAndObjectBoundary[0] = authenticateProjectMemberByEmail(input.getInvokedBy().getUserId().getSuperapp(),
					targetObject.getObjectDetails().get("project name").toString(),
					input.getInvokedBy().getUserId().getEmail());
			userAndObjectBoundary[1] = targetObject;

			if (input.getCommandAttributes().get("active") != null
					&& (boolean) input.getCommandAttributes().get("active") == false)
				commandService.invokeCommand(input, input.getCommandId().getMiniapp());

			return userAndObjectBoundary;
		}
		return userAndObjectBoundary;
	}

	public boolean doesProjectExist(String projectName) {
		Optional<ObjectBoundary> project = objectService.findByTypeAndProjectNameAndActive(type, projectName, true);

		if (project.isEmpty())
			return false;
		return true;

	}

	public UserBoundary validateMiniAppUser(String email, String superAppName) {
		UserBoundary user = this.userService.getSpecificUser(superAppName, email).get();

		if (user.getRole().compareTo(UserRoleEnum.MINIAPP_USER) != 0)
			throw new NoPremissionException(
					"This email doesn't belongs to a Miniapp user ---> " + user.getUserId().getEmail());
		return user;
	}

	public UserBoundary authenticateProjectMemberByEmail(String superAppName, String projectName, String email) {
		UserBoundary user = validateMiniAppUser(email, superAppName);

		Optional<ObjectBoundary> newOb = objectService.findByTypeAndProjectNameAndActive(type, projectName, true);
		if (newOb.isEmpty())
			throw new NotFoundException("No project found with the name provided");
		List<String> memberList = Arrays.asList((String[]) newOb.get().getObjectDetails().get("member list"));
		if (memberList.contains(email) == false)
			throw new NoPremissionException("User provided isn't a member of the project.Project Name: " + projectName
					+ " User Email: " + email);
		return user;
	}

	private void addMembersToObject(ObjectBoundary newOb, List<Object> newMemberList, String projectName,
			boolean forUpdate) {
		List<Object> currentMemberList = new ArrayList<>();

		for (Object member : newMemberList) {
			if (currentMemberList.contains(member))
				throw new InputException("Member's email: " + (member.toString())
						+ " already exist, please provide an email of another mini app user");
			else {
				validateMiniAppUser(member.toString(), newOb.getCreatedBy().getUserId().getSuperapp());
				currentMemberList.add(member);
			}

		}
		if (forUpdate == false && newOb.getObjectDetails().get("team leader") != null
				&& currentMemberList.contains(newOb.getObjectDetails().get("team leader")) == false)
			currentMemberList.add(newOb.getObjectDetails().get("team leader"));
		newOb.getObjectDetails().put("member list", currentMemberList.toArray());

	}

	private void updateMembersForRelatedObjects(List<Object> memberList, List<String> oldMemberList,

			String projectName) {
		ArrayList<String> memberArrayList = new ArrayList<>();
		ArrayList<String> oldMemberArrayList = new ArrayList<>();

		for (Object member : memberList)
			memberArrayList.add(member.toString());

		for (Object member : oldMemberList)
			oldMemberArrayList.add(member.toString());

		Iterator<String> iterator = oldMemberArrayList.iterator();
		while (iterator.hasNext()) {
			String member = iterator.next();
			if (memberArrayList.contains(member)) {
				iterator.remove(); // Use iterator's remove method to avoid ConcurrentModificationException
			}
		}

		objectService.updateMembers(oldMemberArrayList, projectName);

	}

	private void deactivateAllRelatedObject(String projectName) {
		objectService.deactivateAllObjectRelatedToProjectName(projectName);
	}

}
