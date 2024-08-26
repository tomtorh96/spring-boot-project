package superapp.superappobjects;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.query.Param;


public interface MiniAppSupportObjectService extends EnhancedObjectService {

	public Optional<ObjectBoundary> findByTypeAndProjectNameAndActive(String type, String projectName, boolean active);

	public List<ObjectBoundary> findAllByTypeAndProjectNameAndActive(String type, String projectName, boolean active,
			int size, int page);

	public List<ObjectBoundary> findAllByTypeAndActiveAndByMemberListContaining(String type, boolean active,
			String member);

	public ObjectBoundary getDefault();

	public void updateMembers(ArrayList<String> membersToRemove, String projectName);

	ObjectBoundary retrieveObjectForUpdateForMiniApps(String superApp, String id, String userSuperapp, String email);

	public List<ObjectBoundary> checkEventConflicts(Date date, double startTime, double endTime,String id);
	public List<ObjectBoundary> checkEventConflicts(Date date, double startTime, double endTime);

	public List<ObjectBoundary> findAllByEventDateBetweenAndActiveAndProjectName(Date startDate, Date endDate,
			String projectName);
	
	public void deactivateAllObjectRelatedToProjectName(@Param("projectName") String projectName);

}
