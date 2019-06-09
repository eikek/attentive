module Page.Home.View exposing (view)

import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick)
import Markdown

import Util.List
import Page exposing (Page(..))
import Page.Home.Data exposing (..)
import Data.Flags
import Data.RegistrationMode exposing (RegistrationMode(..))

view: Model -> Html Msg
view model =
    let
        regmode = Data.Flags.regMode model.flags
    in
    div [class "home-page"]
        [h1 [class "ui dividing header"][text "A personal scrobble daemon"]
        ,div [class "ui equal width grid"]
             [div [class "row"]
                  [div [class "ten wide centered column"]
                       [div [class "segment"]
                            [(basicStats model)
                            ]
                       ]
                  ]
             ,div [class "row"]
                  [div [classList [("column", True)
                                  ,("invisible", regmode /= Invite)
                                  ]
                       ]
                       [div [class "ui placeholder segment"]
                            [div [class "ui icon header"]
                                 [i [class "id card outline icon"][]
                                 ,text "Generate new invitations"
                                 ]
                            ,a [(Page.href Page.NewInvitePage)
                               ,class "ui primary button"
                               ]
                               [text "New Invite"
                               ]
                            ]
                       ]
                  ,div [classList [("column", True)
                                  ,("invisible", regmode == Closed)
                                  ]
                       ]
                       [div [class "ui placeholder segment"]
                            [div [class "ui icon header"]
                                 [i [class "user plus icon"][]
                                 ,text "Create a new account"
                                 ]
                            ,a [(Page.href Page.RegistrationPage)
                               ,class "ui primary button"
                               ]
                               [text "Registration"
                               ]
                            ]
                       ]
                  ]
             ,div [class "row"]
                  [div [class "sixteen wide column"]
                       [div [class "ui info message"]
                            [h2 [class "ui header"]
                                [i [class "linkify icon"][]
                                ,div [class "content"]
                                     [text "Scrobble URL"
                                     ,div [class "sub header"]
                                          [code [][text (model.flags.config.base ++ "/scrobble")
                                                  ]
                                          ]
                                     ]
                                ]
                            ]
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
