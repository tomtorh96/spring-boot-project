package superapp.miniapps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;
import exception.InputException;
import superapp.command.MiniAppCommandBoundary;
import superapp.miniapps.ProjectManagementApp.eAction;
import superapp.superappobjects.MiniAppSupportObjectService;
import superapp.superappobjects.ObjectBoundary;
import superapp.users.UserBoundary;
import superapp.users.UserRoleEnum;
import superapp.users.UserService;

@Component("Kanban App")
public class KanbanApp implements MiniAppAbstraction {

	private List<String> category = Arrays.asList("backlog", "to do", "in progress", "done", "archive");

	private MiniAppSupportObjectService objectService;
	private UserService userService;
	private ProjectManagementApp managementService;
	static String type = "kanban";
	static String alias = "kanban task";

	public KanbanApp(MiniAppSupportObjectService objectService, ProjectManagementApp managementService,
			UserService userService) {
		this.objectService = objectService;
		this.managementService = managementService;
		this.userService = userService;
	}

	public ObjectBoundary addMiniAppObject(MiniAppCommandBoundary input) {
		Object[] userAndNewOb = managementService.initialValidationAndProcessing(input, eAction.ADD, alias, type);
		UserBoundary user = (UserBoundary) userAndNewOb[0];
		ObjectBoundary newOb = (ObjectBoundary) userAndNewOb[1];
		String projectName = input.getCommandAttributes().get("project name").toString();

		if (input.getCommandAttributes().get("status") == null)
			throw new InputException("status not provided in command attributes");
		if (category.contains(input.getCommandAttributes().get("status").toString()) == false)
			throw new InputException(
					"status is case sensitive, these are the options: backlog,to do,in progress,done,archive\n");
		newOb.getObjectDetails().put("status", input.getCommandAttributes().get("status"));

		if (input.getCommandAttributes().get("member list") != null) {
			List<Object> memberList = (List) input.getCommandAttributes().get("member list");
			addMembersToObject(newOb, memberList, projectName);

		}

		if (input.getCommandAttributes().get("title") != null)
			newOb.getObjectDetails().put("title", input.getCommandAttributes().get("title"));
		else
			newOb.getObjectDetails().put("title", "");

		if (input.getCommandAttributes().get("description") != null)
			newOb.getObjectDetails().put("description", input.getCommandAttributes().get("description"));
		else
			newOb.getObjectDetails().put("description", "");

		user.setRole(UserRoleEnum.SUPERAPP_USER);
		userService.updateUser(user.getUserId().getSuperapp(), user.getUserId().getEmail(), user);

		newOb = this.objectService.createAnObject(newOb);

		user.setRole(UserRoleEnum.MINIAPP_USER);
		userService.updateUser(user.getUserId().getSuperapp(), user.getUserId().getEmail(), user);

		return newOb;
	}

	public List<ObjectBoundary> getAllMiniAppObjects(MiniAppCommandBoundary input, int size, int page) {
		managementService.initialValidationAndProcessing(input, eAction.GET_ALL, alias, type);

		if (input.getCommandAttributes().get("size") != null)
			size = (int) input.getCommandAttributes().get("size");
		if (input.getCommandAttributes().get("page") != null)
			page = (int) input.getCommandAttributes().get("page");

		return objectService.findAllByTypeAndProjectNameAndActive(type,
				input.getCommandAttributes().get("project name").toString(), true, size, page);

	}

	public ObjectBoundary updateMiniAppObject(MiniAppCommandBoundary input) {

		Object[] userAndTargetObject = managementService.initialValidationAndProcessing(input, eAction.UPDATE, alias,
				type);
		UserBoundary user = (UserBoundary) userAndTargetObject[0];
		ObjectBoundary targetObject = (ObjectBoundary) userAndTargetObject[1];

		if (input.getCommandAttributes().get("active") != null) {
			if ((boolean) input.getCommandAttributes().get("active"))
				throw new InputException(
						"The target object is already active: " + targetObject.getObjectId().toString());
			else
				targetObject.setActive((boolean) input.getCommandAttributes().get("active"));
		} else {
			if (input.getCommandAttributes().get("status") != null
					&& category.contains(input.getCommandAttributes().get("status").toString()) == false)
				throw new InputException(
						"status is case sensitive, these are the options: backlog,to do,in progress,done,archive\n");

			if (input.getCommandAttributes().get("description") != null)
				targetObject.getObjectDetails().put("description", input.getCommandAttributes().get("description"));
			if (input.getCommandAttributes().get("title") != null)
				targetObject.getObjectDetails().put("title", input.getCommandAttributes().get("title"));
			if (input.getCommandAttributes().get("status") != null)
				targetObject.getObjectDetails().put("status", input.getCommandAttributes().get("status"));

			if (input.getCommandAttributes().get("member list") != null) {

				List<Object> memberList = (List) input.getCommandAttributes().get("member list");

				addMembersToObject(targetObject, memberList,
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

	public void addMembersToObject(ObjectBoundary newOb, List<Object> newMemberList, String projectName) {

		List<Object> currentMemberList = new ArrayList<>();

		for (Object member : newMemberList) {
			if (currentMemberList.contains(member))
				throw new InputException("Member's email: " + (member.toString())
						+ " already exist, please provide an email of another mini app user");
			else if (managementService.authenticateProjectMemberByEmail(newOb.getCreatedBy().getUserId().getSuperapp(),
					projectName, member.toString()) != null)
				currentMemberList.add(member);
			else
				throw new InputException("Email provided in one of the members does not belong to any project member");

		}
		for (Object mem : currentMemberList)
			System.err.println(mem.toString());
		newOb.getObjectDetails().put("member list", currentMemberList.toArray());
		List<Object> test = new ArrayList<>(Arrays.asList(newOb.getObjectDetails().get("member list")));
		for (Object mem : test)
			System.err.println(mem.toString());

	}

}
