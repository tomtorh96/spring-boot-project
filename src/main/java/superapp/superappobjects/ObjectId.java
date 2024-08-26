package superapp.superappobjects;

public class ObjectId {
	private String superapp;
	private String id;
	
	public ObjectId() {
		
	}
	
	public ObjectId(String superApp,String id) {
		this.id = id;
		this.superapp = superApp;
	}
	public String getSuperapp() {
		return superapp;
	}
	public void setSuperapp(String superApp) {
		this.superapp = superApp;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Override
	public String toString() {
		return "ObjectID [superApp=" + superapp + ", id=" + id + "]";
	}
	
}
