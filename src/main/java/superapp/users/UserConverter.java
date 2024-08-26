package superapp.users;

import org.springframework.stereotype.Component;

@Component
public class UserConverter {

	public String convertToEntityId(String sa, String e) {
		return sa + "@@" + e;
	}

	public String[] convertFromEntityId(String entityId) {
		String[] str1 = entityId.split("@@");
		return str1;
	}

	public UserBoundary toBoundary(UserEntity entity) {
		UserBoundary boundary = new UserBoundary();
		String[] id = convertFromEntityId(entity.getUserId());
		boundary.setUserId(new UserIdBoundary(id[0], id[1]));
		boundary.setRole(toBoundary(entity.getRole()));
		boundary.setUsername(entity.getUsername());
		boundary.setAvatar(entity.getAvatar());
		return boundary;
	}

	public UserEntity toEntity(UserBoundary boundary) {
		UserEntity entity = new UserEntity();
		entity.setUserId(convertToEntityId(boundary.getUserId().getSuperapp(), boundary.getUserId().getEmail()));
		entity.setUsername(boundary.getUsername());
		entity.setAvatar(boundary.getAvatar());
		entity.setRole(toEntity(boundary.getRole()));
		return entity;
	}

	public UserRoleEnum toBoundary(UserRoleEnumInDB e) {
		return UserRoleEnum.valueOf(e.name().toUpperCase());
	}

	public UserRoleEnumInDB toEntity(UserRoleEnum e) {
		return UserRoleEnumInDB.valueOf(e.name().toLowerCase());
	}

}
