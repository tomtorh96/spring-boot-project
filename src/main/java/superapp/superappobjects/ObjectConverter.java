package superapp.superappobjects;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.stereotype.Component;
import exception.InputException;
import superapp.users.UserConverter;
import superapp.users.UserIdBoundary;

@Component
public class ObjectConverter {
	private UserConverter userConverter;

	public ObjectConverter(UserConverter userConverter) {
		this.userConverter = userConverter;
	}

	public ObjectBoundary toBoundary(ObjectEntity entity) {
		ObjectBoundary boundary = new ObjectBoundary();
		String objectId[] = entity.getId().split("@@");
		boundary.setObjectId(new ObjectId(objectId[0], objectId[1]));
		boundary.setAlias(entity.getAlias());
		boundary.setType(entity.getType());
		boundary.setObjectDetails(entity.getDetails());
		boundary.setLocation(new Location(entity.getLatitude(), entity.getLongitude()));
		boundary.setActive(entity.getActive());
		boundary.setCreationTimestamp(entity.getCreationTimestamp());
		String created[] = entity.getCreatedBy().split("@@");
		boundary.setCreatedBy(new CreatedBy(new UserIdBoundary(created[0], created[1])));

		if (entity.getProjectName() != null)
			boundary.getObjectDetails().put("project name", entity.getProjectName());
		if (entity.getMemberList() != null) {
			String[] memberList = memberListStringToArray(entity.getMemberList());
			boundary.getObjectDetails().put("member list", memberList);
		}
	
		if (entity.getEventDate() != null) {
			boundary.getObjectDetails().put("event date", convertDateToString(entity.getEventDate()));
			boundary.getObjectDetails().put("start time", entity.getStartTime());
			boundary.getObjectDetails().put("end time", entity.getEndTime());
		}

		return boundary;
	}

	public ObjectEntity toEntity(ObjectBoundary boundary) {

		ObjectEntity entity = new ObjectEntity();
		entity.setId(this.userConverter.convertToEntityId(boundary.getObjectId().getSuperapp(),
				boundary.getObjectId().getId()));
		if (boundary.getAlias() != null) {
			entity.setAlias(boundary.getAlias());
		} else {
			entity.setAlias("defaultAlias:" + boundary.getObjectId().getSuperapp());
		}
		entity.setCreationTimestamp(boundary.getCreationTimestamp());
		if (boundary.getActive() != null)
			entity.setActive(boundary.getActive());
		else
			entity.setActive(false);
		if (boundary.getType() != null)
			entity.setType(boundary.getType());
		else
			entity.setType("dummy");
		if (boundary.getCreatedBy() != null)
			entity.setCreatedBy(this.userConverter.convertToEntityId(boundary.getCreatedBy().getUserId().getSuperapp(),
					boundary.getCreatedBy().getUserId().getEmail()));
		entity.setLatitude(boundary.getLocation().getLatitude());
		entity.setLongitude(boundary.getLocation().getLongitude());
		entity.setDetails(boundary.getObjectDetails());
		entity.setType(boundary.getType());
		if (boundary.getObjectDetails().get("project name") != null)
			entity.setProjectName(boundary.getObjectDetails().get("project name").toString());
		if (boundary.getObjectDetails().get("member list") != null) {
			String memberList = memberListArrayToString((Object[]) boundary.getObjectDetails().get("member list"));
			entity.setMemberList(memberList);
		}
		if (boundary.getObjectDetails().get("event date") != null) {
			entity.setEventDate(convertStringToDate(boundary.getObjectDetails().get("event date").toString()));
			entity.setStartTime((double) boundary.getObjectDetails().get("start time"));
			entity.setEndTime((double) boundary.getObjectDetails().get("end time"));
		}

		return entity;
	}

	public String[] memberListStringToArray(String memberList) {
		String[] arr = memberList.replaceFirst("^,+", "") // Remove leading commas
				.split(",+");
		return arr;
	}

	public String memberListArrayToString(Object[] memberList) {
		String memberListString = "";
		for (int i = 0; i < memberList.length - 1; i++) {
			memberListString += memberList[i].toString();
			memberListString += ",";

		}
		memberListString += memberList[memberList.length - 1].toString();
		System.err.println(memberListString);
		return memberListString;

	}

	public Date convertStringToDate(String stringDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		try {
			Date date = sdf.parse(stringDate);
			return date;
		} catch (ParseException e) {
			throw new InputException(
					"Date is not in the correct format of: dd/MM/yyyy, Date provided ---> " + stringDate);
		}
	}

	public String convertDateToString(Date date) {
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		return dateFormat.format(date);

	}
}
