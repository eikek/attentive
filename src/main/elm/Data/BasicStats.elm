module Data.BasicStats exposing (..)

import Json.Decode as Decode exposing (Decoder, int, string, float, bool)
import Json.Decode.Pipeline exposing (required, optional, hardcoded)
import Json.Encode as Encode

type alias BasicStats =
    { accounts: Int
    , submissions: Int
    , artists: Int
    , albums: Int
    }

empty: BasicStats
empty =
    { accounts = 0
    , submissions = 0
    , artists = 0
    , albums = 0
    }

decoder: Decoder BasicStats
decoder =
    Decode.succeed BasicStats
        |> required "accounts" int
        |> required "submissions" int
        |> required "artists" int
        |> required "albums" int
