package superapp.miniapps;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import superapp.superappobjects.CreatedBy;
import superapp.superappobjects.EnhancedObjectService;
import superapp.superappobjects.Location;
import superapp.superappobjects.ObjectBoundary;
import superapp.superappobjects.ObjectId;
import superapp.users.EnhancedUsersService;
import superapp.users.NewUserBoundary;
import superapp.users.UserBoundary;
import superapp.users.UserRoleEnum;

@Component
@Profile("miniAppInit")
public class MiniAppsInitializer implements CommandLineRunner {

	private EnhancedObjectService objectService;
	private EnhancedUsersService userService;

	public MiniAppsInitializer(EnhancedObjectService objectService, EnhancedUsersService userService) {
		this.objectService = objectService;
		this.userService = userService;
	}

	@Override
	public void run(String... args) throws Exception {

		NewUserBoundary newuser = new NewUserBoundary();
		newuser.setEmail("initcreatoruser" + "@" + "gmail.ai");
		newuser.setUsername("Init super user ");
		newuser.setRole(UserRoleEnum.SUPERAPP_USER);
		newuser.setAvatar("Init");
		UserBoundary superUser = this.userService.createUser(newuser);

	

		for (int i = 1; i < 3; i++) {
			String id = "project" + i, teamLeader = "miniAppUser1@s.afeka.ac.il",
					teamLeader2 = "miniAppUser6@s.afeka.ac.il";

			String[] memberList = { "miniAppUser1@s.afeka.ac.il", "miniAppUser2@s.afeka.ac.il",
					"miniAppUser3@s.afeka.ac.il", "miniAppUser4@s.afeka.ac.il" };
			String[] memberList2 = { "miniAppUser6@s.afeka.ac.il", "miniAppUser7@s.afeka.ac.il",
					"miniAppUser8@s.afeka.ac.il" ,"miniAppUser9@s.afeka.ac.il"};
			ObjectBoundary newProject = setGeneralField(superUser, "project", id);

			Map<String, Object> objectDetailes = new HashMap<>();
			objectDetailes.put("project name", id);

			boolean firstProject = true;
			if (i == 1) {

				objectDetailes.put("member list", memberList);
				objectDetailes.put("team leader", teamLeader);

			} else {

				objectDetailes.put("member list", memberList2);
				objectDetailes.put("team leader", teamLeader2);
				firstProject = false;
			}
			newProject.setObjectDetails(objectDetailes);
			this.objectService.createAnObject(newProject);

			for (int j = 1; j < 6; j++) {
				String day = "1" + j;
				String tId = "task" + j + "_" + id, eId = "event" + j + "_" + id, mId = "message" + j + "_" + id;
				ObjectBoundary newTask = setGeneralField(superUser, "kanban", tId),
						newEvent = setGeneralField(superUser, "calendar", eId),
						newMessage = setGeneralField(superUser, "chat", mId);
				Map<String, Object> taskDetailes = new HashMap<>(), eventDetailes = new HashMap<>(),
						messageDetailes = new HashMap<>();
				taskDetailes.put("project name", id);
				eventDetailes.put("project name", id);
				messageDetailes.put("project name", id);
				if (firstProject) {

					taskDetailes.put("member list", memberList);
					eventDetailes.put("member list", memberList);

					taskDetailes.put("status", "backlog");
					taskDetailes.put("description", "init task of project 1");
					taskDetailes.put("title", "init task " + j + " for project 1");

					eventDetailes.put("event date", day + "/07/2024");
					eventDetailes.put("start time", 8.0);
					eventDetailes.put("end time", 10.0);
					eventDetailes.put("title", "init event " + j + " for project 1");

					messageDetailes.put("sent by", memberList[(j-1)%4].replace("@s.afeka.ac.il", ""));
					messageDetailes.put("message", "init chat message " + j + " for project 1");

				} else {

					taskDetailes.put("member list", memberList2);
					eventDetailes.put("member list", memberList2);

					taskDetailes.put("status", "backlog");
					taskDetailes.put("description", "init task of project 2");
					taskDetailes.put("title", "init task " + j + " for project 2");

					eventDetailes.put("event date", day + "/08/2024");
					eventDetailes.put("start time", 12.5);
					eventDetailes.put("end time", 15.0);
					eventDetailes.put("title", "init event " + j + " for project 2");

				
					messageDetailes.put("sent by",  memberList2[(j-1)%4].replace("@s.afeka.ac.il", ""));
					messageDetailes.put("message", "init chat message " + j + " for project 2");

				}
				newTask.setObjectDetails(taskDetailes);
				newEvent.setObjectDetails(eventDetailes);
				newMessage.setObjectDetails(messageDetailes);

				this.objectService.createAnObject(newTask);
				this.objectService.createAnObject(newEvent);
				this.objectService.createAnObject(newMessage);
			}

		}
		superUser.setRole(UserRoleEnum.MINIAPP_USER);
		userService.updateUser(superUser.getUserId().getSuperapp(), superUser.getUserId().getEmail(), superUser);

	}

	private ObjectBoundary setGeneralField(UserBoundary superUser, String type, String objectID) {
		ObjectBoundary newOb = new ObjectBoundary();
		newOb.setAlias(objectID);
		newOb.setType(type);
		newOb.setActive(true);
		newOb.setLocation(new Location(0, 0));
		newOb.setCreatedBy(new CreatedBy(superUser.getUserId()));
		return newOb;
	}

}
