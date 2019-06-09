module Page.Profile.Update exposing (update)

import Api
import Page.Profile.Data exposing (..)
import Data.Account exposing (Account)
import Data.Flags exposing (Flags)
import Data.RecentTracks exposing (RecentTracks)
import Data.BasicStats exposing (BasicStats)

update: Msg -> Model -> (Model, Cmd Msg)
update msg model =
    case msg of
        GetData ->
            (model
            , Cmd.batch
                [ withCmd model (Api.getNowPlaying NowPlayingResp)
                , withCmd model (Api.getRecentTracks RecentTracksResp)
                , withCmd model (Api.getUserStats BasicStatsResp)
                ]
            )

        NowPlayingResp (Ok np) ->
            ({model|nowPlaying = Just np}, Cmd.none)
        NowPlayingResp (Err err) ->
            ({model|nowPlaying = Nothing}, Cmd.none)

        RecentTracksResp (Ok rt) ->
            ({model|recent = rt}, Cmd.none)
        RecentTracksResp (Err err) ->
            ({model|recent = Data.RecentTracks.empty}, Cmd.none)

        BasicStatsResp (Ok s) ->
            ({model|stats = s}, Cmd.none)
        BasicStatsResp (Err err) ->
            ({model|stats = Data.BasicStats.empty}, Cmd.none)

withCmd: Model -> (Flags -> Account -> Cmd msg) -> Cmd msg
withCmd model f =
    case model.flags.account of
        Just acc -> f model.flags acc
        Nothing -> Cmd.none
