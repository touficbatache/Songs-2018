# Songs

A music player for Android that I built and kept improving from 2018 to 2020: browse and search songs, stream them or save them for offline listening, organise playlists and follow along with the lyrics.

Written in Java and Kotlin, with ExoPlayer, Room, Retrofit, Epoxy, Fresco and the Navigation component.

## Features

- **Explore:** curated "New this week" and "Popular" lists.
- **Search:** song search with autocomplete suggestions.
- **Stream or download:** play a song right away, or keep it for offline listening in "My Music".
- **Playlists and likes,** stored locally with Room.
- **Player:** play queue with "Next up" and shuffle, a mini player with progress, and colours taken from the cover art.
- **Media notification:** controls on the lock screen and in the notification shade.
- **Lyrics:** from Genius, plus synced lyrics that follow the song where available.
- **Arabic songs:** an extra provider for Arabic music.

Early design notes (navigation, colour, iconography) are in [docs/design-notes.md](docs/design-notes.md).

## API keys

The app calls the YouTube Data API, Google Custom Search and Last.fm. Keys are not in the repository: add your own to `local.properties` (already gitignored), and the build passes them to the app through `BuildConfig`:

```properties
songs.youtubeApiKey=...
songs.youtubeApiKey2=...
songs.lastfmApiKey=...
songs.cseApiKey=...
songs.cseDndnhaCx=...
songs.cseGeniusCx=...
```

## Status and disclaimer

A personal learning project, no longer developed and never distributed. It builds with the toolchain of its time (Android Gradle Plugin 3.5, Gradle 5.4, SDK 29).

It gets audio through third-party extraction of YouTube streams and lyrics by reading third-party websites. Both may conflict with those services' terms. The code is shared for reference only: it is not affiliated with YouTube, Genius or any music service, and it is not meant to be used to download copyrighted music.

## License

MIT, see [LICENSE](LICENSE). The bundled fonts (PT Sans, Rubik) are under the SIL Open Font License.
