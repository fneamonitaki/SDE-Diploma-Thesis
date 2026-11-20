package lib.ReqSketchLib.HelperClasses;

import java.io.Serializable;

public class CDFItem implements Serializable {
    double item;
    double probability;

    public CDFItem(double item, double probability) {
        this.item = item;
        this.probability = probability;
    }
}
