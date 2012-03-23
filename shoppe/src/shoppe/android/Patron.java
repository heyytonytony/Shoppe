package shoppe.android;

public class Patron extends GridElement {
	int wealth;
	int tenacity;

	public Patron() {
		// TODO Auto-generated constructor stub
	}

	public Patron(int xpos, int ypos, int elementType, int wealth) {
		super(xpos, ypos, elementType);
		this.wealth = wealth;
	}

}
