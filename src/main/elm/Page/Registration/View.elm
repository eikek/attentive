module Page.Registration.View exposing (view)

import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onInput, onClick)

import Page exposing (Page(..))
import Page.Registration.Data exposing (..)
import Data.RegistrationMode exposing (RegistrationMode(..))
import Data.Flags

view: Model -> Html Msg
view model =
    let
        regmode = Data.Flags.regMode model.flags
    in
    div [class "home-page"]
        [div [class "ui text container"]
             [h1 [class "ui dividing header"][text "Create new account"]
             ,p [][text "Create a new account to start scrobbling."]
             ,div [class "ui error form"]
                  [div [class "field"]
                       [label [][text "Name"]
                       ,input [type_ "text"
                              ,onInput SetName
                              ,value model.nameField
                              ][]
                       ,div [classList [("ui error message", True)
                                       ,("invisible", not model.nameExists)
                                       ]
                            ]
                            [text "The name is already in use."
                            ]
                       ]
                  ,div [class "field"]
                       [label [][text "Password"]
                       ,input [type_ "password"
                              ,onInput SetPass
                              ,value model.passField
                              ][]
                       ]
                  ,div [classList [("field", True)
                                  ,("invisible", regmode == Open)
                                  ]
                       ]
                       [label [][text "Invitation"]
                       ,input [type_ "text"
                              ,onInput SetInvite
                              ,value model.invField
                              ][]
                       ]
                  ,button [class "ui primary button"
                          ,onClick Register
                          ]
                          [text "Submit"
                          ]
                  ]
             ,(resultMessage model)
             ]
        ]

resultMessage: Model -> Html Msg
resultMessage model =
    case model.regError of
        Just True ->
            div [class "ui error message"]
                [p[][text "Registration failed."
                    ]
                ]
        Just False ->
            div [class "ui success message"]
                [p[][text "Registration successful."
                    ]
                ]
        Nothing ->
            span[][]
