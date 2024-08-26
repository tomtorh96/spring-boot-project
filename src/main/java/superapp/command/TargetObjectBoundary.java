package superapp.command;

import superapp.superappobjects.ObjectId;

public class TargetObjectBoundary {
	private ObjectId objectId;

	public TargetObjectBoundary() {

	}

	public TargetObjectBoundary(String superApp, String id) {
		this.setObjectId(new ObjectId(superApp, id));

	}

	public ObjectId getObjectId() {
		return objectId;
	}

	public void setObjectId(ObjectId objectId) {
		this.objectId = objectId;
	}

	@Override
	public String toString() {
		return "TargetObjectBoundary [objectId=" + objectId + "]";
	}

}
