package com.wildermods.wilderforge.launch.exception;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import com.wildermods.wilderforge.api.Coremod;

/**
 * Thrown when Wilderforge detects a circularity in the dependency hierarchy of a mod being
 * loaded.
 */

@SuppressWarnings("serial")
public class DependencyCircularityError extends CoremodLinkageError {

	public DependencyCircularityError(DefaultDirectedGraph<Coremod, DefaultEdge> graph) {
		super(getDependencyGraph(graph));
	}
	
	private static String getDependencyGraph(DefaultDirectedGraph<Coremod, DefaultEdge> graph) {
		
		return null;
	}

}
