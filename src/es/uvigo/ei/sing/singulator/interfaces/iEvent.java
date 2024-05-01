package es.uvigo.ei.sing.singulator.interfaces;

public interface iEvent {
	public int act(iMolecule... agents);

	public boolean checkInputs(iMolecule... agents);
}
