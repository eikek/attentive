module App.Update exposing (update, initPage)

import Api
import Ports
import Browser exposing (UrlRequest(..))
import Browser.Navigation as Nav
import Url
import App.Data exposing (..)
import Page exposing (Page(..))
import Page.Home.Data
import Page.Home.Update
import Page.NewInvite.Data
import Page.NewInvite.Update
import Page.Registration.Data
import Page.Registration.Update
import Page.Login.Data
import Page.Login.Update
import Page.Profile.Data
import Page.Profile.Update

update: Msg -> Model -> (Model, Cmd Msg)
update msg model =
    case msg of
        HomeMsg lm ->
            updateHome lm model

        NewInviteMsg dm ->
            updateNewInvite dm model

        RegistrationMsg cm ->
            updateRegistration cm model

        LoginMsg lm ->
            updateLogin lm model

        ProfileMsg pm ->
            updateProfile pm model

        SetPage p ->
            ( {model | page = p }
            , Cmd.none
            )

        VersionResp (Ok info) ->
            ({model|version = info}, Cmd.none)

        VersionResp (Err err) ->
            (model, Cmd.none)

        Logout ->
            (model, Api.logout model.flags LogoutResp)
        LogoutResp _ ->
            ({model|loginModel = Page.Login.Data.empty model.flags}, Ports.removeAccount (Page.pageToString HomePage))
        SessionCheckResp res ->
            case res of
                Ok lr ->
                    if (lr.success) then (model, Cmd.none)
                    else  (model, Ports.removeAccount (Page.pageToString LoginPage))
                Err _ -> (model, Ports.removeAccount (Page.pageToString LoginPage))

        NavRequest req ->
            case req of
                Internal url ->
                    let
                        isCurrent =
                            Page.fromUrl url |>
                            Maybe.map (\p -> p == model.page) |>
                            Maybe.withDefault True
                    in
                        ( model
                        , if isCurrent then Cmd.none else Nav.pushUrl model.key (Url.toString url)
                        )

                External url ->
                    ( model
                    , Nav.load url
                    )

        NavChange url ->
            let
                page = Page.fromUrl url |> Maybe.withDefault HomePage
                (m, c) = initPage model page
            in
            ( { m | page = page }, c )

updateProfile: Page.Profile.Data.Msg -> Model -> (Model, Cmd Msg)
updateProfile pm model =
    let
        (lm, lc) = Page.Profile.Update.update pm model.profileModel
    in
        ({model| profileModel = lm}
        ,Cmd.map ProfileMsg lc
        )

updateLogin: Page.Login.Data.Msg -> Model -> (Model, Cmd Msg)
updateLogin lmsg model =
    let
        (lm, lc) = Page.Login.Update.update lmsg model.loginModel
    in
        ({model | loginModel = lm}
        ,Cmd.map LoginMsg lc
        )

updateHome: Page.Home.Data.Msg -> Model -> (Model, Cmd Msg)
updateHome lmsg model =
    let
        (lm, lc) = Page.Home.Update.update lmsg model.homeModel
    in
        ( {model | homeModel = lm }
        , Cmd.map HomeMsg lc
        )

updateNewInvite: Page.NewInvite.Data.Msg -> Model -> (Model, Cmd Msg)
updateNewInvite dmsg model =
    let
        (dm, dc) = Page.NewInvite.Update.update dmsg model.newInviteModel
    in
        ( {model | newInviteModel = dm }
        , Cmd.map NewInviteMsg dc
        )

updateRegistration: Page.Registration.Data.Msg -> Model -> (Model, Cmd Msg)
updateRegistration cmsg model =
    let
        (cm, cc) = Page.Registration.Update.update cmsg model.registrationModel
    in
        ( {model| registrationModel = cm }
        , Cmd.map RegistrationMsg cc
        )

initPage: Model -> Page -> (Model, Cmd Msg)
initPage model page =
    case page of
        NewInvitePage ->
            (model, Cmd.none)

        HomePage ->
            updateHome Page.Home.Data.GetBasicStats model

        RegistrationPage ->
            (model, Cmd.none)

        LoginPage ->
            (model, Cmd.none)

        ProfilePage ->
            updateProfile Page.Profile.Data.GetData model
