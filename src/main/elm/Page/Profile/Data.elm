module Page.Profile.Data exposing (..)

import Http
import Data.Flags exposing (Flags)
import Data.Track exposing (Track)
import Data.RecentTracks exposing (RecentTracks)
import Data.BasicStats exposing (BasicStats)

type alias Model =
    { flags: Flags
    , nowPlaying: Maybe Track
    , recent: RecentTracks
    , stats: BasicStats
    }

empty: Flags -> Model
empty flags =
    { flags = flags
    , nowPlaying = Nothing
    , recent = Data.RecentTracks.empty
    , stats = Data.BasicStats.empty
    }

type Msg
    = GetData
    | NowPlayingResp (Result Http.Error Track)
    | RecentTracksResp (Result Http.Error RecentTracks)
    | BasicStatsResp (Result Http.Error BasicStats)
