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

/**
 * @author Eric Ostrowski, Doug MacDonald
 *
 */
public class Herbivore {

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private final int LifeSpan = 1825; // 5 Year life span
	private final int ReproductionPeriod = 182; // Reproduces bi-annually
	private final int FullHunger = 15;
	private int hunger = FullHunger;
	private int age = 1;
	
	public Herbivore(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		// get the grid location of this herbivore
		GridPoint pt = grid.getLocation(this);
		boolean ate = false;
		
		// use the GridCellNgh class to create GridCells for
		// the surrounding neighborhood
		GridCellNgh<Plant> nghCreator = new GridCellNgh<Plant>(grid, pt,
				Plant.class, 1, 1);
		List<GridCell<Plant>> gridCells = nghCreator.getNeighborhood(true);
		SimUtilities.shuffle(gridCells, RandomHelper.getUniform());
		
		GridPoint pointWithMostPlants = null;
		int maxCount = -1;
		for(GridCell<Plant> cell : gridCells) {
			if(cell.size() > maxCount) {
				pointWithMostPlants = cell.getPoint();
				maxCount = cell.size();
			}
			
			// Rather than having to be on the exact location, allow
			// the herbivore to eat from neighboring cells.
			if(hunger < FullHunger) { // Only eat when hungry
				ate = attemptToEat(cell);
			}
		}
		
		if(hunger == 0) {
			die(); // Starved to death
			return; // Must return after death to prevent further simulation of this agent
		}
		
		if(!ate) {
			moveTowards(pointWithMostPlants);
			hunger--;
		}

		if(age % ReproductionPeriod == 0) {
			spawn();
		}
		
		if(age == LifeSpan) {
			die();
			return;
		}
		
		age++;
	}
	
	private boolean attemptToEat(GridCell<Plant> cell) {
		if(cell.size() > 0) {
			// Eat first plant in cell
			Plant plant = cell.items().iterator().next();
			Context<Object> context = ContextUtils.getContext(plant);
			context.remove(plant);
			hunger = FullHunger; // No longer hungry
			
			return true;
		}
		
		return false;
	}

	private void die() {
		// Remove Herbivore
		Context<Object> context = ContextUtils.getContext(this);
		context.remove(this);
	}

	private void spawn() {
		// Spawn a new Herbivore 
		Context<Object> context = ContextUtils.getContext(this);
		
		Herbivore c = new Herbivore(space,grid);		
		context.add(c);
		
		NdPoint pt = space.getLocation(this);
		grid.moveTo(c, (int)pt.getX(), (int)pt.getY());
	}
	
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
