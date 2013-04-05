package org.mariotaku.harmony.model;

public interface Lyrics {

	public Line get(int index);
	
	public Line find(long current);

	public Line findNoOffset(long current);

	public Line[] getAll();

	public long getOffset();

	public int size();

	public static interface Line {

		public String getText();
		
		public int getIndex();

		public long getTime();

		public long getActualTime();

	}
}
