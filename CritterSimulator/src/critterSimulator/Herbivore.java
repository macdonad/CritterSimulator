/**
 * 
 */
package critterSimulator;



import java.util.List;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.SimUtilities;

/**
 * @author Eric Ostrowski, Doug MacDonald
 *
 */
public class Herbivore {

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	
	public Herbivore(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		// get the grid location of this herbivore
		GridPoint pt = grid.getLocation(this);
		
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
		}
		
		moveTowards(pointWithMostPlants);
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
