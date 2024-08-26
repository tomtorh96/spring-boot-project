package superapp.command;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import exception.InputException;


@RestController
@RequestMapping(path = { "superapp/miniapp" })
public class CommandController {
	private MiniAppCommandsService commandService;
	

	public CommandController(MiniAppCommandsService commandService) {
		this.commandService = commandService;
		
	}

	@PostMapping(path = {
			"/{miniAppName}" }, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Object[] store(@RequestBody MiniAppCommandBoundary command,
			@PathVariable("miniAppName") String miniAppName) {

		if (command.getCommand() == null)
			new InputException("No command provided");

		String action = command.getCommand();
		if (command.getCommandId() == null)
			command.setCommandId(new CommandIdBoundary());
		command.getCommandId().setMiniapp(miniAppName);

		switch (action) {
		case "addminiappobject":
			return commandService.addMiniAppObject(command);
		case "updateminiappobject":
			return commandService.updateMiniAppObject(command);
		case "getallminiappobjects":
			return commandService.getAllMiniAppObjects(command, 20, 0);
		default:
			throw new InputException(
					"command provided doesn't exist, these are the options: addminiappobject , updateminiappobject , getallminiappobjects");
		}

	}

}
