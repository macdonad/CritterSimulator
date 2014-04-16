/**
 * 
 */
package critterSimulator;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.collections.IndexedIterable;

/**
 * @author Eric Ostrowski, Doug MacDonald
 *
 */
public class Carnivore{

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private final int LifeSpan = 3650; // 10 Year life span
	private final int ReproductionPeriod = 365; // Reproduces annually
	private final int FullHunger = 20;
	private int hunger = FullHunger;
	private int age = 1;
	private Herbivore prey;
	
	public Carnivore(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
		
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		// get the grid location of this Carnivore
		GridPoint pt = grid.getLocation(this);
		boolean ate = false;
		
		Context context = ContextUtils.getContext(this);
		
		// Acquire new prey if ours is invalid
		if(prey != null || !context.contains(prey)) {
			
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
					ate = attemptToEat(prey);
				}
			}
		}
		
		if(hunger == 0) {
			die();
		}
		
		if(age == LifeSpan) {
			die();
		}
		
		if(age % ReproductionPeriod == 0) {
			spawn();
		}
		
		hunger--;
		age++;
		
	}
	
	private boolean attemptToEat(Herbivore herbivore) {
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

	private void die() {
		// Remove Carnivore
		Context<Object> context = ContextUtils.getContext(this);
		context.remove(this);
	}

	private void spawn() {
		// Spawn a new Carnivore 
		Context<Object> context = ContextUtils.getContext(this);
		
		Carnivore c = new Carnivore(space,grid);		
		context.add(c);
		
		NdPoint pt = space.getLocation(this);
		grid.moveTo(c, (int)pt.getX(), (int)pt.getY());
	}

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
