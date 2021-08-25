package com.wildermods.wilderforge.launch;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.MarkerManager.Log4jMarker;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.wildermods.wilderforge.api.versionV1.MultiVersionRange;
import com.wildermods.wilderforge.api.versionV1.Versioned;
import com.wildermods.wilderforge.launch.exception.CoremodFormatError;
import com.wildermods.wilderforge.launch.exception.CoremodLinkageError;
import com.wildermods.wilderforge.launch.exception.CoremodNotFoundError;
import com.wildermods.wilderforge.launch.exception.CoremodVersionError;
import com.wildermods.wilderforge.launch.exception.DependencyCircularityError;
import com.wildermods.wilderforge.launch.exception.DuplicateCoremodError;
import com.wildermods.wilderforge.launch.exception.DuplicateDependencyDeclarationError;

import static com.wildermods.wilderforge.launch.LoadStatus.*;

public class Coremods {
	public static final Logger LOGGER = LogManager.getLogger(Coremods.class);
	protected static final DefaultDirectedGraph<String, DependencyEdge> dependencyGraph = new DefaultDirectedGraph<String, DependencyEdge>(DependencyEdge.class);
	protected static final @SuppressWarnings("serial") HashMap<String, LoadStatus> loadStatuses = new HashMap<String, LoadStatus>(){
		{
			put("wilderforge", UNDISCOVERED);//put("wildermyth", UNDISCOVERED); 
		}
	};
	protected static final HashMap<String, Coremod> coremods = new HashMap<String, Coremod>();
	
	public static void addFoundCoremod(Coremod coremod) throws CoremodLinkageError {
		if(coremods.put(coremod.value(), coremod) == null) {
			loadStatuses.put(coremod.value(), DISCOVERED);
			return;
		}
		throw new DuplicateCoremodError(coremod);
	}
	
	public static void addDeclaredCoremod(String modid) {
		if(!dependencyGraph.addVertex(modid)) {
			loadStatuses.put(modid, UNDISCOVERED);
		}
	}
	
	public static void addOptionalDependency(String declarer, Dependency dep) {
		dependencyGraph.addVertex(dep.value());
		if(!loadStatuses.containsKey(dep.value())) {
			loadStatuses.put(dep.value(), UNDISCOVERED);
		}
		if(!dependencyGraph.addEdge(declarer, dep.value(), new DependencyEdge(dep, dep.getVersionRange(), true))) {
			loadStatuses.put(declarer, ERRORED);
			loadStatuses.put(dep.value(), ERRORED);
			throw new DuplicateDependencyDeclarationError(declarer, dep);
		}
	}
	
	public static void addRequiredDependency(String declarer, Dependency dep) {
		dependencyGraph.addVertex(dep.value());
		if(!dependencyGraph.addEdge(declarer, dep.value(), new DependencyEdge(dep, dep.getVersionRange(), false))) {
			throw new DuplicateDependencyDeclarationError(declarer, dep);
		}
	}
	
	public static Coremod getCoremod(Coremod coremod) {
		return getCoremod(coremod.value());
	}
	
	public static Coremod getCoremod(String modid) {
		return coremods.get(modid);
	}
	
	static void loadCoremods(ClassLoader classLoader) {
		LOGGER.info("Looking for coremod jsons...");
		discoverCoremodJsons(classLoader);
		LOGGER.info("Found " + dependencyGraph.vertexSet().size() + " declared coremods ");
		parseJsons();
		validateCoremods(classLoader);
	}
	
	protected static void parseDependencies(JsonObject json) throws IOException, CoremodLinkageError {
		HashSet<Dependency> required = new HashSet<Dependency>();
		HashSet<Dependency> optional = new HashSet<Dependency>();
		JsonElement reqDepsEle = json.get("requires");
		JsonElement optDepsEle = json.get("optional");
		JsonArray reqDeps = null;
		JsonArray optDeps = null;
		
		if(reqDepsEle != null) {
			reqDeps = reqDepsEle.getAsJsonArray();
		}
		if(optDepsEle != null) {
			optDeps = optDepsEle.getAsJsonArray();
		}
		
		String coremod = getModId(json);
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
			addDeclaredCoremod(json.get("modid").getAsString());
			for(Dependency dep : required) {
				addRequiredDependency(coremod, dep);

			}
			for(Dependency dep : optional) {
				addOptionalDependency(coremod, dep);
			}
		}
		catch(CoremodLinkageError e) {
			throw new CoremodLinkageError("Could not load coremod " + coremod + " More information below", e);
		}
	}
	
	@SuppressWarnings("serial")
	static class DependencyEdge extends DefaultEdge {
		private final boolean required;
		private final MultiVersionRange versionRange;
		
		public DependencyEdge(Coremod dependency, MultiVersionRange versionRange, boolean required) {
			this.required = required;
			this.versionRange = versionRange;
		}
		
		public boolean isRequired() {
			return required;
		}

		public MultiVersionRange getVersionRange() {
			return versionRange;
		}
		
		public boolean isWithinRange(Versioned versioned) {
			return versionRange.isWithinRange(versioned.getVersion());
		}
	}
	
	private static String getModId(JsonObject o) {
		if(o == null) {
			throw new CoremodFormatError("Could not parse json");
		}
		JsonElement modidElement = o.get("modid");
		if(modidElement == null) {
			throw new CoremodFormatError("No modid declared in json");
		}
		try {
			return modidElement.getAsString();
		}
		catch(ClassCastException | IllegalStateException e) {
			throw new CoremodFormatError("modid is not a string", e);
		}
	}
	
	static final void discoverCoremodJsons(ClassLoader classLoader) {
		try {
			Field jarField = classLoader.getClass().getDeclaredField("specialJars");
			jarField.setAccessible(true);
			URL[] jarLocs = (URL[]) jarField.get(classLoader);
			Wildermyth wildermyth;
			WilderForge wilderforge;
			try {
				wildermyth = new Wildermyth();
				wilderforge = new WilderForge();
			}
			catch(IOException e) {
				throw new CoremodLinkageError(e);
			}
			addFoundCoremod(wildermyth);
			addFoundCoremod(wilderforge);
			for(URL url : jarLocs) {
				if(url.toString().contains("/mods/")) {
					try {
						URL url2 = new URL("jar:" + url.toString() + "!/");
						LOGGER.debug("Opening " + url2);
						URLConnection urlConnection = url2.openConnection();
						Main.LOGGER.info(urlConnection);
						if(urlConnection instanceof JarURLConnection) {
							JarCoremod jarCoremod = new JarCoremod((JarURLConnection)urlConnection);
							addDeclaredCoremod(getModId(jarCoremod.getModJson()));
							addFoundCoremod(jarCoremod);
						}
						else {
							LOGGER.error(url2 + " is not a JarURLConnection. (" + urlConnection.getClass().getName() + ")");
						}
					} catch (IOException e) {
						LOGGER.catching(e);
					}
				}
				else {
					LOGGER.debug("Skipping non-mod url: " + url);
				}
			}
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new AssertionError(e);
		}
	}
	
	static final void parseJsons() {
		for(Coremod coremod : coremods.values()) {
			try {
				parseDependencies(coremod.getModJson());
			}
			catch(IOException e) {
				loadStatuses.put(coremod.value(), ERRORED);
				throw new CoremodLinkageError(e);
			}
		}
	}
	
	static final void validateCoremods(ClassLoader classLoader) {
		Set<Class<?>> classes = Main.reflectionsHelper.getAllClassesAnnotatedWith(com.wildermods.wilderforge.api.modLoadingV1.Coremod.class);
		LOGGER.info("Found " + classes.size() + " classes annotated with @Coremod:");
		for(Class<?> clazz : classes) {
			LOGGER.info("@Coremod(" + clazz.getAnnotation(com.wildermods.wilderforge.api.modLoadingV1.Coremod.class).value() + ") is " + clazz.getCanonicalName());
		}
		
		Logger logger = LogManager.getLogger("Coremod Validator");
		for(String modid: dependencyGraph.vertexSet()) {
			Coremod coremod = getCoremod(modid);
			for(DependencyEdge edge : dependencyGraph.outgoingEdgesOf(modid)) {
				Log4jMarker marker = new Log4jMarker(modid);
				String depID = dependencyGraph.getEdgeTarget(edge);
				Coremod dep = getCoremod(depID);
				if(edge.isRequired()) {
					if(dep != null) {
						logger.info(marker, "Found required dependency " + dep.value());
					}
					else {
						throw new CoremodNotFoundError(coremod, depID);
					}
				}
				else {
					if(dep != null) {
						logger.info(marker, "Found optional dependency " + dep.value());
					}
					else {
						logger.info(marker, "Did not find optional dependency " + depID);
					}
				}
				if(!edge.getVersionRange().isWithinRange(dep.getVersion())) {
					throw new CoremodVersionError(coremod, dep, edge.getVersionRange());
				}
			}
		}
		
		CycleDetector<String, DependencyEdge> cycleDetector = new CycleDetector<String, DependencyEdge>(dependencyGraph);
		if(cycleDetector.detectCycles()) {	
			Set<String> cycleVerticies;
			LOGGER.fatal("Dependency circularity detected");
			cycleVerticies = cycleDetector.findCycles();
			String errMsg = "";
			int i = 1;
			while(!cycleVerticies.isEmpty()) {
				errMsg = errMsg + "Cycle " + i + ":";
				Iterator<String> iterator = cycleVerticies.iterator();
				String baseVertex = iterator.next();
				loadStatuses.put(baseVertex, ERRORED);
				for(String s : cycleDetector.findCyclesContainingVertex(baseVertex)) {
					errMsg = errMsg + "[" + s + "] -> ";
					loadStatuses.put(s, ERRORED);
					cycleVerticies.remove(s);
				}
				errMsg = errMsg + "[" + baseVertex + "]";
				if(!cycleVerticies.isEmpty()) {
					errMsg = errMsg + "\n";
				}
			}
			throw new DependencyCircularityError(errMsg);
		}
	}
	
}
