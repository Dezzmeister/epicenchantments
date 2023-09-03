package com.dezzmeister.epicenchantments.bindings;

public class Activation {
	
	public static enum LeftClick {
		ALL("all"),
		ENTITY("entity"),
		BLOCK("block");
		
		private final String nbtName;
		
		private LeftClick(final String _nbtName) {
			nbtName = _nbtName;
		}
		
		public final String getNbtName() {
			return nbtName;
		}
		
		public static final LeftClick getEnumForName(final String nbtName) {
			if (nbtName == null) {
				return null;
			}
			
			switch (nbtName) {
				case "all":
					return LeftClick.ALL;
				case "entity":
					return LeftClick.ENTITY;
				case "block":
					return LeftClick.BLOCK;
				default:
					return null;
			}
		}
	}
	
	public static enum RightClick {
		ALL("all"),
		ENTITY("entity"),
		BLOCK("block"),
		AIR("air");
		
		private final String nbtName;
		
		private RightClick(final String _nbtName) {
			nbtName = _nbtName;
		}
		
		public final String getNbtName() {
			return nbtName;
		}
		
		public static final RightClick getEnumForName(final String nbtName) {
			if (nbtName == null) {
				return null;
			}
			
			switch (nbtName) {
				case "all":
					return RightClick.ALL;
				case "entity":
					return RightClick.ENTITY;
				case "block":
					return RightClick.BLOCK;
				case "air":
					return RightClick.AIR;
				default:
					return null;
			}
		}
	}
}
