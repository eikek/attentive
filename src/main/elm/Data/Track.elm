module Data.Track exposing (..)

import Json.Decode as Decode exposing (Decoder, int, string, float, bool)
import Json.Decode.Pipeline exposing (required, optional, hardcoded)
import Json.Encode as Encode

type alias Track =
    { title: String
    , track: Int
    , artist: String
    , album: String
    , length: Int
    , created: String
    , createdMillis: Int
    }

empty: Track
empty =
    { title = ""
    , track = 0
    , artist = ""
    , album = ""
    , length = 0
    , created = ""
    , createdMillis = 0
    }

decoder: Decoder Track
decoder =
    Decode.succeed Track
        |> required "title" string
        |> required "track" int
        |> required "artist" string
        |> required "album" string
        |> required "length" int
        |> required "created" string
        |> required "createdMillis" int
