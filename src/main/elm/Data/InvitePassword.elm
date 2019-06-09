module Data.InvitePassword exposing (..)

import Json.Decode as Decode exposing (Decoder, int, string, float, bool)
import Json.Decode.Pipeline exposing (required, optional, hardcoded)
import Json.Encode as Encode

type alias InvitePassword =
    { password: String
    }

decoder: Decoder InvitePassword
decoder =
    Decode.succeed InvitePassword
        |> required "password" string

encode: InvitePassword -> Encode.Value
encode ip =
    Encode.object
        [ ("password", Encode.string ip.password)
        ]
