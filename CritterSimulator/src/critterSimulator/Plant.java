package critterSimulator;

import java.util.Random;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;

/**
 * The Plant agent serves as the foundation
 * food source for the other agents.
 * Plants neither more nor eat, but do
 * reproduce every 100 ticks and die after
 * 365 ticks.
 * 
 * @author Eric Ostrowski, Doug MacDonald
 *
 */
public class Plant {

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private int age = 1;
	
	public boolean isDead = false;
	
	public Plant(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		//Grow
		if(age % 100 == 0) {
			spawn();
		}
		
		//Die of old age
		if(age == 365) {
			die();
		}
		
		//Get older
		age++;
	}
	
	//Remove Plant from Simulation
	@SuppressWarnings("unchecked")
	private void die() {
		isDead = true;
		Context<Object> context = ContextUtils.getContext(this);
		context.remove(this);
	}
	
	//Add Plant to Simulation
	@SuppressWarnings("unchecked")
	private void spawn() {
		// Spawn a new plant 
		Context<Object> context = ContextUtils.getContext(this);
		
		Plant c = new Plant(space, grid);		
		context.add(c);
		
		Random rand = new Random();
		NdPoint pt = space.getLocation(this);
		grid.moveTo(c, (int)pt.getX() + rand.nextInt(20), (int)pt.getY() + rand.nextInt(20));
	}
}
