/*
 *  YAMMP - Yet Another Multi Media Player for android
 *  Copyright (C) 2011-2012  Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This file is part of YAMMP.
 *
 *  YAMMP is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  YAMMP is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with YAMMP.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.harmony;

public interface Constants {

 	public static final String CACHE_DIR_NAME_ALBUMART = "albumart";
 
	public final static int ACTION_NOW = 1;
	public final static int ACTION_NEXT = 2;
	public final static int ACTION_LAST = 3;
	public final static int ID_NOTIFICATION_PLAYBACK = 1;
	public final static int ID_NOTIFICATION_SLEEPTIMER = 2;

	public final static int SHUFFLE_NONE = 0;
	public final static int SHUFFLE_NORMAL = 1;

	public final static int REPEAT_NONE = 0;
	public final static int REPEAT_CURRENT = 1;
	public final static int REPEAT_ALL = 2;

	public final static int PAGE_ARTIST = 0;
	public final static int PAGE_ALBUM = 1;
	public final static int PAGE_TRACK = 2;
	public final static int PAGE_PLAYLIST = 3;

	public final static long PLAYLIST_UNKNOWN = -1;
	public final static long PLAYLIST_ALL_SONGS = -2;
	public final static long PLAYLIST_QUEUE = -3;
	public final static long PLAYLIST_NEW = -4;
	public final static long PLAYLIST_FAVORITES = -5;
	public final static long PLAYLIST_RECENTLY_ADDED = -6;
	public final static long PLAYLIST_PODCASTS = -7;

	public static final String INTERNAL_VOLUME = "internal";
	public static final String EXTERNAL_VOLUME = "external";

	public final static String PLAYLIST_NAME_FAVORITES = "YAMMP Favorites";

	public final static String TYPE_ARTIST_ALBUM = "artist_album";
	public final static String TYPE_ALBUM = "album";
	public final static String TYPE_TRACK = "track";

	public final static String LOGTAG_SERVICE = "YAMMP.Service";
	public final static String LOGTAG_TEST = "YAMMP.Test";
	public final static String LOGTAG_MUSICUTILS = "YAMMP.MusicUtils";
	public final static String LOGTAG_WIDGET_4x1 = "MusicAppWidgetProvider4x1";
	public final static String LOGTAG_WIDGET_2x2 = "MusicAppWidgetProvider2x2";

	public final static String SCROBBLE_SLS_API = "com.adam.aslfms.notify.playstatechanged";

	public final static int SCROBBLE_PLAYSTATE_START = 0;
	public final static int SCROBBLE_PLAYSTATE_RESUME = 1;
	public final static int SCROBBLE_PLAYSTATE_PAUSE = 2;
	public final static int SCROBBLE_PLAYSTATE_COMPLETE = 3;

	public final static int VISUALIZER_TYPE_WAVE_FORM = 1;
	public final static int VISUALIZER_TYPE_FFT_SPECTRUM = 2;

	public final static int LYRICS_STATUS_OK = 0;
	public final static int LYRICS_STATUS_NOT_FOUND = 1;
	public final static int LYRICS_STATUS_INVALID = 2;

	public final static String PLUGINS_PNAME_PATTERN = "org.mariotaku.harmony.plugin";
	public final static String THEMES_PNAME_PATTERN = "org.mariotaku.harmony.theme";

	public final static String SERVICECMD = "org.mariotaku.harmony.musicservicecommand";
	public final static String CMDNAME = "command";
	public final static String CMDTOGGLEPAUSE = "togglepause";
	public final static String CMDSTOP = "stop";
	public final static String CMDPAUSE = "pause";
	public final static String CMDPREVIOUS = "previous";
	public final static String CMDNEXT = "next";
	public static final String CMDCYCLEREPEAT = "cyclerepeat";
	public static final String CMDTOGGLESHUFFLE = "toggleshuffle";
	public final static String CMDREFRESHLYRICS = "refreshlyrics";
	public final static String CMDRESENDALLLYRICS = "resendalllyrics";
	public final static String CMDREFRESHMETADATA = "refreshmetadata";
	public final static String CMDTOGGLEFAVORITE = "togglefavorite";
	public final static String CMDMUSICWIDGETUPDATE_4x1 = "musicwidgetupdate4x1";
	public final static String CMDMUSICWIDGETUPDATE_2x2 = "musicwidgetupdate2x2";

	public final static String TOGGLEPAUSE_ACTION = "org.mariotaku.harmony.musicservicecommand.togglepause";
	public final static String PAUSE_ACTION = "org.mariotaku.harmony.musicservicecommand.pause";
	public final static String PREVIOUS_ACTION = "org.mariotaku.harmony.musicservicecommand.previous";
	public final static String NEXT_ACTION = "org.mariotaku.harmony.musicservicecommand.next";
	public static final String CYCLEREPEAT_ACTION = "org.mariotaku.harmony.musicservicecommand.cyclerepeat";
	public static final String TOGGLESHUFFLE_ACTION = "org.mariotaku.harmony.musicservicecommand.toggleshuffle";

	public final static String TESTCMD_MUSICPLAYBACKACTIVITY = "org.mariotaku.harmony.test.musicplaybackactivity";

	public final static String SHAREDPREFS_PREFERENCES = "preferences";
	public final static String SHAREDPREFS_EQUALIZER = "equalizer";
	public final static String SHAREDPREFS_STATES = "states";

	public final static String MEDIASTORE_EXTERNAL_AUDIO_ALBUMART_URI = "content:media/external/audio/albumart";
	public final static String MEDIASTORE_EXTERNAL_AUDIO_MEDIA_URI = "content:media/external/audio/media/";
	public final static String MEDIASTORE_EXTERNAL_AUDIO_ALBUMS_URI = "content:media/external/audio/albums/";
	public final static String MEDIASTORE_EXTERNAL_AUDIO_ARTISTS_URI = "content:media/external/audio/artists/";
	public final static String MEDIASTORE_EXTERNAL_AUDIO_SEARCH_FANCY_URI = "content:media/external/audio/search/fancy/";

	public final static String LASTFM_APIKEY = "e682ad43038e19de1e33f583b191f5b2";

	public final static String BEHAVIOR_NEXT_SONG = "next_song";
	public final static String BEHAVIOR_PLAY_PAUSE = "play_pause";
	public final static String DEFAULT_SHAKING_BEHAVIOR = BEHAVIOR_NEXT_SONG;

	public final static boolean DEFAULT_LYRICS_WAKELOCK = false;
	public final static boolean DEFAULT_SPLIT_LYRICS = true;
	public final static boolean DEFAULT_SKIP_BLANK = true;
	public final static boolean DEFAULT_DISPLAY_LYRICS = true;
	public final static boolean DEFAULT_DISPLAY_VISUALIZER = true;
	public final static int DEFAULT_VISUALIZER_TYPE = VISUALIZER_TYPE_WAVE_FORM;
	public final static int DEFAULT_VISUALIZER_REFRESHRATE = 1;
	public final static int DEFAULT_VISUALIZER_ACCURACY = 1;
	public final static boolean DEFAULT_VISUALIZER_ANTIALIAS = true;

	public final static String STATE_KEY_CURRENTTAB = "currenttab";
	public final static String STATE_KEY_CURRPOS = "curpos";
	public final static String STATE_KEY_CARDID = "cardid";
	public final static String STATE_KEY_QUEUE = "queue";
	public final static String STATE_KEY_HISTORY = "history";
	public final static String STATE_KEY_SEEKPOS = "seekpos";
	public final static String STATE_KEY_REPEATMODE = "repeatmode";
	public final static String STATE_KEY_SHUFFLEMODE = "shufflemode";
	public final static String STATE_KEY_PAGE_POSITION_PLAYBACK = "page_position_playback";

	public final static String PREF_KEY_NUMWEEKS = "numweeks";
	
	public final static String PREFERENCE_KEY_LYRICS_TEXTSIZE = "lyrics_textsize";

	public final static float TEXTSIZE_LYRICS_MIN = 12;
	public final static float TEXTSIZE_LYRICS_MAX = 26;
	public final static float PREFERENCE_DEFAULT_TEXTSIZE_LYRICS = 14;
	
	public final static String KEY_RESCAN_MEDIA = "rescan_media";
	public final static String KEY_LYRICS_WAKELOCK = "lyrics_wakelock";
	public final static String KEY_ALBUMART_SIZE = "albumart_size";
	public final static String KEY_DISPLAY_LYRICS = "display_lyrics";
	public final static String KEY_PLUGINS_MANAGER = "plugins_manager";
	public final static String KEY_ENABLE_SCROBBLING = "enable_scrobbling";
	public final static String KEY_GENTLE_SLEEPTIMER = "gentle_sleeptimer";
	public final static String KEY_DISPLAY_VISUALIZER = "display_visualizer";
	public final static String KEY_VISUALIZER_TYPE = "visualizer_type";
	public final static String KEY_VISUALIZER_REFRESHRATE = "visualizer_refreshrate";
	public final static String KEY_VISUALIZER_ACCURACY = "visualizer_accuracy";
	public final static String KEY_VISUALIZER_ANTIALIAS = "visualizer_antialias";
	public final static String KEY_UI_COLOR = "ui_color";
	public final static String KEY_AUTO_COLOR = "auto_color";
	public final static String KEY_CUSTOMIZED_COLOR = "customized_color";
	public final static String KEY_EQUALIZER_ENABLED = "equalizer_enabled";
	public final static String KEY_EQUALIZER_SETTINGS = "equalizer_settings";
	public final static String KEY_SHAKE_ENABLED = "shake_enabled";
	public final static String KEY_SHAKING_THRESHOLD = "shaking_threshold";
	public final static String KEY_SHAKING_BEHAVIOR = "shaking_behavior";
	public final static String KEY_BLUR_BACKGROUND = "blur_background";

	public final static float DEFAULT_SHAKING_THRESHOLD = 5000f;

	public final static int RESULT_DELETE_MUSIC = 1;
	public final static int RESULT_DELETE_ART = 2;
	public final static int RESULT_DELETE_LYRICS = 3;

	public final static String BROADCAST_KEY_ID = "id";
	public final static String BROADCAST_KEY_ARTIST = "artist";
	public final static String BROADCAST_KEY_ALBUM = "album";
	public final static String BROADCAST_KEY_TRACK = "track";
	public final static String BROADCAST_KEY_PLAYING = "playing";
	public final static String BROADCAST_KEY_ISFAVORITE = "isfavorite";
	public final static String BROADCAST_KEY_SONGID = "songid";
	public final static String BROADCAST_KEY_ALBUMID = "albumid";
	public final static String BROADCAST_KEY_POSITION = "position";
	public final static String BROADCAST_KEY_REPEATMODE = "repeatmode";
	public final static String BROADCAST_KEY_SHUFFLEMODE = "shufflemode";
	public final static String BROADCAST_KEY_DURATION = "duration";
	public final static String BROADCAST_KEY_LISTSIZE = "listsize";
	public final static String BROADCAST_KEY_STATE = "state";
	public final static String BROADCAST_KEY_APP_NAME = "app-name";
	public final static String BROADCAST_KEY_APP_PACKAGE = "app-package";
	public final static String BROADCAST_KEY_LYRICS_STATUS = "lyrics_status";
	public final static String BROADCAST_KEY_LYRICS_ID = "lyrics_id";
	public final static String BROADCAST_KEY_LYRICS = "lyrics";

	public final static String INTENT_KEY_CONTENT = "content";
	public final static String INTENT_KEY_ITEMS = "items";
	public final static String INTENT_KEY_ALBUM = "album";
	public final static String INTENT_KEY_ARTIST = "artist";
	public final static String INTENT_KEY_TRACK = "track";
	public final static String INTENT_KEY_PLAYLIST = "playlist";
	public final static String INTENT_KEY_PATH = "path";
	public final static String INTENT_KEY_LIST = "list";
	public final static String INTENT_KEY_RENAME = "rename";
	public final static String INTENT_KEY_DEFAULT_NAME = "default_name";
	public final static String INTENT_KEY_FILTER = "filter";

	public final static String INTENT_KEY_TYPE = "type";
	public final static String INTENT_KEY_ACTION = "action";
	public final static String INTENT_KEY_DATA = "data";

	public final static String MAP_KEY_NAME = "name";
	public final static String MAP_KEY_ID = "id";

	public final static String INTENT_SEARCH_LYRICS = "org.mariotaku.harmony.SEARCH_LYRICS";
	public final static String INTENT_SEARCH_ALBUMART = "org.mariotaku.harmony.SEARCH_ALBUMART";
	public final static String INTENT_DELETE_ITEMS = "org.mariotaku.harmony.DELETE_ITEMS";
	public final static String INTENT_CONFIGURE_PLUGIN = "org.mariotaku.harmony.CONFIGURE_PLUGIN";
	public final static String INTENT_OPEN_PLUGIN = "org.mariotaku.harmony.OPEN_PLUGIN";
	public final static String INTENT_CONFIGURE_THEME = "org.mariotaku.harmony.CONFIGURE_THEME";
	public final static String INTENT_PREVIEW_THEME = "org.mariotaku.harmony.PREVIEW_THEME";
	public final static String INTENT_APPEARANCE_SETTINGS = "org.mariotaku.harmony.APPEARANCE_SETTINGS";
	public final static String INTENT_MUSIC_SETTINGS = "org.mariotaku.harmony.MUSIC_SETTINGS";
	public final static String INTENT_PLAYBACK_VIEWER = "org.mariotaku.harmony.PLAYBACK_VIEWER";
	public final static String INTENT_MUSIC_BROWSER = "org.mariotaku.harmony.MUSIC_BROWSER";
	public final static String INTENT_STREAM_PLAYER = "org.mariotaku.harmony.STREAM_PLAYER";
	public final static String INTENT_ADD_TO_PLAYLIST = "org.mariotaku.harmony.ADD_TO_PLAYLIST";
	public final static String INTENT_CREATE_PLAYLIST = "org.mariotaku.harmony.CREATE_PLAYLIST";
	public final static String INTENT_RENAME_PLAYLIST = "org.mariotaku.harmony.RENAME_PLAYLIST";
	public final static String INTENT_WEEK_SELECTOR = "org.mariotaku.harmony.WEEK_SELECTOR";
	public final static String INTENT_SLEEP_TIMER = "org.mariotaku.harmony.SLEEP_TIMER";
	public final static String INTENT_PLAY_SHORTCUT = "org.mariotaku.harmony.PLAY_SHORTCUT";
	public final static String INTENT_PLUGINS_MANAGER = "org.mariotaku.harmony.PLUGINS_MANAGER";
	public final static String INTENT_PLAYBACK_SERVICE = "org.mariotaku.harmony.PLAYBACK_SERVICE";

	public final static String BROADCAST_PLAYSTATE_CHANGED = "org.mariotaku.harmony.playstatechanged";
	public final static String BROADCAST_MEDIA_CHANGED = "org.mariotaku.harmony.metachanged";
	public final static String BROADCAST_FAVORITESTATE_CHANGED = "org.mariotaku.harmony.favoritestatechanged";
	public final static String BROADCAST_NEW_LYRICS_LOADED = "org.mariotaku.harmony.newlyricsloaded";
	public final static String BROADCAST_LYRICS_REFRESHED = "org.mariotaku.harmony.lyricsrefreshed";
	public final static String BROADCAST_QUEUE_CHANGED = "org.mariotaku.harmony.queuechanged";
	public final static String BROADCAST_REPEATMODE_CHANGED = "org.mariotaku.harmony.repeatmodechanged";
	public final static String BROADCAST_SHUFFLEMODE_CHANGED = "org.mariotaku.harmony.shufflemodechanged";
	public final static String BROADCAST_PLAYBACK_COMPLETE = "org.mariotaku.harmony.playbackcomplete";
	public final static String BROADCAST_ASYNC_OPEN_COMPLETE = "org.mariotaku.harmony.asyncopencomplete";
	public final static String BROADCAST_SEEK_CHANGED = "org.mariotaku.harmony.refreshui";
	public final static String BROADCAST_PLAYSTATUS_REQUEST = "org.mariotaku.harmony.playstatusrequest";
	public final static String BROADCAST_PLAYSTATUS_RESPONSE = "org.mariotaku.harmony.playstatusresponse";

	public final static int MENU_OPEN_URL = R.id.open_url;
	public final static int MENU_ADD_TO_PLAYLIST = R.id.add_to_playlist;
	public final static int MENU_SLEEP_TIMER = R.id.sleep_timer;
	public final static int MENU_SAVE_AS_PLAYLIST = R.id.save_as_playlist;
	public final static int MENU_CLEAR_PLAYLIST = R.id.clear_playlist;
	public final static int MENU_PLAYLIST_SELECTED = R.id.playlist_selected;
	public final static int MENU_NEW_PLAYLIST = R.id.new_playlist;
	public final static int PLAY_SELECTION = R.id.play_selection;
	public final static int GOTO_PLAYBACK = R.id.goto_playback;
	public final static int GOTO_HOME = android.R.id.home;
	public final static int ADD_TO_FAVORITES = R.id.add_to_favorite;
	public final static int PARTY_SHUFFLE = R.id.party_shuffle;
	public final static int SHUFFLE_ALL = R.id.shuffle_all;
	public final static int PLAY_ALL = R.id.play_all;
	public final static int DELETE_ITEMS = R.id.delete_items;
	public final static int EQUALIZER = R.id.equalizer;
	public final static int EQUALIZER_PRESETS = R.id.equalizer_presets;
	public final static int EQUALIZER_RESET = R.id.equalizer_reset;
	public final static int SCAN_DONE = R.id.scan_done;
	public final static int QUEUE = R.id.queue;
	public final static int SETTINGS = R.id.settings;
	public final static int SEARCH = R.id.search;
	public final static int REMOVE = R.id.remove;
	public final static int PLAY_PAUSE = R.id.play_pause;
	public final static int NEXT = R.id.next;
	public final static int CHILD_MENU_BASE = 15; // this should be the last

	public final static String[] HIDE_PLAYLISTS = new String[] { "Sony Ericsson play queue",
			"Sony Ericsson played tracks", "Sony Ericsson temporary playlist", "$$miui" };

	/**
	 * Following genres data is copied from from id3lib 3.8.3
	 */
	public final static String[] GENRES_DB = { "Blues", "Classic Rock", "Country", "Dance",
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

}
