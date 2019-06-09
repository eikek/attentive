module Page.Registration.Update exposing (update)

import Api
import Page.Registration.Data exposing (..)
import Data.Registration exposing (Registration)

update: Msg -> Model -> (Model, Cmd Msg)
update msg model =
    case msg of
        SetName s ->
            ({model|nameField = s}, Api.accountExists model.flags s NameExistsResp)
        SetPass s ->
            ({model|passField = s}, Cmd.none)
        SetInvite s ->
            ({model|invField = s}, Cmd.none)

        NameExistsResp (Ok r) ->
            ({model|nameExists = r.exists}, Cmd.none)
        NameExistsResp (Err err) ->
            ({model|nameExists = False}, Cmd.none)

        Register ->
            let
                reg = Registration model.nameField model.passField model.invField
            in
                ({model|regError = Nothing}, Api.register model.flags reg RegisterResp)

        RegisterResp (Ok _) ->
            let
                m = emptyModel model.flags
            in
                ({m|regError = Just False}, Cmd.none)
        RegisterResp (Err err) ->
            ({model|regError = Just True}, Cmd.none)
