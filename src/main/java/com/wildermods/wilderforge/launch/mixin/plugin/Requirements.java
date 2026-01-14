package com.wildermods.wilderforge.launch.mixin.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

class Requirements {

	static record RequireData(String modid, String version, boolean shouldCrash) {}
	
	static List<RequireData> getRequirementsFromClass(ClassNode node) {
		return getRequirements(node.visibleAnnotations);
	}
	
	static List<RequireData> getRequirements(List<AnnotationNode> annotations) {
		if (annotations == null || annotations.isEmpty()) {
			return Collections.emptyList();
		}

		List<RequireData> out = new ArrayList<>();

		for (AnnotationNode ann : annotations) {
			String desc = ann.desc;

			// @Require
			if (desc.startsWith("Lcom/wildermods") && desc.endsWith("/Require;")) {
				collectRequire(ann, out);
			}
			// @Requires
			else if (desc.startsWith("Lcom/wildermods") && desc.endsWith("/Requires;")) {
				if (ann.values == null) continue;

				for (int i = 0; i < ann.values.size(); i += 2) {
					String name = (String) ann.values.get(i);
					Object value = ann.values.get(i + 1);

					if ("value".equals(name) && value instanceof List<?> list) {
						for (Object o : list) {
							if (o instanceof AnnotationNode nested
									&& nested.desc.endsWith("/Require;")) {
								collectRequire(nested, out);
							}
						}
					}
				}
			}
		}

		return out;
	}
	
	private static void collectRequire(AnnotationNode require, List<RequireData> out) {
		if (require.values == null) return;

		String modid = null;
		String version = null;
		boolean shouldCrash = false;

		for (int i = 0; i < require.values.size(); i += 2) {
			String name = (String) require.values.get(i);
			Object value = require.values.get(i + 1);

			if ("value".equals(name) && value instanceof AnnotationNode modAnn && modAnn.desc.endsWith("/Mod;")) {
				if (modAnn.values != null) {
					for (int j = 0; j < modAnn.values.size(); j += 2) {
						String key = (String) modAnn.values.get(j);
						Object v = modAnn.values.get(j + 1);

						if ("modid".equals(key)) modid = (String) v;
						else if ("version".equals(key)) version = (String) v;
					}
				}
			} else if ("crash".equals(name)) {
				shouldCrash = (Boolean) value;
			}
		}

		if (modid != null) {
			out.add(new RequireData(modid, version, shouldCrash));
		}
	}
	
}
