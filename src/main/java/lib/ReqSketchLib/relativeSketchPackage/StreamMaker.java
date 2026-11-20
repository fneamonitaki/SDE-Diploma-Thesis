package lib.ReqSketchLib.relativeSketchPackage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

//Written by Zografoula Ioanna Neamonitaki
//Based on repo https://github.com/edoliberty/streaming-quantiles/tree/master
public class StreamMaker {

	//this is an enum for the diffrent types of generating data
    public enum Order {
        SORTED, REVERSED, ZOOMIN, ZOOMOUT, SQRT,
        RANDOM, ADV, CLUSTERED, CLUSTERED_ZOOMIN
    }
    
    /*
     * number of items = n 
     * order enum
     * p = length of cluster
     * 
     */
    public static List<Double> make(int n, Order order, int p, int g, int s){
		List<Double> result = new ArrayList<Double>();
    	
    	switch(order) {
		case SORTED:
			for (int i=0; i<n; i++) {
				result.add((double) i);
			}
			break;
		case REVERSED:
			for (int i=n-1; i>=0; i--) {
				result.add((double) i);
			}
			break;
		case ZOOMIN:
			for (int i=0; i<n/2; i++) {
				result.add((double) i);
				result.add((double) n-1);
			}
			break;
		case ZOOMOUT:
			for (int i=1; i<n/2; i++) {
				result.add((double) (n/2 + i));
				result.add((double) (n/2 - i));
			}
			break;
		case SQRT:
			int t = (int) Math.sqrt(2*n);
			int initialItem =0;
			int initialSkip = 1;
			
			for (int i=0; i<t; i++) {
				int item=initialItem;
				int skip = initialSkip;
				
				for(int j=0; j<t-i; j++) {
					result.add( (double) item);
					item+=skip;
					skip++;
				}
				initialSkip++;
				initialItem+=initialSkip;
			}
			break;
        case RANDOM:
            List<Integer> items = IntStream.range(0, n).boxed().collect(Collectors.toList());
            Collections.shuffle(items);

            for (int v : items)
                result.add((double) v);

            break;
		 default:
			 throw new IllegalArgumentException("Order not implemented yet");
    	}

    	return result;
    	
    }
    
    
    public static void main(String[] args) {

        int n = 1000;
        int p = 1000;
        int g = 0;
        int s = 1;
        Order order = Order.SORTED;

        List<Double> stream = make(n, order, p, g, s);

        for (double value : stream) {
            System.out.println(value);
        }
    }
}
