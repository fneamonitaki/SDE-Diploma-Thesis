package infore.SDE.reduceFunctions;
import infore.SDE.messages.Estimation;
import lib.ReqSketchLib.relativeSketchPackage.RelativeErrorSketch;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ReqSketchReduce extends ReduceFunction  {

    private List<RelativeErrorSketch> partialSketches = new ArrayList<>();
    private String mergeOp;
    private int queryType;
    private String queryParam;

    public ReqSketchReduce(int nOfP, int count, String[] parameters, int syn, int rq){
        super(nOfP, count, parameters, syn, rq);
        this.mergeOp = parameters.length > 0 ? parameters[0] : "random";
        this.queryParam    = parameters.length > 1 ? parameters[1] : "50.0";
        this.queryType     = parameters.length > 2 ? Integer.parseInt(parameters[2]) : 0;

    }

    @Override
    public boolean add(Estimation e) {

        Object obj = e.getEstimation();

        if (obj instanceof RelativeErrorSketch) {
            RelativeErrorSketch s = (RelativeErrorSketch) obj;
            partialSketches.add(s);
        } else {
            System.out.println( "[REQSketchReduce] WARNING expected RelativeErrorSketch but got "+ (obj == null ? "null" : obj.getClass().getSimpleName()));
        }
        count++;
        if (count == nOfP) {
            return true;
        }
        return false;
    }

    @Override
    public Object reduce() {
        if (partialSketches.isEmpty()) {
            System.out.println("[REQSketchReduce] No sketches to merge!");
            return null;
        }

        for (int i = 0; i < partialSketches.size(); i++) {
            RelativeErrorSketch s = partialSketches.get(i);
        }

        //merge all partial sketches into one final sketch
        RelativeErrorSketch finalSketch;
        if ("random".equalsIgnoreCase(mergeOp)) {
            finalSketch = randomMerge(partialSketches);
        } else {
            finalSketch = binaryMerge(partialSketches);
        }

        //now query the merged sketch
        String result;
        switch (queryType) {
            case 0: {
                //rank query
                double value = Double.parseDouble(queryParam);
                int rank = finalSketch.rank(value);
                result = Integer.toString(rank);
                break;
            }
            case 1: {
                //quantile query
                double q = Double.parseDouble(queryParam);
                double quantile = finalSketch.quantile(q);
                result = Double.toString(quantile);
                break;
            }
            default:
                result = "Unsupported queryType: " + queryType;
        }

        return result;
    }

    //Binary merge
    private RelativeErrorSketch binaryMerge(List<RelativeErrorSketch> sketches) {
        while (sketches.size() > 1) {
            List<RelativeErrorSketch> newList = new ArrayList<>();
            for (int i = 0; i < sketches.size() - 1; i += 2) {
                RelativeErrorSketch merged = RelativeErrorSketch.merge(sketches.get(i), sketches.get(i + 1));
                newList.add(merged);
            }
            if (sketches.size() % 2 == 1) {
                newList.add(sketches.get(sketches.size() - 1));
            }
            sketches = newList;
        }
        return sketches.get(0);
    }

    //Random merge
    private RelativeErrorSketch randomMerge(List<RelativeErrorSketch> sketches) {
        Random rand = new Random();
        while (sketches.size() > 1) {
            int i = rand.nextInt(sketches.size());
            int j = i;
            while (j == i) j = rand.nextInt(sketches.size());
            RelativeErrorSketch merged = RelativeErrorSketch.merge(sketches.get(i), sketches.get(j));
            sketches.remove(Math.max(i, j));
            sketches.remove(Math.min(i, j));
            sketches.add(merged);
        }
        return sketches.get(0);
    }
}

