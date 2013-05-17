package org.mariotaku.harmony.model;

import static org.mariotaku.harmony.util.Utils.parseInt;

import org.mariotaku.harmony.util.ArrayUtils;

import android.text.TextUtils;

public class GenreInfo implements Comparable<GenreInfo> {

	public static final GenreInfo UNKNOWN = new GenreInfo(-1, "Unknown");

	/**
	 * Following genres data is copied from from id3lib 3.8.3
	 */
	private final static String[] ID3_GENRES_LIST = { "Blues", "Classic Rock", "Country", "Dance",
		"Disco", "Funk", "Grunge", "Hip-Hop", "Jazz", "Metal", "New Age", "Oldies", "Other",
		"Pop", "R&B", "Rap", "Reggae", "Rock", "Techno", "Industrial", "Alternative", "Ska",
		"Death Metal", "Pranks", "Soundtrack", "Euro-Techno", "Ambient", "Trip-Hop", "Vocal",
		"Jazz+Funk", "Fusion", "Trance", "Classical", "Instrumental", "Acid", "House", "Game",
		"Sound Clip", "Gospel", "Noise", "AlternRock", "Bass", "Soul", "Punk", "Space",
		"Meditative", "Instrumental Pop", "Instrumental Rock", "Ethnic", "Gothic", "Darkwave",
		"Techno-Industrial", "Electronic", "Pop-Folk", "Eurodance", "Dream", "Southern Rock",
		"Comedy", "Cult", "Gangsta", "Top 40", "Christian Rap", "Pop/Funk", "Jungle",
		"Native American", "Cabaret", "New Wave", "Psychedelic", "Rave", "Showtunes",
		"Trailer", "Lo-Fi", "Tribal", "Acid Punk", "Acid Jazz", "Polka", "Retro", "Musical",
		"Rock & Roll", "Hard Rock", "Folk", "Folk-Rock", "National Folk", "Swing",
		"Fast Fusion", "Bebob", "Latin", "Revival", "Celtic", "Bluegrass", "Avantgarde",
		"Gothic Rock", "Progressive Rock", "Psychedelic Rock", "Symphonic Rock", "Slow Rock",
		"Big Band", "Chorus", "Easy Listening", "Acoustic", "Humour", "Speech", "Chanson",
		"Opera", "Chamber Music", "Sonata", "Symphony", "Booty Bass", "Primus", "Porn Groove",
		"Satire", "Slow Jam", "Club", "Tango", "Samba", "Folklore", "Ballad", "Power Ballad",
		"Rhythmic Soul", "Freestyle", "Duet", "Punk Rock", "Drum Solo", "A capella",
		"Euro-House", "Dance Hall", "Goa", "Drum & Bass", "Club-House", "Hardcore", "Terror",
		"Indie", "Britpop", "Negerpunk", "Polsk Punk", "Beat", "Christian Gangsta Rap",
		"Heavy Metal", "Black Metal", "Crossover", "Contemporary Christian", "Christian Rock ",
		"Merengue", "Salsa", "Thrash Metal", "Anime", "JPop", "Synthpop" };
		
	private final int id;
	private final String name;

	private GenreInfo(int id, String name) {
		this.id = id;
		this.name = name;
	}

	@Override
	public int compareTo(final GenreInfo another) {
		return id - another.getId();
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof GenreInfo)) return false;
		return ((GenreInfo) object).getId() == id;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isUnknown() {
		return id < 0;
	}
	
	@Override
	public String toString() {
		return "GenreInfo{id=" + id + ",name=" + name + "}";
	}

	public static GenreInfo valueOf(final String name) {
		final int idx;
		if (TextUtils.isEmpty(name)) {
			idx = -1;
		} else if (TextUtils.isDigitsOnly(name)) {
			idx = parseInt(name, -1);
		} else {
			idx = ArrayUtils.indexOfIgnoreCase(ID3_GENRES_LIST, name);
		}
		if (idx < 0 || idx >= ID3_GENRES_LIST.length) return UNKNOWN;
		return new GenreInfo(idx, ID3_GENRES_LIST[idx]);
	}
}
