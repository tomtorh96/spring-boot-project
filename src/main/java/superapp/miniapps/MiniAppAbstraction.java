package superapp.miniapps;

import java.util.List;



import superapp.command.MiniAppCommandBoundary;
import superapp.superappobjects.ObjectBoundary;

public interface MiniAppAbstraction {
	
	public ObjectBoundary addMiniAppObject(MiniAppCommandBoundary input);

	public List<ObjectBoundary> getAllMiniAppObjects(MiniAppCommandBoundary input,int size, int page);

	public ObjectBoundary updateMiniAppObject(MiniAppCommandBoundary input);
	
	
	
}
