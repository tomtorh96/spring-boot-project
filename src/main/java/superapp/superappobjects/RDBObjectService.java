package superapp.superappobjects;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import exception.DeprecationException;
import exception.InputException;
import exception.NotFoundException;
import exception.UnauthorizedException;
import superapp.users.UserBoundary;
import superapp.users.UserConverter;
import superapp.users.UserRoleEnum;
import superapp.users.UserService;

@Service
public class RDBObjectService implements MiniAppSupportObjectService {

	private ObjectDao superAppDao;
	private ObjectConverter objectConverter;
	private UserConverter userConverter;
	private String defaultName;
	private UserService userService;

	public RDBObjectService(ObjectDao superAppDao, ObjectConverter objectConverter, UserConverter userconverter,
			UserService userService) {
		this.superAppDao = superAppDao;
		this.objectConverter = objectConverter;
		this.userConverter = userconverter;
		this.userService = userService;

	}

	@Deprecated
	@Override
	public Optional<ObjectBoundary> retrieveObject(String superApp, String id) {
		throw new DeprecationException(
				"You should not invoke this method, use the pagaination supporting method instead");
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<ObjectBoundary> retrieveObject(String superApp, String id, String userSuperapp, String email) {
		Optional<UserBoundary> user = this.userService.getSpecificUser(userSuperapp, email);
		if (user == null)
			throw new NotFoundException("didnt find the user: " + email);
		if (user.get().getRole().compareTo(UserRoleEnum.ADMIN) == 0) {
			throw new UnauthorizedException("Admin users aren't authorized to retrieve Superapp objects");
		}
		String objectID = this.userConverter.convertToEntityId(superApp, id);
		Optional<ObjectEntity> entityOp = this.superAppDao.findById(objectID);
		Optional<ObjectBoundary> boundaryOp = entityOp.map(this.objectConverter::toBoundary);
		if (boundaryOp.isEmpty() || (user.get().getRole().compareTo(UserRoleEnum.MINIAPP_USER) == 0
				&& boundaryOp.get().getActive() != true)) {
			throw new NotFoundException("No Super app objects found with the objectID provided");
		}

		return boundaryOp;

	}

	@Override
	@Deprecated
	public List<ObjectBoundary> retrieveAllObject() {
		throw new DeprecationException(
				"You should not invoke this method, use the pagaination supporting method instead");
	}

	@Override
	@Transactional(readOnly = false)
	public ObjectBoundary createAnObject(ObjectBoundary demoBoundary) {
		if (demoBoundary.getObjectId() == null)
			demoBoundary.setObjectId(new ObjectId());
		demoBoundary.getObjectId().setId(UUID.randomUUID().toString());
		demoBoundary.getObjectId().setSuperapp(defaultName);
		demoBoundary.setCreationTimestamp(new Date());
		if (demoBoundary.getAlias() == null || demoBoundary.getAlias().isEmpty())
			throw new InputException("object must have an alias");
		if (demoBoundary.getType() == null || demoBoundary.getType().isEmpty())
			throw new InputException("object must have a type");

		if (!locationIsCorrect(demoBoundary.getLocation()))
			throw new InputException("object must have an a location");

		if (demoBoundary.getCreatedBy() == null || demoBoundary.getCreatedBy().getUserId() == null
				|| demoBoundary.getCreatedBy().getUserId().getEmail() == null
				|| demoBoundary.getCreatedBy().getUserId().getSuperapp() == null)
			throw new InputException("object must have a user with a superApp name and email");

		Optional<UserBoundary> user = this.userService.getSpecificUser(
				demoBoundary.getCreatedBy().getUserId().getSuperapp(),
				demoBoundary.getCreatedBy().getUserId().getEmail());
		if (user == null)
			throw new NotFoundException("didnt find the user: " + demoBoundary.getCreatedBy().getUserId().getEmail());

		if (user.get().getRole().compareTo(UserRoleEnum.SUPERAPP_USER) != 0) {
			throw new UnauthorizedException("you are unathorized to use this function");
		}

		ObjectEntity entity = this.objectConverter.toEntity(demoBoundary);

		entity = this.superAppDao.save(entity);
		return this.objectConverter.toBoundary(entity);
	}

	private boolean locationIsCorrect(Location location) {
		if (location == null)
			return false;
		if (location.getLatitude() < 0 || location.getLongitude() < 0)
			return false;
		return true;
	}

	@Override
	@Transactional(readOnly = false)
	public void deleteAllObjects() {
		this.superAppDao.deleteAll();
	}

	@Deprecated
	@Override
	@Transactional(readOnly = false)
	public void updateAnObject(String superApp, String id, ObjectBoundary update) {
		throw new DeprecationException("You should not invoke this method");
	}

	@Override
	@Transactional(readOnly = false)
	public ObjectBoundary updateAnObject(String superApp, String id, String userSuperapp, String email,
			ObjectBoundary update) {
		Optional<UserBoundary> user = this.userService.getSpecificUser(userSuperapp, email);
		if (user == null)
			throw new NotFoundException("didnt find the user: " + email);

		if (user.get().getRole().compareTo(UserRoleEnum.SUPERAPP_USER) != 0) {
			throw new UnauthorizedException("you are unathorized to use this function");
		}

		String objectID = this.userConverter.convertToEntityId(superApp, id);
		ObjectEntity entity = this.superAppDao.findById(objectID).orElseThrow(() -> new NotFoundException(
				"************Could not find super app object for update by id: " + objectID));
		/*
		 * Can't Change createdBy
		 */
		if (update.getAlias() != null)
			entity.setAlias(update.getAlias());

		if (update.getObjectDetails() != null) {
			if (update.getObjectDetails().get("member list") != null) {
				String memberList = objectConverter
						.memberListArrayToString((Object[]) update.getObjectDetails().get("member list"));
				entity.setMemberList(memberList);
				update.getObjectDetails().remove("member list");
			}
			if (update.getObjectDetails().get("event date") != null)
				entity.setEventDate(
						objectConverter.convertStringToDate(update.getObjectDetails().get("event date").toString()));
			if (update.getObjectDetails().get("start time") != null)
				entity.setStartTime((double) update.getObjectDetails().get("start time"));
			if (update.getObjectDetails().get("end time") != null)
				entity.setEndTime((double) update.getObjectDetails().get("end time"));
			entity.setDetails(update.getObjectDetails());
		}

		if (update.getType() != null)
			entity.setType(update.getType());
		if (update.getLocation() != null && locationIsCorrect(update.getLocation())) {
			entity.setLatitude(update.getLocation().getLatitude());
			entity.setLongitude(update.getLocation().getLongitude());
		}
		if (update.getActive() != null)
			entity.setActive(update.getActive());

		entity = this.superAppDao.save(entity);
		return objectConverter.toBoundary(entity);
	}

	@Override
	@Deprecated
	public List<ObjectBoundary> searchByType(String type) {
		throw new DeprecationException("You should not invoke this method");
	}

	@Value("${spring.application.name}")
	public void setDefaultSuperAppName(String name) {
		this.defaultName = name;
	}

	@Deprecated
	@Override
	@Transactional(readOnly = true)
	public List<ObjectBoundary> getAllObjects() {
		throw new DeprecationException("You should not invoke this method");
	}

	@Override
	@Transactional(readOnly = true)
	public List<ObjectBoundary> getAllObjects(String superApp, String email, int size, int page) {
		Optional<UserBoundary> user = this.userService.getSpecificUser(superApp, email);
		if (user == null)
			throw new NotFoundException("didnt find the user: " + email);

		if (user.get().getRole().compareTo(UserRoleEnum.ADMIN) == 0) {
			throw new UnauthorizedException("you are unathorized to use this function");
		}
		if (user.get().getRole().compareTo(UserRoleEnum.MINIAPP_USER) == 0)
			return this.superAppDao
					.findAllByActive(true, PageRequest.of(page, size, Direction.DESC, "type", "alias", "id")).stream()
					.map(entity -> this.objectConverter.toBoundary(entity)).toList();
		return this.superAppDao.findAll(PageRequest.of(page, size, Direction.DESC, "type", "alias", "id")).stream()
				.map(this.objectConverter::toBoundary).peek(System.err::println).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<ObjectBoundary> searchByType(String type, int size, String superapp, String email, int page) {
		Optional<UserBoundary> user = this.userService.getSpecificUser(superapp, email);
		if (user == null)
			throw new NotFoundException("didnt find the user: " + email);

		if (user.get().getRole().compareTo(UserRoleEnum.ADMIN) == 0) {
			throw new UnauthorizedException("you are unathorized to use this function");
		}
		if (user.get().getRole().compareTo(UserRoleEnum.MINIAPP_USER) == 0)
			return this.superAppDao
					.findAllByTypeAndActive(type, true,
							PageRequest.of(page, size, Direction.ASC, "type", "alias", "id"))
					.stream().map(entity -> this.objectConverter.toBoundary(entity)).toList();

		return this.superAppDao.findAllByType(type, PageRequest.of(page, size, Direction.ASC, "type", "alias", "id"))
				.stream().map(entity -> this.objectConverter.toBoundary(entity)).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<ObjectBoundary> searchByAlias(String alias, String superapp, String email, int size, int page) {
		Optional<UserBoundary> user = this.userService.getSpecificUser(superapp, email);
		if (user == null)
			throw new NotFoundException("didnt find the user: " + email);

		if (user.get().getRole().compareTo(UserRoleEnum.ADMIN) == 0) {
			throw new UnauthorizedException("you are unauthorized to use this function");
		}
		if (user.get().getRole().compareTo(UserRoleEnum.MINIAPP_USER) == 0)

			return this.superAppDao
					.findAllByAliasAndActive(alias, true,
							PageRequest.of(page, size, Direction.ASC, "type", "alias", "id"))
					.stream().map(entity -> this.objectConverter.toBoundary(entity)).toList();

		return this.superAppDao.findAllByAlias(alias, PageRequest.of(page, size, Direction.ASC, "type", "alias", "id"))
				.stream().map(entity -> this.objectConverter.toBoundary(entity)).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<ObjectBoundary> searchByAliasPattern(String pattern, String superapp, String email, int size,
			int page) {
		String finalPattern = "%" + pattern.trim() + "%";
		Optional<UserBoundary> user = this.userService.getSpecificUser(superapp, email);
		if (user == null)
			throw new NotFoundException("didnt find the user: " + email);
		if (user.get().getRole().compareTo(UserRoleEnum.ADMIN) == 0) {
			throw new UnauthorizedException("you are unathorized to use this function");
		}
		if (user.get().getRole().compareTo(UserRoleEnum.MINIAPP_USER) == 0)
			return this.superAppDao
					.findAllByAliasLikeAndActive(finalPattern, true,
							PageRequest.of(page, size, Direction.ASC, "type", "alias", "id"))
					.stream().map(entity -> this.objectConverter.toBoundary(entity)).toList();

		return this.superAppDao
				.findAllByAliasLike(finalPattern, PageRequest.of(page, size, Direction.ASC, "type", "alias", "id"))
				.stream().map(entity -> this.objectConverter.toBoundary(entity)).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<ObjectBoundary> searchByLocation(double lat, double lng, double distance, String unit, String superapp,
			String email, int size, int page) {
		Optional<UserBoundary> user = this.userService.getSpecificUser(superapp, email);
		if (user == null)
			throw new NotFoundException("didnt find the user: " + email);
		if (user.get().getRole().compareTo(UserRoleEnum.ADMIN) == 0) {
			throw new UnauthorizedException("you are unathorized to use this function");
		}
		double units;
		switch (unit.toLowerCase()) {
		case "miles":
			units = 3959;
			if (user.get().getRole().compareTo(UserRoleEnum.MINIAPP_USER) == 0)
				return this.superAppDao
						.findAllBylocationBetweenCurveAndActive(lat, lng, distance, units, true,
								PageRequest.of(page, size, Direction.ASC, "type", "alias", "id"))
						.stream().map(entity -> this.objectConverter.toBoundary(entity)).toList();
			return this.superAppDao
					.findAllBylocationBetweenCurve(lat, lng, distance, units,
							PageRequest.of(page, size, Direction.ASC, "type", "alias", "id"))
					.stream().map(entity -> this.objectConverter.toBoundary(entity)).toList();
		case "kilometers":
			units = 6371;
			if (user.get().getRole().compareTo(UserRoleEnum.MINIAPP_USER) == 0)
				return this.superAppDao
						.findAllBylocationBetweenCurveAndActive(lat, lng, distance, units, true,
								PageRequest.of(page, size, Direction.ASC, "type", "alias", "id"))
						.stream().map(entity -> this.objectConverter.toBoundary(entity)).toList();
			return this.superAppDao
					.findAllBylocationBetweenCurve(lat, lng, distance, units,
							PageRequest.of(page, size, Direction.ASC, "type", "alias", "id"))
					.stream().map(entity -> this.objectConverter.toBoundary(entity)).toList();
		default:
			if (user.get().getRole().compareTo(UserRoleEnum.MINIAPP_USER) == 0)
				return this.superAppDao
						.findAllBylocationBetweenAndActive(lat, lng, distance, true,
								PageRequest.of(page, size, Direction.ASC, "type", "alias", "id"))
						.stream().map(entity -> this.objectConverter.toBoundary(entity)).toList();
			return this.superAppDao
					.findAllBylocationBetween(lat, lng, distance,
							PageRequest.of(page, size, Direction.ASC, "type", "alias", "id"))
					.stream().map(entity -> this.objectConverter.toBoundary(entity)).toList();
		}

	}

	public Optional<ObjectBoundary> findByTypeAndProjectNameAndActive(String type, String projectName, boolean active) {
		return superAppDao.findByTypeAndProjectNameAndActive(type, projectName, active)
				.map(this.objectConverter::toBoundary);
	}

	public List<ObjectBoundary> findAllByTypeAndProjectNameAndActive(String type, String projectName, boolean active,
			int size, int page) {
		return superAppDao
				.findAllByTypeAndProjectNameAndActive(type, projectName, active,
						PageRequest.of(page, size, Direction.DESC, "creationTimestamp"))
				.stream().map(entity -> this.objectConverter.toBoundary(entity)).toList();
	}

	@Override
	public List<ObjectBoundary> findAllByTypeAndActiveAndByMemberListContaining(String type, boolean active,
			String member) {
		return superAppDao.findAllByTypeAndActiveAndByMemberListContaining(type, active, member).stream()
				.map(entity -> this.objectConverter.toBoundary(entity)).toList();
	}

	@Override
	public ObjectBoundary getDefault() {
		ObjectBoundary defaultTargetOb = null;
		String objectID = this.userConverter.convertToEntityId("default", "default");
		Optional<ObjectEntity> entityOp = this.superAppDao.findById(objectID);
		if (entityOp.isEmpty()) {
			defaultTargetOb = new ObjectBoundary();
			defaultTargetOb.setActive(true);
			defaultTargetOb.setObjectId(new ObjectId("default", "default"));
			defaultTargetOb.setAlias("default");
			defaultTargetOb.setType("default");
			defaultTargetOb.setLocation(new Location());
			defaultTargetOb.setCreatedBy(new CreatedBy(this.userService
					.getSpecificUser("2024b.Gal.Israeli", "miniAppUser1@s.afeka.ac.il").get().getUserId()));
			defaultTargetOb.setObjectDetails(new HashMap<String, Object>());
			ObjectEntity entity = this.objectConverter.toEntity(defaultTargetOb);

			entity = this.superAppDao.save(entity);

			return objectConverter.toBoundary(entity);
		} else
			return objectConverter.toBoundary(entityOp.get());

	}

	public void updateMembers(ArrayList<String> membersToRemove, String projectName) throws RuntimeException {
		for (String member : membersToRemove) {
			superAppDao.updateMemberList("kanban", true, projectName, member.toString());
			superAppDao.updateMemberList("calendar", true, projectName, member.toString());
		}

	}

	@Override
	// @Transactional(readOnly = false)
	public ObjectBoundary retrieveObjectForUpdateForMiniApps(String superApp, String id, String userSuperapp,
			String email) throws RuntimeException {
		Optional<UserBoundary> user = this.userService.getSpecificUser(userSuperapp, email);

		if (user == null)
			throw new NotFoundException("didnt find the user: " + email);
		if (user.get().getRole().compareTo(UserRoleEnum.ADMIN) == 0) {
			throw new UnauthorizedException("Admin users aren't authorized to do any actions on miniapp objects");
		}
		String objectID = this.userConverter.convertToEntityId(superApp, id);
		ObjectEntity entityOp = this.superAppDao.findById(objectID).orElseThrow(
				() -> new NotFoundException("Could not find super app object for update by id: " + objectID));

		ObjectBoundary boundaryOp = objectConverter.toBoundary(entityOp);
		if ((user.get().getRole().compareTo(UserRoleEnum.MINIAPP_USER) == 0 && boundaryOp.getActive() != true))
			throw new NotFoundException("No Super app objects found with the objectID provided");

		return boundaryOp;
	}

	@Override
	public List<ObjectBoundary> checkEventConflicts(Date date, double startTime, double endTime, String id) {

		return superAppDao.checkEventConstraints(date, startTime, endTime, true, id).stream()
				.map(entity -> objectConverter.toBoundary(entity)).toList();
	}

	@Override
	public List<ObjectBoundary> checkEventConflicts(Date date, double startTime, double endTime) {
		return superAppDao.checkEventConstraints(date, startTime, endTime, true).stream()
				.map(entity -> objectConverter.toBoundary(entity)).toList();
	}

	@Override
	public List<ObjectBoundary> findAllByEventDateBetweenAndActiveAndProjectName(Date startDate, Date endDate,
			String projectName) {
		return superAppDao.findAllByEventDateBetweenAndActiveAndProjectName(startDate, endDate, true, projectName)
				.stream().map(entity -> objectConverter.toBoundary(entity)).toList();
	}

	@Override
	public void deactivateAllObjectRelatedToProjectName(String projectName) {
		superAppDao.deactivateAllObjectRelatedToProjectName(projectName);

	}

}
