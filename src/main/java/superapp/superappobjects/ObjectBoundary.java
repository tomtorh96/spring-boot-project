package superapp.superappobjects;
import java.util.Date;
import java.util.Map;

public class ObjectBoundary {
	private ObjectId objectId;
	private String type;
	private String alias;
	private Location location;
	private Boolean active;
	private Date creationTimestamp;
	private CreatedBy createdBy;
	private Map<String,Object> objectDetails;
	
	public ObjectBoundary() {}


	public ObjectId getObjectId() {
		return objectId;
	}

	public void setObjectId(ObjectId objectId) {
		this.objectId = objectId;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public Location getLocation() {
		return location;
	}


	public void setLocation(Location location) {
		this.location = location;
	}


	public CreatedBy getCreatedBy() {
		return createdBy;
	}


	public void setCreatedBy(CreatedBy createdBy) {
		this.createdBy = createdBy;
	}


	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}
	

	public Date getCreationTimestamp() {
		return creationTimestamp;
	}


	public void setCreationTimestamp(Date creationTimestamp) {
		this.creationTimestamp = creationTimestamp;
	}


	public Map<String, Object> getObjectDetails() {
		return objectDetails;
	}


	public void setObjectDetails(Map<String, Object> objectDetails) {
		this.objectDetails = objectDetails;
	}


	@Override
	public String toString() {
		return "ObjectBoundary [objectId=" + objectId + ", alias=" + alias + ", type=" + type + ", location=" + location
				+ ", active=" + active + ", creationTimestamp=" + creationTimestamp + ", createdBy=" + createdBy + ", objectDetails="
				+ objectDetails + "]";
	}




}






	


	

