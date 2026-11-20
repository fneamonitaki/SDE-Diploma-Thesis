//this is the relative compactor class
//it compacts items and sends them to the above level

package lib.ReqSketchLib.relativeSketchPackage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import lib.ReqSketchLib.HelperClasses.Constants;

public class RelativeCompactor implements Serializable{
    private List<Double> values = new ArrayList<>();
	int numCompactions = 0;
	int state =0;
	int offset = 0;
	int sectionSize;
	double sectionSizeF;
	int numSections = 3;
	int height;
    private static final long serialVersionUID = 1L;
	
    Random rand = new Random();

    public RelativeCompactor(int sectionSize, int height) {
        this.sectionSize = sectionSize;
        this.sectionSizeF = sectionSize;
        this.height = height;
    }

    public int nomCapacity() {
    	return 2 * numSections * sectionSize;
    }
    
    public int rank(double value) {
   
        int count = 0;

        for(double v : values){
            if(v <= value)
                count++;
        }

        return count;
    	
    }
    
    //compact method but not random selection of 
    //the promoted indices
    public List<Double> compactNotRandom(){

    	int cap = nomCapacity();
    	int secsToCompact;
    	int s=0;

        //sort the items int the buffer
        Collections.sort(values);

        if (sectionSize < 4) {
        	s = Math.floorDiv(cap, 2);
        	secsToCompact = numSections;
        }
        else {
        	secsToCompact = trailingOnesBinary(state) + 1;
        	s = Math.floorDiv(cap, 2)  + (numSections - secsToCompact)*sectionSize;
        	ensureEnoughSections();
        }
        
        if ( (values.size() - s)%2 == 1) {
        	if (s>0) {
        		s--;
        	}
        	else {
        		s++;
        	}
        }
        
        //not random!!
        if (numCompactions % 2 == 1) {
            offset = 1 - offset;
        } else {
            offset = 0;
        }
     
        List<Double> promoted = new ArrayList<>();

        for(int i = s+offset; i < values.size(); i += 2){
            promoted.add(values.get(i));
        }

        List<Double> remaining = new ArrayList<>(values.subList(0, s));
        
        values.clear();
        values.addAll(remaining);

        numCompactions++;
        state++;

        return promoted;
        
    }
    
    
    public List<Double> compact(){

    	int cap = nomCapacity();
    	int secsToCompact;
    	int s=0;

        //sort the items int the buffer
        Collections.sort(values);

        if (sectionSize < 4) {
        	s = Math.floorDiv(cap, 2);
        	secsToCompact = numSections;
        }
        else {
        	secsToCompact = trailingOnesBinary(state) + 1;
        	s = Math.floorDiv(cap, 2)  + (numSections - secsToCompact)*sectionSize;
        	ensureEnoughSections();
        }

        if ( (values.size() - s)%2 == 1) {
        	if (s>0) {
        		s--;
        	}
        	else {
        		s++;
        	}
        }
        
        //random selection of the offset
        if(this.numCompactions%2 ==1) {
        	this.offset = 1 - this.offset;
        }
        else {
        	this.offset = rand.nextInt(2);
        }
        
        List<Double> promoted = new ArrayList<>();

        for(int i = s+offset; i < values.size(); i += 2){
            promoted.add(values.get(i));
        }
        
        List<Double> remaining = new ArrayList<>(values.subList(0, s));

        values.clear();
        values.addAll(remaining);

        numCompactions++;
        state++;

        return promoted;
            
    }
    
    public void ensureEnoughSections() {
    	if(numCompactions >= Math.pow(2, numSections -1)) {
    		numSections *= 2;
    		sectionSizeF = sectionSizeF / Math.sqrt(2);
    		sectionSize = (int) sectionSizeF;
    	}
    }
    public List<Double> getValues() {
        return values;
    }
	public void mergeIntoSelf(RelativeCompactor otherRelCompactor) {
		//bitwise OR to merge states of the deterministic compaction schedule
		this.state = this.state | otherRelCompactor.state;
		this.numCompactions += otherRelCompactor.numCompactions;
		ensureEnoughSections();
        this.values.addAll(otherRelCompactor.getValues());
	}
    
    public static int trailingOnesBinary(int state) {
    	//convert the state into a binary string
    	String s = Integer.toBinaryString(state);
    	//replace the last "1" with a space
        String trimmed = s.replaceAll("1+$", "");
        //calculate the difference between the two strings
        return s.length() - trimmed.length();
    }

    public void add(Double x) {
        values.add(x);
    }

    public void addAll(List<Double> xs) {
        values.addAll(xs);
    }

    public Double get(int i) {
        return values.get(i);
    }

    public int size() {
        return values.size();
    }

    public void clear() {
        values.clear();
    }

    public List<Double> subList(int from, int to) {
        return values.subList(from, to);
    }
}
