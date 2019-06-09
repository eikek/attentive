module Page.NewInvite.Update exposing (update)

import Api
import Ports
import Page.NewInvite.Data exposing (..)
import Data.InvitePassword exposing (InvitePassword)

update: Msg -> Model -> (Model, Cmd Msg)
update msg model =
    case msg of
        SetPassword text ->
            ({model|invpass = text}, Cmd.none)

        Generate ->
            ({model|process = True}, Api.generateInvite model.flags (InvitePassword model.invpass) GenerateResp)

        GenerateResp (Ok inv) ->
            ({model|process = False, invite = Just inv, error = Nothing}, Cmd.none)

        GenerateResp (Err err) ->
            let
                _ = Debug.log "Error:" err
            in
                ({model|process = False, error = Just "Generating key failed", invite = Nothing}, Cmd.none)
