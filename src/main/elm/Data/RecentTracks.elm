module Data.RecentTracks exposing (..)

import Json.Decode as Decode exposing (Decoder, int, string, float, bool)
import Json.Decode.Pipeline exposing (required, optional, hardcoded)
import Json.Encode as Encode

import Data.Track exposing (Track)

type alias RecentTracks =
    { items: List Track
    }

empty: RecentTracks
empty =
    { items = []
    }

decoder: Decoder RecentTracks
decoder =
    Decode.succeed RecentTracks
        |> required "items" (Decode.list Data.Track.decoder)
