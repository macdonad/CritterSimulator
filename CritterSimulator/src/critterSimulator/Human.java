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
public class Human{

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private int LifeSpan = 1000;
	private int Age = 1;
	
	public Human(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		// get the grid location of this Human
		GridPoint pt = grid.getLocation(this);
		boolean FoodHere = false;
		
		// use the GridCellNgh class to create GridCells for
		// the surrounding neighborhood
		GridCellNgh<Object> nghCreator = new GridCellNgh<Object>(grid, pt,
				Object.class, 1, 1);
		List<GridCell<Object>> gridCells = nghCreator.getNeighborhood(true);
		SimUtilities.shuffle(gridCells, RandomHelper.getUniform());
		
		GridPoint pointWithMostHerbivores = null;
		int maxCount = -1;
		for(GridCell<Object> cell : gridCells) {
			if(cell.size() > maxCount) {
				pointWithMostHerbivores = cell.getPoint();
				maxCount = cell.size();
			}
			if(cell.getPoint() == pt)
			{
				if(cell.size() > 0)
				{
					FoodHere = true;
					eat(this, cell.items());
				}
			}
		}
		
		//If Age is multiple of 100 Spawn a Child
		if(Age % 300 == 0)
		{
			//Spawn(this);
		}
		
		//Chase if not eating
		if(!FoodHere)
		{
			moveTowards(pointWithMostHerbivores);
		}
		
		
		//Decrement Life Span every step and Increment Age
		LifeSpan--;
		Age++;
		
		if(LifeSpan <= 0)
		{
			//die(this);
		}
	}
	
	private void eat(Human carnivore, Iterable<Object> iterable) {
		// Eat Herbivore and Increase Life Span
		LifeSpan += 10;
				
		Context<Object> context = ContextUtils.getContext(carnivore);
		context.remove(iterable.iterator().next());
	}

	private void die(Human me) {
		// Remove Human
		Context<Object> context = ContextUtils.getContext(me);
		context.remove(me);
	}

	private void Spawn(Human me) {
		// Spawn a new Human 
		Context<Object> context = ContextUtils.getContext(me);
		
		Human c = new Human(space,grid);		
		context.add(c);
		
		NdPoint pt = space.getLocation(me);
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
