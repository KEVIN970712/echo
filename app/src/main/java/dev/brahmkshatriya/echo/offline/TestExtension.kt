package dev.brahmkshatriya.echo.offline

import dev.brahmkshatriya.echo.common.clients.ExtensionClient
import dev.brahmkshatriya.echo.common.clients.HomeFeedClient
import dev.brahmkshatriya.echo.common.clients.LoginClient
import dev.brahmkshatriya.echo.common.clients.RadioClient
import dev.brahmkshatriya.echo.common.clients.TrackClient
import dev.brahmkshatriya.echo.common.helpers.PagedData
import dev.brahmkshatriya.echo.common.models.Album
import dev.brahmkshatriya.echo.common.models.Artist
import dev.brahmkshatriya.echo.common.models.EchoMediaItem.Companion.toMediaItem
import dev.brahmkshatriya.echo.common.models.MediaItemsContainer
import dev.brahmkshatriya.echo.common.models.Playlist
import dev.brahmkshatriya.echo.common.models.Streamable
import dev.brahmkshatriya.echo.common.models.Streamable.Audio.Companion.toAudio
import dev.brahmkshatriya.echo.common.models.Streamable.Media.Companion.toAudioVideoMedia
import dev.brahmkshatriya.echo.common.models.Streamable.Media.Companion.toMedia
import dev.brahmkshatriya.echo.common.models.Streamable.Media.Companion.toVideoMedia
import dev.brahmkshatriya.echo.common.models.Tab
import dev.brahmkshatriya.echo.common.models.Track
import dev.brahmkshatriya.echo.common.models.User
import dev.brahmkshatriya.echo.common.settings.Setting
import dev.brahmkshatriya.echo.common.settings.Settings
import dev.brahmkshatriya.echo.plugger.ExtensionMetadata
import dev.brahmkshatriya.echo.plugger.ImportType

class TestExtension : ExtensionClient, LoginClient.UsernamePassword, TrackClient, HomeFeedClient,
    RadioClient {

    companion object {
        val metadata = ExtensionMetadata(
            "TestExtension",
            "",
            ImportType.Inbuilt,
            "test",
            "Test Extension",
            "1.0.0",
            "Test extension for offline testing",
            "Test",
            null,
        )
    }

    override suspend fun onExtensionSelected() {}
    override val settingItems: List<Setting> = emptyList()
    override fun setSettings(settings: Settings) {}
    override suspend fun onLogin(username: String, password: String): List<User> {
        return listOf(User(username, username, null))
    }

    override suspend fun onSetLoginUser(user: User?) {
        println("onSetLoginUser: $user")
    }

    override suspend fun getCurrentUser(): User? = null
    override suspend fun loadTrack(track: Track) = track
    override suspend fun getStreamableMedia(streamable: Streamable): Streamable.Media {
        return when (streamable.mediaType) {
            Streamable.MediaType.Audio -> streamable.id.toAudio().toMedia()
            Streamable.MediaType.Video -> streamable.id.toVideoMedia()
            Streamable.MediaType.AudioVideo -> streamable.id.toAudioVideoMedia()
        }
    }

    override fun getMediaItems(track: Track): PagedData<MediaItemsContainer> = PagedData.Single {
        emptyList()
    }

    override suspend fun getHomeTabs() = listOf<Tab>()

    private val audio =
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"

    private val video =
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"

    private fun createTrack(id: String, title: String, streamables: List<Streamable>) = Track(
        id,
        title,
        emptyList(),
        null,
        null,
        streamables = streamables
    ).toMediaItem().toMediaItemsContainer()

    override fun getHomeFeed(tab: Tab?): PagedData<MediaItemsContainer> = PagedData.Single {
        listOf(
            createTrack("audio", "Audio", listOf(Streamable.audio(audio, 0))),
            createTrack("video", "Video", listOf(Streamable.video(video, 0))),
            createTrack(
                "both", "Both", listOf(Streamable.audio(audio, 0), Streamable.video(video, 0))
            ),
            createTrack(
                "audioVideo", "Audio Video", listOf(Streamable.audioVideo(audio, 0))
            )
        )
    }

    private val emptyPlaylist = Playlist("empty", "empty", false)
    override suspend fun radio(track: Track) = emptyPlaylist
    override suspend fun radio(album: Album) = emptyPlaylist
    override suspend fun radio(artist: Artist) = emptyPlaylist
    override suspend fun radio(user: User) = emptyPlaylist
    override suspend fun radio(playlist: Playlist) = emptyPlaylist
    override suspend fun loadPlaylist(playlist: Playlist) = playlist
    override fun loadTracks(playlist: Playlist): PagedData<Track> = PagedData.Single {
        listOf(
            Track(
                "track",
                "Track",
                emptyList(),
                null,
                null,
                streamables = listOf(Streamable.audioVideo(video, 0))
            )
        )
    }

    override fun getMediaItems(playlist: Playlist): PagedData<MediaItemsContainer> =
        PagedData.Single { listOf() }
}