package critterSimulator;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.collections.IndexedIterable;

/**
 * The Carnivore agent preys specifically on
 * the Herbivore agent.
 * 
 * Carnivores actively select the nearest
 * Herbivore agent and pursues and consumes
 * it. Once a Carnivore has begun to hunt
 * a specific Herbivore, it will not change
 * target until that Herbivore is dead.
 * 
 * Carnivores can live for 3650 ticks and
 * reproduce every 365 ticks. They are
 * capable of going without food for 30
 * ticks.
 * 
 * @author Eric Ostrowski, Doug MacDonald
 *
 */
public class Carnivore{

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private final int LifeSpan = 3650;
	private final int ReproductionPeriod = 365;
	private final int FullHunger = 30;
	private int hunger = FullHunger;
	private int age = 1;
	private Herbivore prey;
	
	public boolean isDead = false;
	
	public Carnivore(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		// get the grid location of this Carnivore
		GridPoint pt = grid.getLocation(this);
		
		Context context = ContextUtils.getContext(this);
		
		// Acquire new prey if ours is invalid
		if(prey == null || !context.contains(prey)) {
			
			IndexedIterable<Herbivore> herbivores = context.getObjects(Herbivore.class);
			
			double lstDst = 1000;
			
			for(Herbivore herbivore : herbivores) {
				
				//Find the closest prey
				GridPoint preyPt = grid.getLocation(herbivore);
				double dist = grid.getDistance(pt, preyPt);
				
				if(dist < lstDst) {
					lstDst = dist;
					prey = herbivore;
				}
			}
		}
		
		// Only hunt if hungry and have valid prey
		if(hunger < FullHunger) {
			if(prey != null) {
				if(!prey.isDead) {
					moveTowards(grid.getLocation(prey));
					attemptToEat(prey);
				}
			}
		}
		
		//Die of Hunger
		if(hunger == 0) {
			die();
		}
		
		//Die of Old Age
		if(age == LifeSpan) {
			die();
		}
		
		//Spawn a new Carnivore every Reproduction Period
		if(age % ReproductionPeriod == 0) {
			spawn();
		}
		
		//Get Hungrier and Older
		hunger--;
		age++;
		
	}
	
	//Attempt to eat selected Herbivore
	@SuppressWarnings("unchecked")
	private boolean attemptToEat(Herbivore herbivore) {
		//Find Carnivore and Herbivore Location
		GridPoint pt = grid.getLocation(this);
		GridPoint preyPt = grid.getLocation(herbivore);
		
		if(preyPt != null) {
			if(grid.getDistance(pt, preyPt) <= 1) {
				// Eat it
				Context<Object> context = ContextUtils.getContext(herbivore);
				context.remove(herbivore);
				hunger = FullHunger; // No longer hungry
				
				return true;
			}
		}
		
		return false;
	}

	//Remove Carnivore from Simulation
	@SuppressWarnings("unchecked")
	private void die() {
		// Remove Carnivore
		isDead = true;
		Context<Object> context = ContextUtils.getContext(this);
		context.remove(this);
	}

	//Add Carnivore to Simulation
	@SuppressWarnings("unchecked")
	private void spawn() {
		// Spawn a new Carnivore 
		Context<Object> context = ContextUtils.getContext(this);
		
		Carnivore c = new Carnivore(space,grid);		
		context.add(c);
		
		NdPoint pt = space.getLocation(this);
		grid.moveTo(c, (int)pt.getX(), (int)pt.getY());
	}

	//Move Carnivore
	public void moveTowards(GridPoint pt) {
		if(pt != null) {
			// only move if we are not already in this grid location
			if(!pt.equals(grid.getLocation(this))) {
				NdPoint myPoint = space.getLocation(this);
				NdPoint otherPoint = new NdPoint(pt.getX(), pt.getY());
				double angle = SpatialMath.calcAngleFor2DMovement(space,
						myPoint, otherPoint);
				space.moveByVector(this,  1,  angle, 0);
				myPoint = space.getLocation(this);
				grid.moveTo(this, (int)myPoint.getX(), (int)myPoint.getY());
			}
		}
	}
}
