package biomass.continuospace.test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import biomass.model.utils.Vector2d;
import biomass.simulator.gui.DrawableObject;
import biomass.simulator.gui.SimulationModel;
import multiagent.model.agent.Agent;
import multiagent.model.environment.PhysicalObject;


public class Prey extends Agent implements DrawableObject {
	private ArrayList<Object> neighbors = new ArrayList<Object>(); 
	protected Color preyColor = new Color(0,0,0);
	private double scale=1;
	SimulationModel state;

	public Prey(double x, double y, SimulationModel state) 
	{
		this.x=x;
		this.y=y;
		this.state = state;
	}

	@Override
	public void step() {	
		neighbors = state.environment.getObjectsAtRadioTorus(id, state.parameters.getPreyNeighborDistance());
		TreeMap<Double, Prey> foundNeigh = new TreeMap<Double, Prey>();

		for (int i = 0; i < neighbors.size(); i++) {
			Prey neigh = null;
			if (neighbors.get(i) instanceof Prey)
				neigh = (Prey) neighbors.get(i);
			else
				continue;
			foundNeigh.put(state.environment.getSqDistanceTorus(neigh.id, id),neigh);
		}

		int count = 0;
		neighbors.clear();
		for (Map.Entry<Double, Prey> e : foundNeigh.entrySet()){
			neighbors.add(e.getValue());
			count++;
			if (count >= state.parameters.getMaxPreyNeighbors()) break;
		}

		Vector2d velocityUpdate = new Vector2d();     
		for (Object neigh : neighbors){	
			Vector2d distanceToNeighbor = state.environment.getVector2dTorus(((PhysicalObject)(neigh)).id, id);
			if ( distanceToNeighbor.lengthSquared() >= state.parameters.getPreySpacing() * state.parameters.getPreySpacing()){
				distanceToNeighbor.scale(state.parameters.getPreyAttractForce());
				velocityUpdate.add(distanceToNeighbor);
			}
			else{
				distanceToNeighbor.scale(state.parameters.getPreyRepelForce());
				velocityUpdate.add(distanceToNeighbor);
			}
		}

		for (Predator pred : state.predators){
			Vector2d distanceToPredator = state.environment.getVector2dTorus(pred.id, id);			
			if ( distanceToPredator.lengthSquared() < state.parameters.getPreyFearRadius() * state.parameters.getPreyFearRadius()){

				Vector2d v = new Vector2d(0,0);
				v.normalize(distanceToPredator);
				v.scale(state.parameters.getPreyFearForce());
				velocityUpdate.add(v);
			}
		}		
		velocityUpdate.scale(state.parameters.getPreyAcceleration() * state.parameters.getTimeScale());	
		velocity.add(velocityUpdate); 

		if (velocity.lengthSquared() > state.parameters.getPreyMaxSpeed() * state.parameters.getPreyMaxSpeed()){
			velocity.normalize();
			velocity.scale(state.parameters.getPreyMaxSpeed());
		}
		velocity.scale(state.parameters.getTimeScale());			
		state.environment.moveByDisplacement(id, velocity.x, velocity.y);
	}

	@Override
	public void draw(Graphics2D g2d) {

		g2d.setPaint(preyColor);
		Ellipse2D shape = new Ellipse2D.Double(x-scale/2,y-scale/2,scale, scale);
		g2d.fill(shape);

	}
}
