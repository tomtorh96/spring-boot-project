package superapp.command;

public class CommandIdBoundary {
	private String superapp;
	private String miniapp;
	private String id;

	public CommandIdBoundary() {

	}

	public CommandIdBoundary(String superApp,String miniApp,String id) {
		this.setSuperapp(superApp);
		this.setMiniapp(miniApp);
		this.setId(id);

	}

	public String getSuperapp() {
		return superapp;
	}

	public void setSuperapp(String superApp) {
		this.superapp = superApp;
	}

	public String getMiniapp() {
		return miniapp;
	}

	public void setMiniapp(String miniApp) {
		this.miniapp = miniApp;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "CommandIdBoundary [superApp=" + superapp + ", miniApp=" + miniapp + ", id=" + id + "]";
	}

}
