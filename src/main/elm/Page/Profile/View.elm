module Page.Profile.View exposing (view)

import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick)

import Util.Time
import Util.Duration
import Page exposing (Page(..))
import Page.Profile.Data exposing (..)
import Data.Track exposing (Track)
import Data.RecentTracks exposing (RecentTracks)

view: Model -> Html Msg
view model =
    div [class "profile-page"]
        [div [class "ui grid"]
             [div [class "row"]
                  [div [class "ten wide centered column"]
                       [(basicStats model)
                       ]
                  ]
             ,div [class "row"]
                  [div [class "sixteen wide column"]
                       [(withData model nowPlaying nonePlaying)
                       ]
                  ]

             ,div [class "row"]
                  [div [class "sixteen wide column"]
                       [(recentTracks model.recent)
                       ]
                  ]
             ]
        ]

basicStats: Model -> Html Msg
basicStats model =
    div [class "ui statistics"]
        [div [class "statistic"]
             [div [class "value"]
                  [model.stats.submissions |> String.fromInt |> text
                  ]
             ,div [class "label"]
                  [text "Srcobbled Songs"
                  ]
             ]
        ,div [class "statistic"]
             [div [class "value"]
                  [model.stats.artists |> String.fromInt |> text
                  ]
             ,div [class "label"]
                  [text "Srcobbled Artists"
                  ]
             ]
        ,div [class "statistic"]
             [div [class "value"]
                  [model.stats.albums |> String.fromInt |> text
                  ]
             ,div [class "label"]
                  [text "Srcobbled Albums"
                  ]
             ]
        ]

recentTracks: RecentTracks -> Html Msg
recentTracks tracks =
    let
        trackItem: Track -> Html Msg
        trackItem track =
            div [class "row"]
                [div [class "ten wide column"]
                     [span [class "track-item title"]
                           [text track.title
                           ]
                     ,br[][]
                     ,span [class "track-item-fill"]
                           [text " by "
                           ]
                     ,span [class "track-item artist"]
                           [text track.artist
                           ]
                     ,br[][]
                     ,span [class "track-item-fill"]
                           [text "track "
                           ]
                     ,span [class "track-item track-number"]
                           [String.fromInt track.track |> text
                           ]
                     ,span [class "track-item-fill"]
                           [text " on album "
                           ]
                     ,span [class "track-item album"]
                           [text track.album
                           ]
                     ,br[][]
                     ,span [class "track-item-fill"]
                           [text "length "
                           ]
                     ,span [class "track-item track-length"]
                           [Util.Duration.fromSeconds track.length |> text
                           ]
                     ]
                ,div [class "six wide right aligned column"]
                     [span [class "track-item submitted"]
                          [Util.Time.formatDateTime track.createdMillis |> text
                          ]
                     ]
                ]
    in
        div [class "ui blue segment"]
            [div [class "ui grid container"]
                 ([div [class "row"]
                      [h2 [class "ui header"]
                          [text "Recent Tracks"
                          ]
                      ]
                 ] ++ (List.map trackItem tracks.items))

            ]




nowPlaying: Track -> Html Msg
nowPlaying track =
    div [class "ui segments"]
        [div [class "ui basic blue segment"]
             [h2 [class "ui header"]
                 [i [class "ui music icon"][]
                 ,text "Now Playing"
                 ]
             ]
        ,div [class "ui basic segment"]
             [h4 [class "ui header"]
                 [text track.title
                 ,div [class "sub header"]
                      [text "Title"
                      ]
                 ]
             ,h4 [class "ui header"]
                 [text track.artist
                 ,div [class "sub header"]
                      [text "Artist"
                      ]
                 ]
             ,h4 [class "ui header"]
                 [text track.album
                 ,div [class "sub header"]
                      [text "Album, Track "
                      ,text (String.fromInt track.track)
                      ]
                 ]
             ]
        ]

nonePlaying: Html Msg
nonePlaying =
    div [class "ui basic blue segments"]
        [div [class "ui basic blue segment"]
             [h2 [class "ui header"]
                 [i [class "ui music icon"][]
                 ,text "Now Playing"
                 ]
             ]
        ]

withData: Model -> (Track -> Html msg) -> Html msg -> Html msg
withData model f default =
    case model.nowPlaying of
        Just np -> f np
        Nothing -> default
