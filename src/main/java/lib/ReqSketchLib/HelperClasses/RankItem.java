package lib.ReqSketchLib.HelperClasses;
import java.io.Serializable;

public class RankItem implements Serializable {
    private double item;
    private int rank;

    public RankItem(double item, int rank) {
        this.setItem(item);
        this.setRank(rank);
    }

	public double getItem() {
		return item;
	}

	public void setItem(double item) {
		this.item = item;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}
}
