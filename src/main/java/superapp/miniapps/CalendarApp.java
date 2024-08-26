
package superapp.miniapps;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import exception.InputException;
import exception.UnauthorizedException;
import superapp.command.MiniAppCommandBoundary;
import superapp.miniapps.ProjectManagementApp.eAction;
import superapp.superappobjects.MiniAppSupportObjectService;
import superapp.superappobjects.ObjectBoundary;
import superapp.users.UserBoundary;
import superapp.users.UserRoleEnum;
import superapp.users.UserService;

@Component("Calendar App")
public class CalendarApp implements MiniAppAbstraction {

	private MiniAppSupportObjectService objectService;
	private UserService userService;
	private ProjectManagementApp managementService;
	static String type = "calendar";
	static String alias = "calendar event";

	public CalendarApp(MiniAppSupportObjectService objectService, ProjectManagementApp managementService,
			UserService userService) {
		this.objectService = objectService;
		this.managementService = managementService;
		this.userService = userService;
	}

	/**
	 * Adds a new calendar event object based on the provided command attributes.
	 * Validates and processes attributes such as project name, title, event date,
	 * start/end times, member list, and resolves conflicts before creation.
	 * 
	 * @param input The MiniAppCommandBoundary containing command and attributes for
	 *              adding the object. Must not be null; commandAttributes must
	 *              include "project name", "event date", "start time", and "end
	 *              time". Optional keys include "title" and "member list".
	 * @return The newly created ObjectBoundary representing the calendar event.
	 * @throws InputException        If commandAttributes are null or missing
	 *                               required keys (e.g., "project name", "event
	 *                               date", "start time", "end time"), or if there
	 *                               are conflicts with existing events on the
	 *                               specified date and time.
	 * @throws NotFoundException     If no project is found with the specified name.
	 * @throws NoPremissionException If the user is not a member of the specified
	 *                               project or if any required attribute is missing
	 *                               or invalid.
	 * @throws RuntimeException      If there are runtime issues during the creation
	 *                               process, such as database access or data
	 *                               conversion errors.
	 */
	@Override
	@Transactional(readOnly = false)
	public ObjectBoundary addMiniAppObject(MiniAppCommandBoundary input) {
		Object[] userAndNewOb = managementService.initialValidationAndProcessing(input, eAction.ADD, alias, type);
		UserBoundary user = (UserBoundary) userAndNewOb[0];
		ObjectBoundary newOb = (ObjectBoundary) userAndNewOb[1];
		String projectName = input.getCommandAttributes().get("project name").toString();

		if (input.getCommandAttributes().get("event date") != null) {
			newOb.getObjectDetails().put("event date", input.getCommandAttributes().get("event date"));
		} else
			throw new InputException("No event date provided in command attributes");

		double[] startAndEndTimes = validateStartAndEndTimes(input, false);
		Date finalDate = parseDateFromString(newOb.getObjectDetails().get("event date").toString());

		List<ObjectBoundary> conflicts = objectService.checkEventConflicts(finalDate, startAndEndTimes[0],
				startAndEndTimes[1]);
		if (!conflicts.isEmpty())
			throw new InputException("The event time and day is conflicting with other events");

		if (input.getCommandAttributes().get("member list") != null) {
			List<Object> memberList = (List) input.getCommandAttributes().get("member list");
			addMembersToObject(newOb, memberList, projectName);
		}

		newOb.getObjectDetails().put("start time", startAndEndTimes[0]);
		newOb.getObjectDetails().put("end time", startAndEndTimes[1]);

		if (input.getCommandAttributes().get("title") != null)
			newOb.getObjectDetails().put("title", input.getCommandAttributes().get("title"));
		else
			newOb.getObjectDetails().put("title", "");

		user.setRole(UserRoleEnum.SUPERAPP_USER);
		userService.updateUser(user.getUserId().getSuperapp(), user.getUserId().getEmail(), user);

		newOb = this.objectService.createAnObject(newOb);

		user.setRole(UserRoleEnum.MINIAPP_USER);
		userService.updateUser(user.getUserId().getSuperapp(), user.getUserId().getEmail(), user);

		return newOb;
	}

	/**
	 * Retrieves a list of calendar event objects within a specified date range for
	 * a given project.
	 * 
	 * @param input The MiniAppCommandBoundary containing command and attributes for
	 *              retrieving objects. Must not be null; commandAttributes must
	 *              include "project name", "start date", and "end date".
	 * @param size  The number of objects to retrieve per page (not used in this
	 *              method).
	 * @param page  The page number of results to retrieve (not used in this
	 *              method).
	 * @return A list of ObjectBoundary representing calendar events within the
	 *         specified date range for the project.
	 * @throws InputException        If commandAttributes are null or missing
	 *                               required keys (e.g., "project name", "start
	 *                               date", "end date"), or if the user is not a
	 *                               member of the specified project.
	 * @throws NotFoundException     If no project is found with the specified name.
	 * @throws NoPermissionException If the user does not have permission to perform
	 *                               the operation.
	 * @throws RuntimeException      If there are runtime issues during the
	 *                               retrieval process, such as database access or
	 *                               data conversion errors.
	 */
	@Override
	@Transactional(readOnly = true)
	public List<ObjectBoundary> getAllMiniAppObjects(MiniAppCommandBoundary input, int size, int page) {
		managementService.initialValidationAndProcessing(input, eAction.GET_ALL, alias, type);
		if (input.getCommandAttributes().get("start date") == null)
			throw new InputException("No start date provided in command attributes");
		if (input.getCommandAttributes().get("end date") == null)
			throw new InputException("No end date provided in command attributes");

		String projectName = input.getCommandAttributes().get("project name").toString();
		Date startDate = parseDateFromString(input.getCommandAttributes().get("start date").toString());
		Date endDate = parseDateFromString(input.getCommandAttributes().get("end date").toString());
		if (endDate.before(startDate))
			throw new InputException("end date must come after start date or be identical");
		return objectService.findAllByEventDateBetweenAndActiveAndProjectName(startDate, endDate, projectName);

	}

	/**
	 * Updates an existing calendar event object based on the provided command
	 * attributes. Validates and processes attributes such as event date, start/end
	 * times, title, and member list. Checks for conflicts and ensures user
	 * permissions before updating.
	 * 
	 * @param input The MiniAppCommandBoundary containing command and attributes for
	 *              updating the object. Must not be null; commandAttributes must
	 *              not include "project name". Required keys include "event date"
	 *              and/or "start time"/"end time" if updating. Optional keys
	 *              include "title" and "member list".
	 * @return The updated ObjectBoundary representing the modified calendar event.
	 * @throws InputException        If commandAttributes are null, contain "project
	 *                               name", or fail validation, such as conflicting
	 *                               event times or missing required attributes.
	 * @throws NotFoundException     If no object is found with the provided ID.
	 * @throws UnauthorizedException If the user does not have permission to update
	 *                               the object.
	 * @throws RuntimeException      If there are runtime issues during the update
	 *                               process, such as database access or data
	 *                               conversion errors.
	 */
	@Transactional(readOnly = false)
	public ObjectBoundary updateMiniAppObject(MiniAppCommandBoundary input) {
		Object[] userAndTargetEvent = managementService.initialValidationAndProcessing(input, eAction.UPDATE, alias, type);
		UserBoundary user = (UserBoundary) userAndTargetEvent[0];
		ObjectBoundary targetObject = (ObjectBoundary) userAndTargetEvent[1];
		boolean dateOrTimeChanged = false;

		if (input.getCommandAttributes().get("active") != null) {
			if ((boolean) input.getCommandAttributes().get("active"))
				throw new InputException(
						"The target object is already active: " + targetObject.getObjectId().toString());
			else
				targetObject.setActive((boolean) input.getCommandAttributes().get("active"));
		} else {
			if (input.getCommandAttributes().get("event date") != null) {
				targetObject.getObjectDetails().put("event date", input.getCommandAttributes().get("event date"));
				dateOrTimeChanged = true;
			}

			double[] newStartAndEndTimes = validateStartAndEndTimes(input, true);

			if (newStartAndEndTimes[0] != -1.0) {
				targetObject.getObjectDetails().put("start time", newStartAndEndTimes[0]);
				dateOrTimeChanged = true;
			}

			if (newStartAndEndTimes[1] != -1.0) {
				targetObject.getObjectDetails().put("end time", newStartAndEndTimes[1]);
				dateOrTimeChanged = true;
			}
			double startTime=(double)targetObject.getObjectDetails().get("start time");
			double endTime=(double)targetObject.getObjectDetails().get("end time");
			if(endTime<=startTime) 
				throw new InputException("end time must be greater than the start time, start time: "+startTime+" end time: "+endTime);
			

			if (dateOrTimeChanged) {
				Date newDate = parseDateFromString(targetObject.getObjectDetails().get("event date").toString());

				String ignoreThisEvent = targetObject.getObjectId().getSuperapp() + "@@"
						+ targetObject.getObjectId().getId();
				List<ObjectBoundary> conflicts = objectService.checkEventConflicts(newDate,
						(double) targetObject.getObjectDetails().get("start time"),
						(double) targetObject.getObjectDetails().get("end time"), ignoreThisEvent);
				if (!conflicts.isEmpty())
					throw new InputException("The event time and day is conflicting with other events");
			}

			if (input.getCommandAttributes().get("title") != null)
				targetObject.getObjectDetails().put("title", input.getCommandAttributes().get("title"));

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
		newOb.getObjectDetails().put("member list", currentMemberList.toArray());

	}

	public Date parseDateFromString(String dateString) throws RuntimeException {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		sdf.setLenient(false);
		try {
			Date date = sdf.parse(dateString);
			return date;
		} catch (ParseException e) {
			throw new InputException("The date provided isn't in the correct format: dd/MM/yyyy ---> " + dateString);
		}

	}

	public double[] validateStartAndEndTimes(MiniAppCommandBoundary input, boolean forUpdate) {
		
		double startTime = -1.0, endTime = -1.0;
		double[] startAndEnd = {startTime,endTime};
		if (input.getCommandAttributes().get("start time") != null) {
			if (input.getCommandAttributes().get("start time") instanceof Integer)
				startTime = Double.valueOf((int) input.getCommandAttributes().get("start time"));
			else
				startTime = (double) input.getCommandAttributes().get("start time");
			if (startTime % 0.5 != 0 || startTime > 23.5 || startTime < 0)
				throw new InputException(
						"start time provided needs to be a number that can be divided by 0.5, and is between 0 and 23.5...Input Received --> "
								+ startTime);
			else
				startAndEnd[0] = startTime;

		} else if (!forUpdate)
			throw new InputException("No start time provided in command attributes");

		if (input.getCommandAttributes().get("end time") != null) {
			if (input.getCommandAttributes().get("end time") instanceof Integer)
				endTime = Double.valueOf((int) input.getCommandAttributes().get("end time"));
			else
				endTime = (double) input.getCommandAttributes().get("end time");
			if (endTime % 0.5 != 0 || endTime > 23.5 || endTime < 0)
				throw new InputException(
						"end time provided needs to be a number that can be divided by 0.5, is between 0 and 23.5 ...Input Received --> "
								+ endTime);
			else
				startAndEnd[1] = endTime;

		} else if (!forUpdate)
			throw new InputException("No end time provided in command attributes");
		return startAndEnd;

	}
}
