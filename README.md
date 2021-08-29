This is my personal edition of StreetComplete, including a bunch of modifications:
* renamed: allows install parallel to StreetComplete, and shows a different name in changesets
* show POIs (tap colored dots for details) and adjust quest dot and pin offsets
  * in some rare cases, existing POIs are not shown. No idea why...
* new quests
  * ask for level of shops and other places/things inside buildings (if there are at least 5 candidates, and at least one has a level tagged)
  * contact quests for phone and website
  * re-ask/remove surface for badly tagged tracks (by matkoniecz, https://github.com/matkoniecz/Zazolc)
  * service building operator (no proper auto-complete list like ATM operator)
  * service building type (currently can only answer substation or hide)
* quest changes
  * allow "paved" surface without note (because one lane concrete, one lane asphalt is too common)
  * ask for more surveillance cameras
  * move "no cycleway" answer to different position (because it's more accessible/convenient)
  * per-quest-settings (only few quests): allows modification of elements the quest is asked for
    * this does not take effect immediately, but only on newly created quests
    * additionally for most quests it is necessary to restart the app to actually use changed quest-settings
  * add "private" other answer to track type, way surface and way lit quests
  * add "demolished" other answer to building type quest
  * some additional building types
  * add "zebra" answer to crossing quest
  * ask for step count only for steps shorter than 15 m
  * building type quest is split into two quests
    * one for buildings that definitely have an address
    * one for buildings that probably have no address (not using the entire address quest logic, so some buildings may actually have an address)
    * for some reason the added quest icon for buildings with address is not displayed on the map, so the quest shows as large light blue dots (like the dots for POIs)
* allow creating text notes in a gpx file
  * notes file must be copied manually from Android/data/de.westnordost.streetcomplete.h3/files
* optional volume button zoom
* don't let manual download override cache
* auto-download happens only if auto-upload is allowe
* quick settings button (+)
  * level filter: only show quests on the chosen level (this may take a few seconds to initialize)
  * temporarily reverse quest order: to show different quests without hiding or changing quest visibility/order
  * quest profile/preset switcher
* hide button when answering a quest (shortcut for can't say -> no, just hide)
* don't update statistics on upload: they are not counted anyway when uploading with this version (due to different name)
* enable satellite images
* adjust dark theme: more contrast for better visiblity during the day, more natural colors for water, grass and forest areas
  * make buildings more transparent: to display indoor paths
* improve location services: higher GPS frequency, use last known location as initial location
* switch off 3D buildings: allows to better determine position of quests inside or close to buildings
  * currently no option to re-enable, don't know how to do this
* increase map tile cache time from 12 hours to 8 days: tiles will now not be re-downloaded for 8 days, even if manual download was initiated

Most of these modifications are not thoroughly testes, and some even have known bugs.
