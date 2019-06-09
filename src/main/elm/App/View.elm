module App.View exposing (view)

import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick)

import App.Data exposing (..)
import Page exposing (Page(..))
import Page.Home.View
import Page.NewInvite.View
import Page.Registration.View
import Page.Login.View
import Page.Profile.View

view: Model -> Html Msg
view model =
    div [class "default-layout"]
        [ div [class "ui fixed top sticky attached large menu black-bg"]
              [div [class "ui fluid container"]
                   [ a [class "header item narrow-item"
                       ,Page.href HomePage
                       ]
                         [i [classList [("lemon outline icon", True)
                                       ]]
                              []
                         ,text model.flags.config.appName]
                   , (loginInfo model)
                   ]
              ]
        , div [ class "ui container main-content" ]
            [ (case model.page of
                   HomePage ->
                       viewHome model
                   NewInvitePage ->
                       viewNewInvite model
                   RegistrationPage ->
                       viewRegistration model
                   LoginPage ->
                       viewLogin model
                   ProfilePage ->
                       viewProfile model
              )
            ]
        , div [ class "ui footer" ]
            [ a [href "https://github.com/eikek/attentive"]
                [ i [class "ui github icon"][]
                ]
            , span []
                  [ text "Attentive "
                  , text model.version.version
                  , text " (#"
                  , String.left 8 model.version.gitCommit |> text
                  , text ")"
                  ]
            ]
        ]

viewProfile: Model -> Html Msg
viewProfile model =
    Html.map ProfileMsg (Page.Profile.View.view model.profileModel)

viewLogin: Model -> Html Msg
viewLogin model =
    Html.map LoginMsg (Page.Login.View.view model.loginModel)

viewHome: Model -> Html Msg
viewHome model =
    Html.map HomeMsg (Page.Home.View.view model.homeModel)

viewNewInvite: Model -> Html Msg
viewNewInvite model =
    Html.map NewInviteMsg (Page.NewInvite.View.view model.newInviteModel)

viewRegistration: Model -> Html Msg
viewRegistration model =
    Html.map RegistrationMsg (Page.Registration.View.view model.registrationModel)

loginInfo: Model -> Html Msg
loginInfo model =
    div [class "right menu"]
        (case model.flags.account of
            Just acc ->
                [a [class "item"
                   ,Page.href ProfilePage
                   ]
                     [text "Profile"
                     ]
                ,a [class "item"
                   ,Page.href model.page
                   ,onClick Logout
                   ]
                     [text "Logout "
                     ,text acc.name
                     ]
                ]
            Nothing ->
                [a [class "item"
                   ,Page.href LoginPage
                   ]
                     [text "Login"
                     ]
                ]
        )
