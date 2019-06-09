module Page.Login.Update exposing (update)

import Api
import Ports
import Page exposing (Page(..))
import Page.Login.Data exposing (..)
import Data.UserPass exposing (UserPass)
import Data.Account exposing (Account)
import Data.LoginResult exposing (LoginResult)

update: Msg -> Model -> (Model, Cmd Msg)
update msg model =
    case msg of
        SetUsername str ->
            ({model | username = str}, Cmd.none)
        SetPassword str ->
            ({model | password = str}, Cmd.none)

        Authenticate ->
            (model, Api.login model.flags (UserPass model.username model.password) AuthResp)

        AuthResp (Ok lr) ->
            ({model|result = Just lr, password = ""}, setAccount lr)

        AuthResp (Err err) ->
            ({model|password = ""}, Ports.removeAccount "")

setAccount: LoginResult -> Cmd msg
setAccount result =
    if result.success
    then
        Maybe.withDefault "" result.authenticator
            |> Account result.account
            |> \a -> Ports.setAccount (a, (Page.pageToString ProfilePage))
    else
        Ports.removeAccount ""
