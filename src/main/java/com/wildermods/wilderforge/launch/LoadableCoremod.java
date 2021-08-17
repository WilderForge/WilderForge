package com.wildermods.wilderforge.launch;

import java.io.IOException;

import java.util.HashSet;
import java.util.Iterator;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import com.wildermods.wilderforge.launch.exception.CoremodLinkageError;
import com.wildermods.wilderforge.launch.exception.DuplicateDependencyDeclarationError;

public abstract class LoadableCoremod extends Coremod {
	
	protected static final DefaultDirectedGraph<Coremod, DependencyEdge> dependencyGraph = new DefaultDirectedGraph<Coremod, DependencyEdge>(DependencyEdge.class);
	
	protected LoadableCoremod() throws IOException, CoremodLinkageError {
		
	}

	protected void parseDependencies() throws IOException, CoremodLinkageError {
		HashSet<Dependency> required = new HashSet<Dependency>();
		HashSet<Dependency> optional = new HashSet<Dependency>();
		JsonElement reqDepsEle = getModJson().get("requires");
		JsonElement optDepsEle = getModJson().get("optional");
		JsonArray reqDeps = null;
		JsonArray optDeps = null;
		
		if(reqDepsEle != null) {
			reqDeps = reqDepsEle.getAsJsonArray();
		}
		if(optDepsEle != null) {
			optDeps = optDepsEle.getAsJsonArray();
		}
		

		
		try {
			if(reqDeps != null) {
				Iterator<JsonElement> iterator = reqDeps.iterator();
				while(iterator.hasNext()) {
					Dependency dep = new Dependency(true, iterator.next().getAsJsonObject());
					required.add(dep);
				}
			}
			if(optDeps != null) {
				Iterator<JsonElement> iterator = optDeps.iterator();
				while(iterator.hasNext()) {
					Dependency dep = new Dependency(false, iterator.next().getAsJsonObject());
					optional.add(dep);
				}
			}
			dependencyGraph.addVertex(this);
			System.out.println(this.toString());
			System.out.println(dependencyGraph.containsVertex(this));
			System.out.println(dependencyGraph.toString());
			for(Coremod dep : required) {
				if(!dependencyGraph.addVertex(dep)) {
					dep = getCoremod(dep);
				}
				if(!dependencyGraph.addEdge(this, dep, new DependencyEdge(true))) {
					throw new DuplicateDependencyDeclarationError(this, dep);
				}
			}
			for(Coremod dep : optional) {
				if(!dependencyGraph.addVertex(dep)) {
					dep = getCoremod(dep);
				}
				if(!dependencyGraph.addEdge(this, dep, new DependencyEdge(false))) {
					throw new DuplicateDependencyDeclarationError(this, dep);
				}
			}
		}
		catch(CoremodLinkageError e) {
			throw new CoremodLinkageError("Could not load coremod " + this + " More information below", e);
		}
	}
	
	static class DependencyEdge extends DefaultEdge {
		private final boolean required;
		
		public DependencyEdge(boolean required) {
			this.required = required;
		}
		
		public boolean isRequired() {
			return required;
		}
	}
	
	Coremod getCoremod(Coremod coremod) {
		Iterator<Coremod> iterator = dependencyGraph.vertexSet().iterator();
		while(iterator.hasNext()) {
			Coremod vertex = iterator.next();
			if(vertex.equals(coremod)) {
				return vertex;
			}
		}
		return null;
	}
	
}
