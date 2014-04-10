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
	private int LifeSpan = 4000;
	private int Age = 1;
	
	public Herbivore(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		// get the grid location of this herbivore
		GridPoint pt = grid.getLocation(this);
		boolean PlantHere = false;
		
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
			if(cell.getPoint() == pt)
			{
				if(cell.size() > 0)
				{
					PlantHere = true;
					eat(this, cell.items());
				}
			}
		}
				
		if(!PlantHere)
		{
			moveTowards(pointWithMostPlants);
		}
		
		LifeSpan--;
		Age++;
		

		if(Age % 20 == 0)
		{
			Spawn(this);
		}
		if(LifeSpan <= 0)
		{
			die(this);
		}
	}
	
	private void eat(Herbivore herbivore, Iterable<Plant> iterable) {
		// Eat Herbivore and Increase Life Span
		LifeSpan += 10;
				
		Context<Object> context = ContextUtils.getContext(herbivore);
		context.remove(iterable.iterator().next());
	}

	private void die(Herbivore me) {
		// Remove Herbivore
		Context<Object> context = ContextUtils.getContext(me);
		context.remove(me);
	}

	private void Spawn(Herbivore me) {
		// Spawn a new Herbivore 
		Context<Object> context = ContextUtils.getContext(me);
		
		Herbivore c = new Herbivore(space,grid);		
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
