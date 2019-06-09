module Data.AccountExists exposing (..)

import Json.Decode as Decode exposing (Decoder, int, string, float, bool)
import Json.Decode.Pipeline exposing (required, optional, hardcoded)
import Json.Encode as Encode

type alias AccountExists =
    { name: String
    , exists: Bool
    }

decoder: Decoder AccountExists
decoder =
    Decode.succeed AccountExists
        |> required "name" string
        |> required "exists" bool
