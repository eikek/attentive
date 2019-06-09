module Data.Invitation exposing (..)

import Json.Decode as Decode exposing (Decoder, int, string, float, bool)
import Json.Decode.Pipeline exposing (required, optional, hardcoded)
import Json.Encode as Encode

type alias Invitation =
    { key: String
    , validMs: Int
    }

decoder: Decoder Invitation
decoder =
    Decode.succeed Invitation
        |> required "key" string
        |> required "validMs" int
