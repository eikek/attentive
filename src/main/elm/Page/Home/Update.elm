module Page.Home.Update exposing (update)

import Api
import Page.Home.Data exposing (..)

update: Msg -> Model -> (Model, Cmd Msg)
update msg model =
    case msg of
        GetBasicStats ->
            ( model, Api.basicStats model.flags BasicStatsResp )

        BasicStatsResp (Ok stats) ->
            ({ model | stats = stats }, Cmd.none)

        BasicStatsResp (Err err) ->
            (model, Cmd.none)
