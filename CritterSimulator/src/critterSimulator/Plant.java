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
public class Plant {

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private int Age = 1;
	
	public Plant(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		// get the grid location of this herbivore
		GridPoint pt = grid.getLocation(this);
		
		Age++;
		
		if(Age % 5 == 0)
		{
			Spawn(this);
		}		
	}
	private void Spawn(Plant me) {
		// Spawn a new Carnivore 
		Context<Object> context = ContextUtils.getContext(me);
		
		Plant c = new Plant(space,grid);		
		context.add(c);
		
		NdPoint pt = space.getLocation(me);
		grid.moveTo(c, (int)pt.getX(), (int)pt.getY());
	}
}
