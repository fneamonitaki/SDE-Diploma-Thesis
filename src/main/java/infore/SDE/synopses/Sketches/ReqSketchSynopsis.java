package infore.SDE.synopses.Sketches;
import com.sun.org.apache.xalan.internal.xsltc.dom.KeyIndex;
import infore.SDE.synopses.Synopsis;
import lib.ReqSketchLib.relativeSketchPackage.RelativeErrorSketch;
import com.fasterxml.jackson.databind.JsonNode;
import infore.SDE.messages.Estimation;
import infore.SDE.messages.Request;

public class ReqSketchSynopsis extends Synopsis {

    private RelativeErrorSketch reqsketch;
    int k;
    public ReqSketchSynopsis(int uid, String[] parameters) {
        super(uid,parameters[0],parameters[1], parameters[2]);
        this.k = Integer.parseInt(parameters[3]);
        this.reqsketch = new RelativeErrorSketch(k);
    }

    @Override
    public void add(Object k) {
        JsonNode node = (JsonNode)k;
        double value = node.get(this.keyIndex).asDouble();
        reqsketch.update(value);
    }

    @Override
    public Object estimate(Object o) {
        double value = ((Number) o).doubleValue();
        return Integer.toString(reqsketch.rank(value));
    }

    @Override
    public Estimation estimate(Request rq) {

        String[] params = rq.getParam();

        //if noOfP > 1 then send partial sketch to ReduceFlatMap for merging
        if (rq.getNoOfP() > 1) {

            String[] reduceParams = {params[0], params[1], params[2]};
            rq.setParam(reduceParams);
            return new Estimation(rq, reqsketch, String.valueOf(rq.getUID()));
        }

        //params[0] = value or q
        //params[1] = query type: "0" = rank, "1" = quantile
        int queryType = Integer.parseInt(params[2]);
        String result;
        switch (queryType) {
            case 0: {
                double value = Double.parseDouble(params[1]);
                int rank = reqsketch.rank(value);
                result = Integer.toString(rank);
                break;
            }
            case 1: {
                double q = Double.parseDouble(params[1]);
                double quantile = reqsketch.quantile(q);
                result = Double.toString(quantile);
                break;
            }
            default:
                result = "Unsupported queryType: " + queryType;
        }
        return new Estimation(rq, result, Integer.toString(rq.getUID()));
    }

    @Override
    public Synopsis merge(Synopsis sk) {
        return sk;
    }

    @Override
    public String toString() {
        return "REQSketch{uid=" + SynopsisID
                + ", k=" + k
                + ", size=" + reqsketch.size
                + ", levels=" + reqsketch.H
                + "}";
    }
}

