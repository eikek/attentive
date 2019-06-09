module Page.NewInvite.View exposing (view)

import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick, onInput)

import Page exposing (Page(..))
import Page.NewInvite.Data exposing (..)

view: Model -> Html Msg
view model =
    div [class "newinvite-page"]
        [div [class "ui text container"]
             [h1 [class "ui dividing header"][text "New Invitation"]
             ,p[][text "A user that wants to create an account must provide an invitation key."
                 ,text "This key can be generated here. It is a one-time key."
                 ]
             ,p [][text "To generate an invitation key, the invitation password is required."
                  ,text "This has been configured in the configuration file."
                  ]
             ,div [class "ui form"]
                  [div [class "field"]
                       [label [][text "Invitation Key Password"]
                       ,input [type_ "text"
                              ,value model.invpass
                              ,onInput SetPassword
                              ][]
                       ]
                  ,button [class "ui primary button"
                          ,onClick Generate
                          ]
                       [text "Generate"
                       ]
                  ]
             ,(successMessage model)
             ,(errorMessage model)
             ]
        ]

successMessage: Model -> Html Msg
successMessage model =
    case model.invite of
        Just inv ->
            div [class "ui success message"]
                [p[][text "Invitation key: "
                    ,code [][text inv.key
                            ]
                    ]
                ]
        Nothing ->
            span[][]

errorMessage: Model -> Html Msg
errorMessage model =
    case model.error of
        Just m ->
            div [class "ui error message"]
                [p [][text m]
                ]
        Nothing ->
            span [][]
