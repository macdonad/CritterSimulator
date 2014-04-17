/**
 * 
 */
package critterSimulator;

import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;
import repast.simphony.util.collections.IndexedIterable;

/**
 * @author Eric Ostrowski, Doug MacDonald
 *
 */
public class Human{

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private final int LifeSpan = 3650; // 10 Year life span
	private final int ReproductionPeriod = 365; // Reproduces annually
	private final int FullHunger = 20;
	private int hunger = FullHunger;
	private int age = 1;
	private Object prey;
	
	public Human(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;		
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		// get the grid location of this Human
		GridPoint pt = grid.getLocation(this);
		boolean ate = false;
		
		Context context = ContextUtils.getContext(this);
		
		// Acquire new prey if ours is invalid
		if((prey != null || !context.contains(prey)) && !(prey instanceof Human)) {
			
			//Get List of All Agents that the Human Considers Prey
			IndexedIterable<Herbivore> herbivoreprey = context.getObjects(Herbivore.class);
			IndexedIterable<Plant> plantprey = context.getObjects(Plant.class);
			IndexedIterable<Carnivore> carnivoreprey = context.getObjects(Carnivore.class);
			
			//Set Maximum Hunting Distance from Current Space
			double lstDst = 200;
			
			//Will Look for something to eat, Closest Prey wins
			//If Tie Carnivore > Herbivore > Plant
			//Look for Herbivores to Eat
			for(Herbivore herbivore : herbivoreprey) {
				
				//Find the closest prey
				GridPoint preyPt = grid.getLocation(herbivore);
				double dist = grid.getDistance(pt, preyPt);
				
				if(dist <= lstDst) {
					lstDst = dist;
					prey = herbivore;
				}
			}
			
			//Look for Carnivores to Eat
			for(Carnivore carnivore : carnivoreprey) {
				
				//Find the closest prey
				GridPoint preyPt = grid.getLocation(carnivore);
				double dist = grid.getDistance(pt, preyPt);
				
				if(dist <= lstDst) {
					lstDst = dist;
					prey = carnivore;
				}
			}
			//Only eat a plant if nothing else is around
			if(prey == null)
			{
				//Look for Plants to Eat
				for(Plant plant : plantprey) {
					
					//Find the closest prey
					GridPoint preyPt = grid.getLocation(plant);
					double dist = grid.getDistance(pt, preyPt);
					
					if(dist <= lstDst) {
						lstDst = dist;
						prey = plant;
					}
				}				
			}
		}
		
		// Only hunt if hungry and have valid prey
		if(hunger < FullHunger) {
			if(prey != null) {
				//Check if Prey is a Plant
				if(prey instanceof Plant)
				{
					//Check if Prey is already dead
					if(!((Plant)prey).isDead) {
						//Move to prey and attempt to eat
						moveTowards(grid.getLocation(prey));
						ate = attemptToEat(prey);
					}
				}
				//Check if prey is a herbivore
				else if(prey instanceof Herbivore)
				{
					//Check if prey is dead
					if(!((Herbivore)prey).isDead) {
						//Move to prey and attempt to eat
						moveTowards(grid.getLocation(prey));
						ate = attemptToEat(prey);
					}
				}
				//Check if prey is a carnivore
				else if(prey instanceof Carnivore)
				{
					//Check if prey is dead
					if(!((Carnivore)prey).isDead) {
						//Move to prey and attempt to eat
						moveTowards(grid.getLocation(prey));
						ate = attemptToEat(prey);
					}
				}
			}
		}
		
		//If starving then die
		if(hunger == 0) {
			die();
		}
		
		//If reach life span then die
		if(age == LifeSpan) {
			die();
		}
		
		//Spawn a new Human every Reproduction Period
		if(age % ReproductionPeriod == 0) {
			spawn();
		}
		
		//Get Hungrier and Older
		hunger--;
		age++;
		
	}
	
	//Attempt to eat the selected prey
	private boolean attemptToEat(Object prey) {
		GridPoint pt = grid.getLocation(this);
		GridPoint preyPt = grid.getLocation(prey);
		
		if(preyPt != null) {
			if(grid.getDistance(pt, preyPt) <= 1) {
				// Eat it
				Context<Object> context = ContextUtils.getContext(prey);
				context.remove(prey);
				hunger = FullHunger; // No longer hungry
				
				return true;
			}
		}
		
		return false;
	}

	//Die, Remove Human from Simulation
	private void die() {
		// Remove Human
		Context<Object> context = ContextUtils.getContext(this);
		context.remove(this);
	}

	//Add a Human to the Simulation
	private void spawn() {
		// Spawn a new Human 
		Context<Object> context = ContextUtils.getContext(this);
		
		Human c = new Human(space,grid);		
		context.add(c);
		
		NdPoint pt = space.getLocation(this);
		grid.moveTo(c, (int)pt.getX(), (int)pt.getY());
	}

	//Move the Human
	public void moveTowards(GridPoint pt) {
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
