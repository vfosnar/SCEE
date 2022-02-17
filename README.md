This is my personal edition of StreetComplete, including a bunch of modifications.
Most of these modifications are _not thoroughly tested_ any may have bugs.
New quests may require knowledge about the changed/added OSM tags.

* renamed: allows install parallel to StreetComplete, and shows a different name in changesets
* show POIs
  * can be enabled like quests, but shows colored dots instead of pins (tap dots for details)
  * adjust quest dot and pin offsets, so everything is at the correct position
  * in some rare cases, existing POIs are not shown. No idea why...
* new quests
  * contact quests for phone and website
  * re-ask/remove surface for badly tagged tracks (by matkoniecz, https://github.com/matkoniecz/Zazolc)
  * service building operator (no proper auto-complete list like ATM operator)
  * service building type: substation, well, pumping station, ...
  * ask for cuisine of restaurants and fast food, including some auto-complete function for very common values
  * ask for healthcare specialty of amenity = doctors
* quest changes
  * allow "paved" surface without note (because one lane concrete, one lane asphalt is too common)
  * ask type for more surveillance cameras
  * move "no cycleway" answer to move accessible position
  * per-quest-settings (only few quests)
    * allows modification of elements the quest is asked for, or of tags used in answer (maxspeed quest)
    * allow asking for for more objects in the level quest: more than just shops, and also in normal buildings
    * changes in element selection do not take effect immediately, but only on newly created quests
    * for most quests it is necessary to restart the app to actually use changed quest-settings
  * add "private" other answer to track type, way surface and way lit quests
  * add "demolished" other answer to building type quest
  * some additional building types
  * crossing type quest: keep order of answers constant, add "zebra" and "raised" answers
  * ask for step count only for steps shorter than 15 m
  * building type quest is split into two quests
    * one for buildings that definitely have an address
    * one for buildings that probably have no address (not using the entire address quest logic, so some buildings may actually have an address)
  * reduced auto-completion threshold (suggestions will appear after the first letter)
* allow creating text notes in a gpx file
  * notes.gpx file must be copied manually from Android/data/de.westnordost.streetcomplete.h3/files
  * button for normal OSM notes is hidden if keyboard is shown. sorry.
* allow pre-filling new profile from existing profile
* delete cache: choose between deleting tile cache (background) or OSM data (quests)
* option to show quest geometries even without selecting a quest pin (ways are shown at intermediate zoom, areas at high zoom)
* option to zoom in/out using volume buttons
* option to switch off 3D buildings
* auto-download happens only if auto-upload is allowed
* quick settings button (+)
  * level filter: only show quests on the chosen level (this may take a few seconds to initialize)
  * temporarily reverse quest order: to show different quests without hiding or changing quest visibility/order
  * quest profile/preset switcher
  * switch between aerial and map backgrounds
* show a hide button when answering a quest
  * click temporarily hides quest until app restart (cannot be undone other than restarting)
  * long click is a shortcut for can't say -> no, just hide
* don't update statistics on upload: they are not counted anyway when uploading with this version (due to different name)
* allow choosing satellite/aerial images as background (will be enabled in StreetComplete once proper implementation is finished)
  * currently disabled as it keeps crashing
* adjust dark theme
  * more contrast for better visibility during the day
  * more natural colors for water, grass and forest areas
  * no fancy style for railways and highways
* (more) transparent buildings: to display indoor paths
* don't let manual download override cache: OSM data will only be re-downloaded if older than 12 hours
* increase map tile cache time from 12 hours to 8 days: map tiles will now not be re-downloaded for 8 days, even if manual download was initiated
* pause upload when manually scanning for quests
* improve location services: higher GPS frequency, use last known location as initial location
* add [requery sqlite library](https://github.com/requery/sqlite-android), which adds 3 MB, but is ca 20% faster than default SQLite
* slightly improve performance when clicking a quest
