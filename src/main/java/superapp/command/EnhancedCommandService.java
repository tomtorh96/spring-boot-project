package superapp.command;

import java.util.List;

public interface EnhancedCommandService extends CommandService{
	public List<MiniAppCommandBoundary> getAllMiniAppCommands(int size, int page);

	public List<MiniAppCommandBoundary> getAllMiniAppCommandsByMiniAppName(String miniAppName,int size, int page);

}
