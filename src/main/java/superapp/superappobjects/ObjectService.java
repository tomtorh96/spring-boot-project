package superapp.superappobjects;

import java.util.List;
import java.util.Optional;

public interface ObjectService {
	@Deprecated
	public Optional<ObjectBoundary> retrieveObject(String superApp,String id);
	@Deprecated
	public List<ObjectBoundary> retrieveAllObject();
	public ObjectBoundary createAnObject (ObjectBoundary SuperAppBoundary);
	public void deleteAllObjects ();
	@Deprecated
	public void updateAnObject (String superApp,String id, ObjectBoundary update);
	
	/*public ObjectBoundary updateAnObject (String superApp,String id,String userSuperApp,String email,
			ObjectBoundary update);*/
	@Deprecated
	public List<ObjectBoundary> searchByType(String message);
}
