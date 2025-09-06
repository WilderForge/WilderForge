package com.wildermods.wilderforge.api.modLoadingV1.config;

import java.lang.reflect.Field;

import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.wildermods.wilderforge.api.eventV3.Event;
import com.wildermods.wilderforge.api.modLoadingV1.CoremodInfo;

public class CustomConfigEvent extends Event {
	
	private final CoremodInfo coremod;
	private final Object configuration;
	
	public CustomConfigEvent(CoremodInfo coremod, Object configuration, boolean cancellable) {
		super(cancellable);
		this.coremod = coremod;
		this.configuration = configuration;
	}
	
	public final String modid() {
		return coremod.modid()
;	}
	
	public final CoremodInfo getCoremod() {
		return coremod;
	}
	
	public final Object getConfigurationObject() {
		return configuration;
	}
	
	public static class AlterRowEvent extends CustomConfigEvent {
		
		private final Field field;
		private Table row;
		
		public AlterRowEvent(CoremodInfo coremod, Object configuration, Field field, Table row) {
			super(coremod, configuration, false);
			this.field = field;
			this.row = row;
		}
		
		public final Field getField() {
			return field;
		}
		
		public final Table getRow() {
			return row;
		}
		
		public final void setRow(Table row) {
			this.row = row;
		}
		
	}

	@SuppressWarnings("rawtypes")
	public static class AlterInputFieldEvent extends CustomConfigEvent {
		private final Field field;
		private Cell cell;
		private Widget toPlaceInCell;
		
		public AlterInputFieldEvent(CoremodInfo coremod, Object configuration, Field field, Cell cell, Widget toPlaceInCell) {
			super(coremod, configuration, false);
			this.field = field;
			this.cell = cell;
			this.toPlaceInCell = toPlaceInCell;
		}
		
		public final Field getField() {
			return field;
		}
		
		public final Cell getCell() {
			return cell;
		}
		
		public final Widget getWidgetToPlaceInCell() {
			return toPlaceInCell;
		}
		
		public final void setCell(Cell cell) {
			this.cell = cell;
		}
		
		public final void setWidgetToPlaceInCell(Widget toPlaceInCell) {
			this.toPlaceInCell = toPlaceInCell;
		}
	}
	
}
