module Page.Login.View exposing (view)

import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick, onInput, onSubmit)

import Page.Login.Data exposing (..)

view: Model -> Html Msg
view model =
    div [class "login-page"]
        [div [class "ui text container"]
             [h1 [class "ui dividing header"][text "Sign in"]
             ,Html.form [class "ui error form", onSubmit Authenticate]
                  [div [class "field"]
                       [label [][text "Username"]
                       ,input [type_ "text"
                              ,onInput SetUsername
                              ,value model.username
                              ][]
                       ]
                  ,div [class "field"]
                       [label [][text "Password"]
                       ,input [type_ "password"
                              ,onInput SetPassword
                              ,value model.password
                              ][]
                       ]
                  ,button [class "ui primary button"
                          ,type_ "submit"
                          ,onClick Authenticate
                          ]
                          [text "Login"
                          ]
                  ]
             ,(resultMessage model)
             ]
        ]

resultMessage: Model -> Html Msg
resultMessage model =
    case model.result of
        Just r ->
            if r.success
            then
                div [class "ui success message"]
                    [text "Login successful."
                    ]
            else
                div [class "ui error message"]
                    [text r.message
                    ]

        Nothing ->
            span [][]
