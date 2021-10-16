package com.wildermods.wildermyth.gson;

import org.junit.Test;

import com.google.gson.Gson;

public class TestSerialization {

	@Test
	public void test() {
		Gson gson = new Gson();
		String json = gson.toJson(new Thing() {
			@Override
			public void doThing() {
				System.out.println("test");
			};
		});
		gson.fromJson(json, Thing.class);
	}

	static interface Thing {
		
		public void doThing();
		
	}
	
}
