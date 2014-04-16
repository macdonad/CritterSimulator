package critterSimulator;

import java.util.Collection;

import com.ibm.media.content.application.mvr.Master;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunState;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;

public class CritterSimulatorBuilder implements ContextBuilder<Object> {

	private Collection context;
	@SuppressWarnings("rawtypes")
	@Override
	public Context build(Context<Object> context) {
		context.setId("CritterSimulator");
		
		ContinuousSpaceFactory spaceFactory =
				ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		
		ContinuousSpace<Object> space =
				spaceFactory.createContinuousSpace("space", context,
						new RandomCartesianAdder<Object>(),
						new repast.simphony.space.continuous.WrapAroundBorders(),
						100, 100);
		
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		
		Grid<Object> grid = gridFactory.createGrid("grid", context, 
				new GridBuilderParameters<Object>(
						new WrapAroundBorders(),
						new SimpleGridAdder<Object>(),
						true, 100, 100));
		
		int herbivoreCount = 100;
		for(int i = 0; i < herbivoreCount; i++) {
			context.add(new Herbivore(space, grid));
		}
		
		int carnivoreCount = 2;
		for(int i = 0; i < carnivoreCount; i++) {
			context.add(new Carnivore(space, grid));
		}
		
		int plantCount = 3000;
		for(int i = 0; i < plantCount; i++) {
			context.add(new Plant(space, grid));
		}
		
		int humanCount = 4;
		for(int i = 0; i < humanCount; i++) {
			context.add(new Human(space, grid));
		}
		
		for(Object obj : context) {
			NdPoint pt = space.getLocation(obj);
			grid.moveTo(obj, (int)pt.getX(), (int)pt.getY());
		}
		
		return context;
	}
	
	public int plantCount() {
        final RunState runState = RunState.getInstance();

        // If simulation is not yet started or initialized correctly
        if (runState == null) {
                return 0;
        }

        @SuppressWarnings("unchecked")
        final Context<Object> masterContext = runState.getMasterContext();

        // If simulation is not initialized correctly and there is no root
        // context
        if (null == masterContext) {
                return 0;
        }

        System.out.println("Count: " + masterContext.getObjects(Plant.class).size());
        return masterContext.getObjects(Plant.class).size();
	}
}
