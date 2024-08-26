package superapp.command;

import java.util.List;

public interface CommandService {

	public Object[] invokeCommand(MiniAppCommandBoundary commandBoundary, String miniapp);

	@Deprecated
	public List<MiniAppCommandBoundary> getAllMiniAppCommands();

	@Deprecated
	public List<MiniAppCommandBoundary> getAllMiniAppCommandsByMiniAppName(String miniAppName);

	public void deleteAllMiniAppCommands();

}