package de.dfki.km.text20.browserplugin.services.emotiondetector.impl.brain.rmserror;

import java.util.ArrayList;

import de.dfki.km.text20.browserplugin.services.emotiondetector.EmotionClassifier;
import de.dfki.km.text20.services.trackingdevices.brain.BrainTrackingEvent;

public class TrainedAvgEmotionClassifier implements EmotionClassifier {
	 /** Contains the brain tracking events to be evaluated for the next emotion */
    private ArrayList<BrainTrackingEvent> events = new ArrayList<BrainTrackingEvent>();
    
	/** Training data */
	public TrainingModel model;
	
	/**
	 * Constructor
	 */
	public TrainedAvgEmotionClassifier(){
		model = new TrainingModel();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String getEmotion() {
		
		// sum of channels to be used
		double sumSmile = 0, sumFurrow = 0, sumEngagement = 0;
		
		// count of events taken into consideration for each channel (e.g. when smile is zero it is not included for its calculations)
		double cntSmile = 0, cntFurrow = 0, cntEngagement = 0;
		
		// average of each channel
		double avgSmile, avgFurrow, avgEngagement;
		
		// Root Mean Squared Error for each emotion
		//double rmseHappy, rmseInterested, rmseDoubt, rmseBored;
		
		// goes through all events and calculates sum and count
		ArrayList<BrainTrackingEvent> currEvents;
		
		synchronized(events){
			currEvents = (ArrayList<BrainTrackingEvent>)events.clone();
			events.clear();
		}
		
		if(currEvents.isEmpty())
			return null;
		
		for(BrainTrackingEvent e : currEvents){
			
			double value = e.getValue("channel:smile");
			sumSmile += value;
			cntSmile = (value == 0? cntSmile : cntSmile+1);
			
			value = e.getValue("channel:furrow");
			sumFurrow += value;
			cntFurrow = (value == 0? cntFurrow : cntFurrow+1);
			
			value = e.getValue("channel:engagement");
			sumEngagement += value;		
			cntEngagement++;
		}
		
		// calculate error		
		if(cntSmile != 0){
			avgSmile = sumSmile / cntSmile;
			//rmseHappy = Math.abs(avgSmile - model.avgSimple.get("happy").doubleValue());
		} else{
			avgSmile = 0;
			//rmseHappy = Double.MAX_VALUE;			
		}
		
		if(cntFurrow != 0){
			avgFurrow = sumFurrow / cntFurrow;			
			//rmseDoubt = Math.abs(avgFurrow - model.avgSimple.get("doubt").doubleValue());
		} else{
			avgFurrow = 0;
			//rmseDoubt = Double.MAX_VALUE;		
		}
		
		if(cntEngagement != 0){
			avgEngagement = sumEngagement / cntEngagement;
			//rmseInterested = Math.abs(avgEngagement - model.avgSimple.get("interested"));
			//rmseBored = Math.abs(avgEngagement - model.avgSimple.get("bored").doubleValue());
		} else{
			avgEngagement = 0;
			//rmseInterested = Double.MAX_VALUE;	
			//rmseBored = Double.MAX_VALUE;
		}	
		
		/*// Return the emotion with smallest Root Mean Squared Error
		if(rmseHappy < rmseInterested && rmseHappy < rmseDoubt && rmseHappy < rmseBored)
			return "happy";
		if(rmseInterested < rmseHappy && rmseInterested < rmseDoubt && rmseInterested < rmseBored)
			return "interested";
		if(rmseDoubt < rmseHappy && rmseDoubt < rmseInterested && rmseDoubt < rmseBored)
			return "doubt";
		if(rmseBored < rmseHappy && rmseBored < rmseInterested && rmseBored < rmseDoubt)
			return "bored";		*/
		
		//if(model.avgSimple.isEmpty()){
			
		double thresholdBored = model.avgSimple.containsKey("bored")? model.avgSimple.get("bored").doubleValue(): 0.4;
		if(avgEngagement <= thresholdBored)
			return "bored";
		
		double thresholdDoubt = model.avgSimple.containsKey("doubt")? model.avgSimple.get("doubt").doubleValue(): 0.2;
		if(avgFurrow >= thresholdDoubt)
			return "doubt";
		
		double thresholdInterested = model.avgSimple.containsKey("interested")? model.avgSimple.get("interested").doubleValue(): 0.7;
		if(avgEngagement >= thresholdInterested)
			return "interested";

		double thresholdHappy = model.avgSimple.containsKey("happy")? model.avgSimple.get("happy").doubleValue(): 0.8;
		if(avgSmile >= thresholdHappy)
			return "happy";
			
		return "neutral";
			
		/*} else {
			if(avgEngagement <= model.avgSimple.get("bored").doubleValue())
				return "bored";
			if(avgFurrow >= model.avgSimple.get("doubt").doubleValue())
				return "doubt";
			if(avgEngagement >= model.avgSimple.get("interested").doubleValue())
				return "interested";
			if(avgSmile >= model.avgSimple.get("happy").doubleValue())
				return "happy";
				
			return "neutral";			
		}		*/
		
	}
	
	public String getEmotionRMSE(){
		// 0-furrow, 1-smile, 2-engagement
		double [] values = new double[3];
		int cnt = 0;
		synchronized (events) {
			cnt = events.size();
			for(BrainTrackingEvent b : events){
				values[0] += b.getValue("channel:furrow");
				values[1] += b.getValue("channel:smile");
				values[2] += b.getValue("channel:engagement");
			}
			events.clear();
		}
		
		// calculate average
		for(double d : values)
			d = d / cnt;		
	
		double rmseHappy = getRMSE(values, "happy"), rmseInterested = getRMSE(values, "interested"), rmseDoubt = getRMSE(values, "doubt"), rmseBored = getRMSE(values, "bored");
		
		// return emotion with minimum Root Mean Squared Error
		if(rmseHappy < rmseInterested && rmseHappy < rmseDoubt && rmseHappy < rmseBored)
			return "happy";
		if(rmseInterested < rmseHappy && rmseInterested < rmseDoubt && rmseInterested < rmseBored)
			return "interested";
		if(rmseDoubt < rmseHappy && rmseDoubt < rmseInterested && rmseDoubt < rmseBored)
			return "happy";
		if(rmseBored < rmseHappy && rmseBored < rmseInterested && rmseBored < rmseDoubt)
			return "happy";		
		
		return null;		
	}
	
	/**
	 * Returns the Root Mean Squared Error of the current values and the emotion class
	 * 
	 * @param values the calculated average of the events
	 * @param emotion the emotion to be compared to
	 * @return the Root Mean Square Error of the values and the given emotion
	 */
	public double getRMSE(double [] values, String emotion){
		double [] avg = model.avgComplex.get(emotion);
		double squareSum = 0;
		for(int i = 0; i< avg.length; i++)
			squareSum += Math.pow(values[i] - avg [i], 2);
		return Math.pow(squareSum / avg.length, 0.5);
	}
	
	
	/**
     * Adds an event to the list
     * 
     * @param event the new event to be added to the list
     */
    public void addEvent(BrainTrackingEvent event){
    	synchronized (events) {
    		events.add(event);
		}
    }
    
    /** Clears event list */
    public void clearEvents(){
    	synchronized (events) {
    		events.clear();
		}
    }
    
    
  }