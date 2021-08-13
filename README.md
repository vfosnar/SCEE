This is my personal edition of StreetComplete, including a bunch of modifications:
* renamed: allows install parallel to StreetComplete, and shows a different name in changesets
* show POIs (tap colored dots for details) and adjust quest dot and pin offsets
  * in some rare casesy, existing POIs are not shown. no idea why
* new quests
  * ask for level of shops and other places/things inside mall or retail buildings
  * contact quests for phone and website
  * re-ask/remove surface for badly tagged tracks (by matkoniecz, https://github.com/matkoniecz/Zazolc)
* quest changes
  * allow "paved" surface without note (because one lane concrete, one lane asphalt is too common)
  * ask for more surveillance cameras
  * move "no cycleway" answer to different position (because it's more accessible/convenient)
  * per-quest-settings (only few quests): allows modification of elements the quest is asked for
  * add "private" other answer to track type, way surface and way lit quests
  * add "demolished" other answer to building type quest
* allow creating text notes in a gpx file
  * notes file must be copied manually from Android/data/de.westnordost.streetcomplete.h3/files
* optional volume button zoom
* quest profiles (to be removed with the upcoming better implementation in SC)
* don't let manual download override cache
* auto-download happens only if auto-upload is allowed
* button to temporarily reverse quest order: to show additional quests without hiding or changing quest visibility/order
* hide button when answering a quest (shortcut for can't say -> no, just hide)
* don't update statistics on upload: they are not counted anyway when uploading with this version (due to different name)
* enable satellite images
* adjust dark theme: more contrast for better visiblity during the day, more natural colors for water, grass and forest areas
* improve location services: higher GPS frequency, use last known location as initial location
