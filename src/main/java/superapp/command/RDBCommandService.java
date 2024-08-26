package superapp.command;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import exception.InputException;
import exception.UnauthorizedException;
import superapp.miniapps.MiniAppAbstraction;
import superapp.superappobjects.MiniAppSupportObjectService;
import superapp.superappobjects.ObjectBoundary;
import superapp.superappobjects.ObjectId;
import superapp.users.UserBoundary;
import superapp.users.UserRoleEnum;
import superapp.users.UserService;

@Service
public class RDBCommandService implements MiniAppCommandsService {
	private CommandDao commandDao;
	private CommandConverter commandConverter;
	private String superAppName;
	private ApplicationContext applicationContext;
	private UserService userService;
	private MiniAppSupportObjectService objectService;

	public RDBCommandService(CommandDao commandDao, CommandConverter commandConverter, UserService userService,
			ApplicationContext applicationContext, MiniAppSupportObjectService objectService) {
		this.commandDao = commandDao;
		this.commandConverter = commandConverter;
		this.applicationContext = applicationContext;
		this.userService = userService;
		this.objectService = objectService;
	}

	@Value("${spring.application.name}")
	public void setDefaultSuperAppName(String name) {
		this.superAppName = name;
	}

	@Override
	@Transactional(readOnly = false)
	public Object[] invokeCommand(MiniAppCommandBoundary commandBoundary, String miniapp) throws RuntimeException {

		Object array[] = new Object[5];

		UserBoundary user = this.userService.getSpecificUser(commandBoundary.getInvokedBy().getUserId().getSuperapp(),
				commandBoundary.getInvokedBy().getUserId().getEmail()).get();

		commandBoundary.getCommandId().setId(UUID.randomUUID().toString());
		commandBoundary.getCommandId().setMiniapp(miniapp);
		commandBoundary.getCommandId().setSuperapp(this.superAppName);
		commandBoundary.setInvocationTimestamp(new Date());

		Optional<ObjectBoundary> ob = objectService.retrieveObject(
				commandBoundary.getTargetObject().getObjectId().getSuperapp(),
				commandBoundary.getTargetObject().getObjectId().getId(), user.getUserId().getSuperapp(),
				user.getUserId().getEmail());

		if (ob.isEmpty())
			throw new InputException("Command's targetObject was not found in DB");
		if (!ob.get().getActive())
			throw new InputException("Command's targetObject is not active");

		if (user.getRole().compareTo(UserRoleEnum.MINIAPP_USER) != 0)
			throw new UnauthorizedException("you are unathorized to use this function");

		CommandEntity entity = this.commandConverter.toEntity(commandBoundary);
		entity = this.commandDao.save(entity);

		MiniAppCommandBoundary rv = this.commandConverter.toBoundary(entity);

		System.err.println("* server stored: " + rv);

		return array;
	}

	@Override
	@Deprecated
	public List<MiniAppCommandBoundary> getAllMiniAppCommands() {
		throw new CommandDeprecationException(
				"You should not invoke this method, use the pagaination supporting method instead");
	}

	@Override
	@Transactional(readOnly = true)
	public List<MiniAppCommandBoundary> getAllMiniAppCommands(int size, int page) {
		List<CommandEntity> entities = this.commandDao
				.findAll(PageRequest.of(page, size, Direction.DESC, "invocationTimeStamp", "commandId")).toList();
		List<MiniAppCommandBoundary> boundaries = new ArrayList<>();
		for (CommandEntity entity : entities) {
			boundaries.add(this.commandConverter.toBoundary(entity));
		}
		System.err.println("* data from database: " + boundaries);
		return boundaries;
	}

	@Override
	@Deprecated
	public List<MiniAppCommandBoundary> getAllMiniAppCommandsByMiniAppName(String miniAppName) {
		throw new CommandDeprecationException(
				"You should not invoke this method, use the pagaination supporting method instead");
	}

	@Override
	@Transactional(readOnly = true)
	public List<MiniAppCommandBoundary> getAllMiniAppCommandsByMiniAppName(String miniAppName, int size, int page) {
		List<MiniAppCommandBoundary> boundaries = this.commandDao
				.findAllByMiniAppName(miniAppName, PageRequest.of(page, size, Direction.ASC, "miniAppName")).stream()
				.map(entity -> this.commandConverter.toBoundary(entity)).toList();
		return boundaries;
	}

	@Override
	@Transactional(readOnly = false)
	public void deleteAllMiniAppCommands() {
		System.err.println("* deleting table for commands");
		this.commandDao.deleteAll();

	}

	@Override
	@Transactional(readOnly = false)
	public Object[] addMiniAppObject(MiniAppCommandBoundary input) {
		Object[] output = new Object[2];
		MiniAppAbstraction miniapp = null;
		ObjectBoundary newOb = null;

		setGeneralCommandFields(input);

		try {
			miniapp = this.applicationContext.getBean(input.getCommandId().getMiniapp(), MiniAppAbstraction.class);

		} catch (Exception e) {
			throw new InputException("Couldn't find any miniapps with the provided name");
		}

		newOb = miniapp.addMiniAppObject(input);

		output[0] = "Success";
		output[1] = newOb;
		if (newOb != null)
			input.getTargetObject().setObjectId(newOb.getObjectId());
		invokeCommand(input, input.getCommandId().getMiniapp());

		return output;

	}

	@Override
	@Transactional(readOnly = false)
	public Object[] getAllMiniAppObjects(MiniAppCommandBoundary input, int size, int page) {
		Object[] output = new Object[2];
		MiniAppAbstraction miniapp = null;
		List<ObjectBoundary> boundaryList = null;

		setGeneralCommandFields(input);

		try {
			miniapp = this.applicationContext.getBean(input.getCommandId().getMiniapp(), MiniAppAbstraction.class);

		} catch (Exception e) {
			throw new InputException("Couldn't find any miniapps with the provided name");
		}

		boundaryList = miniapp.getAllMiniAppObjects(input, size, page);

		output[0] = "Success";
		output[1] = boundaryList.toArray();
		if (!boundaryList.isEmpty())
			input.getTargetObject().setObjectId(boundaryList.getFirst().getObjectId());
		else {
			input.getTargetObject().setObjectId(objectService.getDefault().getObjectId());

		}
		invokeCommand(input, input.getCommandId().getMiniapp());

		return output;
	}

	@Override
	@Transactional(readOnly = false)
	public Object[] updateMiniAppObject(MiniAppCommandBoundary input) {
		Object[] output = new Object[2];
		MiniAppAbstraction miniapp = null;
		ObjectBoundary newOb = null;

		setGeneralCommandFields(input);

		try {
			miniapp = this.applicationContext.getBean(input.getCommandId().getMiniapp(),
					MiniAppAbstraction.class);
		} catch (Exception e) {
			throw new InputException("Couldn't find any miniapps with the provided name");
		}

		if (input.getInvokedBy() == null || input.getInvokedBy().getUserId() == null)
			throw new InputException("Missing Invoked by field");

		newOb = miniapp.updateMiniAppObject(input);

		output[0] = "Success";
		if (newOb.getActive()) {
			output[1] = newOb;
			invokeCommand(input, input.getCommandId().getMiniapp());
		} else
			output[1] = "Mini App object deactivated successfully";

		return output;
	}

	public void setGeneralCommandFields(MiniAppCommandBoundary input) {
		if (input.getCommandId() == null)
			input.setCommandId(new CommandIdBoundary());
		if (input.getInvokedBy() == null || input.getInvokedBy().getUserId() == null
				|| input.getInvokedBy().getUserId().getEmail() == null)
			throw new InputException("All commands must be invoked by a valid mini app user");
		String superAppName = input.getInvokedBy().getUserId().getSuperapp();
		input.getCommandId().setSuperapp(superAppName);
		if (input.getTargetObject() == null)
			input.setTargetObject(new TargetObjectBoundary(superAppName, ""));
		else if (input.getTargetObject().getObjectId() == null)
			input.getTargetObject().setObjectId(new ObjectId(superAppName, ""));

	}

}
