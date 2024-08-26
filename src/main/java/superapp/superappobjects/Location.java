package superapp.superappobjects;

public class Location {
	private double longitude;
	private double latitude;

	public Location() {
	}

	public Location(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;

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

	@Override
	public String toString() {
		return "Location [Latitude=" + latitude + ", Longitude=" + longitude + "]";
	}
}
