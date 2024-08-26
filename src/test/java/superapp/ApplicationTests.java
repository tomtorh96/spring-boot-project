package superapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;



import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import exception.InputException;
import superapp.command.InvokedByBoundary;
import superapp.command.MiniAppCommandBoundary;
import superapp.command.TargetObjectBoundary;
import superapp.superappobjects.CreatedBy;
import superapp.superappobjects.Location;
import superapp.superappobjects.ObjectBoundary;
import superapp.superappobjects.ObjectId;
import superapp.users.NewUserBoundary;
import superapp.users.UserBoundary;
import superapp.users.UserRoleEnum;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ApplicationTests {

	private RestClient restClient;
	private String adminEmail ="admin@gamil.com";
	private String adminSuperapp="2024b.Gal.Israeli";
	@Autowired
	private ObjectMapper mapper;
	@Value("${server.port:8085}")
	public void setPort(int port) {
		this.restClient = RestClient.create("http://localhost:" + port + "/superapp");
	}

	@BeforeEach
	public void setup() {
		myUsers();
	}
	public void myUsers()
	{
		NewUserBoundary newAdminBoundary = new NewUserBoundary();
		newAdminBoundary.setAvatar("GOD");
		newAdminBoundary.setRole(UserRoleEnum.ADMIN);
		newAdminBoundary.setEmail("admin@gamil.com");
		newAdminBoundary.setUsername("admin");
		this.restClient.post()
		.uri("/users")
		.body(newAdminBoundary)
		.retrieve()
		.body(UserBoundary.class);

		for(int i =0;i<10;i++)
		{
			NewUserBoundary newMiniAppUserBoundary = new NewUserBoundary();
			newMiniAppUserBoundary.setRole(UserRoleEnum.MINIAPP_USER);
			newMiniAppUserBoundary.setEmail("miniAppUserTest" + i +"@s.afeka.ac.il");
			newMiniAppUserBoundary.setUsername("miniAppUser" + i);
			newMiniAppUserBoundary.setAvatar(String.valueOf(i % 2 == 0));
			UserBoundary miniAppUserboundary = this.restClient.post()
					.uri("/users")
					.body(newMiniAppUserBoundary)
					.retrieve()
					.body(UserBoundary.class);
			System.err.println("* " + miniAppUserboundary);
		}

		for (int j = 0; j < 10; j++) {
			NewUserBoundary newSuperAppUserboundary = new NewUserBoundary();
			newSuperAppUserboundary.setRole(UserRoleEnum.SUPERAPP_USER);
			newSuperAppUserboundary.setEmail("superAppUserTest" + j +"@s.afeka.ac.il");
			System.err.println(newSuperAppUserboundary.getEmail());
			newSuperAppUserboundary.setUsername("superAppUser" + String.valueOf(j));
			newSuperAppUserboundary.setAvatar(String.valueOf(j % 2 == 0));
			UserBoundary superAppUserboundary = this.restClient.post()
					.uri("/users")
					.body(newSuperAppUserboundary)
					.retrieve()
					.body(UserBoundary.class);
			System.err.println("** " + superAppUserboundary);
		}
	}
	@AfterEach
	public void tearDown() {
		this.restClient.delete()
				.uri("/admin/commands?userEmail={email}&userSuperapp={userSuperapp}"
						, adminEmail, adminSuperapp)
				.retrieve();
		this.restClient.delete()
				.uri("/admin/objects?userEmail={email}&userSuperapp={userSuperapp}"
						, adminEmail, adminSuperapp)
				.retrieve();
		this.restClient.delete()
				.uri("/admin/users?userEmail={email}&userSuperapp={userSuperapp}"
						, adminEmail, adminSuperapp)
				.retrieve();
		

	}
	@Test
	public void testPostObjectRequest() throws Exception {
		// GIVEN the server is up
		// AND the database contains 9 object
		List<ObjectBoundary> created = new ArrayList<>();
		for (int i = 1; i < 10; i++) {
			ObjectBoundary boundary = new ObjectBoundary();
			boundary.setObjectId(new ObjectId("SuperApp #" + i, "1"));
			boundary.setAlias("object #" + i);
			boundary.setType("app");
			boundary.setLocation(new Location(i * 2.0, i * 2.5));
			boundary.setActive(i % 2 == 0);
			NewUserBoundary newuser = new NewUserBoundary();
			newuser.setEmail(i + "@gmail.com");
			newuser.setUsername("tom#" + i);
			newuser.setRole(UserRoleEnum.SUPERAPP_USER);
			newuser.setAvatar("avatar#" + i);
			UserBoundary user = new UserBoundary();
			user = this.restClient.post()
					.uri("/users")
					.body(newuser)
					.retrieve()
					.body(UserBoundary.class);
			boundary.setCreatedBy(new CreatedBy(user.getUserId()));
			boundary.setObjectDetails(Collections.singletonMap("app", "#" + i));
			boundary = this.restClient.post()
					.uri("/objects")
					.body(boundary)
					.retrieve()
					.body(ObjectBoundary.class);
			created.add(boundary);
		}
		// WHEN i invoke the GET
		ObjectBoundary got[] = this.restClient.get()
				.uri("/objects?userSuperapp={userSuperapp}&userEmail={userEmail}&size={size}&page={page}",
						"2024b.Gal.Israeli", "1@gmail.com", 100, 0)
				.retrieve().body(ObjectBoundary[].class);
		// THEN the server will responds with the same objects sent
		assertThat(got).hasSize(9).usingRecursiveFieldByFieldElementComparator()
				.containsExactlyInAnyOrderElementsOf(created);
	}
	@Test
	public void testPostCommandForAdminRequest(){
		// GIVEN the server is up
		// AND getting the number of ObjectBoundary in server
		ObjectBoundary got[] = this.restClient.get()
				.uri("/objects?userSuperapp={userSuperapp}&userEmail={userEmail}&size={size}&page={page}",
						"2024b.Gal.Israeli", "miniAppUserTest0@s.afeka.ac.il", 100, 0)
				.retrieve().body(ObjectBoundary[].class);
		int len = got.length;
		MiniAppCommandBoundary command = new MiniAppCommandBoundary();
		command.setCommand("addminiappobject");
		command.setInvokedBy(new InvokedByBoundary("2024b.Gal.Israeli", "miniAppUserTest1@s.afeka.ac.il"));
		Map<String,Object> arr = new HashMap<>();
		command.setCommandAttributes(arr);
		command.getCommandAttributes().put("project name", "new project12");
		String users[] = {"miniAppUserTest2@s.afeka.ac.il","miniAppUserTest3@s.afeka.ac.il","miniAppUserTest4@s.afeka.ac.il","miniAppUserTest5@s.afeka.ac.il"};
		command.getCommandAttributes().put("member list",users);
		// WHEN i invoke the POST for command and then GET for ObjectBoundary
		this.restClient
				.post()
				.uri("/miniapp/Admin App")
				.body(command)
				.retrieve();
		 got = this.restClient.get()
				.uri("/objects?userSuperapp={userSuperapp}&userEmail={userEmail}&size={size}&page={page}",
						"2024b.Gal.Israeli", "miniAppUserTest0@s.afeka.ac.il", 100, 0)
				.retrieve().body(ObjectBoundary[].class);
		 //THEN the number of ObjectBoundary will increase by 1
		assertThat(got).hasSize(len+1);
		
	}
	@Test
	public void testPostCommandAndPutProject()
	{
		// GIVEN the server is up
		// AND adding a new project to the server
		MiniAppCommandBoundary command = new MiniAppCommandBoundary();
		command.setCommand("addminiappobject");
		command.setInvokedBy(new InvokedByBoundary("2024b.Gal.Israeli", "miniAppUserTest1@s.afeka.ac.il"));
		Map<String,Object> arr = new HashMap<>();
		command.setCommandAttributes(arr);
		command.getCommandAttributes().put("project name", "new project12");
		String users[] = {"miniAppUserTest2@s.afeka.ac.il","miniAppUserTest3@s.afeka.ac.il"};
		command.getCommandAttributes().put("member list",users);
		Object[] obj = this.restClient
		.post()
		.uri("/miniapp/Admin App")
		.body(command)
		.retrieve()
		.body(Object[].class);
		LinkedHashMap<String, Object> hash = (LinkedHashMap<String, Object>) obj[1];
		ObjectBoundary boundary = mapper.convertValue(hash, ObjectBoundary.class);
		ArrayList<Object> oldUsers = (ArrayList<Object>) boundary.getObjectDetails().get("member list");
		MiniAppCommandBoundary update = new MiniAppCommandBoundary();
		update.setCommand("updateminiappobject");
		update.setInvokedBy(new InvokedByBoundary("2024b.Gal.Israeli", "miniAppUserTest1@s.afeka.ac.il"));
		Map<String,Object> arr2 = new HashMap<>();
		update.setCommandAttributes(arr2);
		ArrayList<Object> users2 = (ArrayList<Object>) oldUsers.clone();
		users2.add("miniAppUserTest4@s.afeka.ac.il");
		update.getCommandAttributes().put("member list",users2);
		update.setTargetObject(new TargetObjectBoundary(boundary.getObjectId().getSuperapp(),boundary.getObjectId().getId()));
		//WHEN invoke the POST command to update a new member in the mini app
		obj = this.restClient
				.post()
				.uri("/miniapp/Admin App")
				.body(update)
				.retrieve()
				.body(Object[].class);
		 hash = (LinkedHashMap<String, Object>) obj[1];
		 ObjectBoundary NewBoundary = mapper.convertValue(hash, ObjectBoundary.class);
		 ArrayList<Object> newUsers = (ArrayList<Object>) NewBoundary.getObjectDetails().get("member list");
		 //THEN the number of members has increased by 1
		 assertThat(newUsers).hasSize(oldUsers.size() + 1);
		 
	}
	@Test
	public void testGetallProject()
	{
		// GIVEN the server is up
		// AND adding 2 new projects
		MiniAppCommandBoundary command = new MiniAppCommandBoundary();
		command.setCommand("addminiappobject");
		command.setInvokedBy(new InvokedByBoundary("2024b.Gal.Israeli", "miniAppUserTest1@s.afeka.ac.il"));
		Map<String,Object> arr = new HashMap<>();
		command.setCommandAttributes(arr);
		command.getCommandAttributes().put("project name", "new project12");
		String users[] = {"miniAppUserTest2@s.afeka.ac.il","miniAppUserTest3@s.afeka.ac.il"};
		command.getCommandAttributes().put("member list",users);
		Object[] obj = this.restClient
		.post()
		.uri("/miniapp/Admin App")
		.body(command)
		.retrieve()
		.body(Object[].class);
		LinkedHashMap<String, Object> hash = (LinkedHashMap<String, Object>) obj[1];
		ObjectBoundary project1 = mapper.convertValue(hash, ObjectBoundary.class);
		command.getCommandAttributes().replace("project name", "new project 55");
		obj=this.restClient
		.post()
		.uri("/miniapp/Admin App")
		.body(command)
		.retrieve()
		.body(Object[].class);
		hash = (LinkedHashMap<String, Object>) obj[1];
		ObjectBoundary project2 = mapper.convertValue(hash, ObjectBoundary.class);
		command.setCommand("getallminiappobjects");
		//WHEN invoke the POST command to get all miniapps objects 
		obj = this.restClient
				.post()
				.uri("/miniapp/Admin App")
				.body(command)
				.retrieve()
				.body(Object[].class);
		ArrayList<LinkedHashMap<String, Object>> newHash = (ArrayList<LinkedHashMap<String, Object>>) obj[1];
		ObjectBoundary[] boundary = mapper.convertValue(newHash, ObjectBoundary[].class);
		//THEN our projects are in the DB
		assertThat(boundary).usingRecursiveFieldByFieldElementComparator().contains(project1,project2);
	}

	@Test
	public void testGotMassageFromCommand()
	{
		//WHEN server is up
		//AND add a project to DB
		MiniAppCommandBoundary admin = new MiniAppCommandBoundary();
		admin.setCommand("addminiappobject");
		admin.setInvokedBy(new InvokedByBoundary("2024b.Gal.Israeli", "miniAppUserTest3@s.afeka.ac.il"));
		Map<String,Object> arr = new HashMap<>();
		admin.setCommandAttributes(arr);
		admin.getCommandAttributes().put("project name", "new project12");
		String users[] = {"miniAppUserTest2@s.afeka.ac.il","miniAppUserTest3@s.afeka.ac.il","miniAppUserTest4@s.afeka.ac.il","miniAppUserTest5@s.afeka.ac.il"};
		admin.getCommandAttributes().put("member list",users);
		this.restClient
				.post()
				.uri("/miniapp/Admin App")
				.body(admin)
				.retrieve();
		//AND sended to Chat App message
		MiniAppCommandBoundary command = new MiniAppCommandBoundary();
		command.setCommand("addminiappobject");
		command.setInvokedBy(new InvokedByBoundary("2024b.Gal.Israeli", "miniAppUserTest3@s.afeka.ac.il"));
		Map<String,Object> arr2 = new HashMap<>();
		command.setCommandAttributes(arr2);
		command.getCommandAttributes().put("project name", "new project12");
		String users2[] = {"miniAppUserTest2@s.afeka.ac.il","miniAppUserTest3@s.afeka.ac.il","miniAppUserTest4@s.afeka.ac.il","miniAppUserTest5@s.afeka.ac.il"};
		command.getCommandAttributes().put("member list",users2);
		command.getCommandAttributes().put("message","my message");
		Object[] obj= this.restClient
				.post()
				.uri("/miniapp/Chat App")
				.body(command)
				.retrieve()
				.body(Object[].class);
		LinkedHashMap<String, Object> hash = (LinkedHashMap<String, Object>) obj[1];
		ObjectBoundary boundary = mapper.convertValue(hash, ObjectBoundary.class);
		ObjectBoundary[] responds = this.restClient
				.get()
				.uri("/objects/search/byType/chat?userSuperapp={userSuperapp}&userEmail={userEmail}&size={size}&page={page}"
						,"2024b.Gal.Israeli","miniAppUserTest3@s.afeka.ac.il",10,0)
				.retrieve()
				.body(ObjectBoundary[].class);
		//THEN the meassge will go to DB
		assertThat(boundary).usingRecursiveComparison().isIn(responds);
	}
	@Test
	public void testPostChatAndPutChat()
	{
		//WHEN server is up
		//AND add a project to DB
		MiniAppCommandBoundary admin = new MiniAppCommandBoundary();
		admin.setCommand("addminiappobject");
		admin.setInvokedBy(new InvokedByBoundary("2024b.Gal.Israeli", "miniAppUserTest3@s.afeka.ac.il"));
		Map<String,Object> arr = new HashMap<>();
		admin.setCommandAttributes(arr);
		admin.getCommandAttributes().put("project name", "new project999");
		String users[] = {"miniAppUserTest2@s.afeka.ac.il","miniAppUserTest3@s.afeka.ac.il","miniAppUserTest4@s.afeka.ac.il","miniAppUserTest5@s.afeka.ac.il"};
		admin.getCommandAttributes().put("member list",users);
		this.restClient
				.post()
				.uri("/miniapp/Admin App")
				.body(admin)
				.retrieve();
		MiniAppCommandBoundary command = new MiniAppCommandBoundary();
		command.setCommand("addminiappobject");
		command.setInvokedBy(new InvokedByBoundary("2024b.Gal.Israeli", "miniAppUserTest3@s.afeka.ac.il"));
		Map<String,Object> arr2 = new HashMap<>();
		command.setCommandAttributes(arr2);
		command.getCommandAttributes().put("project name", "new project999");
		String users2[] = {"miniAppUserTest2@s.afeka.ac.il","miniAppUserTest3@s.afeka.ac.il","miniAppUserTest4@s.afeka.ac.il","miniAppUserTest5@s.afeka.ac.il"};
		command.getCommandAttributes().put("member list",users2);
		command.getCommandAttributes().put("message","my message");
		Object[] obj= this.restClient
				.post()
				.uri("/miniapp/Chat App")
				.body(command)
				.retrieve()
				.body(Object[].class);
		LinkedHashMap<String, Object> hash = (LinkedHashMap<String, Object>) obj[1];
		ObjectBoundary chat1 = mapper.convertValue(hash, ObjectBoundary.class);
		command.getCommandAttributes().replace("message","my message2");
		command.setCommand("updateminiappobject");
		command.setTargetObject(new TargetObjectBoundary(chat1.getObjectId().getSuperapp(), chat1.getObjectId().getId()));
		//WHEN invoke the POST to update the miniapp 
		obj= this.restClient
					.post()
					.uri("/miniapp/Chat App")
					.body(command)
					.retrieve()
					.body(Object[].class);
		hash = (LinkedHashMap<String, Object>) obj[1];
		ObjectBoundary chat2 = mapper.convertValue(hash, ObjectBoundary.class);
		//THEN the ObjectBoundary will change
		assertThat(chat2).usingRecursiveAssertion().isNotEqualTo(chat1);
	}
	@Test
	public void testGetAllChat()
	{
		//WHEN server is up
		//AND add a project to DB
		MiniAppCommandBoundary command = new MiniAppCommandBoundary();
		command.setCommand("addminiappobject");
		command.setInvokedBy(new InvokedByBoundary("2024b.Gal.Israeli", "miniAppUserTest1@s.afeka.ac.il"));
		Map<String,Object> arr = new HashMap<>();
		command.setCommandAttributes(arr);
		command.getCommandAttributes().put("project name", "new project12");
		String users[] = {"miniAppUserTest2@s.afeka.ac.il","miniAppUserTest3@s.afeka.ac.il"};
		command.getCommandAttributes().put("member list",users);
		this.restClient
		.post()
		.uri("/miniapp/Admin App")
		.body(command)
		.retrieve();
		command.getCommandAttributes().put("message","my message1");
		Object[] obj=this.restClient
				.post()
				.uri("/miniapp/Chat App")
				.body(command)
				.retrieve()
				.body(Object[].class);
		LinkedHashMap<String, Object> hash = (LinkedHashMap<String, Object>) obj[1];
		ObjectBoundary chat1 = mapper.convertValue(hash, ObjectBoundary.class);
		command.setInvokedBy(new InvokedByBoundary("2024b.Gal.Israeli", "miniAppUserTest2@s.afeka.ac.il"));
		command.getCommandAttributes().replace("message", "my message2");
		//AND add 2 chat app to DB
		obj=this.restClient
		.post()
		.uri("/miniapp/Chat App")
		.body(command)
		.retrieve()
		.body(Object[].class);
		hash = (LinkedHashMap<String, Object>) obj[1];
		ObjectBoundary chat2 = mapper.convertValue(hash, ObjectBoundary.class);
		command.setCommand("getallminiappobjects");
		//WHEN invoke the post command to get all chat miniapps
		obj = this.restClient
				.post()
				.uri("/miniapp/Chat App")
				.body(command)
				.retrieve()
				.body(Object[].class);
		ArrayList<LinkedHashMap<String, Object>> newHash = (ArrayList<LinkedHashMap<String, Object>>) obj[1];
		ObjectBoundary[] boundary = mapper.convertValue(newHash, ObjectBoundary[].class);
		//THEN the chats will be in the DB
		assertThat(boundary).usingRecursiveFieldByFieldElementComparator().contains(chat1,chat2);
	}
	@Test
	public void testPostKanban()
	{
		//WHEN server is up
		//AND add a project to DB
		MiniAppCommandBoundary admin = new MiniAppCommandBoundary();
		admin.setCommand("addminiappobject");
		admin.setInvokedBy(new InvokedByBoundary("2024b.Gal.Israeli", "miniAppUserTest3@s.afeka.ac.il"));
		Map<String,Object> arr = new HashMap<>();
		admin.setCommandAttributes(arr);
		admin.getCommandAttributes().put("project name", "new project 420");
		String users[] = {"miniAppUserTest2@s.afeka.ac.il","miniAppUserTest3@s.afeka.ac.il","miniAppUserTest4@s.afeka.ac.il","miniAppUserTest5@s.afeka.ac.il"};
		admin.getCommandAttributes().put("member list",users);
		this.restClient
				.post()
				.uri("/miniapp/Admin App")
				.body(admin)
				.retrieve();
		admin.getCommandAttributes().put("status","backlog");
		admin.getCommandAttributes().put("description","new desc");
		admin.getCommandAttributes().put("title","new title");
		//AND add a new Kanban app
		Object[] obj = this.restClient
				.post()
				.uri("/miniapp/Kanban App")
				.body(admin)
				.retrieve()
				.body(Object[].class);
		LinkedHashMap<String, Object> hash = (LinkedHashMap<String, Object>) obj[1];
		ObjectBoundary boundary = mapper.convertValue(hash, ObjectBoundary.class);
		//WHEN invoke the GET command 
		ObjectBoundary got[] = this.restClient.get()
				.uri("/objects?userSuperapp={userSuperapp}&userEmail={userEmail}&size={size}&page={page}",
						"2024b.Gal.Israeli", "miniAppUserTest3@s.afeka.ac.il", 100, 0)
				.retrieve().body(ObjectBoundary[].class);
		//THEN the Kanban app will be added to the DB
		assertThat(got).usingRecursiveFieldByFieldElementComparator().contains(boundary);
		
	}
	@Test
	public void testGetAllKanban()
	{
		//WHEN server is up
		//AND add a project to DB
		MiniAppCommandBoundary admin = new MiniAppCommandBoundary();
		admin.setCommand("addminiappobject");
		admin.setInvokedBy(new InvokedByBoundary("2024b.Gal.Israeli", "miniAppUserTest3@s.afeka.ac.il"));
		Map<String,Object> arr = new HashMap<>();
		admin.setCommandAttributes(arr);
		admin.getCommandAttributes().put("project name", "new project 420");
		String users[] = {"miniAppUserTest2@s.afeka.ac.il","miniAppUserTest3@s.afeka.ac.il","miniAppUserTest4@s.afeka.ac.il","miniAppUserTest5@s.afeka.ac.il"};
		admin.getCommandAttributes().put("member list",users);
		this.restClient
				.post()
				.uri("/miniapp/Admin App")
				.body(admin)
				.retrieve();
		admin.getCommandAttributes().put("status","backlog");
		admin.getCommandAttributes().put("description","new desc");
		admin.getCommandAttributes().put("title","new title");
		Object[] obj = this.restClient
				.post()
				.uri("/miniapp/Kanban App")
				.body(admin)
				.retrieve()
				.body(Object[].class);
		
		LinkedHashMap<String, Object> hash = (LinkedHashMap<String, Object>) obj[1];
		ObjectBoundary kanban1 = mapper.convertValue(hash, ObjectBoundary.class);
		admin.setInvokedBy(new InvokedByBoundary("2024b.Gal.Israeli", "miniAppUserTest4@s.afeka.ac.il"));
		admin.getCommandAttributes().replace("status","done");
		admin.getCommandAttributes().replace("description","done done");
		admin.getCommandAttributes().replace("title","BIG TITLE");
		//AND add 2 kanban commands
		obj = this.restClient
				.post()
				.uri("/miniapp/Kanban App")
				.body(admin)
				.retrieve()
				.body(Object[].class);
		hash = (LinkedHashMap<String, Object>) obj[1];
		ObjectBoundary kanban2 = mapper.convertValue(hash, ObjectBoundary.class);
		admin.setCommand("getallminiappobjects");
		//WHEN invoke the POST command to get all kanban miniApps 
		obj = this.restClient
				.post()
				.uri("/miniapp/Kanban App")
				.body(admin)
				.retrieve()
				.body(Object[].class);
		ArrayList<LinkedHashMap<String, Object>> newHash = (ArrayList<LinkedHashMap<String, Object>>) obj[1];
		ObjectBoundary[] boundary = mapper.convertValue(newHash, ObjectBoundary[].class);
		//THEN the 2 kanban miniApps will be in the DB
		assertThat(boundary).usingRecursiveFieldByFieldElementComparator().contains(kanban1,kanban2);
		
	}
	@Test
	public void testPostKanbanAndPutKanban()
	{
		//WHEN server is up
		//AND add a project to DB
		MiniAppCommandBoundary admin = new MiniAppCommandBoundary();
		admin.setCommand("addminiappobject");
		admin.setInvokedBy(new InvokedByBoundary("2024b.Gal.Israeli", "miniAppUserTest3@s.afeka.ac.il"));
		Map<String,Object> arr = new HashMap<>();
		admin.setCommandAttributes(arr);
		admin.getCommandAttributes().put("project name", "new project12");
		String users[] = {"miniAppUserTest2@s.afeka.ac.il","miniAppUserTest3@s.afeka.ac.il","miniAppUserTest4@s.afeka.ac.il","miniAppUserTest5@s.afeka.ac.il"};
		admin.getCommandAttributes().put("member list",users);
		this.restClient
				.post()
				.uri("/miniapp/Admin App")
				.body(admin)
				.retrieve();
		MiniAppCommandBoundary command = new MiniAppCommandBoundary();
		command.setCommand("addminiappobject");
		command.setInvokedBy(new InvokedByBoundary("2024b.Gal.Israeli", "miniAppUserTest3@s.afeka.ac.il"));
		Map<String,Object> arr2 = new HashMap<>();
		command.setCommandAttributes(arr2);
		command.getCommandAttributes().put("project name", "new project12");
		String users2[] = {"miniAppUserTest2@s.afeka.ac.il","miniAppUserTest3@s.afeka.ac.il","miniAppUserTest4@s.afeka.ac.il","miniAppUserTest5@s.afeka.ac.il"};
		command.getCommandAttributes().put("member list",users2);
		command.getCommandAttributes().put("status","backlog");
		command.getCommandAttributes().put("description","new desc");
		command.getCommandAttributes().put("title","new title");
		//AND add a kanban miniapp
		Object[] obj = this.restClient
		.post()
		.uri("/miniapp/Kanban App")
		.body(command)
		.retrieve()
		.body(Object[].class);
		LinkedHashMap<String, Object> hash = (LinkedHashMap<String, Object>) obj[1];
		ObjectBoundary boundary = mapper.convertValue(hash, ObjectBoundary.class);
		MiniAppCommandBoundary update = new MiniAppCommandBoundary();
		update.setCommand("updateminiappobject");
		update.setInvokedBy(new InvokedByBoundary("2024b.Gal.Israeli", "miniAppUserTest3@s.afeka.ac.il"));
		update.setCommandAttributes(arr2);
		update.getCommandAttributes().put("project name", "new project12");
		update.getCommandAttributes().put("member list",users2);
		update.getCommandAttributes().put("status","to do");
		update.getCommandAttributes().put("description","new desc");
		update.getCommandAttributes().put("title","new title");
		update.setTargetObject(new TargetObjectBoundary(boundary.getObjectId().getSuperapp()
				,boundary.getObjectId().getId()));
		//WHEN invoke the POST to update the kanban miniapp
		obj = this.restClient
				.post()
				.uri("/miniapp/Kanban App")
				.body(command)
				.retrieve()
				.body(Object[].class);
		hash = (LinkedHashMap<String, Object>) obj[1];
		ObjectBoundary newBoundary = mapper.convertValue(hash, ObjectBoundary.class);
		ObjectBoundary[] responds = this.restClient
				.get()
				.uri("/objects/search/byType/kanban?userSuperapp={userSuperapp}&userEmail={userEmail}&size={size}&page={page}"
						,"2024b.Gal.Israeli","miniAppUserTest3@s.afeka.ac.il",10,0)
				.retrieve()
				.body(ObjectBoundary[].class);
		//THEN the updated kanban will be in the DB
		assertThat(newBoundary).usingRecursiveComparison().isIn(responds);
	}
	@Test
	public void testPostCalander()
	{
		//WHEN server is up
		//AND add a project to DB
		MiniAppCommandBoundary admin = new MiniAppCommandBoundary();
		admin.setCommand("addminiappobject");
		admin.setInvokedBy(new InvokedByBoundary("2024b.Gal.Israeli", "miniAppUserTest3@s.afeka.ac.il"));
		Map<String,Object> arr = new HashMap<>();
		admin.setCommandAttributes(arr);
		admin.getCommandAttributes().put("project name", "new project 420");
		String users[] = {"miniAppUserTest2@s.afeka.ac.il","miniAppUserTest3@s.afeka.ac.il","miniAppUserTest4@s.afeka.ac.il","miniAppUserTest5@s.afeka.ac.il"};
		admin.getCommandAttributes().put("member list",users);
		this.restClient
				.post()
				.uri("/miniapp/Admin App")
				.body(admin)
				.retrieve();
		admin.getCommandAttributes().put("event date","18/7/2024");
		admin.getCommandAttributes().put("start time",5);
		admin.getCommandAttributes().put("end time",8);
		//AND add a new calendar event  
		Object[] obj = this.restClient
		.post()
		.uri("/miniapp/Calendar App")
		.body(admin)
		.retrieve()
		.body(Object[].class);
		LinkedHashMap<String, Object> hash = (LinkedHashMap<String, Object>) obj[1];
		ObjectBoundary event = mapper.convertValue(hash, ObjectBoundary.class);
		//WHEN INVOKE the GET command 
		ObjectBoundary got[] = this.restClient.get()
				.uri("/objects?userSuperapp={userSuperapp}&userEmail={userEmail}&size={size}&page={page}",
						"2024b.Gal.Israeli", "miniAppUserTest3@s.afeka.ac.il", 100, 0)
				.retrieve().body(ObjectBoundary[].class);
		//THEN the new calendar event will be added to the DB
		assertThat(got).usingRecursiveFieldByFieldElementComparator().contains(event);
	}
	@Test
	public void testPutCalander()
	{
		//WHEN server is up
		//AND add a project to DB
		MiniAppCommandBoundary admin = new MiniAppCommandBoundary();
		admin.setCommand("addminiappobject");
		admin.setInvokedBy(new InvokedByBoundary("2024b.Gal.Israeli", "miniAppUserTest3@s.afeka.ac.il"));
		Map<String,Object> arr = new HashMap<>();
		admin.setCommandAttributes(arr);
		admin.getCommandAttributes().put("project name", "new project 420");
		String users[] = {"miniAppUserTest2@s.afeka.ac.il","miniAppUserTest3@s.afeka.ac.il","miniAppUserTest4@s.afeka.ac.il","miniAppUserTest5@s.afeka.ac.il"};
		admin.getCommandAttributes().put("member list",users);
		this.restClient
				.post()
				.uri("/miniapp/Admin App")
				.body(admin)
				.retrieve();
		admin.getCommandAttributes().put("event date","18/7/2024");
		admin.getCommandAttributes().put("start time",5);
		admin.getCommandAttributes().put("end time",8);
		//AND add a new calendar event
		Object[] obj = this.restClient
		.post()
		.uri("/miniapp/Calendar App")
		.body(admin)
		.retrieve()
		.body(Object[].class);
		LinkedHashMap<String, Object> hash = (LinkedHashMap<String, Object>) obj[1];
		ObjectBoundary event1 = mapper.convertValue(hash, ObjectBoundary.class);
		admin.setCommand("updateminiappobject");
		admin.getCommandAttributes().put("start time",6);
		admin.getCommandAttributes().put("title", "BIG title");
		admin.setTargetObject(new TargetObjectBoundary(event1.getObjectId().getSuperapp(),
				event1.getObjectId().getId()));
		//WHEN invoke the POST command to update the calendar miniapp
		obj = this.restClient
				.post()
				.uri("/miniapp/Calendar App")
				.body(admin)
				.retrieve()
				.body(Object[].class);
		hash = (LinkedHashMap<String, Object>) obj[1];
		ObjectBoundary event2 = mapper.convertValue(hash, ObjectBoundary.class);
		//THEN the calendar event will be different
		assertThat(event1).usingRecursiveAssertion().isNotEqualTo(event2);
	}
	@Test
	public void testGetAllCalander()
	{
		//WHEN server is up
		//AND add a project to DB
		MiniAppCommandBoundary admin = new MiniAppCommandBoundary();
		admin.setCommand("addminiappobject");
		admin.setInvokedBy(new InvokedByBoundary("2024b.Gal.Israeli", "miniAppUserTest3@s.afeka.ac.il"));
		Map<String,Object> arr = new HashMap<>();
		admin.setCommandAttributes(arr);
		admin.getCommandAttributes().put("project name", "new project 420");
		String users[] = {"miniAppUserTest2@s.afeka.ac.il","miniAppUserTest3@s.afeka.ac.il","miniAppUserTest4@s.afeka.ac.il","miniAppUserTest5@s.afeka.ac.il"};
		admin.getCommandAttributes().put("member list",users);
		this.restClient
				.post()
				.uri("/miniapp/Admin App")
				.body(admin)
				.retrieve();
		admin.getCommandAttributes().put("event date","18/7/2024");
		admin.getCommandAttributes().put("start time",5);
		admin.getCommandAttributes().put("end time",8);
		admin.getCommandAttributes().put("title", "BIG TITLE");
		Object[] obj = this.restClient
				.post()
				.uri("/miniapp/Calendar App")
				.body(admin)
				.retrieve()
				.body(Object[].class);
		LinkedHashMap<String, Object> hash = (LinkedHashMap<String, Object>) obj[1];
		ObjectBoundary event1 = mapper.convertValue(hash, ObjectBoundary.class);
		admin.getCommandAttributes().put("event date","20/7/2024");
		admin.getCommandAttributes().put("title", "small title");
		//AND add 2 calendar events
		obj = this.restClient
				.post()
				.uri("/miniapp/Calendar App")
				.body(admin)
				.retrieve()
				.body(Object[].class);
		hash = (LinkedHashMap<String, Object>) obj[1];
		ObjectBoundary event2 = mapper.convertValue(hash, ObjectBoundary.class);
		admin.setCommand("getallminiappobjects");
		admin.getCommandAttributes().put("start date","15/7/2024");
		admin.getCommandAttributes().put("end date","21/7/2024");
		//WHEN invoke the POST command to get all calendar miniapp events in the time frame
		obj = this.restClient
				.post()
				.uri("/miniapp/Calendar App")
				.body(admin)
				.retrieve()
				.body(Object[].class);
		ArrayList<LinkedHashMap<String, Object>> newHash = (ArrayList<LinkedHashMap<String, Object>>) obj[1];
		ObjectBoundary[] boundary = mapper.convertValue(newHash, ObjectBoundary[].class);
		//THEN the events will be returned
		assertThat(boundary).usingRecursiveFieldByFieldElementComparator().contains(event1,event2);
		
	}
	@Test
	public void testPostEventGetAnError()
	{
		//WHEN server is up
		//AND add a project to DB
		MiniAppCommandBoundary admin = new MiniAppCommandBoundary();
		admin.setCommand("addminiappobject");
		admin.setInvokedBy(new InvokedByBoundary("2024b.Gal.Israeli", "miniAppUserTest3@s.afeka.ac.il"));
		Map<String,Object> arr = new HashMap<>();
		admin.setCommandAttributes(arr);
		admin.getCommandAttributes().put("project name", "new project 420");
		String users[] = {"miniAppUserTest2@s.afeka.ac.il","miniAppUserTest3@s.afeka.ac.il","miniAppUserTest4@s.afeka.ac.il","miniAppUserTest5@s.afeka.ac.il"};
		admin.getCommandAttributes().put("member list",users);
		this.restClient
				.post()
				.uri("/miniapp/Admin App")
				.body(admin)
				.retrieve();
		admin.getCommandAttributes().put("event date","18/7/2024");
		admin.getCommandAttributes().put("start time",5);
		admin.getCommandAttributes().put("end time",8);
		admin.getCommandAttributes().put("title", "BIG TITLE");
		//AND add a new calendar event
		this.restClient
				.post()
				.uri("/miniapp/Calendar App")
				.body(admin)
				.retrieve();
		try {
			//WHEN invoke the POST command to add the same event again
			this.restClient
			.post()
			.uri("/miniapp/Calendar App")
			.body(admin)
			.retrieve();
		} catch (Exception e) {
			//THEN an inputException will be thrown
			assertEquals(e, InputException.class);
		}
		
		
	}
}
