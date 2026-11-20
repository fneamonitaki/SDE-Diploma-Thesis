package lib.ReqSketchLib.relativeSketchPackage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lib.ReqSketchLib.HelperClasses.CDFItem;
import lib.ReqSketchLib.HelperClasses.Constants;
import lib.ReqSketchLib.HelperClasses.ItemWeight;
import lib.ReqSketchLib.HelperClasses.RankItem;

public class RelativeErrorSketch implements Serializable{

	private static final long serialVersionUID = 1L;
    public List<RelativeCompactor> compactors = new ArrayList<>();
    int k;
    public int H = 0;
    public int size = 0;
	public int N = 0; //size of the input summarized
	public int maxNomSize;


	public RelativeErrorSketch() {
		this.compactors = new ArrayList<>();
	}

    public RelativeErrorSketch(int k){
    	this.k = k;
        grow();
    }
   
    public void grow() {
    	compactors.add(new RelativeCompactor(this.k, H));
    	H = compactors.size();
    	updateMaxNomSize();
    }
    
    //computes a new bound for determining when to compress the sketch
    public void updateMaxNomSize() {
        maxNomSize = 0;

        for(RelativeCompactor c : compactors){
            maxNomSize += c.nomCapacity();
        }
    }
    
    public void update(double item) {
    	compactors.get(0).add(item);
    	size++;
    	N++;
    	
    	if (size >= maxNomSize) {
    		compress();
    	}
    }
    
    public void compress() {
    	updateMaxNomSize();
        if (size < maxNomSize) {
        	return;
        }
           
        for (int h =0; h<compactors.size(); h++) {
        	if (compactors.get(h).size() >= compactors.get(h).nomCapacity()) {
        		if (h+1 >= H) {
        			grow();
        		}
        		List<Double> promoted = compactors.get(h).compact();
        		compactors.get(h+1).addAll(promoted);
        	}
        }
        
        size=0; 
        for(RelativeCompactor c : compactors){
            size += c.size();
        } 
    }


	public void mergeIntoSelf(RelativeErrorSketch otherS) {

		while(this.H < otherS.H) {
			grow();
		}

		for (int h=0; h<otherS.H; h++) {
			this.compactors.get(h).mergeIntoSelf(otherS.compactors.get(h));
		}

		this.N += otherS.N;

		size = 0;

		for(RelativeCompactor c : compactors){
			size += c.size();
		}

		if(size >= maxNomSize){
			compress();
		}
	}

    public static RelativeErrorSketch merge(RelativeErrorSketch one, RelativeErrorSketch two) {
    	if(one.size >= two.size) {
    		one.mergeIntoSelf(two);
    		return one;
    	}else {
    		two.mergeIntoSelf(one);
    		return two;
    	}
		
    }

    public int rank(double value) {
    	int result =0;
    	for (int h=0; h<compactors.size(); h++) {
    		RelativeCompactor c = compactors.get(h);
    		result += c.rank(value) * (1<<h);
    	    int count = c.rank(value);
    	    int weight = 1 << h;

    	}
    	return result;
    }

    public List<CDFItem> cdf(){
    	List<ItemWeight> itemsAndWeigths = new ArrayList<>();
    	for (int h=0; h<compactors.size(); h++) {
    		List<Double> items = compactors.get(h).getValues();
    		
    		for(double item : items) {
    			//bitwise left shift
    			itemsAndWeigths.add(new ItemWeight(item, 1<<h));
    		}
    	}
        int totWeight=0;
        for(ItemWeight iw : itemsAndWeigths){
        	totWeight += iw.getWeight();
        } 
        itemsAndWeigths.sort((a,b)-> Double.compare(a.getItem(), b.getItem()));
    	int cumWeight=0;
    	List<CDFItem> cdf = new ArrayList<>();
    	
    	for (ItemWeight iw : itemsAndWeigths) {
    		cumWeight += iw.getWeight();
    		cdf.add(new CDFItem(iw.getItem(), ((double)(cumWeight))/((double)(totWeight))));
    	}
    	return cdf;
    }

    public List<RankItem> ranks() {
    	List<RankItem> ranksList = new ArrayList<>();
    	List<ItemWeight> itemsAndWeigths = new ArrayList<>();
    	
    	for (int h=0; h<compactors.size(); h++) {
    		List<Double> items = compactors.get(h).getValues();
    		
    		for(double item : items) {
    			//bitwise left shift
    			itemsAndWeigths.add(new ItemWeight(item, 1<<h));
    		}
    	}
    	
    	itemsAndWeigths.sort((a,b)-> Double.compare(a.getItem(), b.getItem())); 
    	int cumWeight = 0;
    	
    	for (ItemWeight iw : itemsAndWeigths) {
    		cumWeight += iw.getWeight();
    		ranksList.add(new RankItem(iw.getItem(), cumWeight));
    	}
		return ranksList;
    
    }
    

    public double quantile(double q) {
    	//q must be between [0, 1]

    	double desiredRank = q*this.N;
    	List<RankItem> ranks=ranks();
    	
    	int i =0;
    	int j = ranks.size();
    	
    	while(i<j) {
    		int m;
    		m=Math.floorDiv(i+j, 2); 
    		//(item, rank) = ranks[m]
    		RankItem itemrank = ranks.get(m);
    		if(desiredRank > itemrank.getRank()) {
    			i=m+1;
    		}
    		else {
    			j=m;
    		}
    	}
    	RankItem itemrank2= ranks.get(i);
    	
    	
    	return itemrank2.getItem();
    }
    
    public int getRankUpperBound(double value, double numStdDev) {
    	int rank = rank(value);
    	if(rank <= this.k * 3) {
    		return rank;
    	}
    	else {
    		return (int) Math.ceil((1 + numStdDev * getMaximumRSE(k)) * rank);
    	}
    	
    }
    
    public int getRankLowerBound(double value, double numStdDev) {
    	int rank = rank(value);
    	if(rank <= this.k * 3) {
    		return rank;
    	}
    	else {
    		return (int) Math.floor((1 - numStdDev * getMaximumRSE(k)) * rank);
    	}
    	
    }
    
    public static double getMaximumRSE(int k) {
        return Math.sqrt(8.0 / 3) / k;
    }
}

