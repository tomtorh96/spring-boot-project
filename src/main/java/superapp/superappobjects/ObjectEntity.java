package superapp.superappobjects;

import java.util.Date;
import java.util.Map;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "ObjectTable")
public class ObjectEntity {
	@Id
	private String id;
	@Lob
	@Convert(converter = ConverterMapDetailsAndString.class)
	private Map<String, Object> details;
	private String alias;
	private Date creationTimestamp;
	private double latitude;
	private double longitude;
	private boolean active;
	private String createdBy;
	private String type;
	private String projectName;
	private String memberList;
	private Date eventDate;
	private double startTime;
	private double endTime;

	public ObjectEntity() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Map<String, Object> getDetails() {
		return details;
	}

	public void setDetails(Map<String, Object> details) {
		this.details = details;
	}

	public String getMemberList() {
		return memberList;
	}

	public void setMemberList(String memberList) {

		this.memberList = memberList;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreationTimestamp() {
		return creationTimestamp;
	}

	public void setCreationTimestamp(Date createdAt) {
		this.creationTimestamp = createdAt;
	}

	public boolean getActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public Date getEventDate() {
		return eventDate;
	}

	public void setEventDate(Date eventDate) {
		this.eventDate = eventDate;
	}

	public double getStartTime() {
		return startTime;
	}

	public void setStartTime(double startTime) {
		this.startTime = startTime;
	}

	public double getEndTime() {
		return endTime;
	}

	public void setEndTime(double endTime) {
		this.endTime = endTime;
	}

	@Override
	public String toString() {
		return "ObjectEntity [id=" + id + ", details=" + details + ", alias=" + alias + ", creationTimestamp="
				+ creationTimestamp + ", latitude=" + latitude + ", longitude=" + longitude + ", active=" + active
				+ ", createdBy=" + createdBy + ", type=" + type + ", projectName=" + projectName + ", memberList="
				+ memberList + ", eventDate=" + eventDate + ", startTime=" + startTime + ", endTime=" + endTime + "]";
	}

}
