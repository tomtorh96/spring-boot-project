package superapp.superappobjects;

import java.util.List;
import java.util.Optional;

public interface EnhancedObjectService extends ObjectService {
	@Deprecated
	public List<ObjectBoundary> getAllObjects();
	public List<ObjectBoundary> getAllObjects(String userSuperapp,String email,int size, int page);
	@Deprecated
	public Optional<ObjectBoundary> retrieveObject(String superApp,String id);
	public Optional<ObjectBoundary> retrieveObject(String superApp,String id,
			String userSuperapp,String email);
	
	public ObjectBoundary updateAnObject (String superApp,String id,
			String userSuperapp,String email, ObjectBoundary update);
	
	public List<ObjectBoundary> searchByType(
		String type,int size, String superapp, String email, int page);

	public List<ObjectBoundary> searchByAlias(String alias, String superapp, String email,int size, int page);

	public List<ObjectBoundary> searchByAliasPattern(String Pattern, String superapp, String email,int size, int page);

	public List<ObjectBoundary> searchByLocation(double lat,double lng,double distance,String unit,String superapp, String email, int size, int page);
}
