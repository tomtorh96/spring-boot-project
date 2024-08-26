package superapp.command;


public interface MiniAppCommandsService extends EnhancedCommandService {
	public Object[] addMiniAppObject(MiniAppCommandBoundary input);

	public Object[] getAllMiniAppObjects(MiniAppCommandBoundary input,
			int size, int page);
	public Object[] updateMiniAppObject(MiniAppCommandBoundary input);
}
