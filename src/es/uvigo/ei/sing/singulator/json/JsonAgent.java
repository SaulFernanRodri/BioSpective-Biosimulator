package es.uvigo.ei.sing.singulator.json;

import java.io.Serializable;

public class JsonAgent implements Serializable {

	private static final long serialVersionUID = 1L;

	private JsonMolecule[] molecules;
	private JsonRibosome[] ribosomes;

	public JsonAgent() {

	}

	public JsonMolecule[] getMolecules() {
		return molecules;
	}

	public void setMolecules(JsonMolecule[] molecules) {
		this.molecules = molecules;
	}

	public JsonRibosome[] getRibosomes() {
		return ribosomes;
	}

	public void setRibosomes(JsonRibosome[] ribosomes) {
		this.ribosomes = ribosomes;
	}
}
