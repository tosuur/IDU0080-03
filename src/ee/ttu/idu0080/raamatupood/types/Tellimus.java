package ee.ttu.idu0080.raamatupood.types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Tellimus implements Serializable {
	private static final long serialVersionUID = 1L;
	public List<TellimuseRida> tellimuseRead;
	
	public Tellimus() {
		tellimuseRead = new ArrayList<TellimuseRida>();
	}
	
	public void addTellimus(TellimuseRida tellimusRida) {
		tellimuseRead.add(tellimusRida);
	}
	
	public List<TellimuseRida> getTellimusRead() {
		return tellimuseRead;
	}

	@Override
	public String toString() {
		return "Tellimus [tellimuseRead=" + tellimuseRead + "]";
	}
	
}
