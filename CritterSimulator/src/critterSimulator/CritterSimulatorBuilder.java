package critterSimulator;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;


/**
 * Primary ContextBuilder class for the CritterSimulator
 * model.
 * 
 * Used to create the main context and seed the initial
 * set of agents into the context.
 * 
 * @author Eric Ostrowski, Doug MacDonald
 *
 */
public class CritterSimulatorBuilder implements ContextBuilder<Object> {
	@SuppressWarnings("rawtypes")
	@Override
	public Context build(Context<Object> context) {
		//Create the Simulation Area
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
		
		//Add Herbivores
		int herbivoreCount = 150;
		for(int i = 0; i < herbivoreCount; i++) {
			context.add(new Herbivore(space, grid));
		}
		
		//Add Carnivores
		int carnivoreCount = 2;
		for(int i = 0; i < carnivoreCount; i++) {
			context.add(new Carnivore(space, grid));
		}
		
		//Add Plants
		int plantCount = 3000;
		for(int i = 0; i < plantCount; i++) {
			context.add(new Plant(space, grid));
		}
		
		//Add Humans
		int humanCount = 1;
		for(int i = 0; i < humanCount; i++) {
			context.add(new Human(space, grid));
		}
		
		//Place Agents on simulation
		for(Object obj : context) {
			NdPoint pt = space.getLocation(obj);
			grid.moveTo(obj, (int)pt.getX(), (int)pt.getY());
		}
		
		return context;
	}
}