package ee.ttu.idu0080.raamatupood.types;

import java.io.Serializable;

public class Toode implements Serializable {
	private static final long serialVersionUID = 1L;
	private int kood;
	private String nimetus;
	private double hind;
	
	public Toode(int kood, String nimetus, double hind) {
		this.kood = kood;
		this.nimetus = nimetus;
		this.hind = hind;
	}

	public Integer getKood() {
		return kood;
	}

	public void setKood(Integer kood) {
		this.kood = kood;
	}

	public String getNimetus() {
		return nimetus;
	}

	public void setNimetus(String nimetus) {
		this.nimetus = nimetus;
	}

	public double getHind() {
		return hind;
	}

	public void setHind(double hind) {
		this.hind = hind;
	}

	@Override
	public String toString() {
		return "Toode [kood=" + kood + ", nimetus=" + nimetus + ", hind=" + hind + "]";
	}
	
}