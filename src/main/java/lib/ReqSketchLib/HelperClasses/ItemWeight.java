package lib.ReqSketchLib.HelperClasses;
import java.io.Serializable;

//this class is for ranks instead of the tuple.
public class ItemWeight implements Serializable {
	//class instead of the tuple
    private double item;
    private int weight;

    public ItemWeight(double item, int weight) {
        this.setItem(item);
        this.setWeight(weight);
    }

	public double getItem() {
		return item;
	}

	public void setItem(double item) {
		this.item = item;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}
}
